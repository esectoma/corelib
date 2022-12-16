package com.nanako.encryption;

public class Constant {
	public static class CipherMode {
		public static final String AES_CBC_PKCS5Padding = new String("AES/CBC/PKCS5Padding");
	}
	
	public static class Algorithm {
		public static final String AES = new String("AES");
		public static final String MD5 = new String("MD5");
		public static final String SHA1PRNG = new String("SHA1PRNG");
		public static final String RSA = new String("RSA");
		public static final String RSA_CIPHER = new String("RSA/ECB/PKCS1Padding");
	}
	
	public static class Charset {
		public static final String UTF_8 = new String("UTF-8");
	}

	public static class Provider {
		public static final String CRYPTO = "Crypto";
	}
}
