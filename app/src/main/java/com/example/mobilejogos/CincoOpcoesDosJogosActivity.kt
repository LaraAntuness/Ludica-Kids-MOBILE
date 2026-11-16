package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
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
    private val alturaBase = 393f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tela na horizontal
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Tela cheia
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_cinco_opcoes_dos_jogos)

        tela = findViewById(R.id.telaCincoOpcoes)

        tela.setBackgroundColor(
            Color.parseColor("#CBE0ED")
        )

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {

        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        tela.setBackgroundColor(
            Color.parseColor("#CBE0ED")
        )

        // =====================================
        // BOTÃO VOLTAR
        // =====================================

        val voltar = adicionarImagem(
            "botaoretornoamarelo",
            19, 25, 40, 40, sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true

        voltar.setOnClickListener {
            finish()
        }

        // =====================================
        // TÍTULO
        // =====================================

        adicionarImagem(
            "titulocincosentidos",
            205, 2, 490, 85, sx, sy
        )

        // =====================================
        // BORBOLETA SUPERIOR ESQUERDA
        // =====================================

        adicionarImagem(
            "borboleta",
            180, 76, 100, 90, sx, sy
        )

        // =====================================
        // GIRAFA
        // =====================================

        adicionarImagem(
            "girafa",
            53, 193, 180, 200, sx, sy
        )

        // =====================================
        // CRIANÇAS
        // =====================================

        adicionarImagem(
            "crianca",
            710, 90, 220, 350, sx, sy
        )

        // =====================================
        // BORBOLETA SUPERIOR DIREITA
        // =====================================

        adicionarImagem(
            "borboleta2",
            664, 25, 40, 30, sx, sy
        )

        // =====================================
        // BOTÃO 1 - OLHINHOS CURIOSOS
        // =====================================

        criarBotao(
            "OLHINHOS CURIOSOS",
            290, 94, 310, 48, sx, sy
        ) {
            abrirTela(MainActivity::class.java)
        }

        // =====================================
        // BOTÃO 2 - MÃOZINHAS MÁGICAS
        // =====================================

        criarBotao(
            "MÃOZINHAS MÁGICAS",
            290, 149, 310, 48, sx, sy
        ) {
            abrirTela(MaoszinhasMagicasActivity::class.java)
        }

        // =====================================
        // BOTÃO 3 - ESCUTE E DESCUBRA
        // =====================================

        criarBotao(
            "ESCUTE E DESCUBRA",
            290, 204, 310, 48, sx, sy
        ) {
            abrirTela(BibliotecaSonoraActivity::class.java)
        }

        // =====================================
        // BOTÃO 4 - O NARIZ DETETIVE
        // =====================================

        criarBotao(
            "O NARIZ DETETIVE",
            290, 259, 310, 48, sx, sy
        ) {
            abrirTelaPorNome("DescubraOCheiroActivity")
        }

        // =====================================
        // BOTÃO 5 - PROVE NA IMAGINAÇÃO
        // =====================================

        criarBotao(
            "PROVE NA IMAGINAÇÃO",
            290, 314, 310, 48, sx, sy
        ) {
            abrirTelaPorNome("ProveNaImaginacaoActivity")
        }
    }

    // =========================================
    // FUNÇÃO PARA ADICIONAR IMAGENS
    // =========================================

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

        imagem.scaleType = ImageView.ScaleType.FIT_CENTER

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(imagem, parametros)

        return imagem
    }

    // =========================================
    // BOTÕES AMARELOS COM CONTORNO E SOMBRA
    // =========================================

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

        val botao = TextView(this)

        // =====================================
        // CONFIGURAÇÃO DO TEXTO
        // =====================================

        botao.text = texto

        botao.gravity = Gravity.CENTER

        botao.includeFontPadding = false

        // Azul forte
        botao.setTextColor(
            Color.parseColor("#1451AF")
        )

        // Tamanho do texto
        botao.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            20f * sy
        )

        // Fonte arredondada
        botao.typeface = Typeface.create(
            "sans-serif-rounded",
            Typeface.BOLD
        )

        botao.maxLines = 1

        botao.setPadding(
            (12 * sx).toInt(),
            0,
            (12 * sx).toInt(),
            0
        )

        // =====================================
        // FORMATO ARREDONDADO
        // =====================================

        val arredondamento = 30f * sy

        // =====================================
        // APARÊNCIA NORMAL
        // =====================================

        val fundoNormal = GradientDrawable().apply {

            // Amarelo-claro
            setColor(
                Color.parseColor("#FFFFB5")
            )

            // Cantos arredondados
            cornerRadius = arredondamento

            // Contorno dourado
            setStroke(
                (2.5f * sy).toInt().coerceAtLeast(1),
                Color.parseColor("#F5B800")
            )
        }

        // =====================================
        // APARÊNCIA QUANDO PRESSIONADO
        // =====================================

        val fundoPressionado = GradientDrawable().apply {

            // Amarelo um pouco mais intenso
            setColor(
                Color.parseColor("#FFE878")
            )

            cornerRadius = arredondamento

            setStroke(
                (2.5f * sy).toInt().coerceAtLeast(1),
                Color.parseColor("#E5A000")
            )
        }

        // =====================================
        // ALTERNAR COR AO PRESSIONAR
        // =====================================

        val estados = StateListDrawable().apply {

            addState(
                intArrayOf(android.R.attr.state_pressed),
                fundoPressionado
            )

            addState(
                intArrayOf(),
                fundoNormal
            )
        }

        botao.background = estados

        // =====================================
        // SOMBRA SUAVE
        // =====================================

        botao.elevation =
            5f * resources.displayMetrics.density

        // =====================================
        // INTERAÇÃO
        // =====================================

        botao.isClickable = true
        botao.isFocusable = true

        // =====================================
        // POSICIONAMENTO
        // =====================================

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt(),
            (altura * sy).toInt()
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(botao, parametros)

        // =====================================
        // ANIMAÇÃO DE CLIQUE
        // =====================================

        botao.setOnTouchListener { view, evento ->

            when (evento.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    view.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(100)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
            }

            // Permite executar o clique normalmente
            false
        }

        // =====================================
        // ABRIR A TELA CORRESPONDENTE
        // =====================================

        botao.setOnClickListener {
            acao()
        }
    }

    // =========================================
    // ABRIR ACTIVITY
    // =========================================

    private fun abrirTela(
        classe: Class<out Activity>
    ) {

        try {

            startActivity(
                Intent(this, classe)
            )

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Não foi possível abrir essa tela.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================================
    // ABRIR ACTIVITY PELO NOME
    // =========================================

    private fun abrirTelaPorNome(
        nomeClasse: String
    ) {

        try {

            val classe = Class.forName(
                "$packageName.$nomeClasse"
            ).asSubclass(Activity::class.java)

            abrirTela(classe)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "A tela $nomeClasse ainda não está disponível.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}