/**
 * IonDomo - CryptoUtils
 * Criado pela IONKOD | Fundador: Douglas Lúcio
 * GitHub: https://github.com/dlucioyauh
 * LinkedIn: https://www.linkedin.com/in/douglaslucio/
 * Empresa: https://www.linkedin.com/company/144483944/
 * 
 * Padrão: Híbrido RSA + AES-GCM (mesmo padrão usado em bancos)
 * Nenhuma chave é hardcoded. Todas vêm do SecureConfigManager.
 */
package com.ionkod.iondomo.utils

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtils {

    private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * Criptografa payload usando esquema híbrido:
     * 1. Gera chave AES aleatória (eficiente para dados grandes)
     * 2. Criptografa os dados com AES-GCM
     * 3. Criptografa a chave AES com RSA do backend
     */
    fun encryptPayload(plainText: String, publicKeyBase64: String): String {
        val publicKey = loadPublicKey(publicKeyBase64)
        val aesKey = generateAesKey()
        
        val iv = ByteArray(GCM_IV_LENGTH).also { 
            java.security.SecureRandom().nextBytes(it) 
        }
        
        // Criptografa dados com AES-GCM
        val aesCipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        }
        val encryptedData = aesCipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        // Criptografa a chave AES com RSA
        val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }
        val encryptedKey = rsaCipher.doFinal(aesKey.encoded)
        
        // Monta payload estruturado (IV + chave criptografada + dados criptografados)
        val combined = iv + encryptedKey + encryptedData
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun generateAesKey(): SecretKey {
        return KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey()
    }

    private fun loadPublicKey(publicKeyBase64: String): PublicKey {
        val keyBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }
}
