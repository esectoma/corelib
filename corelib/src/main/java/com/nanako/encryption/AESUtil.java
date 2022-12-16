package com.nanako.encryption;

import android.os.Build;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Administrator qbw
 * @创建密钥时要注意你用的是那种128，还是192还是256位 他们对应的密钥长度分别是16/24/32一个中文字符占用2个字节(有些CiperMode不支持192和256位的)
 * @IvParameterSpec必须16个字节长度 默认使用128位进行加密和解密
 */
public class AESUtil {

    public enum SecretLen {
        LEN_BIT_128,
        LEN_BIT_192,
        LEN_BIT_256,
    }

    private static int convertSecretLen(SecretLen secretLen) {
        switch (secretLen) {
            case LEN_BIT_192:
                return 192;
            case LEN_BIT_256:
                return 256;
            default:
                return 128;
        }
    }

    /**
     * 默认获取字节采用的utf-8编码
     *
     * @param secretSeed 密钥
     * @param content    要加密的内容
     * @return 加密之后采用base64转码
     */
    public static String encryptBase64(String secretSeed, String content) {
        String sen = "";
        try {
            sen = new String(Base64Util.encode(encrypt(secretSeed.getBytes(Constant.Charset.UTF_8), content.getBytes(Constant.Charset.UTF_8))), Constant.Charset.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sen;
    }

    /**
     * 默认获取字节采用的utf-8编码
     *
     * @param secretSeed    密钥
     * @param contentBase64 要解密经过base64转码的字符串
     * @return 解密之后的内容
     */
    public static String decryptBase64(String secretSeed, String contentBase64) {
        String den = "";
        try {
            den = new String(decrypt(secretSeed.getBytes(Constant.Charset.UTF_8), Base64Util.decode(contentBase64.getBytes(Constant.Charset.UTF_8))), Constant.Charset.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return den;
    }

    /**
     * 参数以及返回值都是纯字节
     *
     * @param secretSeed 密钥
     * @param content    要加密的字节
     * @return 加密之后的字节
     */
    public static byte[] encrypt(byte[] secretSeed, byte[] content) {
        return work(true, secretSeed, SecretLen.LEN_BIT_128, content);
    }

    /**
     * 参数以及返回值都是纯字节
     *
     * @param secretSeed 密钥
     * @param content    要解密的字节
     * @return 解密之后的字节
     */
    public static byte[] decrypt(byte[] secretSeed, byte[] content) {
        return work(false, secretSeed, SecretLen.LEN_BIT_128, content);
    }

    public static byte[] encrypt(byte[] secretSeed, SecretLen secretLen, byte[] content) {
        return work(true, secretSeed, secretLen, content);
    }

    public static byte[] decrypt(byte[] secretSeed, SecretLen secretLen, byte[] content) {
        return work(false, secretSeed, secretLen, content);
    }

    private static byte[] work(boolean isEncrypt, byte[] secretSeed, SecretLen secretLen, byte[] todoContent) {
        try {
            int iSecretLen = convertSecretLen(secretLen);
            int iSecrentLenByte = iSecretLen / 8;
            byte[] secretSeedBytes = secretSeed;
            if (secretSeedBytes.length == iSecrentLenByte) {
                return work(isEncrypt, secretSeedBytes, iSecretLen, todoContent);
            } else if (secretSeedBytes.length > iSecrentLenByte) {
                Log.w("AES", "密钥种子最好使用英文,如果使用了中文,获取的字节长度可能与期望不符.当种子长度大于'secretLen'时会截取!");
                byte[] _secretSeedBytes = new byte[iSecrentLenByte];
                System.arraycopy(secretSeedBytes, 0, _secretSeedBytes, 0, iSecrentLenByte);
                return work(isEncrypt, _secretSeedBytes, iSecretLen, todoContent);
            } else if (secretSeedBytes.length < iSecrentLenByte) {
                Log.e("AES", "密钥种子长度应该为128,192或者256位!");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param isEncrypt   是否为加密模式
     * @param secretSeed  密钥种子,密钥是根据密钥种子和密钥长度生成的
     * @param secretLen   密钥长度,只支持128,192,256三种(192,256两种有可能不支持)
     * @param todoContent 需要被加密或者解密的字符串
     * @return
     */
    private static byte[] work(boolean isEncrypt, byte[] secretSeed, int secretLen, byte[] todoContent) {
        byte[] result = null;
        try {
            SecretKeySpec key = new SecretKeySpec(secretSeed, Constant.Algorithm.AES);
            Cipher cipher = Cipher.getInstance(Constant.CipherMode.AES_CBC_PKCS5Padding);
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key, new IvParameterSpec(getRawKey(secretLen, secretSeed)));//IvParameterSpec增加加密算法的强度
            if (isEncrypt) {
                result = cipher.doFinal(todoContent);
            } else {
                result = cipher.doFinal(todoContent);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param secretLen  密钥长度,只支持128,192,256三种(192,256两种有可能不支持)
     * @param secretSeed 密钥种子,密钥是根据密钥种子和密钥长度生成的
     * @return
     */
    private static byte[] getRawKey(int secretLen, byte[] secretSeed) {
        byte[] rawkey = null;
        try {
            SecureRandom sr;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                sr = SecureRandom.getInstance(Constant.Algorithm.SHA1PRNG, Constant.Provider.CRYPTO);
            } else {
                sr = SecureRandom.getInstance(Constant.Algorithm.SHA1PRNG);
            }
            sr.setSeed(secretSeed);

            KeyGenerator kg = KeyGenerator.getInstance(Constant.Algorithm.AES);
            kg.init(secretLen, sr);

            SecretKey sk = kg.generateKey();
            rawkey = sk.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawkey;
    }
}
