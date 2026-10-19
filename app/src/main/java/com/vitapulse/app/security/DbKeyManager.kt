package com.vitapulse.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Envelope encryption for the database key:
 *  - A random 256-bit passphrase encrypts the SQLCipher database.
 *  - That passphrase is itself encrypted (AES-256-GCM) by a key that lives in the
 *    Android Keystore (StrongBox hardware when available) and can never be exported.
 */
object DbKeyManager {
    private const val KEY_ALIAS = "vitapulse_db_kek"
    private const val PREFS = "vp_secure"
    private const val PREF_BLOB = "db_key_blob"
    private const val GCM_IV_BYTES = 12

    @Synchronized
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(PREF_BLOB, null)?.let { stored ->
            val blob = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKek(),
                GCMParameterSpec(128, blob.copyOfRange(0, GCM_IV_BYTES)))
            return cipher.doFinal(blob.copyOfRange(GCM_IV_BYTES, blob.size))
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        // Hex-encode so the passphrase is plain ASCII for SQLCipher.
        val passphrase = raw.joinToString("") { "%02x".format(it) }.toByteArray(Charsets.US_ASCII)
        raw.fill(0)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKek())
        val blob = cipher.iv + cipher.doFinal(passphrase)
        prefs.edit().putString(PREF_BLOB, Base64.encodeToString(blob, Base64.NO_WRAP)).commit()
        return passphrase
    }

    private fun getOrCreateKek(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return try {
            generate(strongBox = true)
        } catch (e: StrongBoxUnavailableException) {
            generate(strongBox = false)
        }
    }

    private fun generate(strongBox: Boolean): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .setIsStrongBoxBacked(strongBox)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply { init(spec) }
            .generateKey()
    }
}
