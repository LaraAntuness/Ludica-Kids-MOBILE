
package com.example.mobilejogos

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class CalculadoraPaisDialog(
    private val activity: Activity,
    private val aoFechar: () -> Unit = {},
    private val aoConfirmar: () -> Unit
) {

    companion object {
        private const val URL_API =
            "https://ludicakids-backend.up.railway.app"

        private const val LIMITE_CPF = 11
    }

    private val azul = Color.rgb(59, 141, 238)
    private val azulEscuro = Color.rgb(46, 85, 147)
    private val azulClaro = Color.rgb(232, 244, 255)
    private val lilas = Color.rgb(241, 228, 255)
    private val roxo = Color.rgb(137, 91, 193)
    private val amarelo = Color.rgb(255, 214, 104)
    private val vermelho = Color.rgb(213, 88, 102)

    private lateinit var dialog: Dialog
    private lateinit var visor: TextView
    private lateinit var mensagem: TextView
    private lateinit var botaoConfirmar: TextView
    private lateinit var camada: FrameLayout

    private val cpf = StringBuilder()

    private var verificando = false
    private var confirmado = false
    private var som: MediaPlayer? = null

    private fun dp(valor: Int): Int {
        return (
                valor * activity.resources.displayMetrics.density
                ).toInt()
    }

    private fun formato(
        cor: Int,
        raio: Int,
        borda: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raio).toFloat()

            if (borda != null) {
                setStroke(dp(1), borda)
            }
        }
    }

    private fun texto(
        valor: String,
        tamanho: Float,
        cor: Int,
        negrito: Boolean = false
    ): TextView {

        return TextView(activity).apply {
            text = valor
            textSize = tamanho
            gravity = Gravity.CENTER
            setTextColor(cor)

            typeface = Typeface.create(
                "sans-serif-rounded",
                if (negrito) Typeface.BOLD
                else Typeface.NORMAL
            )
        }
    }

    private fun tocarClique() {
        try {
            val player = som

            if (player == null) {
                som = MediaPlayer.create(
                    activity,
                    R.raw.clique_suave
                )
            }

            som?.let {
                it.seekTo(0)
                it.start()
            }
        } catch (_: Exception) {
            // Um erro de áudio não impede o uso.
        }
    }

    private fun formatarCpf(valor: String): String {
        val digitos = valor.take(11)

        val resultado = StringBuilder()

        digitos.forEachIndexed { i, c ->
            if (i == 3 || i == 6) {
                resultado.append(".")
            }

            if (i == 9) {
                resultado.append("-")
            }

            resultado.append(c)
        }

        return resultado.toString()
    }

    private fun atualizarVisor() {
        visor.text = if (cpf.isEmpty()) {
            "000.000.000-00"
        } else {
            formatarCpf(cpf.toString())
        }

        visor.alpha = if (cpf.isEmpty()) 0.55f else 1f

        mensagem.text = "${cpf.length}/11 dígitos"
        mensagem.setTextColor(azulEscuro)
    }

    private fun adicionarNumero(numero: String) {
        if (verificando || confirmado) return

        tocarClique()

        if (cpf.length < LIMITE_CPF) {
            cpf.append(numero)
            atualizarVisor()
        }
    }

    private fun apagar() {
        if (verificando || confirmado) return

        tocarClique()

        if (cpf.isNotEmpty()) {
            cpf.deleteCharAt(cpf.lastIndex)
        }

        atualizarVisor()
    }

    private fun limpar() {
        if (verificando || confirmado) return

        tocarClique()
        cpf.clear()
        atualizarVisor()
    }

    private fun criarTecla(
        rotulo: String,
        cor: Int,
        corTexto: Int,
        acao: () -> Unit
    ): TextView {

        return texto(
            rotulo,
            22f,
            corTexto,
            true
        ).apply {

            background = formato(cor, 24)
            elevation = dp(3).toFloat()

            isClickable = true
            isFocusable = true

            setOnClickListener {
                if (verificando || confirmado) {
                    return@setOnClickListener
                }

                animate()
                    .scaleX(0.93f)
                    .scaleY(0.93f)
                    .setDuration(75)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()

                        acao()
                    }
                    .start()
            }
        }
    }

    fun mostrar() {

        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        dialog = Dialog(activity)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val larguraTela =
            activity.resources.displayMetrics.widthPixels

        val alturaTela =
            activity.resources.displayMetrics.heightPixels

        // Layout compacto para o modo horizontal.
        val largura = minOf(
            dp(440),
            (larguraTela * 0.80f).toInt()
        )

        val altura = minOf(
            dp(360),
            (alturaTela * 0.91f).toInt()
        )

        camada = FrameLayout(activity).apply {
            background = formato(
                Color.WHITE,
                25
            )
            clipChildren = false
            clipToPadding = false
        }

        val coluna = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(12)
            )
        }

        camada.addView(
            coluna,
            FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        // =====================================
        // TÍTULO E FECHAR
        // =====================================

        val cabecalho = FrameLayout(activity)

        val titulo = texto(
            "☀️ Cantinho dos responsáveis",
            17f,
            azulEscuro,
            true
        )

        cabecalho.addView(
            titulo,
            FrameLayout.LayoutParams(
                -1,
                dp(32)
            )
        )

        val fechar = texto(
            "×",
            23f,
            azulEscuro,
            true
        ).apply {
            background = formato(azulClaro, 18)
            isClickable = true

            setOnClickListener {
                if (!verificando && !confirmado) {
                    tocarClique()
                    dialog.dismiss()
                }
            }
        }

        cabecalho.addView(
            fechar,
            FrameLayout.LayoutParams(
                dp(30),
                dp(30),
                Gravity.END or Gravity.TOP
            )
        )

        coluna.addView(
            cabecalho,
            LinearLayout.LayoutParams(
                -1,
                dp(35)
            )
        )

        coluna.addView(
            texto(
                "Digite o CPF do responsável para continuar",
                11f,
                azulEscuro
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(20)
            )
        )

        // =====================================
        // VISOR
        // =====================================

        visor = texto(
            "000.000.000-00",
            23f,
            azulEscuro,
            true
        ).apply {
            background = formato(
                azulClaro,
                18,
                Color.rgb(195, 220, 249)
            )
        }

        coluna.addView(
            visor,
            LinearLayout.LayoutParams(
                -1,
                dp(43)
            ).apply {
                topMargin = dp(3)
            }
        )

        mensagem = texto(
            "0/11 dígitos",
            11f,
            azulEscuro
        )

        coluna.addView(
            mensagem,
            LinearLayout.LayoutParams(
                -1,
                dp(22)
            )
        )

        // =====================================
        // TECLADO DA CALCULADORA
        // =====================================

        val grade = GridLayout(activity).apply {
            columnCount = 3
            rowCount = 4
        }

        val teclas = listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "C", "0", "⌫"
        )

        teclas.forEachIndexed { indice, item ->

            val corFundo = when (item) {
                "C" -> lilas
                "⌫" -> Color.rgb(255, 235, 223)
                else -> azulClaro
            }

            val corTexto = when (item) {
                "C" -> roxo
                "⌫" -> vermelho
                else -> azulEscuro
            }

            val tecla = criarTecla(
                item,
                corFundo,
                corTexto
            ) {
                when (item) {
                    "C" -> limpar()
                    "⌫" -> apagar()
                    else -> adicionarNumero(item)
                }
            }

            val larguraTecla =
                ((largura - dp(40) - dp(18)) / 3)

            val parametros = GridLayout.LayoutParams(
                GridLayout.spec(indice / 3),
                GridLayout.spec(indice % 3)
            ).apply {

                width = larguraTecla
                height = dp(40)

                setMargins(
                    dp(3),
                    dp(3),
                    dp(3),
                    dp(3)
                )
            }

            grade.addView(tecla, parametros)
        }

        coluna.addView(
            grade,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        // =====================================
        // CONFIRMAR
        // =====================================

        botaoConfirmar = texto(
            "✨ Confirmar CPF",
            15f,
            azulEscuro,
            true
        ).apply {

            background = formato(
                amarelo,
                22
            )

            elevation = dp(4).toFloat()
            isClickable = true

            setOnClickListener {
                confirmarCpf()
            }
        }

        coluna.addView(
            botaoConfirmar,
            LinearLayout.LayoutParams(
                -1,
                dp(43)
            ).apply {
                topMargin = dp(4)
            }
        )

        dialog.setContentView(camada)

        dialog.setOnDismissListener {
            som?.release()
            som = null
            aoFechar()
        }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            setDimAmount(0.55f)

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            setGravity(Gravity.CENTER)
            setLayout(largura, altura)

            setSoftInputMode(
                WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
        }

        atualizarVisor()

        camada.alpha = 0f
        camada.scaleX = 0.9f
        camada.scaleY = 0.9f

        camada.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(320)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()
    }

    // ==========================================
    // CONFIRMAÇÃO
    // ==========================================

    private fun confirmarCpf() {

        if (verificando || confirmado) return

        tocarClique()

        if (cpf.length != LIMITE_CPF) {
            exibirErro(
                "Digite os 11 números do CPF."
            )
            return
        }

        val sessao = LudicaSessao.obter(activity)

        if (sessao == null) {
            exibirErro(
                "Faça login para acessar esta área."
            )
            return
        }

        verificando = true
        dialog.setCancelable(false)

        botaoConfirmar.text = "Verificando..."
        botaoConfirmar.alpha = 0.7f

        // A verificação ocorre no servidor.
        // O CPF não fica salvo no aplicativo.

        val cpfDigitado = cpf.toString()
        val token = sessao.first

        Thread {

            var codigo = -1

            try {
                val conexao = (
                        URL(
                            "$URL_API/responsaveis/confirmar-cpf"
                        ).openConnection()
                                as HttpURLConnection
                        )

                try {
                    conexao.requestMethod = "POST"
                    conexao.connectTimeout = 12000
                    conexao.readTimeout = 12000
                    conexao.doOutput = true

                    conexao.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                    )

                    conexao.setRequestProperty(
                        "Authorization",
                        "Bearer $token"
                    )

                    val json = JSONObject().apply {
                        put("cpf", cpfDigitado)
                    }

                    conexao.outputStream.use {
                        it.write(
                            json.toString()
                                .toByteArray(Charsets.UTF_8)
                        )
                    }

                    codigo = conexao.responseCode

                } finally {
                    conexao.disconnect()
                }

            } catch (_: Exception) {
                codigo = -1
            }

            val resposta = codigo

            activity.runOnUiThread {

                if (
                    activity.isFinishing ||
                    activity.isDestroyed ||
                    !dialog.isShowing
                ) {
                    return@runOnUiThread
                }

                verificando = false
                botaoConfirmar.alpha = 1f
                botaoConfirmar.text = "✨ Confirmar CPF"
                dialog.setCancelable(true)

                when (resposta) {

                    200 -> {
                        mostrarSucesso()
                    }

                    401 -> {
                        LudicaSessao.limpar(activity)
                        exibirErro(
                            "Sua sessão expirou. Faça login novamente."
                        )
                    }

                    403 -> {
                        exibirErro(
                            "CPF não confirmado. Verifique e tente novamente."
                        )
                    }

                    429 -> {
                        exibirErro(
                            "Muitas tentativas. Tente mais tarde."
                        )
                    }

                    -1 -> {
                        exibirErro(
                            "Não foi possível conectar ao servidor."
                        )
                    }

                    else -> {
                        exibirErro(
                            "Não foi possível confirmar o CPF."
                        )
                    }
                }
            }

        }.start()
    }

    private fun exibirErro(textoErro: String) {

        mensagem.text = textoErro
        mensagem.setTextColor(vermelho)

        visor.animate()
            .translationX(-dp(6).toFloat())
            .setDuration(70)
            .withEndAction {
                visor.animate()
                    .translationX(dp(6).toFloat())
                    .setDuration(70)
                    .withEndAction {
                        visor.animate()
                            .translationX(0f)
                            .setDuration(70)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    // ==========================================
    // GLITTER E LIBERAÇÃO
    // ==========================================

    private fun mostrarSucesso() {

        confirmado = true

        dialog.setCancelable(false)

        mensagem.text = "CPF confirmado! 💜"
        mensagem.setTextColor(roxo)

        botaoConfirmar.text = "Acesso liberado! ✨"
        botaoConfirmar.background = formato(
            lilas,
            22
        )

        val cores = listOf(
            Color.rgb(170, 130, 236),
            Color.rgb(255, 195, 117),
            Color.rgb(251, 166, 217),
            Color.rgb(111, 193, 247)
        )

        camada.post {

            repeat(45) {

                val brilho = texto(
                    if (it % 2 == 0) "✦" else "✧",
                    Random.nextInt(13, 25).toFloat(),
                    cores.random(),
                    true
                )

                camada.addView(
                    brilho,
                    FrameLayout.LayoutParams(
                        dp(28),
                        dp(28),
                        Gravity.CENTER
                    )
                )

                brilho.translationX =
                    Random.nextInt(-45, 45).toFloat()

                brilho.translationY =
                    Random.nextInt(-35, 35).toFloat()

                brilho.animate()
                    .translationX(
                        Random.nextInt(-200, 200)
                            .toFloat()
                    )
                    .translationY(
                        Random.nextInt(-130, 130)
                            .toFloat()
                    )
                    .rotation(
                        Random.nextInt(-180, 180)
                            .toFloat()
                    )
                    .alpha(0f)
                    .setDuration(
                        Random.nextLong(700, 1400)
                    )
                    .withEndAction {
                        camada.removeView(brilho)
                    }
                    .start()
            }

            camada.postDelayed({

                if (
                    !activity.isFinishing &&
                    !activity.isDestroyed &&
                    dialog.isShowing
                ) {
                    // O servidor já confirmou o CPF.
                    dialog.dismiss()
                    aoConfirmar()
                }

            }, 1400L)
        }
    }
}
