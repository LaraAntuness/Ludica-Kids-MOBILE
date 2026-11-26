package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class ProveNaImaginacao2Activity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private val rosa = Color.parseColor("#FFB2D3")
    private val textoEscuro = Color.parseColor("#75405B")

    private var popupAberto = false
    private var navegando = false

    private var audioAtual: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_prove_na_imaginacao2)

        tela = findViewById(R.id.telaProveNaImaginacao2)

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        popupAberto = false
        navegando = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // =================================================
        // PLANO DE FUNDO
        // =================================================

        val fundo = ImageView(this)

        fundo.setImageResource(R.drawable.fundosabor)

        fundo.scaleType = ImageView.ScaleType.FIT_XY

        fundo.isClickable = false
        fundo.isFocusable = false

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO DE RETORNO ROSA
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornorosa",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.contentDescription = "Voltar aos cinco jogos"

        voltar.setOnClickListener {

            voltarParaCincoOpcoes()
        }

        // =================================================
        // TÍTULO MELHOR SABOR
        // =================================================

        adicionarImagem(
            "titulomelhorsabor",
            205, 2, 490, 85,
            sx, sy
        )

        // =================================================
        // BALÃO DE PERGUNTA ROSA
        // =================================================

        val balao = adicionarImagem(
            "balaopergunta",
            85, 80, 340, 135,
            sx, sy
        )

        balao.setColorFilter(
            rosa,
            PorterDuff.Mode.SRC_IN
        )

        // =================================================
        // TEXTO DA PERGUNTA
        // =================================================

        val pergunta = TextView(this).apply {

            text = "Qual desses alimentos\nvocê mais gosta?"

            gravity = Gravity.CENTER

            setTextColor(textoEscuro)

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                18f * sy
            )

            typeface = Typeface.DEFAULT_BOLD

            maxLines = 2
        }

        adicionarView(
            pergunta,
            125, 108, 235, 48,
            sx, sy
        )

        // =================================================
        // MENINA DE ROSA
        // =================================================

        adicionarImagem(
            "meninaderosa",
            55, 190, 150, 210,
            sx, sy
        )

        // =================================================
        // MAÇÃ - SUPERIOR ESQUERDO
        // =================================================

        criarOpcao(
            "maca",
            "Maçã",
            395, 105,
            sx, sy
        )

        // =================================================
        // PIZZA - SUPERIOR DIREITO
        // =================================================

        criarOpcao(
            "pizza",
            "Pizza",
            600, 105,
            sx, sy
        )

        // =================================================
        // SORVETE - INFERIOR ESQUERDO
        // =================================================

        criarOpcao(
            "sorvete",
            "Sorvete",
            395, 245,
            sx, sy
        )

        // =================================================
        // BANANA - INFERIOR DIREITO
        // =================================================

        criarOpcao(
            "banana",
            "Banana",
            600, 245,
            sx, sy
        )
    }

    // =====================================================
    // CRIAR CARTÕES DOS ALIMENTOS
    // =====================================================

    private fun criarOpcao(
        nomeImagem: String,
        nomeAlimento: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float
    ) {

        // =================================================
        // SOMBRA DO CARTÃO
        // =================================================

        val sombra = View(this)

        sombra.background = GradientDrawable().apply {

            setColor(
                Color.parseColor("#E8E7E7")
            )

            cornerRadius = 17f * sx
        }

        sombra.isClickable = false

        adicionarView(
            sombra,
            x + 13,
            y + 13,
            155,
            100,
            sx,
            sy
        )

        // =================================================
        // CARTÃO ROSA
        // =================================================

        val cartao = FrameLayout(this)

        cartao.background = GradientDrawable().apply {

            setColor(rosa)

            cornerRadius = 17f * sx
        }

        cartao.isClickable = true
        cartao.isFocusable = true

        cartao.contentDescription =
            "Selecionar $nomeAlimento"

        val parametros = FrameLayout.LayoutParams(
            (155 * sx).toInt().coerceAtLeast(1),
            (100 * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            cartao,
            parametros
        )

        // =================================================
        // IMAGEM DO ALIMENTO
        // =================================================

        val alimento = ImageView(this)

        val id = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (id != 0) {

            alimento.setImageResource(id)

        } else {

            Log.e(
                "ProveNaImaginacao2",
                "Imagem não encontrada: $nomeImagem"
            )

            Toast.makeText(
                this,
                "Imagem não encontrada: $nomeImagem",
                Toast.LENGTH_LONG
            ).show()
        }

        alimento.scaleType =
            ImageView.ScaleType.FIT_CENTER

        alimento.contentDescription = nomeAlimento

        alimento.isClickable = false
        alimento.isFocusable = false

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt().coerceAtLeast(1),
            (72 * sy).toInt().coerceAtLeast(1),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        parametrosImagem.topMargin =
            (3 * sy).toInt()

        cartao.addView(
            alimento,
            parametrosImagem
        )

        // =================================================
        // NOME DO ALIMENTO
        // =================================================

        val texto = TextView(this)

        texto.text = nomeAlimento

        texto.gravity = Gravity.CENTER

        texto.setTextColor(textoEscuro)

        texto.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            13f * sy
        )

        texto.typeface = Typeface.DEFAULT_BOLD

        texto.isClickable = false
        texto.isFocusable = false

        val parametrosTexto = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (25 * sy).toInt().coerceAtLeast(1),
            Gravity.BOTTOM
        )

        cartao.addView(
            texto,
            parametrosTexto
        )

        // =================================================
        // SELEÇÃO DO ALIMENTO COM SOM
        // =================================================

        cartao.setOnClickListener {

            if (!popupAberto && !navegando) {

                // SOM AO TOCAR NO ALIMENTO

                tocarAudio(R.raw.audio_toque)

                mostrarAlimentoPreferido(
                    nomeImagem,
                    nomeAlimento,
                    sx,
                    sy
                )
            }
        }
    }

    // =====================================================
    // POP-UP DO ALIMENTO PREFERIDO
    // =====================================================

    private fun mostrarAlimentoPreferido(
        nomeImagem: String,
        nomeAlimento: String,
        sx: Float,
        sy: Float
    ) {

        if (popupAberto || navegando) {

            return
        }

        popupAberto = true

        // =================================================
        // FUNDO ESCURECIDO
        // =================================================

        val sobreposicao = FrameLayout(this)

        sobreposicao.setBackgroundColor(
            Color.parseColor("#99000000")
        )

        sobreposicao.isClickable = true

        tela.addView(
            sobreposicao,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // PAINEL CENTRAL
        // =================================================

        val painel = FrameLayout(this)

        painel.background = GradientDrawable().apply {

            setColor(Color.WHITE)

            cornerRadius = 25f * sx

            setStroke(
                (4 * sx).toInt().coerceAtLeast(1),
                rosa
            )
        }

        painel.elevation = 12f * sx

        painel.isClickable = true

        val parametrosPainel = FrameLayout.LayoutParams(
            (430 * sx).toInt(),
            (300 * sy).toInt(),
            Gravity.CENTER
        )

        sobreposicao.addView(
            painel,
            parametrosPainel
        )

        // =================================================
        // TÍTULO DO POP-UP
        // =================================================

        val titulo = TextView(this)

        titulo.text = "Seu alimento preferido é:"

        titulo.gravity = Gravity.CENTER

        titulo.setTextColor(textoEscuro)

        titulo.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            23f * sy
        )

        titulo.typeface = Typeface.DEFAULT_BOLD

        val parametrosTitulo = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (55 * sy).toInt()
        )

        parametrosTitulo.topMargin =
            (10 * sy).toInt()

        painel.addView(
            titulo,
            parametrosTitulo
        )

        // =================================================
        // IMAGEM DO ALIMENTO ESCOLHIDO
        // =================================================

        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (id != 0) {

            imagem.setImageResource(id)
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        imagem.contentDescription = nomeAlimento

        val parametrosImagem = FrameLayout.LayoutParams(
            (145 * sx).toInt(),
            (105 * sy).toInt(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        parametrosImagem.topMargin =
            (65 * sy).toInt()

        painel.addView(
            imagem,
            parametrosImagem
        )

        // =================================================
        // NOME DO ALIMENTO ESCOLHIDO
        // =================================================

        val nome = TextView(this)

        nome.text = nomeAlimento

        nome.gravity = Gravity.CENTER

        nome.setTextColor(textoEscuro)

        nome.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            24f * sy
        )

        nome.typeface = Typeface.DEFAULT_BOLD

        val parametrosNome = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (40 * sy).toInt()
        )

        parametrosNome.topMargin =
            (170 * sy).toInt()

        painel.addView(
            nome,
            parametrosNome
        )

        // =================================================
        // BOTÃO VOLTAR - ROSA
        // =================================================

        criarBotaoPopup(
            painel,
            "← Voltar",
            25,
            230,
            175,
            48,
            "#FFB2D3",
            sx,
            sy
        ) {

            voltarParaCincoOpcoes()
        }

        // =================================================
        // BOTÃO PRÓXIMO NÍVEL - VERDE
        // =================================================

        criarBotaoPopup(
            painel,
            "Próximo nível →",
            220,
            230,
            185,
            48,
            "#8ACF8B",
            sx,
            sy
        ) {

            abrirProximoNivel()
        }

        // =================================================
        // TOCAR FORA FECHA O POP-UP COM SOM
        // =================================================

        sobreposicao.setOnClickListener {

            if (!navegando) {

                tocarAudio(R.raw.clique_pop)

                popupAberto = false

                tela.removeView(sobreposicao)
            }
        }
    }

    // =====================================================
    // CRIAR BOTÕES DO POP-UP
    // =====================================================

    private fun criarBotaoPopup(
        painel: FrameLayout,
        texto: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        cor: String,
        sx: Float,
        sy: Float,
        acao: () -> Unit
    ) {

        val botao = TextView(this)

        botao.text = texto

        botao.gravity = Gravity.CENTER

        botao.setTextColor(textoEscuro)

        botao.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            18f * sy
        )

        botao.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        botao.background = GradientDrawable().apply {

            setColor(
                Color.parseColor(cor)
            )

            cornerRadius = 17f * sx
        }

        botao.elevation = 3f * sx

        botao.isClickable = true
        botao.isFocusable = true

        botao.setOnClickListener {

            if (!navegando) {

                acao()
            }
        }

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        painel.addView(
            botao,
            parametros
        )
    }

    // =====================================================
    // VOLTAR PARA A TELA DOS CINCO JOGOS
    // =====================================================

    private fun voltarParaCincoOpcoes() {

        if (navegando || isFinishing) return

        navegando = true

        pararAudio()

        tocarSomNavegacao()

        val intent = Intent(
            this,
            CincoOpcoesDosJogosActivity::class.java
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

        voltarParaCincoOpcoes()
    }

    // =====================================================
    // ABRIR NÍVEL 3
    // =====================================================

    private fun abrirProximoNivel() {

        if (navegando || isFinishing) return

        navegando = true

        pararAudio()

        tocarSomNavegacao()

        try {

            val classeProximoNivel = Class.forName(
                "$packageName.ProveNaImaginacao3Activity"
            )

            val intent = Intent(
                this,
                classeProximoNivel
            )

            startActivity(intent)

            finish()

        } catch (erro: Exception) {

            navegando = false

            Log.e(
                "ProveNaImaginacao2",
                "Erro ao abrir nível 3",
                erro
            )

            Toast.makeText(
                this,
                "O próximo nível ainda não está disponível!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // REPRODUZIR ÁUDIO AO TOCAR NOS ALIMENTOS
    // =====================================================

    private fun tocarAudio(recursoAudio: Int) {

        pararAudio()

        try {

            val player = MediaPlayer.create(
                this,
                recursoAudio
            ) ?: return

            audioAtual = player

            player.setOnCompletionListener {

                if (audioAtual === player) {

                    audioAtual = null

                    player.release()
                }
            }

            player.setOnErrorListener { _, _, _ ->

                if (audioAtual === player) {

                    pararAudio()
                }

                true
            }

            player.start()

        } catch (erro: Exception) {

            pararAudio()

            Log.e(
                "ProveNaImaginacao2",
                "Erro ao reproduzir áudio",
                erro
            )
        }
    }

    // =====================================================
    // SOM DOS BOTÕES DE NAVEGAÇÃO
    // =====================================================

    private fun tocarSomNavegacao() {

        var player: MediaPlayer? = null

        try {

            player = MediaPlayer.create(
                applicationContext,
                R.raw.clique_pop
            )

            player?.setOnCompletionListener {

                it.release()
            }

            player?.setOnErrorListener { mp, _, _ ->

                mp.release()

                true
            }

            player?.start()

        } catch (erro: Exception) {

            player?.release()

            Log.e(
                "ProveNaImaginacao2",
                "Erro no som de navegação",
                erro
            )
        }
    }

    // =====================================================
    // PARAR ÁUDIO
    // =====================================================

    private fun pararAudio() {

        val player = audioAtual

        audioAtual = null

        player?.let {

            try {

                it.setOnCompletionListener(null)

                it.setOnErrorListener(null)

                if (it.isPlaying) {

                    it.stop()
                }

            } catch (_: Exception) {
            }

            it.release()
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

            Log.e(
                "ProveNaImaginacao2",
                "Imagem não encontrada: $nome"
            )

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_LONG
            ).show()
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x,
            y,
            largura,
            altura,
            sx,
            sy
        )

        return imagem
    }

    // =====================================================
    // ADICIONAR ELEMENTOS NA TELA
    // =====================================================

    private fun adicionarView(
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            view,
            parametros
        )
    }

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    override fun onPause() {

        pararAudio()

        super.onPause()
    }

    override fun onDestroy() {

        pararAudio()

        super.onDestroy()
    }
}