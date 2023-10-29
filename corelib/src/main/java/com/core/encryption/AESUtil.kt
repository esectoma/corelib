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
    private fun convertSecretLen(secretLen: SecretLen): Int {
        return when (secretLen) {
            SecretLen.LEN_BIT_192 -> 192
            SecretLen.LEN_BIT_256 -> 256
            else -> 128
        }
    }

    /**
     * 默认获取字节采用的utf-8编码
     *
     * @param secretSeed 密钥
     * @param content    要加密的内容
     * @return 加密之后采用base64转码
     */
    fun encryptBase64(secretSeed: String, content: String): String {
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
                        )
                    )
                ), Charset.forName(Constant.Charset.UTF_8)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sen
    }

    /**
     * 默认获取字节采用的utf-8编码
     *
     * @param secretSeed    密钥
     * @param contentBase64 要解密经过base64转码的字符串
     * @return 解密之后的内容
     */
    fun decryptBase64(secretSeed: String, contentBase64: String): String {
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
                    )
                )!!, Charset.forName(Constant.Charset.UTF_8)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return den
    }

    /**
     * 参数以及返回值都是纯字节
     *
     * @param secretSeed 密钥
     * @param content    要加密的字节
     * @return 加密之后的字节
     */
    fun encrypt(secretSeed: ByteArray, content: ByteArray?): ByteArray? {
        return work(true, secretSeed, SecretLen.LEN_BIT_128, content)
    }

    /**
     * 参数以及返回值都是纯字节
     *
     * @param secretSeed 密钥
     * @param content    要解密的字节
     * @return 解密之后的字节
     */
    fun decrypt(secretSeed: ByteArray, content: ByteArray?): ByteArray? {
        return work(false, secretSeed, SecretLen.LEN_BIT_128, content)
    }

    fun encrypt(secretSeed: ByteArray, secretLen: SecretLen, content: ByteArray?): ByteArray? {
        return work(true, secretSeed, secretLen, content)
    }

    fun decrypt(secretSeed: ByteArray, secretLen: SecretLen, content: ByteArray?): ByteArray? {
        return work(false, secretSeed, secretLen, content)
    }

    private fun work(
        isEncrypt: Boolean,
        secretSeed: ByteArray,
        secretLen: SecretLen,
        todoContent: ByteArray?
    ): ByteArray? {
        try {
            val iSecretLen = convertSecretLen(secretLen)
            val iSecrentLenByte = iSecretLen / 8
            if (secretSeed.size == iSecrentLenByte) {
                return work(isEncrypt, secretSeed, iSecretLen, todoContent)
            } else if (secretSeed.size > iSecrentLenByte) {
                Log.w("AES", "密钥种子最好使用英文,如果使用了中文,获取的字节长度可能与期望不符.当种子长度大于'secretLen'时会截取!")
                val _secretSeedBytes = ByteArray(iSecrentLenByte)
                System.arraycopy(secretSeed, 0, _secretSeedBytes, 0, iSecrentLenByte)
                return work(isEncrypt, _secretSeedBytes, iSecretLen, todoContent)
            } else if (secretSeed.size < iSecrentLenByte) {
                Log.e("AES", "密钥种子长度应该为128,192或者256位!")
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * @param isEncrypt   是否为加密模式
     * @param secretSeed  密钥种子,密钥是根据密钥种子和密钥长度生成的
     * @param secretLen   密钥长度,只支持128,192,256三种(192,256两种有可能不支持)
     * @param todoContent 需要被加密或者解密的字符串
     * @return
     */
    private fun work(
        isEncrypt: Boolean,
        secretSeed: ByteArray,
        secretLen: Int,
        todoContent: ByteArray?
    ): ByteArray? {
        var result: ByteArray? = null
        try {
            val key = SecretKeySpec(secretSeed, Constant.Algorithm.AES)
            val cipher = Cipher.getInstance(CipherMode.AES_CBC_PKCS5Padding)
            cipher.init(
                if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, key, IvParameterSpec(
                    getRawKey(secretLen, secretSeed)
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

    /**
     * @param secretLen  密钥长度,只支持128,192,256三种(192,256两种有可能不支持)
     * @param secretSeed 密钥种子,密钥是根据密钥种子和密钥长度生成的
     * @return
     */
    private fun getRawKey(secretLen: Int, secretSeed: ByteArray): ByteArray? {
        var rawkey: ByteArray? = null
        try {
            val sr: SecureRandom
            /*sr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                SecureRandom.getInstance(Constant.Algorithm.SHA1PRNG, Constant.Provider.CRYPTO)
            } else {
                SecureRandom.getInstance(Constant.Algorithm.SHA1PRNG)
            }*/
            sr = SecureRandom.getInstance(Constant.Algorithm.SHA1PRNG)
            sr.setSeed(secretSeed)
            val kg = KeyGenerator.getInstance(Constant.Algorithm.AES)
            kg.init(secretLen, sr)
            val sk = kg.generateKey()
            rawkey = sk.encoded
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rawkey
    }

    enum class SecretLen {
        LEN_BIT_128, LEN_BIT_192, LEN_BIT_256
    }
}