package com.nanako.encryption

import com.nanako.encryption.AESUtil.SecretLen
import com.nanako.encryption.Base64Util
import com.nanako.encryption.AESUtil
import com.nanako.encryption.Constant.CipherMode
import com.nanako.encryption.MD5Util
import kotlin.jvm.JvmOverloads
import com.nanako.encryption.StringUtil
import java.security.Provider

class CryptoProvider :
    Provider("Crypto", 1.0, "HARMONY (SHA1 digest; SecureRandom; SHA1withDSA signature)") {
    init {
        put(
            "SecureRandom.SHA1PRNG",
            "org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl"
        )
        put("SecureRandom.SHA1PRNG ImplementedIn", "Software")
    }
}