package com.core.encryption

import android.util.Log
import com.core.encryption.Constant.CipherMode
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.charset.Charset
import java.security.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @创建密钥时要注意你用的是那种128，还是192还是256位 他们对应的密钥长度分别是16/24/32一个中文字符占用2个字节(有些CiperMode不支持192和256位的)
 * @IvParameterSpec必须16个字节长度 默认使用128位进行加密和解密
 */
object AESUtil {

    fun encryptBase64(secretSeed: String, content: String, iv: ByteArray?): String {
        var sen = ""
        try {
            sen = String(
                Base64Util.encode(
                    encrypt(
                        secretSeed.toByteArray(charset(Constant.Charset.UTF_8)),
                        content.toByteArray(
                            charset(
                                Constant.Charset.UTF_8
                            )
                        ), iv
                    )
                ), Charset.forName(Constant.Charset.UTF_8)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sen
    }

    fun decryptBase64(secretSeed: String, contentBase64: String, iv: ByteArray?): String {
        var den = ""
        try {
            den = String(
                decrypt(
                    secretSeed.toByteArray(charset(Constant.Charset.UTF_8)), Base64Util.decode(
                        contentBase64.toByteArray(
                            charset(
                                Constant.Charset.UTF_8
                            )
                        )
                    ), iv
                )!!, Charset.forName(Constant.Charset.UTF_8)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return den
    }

    fun encrypt(secretSeed: ByteArray, content: ByteArray?, iv: ByteArray?): ByteArray? {
        return work(true, secretSeed, content, iv)
    }

    fun decrypt(secretSeed: ByteArray, content: ByteArray?, iv: ByteArray?): ByteArray? {
        return work(false, secretSeed, content, iv)
    }

    private fun work(
        isEncrypt: Boolean,
        secretSeed: ByteArray,
        todoContent: ByteArray?,
        iv: ByteArray?
    ): ByteArray? {
        var result: ByteArray? = null
        try {
            val key = SecretKeySpec(secretSeed, Constant.Algorithm.AES)
            val cipher = Cipher.getInstance(CipherMode.AES_CBC_PKCS5Padding)
            val params = iv ?: "Xegh5pNZq0tTobQR".toByteArray()
            cipher.init(
                if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, key, IvParameterSpec(
                    params
                )
            ) //IvParameterSpec增加加密算法的强度
            result = if (isEncrypt) {
                cipher.doFinal(todoContent)
            } else {
                cipher.doFinal(todoContent)
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}