package com.example.mobilejogos


import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class BibliotecaSonoraActivity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var audioAtual: MediaPlayer? = null



    private val audioUrso = R.raw.som_urso
    private val audioBola = R.raw.som_bola
    private val audioCavalinho = R.raw.som_cavalinho
    private val audioMeninoAviao = R.raw.som_aviao

    private val audioDinossauro = R.raw.som_aviao
    private val audioBrinquedos = R.raw.som_brinquedos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_biblioteca_sonora)

        tela = findViewById(R.id.telaBibliotecaSonora)

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {
        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase


        adicionarImagem(
            "planofundobibliotecasonora",
            0, 0, 900, 393, sx, sy
        )


        val voltar = adicionarImagem(
            "botaoretornorosa",
            18, 25, 60, 60, sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true
        voltar.setOnClickListener {
            pararAudio()
            finish()
        }

        adicionarImagem(
            "titulobibliotecasonora",
            202, 7, 480, 50, sx, sy
        )


        val urso = adicionarImagem(
            "urso",
            78, 172, 112, 145, sx, sy
        )
        configurarToque(urso, audioUrso)


        val bola = adicionarImagem(
            "bola",
            188, 276, 108, 108, sx, sy
        )
        configurarToque(bola, audioBola)


        val cavalinho = adicionarImagem(
            "cavalinho",
            258, 111, 175, 170, sx, sy
        )
        configurarToque(cavalinho, audioCavalinho)


        val meninoAviao = adicionarImagem(
            "meninocomaviao",
            438, 78, 168, 190, sx, sy
        )
        configurarToque(meninoAviao, audioMeninoAviao)

        val Dinossauro = adicionarImagem(
            "Dinossauro",
            438, 78, 168, 190, sx, sy
        )
        configurarToque(meninoAviao, audioMeninoAviao)

        val brinquedos = adicionarImagem(
            "brinquedos",
            490, 288, 174, 96, sx, sy
        )
        configurarToque(brinquedos, audioBrinquedos)
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

    private fun configurarToque(
        imagem: ImageView,
        recursoAudio: Int
    ) {
        imagem.isClickable = true

        imagem.setOnClickListener {
            tocarAudio(recursoAudio)
        }
    }

    private fun tocarAudio(recursoAudio: Int) {

        pararAudio()

        try {
            audioAtual = MediaPlayer.create(this, recursoAudio)

            if (audioAtual == null) {
                Toast.makeText(
                    this,
                    "Não foi possível abrir o áudio.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            audioAtual?.setOnCompletionListener { player ->
                player.release()

                if (audioAtual === player) {
                    audioAtual = null
                }
            }

            audioAtual?.start()

        } catch (e: Exception) {
            pararAudio()

            Toast.makeText(
                this,
                "Erro ao reproduzir o áudio.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun pararAudio() {
        audioAtual?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (_: Exception) {

            }

            player.release()
        }

        audioAtual = null
    }

    override fun onStop() {
        pararAudio()
        super.onStop()
    }

    override fun onDestroy() {
        pararAudio()
        super.onDestroy()
    }
}
