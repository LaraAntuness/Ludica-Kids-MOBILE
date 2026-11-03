
package com.example.mobilejogos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class IdentificandoEmocoesActivity : AppCompatActivity() {

    private lateinit var camadaNuvens: FrameLayout
    private lateinit var mensagem: TextView
    private lateinit var girafa: ImageView

    private val handler = Handler(Looper.getMainLooper())

    private var jogoAtivo = true
    private var acertou = false

    private val perguntaInicial =
        "O que a girafa está sentindo?"

    private val verde = Color.rgb(55, 125, 75)
    private val vermelho = Color.rgb(195, 65, 65)

    private val nuvens = listOf(
        R.drawable.nuvemfeliz to "feliz",
        R.drawable.nuvemtriste to "triste",
        R.drawable.nuvembrava to "brava",
        R.drawable.nuvemconfusa to "duvida",
        R.drawable.nuvemassustada to "medo"
    )

    // voltar a pergunta novamente
    private val restaurarPergunta = Runnable {
        if (jogoAtivo && !acertou) {
            mostrarPerguntaInicial()
        }
    }

    private val criarNuvens = object : Runnable {
        override fun run() {
            if (!jogoAtivo || acertou) return

            criarNuvem()
            handler.postDelayed(this, 2500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emocoes)

        camadaNuvens = findViewById(R.id.camadaNuvens)
        mensagem = findViewById(R.id.mensagem)
        girafa = findViewById(R.id.girafa)

        mostrarPerguntaInicial()

        findViewById<ImageView>(R.id.botaoRetorno)
            .setOnClickListener {
                finish()
            }

        camadaNuvens.post {
            if (jogoAtivo) {
                handler.post(criarNuvens)
            }
        }
    }

    // Exibe a pergunta original no topo.
    private fun mostrarPerguntaInicial() {
        mensagem.text = perguntaInicial
        mensagem.setTextColor(verde)
        mensagem.background = criarFundoMensagem(
            Color.rgb(255, 253, 245)
        )
    }

    // Cria uma mensagem com bordas finas e arredondadas.
    private fun criarFundoMensagem(cor: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(cor)
            cornerRadius =
                12 * resources.displayMetrics.density
            setStroke(
                (1 * resources.displayMetrics.density).toInt(),
                Color.rgb(220, 228, 210)
            )
        }
    }

    private fun criarNuvem() {
        if (!jogoAtivo || acertou ||
            camadaNuvens.width == 0
        ) return

        val emocao = nuvens.random()
        val densidade = resources.displayMetrics.density

        // Calcula a área lateral disponível ao redor da girafa.
        val larguraTela = camadaNuvens.width
        val larguraGirafa = girafa.width.takeIf {
            it > 0
        } ?: (170 * densidade).toInt()

        val margemSeguranca = (8 * densidade).toInt()

        val espacoLateral =
            (larguraTela - larguraGirafa) / 2 -
                    margemSeguranca

        // Ajusta o tamanho para não invadir a área da girafa.
        val tamanho = minOf(
            (105 * densidade).toInt(),
            espacoLateral
        )

        // Em telas muito estreitas, não cria nuvens sobre a girafa.
        if (tamanho < (45 * densidade).toInt()) return

        val nuvem = ImageView(this).apply {
            setImageResource(emocao.first)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Nuvem ${emocao.second}"
            isClickable = true
            isFocusable = true
        }

        val parametros = FrameLayout.LayoutParams(
            tamanho,
            tamanho
        )

        parametros.gravity = Gravity.TOP or Gravity.START

        // Alterna entre o lado esquerdo e o direito.
        val ladoEsquerdo = Random.nextBoolean()

        parametros.leftMargin = if (ladoEsquerdo) {
            Random.nextInt(
                0,
                (espacoLateral - tamanho + margemSeguranca + 1)
                    .coerceAtLeast(1)
            )
        } else {
            Random.nextInt(
                (larguraTela - espacoLateral + margemSeguranca - tamanho)
                    .coerceAtLeast(0),
                (larguraTela - tamanho + 1)
            )
        }

        camadaNuvens.addView(nuvem, parametros)

        // A nuvem começa acima da tela.
        nuvem.translationY = -tamanho.toFloat()

        nuvem.setOnClickListener {
            if (!jogoAtivo || acertou) {
                return@setOnClickListener
            }

            if (emocao.second == "triste") {
                respostaCorreta()
            } else {
                respostaIncorreta(nuvem)
            }
        }

        // Descenso mais lento.
        val alturaDestino =
            camadaNuvens.height.toFloat() + tamanho

        nuvem.animate()
            .translationY(alturaDestino)
            .setDuration(Random.nextLong(10000, 14001))
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    camadaNuvens.removeView(nuvem)
                }
            })
            .start()
    }

    // Resposta correta: mensagem verde e jogo pausado.
    private fun respostaCorreta() {
        acertou = true

        handler.removeCallbacks(criarNuvens)
        handler.removeCallbacks(restaurarPergunta)

        mensagem.text = "Sim, a girafa está triste!"
        mensagem.setTextColor(verde)
        mensagem.background = criarFundoMensagem(
            Color.rgb(225, 248, 225)
        )

        // Para e remove todas as nuvens.
        for (i in camadaNuvens.childCount - 1 downTo 0) {
            camadaNuvens.getChildAt(i).animate().cancel()
        }

        camadaNuvens.removeAllViews()
    }

    // Resposta errada: X vermelho e aviso temporário.
    private fun respostaIncorreta(nuvem: ImageView) {
        if (nuvem.parent == null) return

        handler.removeCallbacks(restaurarPergunta)

        mensagem.text = "Ops! Tente outra emoção!"
        mensagem.setTextColor(vermelho)
        mensagem.background = criarFundoMensagem(
            Color.rgb(255, 235, 235)
        )

        // Guarda a posição atual da nuvem.
        val posicaoY = nuvem.translationY

        val x = TextView(this).apply {
            text = "✕"
            textSize = 38f
            setTextColor(vermelho)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isClickable = false
        }

        val parametros = FrameLayout.LayoutParams(
            nuvem.width,
            nuvem.height
        )

        parametros.leftMargin = nuvem.left
        parametros.topMargin = nuvem.top

        x.translationY = posicaoY

        nuvem.animate().cancel()
        camadaNuvens.removeView(nuvem)
        camadaNuvens.addView(x, parametros)

        // Depois de 1 segundo, o X desaparece.
        handler.postDelayed({
            camadaNuvens.removeView(x)
        }, 1000)

        // Após 2,5 segundos, a pergunta inicial volta.
        handler.postDelayed(restaurarPergunta, 2500)
    }

    override fun onDestroy() {
        jogoAtivo = false

        handler.removeCallbacksAndMessages(null)

        if (::camadaNuvens.isInitialized) {
            for (i in camadaNuvens.childCount - 1 downTo 0) {
                camadaNuvens.getChildAt(i).animate().cancel()
            }
        }

        super.onDestroy()
    }
}
