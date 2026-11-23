
package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class MaoszinhasMagicas2Activity : Activity() {

    private lateinit var root: FrameLayout

    private val larguraBase = 922f
    private val alturaBase = 363f

    private val handler = Handler(Looper.getMainLooper())

    private val alvosGif =
        mutableListOf<CustomTarget<GifDrawable>>()

    private val animaisConcluidos = mutableSetOf<String>()

    private val totalAnimais = 4
    private var jogoFinalizado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_maoszinhas_magicas2)

        root = findViewById(R.id.telaMaoszinhasMagicas2)

        root.post {
            montarJogo()
        }
    }

    // ================================================
    // MONTAR NÍVEL 2
    // ================================================

    private fun montarJogo() {

        root.removeAllViews()

        for (alvo in alvosGif) {
            Glide.with(this).clear(alvo)
        }

        alvosGif.clear()
        animaisConcluidos.clear()
        jogoFinalizado = false

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase

        // PLANO DE FUNDO DO MAR

        val fundo = ImageView(this).apply {
            setImageResource(
                R.drawable.planomaoszinhasmagicas2
            )
            scaleType = ImageView.ScaleType.FIT_XY
        }

        root.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // BOTÃO RETORNO AMARELO CLARO

        val voltar = adicionarImagem(
            "botaoretornoamareloclaro",
            19, 25, 40, 40, sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true

        voltar.setOnClickListener {
            voltarParaJogos()
        }

        // TÍTULO

        adicionarImagem(
            "titulodescubracomotoque",
            205, 2, 490, 85, sx, sy
        )

        // INSTRUÇÃO

        val instrucao = TextView(this).apply {
            text = "Toque nos animais"
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                20f * sy
            )
            setTextColor(Color.rgb(85, 110, 125))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        adicionarView(
            instrucao,
            311, 76, 300, 35, sx, sy
        )

        // POLVO - ESQUERDA

        adicionarAnimal(
            "polvo.gif",
            55, 115, 195, 165,
            sx, sy
        )

        // PEIXE - PARTE SUPERIOR

        adicionarAnimal(
            "peixe.gif",
            330, 100, 165, 105,
            sx, sy
        )

        // TUBARÃO - PARTE INFERIOR

        adicionarAnimal(
            "tubarao.gif",
            360, 208, 275, 125,
            sx, sy
        )

        // CAVALO-MARINHO - DIREITA

        adicionarAnimal(
            "cavalomarinho.gif",
            690, 72, 135, 160,
            sx, sy
        )
    }

    // ================================================
    // ADICIONAR IMAGENS DRAWABLE
    // ================================================

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

        adicionarView(
            imagem,
            x, y, largura, altura, sx, sy
        )

        return imagem
    }

    // ================================================
    // ADICIONAR GIFS DOS ANIMAIS
    // ================================================

    private fun adicionarAnimal(
        arquivo: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val animal = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            isClickable = true
            contentDescription = arquivo.removeSuffix(".gif")
        }

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

                // COMEÇA PARADO
                resource.stop()
                resource.setVisible(true, false)
                resource.setLoopCount(1)

                animal.setImageDrawable(resource)

                // CONTAR APÓS TERMINAR A ANIMAÇÃO
                resource.registerAnimationCallback(
                    object : Animatable2Compat.AnimationCallback() {

                        override fun onAnimationEnd(
                            drawable: Drawable?
                        ) {
                            runOnUiThread {
                                if (!jogoFinalizado) {
                                    registrarAnimalConcluido(arquivo)
                                }
                            }
                        }
                    }
                )

                // TOQUE PARA ANIMAR
                animal.setOnClickListener {

                    if (
                        jogoFinalizado ||
                        arquivo in animaisConcluidos
                    ) {
                        return@setOnClickListener
                    }

                    val gif =
                        animal.drawable as? GifDrawable

                    if (gif != null && !gif.isRunning) {
                        gif.start()
                    }
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
                    this@MaoszinhasMagicas2Activity,
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

    // ================================================
    // CONCLUSÃO DOS ANIMAIS
    // ================================================

    private fun registrarAnimalConcluido(
        arquivo: String
    ) {

        if (jogoFinalizado) return

        val novoAnimal = animaisConcluidos.add(arquivo)

        if (!novoAnimal) return

        if (animaisConcluidos.size == totalAnimais) {

            jogoFinalizado = true

            handler.postDelayed({

                if (!isFinishing && !isDestroyed) {
                    mostrarPopupVitoria()
                }

            }, 500)
        }
    }

    // ================================================
    // POP-UP DE VITÓRIA
    // ================================================

    private fun mostrarPopupVitoria() {

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase

        // FUNDO ESCURECIDO

        val fundoEscuro = View(this).apply {
            setBackgroundColor(
                Color.argb(85, 35, 45, 40)
            )
            isClickable = true
            alpha = 0f
        }

        root.addView(
            fundoEscuro,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        fundoEscuro.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        // PAINEL

        val popup = FrameLayout(this).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 245),
                    Color.rgb(255, 249, 228)
                )
            ).apply {

                cornerRadius = 28f * minOf(sx, sy)

                setStroke(
                    (3 * minOf(sx, sy)).toInt()
                        .coerceAtLeast(2),
                    Color.rgb(245, 222, 160)
                )
            }

            elevation = 12f
            isClickable = true
        }

        root.addView(
            popup,
            FrameLayout.LayoutParams(
                (540 * sx).toInt(),
                (260 * sy).toInt(),
                Gravity.CENTER
            )
        )

        popup.scaleX = 0.6f
        popup.scaleY = 0.6f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()

        // POSICIONAR NO POP-UP

        fun colocar(
            view: View,
            x: Int,
            y: Int,
            largura: Int,
            altura: Int
        ) {
            val params = FrameLayout.LayoutParams(
                (largura * sx).toInt().coerceAtLeast(1),
                (altura * sy).toInt().coerceAtLeast(1)
            ).apply {
                leftMargin = (x * sx).toInt()
                topMargin = (y * sy).toInt()
            }

            popup.addView(view, params)
        }

        fun criarTexto(
            texto: String,
            tamanho: Float,
            cor: Int
        ): TextView {

            return TextView(this).apply {
                this.text = texto
                setTextColor(cor)
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tamanho * minOf(sx, sy)
                )
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                includeFontPadding = false
            }
        }

        // GIRAFINHA NO POP-UP

        val girafinha = ImageView(this).apply {

            val id = resources.getIdentifier(
                "logoinferior",
                "drawable",
                packageName
            )

            if (id != 0) {
                setImageResource(id)
            }

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        colocar(
            girafinha,
            215, 0, 110, 95
        )

        girafinha.scaleX = 0f
        girafinha.scaleY = 0f

        girafinha.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(200)
            .setDuration(600)
            .setInterpolator(
                OvershootInterpolator(1.8f)
            )
            .start()

        // ESTRELAS

        colocar(
            criarTexto(
                "✦", 26f,
                Color.rgb(245, 210, 135)
            ),
            75, 40, 35, 35
        )

        colocar(
            criarTexto(
                "✦", 26f,
                Color.rgb(245, 210, 135)
            ),
            430, 40, 35, 35
        )

        // TÍTULO

        colocar(
            criarTexto(
                "Parabéns, explorador!",
                32f,
                Color.rgb(165, 138, 199)
            ),
            35, 85, 470, 48
        )

        // MENSAGEM

        colocar(
            criarTexto(
                "Você descobriu os animais do mar!",
                21f,
                Color.rgb(145, 123, 105)
            ),
            35, 135, 470, 38
        )

        // BOTÃO VOLTAR

        val botaoVoltar = criarBotaoPopup(
            "← Voltar",
            Color.rgb(190, 232, 248),
            Color.rgb(155, 215, 240),
            Color.rgb(135, 195, 225),
            sx, sy
        )

        colocar(
            botaoVoltar,
            55, 190, 195, 52
        )

        botaoVoltar.setOnClickListener {
            voltarParaJogos()
        }

        // BOTÃO PRÓXIMO NÍVEL

        val botaoProximo = criarBotaoPopup(
            "Próximo nível →",
            Color.rgb(205, 242, 210),
            Color.rgb(175, 225, 185),
            Color.rgb(145, 205, 160),
            sx, sy
        )

        colocar(
            botaoProximo,
            275, 190, 210, 52
        )

        botaoProximo.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MaoszinhasMagicas3Activity::class.java
                )
            )

            finish()
        }
    }

    // ================================================
    // BOTÕES DO POP-UP
    // ================================================

    private fun criarBotaoPopup(
        texto: String,
        corInicio: Int,
        corFim: Int,
        corBorda: Int,
        sx: Float,
        sy: Float
    ): TextView {

        return TextView(this).apply {

            this.text = texto

            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(75, 95, 95))

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                23f * minOf(sx, sy)
            )

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(corInicio, corFim)
            ).apply {

                cornerRadius = 20f * minOf(sx, sy)

                setStroke(
                    (2 * minOf(sx, sy)).toInt()
                        .coerceAtLeast(1),
                    corBorda
                )
            }

            elevation = 4f
            isClickable = true
            isFocusable = true
        }
    }

    // ================================================
    // VOLTAR PARA AS CINCO OPÇÕES
    // ================================================

    private fun voltarParaJogos() {

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

    // ================================================
    // POSICIONAR ELEMENTOS
    // ================================================

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

    // ================================================
    // LIBERAR RECURSOS
    // ================================================

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        for (alvo in alvosGif) {
            Glide.with(this).clear(alvo)
        }

        alvosGif.clear()

        super.onDestroy()
    }
}
