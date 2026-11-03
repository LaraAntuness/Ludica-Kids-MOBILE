
package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
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

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_cinco_opcoes_dos_jogos)

        tela = findViewById(R.id.telaCincoOpcoes)
        tela.setBackgroundColor(Color.parseColor("#CBE0ED"))

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {
        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        tela.setBackgroundColor(Color.parseColor("#CBE0ED"))

        // Botão de voltar
        val voltar = adicionarImagem(
            "botaoretornoamarelo",
            18, 25, 40, 40, sx, sy
        )

        voltar.contentDescription = "Voltar"

        voltar.setOnClickListener {
            finish()
        }

        // Título
        adicionarImagem(
            "titulocincosentidos",
            202, 27, 480, 48, sx, sy
        )

        // Borboleta superior esquerda
        adicionarImagem(
            "borboleta",
            123, 76, 80, 75, sx, sy
        )

        // Girafa
        adicionarImagem(
            "girafa",
            53, 193, 180, 200, sx, sy
        )

        // Criança
        adicionarImagem(
            "crianca",
            735, 93, 190, 306, sx, sy
        )

        // Borboleta superior direita
        adicionarImagem(
            "borboleta2",
            664, 25, 40, 30, sx, sy
        )

        // Botão 1
        criarBotao(
            "OLHINHOS CURIOSOS",
            290, 94, 310, 48, sx, sy
        ) {
            abrirTela(MainActivity::class.java)
        }

        // Botão 2
        criarBotao(
            "MÃOZINHAS MÁGICAS",
            290, 149, 310, 48, sx, sy
        ) {
            abrirTela(MaoszinhasMagicasActivity::class.java)
        }

        // Botão 3
        criarBotao(
            "ESCUTE E DESCUBRA",
            290, 204, 310, 48, sx, sy
        ) {
            abrirTela(BibliotecaSonoraActivity::class.java)
        }

        // Botão 4
        criarBotao(
            "O NARIZ DETETIVE",
            290, 259, 310, 48, sx, sy
        ) {
            abrirTelaPorNome("DescubraOCheiroActivity")
        }

        // Botão 5
        criarBotao(
            "PROVE NA IMAGINAÇÃO",
            290, 314, 310, 48, sx, sy
        ) {
            abrirTelaPorNome("ProveNaImaginacaoActivity")
        }
    }

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

        botao.text = texto
        botao.gravity = Gravity.CENTER
        botao.includeFontPadding = false

        botao.setTextColor(Color.parseColor("#6897E6"))

        botao.setTextSize(
            android.util.TypedValue.COMPLEX_UNIT_PX,
            18f * sy
        )

        botao.setTypeface(
            Typeface.create(
                "sans-serif-condensed",
                Typeface.BOLD_ITALIC
            )
        )

        botao.maxLines = 1

        botao.setPadding(
            (8 * sx).toInt(),
            0,
            (8 * sx).toInt(),
            0
        )

        botao.background = GradientDrawable().apply {
            setColor(Color.parseColor("#FFFFAE"))
            cornerRadius = 22f * sy
        }

        botao.isClickable = true
        botao.isFocusable = true

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt(),
            (altura * sy).toInt()
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(botao, parametros)

        botao.setOnClickListener {
            acao()
        }
    }

    private fun abrirTela(classe: Class<out Activity>) {
        try {
            startActivity(Intent(this, classe))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Não foi possível abrir essa tela.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun abrirTelaPorNome(nomeClasse: String) {
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
