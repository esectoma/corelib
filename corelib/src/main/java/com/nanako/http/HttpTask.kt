package com.nanako.http

import android.app.Application
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
import okhttp3.Response
import kotlin.jvm.JvmOverloads
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
class HttpTask private constructor() {
    var url: String? = null
    var method: String = ""
    var params: Map<String, Any>? = null
    var responseClass: Class<*>? = null
    var backParam: Any? = null
    var beforeCallBack: FlowCallBack? = null
    var files: Map<String, String>? = null
    var headers: Map<String, String>? = null
    var afterCallBack: FlowCallBack? = null
    var callBack: CallBack? = null
    var extraParams: MutableMap<String, Any>? = null
    var backgroundBeforeCallBack: FlowCallBack? = null
    var defaultFileExtension = "jpg"
    var globalDeal = true
    var noCommonParam = false
    private var wrCallBack: WeakReference<CallBack>? = null
    private var weakReferenceCallback = false
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

    fun softRefCallback() {
        callBack?.let {
            wrCallBack = WeakReference(callBack)
            callBack = null
        }
    }

    private fun dealRequest(): Request? {
        try {
            if (sICommonHeadersAndParameters != null && !noCommonParam) {
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
            if (bodyType == BodyType.UPLOAD) {
                val bodyBuilder: MultipartBody.Builder = MultipartBody.Builder().setType(
                    MultipartBody.FORM
                )
                if (files == null || files!!.isEmpty()) {
                    sLog.e("no file!")
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
                        fileExtension = defaultFileExtension
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
                if (bodyType == BodyType.POST) {
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
                } else if (bodyType == BodyType.GET || bodyType == BodyType.DELETE) {
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
                    if (bodyType == BodyType.GET) {
                        reqBuilder.get()
                    } else {
                        reqBuilder.delete()
                    }
                } else if (bodyType == BodyType.PATCH) {
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
            url
        } else url + if (!TextUtils.isEmpty(method)) method else ""
    private val jsonParam: String
        get() {
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
        startTimestamp = System.currentTimeMillis()
        onHttpStart()
        sOkHttpClient.newCall(request).enqueue(object : Callback {
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
                    if (responseClass != null) {
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
                            if (httpResponse.onGetCode() == 0) {
                                onHttpSuccess(result, sGson!!.fromJson<Any>(result, responseClass))
                            } else {
                                onHttpFailed(
                                    httpResponse.onGetCode(),
                                    httpResponse.onGetMessage() ?: ""
                                )
                            }
                        } else {
                            onHttpSuccess(result, iDataConverter.doConvert(result, responseClass))
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
                    val diff = currTimestamp - startTimestamp
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

    private fun printUrlParams() {
        realUrl?.let { r ->
            params?.let { p ->
                sLog.urlD(r, p)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onHttpStart() {
        printUrlParams()
        GlobalScope.launch(Dispatchers.Main) {
            if (weakReferenceCallback) {
                wrCallBack?.get()?.onHttpStart(this@HttpTask)
            } else {
                callBack?.onHttpStart(this@HttpTask)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onHttpSuccess(modelStr: String, model: Any) {
        printUrlParams()
        backgroundBeforeCallBack?.onSuccess(this, model, modelStr)
        GlobalScope.launch(Dispatchers.Main) {
            beforeCallBack?.onSuccess(this@HttpTask, model, modelStr)
            if (weakReferenceCallback) {
                wrCallBack?.get()?.onHttpSuccess(this@HttpTask, model)
            } else {
                callBack?.onHttpSuccess(this@HttpTask, model)
            }
            afterCallBack?.onSuccess(this@HttpTask, model, modelStr)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onHttpFailed(code: Int, msg: String) {
        printUrlParams()
        if (backgroundBeforeCallBack != null) {
            backgroundBeforeCallBack!!.onFailed(this, code, msg)
        }
        GlobalScope.launch(Dispatchers.Main) {
            if (globalDeal) {
                sICommonErrorDeal?.onFailed(this@HttpTask, code, msg)
            }
            beforeCallBack?.onFailed(this@HttpTask, code, msg)
            if (weakReferenceCallback) {
                wrCallBack?.get()?.onHttpFailed(this@HttpTask, code, msg)
            } else {
                callBack?.onHttpFailed(this@HttpTask, code, msg)
            }
            afterCallBack?.onFailed(this@HttpTask, code, msg)
        }
    }

    interface ICommonErrorDeal {
        fun onFailed(ht: HttpTask, code: Int, msg: String?)
    }

    interface CallBack {
        fun onHttpStart(ht: HttpTask)
        fun onHttpSuccess(ht: HttpTask, model: Any)
        fun onHttpFailed(ht: HttpTask, code: Int, msg: String)
    }

    interface FlowCallBack {
        fun onSuccess(ht: HttpTask, entity: Any, modelStr: String)
        fun onFailed(ht: HttpTask, code: Int, msg: String)
    }

    interface IDataConverter {
        fun doConvert(dataStr: String?, responseCls: Class<*>?): Any
    }

    interface ICommonHeadersAndParameters {
        fun init(context: Context)
        fun getHeaders(method: String, params: Map<String, Any>?): Map<String, String>?
        fun getParams(method: String, params: Map<String, Any>?): Map<String, Any>?
    }

    interface IHttpResponse {
        fun onGetCode(): Int
        fun onGetMessage(): String?
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

    open class SimpleCallBack : CallBack {
        override fun onHttpStart(ht: HttpTask) {}
        override fun onHttpSuccess(ht: HttpTask, model: Any) {}
        override fun onHttpFailed(ht: HttpTask, code: Int, msg: String) {}
    }

    open class SimpleFlowCallBack : FlowCallBack {
        override fun onSuccess(ht: HttpTask, entity: Any, modelStr: String) {}
        override fun onFailed(ht: HttpTask, code: Int, msg: String) {}
    }

    interface RealExceptionCallback {
        fun onHttpTaskRealException(httpTask: HttpTask, code: Int, exception: String?)
    }

    interface ClientServerTimeDiffCallback {
        fun onClientServerTimeDiff(millisecond: Long)
    }

    interface DynamicUrlCallback {
        fun onGetDynamicUrl(): String?
    }

    class Param {
        var mConnectTimeout = 30
        var mReadTimeout = 30
        var mWriteTimeout = 30
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
        lateinit var context: Application
            private set
        private lateinit var sOkHttpClient: OkHttpClient
        private var sGson: Gson? = null
        private var sICommonHeadersAndParameters: ICommonHeadersAndParameters? = null
        private var sICommonErrorDeal: ICommonErrorDeal? = null
        var sRealExceptionCallback: RealExceptionCallback? = null
        var sDynamicUrlCallback: DynamicUrlCallback? = null
        var sClientServerTimeDiffCallback: ClientServerTimeDiffCallback? = null
        var sUrl: String? = null
        private var sTimeDiff: Long = 0
        var sLog = Log()
        private var sResponseClass: Class<*>? = null
        private var sType = Type.RAW_METHOD_APPEND_URL
        var dynamicUrl = false

        @JvmOverloads
        fun init(
            isDebug: Boolean,
            context: Application,
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
            sICommonHeadersAndParameters?.init(Companion.context)
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
            return HttpTask().apply {
                this.url = if (dynamicUrl) sDynamicUrlCallback?.onGetDynamicUrl() else sUrl
                this.method = method
                this.params = params
                this.responseClass = responseClass
                this.backParam = backParam
                this.beforeCallBack = beforeCallBack
                this.callBack = callBack
                this.afterCallBack = afterCallBack
            }
        }

        private fun calculateTimeDiff(response: Response) {
            val dateStr = response.header("Date")
            dateStr?.let {
                try {
                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(dateStr)
                        ?.let { date ->
                            sTimeDiff = System.currentTimeMillis() - date.time
                            sClientServerTimeDiffCallback?.onClientServerTimeDiff(sTimeDiff)
                            sLog.v("local and server time differ [${sTimeDiff}]")
                        }
                } catch (e: Exception) {
                    sTimeDiff = 0
                    e.printStackTrace()
                    sLog.e(e)
                }
            }
        }

        val serverCurrentTimeMillis: Long
            get() = System.currentTimeMillis() - sTimeDiff
    }
}