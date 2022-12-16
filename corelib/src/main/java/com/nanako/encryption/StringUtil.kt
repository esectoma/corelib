package com.nanako.encryption

import android.util.Log
import com.nanako.encryption.AESUtil.SecretLen
import com.nanako.encryption.Base64Util
import com.nanako.encryption.AESUtil
import com.nanako.encryption.Constant.CipherMode
import com.nanako.encryption.MD5Util
import kotlin.jvm.JvmOverloads
import com.nanako.encryption.StringUtil
import java.util.*

object StringUtil {
    @JvmOverloads
    fun byteToHexString(bytes: ByteArray, isUpperase: Boolean = true): String {
        val sbytes = StringBuffer()
        for (b in bytes) {
            sbytes.append(String.format(if (isUpperase) "%02X" else "%02x", b))
        }
        return sbytes.toString()
    }

    fun hexStringToByte(hexString: String): ByteArray? {
        var hexString = hexString
        val mapHexToDec = initHexToDec()
        hexString = hexString.uppercase(Locale.getDefault())
        val hexStringLength = hexString.length
        if (hexStringLength % 2 != 0) {
            Log.i("", String.format("wrong hexString[%s]", hexString))
            return null
        }
        val bytesLength = hexStringLength / 2
        val bytes = ByteArray(bytesLength)
        for (i in 0 until bytesLength) {
            val p = mapHexToDec[hexString[2 * i]]
            val b = mapHexToDec[hexString[2 * i + 1]]
            val bt = (p!! shl 4 or b!!).toByte()
            bytes[i] = bt
        }
        return bytes
    }

    private fun initHexToDec(): HashMap<Char, Int> {
        val mapHexToDec = HashMap<Char, Int>()
        mapHexToDec['0'] = 0
        mapHexToDec['1'] = 1
        mapHexToDec['2'] = 2
        mapHexToDec['3'] = 3
        mapHexToDec['4'] = 4
        mapHexToDec['5'] = 5
        mapHexToDec['6'] = 6
        mapHexToDec['7'] = 7
        mapHexToDec['8'] = 8
        mapHexToDec['9'] = 9
        mapHexToDec['A'] = 10
        mapHexToDec['B'] = 11
        mapHexToDec['C'] = 12
        mapHexToDec['D'] = 13
        mapHexToDec['E'] = 14
        mapHexToDec['F'] = 15
        return mapHexToDec
    }
}