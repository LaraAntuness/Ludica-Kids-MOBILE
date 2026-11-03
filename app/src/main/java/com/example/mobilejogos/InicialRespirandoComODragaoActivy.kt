package com.example.mobilejogos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class InicialRespirandoComODragaoActivy : Activity() {

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

        setContentView(
            R.layout.activity_inicial_respirando_com_o_dragao
        )

        tela = findViewById(
            R.id.telaInicialRespirandoComODragao
        )

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {
        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        tela.setBackgroundColor(Color.parseColor("#B8D7F0"))

        // Fundo da tela
        val fundo = adicionarImagem(
            "planorespirandocomodragao",
            0, 0, 874, 393, sx, sy
        )

        fundo.scaleType = ImageView.ScaleType.FIT_XY

        // Envia o fundo para trás dos outros elementos
        fundo.elevation = 0f

        // Botão de voltar
        val voltar = adicionarImagem(
            "botaoretornoazul",
            20, 25, 40, 39, sx, sy
        )

        voltar.contentDescription = "Voltar"

        voltar.setOnClickListener {
            finish()
        }

        // Título
        adicionarImagem(
            "titulorespirandocomodragao",
            270, 5, 340, 75, sx, sy
        )

        // Botão iniciar
        val iniciar = adicionarImagem(
            "botaoiniciar",
            275, 280, 310, 80, sx, sy
        )

        iniciar.contentDescription = "Iniciar jogo"
        iniciar.isClickable = true
        iniciar.isFocusable = true

        iniciar.setOnClickListener {
            Toast.makeText(
                this,
                "O jogo será iniciado em breve!",
                Toast.LENGTH_SHORT
            ).show()

            // Futuramente, abrir a Activity do jogo aqui.
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
            Log.e(
                "InicialRespirandoComODragao",
                "Imagem não encontrada: $nome"
            )

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_LONG
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
}
