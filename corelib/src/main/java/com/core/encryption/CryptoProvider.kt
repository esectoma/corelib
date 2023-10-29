package com.core.encryption

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