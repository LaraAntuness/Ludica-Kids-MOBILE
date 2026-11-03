
package com.example.mobilejogos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private val handler = Handler(Looper.getMainLooper())

    private val animais = listOf(
        Animal("coelho", "coelho", 650, 205, 95, 115),
        Animal("capivara", "capivara", 290, 225, 115, 80),
        Animal("onca", "onça", 175, 120, 110, 85),
        Animal("passaro", "pássaro", 590, 65, 60, 55),
        Animal("burrinho", "burrinho", 15, 170, 155, 155)
    )

    private val selecionados = mutableSetOf<String>()
    private var indiceAtual = 0
    private var finalizado = false

    private var pedidoView: TextView? = null
    private var mensagemView: TextView? = null

    data class Animal(
        val imagem: String,
        val nome: String,
        val x: Int,
        val y: Int,
        val largura: Int,
        val altura: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(203, 224, 237))
        setContentView(root)

        root.post {
            montarJogo()
        }
    }

    private fun montarJogo() {
        root.removeAllViews()

        val sx = root.width / 874f
        val sy = root.height / 402f

        adicionarImagem(
            "fundo_toque_nos_animais",
            0, 0, 874, 402, sx, sy
        )


        val voltar = adicionarImagem(
            "botao_retorno",
            19, 25, 40, 40, sx, sy
        )

        voltar.isClickable = true
        voltar.setOnClickListener {
            finish()
        }


        adicionarImagem(
            "titulo_toquenosanimais",
            255, 18, 365, 48, sx, sy
        )


        pedidoView = TextView(this).apply {
            text = ""
            textSize = 28f
            setTextColor(Color.rgb(87, 75, 25))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setShadowLayer(2f, 1f, 1f, Color.WHITE)
            includeFontPadding = true
            maxLines = 1
            isSingleLine = true
        }


        adicionarView(
            pedidoView!!,
            240, 65, 394, 42, sx, sy
        )

        mensagemView = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(145, 48, 40))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = true
            visibility = View.GONE
        }

        adicionarView(
            mensagemView!!,
            235, 108, 404, 36, sx, sy
        )


        animais.forEach { animal ->


            val halo = FrameLayout(this)

            val fundoHalo = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(100, 255, 221, 40))
                setStroke(
                    (4 * minOf(sx, sy)).toInt().coerceAtLeast(2),
                    Color.rgb(255, 215, 0)
                )
            }

            halo.background = fundoHalo
            halo.visibility =
                if (animal.imagem in selecionados) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            adicionarView(
                halo,
                animal.x,
                animal.y,
                animal.largura,
                animal.altura,
                sx,
                sy
            )


            val imagemId = resources.getIdentifier(
                animal.imagem,
                "drawable",
                packageName
            )

            val imagem = ImageView(this).apply {
                if (imagemId != 0) {
                    setImageResource(imagemId)
                }

                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = animal.nome
                isClickable = true
                isFocusable = true
            }

            val parametros = FrameLayout.LayoutParams(
                (animal.largura * sx).toInt(),
                (animal.altura * sy).toInt()
            ).apply {
                leftMargin = (animal.x * sx).toInt()
                topMargin = (animal.y * sy).toInt()
            }

            root.addView(imagem, parametros)

            imagem.setOnClickListener {
                verificarAnimal(animal, halo, sx, sy)
            }
        }

        atualizarPedido()
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

        val imagemId = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (imagemId != 0) {
            imagem.setImageResource(imagemId)
        }

        imagem.scaleType = ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem, x, y, largura, altura, sx, sy
        )

        return imagem
    }

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

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        root.addView(view, parametros)
    }

    private fun verificarAnimal(
        animal: Animal,
        halo: View,
        sx: Float,
        sy: Float
    ) {
        if (finalizado || animal.imagem in selecionados) {
            return
        }

        val esperado = animais[indiceAtual]

        if (animal.imagem != esperado.imagem) {
            mensagemView?.apply {
                text = "Animal errado, tente novamente!"
                textSize = 15f
                setTextColor(Color.rgb(145, 48, 40))
                visibility = View.VISIBLE
            }

            handler.removeCallbacksAndMessages(null)

            handler.postDelayed({
                if (!finalizado) {
                    mensagemView?.visibility = View.GONE
                }
            }, 1800)

            return
        }

        selecionados.add(animal.imagem)
        halo.visibility = View.VISIBLE

        indiceAtual++

        if (indiceAtual == animais.size) {
            finalizarJogo(sx, sy)
        } else {
            atualizarPedido()
        }
    }

    private fun atualizarPedido() {
        if (indiceAtual >= animais.size) {
            return
        }

        val nomeAnimal = animais[indiceAtual].nome

        pedidoView?.apply {
            text = "Toque no $nomeAnimal"
            visibility = View.VISIBLE
        }

        mensagemView?.visibility = View.GONE
    }

    private fun finalizarJogo(sx: Float, sy: Float) {
        finalizado = true
        pedidoView?.visibility = View.GONE

        mensagemView?.apply {
            text = "Parabéns! Você encontrou todos os animais!"
            textSize = 18f
            setTextColor(Color.rgb(55, 115, 45))
            visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
