package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class CincoOpcoesDosJogosActivity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 402f

    private var navegando = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // TELA HORIZONTAL

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // TELA CHEIA

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // MANTER XML ORIGINAL

        setContentView(
            R.layout.activity_cinco_opcoes_dos_jogos
        )

        tela = findViewById(
            R.id.telaCincoOpcoes
        )

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // =================================================
        // PLANO DE FUNDO
        // =================================================

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planofundo5sentidos
            )

            scaleType = ImageView.ScaleType.FIT_XY
        }

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO RETORNO AZUL
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoazul",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.contentDescription = "Voltar"

        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.isSoundEffectsEnabled = false

        voltar.setOnClickListener {

            voltarTela()
        }

        // =================================================
        // TÍTULO CINCO SENTIDOS MÁGICOS
        // =================================================

        adicionarImagem(
            "cincosentidosmagicosazul",
            190, 7, 490, 65,
            sx, sy
        )

        // =================================================
        // BOTÃO 1 - OLHINHOS CURIOSOS
        // =================================================

        criarBotao(
            "OLHINHOS CURIOSOS",
            290, 72, 300, 50,
            sx, sy
        ) {

            abrirTela(
                MainActivity::class.java
            )
        }

        // =================================================
        // BOTÃO 2 - MÃOZINHAS MÁGICAS
        // =================================================

        criarBotao(
            "MÃOZINHAS MÁGICAS",
            290, 131, 300, 50,
            sx, sy
        ) {

            abrirTela(
                MaoszinhasMagicasActivity::class.java
            )
        }

        // =================================================
        // BOTÃO 3 - ESCUTE E DESCUBRA
        // =================================================

        criarBotao(
            "ESCUTE E DESCUBRA",
            290, 190, 300, 50,
            sx, sy
        ) {

            abrirTela(
                BibliotecaSonoraActivity::class.java
            )
        }

        // =================================================
        // BOTÃO 4 - O NARIZ DETETIVE
        // =================================================

        criarBotao(
            "O NARIZ DETETIVE",
            290, 249, 300, 50,
            sx, sy
        ) {

            abrirTelaPorNome(
                "DescubraOCheiroActivity"
            )
        }

        // =================================================
        // BOTÃO 5 - PROVE NA IMAGINAÇÃO
        // =================================================

        criarBotao(
            "PROVE NA IMAGINAÇÃO",
            290, 308, 300, 50,
            sx, sy
        ) {

            abrirTelaPorNome(
                "ProveNaImaginacaoActivity"
            )
        }
    }

    // =====================================================
    // SOM DE CLIQUE SUAVE
    // =====================================================

    private fun tocarSomClique() {

        var player: MediaPlayer? = null

        try {

            val id = resources.getIdentifier(
                "clique_suave",
                "raw",
                packageName
            )

            if (id == 0) {

                Log.e(
                    "CincoOpcoesDosJogos",
                    "Áudio clique_suave não encontrado"
                )

                return
            }

            val som = MediaPlayer.create(
                applicationContext,
                id
            ) ?: return

            player = som

            // VOLUME DO CLIQUE

            som.setVolume(
                0.45f,
                0.45f
            )

            // LIBERAR APÓS TERMINAR

            som.setOnCompletionListener {

                it.release()
            }

            som.setOnErrorListener { mp, _, _ ->

                mp.release()

                true
            }

            som.start()

        } catch (erro: Exception) {

            player?.release()

            Log.e(
                "CincoOpcoesDosJogos",
                "Erro ao reproduzir som de clique",
                erro
            )
        }
    }

    // =====================================================
    // ADICIONAR IMAGEM
    // =====================================================

    private fun adicionarImagem(
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ): ImageView {

        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (id != 0) {

            imagem.setImageResource(id)

        } else {

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_SHORT
            ).show()
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            imagem,
            parametros
        )

        return imagem
    }

    // =====================================================
    // CRIAR BOTÕES AMARELOS
    // =====================================================

    private fun criarBotao(
        texto: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float,
        acao: () -> Unit
    ) {

        // =================================================
        // SOMBRA AMARELA
        // =================================================

        val sombra = GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            cornerRadius = 22f * minOf(sx, sy)

            setColor(
                Color.rgb(255, 216, 125)
            )
        }

        val sombraView = android.view.View(this).apply {

            background = sombra

            isClickable = false
        }

        val parametrosSombra = FrameLayout.LayoutParams(
            (largura * sx).toInt(),
            ((altura - 3) * sy).toInt()
        )

        parametrosSombra.leftMargin =
            (x * sx).toInt()

        parametrosSombra.topMargin =
            ((y + 7) * sy).toInt()

        tela.addView(
            sombraView,
            parametrosSombra
        )

        // =================================================
        // BOTÃO PRINCIPAL
        // =================================================

        val botao = TextView(this)

        botao.text = texto

        botao.gravity = Gravity.CENTER

        botao.includeFontPadding = false

        // TEXTO LARANJA

        botao.setTextColor(
            Color.rgb(255, 165, 76)
        )

        botao.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            19f * minOf(sx, sy)
        )

        botao.typeface = Typeface.create(
            "sans-serif-condensed",
            Typeface.BOLD
        )

        botao.maxLines = 1

        botao.setPadding(
            (10 * sx).toInt(),
            0,
            (10 * sx).toInt(),
            0
        )

        // =================================================
        // FUNDO NORMAL
        // =================================================

        val fundoNormal = GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            cornerRadius = 22f * minOf(sx, sy)

            setColor(
                Color.rgb(255, 255, 181)
            )
        }

        // =================================================
        // FUNDO PRESSIONADO
        // =================================================

        val fundoPressionado = GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            cornerRadius = 22f * minOf(sx, sy)

            setColor(
                Color.rgb(255, 239, 149)
            )
        }

        // =================================================
        // ESTADOS DO BOTÃO
        // =================================================

        val estados = StateListDrawable().apply {

            addState(
                intArrayOf(
                    android.R.attr.state_pressed
                ),
                fundoPressionado
            )

            addState(
                intArrayOf(),
                fundoNormal
            )
        }

        botao.background = estados

        // =================================================
        // INTERAÇÃO
        // =================================================

        botao.isClickable = true

        botao.isFocusable = true

        botao.isSoundEffectsEnabled = false

        // =================================================
        // POSICIONAMENTO
        // =================================================

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt(),
            ((altura - 7) * sy).toInt()
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            botao,
            parametros
        )

        // =================================================
        // ANIMAÇÃO AO CLICAR
        // =================================================

        botao.setOnTouchListener { view, evento ->

            when (evento.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    view.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(100L)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100L)
                        .start()
                }
            }

            false
        }

        // =================================================
        // SOM E NAVEGAÇÃO
        // =================================================

        botao.setOnClickListener {

            if (navegando || isFinishing) {

                return@setOnClickListener
            }

            tocarSomClique()

            acao()
        }
    }

    // =====================================================
    // ABRIR ACTIVITY
    // =====================================================

    private fun abrirTela(
        classe: Class<out Activity>
    ) {

        if (navegando || isFinishing) return

        navegando = true

        try {

            startActivity(
                Intent(this, classe)
            )

        } catch (e: Exception) {

            navegando = false

            Toast.makeText(
                this,
                "Não foi possível abrir essa tela.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =====================================================
    // ABRIR ACTIVITY PELO NOME
    // =====================================================

    private fun abrirTelaPorNome(
        nomeClasse: String
    ) {

        try {

            val classe = Class.forName(
                "$packageName.$nomeClasse"
            ).asSubclass(Activity::class.java)

            abrirTela(classe)

        } catch (e: Exception) {

            navegando = false

            Toast.makeText(
                this,
                "A tela $nomeClasse ainda não está disponível.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =====================================================
    // VOLTAR PARA A TELA DE OPÇÕES GERAL
    // =====================================================

    private fun voltarTela() {

        if (navegando || isFinishing) return

        navegando = true

        // SOM DE CLIQUE

        tocarSomClique()

        // ABRIR TELA INICIAL GERAL DOS JOGOS

        val intent = Intent(
            this,
            TelaInicialJogosGeral::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)

        finish()
    }

    // =====================================================
    // BOTÃO VOLTAR DO CELULAR
    // =====================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        voltarTela()
    }

    // =====================================================
    // LIBERAR NAVEGAÇÃO AO RETORNAR
    // =====================================================

    override fun onResume() {

        super.onResume()

        navegando = false
    }
}