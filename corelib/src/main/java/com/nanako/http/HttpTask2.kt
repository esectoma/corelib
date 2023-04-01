package com.nanako.http

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nanako.http.CustomTrust.setTrust
import com.nanako.http.CustomTrust.trustAllCerts
import com.nanako.log.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Bond on 2016/4/13.
 */
class HttpTask2 {
    var url: String? = null
    var method: String = ""
    var headers: Map<String, String>? = null
    var params: Map<String, Any>? = null
    var files: Map<String, String>? = null
    lateinit var responseCls: Class<Any>
    var beforeCallBack: HttpCallback? = null
    var afterCallBack: HttpCallback? = null
    var extraParams: MutableMap<String, Any>? = null
    var backgroundBeforeCallBack: HttpCallback? = null
    var defaultFileExtension = "jpg"
    var commonErrDeal = true
    var commonParam = true
    var dataConverter: DataConverter? = null
    var type: Type = Type.RAW_METHOD_APPEND_URL
    var bodyType = BodyType.POST
    private var startTimestamp: Long = 0

    enum class BodyType {
        GET, POST, UPLOAD, PATCH, DELETE
    }

    enum class Type {
        RAW_METHOD_APPEND_URL, FORM_METHOD_IN_FORMBODY
    }

    init {
        url = if (dynamicUrl) dynamicUrlCallback?.onGetDynamicUrl() else HttpTask2.url
    }

    private fun getRequest(): Request? {
        try {
            if (commonParam) {
                params = commonHeadersAndParameters?.getParams(method, params)
            }
            if (params == null) {
                params = TreeMap()
            }
            val reqBuilder = Request.Builder()
            headers = commonHeadersAndParameters?.getHeaders(method, params)?.also {
                for ((key, value) in it) {
                    reqBuilder.addHeader(key, value)
                }
            }
            if (bodyType == BodyType.UPLOAD) {
                val bodyBuilder: MultipartBody.Builder = MultipartBody.Builder().setType(
                    MultipartBody.FORM
                )
                if (files.isNullOrEmpty()) {
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
                reqBuilder.url(fullUrl()).post(bodyBuilder.build())
            } else {
                val url = fullUrl()
                val entrySet = params!!.entries
                if (bodyType == BodyType.POST) {
                    if (type == Type.RAW_METHOD_APPEND_URL) {
                        reqBuilder.url(url).post(RequestBody.create(JSON, param2Json()))
                    } else if (type == Type.FORM_METHOD_IN_FORMBODY) {
                        val formBuilder = FormBody.Builder()
                        for ((key, value) in entrySet) {
                            if (value !is String) {
                                throw RuntimeException("when use form，value must be string！！！")
                            }
                            formBuilder.add(key, value.toString())
                        }
                        reqBuilder.url(url).post(formBuilder.build())
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
                    reqBuilder.url(url).patch(RequestBody.create(JSON, param2Json()))
                }
            }
            return reqBuilder.build()
        } catch (e: Exception) {
            e.printStackTrace()
            log.e(e)
        }
        return null
    }

    @JvmOverloads
    fun <T : Any> execute(
        responseLd: MutableLiveData<T>
    ) {
        val request = getRequest()
        if (request == null) {
            log.e("request == null")
            return
        }
        val host = this
        startTimestamp = System.currentTimeMillis()
        onHttpStart()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                log.e(e)
                val msg = e.message
                if (!TextUtils.isEmpty(msg) && isNetworkError(msg)) {
                    onHttpFailed(NETWORK_INVALID, NETWORK_ERROR, responseLd)
                } else {
                    onHttpFailed(FAILUE, SYSTEM_ERROR, responseLd)
                }
                realExceptionCallback?.onHttpTaskRealException(host, FAILUE, msg)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    calculateTimeDiff(response)
                    log.v("response[${responseCls.name}]")
                    if (response.isSuccessful) {
                        val result = response.body!!.string()
                        if (dataConverter == null) {
                            onHttpSuccess(
                                result,
                                gson.fromJson(result, responseCls) as T,
                                responseLd
                            )
                        } else {
                            onHttpSuccess(
                                result,
                                dataConverter!!.doConvert(result, responseCls) as T,
                                responseLd
                            )
                        }
                    } else {
                        log.e("http error status code[${response.code}]")
                        onHttpFailed(FAILUE, SYSTEM_ERROR, responseLd)
                        realExceptionCallback?.onHttpTaskRealException(
                            host, FAILUE,
                            response.code.toString() + "," + response.message
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    log.e(e)
                    onHttpFailed(FAILUE, SYSTEM_ERROR, responseLd)
                    realExceptionCallback?.onHttpTaskRealException(host, FAILUE, e.message)
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
            }
        })
    }

    private fun fullUrl(): String {
        return if (type == Type.FORM_METHOD_IN_FORMBODY) {
            url ?: ""
        } else {
            url + method.ifEmpty { "" }
        }
    }

    private fun param2Json(): String {
        if (params == null || params!!.isEmpty()) {
            return "{}"
        }
        try {
            return gson.toJson(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "{}"
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
        fullUrl().let { r ->
            params?.let { p ->
                log.urlD(r, p)
            }
        }
    }

    private fun onHttpStart() {
        printUrlParams()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onHttpSuccess(modelStr: String, model: Any, mld: MutableLiveData<*>) {
        printUrlParams()
        backgroundBeforeCallBack?.onHttpSuccess(this, modelStr, model)
        GlobalScope.launch(Dispatchers.Main) {
            beforeCallBack?.onHttpSuccess(this@HttpTask2, modelStr, model)
            mld.value = model
            afterCallBack?.onHttpSuccess(this@HttpTask2, modelStr, model)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onHttpFailed(code: Int, msg: String, mld: MutableLiveData<*>) {
        printUrlParams()
        backgroundBeforeCallBack?.onHttpFailed(this, code, msg)
        GlobalScope.launch(Dispatchers.Main) {
            if (commonErrDeal) {
                commonErrorDeal?.onFailed(this@HttpTask2, code, msg)
            }
            beforeCallBack?.onHttpFailed(this@HttpTask2, code, msg)
            mld.value = null
            afterCallBack?.onHttpFailed(this@HttpTask2, code, msg)
        }
    }

    interface DealCommonErr {
        fun onFailed(ht: HttpTask2, code: Int, msg: String?)
    }

    interface DataConverter {
        fun doConvert(dataStr: String?, responseCls: java.lang.reflect.Type): Any
    }

    interface CommonHeadersAndParameters {
        fun init(context: Context)
        fun getHeaders(method: String, params: Map<String, Any>?): Map<String, String>?
        fun getParams(method: String, params: Map<String, Any>?): Map<String, Any>?
    }

    private fun getFileExtensionFromPath(path: String): String {
        return path.substring(path.lastIndexOf(".") + 1)
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    interface HttpCallback {
        fun onHttpSuccess(ht: HttpTask2, modelStr: String, model: Any)
        fun onHttpFailed(ht: HttpTask2, code: Int, msg: String)
    }

    interface RealExceptionCallback<T> {
        fun onHttpTaskRealException(ht: HttpTask2, code: Int, exception: String?)
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
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        private lateinit var context: Application
        private lateinit var okHttpClient: OkHttpClient
        private lateinit var gson: Gson
        private var timeDiff: Long = 0
        private const val NETWORK_ERROR_CASE = "Failed to connect to"
        private const val NETWORK_ERROR_CASE_1 = "Connection reset"

        const val FAILUE = -1
        const val NETWORK_INVALID = -2
        var SYSTEM_ERROR = "system error"
        var NETWORK_ERROR = "network error"
        val NETWORK_ERROR_CASE_LIST: MutableList<String> = ArrayList()
        var debug = false

        var commonHeadersAndParameters: CommonHeadersAndParameters? = null
        var commonErrorDeal: DealCommonErr? = null
        var realExceptionCallback: RealExceptionCallback<*>? = null
        var dynamicUrlCallback: DynamicUrlCallback? = null
        var clientServerTimeDiffCallback: ClientServerTimeDiffCallback? = null
        var url: String? = null
        var log = Log()
        var type = Type.RAW_METHOD_APPEND_URL
        var dynamicUrl = false

        @JvmOverloads
        fun init(
            isDebug: Boolean,
            context: Application,
            url: String,
            commonHeadersAndParameters: CommonHeadersAndParameters?,
            commonErrorDeal: DealCommonErr?,
            certificateAssetsName: String?,
            type: Type = Type.RAW_METHOD_APPEND_URL,
            param: Param = Param()
        ) {
            debug = isDebug
            log.setFilterTag("[http]")
            log.isEnabled = isDebug
            Companion.context = context
            this.url = url
            this.commonHeadersAndParameters = commonHeadersAndParameters
            this.commonErrorDeal = commonErrorDeal
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
            this.commonHeadersAndParameters?.init(Companion.context)
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
                            clientServerTimeDiffCallback?.onClientServerTimeDiff(timeDiff)
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
}