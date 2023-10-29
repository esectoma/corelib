package com.core.encryption

class Constant {
    object CipherMode {
        val AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding"
    }

    object Algorithm {
        val AES: String = "AES"
        val MD5: String = "MD5"
        val SHA1PRNG: String = "SHA1PRNG"
        val RSA: String = "RSA"
        val RSA_CIPHER: String = "RSA/ECB/PKCS1Padding"
    }

    object Charset {
        val UTF_8: String = "UTF-8"
    }

    object Provider {
        const val CRYPTO = "Crypto"
    }
}