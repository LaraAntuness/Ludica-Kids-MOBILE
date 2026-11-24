package com.example.mobilejogos

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

object LudicaAvisos {

    private fun dp(activity: Activity, valor: Int): Int {
        return (valor * activity.resources.displayMetrics.density).toInt()
    }

    // AVISO DE SUCESSO
    fun mostrarSucesso(activity: Activity, mensagem: String) {
        mostrarMensagem(activity, mensagem, true)
    }

    // AVISO DE ERRO
    fun mostrarErro(activity: Activity, mensagem: String) {
        mostrarMensagem(activity, mensagem, false)
    }

    // MENSAGEM PERSONALIZADA
    private fun mostrarMensagem(
        activity: Activity,
        mensagem: String,
        sucesso: Boolean
    ) {
        val cor = if (sucesso) {
            Color.rgb(98, 165, 109)
        } else {
            Color.rgb(211, 100, 100)
        }

        val texto = TextView(activity).apply {
            text = mensagem
            textSize = 16f
            setTextColor(Color.WHITE)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER

            setPadding(
                dp(activity, 22),
                dp(activity, 14),
                dp(activity, 22),
                dp(activity, 14)
            )

            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 20).toFloat()
                setColor(cor)
            }
        }

        Toast(activity).apply {
            duration = Toast.LENGTH_LONG
            view = texto
            show()
        }
    }

    // CONFIRMAÇÃO DE AÇÃO
    fun mostrarConfirmacao(
        activity: Activity,
        mensagem: String,
        aoConfirmar: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Confirmar ação")
            .setMessage(mensagem)
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Confirmar") { dialog, _ ->
                dialog.dismiss()
                aoConfirmar()
            }
            .show()
    }
}