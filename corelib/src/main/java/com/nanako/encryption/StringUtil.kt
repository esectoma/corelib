package com.nanako.encryption;

import android.util.Log;

import java.util.HashMap;

public class StringUtil {

	public static String byteToHexString(byte[] bytes) {
		return byteToHexString(bytes, true);
	}

	public static String byteToHexString(byte[] bytes, boolean isUpperase) {
		StringBuffer sbytes = new StringBuffer();
		for (byte b : bytes) {
			sbytes.append(String.format(isUpperase ? "%02X" : "%02x", b));
		}
		return sbytes.toString();
	}

	public static byte[] hexStringToByte(String hexString) {
		HashMap<Character, Integer> mapHexToDec = initHexToDec();
		hexString = hexString.toUpperCase();
		int hexStringLength = hexString.length();
		if (hexStringLength % 2 != 0) {
			Log.i("", String.format("wrong hexString[%s]", hexString));
			return null;
		}

		int bytesLength = hexStringLength / 2;
		byte[] bytes = new byte[bytesLength];
		for (int i = 0; i < bytesLength; i++) {
			Integer p = mapHexToDec.get(hexString.charAt(2 * i));
			Integer b = mapHexToDec.get(hexString.charAt(2 * i + 1));
			byte bt = (byte) (p << 4 | b);
			bytes[i] = bt;
		}

		return bytes;
	}

	private static HashMap<Character, Integer> initHexToDec() {
		HashMap<Character, Integer> mapHexToDec = new HashMap<Character, Integer>();
		mapHexToDec.put('0', 0);
		mapHexToDec.put('1', 1);
		mapHexToDec.put('2', 2);
		mapHexToDec.put('3', 3);
		mapHexToDec.put('4', 4);
		mapHexToDec.put('5', 5);
		mapHexToDec.put('6', 6);
		mapHexToDec.put('7', 7);
		mapHexToDec.put('8', 8);
		mapHexToDec.put('9', 9);
		mapHexToDec.put('A', 10);
		mapHexToDec.put('B', 11);
		mapHexToDec.put('C', 12);
		mapHexToDec.put('D', 13);
		mapHexToDec.put('E', 14);
		mapHexToDec.put('F', 15);
		return mapHexToDec;
	}
}
