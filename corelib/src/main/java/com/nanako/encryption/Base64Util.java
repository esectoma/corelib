package com.nanako.encryption;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Util {

	public static String encode(String content) {
		String result = null;
		try {
			result = new String(encode(content.getBytes(Constant.Charset.UTF_8)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static byte[] encode(byte[] bs) {
		return Base64.encode(bs, Base64.DEFAULT);
	}
	
	public static String decode(String content) {
		String result = null;
		try {
			result = new String(decode(content.getBytes()), Constant.Charset.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static byte[] decode(byte[] bs) {
		return Base64.decode(bs, Base64.DEFAULT);
	}
}
