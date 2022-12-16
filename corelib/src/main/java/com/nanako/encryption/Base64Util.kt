package com.nanako.encryption

import android.util.Base64
import com.nanako.encryption.AESUtil.SecretLen
import com.nanako.encryption.Base64Util
import com.nanako.encryption.AESUtil
import com.nanako.encryption.Constant.CipherMode
import com.nanako.encryption.MD5Util
import kotlin.jvm.JvmOverloads
import com.nanako.encryption.StringUtil
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

object Base64Util {
    fun encode(content: String): String? {
        var result: String? = null
        try {
            result = String(encode(content.toByteArray(charset(Constant.Charset.UTF_8))))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    fun encode(bs: ByteArray?): ByteArray {
        return Base64.encode(bs, Base64.DEFAULT)
    }

    fun decode(content: String): String? {
        var result: String? = null
        try {
            result = String(decode(content.toByteArray()), Charset.forName(Constant.Charset.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    fun decode(bs: ByteArray?): ByteArray {
        return Base64.decode(bs, Base64.DEFAULT)
    }
}