package com.nanako.encryption;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 1.RSA加密解密：
 * 　(1)获取密钥，这里是产生密钥，实际应用中可以从各种存储介质上读取密钥 (2)加密 (3)解密
 * 2.RSA签名和验证
 * 　(1)获取密钥，这里是产生密钥，实际应用中可以从各种存储介质上读取密钥 (2)获取待签名的Hash码 (3)获取签名的字符串 (4)验证
 * <p/>
 * 3.公钥与私钥的理解：
 * 　(1)私钥用来进行解密和签名，是给自己用的。
 * 　(2)公钥由本人公开，用于加密和验证签名，是给别人用的。
 * (3)当该用户发送文件时，用私钥签名，别人用他给的公钥验证签名，可以保证该信息是由他发送的。当该用户接受文件时，别人用他的公钥加密，他用私钥解密，可以保证该信息只能由他接收到。
 */

public class RSAUtil {

    /**
     * @return 返回公钥和私钥
     */
    public static byte[][] randomGetKyes() {
        byte[][] keys = new byte[2][];

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constant.Algorithm.RSA);
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();
            PublicKey puk = kp.getPublic();
            PrivateKey prK = kp.getPrivate();
            keys[0] = puk.getEncoded();
            keys[1] = prK.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return keys;
    }

    /**
     * @return base64转码公钥和私钥
     */
    public static String[] randomGetKyesBase64() {
        String[] keystrs = null;
        try {
            byte[][] keys = randomGetKyes();
            keystrs = new String[2];
            keystrs[0] = new String(Base64Util.encode(keys[0]), Constant.Charset.UTF_8);
            keystrs[1] = new String(Base64Util.encode(keys[1]), Constant.Charset.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keystrs;
    }

    /**
     * @param publicKeyBase64 Base64转码之后的publickey
     * @return
     */
    public static PublicKey getPublicKeyBase64(String publicKeyBase64) {
        PublicKey pk = null;
        try {
            pk = getPublicKey(Base64Util.decode(publicKeyBase64.getBytes(Constant.Charset.UTF_8)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pk;
    }

    public static PublicKey getPublicKey(byte[] publicKey) {
        PublicKey pk = null;
        X509EncodedKeySpec xeks = new X509EncodedKeySpec(publicKey);
        try {
            KeyFactory kf = KeyFactory.getInstance(Constant.Algorithm.RSA);
            pk = kf.generatePublic(xeks);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pk;
    }

    /**
     * @param privateKeyBase64 经过转码之后的privateKey
     * @return
     */
    public static PrivateKey getPrivateKeyBase64(String privateKeyBase64) {
        PrivateKey pk = null;
        try {
            pk = getPrivateKey(Base64Util.decode(privateKeyBase64.getBytes(Constant.Charset.UTF_8)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pk;
    }

    public static PrivateKey getPrivateKey(byte[] privateKey) {
        PrivateKey pk = null;
        PKCS8EncodedKeySpec peks = new PKCS8EncodedKeySpec(privateKey);
        try {
            KeyFactory kf = KeyFactory.getInstance(Constant.Algorithm.RSA);
            pk = kf.generatePrivate(peks);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pk;
    }

    /**
     * @param publicKeyBase64 转码后的公钥
     * @param content 要加密的字符串
     */
    public static String encryptBase64(String publicKeyBase64, String content) {
        return encrypt(getPublicKeyBase64(publicKeyBase64), content);
    }

    /**
     * @param privateKeyBase64 转码后的私钥
     * @param content 要解密的字符串
     */
    public static String decryptBase64(String privateKeyBase64, String content) {
        return decrypt(getPrivateKeyBase64(privateKeyBase64), content);
    }

    /**
     * @param publicKey 公钥
     * @param content   默认使用utf-8编码获取字节
     * @return 加密之后的字节，通过base64转码，然后转换成utf-8格式的字符串
     */
    public static String encrypt(PublicKey publicKey, String content) {
        String result = "";
        try {
            byte[] bs = encrypt(publicKey, content.getBytes(Constant.Charset.UTF_8));
            bs = Base64Util.encode(bs);
            result = new String(bs, Constant.Charset.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param publicKey 公钥
     * @param content   需要加密的字节
     * @return 加密之后的字节
     */
    public static byte[] encrypt(PublicKey publicKey, byte[] content) {
        byte[] result = null;
        try {
            Cipher cp = Cipher.getInstance(Constant.Algorithm.RSA_CIPHER);
            cp.init(Cipher.ENCRYPT_MODE, publicKey);
            result = cp.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 先获取'utf-8'字节,在通过base64解码,然后在解密,解密成功后构造'utf-8'字符串
     *
     * @param privateKey 私钥
     * @param content    要解密的字符串
     * @return 解密之后的字符串
     */
    public static String decrypt(PrivateKey privateKey, String content) {
        String result = "";
        try {
            byte[] bs = content.getBytes(Constant.Charset.UTF_8);
            bs = Base64Util.decode(bs);
            result = new String(decrypt(privateKey, bs), Constant.Charset.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param privateKey 私钥
     * @param content    要解密的字节
     * @return 解密后的字节
     */
    public static byte[] decrypt(PrivateKey privateKey, byte[] content) {
        byte[] result = null;
        try {
            Cipher cp = Cipher.getInstance(Constant.Algorithm.RSA_CIPHER);
            cp.init(Cipher.DECRYPT_MODE, privateKey);
            result = cp.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }
}
