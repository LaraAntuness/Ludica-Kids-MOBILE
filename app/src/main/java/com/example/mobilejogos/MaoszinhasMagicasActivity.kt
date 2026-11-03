package com.example.mobilejogos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class MaoszinhasMagicasActivity : Activity() {

    private lateinit var root: FrameLayout

    private val larguraBase = 922f
    private val alturaBase = 363f

    private val alvosGif =
        mutableListOf<CustomTarget<GifDrawable>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_maoszinhas_magicas)

        root = findViewById(R.id.telaMaoszinhasMagicas)

        root.post {
            montarJogo()
        }
    }

    private fun montarJogo() {
        root.removeAllViews()
        alvosGif.clear()

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase


        adicionarImagem(
            "planomaoszinhasmagicas",
            0, 0, 922, 363, sx, sy
        )


        val voltar = adicionarImagem(
            "botaoretornocinza",
            18, 18, 42, 42, sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true

        voltar.setOnClickListener {
            finish()
        }


        adicionarImagem(
            "titulodescubracomotoque",
            235, 8, 452, 55, sx, sy
        )


        adicionarAnimal(
            "lobo.gif",
            77, 40, 210, 180, sx, sy
        )

        adicionarAnimal(
            "sapo.gif",
            420, 185, 210, 180, sx, sy
        )

        adicionarAnimal(
            "castor.gif",
            285, 210, 210, 180, sx, sy
        )

        adicionarAnimal(
            "macaco.gif",
            600, 100, 300, 250, sx, sy
        )
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
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x, y, largura, altura, sx, sy
        )

        return imagem
    }

    private fun adicionarAnimal(
        arquivo: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val animal = ImageView(this)

        animal.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            animal,
            x, y, largura, altura, sx, sy
        )

        val caminho = "file:///android_asset/$arquivo"

        val alvo = object : CustomTarget<GifDrawable>() {

            override fun onResourceReady(
                resource: GifDrawable,
                transition: Transition<in GifDrawable>?
            ) {
                resource.stop()
                resource.setVisible(true, false)

                animal.setImageDrawable(resource)

                animal.setOnClickListener {
                    val gif =
                        animal.drawable as? GifDrawable

                    gif?.start()
                }
            }

            override fun onLoadCleared(
                placeholder: Drawable?
            ) {
                animal.setImageDrawable(null)
            }

            override fun onLoadFailed(
                errorDrawable: Drawable?
            ) {
                super.onLoadFailed(errorDrawable)

                Toast.makeText(
                    this@MaoszinhasMagicasActivity,
                    "Não foi possível carregar $arquivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        alvosGif.add(alvo)

        Glide.with(this)
            .asGif()
            .load(caminho)
            .into(alvo)
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

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        root.addView(view, parametros)
    }

    override fun onDestroy() {
        Glide.with(this).clear(root)
        alvosGif.clear()
        super.onDestroy()
    }
}
