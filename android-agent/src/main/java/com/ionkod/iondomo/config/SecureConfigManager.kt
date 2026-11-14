/**
 * IonDomo - Secure Configuration Manager
 * Criado pela IONKOD | Fundador: Douglas Lúcio
 * GitHub: https://github.com/dlucioyauh
 * LinkedIn: https://www.linkedin.com/in/douglaslucio/
 * Empresa: https://www.linkedin.com/company/144483944/
 * 
 * Propósito: Centralizar o carregamento de configurações sensíveis.
 * Regra: NUNCA fornecer valores fallback (hardcoded) para chaves de produção.
 */
package com.ionkod.iondomo.config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.InvalidParameterException

object SecureConfigManager {

    private const val PREFS_NAME = "iondomo_secure_prefs"
    
    // Chaves de referência (não são segredos, apenas identificadores)
    private const val KEY_API_URL = "api_base_url"
    private const val KEY_TELEMETRY_PUB_KEY = "telemetry_pub_key"

    private lateinit var encryptedPrefs: EncryptedSharedPreferences

    fun initialize(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun getApiBaseUrl(): String {
        return encryptedPrefs.getString(KEY_API_URL, null) 
            ?: throw InvalidParameterException("CRITICAL: API_BASE_URL não configurada no SecureConfigManager.")
    }

    fun getTelemetryPublicKey(): String {
        return encryptedPrefs.getString(KEY_TELEMETRY_PUB_KEY, null)
            ?: throw InvalidParameterException("CRITICAL: Chave pública de telemetria ausente. Abortando inicialização.")
    }

    fun setConfig(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }
}
