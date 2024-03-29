package com.core.log

import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

class Log {

    private val JSON_INDENT = 4
    private val LINE_SEPARATOR = System.getProperty("line.separator")

    var isEnabled: Boolean = false
    var logPath: String? = null
    var filterTag: String = ""


    fun v(log: String) {
        if (isEnabled) l('v', log)
    }

    fun d(log: String) {
        if (isEnabled) l('d', log)
    }

    fun i(log: String) {
        if (isEnabled) l('i', log)
    }

    fun w(log: String) {
        if (isEnabled) l('w', log)
    }

    fun w(e: Throwable?) {
        if (isEnabled)
            if (null != e) {
                val message = e.message
                if (!TextUtils.isEmpty(message)) {
                    l('w', message!!)
                } else {
                    l('w', "message is empty")
                }
            }
    }

    fun e(log: String) {
        if (isEnabled) l('e', log)
    }

    fun e(e: Throwable?) {
        if (isEnabled) if (null != e) {
            val message = e.message
            if (!TextUtils.isEmpty(message)) {
                l('e', message!!)
            } else {
                l('e', "message is empty")
            }
        }
    }

    fun jsonV(message: String) {
        if (isEnabled) log('v', createLog(jsonLog(message), 5))
    }

    fun jsonD(message: String) {
        if (isEnabled) log('d', createLog(jsonLog(message), 5))
    }

    fun jsonI(message: String) {
        if (isEnabled) log('i', createLog(jsonLog(message), 5))
    }

    fun jsonW(message: String) {
        if (isEnabled) log('w', createLog(jsonLog(message), 5))
    }

    fun jsonE(message: String) {
        if (isEnabled) log('e', createLog(jsonLog(message), 5))
    }

    fun urlV(url: String, mapParam: Map<String, Any>) {
        if (isEnabled) l('v', urlLog(url, mapParam))
    }

    fun urlD(url: String, mapParam: Map<String, Any>) {
        if (isEnabled) l('d', urlLog(url, mapParam))
    }

    fun urlI(url: String, mapParam: Map<String, Any>) {
        if (isEnabled) l('i', urlLog(url, mapParam))
    }

    fun urlW(url: String, mapParam: Map<String, Any>) {
        if (isEnabled) l('w', urlLog(url, mapParam))
    }

    fun urlE(url: String, mapParam: Map<String, Any>) {
        if (isEnabled) l('e', urlLog(url, mapParam))
    }

    private fun l(type: Char, log: String) {
        try {
            val isWriteToFile = logPath?.isNotEmpty() == true
            val logs = createLog(log)
            log(type, logs)
            if (isWriteToFile) {
                writeToFile(logs[0], logs[1])
            }
        } catch (e: Exception) {
            val msg = e.message
            if (!TextUtils.isEmpty(msg)) {
                android.util.Log.w("[L]156", msg!!)
            }
        }
    }

    private fun log(level: Char, logs: Array<String>) {
        log(level, logs[0], logs[1])
    }

    /**
     * when log is too long,split it
     *
     * @param level
     * @param tag
     * @param text
     */
    private fun log(level: Char, tag: String, text: String) {
        var text = text
        val PART_LEN = 3000
        do {
            val clipLen = if (text.length > PART_LEN) PART_LEN else text.length
            val clipText = text.substring(0, clipLen)
            text = if (clipText.length == text.length) "" else text.substring(clipLen)
            when (level) {
                'i' -> android.util.Log.i(tag, clipText)
                'd' -> android.util.Log.d(tag, clipText)
                'w' -> android.util.Log.w(tag, clipText)
                'v' -> android.util.Log.v(tag, clipText)
                'e' -> android.util.Log.e(tag, clipText)
                else -> {}
            }
        } while (text.isNotEmpty())
    }

    fun jsonLog(message: String): String {
        if (TextUtils.isEmpty(message)) {
            return ""
        }
        try {
            val job = message.indexOf("{")
            val joe = message.lastIndexOf("}")
            val jab = message.indexOf("[")
            val jae = message.lastIndexOf("]")

            /**
             * -1,不存在json格式字符串
             * 0,jsonobject
             * 1,jsonarray
             */
            val type: Int = if (job != -1 && (-1 == jab || job < jab) && joe != -1 && joe > job) {
                0
            } else if (jab != -1 && (-1 == job || jab < job) && jae != -1 && jae > jab) {
                1
            } else {
                -1
            }
            return if (type == -1) {
                message
            } else {
                val jsonLog = StringBuilder()
                when (type) {
                    0 -> {
                        jsonLog.append(message.substring(0, job)).append(LINE_SEPARATOR)
                        jsonLog.append(
                            JSONObject(message.substring(job, joe + 1)).toString(
                                JSON_INDENT
                            )
                        ).append(LINE_SEPARATOR)
                        jsonLog.append(message.substring(joe + 1, message.length))
                            .append(LINE_SEPARATOR)
                    }

                    1 -> {
                        jsonLog.append(message.substring(0, jab)).append(LINE_SEPARATOR)
                        jsonLog.append(
                            JSONArray(message.substring(jab, jae + 1)).toString(
                                JSON_INDENT
                            )
                        ).append(LINE_SEPARATOR)
                        jsonLog.append(message.substring(jae + 1, message.length))
                            .append(LINE_SEPARATOR)
                    }

                    else -> {}
                }
                jsonLog.toString()
            }
        } catch (e: Exception) {
            val msg = e.message
            if (!TextUtils.isEmpty(msg)) {
                android.util.Log.w("xlog", msg!!)
            }
        }
        return ""
    }

    fun urlLog(url: String, mapParam: Map<String, Any>): String {
        var urlQeury = url
        val stringBuilder = StringBuilder()
        stringBuilder.append("$urlQeury?")
        if (!mapParam.isEmpty()) {
            val entrySet = mapParam.entries
            for ((key, value) in entrySet) {
                stringBuilder.append("$key=$value&")
            }
        }
        urlQeury = stringBuilder.toString()
        return urlQeury.substring(0, urlQeury.length - 1)
    }

    fun line(top: Boolean) {
        if (top) {
            l(
                'v',
                "╔═══════════════════════════════════════════════════════════════════════════════════════"
            )
        } else {
            l(
                'v',
                "╚═══════════════════════════════════════════════════════════════════════════════════════"
            )
        }
    }

    private fun createLog(log: String, depth: Int = 7): Array<String> =
        arrayOf(sCommonFilterTag + filterTag, getFileNameMethodLineNumber(depth) + log)

    private fun writeToFile(tag: String, msg: String) {
        val date = Calendar.getInstance().time
        val logName = String.format(
            "%1$04d%2$02d%3$02d.txt",
            date.year + 1900,
            date.month + 1,
            date.date
        )
        val fLogDir = File(logPath)
        if (!fLogDir.exists()) {
            if (!fLogDir.mkdirs()) {
                android.util.Log.e("", "create dir[$logPath]failed!!!")
                return
            }
        }
        try {
            val f = File(logPath + File.separator + logName)
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    android.util.Log.e("", "create file failed")
                    return
                }
            }
            val fout = FileOutputStream(f, true)
            val swriter = OutputStreamWriter(fout)
            val bwriter = BufferedWriter(swriter)
            bwriter.write(
                String.format(
                    "[%1$02d:%2$02d:%3$02d]%4$50s:%5\$s\n",
                    date.hours,
                    date.minutes,
                    date.seconds,
                    tag,
                    msg
                )
            )
            bwriter.flush()
            bwriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            android.util.Log.d("exception", e.message!!)
        }
    }

    /**
     * @param depth 2,the method it self;3,the method who call this method
     * @return filename + method name + line number
     */
    private fun getFileNameMethodLineNumber(depth: Int): String {
        var info = ""
        try {
            val e = Thread.currentThread().stackTrace[depth]
            if (!TextUtils.isEmpty(e.fileName) && !TextUtils.isEmpty(e.methodName)) {
                info = String.format(
                    "[%1\$s,%2\$s,%3\$s]",
                    e.fileName,
                    e.methodName,
                    e.lineNumber
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("log", "get stack trace element failed!!!")
        }
        return info
    }

    companion object {
        private const val sCommonFilterTag = "[log]"
        val log = Log()
    }
}