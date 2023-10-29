package com.core.encryption

import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.nio.charset.Charset
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * 1.RSA加密解密：
 * 　(1)获取密钥，这里是产生密钥，实际应用中可以从各种存储介质上读取密钥 (2)加密 (3)解密
 * 2.RSA签名和验证
 * 　(1)获取密钥，这里是产生密钥，实际应用中可以从各种存储介质上读取密钥 (2)获取待签名的Hash码 (3)获取签名的字符串 (4)验证
 *
 *
 * 3.公钥与私钥的理解：
 * 　(1)私钥用来进行解密和签名，是给自己用的。
 * 　(2)公钥由本人公开，用于加密和验证签名，是给别人用的。
 * (3)当该用户发送文件时，用私钥签名，别人用他给的公钥验证签名，可以保证该信息是由他发送的。当该用户接受文件时，别人用他的公钥加密，他用私钥解密，可以保证该信息只能由他接收到。
 */
object RSAUtil {
    /**
     * @return 返回公钥和私钥
     */
    fun randomGetKyes(): Array<ByteArray?> {
        val keys = arrayOfNulls<ByteArray>(2)
        try {
            val kpg = KeyPairGenerator.getInstance(Constant.Algorithm.RSA)
            kpg.initialize(1024)
            val kp = kpg.generateKeyPair()
            val puk = kp.public
            val prK = kp.private
            keys[0] = puk.encoded
            keys[1] = prK.encoded
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return keys
    }

    /**
     * @return base64转码公钥和私钥
     */
    fun randomGetKyesBase64(): Array<String?>? {
        var keystrs: Array<String?>? = null
        try {
            val keys = randomGetKyes()
            keystrs = arrayOfNulls(2)
            keystrs[0] = String(Base64Util.encode(keys[0]), Charset.forName(Constant.Charset.UTF_8))
            keystrs[1] = String(Base64Util.encode(keys[1]), Charset.forName(Constant.Charset.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return keystrs
    }

    /**
     * @param publicKeyBase64 Base64转码之后的publickey
     * @return
     */
    fun getPublicKeyBase64(publicKeyBase64: String): PublicKey? {
        var pk: PublicKey? = null
        try {
            pk =
                getPublicKey(Base64Util.decode(publicKeyBase64.toByteArray(charset(Constant.Charset.UTF_8))))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return pk
    }

    fun getPublicKey(publicKey: ByteArray?): PublicKey? {
        var pk: PublicKey? = null
        val xeks = X509EncodedKeySpec(publicKey)
        try {
            val kf = KeyFactory.getInstance(Constant.Algorithm.RSA)
            pk = kf.generatePublic(xeks)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return pk
    }

    /**
     * @param privateKeyBase64 经过转码之后的privateKey
     * @return
     */
    fun getPrivateKeyBase64(privateKeyBase64: String): PrivateKey? {
        var pk: PrivateKey? = null
        try {
            pk =
                getPrivateKey(Base64Util.decode(privateKeyBase64.toByteArray(charset(Constant.Charset.UTF_8))))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return pk
    }

    fun getPrivateKey(privateKey: ByteArray?): PrivateKey? {
        var pk: PrivateKey? = null
        val peks = PKCS8EncodedKeySpec(privateKey)
        try {
            val kf = KeyFactory.getInstance(Constant.Algorithm.RSA)
            pk = kf.generatePrivate(peks)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return pk
    }

    /**
     * @param publicKeyBase64 转码后的公钥
     * @param content 要加密的字符串
     */
    fun encryptBase64(publicKeyBase64: String, content: String): String {
        return encrypt(getPublicKeyBase64(publicKeyBase64), content)
    }

    /**
     * @param privateKeyBase64 转码后的私钥
     * @param content 要解密的字符串
     */
    fun decryptBase64(privateKeyBase64: String, content: String): String {
        return decrypt(getPrivateKeyBase64(privateKeyBase64), content)
    }

    /**
     * @param publicKey 公钥
     * @param content   默认使用utf-8编码获取字节
     * @return 加密之后的字节，通过base64转码，然后转换成utf-8格式的字符串
     */
    fun encrypt(publicKey: PublicKey?, content: String): String {
        var result = ""
        try {
            var bs = encrypt(publicKey, content.toByteArray(charset(Constant.Charset.UTF_8)))
            bs = Base64Util.encode(bs)
            result = String(bs, Charset.forName(Constant.Charset.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * @param publicKey 公钥
     * @param content   需要加密的字节
     * @return 加密之后的字节
     */
    fun encrypt(publicKey: PublicKey?, content: ByteArray?): ByteArray? {
        var result: ByteArray? = null
        try {
            val cp = Cipher.getInstance(Constant.Algorithm.RSA_CIPHER)
            cp.init(Cipher.ENCRYPT_MODE, publicKey)
            result = cp.doFinal(content)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 先获取'utf-8'字节,在通过base64解码,然后在解密,解密成功后构造'utf-8'字符串
     *
     * @param privateKey 私钥
     * @param content    要解密的字符串
     * @return 解密之后的字符串
     */
    fun decrypt(privateKey: PrivateKey?, content: String): String {
        var result = ""
        try {
            var bs: ByteArray? = content.toByteArray(charset(Constant.Charset.UTF_8))
            bs = Base64Util.decode(bs)
            result = String(decrypt(privateKey, bs)!!, Charset.forName(Constant.Charset.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * @param privateKey 私钥
     * @param content    要解密的字节
     * @return 解密后的字节
     */
    fun decrypt(privateKey: PrivateKey?, content: ByteArray?): ByteArray? {
        var result: ByteArray? = null
        try {
            val cp = Cipher.getInstance(Constant.Algorithm.RSA_CIPHER)
            cp.init(Cipher.DECRYPT_MODE, privateKey)
            result = cp.doFinal(content)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return result
    }
}