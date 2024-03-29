package com.core.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.core.download.Downloader.Status.Existed
import com.core.download.Downloader.Status.Progress
import com.core.download.Downloader.Status.Failed
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import com.core.log.Log.Companion.log

class Downloader private constructor(private val mContext: Context) {
    private val listeners: MutableList<Listener> = ArrayList()
    private val uiHandler = Handler(Looper.getMainLooper())
    private val backHandler: Handler
    private lateinit var downloadManager: DownloadManager
    private val tasks: MutableList<Task> = ArrayList()

    fun addTask(task: Task) {
        backHandler.post {
            synchronized(tasks) {
                if (!hasTask(task.downloadUrl)) {
                    try {
                        doAddTask(task)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        log.e(e)
                    }
                }
            }
        }
    }

    fun hasTask(downloadUrl: String): Boolean {
        for (task in tasks) {
            if (task.downloadUrl == downloadUrl) {
                return true
            }
        }
        return false
    }

    private fun doAddTask(task: Task) {
        val file = File(task.getFileTargetDestination(mContext))
        if (file.exists()) {
            if (!task.isReDownloadWhenExist) {
                log.w("target file[${file.absolutePath}] exist")
                notifyListener(Existed(task))
                return
            }
        }
        val request = DownloadManager.Request(Uri.parse(task.downloadUrl))
        request.setAllowedOverRoaming(false)
        request.setMimeType(
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(
                        task.downloadUrl
                    )
                )
        )

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(if (task.isShowNotification) DownloadManager.Request.VISIBILITY_VISIBLE else DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setVisibleInDownloadsUi(task.isShowNotification)
        val subPath: String = if (TextUtils.isEmpty(task.subDir)) {
            task.fileName
        } else {
            task.subDir + File.separator + task.fileName
        }
        if (task.isFileSaveToPublicStorage) {
            request.setDestinationInExternalPublicDir(task.dirType, subPath)
        } else {
            request.setDestinationInExternalFilesDir(mContext, task.dirType, subPath)
        }
        task.downloadId = downloadManager.enqueue(request)
        tasks.add(task)
        log.d("add download task：${Task.toString(mContext, task)}")
        checkMonitorDownloadStatus()
    }

    fun removeTask(task: Task, removeFile: Boolean) {
        backHandler.post { synchronized(tasks) { doRemoveTask(task, removeFile) } }
    }

    private fun doRemoveTask(task: Task, removeFile: Boolean) {
        for (t in tasks) {
            if (t.downloadId == task.downloadId) {
                tasks.remove(t)
                if (removeFile) {
                    downloadManager.remove(t.downloadId)
                }
                log.d("remove download task：${Task.toString(mContext, t)}")
                break
            }
        }
    }

    private fun checkMonitorDownloadStatus() {
        backHandler.removeCallbacks(mCheckDownloadStatusRunn)
        if (tasks.isEmpty()) {
            log.w("no download task exist, not loop check download status")
        } else {
            backHandler.postDelayed(mCheckDownloadStatusRunn, 1000)
        }
    }

    private val mCheckDownloadStatusRunn = Runnable {
        synchronized(tasks) {
            val tasksSuccess: MutableList<Task> = ArrayList()
            val tasksFailed: MutableList<Task> = ArrayList()
            val query = DownloadManager.Query()
            for (task in tasks) {
                query.setFilterById(task.downloadId)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    var reason: Int
                    when (status) {
                        DownloadManager.STATUS_PAUSED -> {
                            reason =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            log.w(
                                "download paused:${
                                    Task.toString(
                                        mContext,
                                        task
                                    )
                                }, reason:${reason}"
                            )
                            log.w("download delayed:${Task.toString(mContext, task)}")
                            val soFar =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            log.v(
                                "downloading:${
                                    Task.toString(
                                        mContext,
                                        task
                                    )
                                },progress:$soFar/$total"
                            )
                            notifyListener(Progress(task, soFar, total))
                        }

                        DownloadManager.STATUS_PENDING -> {
                            log.w("download delayed:${Task.toString(mContext, task)}")
                            val soFar =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            log.v(
                                "downloading:${
                                    Task.toString(
                                        mContext,
                                        task
                                    )
                                },progress:${soFar}/${total}"
                            )
                            notifyListener(Progress(task, soFar, total))
                        }

                        DownloadManager.STATUS_RUNNING -> {
                            val soFar =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            log.v(
                                "downloading:${
                                    Task.toString(
                                        mContext,
                                        task
                                    )
                                },progress:${soFar}/${total}"
                            )
                            notifyListener(Progress(task, soFar, total))
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val destinationUri = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    DownloadManager.COLUMN_LOCAL_URI
                                )
                            )
                            task.fileRealDestinationUri = destinationUri
                            log.i("download success:${Task.toString(mContext, task)}")
                            tasksSuccess.add(task)
                            notifyListener(Status.Success(task))
                        }

                        DownloadManager.STATUS_FAILED -> {
                            reason =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            log.e(
                                "download failed:${
                                    Task.toString(
                                        mContext,
                                        task
                                    )
                                }, reason:${reason}"
                            )
                            tasksFailed.add(task)
                            notifyListener(Failed(task))
                        }
                    }
                }
            }
            for (task in tasksSuccess) {
                doRemoveTask(task, false)
            }
            for (task in tasksFailed) {
                doRemoveTask(task, true)
            }
            checkMonitorDownloadStatus()
        }
    }

    init {
        val thread = HandlerThread("xdownloader")
        thread.start()
        backHandler = Handler(thread.looper)
        downloadManager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    class Task(
        /**
         * 下载地址
         */
        val downloadUrl: String,
        /**
         * 文件夹类型
         */
        val dirType: String,
        /**
         * 子目录
         */
        val subDir: String,
        /**
         * 保存文件的文件名
         */
        var fileName: String,
        /**
         * 是否显示在通知栏
         */
        val isShowNotification: Boolean,
        /**
         * 如果存在是否重新下载（android10之后无法删除下载目录里面的文件，所以会设置一个新的文件名）
         */
        val isReDownloadWhenExist: Boolean
    ) {
        /**
         * 下载id由DownloadManager生成
         */
        var downloadId: Long = 0

        /**
         * true,下载的文件存储在公共文件夹里面（此时mDirType只能是系统支持的那几个）；false,下载的文件存储在app专属的文件夹里面
         */
        var isFileSaveToPublicStorage = false

        /**
         * 文件如果已经存在，重复下载文件名字可能会修改，这个变量保存的是最终存储的位置
         */
        var fileRealDestinationUri: String? = null
        var type = 0
            private set
        var extra: Any? = null
            private set

        fun setType(type: Int): Task {
            this.type = type
            return this
        }

        fun setExtra(extra: Any?): Task {
            this.extra = extra
            return this
        }

        fun getFileTargetDestination(context: Context): String {
            val subPath =
                (if (TextUtils.isEmpty(subDir)) "" else subDir + File.separator) + fileName
            return if (isFileSaveToPublicStorage) {
                Environment.getExternalStoragePublicDirectory(dirType)
                    .toString() + File.separator + subPath
            } else {
                context.getExternalFilesDir(dirType)!!.absolutePath + File.separator + subPath
            }
        }

        companion object {
            fun createInstance(downloadUrl: String, subDir: String, fileName: String): Task {
                return Task(
                    downloadUrl,
                    Environment.DIRECTORY_DOWNLOADS, subDir,
                    fileName, isShowNotification = false, isReDownloadWhenExist = true
                )
            }

            fun toString(context: Context, task: Task): String {
                val sb = StringBuilder()
                sb.append("id=").append(task.downloadId)
                sb.append(",").append("url=").append(task.downloadUrl)
                sb.append(",").append("target=").append(task.getFileTargetDestination(context))
                if (!TextUtils.isEmpty(task.fileRealDestinationUri)) {
                    sb.append(",").append("real=").append(task.fileRealDestinationUri)
                }
                return sb.toString()
            }
        }
    }

    open class Status(val task: Task) {

        class Progress(task: Task, val downloadSize: Int, val totalSize: Int) : Status(task)
        class Success(task: Task) : Status(task)
        class Failed(task: Task) : Status(task)
        class Existed(task: Task) : Status(task)
    }

    fun addListener(listener: Listener) {
        synchronized(listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    fun removeListener(listener: Listener) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    private fun notifyListener(status: Status) {
        uiHandler.post {
            synchronized(listeners) {
                val s = listeners.size
                for (i in 0 until s) {
                    if (listeners[i].onDownload(status)) {
                        break
                    }
                }
            }
        }
    }

    interface Listener {
        fun onDownload(status: Status?): Boolean
    }

    companion object {

        lateinit var downloader: Downloader

        fun init(context: Context) {
            downloader = Downloader(context)
        }
    }
}