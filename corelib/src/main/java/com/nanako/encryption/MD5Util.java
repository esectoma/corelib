package com.nanako.encryption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    /**
     * @param source 获取’utf-8‘字节
     * @return 将加密之后的字节用base64编码
     */
    public static String encryptBase64(String source) {
        String ens = "";
        try {
            ens = encryptBase64(source.getBytes(Constant.Charset.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ens;
    }

    /**
     * @param source 要加密的字节
     * @return 将加密之后的字节用base64编码
     */
    public static String encryptBase64(byte[] source) {
        String sen = "";
        try {
            sen = new String(Base64Util.encode(encrypt(source)), Constant.Charset.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sen;
    }

    /**
     * @param source 获取utf-8编码格式的字节
     * @return 将加密之后的字节转换成16进制的字符串
     */
    public static String encryptHex(String source) {
        String sen = "";
        try {
            sen = encryptHex(source.getBytes(Constant.Charset.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sen;
    }

    /**
     * @param source 要加密的字节
     * @return 将加密之后的字节转换成16进制的字符串
     */
    public static String encryptHex(byte[] source) {
        return StringUtil.byteToHexString(encrypt(source));
    }

    public static byte[] encrypt(byte[] source) {
        byte[] sdigest = null;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(source);
            byte[] digest = encrypt(bais);
            bais.close();
            sdigest = digest;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sdigest;
    }

    public static byte[] encrypt(InputStream in) {
        byte[] digest = null;

        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            digest = digester.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return digest;
    }
}
