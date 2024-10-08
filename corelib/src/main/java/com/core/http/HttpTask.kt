package com.core.http

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.core.log.Log
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import com.core.http.CustomTrust.setTrust
import com.core.http.CustomTrust.trustAllCerts
import okhttp3.Request
import okhttp3.MultipartBody
import okhttp3.FormBody
import okhttp3.Call
import okhttp3.Response
import kotlin.jvm.JvmOverloads
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Exception

class HttpTask(
    var url: String? = null,
    var method: String = "",
    var params: Map<String, Any>? = null,
    var responseClass: Class<*>? = null,
    var backParam: Any? = null,
    var backgroundBeforeListener: Listener? = null,
    var beforeListener: Listener? = null,
    var listener: Listener? = null,
    var afterListener: Listener? = null,
    var defaultFileExtension: String = "jpg",
    var globalDeal: Boolean = true,
    var noCommonParam: Boolean = false,
) {

    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    init {
        if (url == null) {
            url = if (dynamicUrl) dynamicUrlListener?.onGetDynamicUrl() else defaultUrl
        }
    }

    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        const val FAILUE = -1
        const val NETWORK_INVALID = -2
        var SYSTEM_ERROR = "system error"
        var NETWORK_ERROR = "network error"
        const val NETWORK_ERROR_CASE = "Failed to connect to"
        const val NETWORK_ERROR_CASE_1 = "Connection reset"
        val NETWORK_ERROR_CASE_LIST: MutableList<String> = ArrayList()
        private var debug = false
        private lateinit var context: Application
        private lateinit var okHttpClient: OkHttpClient
        private var gson: Gson? = null
        private var headerBodyListener: HeaderBodyListener? = null
        private var errorListener: ErrorListener? = null
        private lateinit var checkResponseListener: CheckResponseListener
        var realExceptionListener: RealExceptionListener? = null
        var dynamicUrlListener: DynamicUrlListener? = null
        var clientServerTimeDiffListener: ClientServerTimeDiffListener? = null
        var defaultUrl: String? = null
        private var timeDiff: Long = 0
        var log = Log()
        private var responseClass: Class<*>? = null
        private var type = Type.RAW_METHOD_APPEND_URL
        var dynamicUrl = false

        @JvmOverloads
        fun init(
            isDebug: Boolean,
            context: Application,
            url: String,
            headerBodyListener: HeaderBodyListener?,
            errorListener: ErrorListener?,
            checkResponseListener: CheckResponseListener,
            certificateAssetsName: String?,
            type: Type = Type.RAW_METHOD_APPEND_URL,
            param: Param = Param()
        ) {
            debug = isDebug
            log.filterTag = "[HttpTask]"
            log.isEnabled = isDebug
            Companion.context = context
            this.defaultUrl = url
            this.headerBodyListener = headerBodyListener
            this.errorListener = errorListener
            this.checkResponseListener = checkResponseListener
            val builder = OkHttpClient.Builder()
            val loggingInterceptor = HttpLoggingInterceptor { message -> log.jsonV(message) }
            loggingInterceptor.setLevel(if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
            builder.addInterceptor(loggingInterceptor)
            builder.connectTimeout(param.mConnectTimeout.toLong(), TimeUnit.SECONDS)
            builder.readTimeout(param.mReadTimeout.toLong(), TimeUnit.SECONDS)
            builder.writeTimeout(param.mWriteTimeout.toLong(), TimeUnit.SECONDS)
            try {
                if (url.startsWith("https")) {
                    if (!TextUtils.isEmpty(certificateAssetsName)) {
                        val inputStream = context.assets.open(certificateAssetsName!!)
                        setTrust(builder, inputStream)
                    } else {
                        log.w("notice that you choose trust all certificates")
                        trustAllCerts(builder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log.e(e)
            }
            okHttpClient = builder.build()
            gson = Gson()
            this.headerBodyListener?.onInit(Companion.context)
            this.type = type
            NETWORK_ERROR_CASE_LIST.add(NETWORK_ERROR_CASE)
            NETWORK_ERROR_CASE_LIST.add(NETWORK_ERROR_CASE_1)
        }

        private fun calculateTimeDiff(response: Response) {
            val dateStr = response.header("Date")
            dateStr?.let {
                try {
                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(dateStr)
                        ?.let { date ->
                            timeDiff = System.currentTimeMillis() - date.time
                            clientServerTimeDiffListener?.onClientServerTimeDiff(timeDiff)
                            log.v("local and server time differ [${timeDiff}]")
                        }
                } catch (e: Exception) {
                    timeDiff = 0
                    e.printStackTrace()
                    log.e(e)
                }
            }
        }

        val serverCurrentTimeMillis: Long
            get() = System.currentTimeMillis() - timeDiff
    }


    private var files: Map<String, String>? = null
    private var headers: Map<String, String>? = null
    private var wrListener: WeakReference<Listener>? = null
    private var weakReferenceListener = false
    private var bodyType = BodyType.POST
    private var startTimestamp: Long = 0

    private enum class BodyType {
        GET, POST, UPLOAD, PATCH, DELETE
    }

    enum class Type {
        RAW_METHOD_APPEND_URL, FORM_METHOD_IN_FORMBODY
    }

    fun get(): HttpTask {
        bodyType = BodyType.GET
        return this
    }

    fun post(): HttpTask {
        bodyType = BodyType.POST
        return this
    }

    fun patch(): HttpTask {
        bodyType = BodyType.PATCH
        return this
    }

    fun delete(): HttpTask {
        bodyType = BodyType.DELETE
        return this
    }

    fun upload(files: Map<String, String>?): HttpTask {
        bodyType = BodyType.UPLOAD
        this.files = files
        noCommonParam = true
        return this
    }

    fun weakRefListener() {
        listener?.let {
            wrListener = WeakReference(listener)
            listener = null
        }
    }

    private fun getRequest(): Request? {
        try {
            if (headerBodyListener != null && !noCommonParam) {
                params = headerBodyListener!!.onGetBody(method, params)
            }
            if (params == null) {
                params = TreeMap()
            }
            val reqBuilder = Request.Builder()
            if (headerBodyListener != null) {
                val headers = headerBodyListener!!.onGetHeaders(
                    method, params
                )
                this.headers = headers
                if (!headers.isNullOrEmpty()) {
                    for ((key, value) in headers) {
                        reqBuilder.addHeader(key, value)
                    }
                }
            }
            if (bodyType == BodyType.UPLOAD) {
                val bodyBuilder: MultipartBody.Builder = MultipartBody.Builder().setType(
                    MultipartBody.FORM
                )
                if (files == null || files!!.isEmpty()) {
                    log.e("no file!")
                    return null
                }
                var file: File
                val params = files!!.entries.iterator()
                var param: Map.Entry<String, String>
                var filePath: String
                while (params.hasNext()) {
                    param = params.next()
                    filePath = param.value
                    file = File(filePath)
                    if (!file.exists()) {
                        log.w("file[$filePath] not exist")
                        continue
                    }
                    val key = param.key
                    var fileExtension = getFileExtensionFromPath(filePath)
                    var mineType = getMimeTypeFromExtension(fileExtension)
                    var fileName: String
                    if (!TextUtils.isEmpty(mineType)) {
                        fileName = "$key.$fileExtension"
                    } else {
                        fileExtension = defaultFileExtension
                        mineType = getMimeTypeFromExtension(fileExtension)
                        fileName = "$key.$fileExtension"
                    }
                    log.d("add upload file[$key], key,fileName[$fileName],fileExtension[$fileExtension],mineType[$mineType]")
                    bodyBuilder.addFormDataPart(
                        key, fileName, RequestBody.create(
                            mineType?.toMediaTypeOrNull(), file
                        )
                    )
                }
                val entrySet: Set<Map.Entry<String?, Any?>> = this.params!!.entries
                for ((key, value) in entrySet) {
                    bodyBuilder.addFormDataPart(key!!, value.toString())
                }
                val url = fullUrl()
                reqBuilder.url(url!!).post(bodyBuilder.build())
            } else {
                val url = fullUrl()
                val entrySet = params!!.entries
                if (bodyType == BodyType.POST) {
                    if (type == Type.RAW_METHOD_APPEND_URL) {
                        reqBuilder.url(url!!).post(RequestBody.create(JSON, jsonParam()))
                    } else if (type == Type.FORM_METHOD_IN_FORMBODY) {
                        val formBuilder = FormBody.Builder()
                        for ((key, value) in entrySet) {
                            if (value !is String) {
                                throw RuntimeException("when use form，value must be string！！！")
                            }
                            formBuilder.add(key, value.toString())
                        }
                        reqBuilder.url(url!!).post(formBuilder.build())
                    }
                } else if (bodyType == BodyType.GET || bodyType == BodyType.DELETE) {
                    var urlBuilder = StringBuilder(url!!)
                    urlBuilder.append("?")
                    for ((key, value) in entrySet) {
                        urlBuilder.append(key).append("=").append(value).append("&")
                    }
                    urlBuilder = urlBuilder.replace(
                        urlBuilder.length - 1, urlBuilder.length, ""
                    )
                    reqBuilder.url(urlBuilder.toString())
                    if (bodyType == BodyType.GET) {
                        reqBuilder.get()
                    } else {
                        reqBuilder.delete()
                    }
                } else if (bodyType == BodyType.PATCH) {
                    reqBuilder.url(url!!).patch(RequestBody.create(JSON, jsonParam()))
                }
            }
            return reqBuilder.build()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return null
    }

    private fun fullUrl(): String? {
        return if (type == Type.FORM_METHOD_IN_FORMBODY) {
            url
        } else {
            url + if (!TextUtils.isEmpty(method)) method else ""
        }
    }

    private fun jsonParam(): String {
        if (params == null || params!!.isEmpty()) {
            return "{}"
        }
        try {
            return gson!!.toJson(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "{}"
    }

    fun executeSync(responseConvertListener: ResponseConvertListener? = null): Any? {
        return getRequest()?.let {
            try {
                startTimestamp = System.currentTimeMillis()
                handleResponse(responseConvertListener, okHttpClient.newCall(it).execute())
            } catch (e: IOException) {
                handleException(e)
                null
            }
        } ?: run {
            log.e("request == null")
            null
        }
    }

    @JvmOverloads
    fun execute(responseConvertListener: ResponseConvertListener? = null): HttpTask {
        val request = getRequest()
        if (request == null) {
            log.e("request == null")
            return this
        }
        startTimestamp = System.currentTimeMillis()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                handleResponse(responseConvertListener, response)
            }
        })
        return this
    }

    private fun handleException(e: IOException) {
        e.printStackTrace()
        log.e(e)
        val msg = e.message
        if (!TextUtils.isEmpty(msg) && isNetworkError(msg)) {
            onHttpFailed(NETWORK_INVALID, NETWORK_ERROR)
        } else {
            onHttpFailed(FAILUE, SYSTEM_ERROR)
        }
        if (realExceptionListener != null) {
            realExceptionListener!!.onHttpTaskRealException(this@HttpTask, FAILUE, msg)
        }
    }

    private fun handleResponse(
        responseConvertListener: ResponseConvertListener?, response: Response
    ): Any? {
        var model: Any? = null
        try {
            calculateTimeDiff(response)
            if (responseClass != null) {
                log.v("response[${responseClass?.name}]")
            }
            if (HttpTask.responseClass != null) {
                log.v("sResponse[${HttpTask.responseClass?.name}]")
            }
            if (response.isSuccessful) {
                val responseBodyString = response.body!!.string()
                if (responseConvertListener == null) {
                    if (checkResponseListener.isSuccess(responseBodyString)) {
                        onHttpSuccess(gson!!.fromJson<Any>(responseBodyString, responseClass)
                            .also { model = it })
                    } else {
                        onHttpFailed(
                            checkResponseListener.getCode(responseBodyString),
                            checkResponseListener.getMessage(responseBodyString)
                        )
                    }
                } else {
                    onHttpSuccess(responseConvertListener.onResponseConvert(
                        responseBodyString, responseClass
                    ).also { model = it })
                }
            } else {
                log.e("http error status code[${response.code}]")
                onHttpFailed(FAILUE, SYSTEM_ERROR)
                if (realExceptionListener != null) {
                    realExceptionListener!!.onHttpTaskRealException(
                        this@HttpTask, FAILUE, response.code.toString() + "," + response.message
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
            onHttpFailed(FAILUE, SYSTEM_ERROR)
            if (realExceptionListener != null) {
                realExceptionListener!!.onHttpTaskRealException(
                    this@HttpTask, FAILUE, e.message
                )
            }
        } finally {
            response.close()
            val currTimestamp = System.currentTimeMillis()
            val diff = currTimestamp - startTimestamp
            if (diff > 2000) {
                log.w(method + "," + formatApiTime(diff))
            } else {
                log.i(method + "," + formatApiTime(diff))
            }
        }
        return model
    }

    private fun isNetworkError(message: String?): Boolean {
        var b = false
        for (s in NETWORK_ERROR_CASE_LIST) {
            if (message!!.contains(s)) {
                b = true
                break
            }
        }
        return b
    }

    private fun formatApiTime(diff: Long): String {
        if (diff < 1000) {
            return diff.toString() + "ms"
        }
        val sec = diff / 1000
        val ms = diff % 1000
        return sec.toString() + "s" + ms + "ms"
    }

    private fun printUrlParams() {
        fullUrl()?.let { r ->
            params?.let { p ->
                log.urlD(r, p)
            }
        }
    }

    private fun onHttpSuccess(model: Any) {
        printUrlParams()
        backgroundBeforeListener?.onHttpSuccess(this, model)
        mainHandler.post {
            beforeListener?.onHttpSuccess(this, model)
            if (weakReferenceListener) {
                wrListener?.get()?.onHttpSuccess(this, model)
            } else {
                listener?.onHttpSuccess(this, model)
            }
            afterListener?.onHttpSuccess(this, model)
        }
    }

    private fun onHttpFailed(code: Int, msg: String) {
        printUrlParams()
        if (backgroundBeforeListener != null) {
            backgroundBeforeListener!!.onHttpFailed(this, code, msg)
        }
        mainHandler.post {
            if (globalDeal) {
                errorListener?.onFailed(this, code, msg)
            }
            beforeListener?.onHttpFailed(this, code, msg)
            if (weakReferenceListener) {
                wrListener?.get()?.onHttpFailed(this, code, msg)
            } else {
                listener?.onHttpFailed(this, code, msg)
            }
            afterListener?.onHttpFailed(this, code, msg)
        }
    }

    private fun getFileExtensionFromPath(path: String): String {
        return path.substring(path.lastIndexOf(".") + 1)
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun getImportantMessage(message: String?): String {
        val sb = StringBuilder()
        if (debug) {
            sb.append("api:").append(method).append(",")
        }
        sb.append(message ?: SYSTEM_ERROR)
        return sb.toString()
    }

    interface RealExceptionListener {
        fun onHttpTaskRealException(httpTask: HttpTask, code: Int, exception: String?)
    }

    interface ClientServerTimeDiffListener {
        fun onClientServerTimeDiff(millisecond: Long)
    }

    interface ErrorListener {
        fun onFailed(ht: HttpTask, code: Int, msg: String?)
    }

    interface Listener {
        fun onHttpSuccess(ht: HttpTask, model: Any)
        fun onHttpFailed(ht: HttpTask, code: Int, msg: String)
    }

    open class SimpleListener : Listener {
        override fun onHttpSuccess(ht: HttpTask, model: Any) {}
        override fun onHttpFailed(ht: HttpTask, code: Int, msg: String) {}
    }

    interface ResponseConvertListener {
        fun onResponseConvert(dataStr: String?, responseCls: Class<*>?): Any
    }

    interface HeaderBodyListener {
        fun onInit(context: Application)
        fun onGetHeaders(method: String, body: Map<String, Any>?): Map<String, String>?
        fun onGetBody(method: String, body: Map<String, Any>?): Map<String, Any>?
    }

    interface CheckResponseListener {
        fun isSuccess(responseBodyString: String): Boolean

        fun getCode(responseBodyString: String): Int
        fun getMessage(responseBodyString: String): String
    }

    interface DynamicUrlListener {
        fun onGetDynamicUrl(): String?
    }

    class Param {
        var mConnectTimeout = 30
        var mReadTimeout = 30
        var mWriteTimeout = 30
    }
}