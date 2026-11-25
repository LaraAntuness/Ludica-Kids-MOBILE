
package com.example.mobilejogos

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object LudicaSessao {

    private const val PREF = "ludica_sessao_segura"
    private const val ALIAS = "ludicakids_token_aes"
    private const val CAMPO = "token_cifrado"
    private const val ID = "id_responsavel"

    private fun chave(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val existente = store.getKey(ALIAS, null) as? SecretKey
        if (existente != null) return existente

        val gerador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        gerador.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return gerador.generateKey()
    }

    fun salvar(
        contexto: Context,
        token: String,
        idResponsavel: String
    ) {
        require(token.isNotBlank() && idResponsavel.isNotBlank())

        val cifra = Cipher.getInstance("AES/GCM/NoPadding")
        cifra.init(Cipher.ENCRYPT_MODE, chave())

        val codificado = cifra.doFinal(
            token.toByteArray(Charsets.UTF_8)
        )
        val dados = cifra.iv + codificado

        contexto.getSharedPreferences(
            PREF,
            Context.MODE_PRIVATE
        ).edit()
            .putString(
                CAMPO,
                Base64.encodeToString(dados, Base64.NO_WRAP)
            )
            .putString(ID, idResponsavel)
            .apply()
    }

    fun obter(contexto: Context): Pair<String, String>? {
        val prefs = contexto.getSharedPreferences(
            PREF,
            Context.MODE_PRIVATE
        )
        val id = prefs.getString(ID, null) ?: return null
        val salvo = prefs.getString(CAMPO, null) ?: return null

        return try {
            val dados = Base64.decode(salvo, Base64.DEFAULT)
            if (dados.size <= 12) return null

            val cifra = Cipher.getInstance("AES/GCM/NoPadding")
            cifra.init(
                Cipher.DECRYPT_MODE,
                chave(),
                GCMParameterSpec(
                    128,
                    dados.copyOfRange(0, 12)
                )
            )

            val token = String(
                cifra.doFinal(
                    dados.copyOfRange(12, dados.size)
                ),
                Charsets.UTF_8
            )

            if (token.isBlank()) null else Pair(token, id)
        } catch (_: Exception) {
            limpar(contexto)
            null
        }
    }

    fun limpar(contexto: Context) {
        contexto.getSharedPreferences(
            PREF,
            Context.MODE_PRIVATE
        ).edit().clear().apply()
    }
}
