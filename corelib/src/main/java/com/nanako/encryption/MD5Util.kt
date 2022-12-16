package com.nanako.encryption

import com.nanako.encryption.AESUtil.SecretLen
import com.nanako.encryption.Base64Util
import com.nanako.encryption.AESUtil
import com.nanako.encryption.Constant.CipherMode
import com.nanako.encryption.MD5Util
import kotlin.jvm.JvmOverloads
import com.nanako.encryption.StringUtil
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5Util {
    /**
     * @param source 获取’utf-8‘字节
     * @return 将加密之后的字节用base64编码
     */
    fun encryptBase64(source: String): String {
        var ens = ""
        try {
            ens = encryptBase64(source.toByteArray(charset(Constant.Charset.UTF_8)))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return ens
    }

    /**
     * @param source 要加密的字节
     * @return 将加密之后的字节用base64编码
     */
    fun encryptBase64(source: ByteArray?): String {
        var sen = ""
        try {
            sen =
                String(Base64Util.encode(encrypt(source)), Charset.forName(Constant.Charset.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sen
    }

    /**
     * @param source 获取utf-8编码格式的字节
     * @return 将加密之后的字节转换成16进制的字符串
     */
    fun encryptHex(source: String): String {
        var sen = ""
        try {
            sen = encryptHex(source.toByteArray(charset(Constant.Charset.UTF_8)))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sen
    }

    /**
     * @param source 要加密的字节
     * @return 将加密之后的字节转换成16进制的字符串
     */
    fun encryptHex(source: ByteArray): String {
        return encrypt(source)?.let { StringUtil.byteToHexString(it) } ?: ""
    }

    fun encrypt(source: ByteArray?): ByteArray? {
        var sdigest: ByteArray? = null
        try {
            val bais = ByteArrayInputStream(source)
            val digest = encrypt(bais)
            bais.close()
            sdigest = digest
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sdigest
    }

    fun encrypt(`in`: InputStream): ByteArray? {
        var digest: ByteArray? = null
        try {
            val digester = MessageDigest.getInstance("MD5")
            val bytes = ByteArray(8192)
            var byteCount: Int
            while (`in`.read(bytes).also { byteCount = it } > 0) {
                digester.update(bytes, 0, byteCount)
            }
            digest = digester.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return digest
    }
}