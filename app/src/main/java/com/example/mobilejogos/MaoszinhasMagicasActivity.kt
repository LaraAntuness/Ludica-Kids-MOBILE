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

        // =====================================
        // ORIENTAÇÃO HORIZONTAL
        // =====================================

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // =====================================
        // TELA CHEIA
        // =====================================

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(
            R.layout.activity_maoszinhas_magicas
        )

        root = findViewById(
            R.id.telaMaoszinhasMagicas
        )

        root.post {
            montarJogo()
        }
    }

    // =========================================
    // MONTAR TELA DO JOGO
    // =========================================

    private fun montarJogo() {

        root.removeAllViews()
        alvosGif.clear()

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase

        // =====================================
        // FUNDO PREENCHENDO TODA A TELA
        // =====================================

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planomaoszinhasmagicas
            )

            // Preenche toda a área disponível
            scaleType = ImageView.ScaleType.FIT_XY
        }

        val parametrosFundo = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Adiciona o fundo atrás dos demais elementos
        root.addView(fundo, 0, parametrosFundo)

        // =====================================
        // BOTÃO VOLTAR
        // =====================================

        val voltar = adicionarImagem(
            "botaoretornocinza",
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
            "titulodescubracomotoque",
            205, 2, 490, 85, sx, sy
        )

        // =====================================
        // LOBO
        // =====================================

        adicionarAnimal(
            "lobo.gif",
            77, 40, 210, 180, sx, sy
        )

        // =====================================
        // SAPO
        // =====================================

        adicionarAnimal(
            "sapo.gif",
            420, 185, 210, 180, sx, sy
        )

        // =====================================
        // CASTOR
        // =====================================

        adicionarAnimal(
            "castor.gif",
            285, 210, 210, 180, sx, sy
        )

        // =====================================
        // MACACO
        // =====================================

        adicionarAnimal(
            "macaco.gif",
            600, 100, 300, 250, sx, sy
        )
    }

    // =========================================
    // ADICIONAR IMAGENS
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

        // Preserva as proporções dos elementos
        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x, y, largura, altura, sx, sy
        )

        return imagem
    }

    // =========================================
    // ADICIONAR ANIMAIS COM GIF
    // =========================================

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

                // GIF começa parado
                resource.stop()

                resource.setVisible(true, false)

                animal.setImageDrawable(resource)

                // GIF é reproduzido quando a criança toca
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

    // =========================================
    // POSICIONAR ELEMENTOS
    // =========================================

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

        // Posição horizontal
        parametros.leftMargin =
            (x * sx).toInt()

        // Posição vertical
        parametros.topMargin =
            (y * sy).toInt()

        root.addView(view, parametros)
    }

    // =========================================
    // LIBERAR RECURSOS
    // =========================================

    override fun onDestroy() {

        for (alvo in alvosGif) {
            Glide.with(this).clear(alvo)
        }

        alvosGif.clear()

        super.onDestroy()
    }
}