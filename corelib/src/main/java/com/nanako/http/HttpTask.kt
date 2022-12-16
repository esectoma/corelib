package com.nanako.http

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.nanako.log.Log
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import com.nanako.http.CustomTrust.setTrust
import com.nanako.http.CustomTrust.trustAllCerts
import okhttp3.Request
import okhttp3.MultipartBody
import okhttp3.FormBody
import okhttp3.Call
import okhttp3.ResponseBody
import okhttp3.Response
import com.nanako.http.HttpTask.FlowCallBack
import com.nanako.http.HttpTask.CallBack
import com.nanako.http.HttpTask.BODY_TYPE
import com.nanako.http.HttpTask
import kotlin.jvm.JvmOverloads
import com.nanako.http.HttpTask.IDataConverter
import com.nanako.http.HttpTask.IHttpResponse
import com.google.gson.Gson
import com.nanako.http.HttpTask.ICommonHeadersAndParameters
import com.nanako.http.HttpTask.ICommonErrorDeal
import com.nanako.http.HttpTask.RealExceptionCallback
import com.nanako.http.HttpTask.ClientServerTimeDiffCallback
import com.nanako.http.CustomTrust
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Bond on 2016/4/13.
 */
class HttpTask private constructor(
    private var mUrl: String?,
    val method: String,
    var params: Map<String, Any>?,
    private val mResponseClass: Class<*>?,
    var backParam: Any?,
    private var mBeforeCallBack: FlowCallBack?,
    callBack: CallBack,
    afterCallBack: FlowCallBack?
) {
    private var mFiles: Map<String, String>? = null
    var headers: Map<String, String>? = null
        private set
    private var mAfterCallBack: FlowCallBack?
    var wrCallBack: WeakReference<CallBack>? = null
    private var mWeakReferenceCallback = false
    private var mCallBack: CallBack? = null
    private var mExtraParams: MutableMap<String, Any>? = null
    private var mBackgroundBeforeCallBack: FlowCallBack? = null
    var isCanceled = false
        private set
    var isFinished = false
        private set
    private var mBodyType = BODY_TYPE.POST
    private var mGlobalDeal = true
    private var mNoCommonParam = false
    private var mStartTimestamp: Long = 0
    private var mDefaultFileExtension = "jpg"

    private enum class BODY_TYPE {
        GET, POST, UPLOAD, PATCH, DELETE
    }

    enum class Type {
        RAW_METHOD_APPEND_URL, FORM_METHOD_IN_FORMBODY
    }

    init {
        if (mWeakReferenceCallback) {
            wrCallBack = WeakReference(callBack)
        } else {
            mCallBack = callBack
        }
        mAfterCallBack = afterCallBack
    }

    fun get(): HttpTask {
        mBodyType = BODY_TYPE.GET
        return this
    }

    fun post(): HttpTask {
        mBodyType = BODY_TYPE.POST
        return this
    }

    fun patch(): HttpTask {
        mBodyType = BODY_TYPE.PATCH
        return this
    }

    fun delete(): HttpTask {
        mBodyType = BODY_TYPE.DELETE
        return this
    }

    fun upload(files: Map<String, String>?): HttpTask {
        mBodyType = BODY_TYPE.UPLOAD
        mFiles = files
        setNoCommonParam(true)
        return this
    }

    fun setUrl(url: String?): HttpTask {
        mUrl = url
        return this
    }

    fun setBackParam(backParam: Any?): HttpTask {
        this.backParam = backParam
        return this
    }

    fun setBeforeCallBack(beforeCallBack: FlowCallBack?): HttpTask {
        mBeforeCallBack = beforeCallBack
        return this
    }

    fun setAfterCallBack(afterCallBack: FlowCallBack?): HttpTask {
        mAfterCallBack = afterCallBack
        return this
    }

    fun setWeakReferenceCallback(weakReferenceCallback: Boolean): HttpTask {
        mWeakReferenceCallback = weakReferenceCallback
        return this
    }

    private fun dealRequest(): Request? {
        try {
            if (sICommonHeadersAndParameters != null && !mNoCommonParam) {
                params = sICommonHeadersAndParameters!!.getParams(method, params)
            }
            if (params == null) {
                params = TreeMap()
            }
            val reqBuilder = Request.Builder()
            if (sICommonHeadersAndParameters != null) {
                val headers = sICommonHeadersAndParameters!!.getHeaders(
                    method,
                    params
                )
                this.headers = headers
                if (headers != null && !headers.isEmpty()) {
                    for ((key, value) in headers) {
                        reqBuilder.addHeader(key, value)
                    }
                }
            }
            if (mBodyType == BODY_TYPE.UPLOAD) {
                val bodyBuilder: MultipartBody.Builder = MultipartBody.Builder().setType(
                    MultipartBody.FORM
                )
                if (mFiles == null || mFiles!!.isEmpty()) {
                    sLog.e("no file!")
                    return null
                }
                var file: File
                val params = mFiles!!.entries.iterator()
                var param: Map.Entry<String, String>
                var filePath: String
                while (params.hasNext()) {
                    param = params.next()
                    filePath = param.value
                    file = File(filePath)
                    if (!file.exists()) {
                        sLog.w("file[\$filePath] not exist")
                        continue
                    }
                    val key = param.key
                    var fileExtension = getFileExtensionFromPath(filePath)
                    var mineType = getMimeTypeFromExtension(fileExtension)
                    var fileName: String
                    if (!TextUtils.isEmpty(mineType)) {
                        fileName = "$key.$fileExtension"
                    } else {
                        fileExtension = mDefaultFileExtension
                        mineType = getMimeTypeFromExtension(fileExtension)
                        fileName = "$key.$fileExtension"
                    }
                    sLog.d("add upload file[\$key], key,fileName[\$fileName],fileExtension[\$fileExtension],mineType[\$mineType]")
                    bodyBuilder.addFormDataPart(
                        key,
                        fileName,
                        RequestBody.create(
                            mineType?.toMediaTypeOrNull(), file
                        )
                    )
                }
                val entrySet: Set<Map.Entry<String?, Any?>> = this.params!!.entries
                for ((key, value) in entrySet) {
                    bodyBuilder.addFormDataPart(key!!, value.toString())
                }
                val url = realUrl
                reqBuilder.url(url!!).post(bodyBuilder.build())
            } else {
                val url = realUrl
                val entrySet = params!!.entries
                if (mBodyType == BODY_TYPE.POST) {
                    if (sType == Type.RAW_METHOD_APPEND_URL) {
                        reqBuilder.url(url!!).post(RequestBody.create(JSON, jsonParam))
                    } else if (sType == Type.FORM_METHOD_IN_FORMBODY) {
                        val formBuilder = FormBody.Builder()
                        for ((key, value) in entrySet) {
                            if (value !is String) {
                                throw RuntimeException("when use form，value must be string！！！")
                            }
                            formBuilder.add(key, value.toString())
                        }
                        reqBuilder.url(url!!).post(formBuilder.build())
                    }
                } else if (mBodyType == BODY_TYPE.GET || mBodyType == BODY_TYPE.DELETE) {
                    var urlBuilder = StringBuilder(url)
                    urlBuilder.append("?")
                    for ((key, value) in entrySet) {
                        urlBuilder.append(key)
                            .append("=")
                            .append(value)
                            .append("&")
                    }
                    urlBuilder = urlBuilder.replace(
                        urlBuilder.length - 1,
                        urlBuilder.length,
                        ""
                    )
                    reqBuilder.url(urlBuilder.toString())
                    if (mBodyType == BODY_TYPE.GET) {
                        reqBuilder.get()
                    } else {
                        reqBuilder.delete()
                    }
                } else if (mBodyType == BODY_TYPE.PATCH) {
                    reqBuilder.url(url!!).patch(RequestBody.create(JSON, jsonParam))
                }
            }
            return reqBuilder.build()
        } catch (e: Exception) {
            e.printStackTrace()
            sLog.e(e)
        }
        return null
    }

    val realUrl: String?
        get() = if (sType == Type.FORM_METHOD_IN_FORMBODY) {
            mUrl
        } else mUrl + if (!TextUtils.isEmpty(method)) method else ""
    private val jsonParam: String
        private get() {
            if (params == null || params!!.isEmpty()) {
                return "{}"
            }
            try {
                return sGson!!.toJson(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "{}"
        }

    @JvmOverloads
    fun execute(iDataConverter: IDataConverter? = null): HttpTask {
        val request = dealRequest()
        if (request == null) {
            sLog.e("request == null")
            return this
        }
        mStartTimestamp = System.currentTimeMillis()
        onHttpStart()
        sOkHttpClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                sLog.e(e)
                val msg = e.message
                if (!TextUtils.isEmpty(msg) && isNetworkError(msg)) {
                    onHttpFailed(NETWORK_INVALID, NETWORK_ERROR)
                } else {
                    onHttpFailed(FAILUE, SYSTEM_ERROR)
                }
                if (sRealExceptionCallback != null) {
                    sRealExceptionCallback!!.onHttpTaskRealException(this@HttpTask, FAILUE, msg)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    calculateTimeDiff(response)
                    if (mResponseClass != null) {
                        sLog.v("response[\${mResponseClass.getName()}]")
                    }
                    if (sResponseClass != null) {
                        sLog.v("sResponse[\${sResponseClass.getName()}]")
                    }
                    if (response.isSuccessful) {
                        val result = response.body!!.string()
                        if (iDataConverter == null) {
                            val httpResponse = sGson!!.fromJson<Any>(result, sResponseClass)
                            if (httpResponse !is IHttpResponse) {
                                sLog.e("result[\$result]")
                                throw RuntimeException(
                                    "sResponseClass must implements " +
                                            "IHttpResponse"
                                )
                            }
                            val iHttpResponse = httpResponse
                            if (iHttpResponse.code == 0) {
                                onHttpSuccess(result, sGson!!.fromJson<Any>(result, mResponseClass))
                            } else {
                                onHttpFailed(iHttpResponse.code, iHttpResponse.message)
                            }
                        } else {
                            onHttpSuccess(result, iDataConverter.doConvert(result, mResponseClass))
                        }
                    } else {
                        sLog.e("http error status code[\${response.code()}]")
                        onHttpFailed(response.code, "")
                        if (sRealExceptionCallback != null) {
                            sRealExceptionCallback!!.onHttpTaskRealException(
                                this@HttpTask, FAILUE,
                                response.code.toString() + "," + response.message
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    sLog.e(e)
                    onHttpFailed(FAILUE, SYSTEM_ERROR)
                    if (sRealExceptionCallback != null) {
                        sRealExceptionCallback!!.onHttpTaskRealException(
                            this@HttpTask,
                            FAILUE,
                            e.message
                        )
                    }
                } finally {
                    response.close()
                    val currTimestamp = System.currentTimeMillis()
                    val diff = currTimestamp - mStartTimestamp
                    if (diff > 2000) {
                        sLog.w(method + "," + formatApiTime(diff))
                    } else {
                        sLog.i(method + "," + formatApiTime(diff))
                    }
                }
            }
        })
        return this
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

    private fun onHttpStart() {
        sLog.urlD(realUrl!!, params!!)
        sHandler!!.post(object : Runnable {
            override fun run() {
                isFinished = false
                if (mWeakReferenceCallback) {
                    val callBack = wrCallBack!!.get()
                    if (null != callBack) {
                        callBack.onHttpStart(this@HttpTask)
                    } else {
                        sLog.w("callBack was destroyed")
                    }
                } else {
                    mCallBack?.onHttpStart(this@HttpTask)
                }
            }
        })
    }

    private fun onHttpSuccess(modelStr: String, entity: Any) {
        sLog.urlI(realUrl!!, params!!)
        if (mBackgroundBeforeCallBack != null) {
            mBackgroundBeforeCallBack!!.onSuccess(this, entity, modelStr)
        }
        sHandler!!.post(object : Runnable {
            override fun run() {
                if (null != mBeforeCallBack) {
                    mBeforeCallBack!!.onSuccess(this@HttpTask, entity, modelStr)
                }
                if (mWeakReferenceCallback) {
                    val callBack = wrCallBack!!.get()
                    if (null != callBack) {
                        callBack.onHttpSuccess(this@HttpTask, entity)
                    } else {
                        sLog.w("callBack was destroyed")
                    }
                } else {
                    mCallBack?.onHttpSuccess(this@HttpTask, entity)
                }
                if (null != mAfterCallBack) {
                    mAfterCallBack!!.onSuccess(this@HttpTask, entity, modelStr)
                }
                isFinished = true
            }
        })
    }

    private fun onHttpFailed(errorCode: Int, message: String) {
        sLog.urlE(realUrl!!, params!!)
        if (mBackgroundBeforeCallBack != null) {
            mBackgroundBeforeCallBack!!.onFailed(this, errorCode, message)
        }
        sHandler!!.post(object : Runnable {
            override fun run() {
                if (sICommonErrorDeal != null && mGlobalDeal) {
                    sICommonErrorDeal!!.onFailed(this@HttpTask, errorCode, message)
                }
                if (null != mBeforeCallBack) {
                    mBeforeCallBack!!.onFailed(this@HttpTask, errorCode, message)
                }
                if (mWeakReferenceCallback) {
                    val callBack = wrCallBack!!.get()
                    if (null != callBack) {
                        callBack.onHttpFailed(this@HttpTask, errorCode, message)
                    } else {
                        sLog.w("callBack was destroyed")
                    }
                } else {
                    mCallBack?.onHttpFailed(this@HttpTask, errorCode, message)
                }
                if (null != mAfterCallBack) {
                    mAfterCallBack!!.onFailed(this@HttpTask, errorCode, message)
                }
                isFinished = true
            }
        })
    }

    fun cancel() {
        isCanceled = true
    }

    fun setGlobalDeal(globalDeal: Boolean): HttpTask {
        mGlobalDeal = globalDeal
        return this
    }

    fun setNoCommonParam(noCommonParam: Boolean): HttpTask {
        mNoCommonParam = noCommonParam
        return this
    }

    fun setBackgroundBeforeCallBack(backgroundBeforeCallBack: FlowCallBack?): HttpTask {
        mBackgroundBeforeCallBack = backgroundBeforeCallBack
        return this
    }

    fun setDefaultFileExtension(defaultFileExtension: String): HttpTask {
        mDefaultFileExtension = defaultFileExtension
        return this
    }

    val backParamBoolean: Boolean
        get() = if (backParam != null && backParam is Boolean) {
            backParam as Boolean
        } else false
    val backParamLong: Long
        get() = if (backParam != null && backParam is Long) {
            backParam as Long
        } else 0L
    val backParamInt: Int
        get() = if (backParam != null && backParam is Int) {
            backParam as Int
        } else 0
    val backParamString: String
        get() = if (backParam != null && backParam is String) {
            backParam as String
        } else ""

    fun addExtraParam(key: String, param: Any): HttpTask {
        if (mExtraParams == null) {
            mExtraParams = HashMap()
        }
        mExtraParams!![key] = param
        return this
    }

    fun getExtraParam(key: String): Any? {
        return if (extraParamSize <= 0) null else mExtraParams!![key]
    }

    val extraParamSize: Int
        get() = if (mExtraParams == null) 0 else mExtraParams!!.size

    fun getExtraParamBoolean(key: String): Boolean {
        val o = getExtraParam(key)
        if (o is Boolean) {
            return o
        }
        sLog.e("the value for [\$key] must be boolean!!!")
        return false
    }

    fun getExtraParamLong(key: String): Long {
        val o = getExtraParam(key)
        if (o is Long) {
            return o
        }
        sLog.e("the value for [\$key] must be long!!!")
        return -1
    }

    fun getExtraParamInt(key: String): Int {
        val o = getExtraParam(key)
        if (o is Int) {
            return o
        }
        sLog.e("the value for [\$key] must be int!!!")
        return -1
    }

    fun getExtraParamString(key: String): String {
        val o = getExtraParam(key)
        if (o is String) {
            return o
        }
        sLog.e("the value for [\$key] must be string!!!")
        return ""
    }

    fun getExtraParamFloat(key: String): Float {
        val o = getExtraParam(key)
        if (o is Float) {
            return o
        }
        sLog.e("the value for [\$key] must be float!!!")
        return -1f
    }

    fun getExtraParamDouble(key: String): Double {
        val o = getExtraParam(key)
        if (o is Double) {
            return o
        }
        sLog.e("the value for [\$key] must be double!!!")
        return (-1).toDouble()
    }

    interface ICommonErrorDeal {
        fun onFailed(httpTask: HttpTask?, code: Int, message: String?)
    }

    interface CallBack {
        fun onHttpStart(httpTask: HttpTask?)
        fun onHttpSuccess(httpTask: HttpTask?, entity: Any?)
        fun onHttpFailed(httpTask: HttpTask?, errorCode: Int, message: String?)
    }

    interface FlowCallBack {
        fun onSuccess(httpTask: HttpTask?, entity: Any?, modelStr: String?)
        fun onFailed(httpTask: HttpTask?, errorCode: Int, message: String?)
    }

    interface IDataConverter {
        fun doConvert(dataStr: String?, responseClass: Class<*>?): Any
    }

    interface ICommonHeadersAndParameters {
        fun init(context: Context?)
        fun getHeaders(method: String, params: Map<String, Any>?): Map<String, String>?
        fun getParams(method: String, params: Map<String, Any>?): Map<String, Any>?
    }

    interface IHttpResponse {
        val code: Int
        val message: String
    }

    private fun getFileExtensionFromPath(path: String): String {
        return path.substring(path.lastIndexOf(".") + 1)
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun getImportantMessage(message: String?): String {
        val sb = StringBuilder()
        if (sDebug) {
            sb.append("api:").append(method).append(",")
        }
        sb.append(message ?: SYSTEM_ERROR)
        return sb.toString()
    }

    class SimpleCallBack : CallBack {
        override fun onHttpStart(httpTask: HttpTask?) {}
        override fun onHttpSuccess(httpTask: HttpTask?, entity: Any?) {}
        override fun onHttpFailed(httpTask: HttpTask?, errorCode: Int, message: String?) {}
    }

    class SimpleFlowCallBack : FlowCallBack {
        override fun onSuccess(httpTask: HttpTask?, entity: Any?, modelStr: String?) {}
        override fun onFailed(httpTask: HttpTask?, errorCode: Int, message: String?) {}
    }

    interface RealExceptionCallback {
        fun onHttpTaskRealException(httpTask: HttpTask?, code: Int, exception: String?)
    }

    interface ClientServerTimeDiffCallback {
        fun onClientServerTimeDiff(millisecond: Long)
    }

    class Param {
        var mConnectTimeout = 30
        var mReadTimeout = 30
        var mWriteTimeout = 30
        fun setConnectTimeout(connectTimeout: Int): Param {
            mConnectTimeout = connectTimeout
            return this
        }

        fun setReadTimeout(readTimeout: Int): Param {
            mReadTimeout = readTimeout
            return this
        }

        fun setWriteTimeout(writeTimeout: Int): Param {
            mWriteTimeout = writeTimeout
            return this
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
        private var sDebug = false
        var context: Context? = null
            private set
        private var sOkHttpClient: OkHttpClient? = null
        private var sGson: Gson? = null
        private var sICommonHeadersAndParameters: ICommonHeadersAndParameters? = null
        private var sICommonErrorDeal: ICommonErrorDeal? = null
        private var sRealExceptionCallback: RealExceptionCallback? = null
        private var sClientServerTimeDiffCallback: ClientServerTimeDiffCallback? = null
        private var sUrl: String? = null
        private var sTimeDiff: Long = 0
        private var sHandler: Handler? = null
        var sLog = Log()
        private var sResponseClass: Class<*>? = null
        private var sType = Type.RAW_METHOD_APPEND_URL

        @JvmOverloads
        fun init(
            isDebug: Boolean,
            context: Context,
            url: String,
            iCommonHeadersAndParameters: ICommonHeadersAndParameters?,
            iCommonErrorDeal: ICommonErrorDeal?,
            responseClass: Class<*>?,
            certificateAssetsName: String?,
            type: Type =
                Type.RAW_METHOD_APPEND_URL, param: Param = Param()
        ) {
            sDebug = isDebug
            sLog.setFilterTag("[http]")
            sLog.isEnabled = isDebug
            Companion.context = context
            sUrl = url
            sICommonHeadersAndParameters = iCommonHeadersAndParameters
            sICommonErrorDeal = iCommonErrorDeal
            sResponseClass = responseClass
            val builder = OkHttpClient.Builder()
            val loggingInterceptor = HttpLoggingInterceptor { message -> sLog.jsonV(message) }
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
                        sLog.w("notice that you choose trust all certificates")
                        trustAllCerts(builder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sLog.e(e)
            }
            sOkHttpClient = builder.build()
            sGson = Gson()
            sHandler = Handler()
            if (sICommonHeadersAndParameters != null) {
                sICommonHeadersAndParameters!!.init(Companion.context)
            }
            sType = type
            NETWORK_ERROR_CASE_LIST.add(NETWORK_ERROR_CASE)
            NETWORK_ERROR_CASE_LIST.add(NETWORK_ERROR_CASE_1)
        }

        fun create(
            method: String,
            params: Map<String, Any>?,
            responseClass: Class<*>?,
            callBack: CallBack
        ): HttpTask {
            return create(method, params, responseClass, null, null, callBack, null)
        }

        fun create(
            method: String,
            params: Map<String, Any>?,
            responseClass: Class<*>?,
            backParam: Any?,
            callBack: CallBack
        ): HttpTask {
            return create(method, params, responseClass, backParam, null, callBack, null)
        }

        fun create(
            method: String,
            params: Map<String, Any>?,
            responseClass: Class<*>?,
            backParam: Any?,
            beforeCallBack: FlowCallBack?,
            callBack: CallBack,
            afterCallBack: FlowCallBack?
        ): HttpTask {
            return HttpTask(
                sUrl,
                method,
                params,
                responseClass,
                backParam,
                beforeCallBack,
                callBack,
                afterCallBack
            )
        }

        fun setRealExceptionCallback(realExceptionCallback: RealExceptionCallback?) {
            sRealExceptionCallback = realExceptionCallback
        }

        fun setClientServerTimeDiffCallback(clientServerTimeDiffCallback: ClientServerTimeDiffCallback?) {
            sClientServerTimeDiffCallback = clientServerTimeDiffCallback
        }

        private fun calculateTimeDiff(response: Response) {
            val dateStr = response.header("Date")
            try {
                val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(
                    dateStr
                )
                sTimeDiff = System.currentTimeMillis() - date.time
                if (sClientServerTimeDiffCallback != null) {
                    sClientServerTimeDiffCallback!!.onClientServerTimeDiff(sTimeDiff)
                }
                sLog.v("local and server time differ [\$sTimeDiff]")
            } catch (e: Exception) {
                sTimeDiff = 0
                e.printStackTrace()
                sLog.e(e)
            }
        }

        val serverCurrentTimeMillis: Long
            get() = System.currentTimeMillis() - sTimeDiff
    }
}