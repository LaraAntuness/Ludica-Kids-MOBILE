
package com.example.mobilejogos

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlin.math.*

class VisualJogosSensoriais(
    private val context: Context,
    private val escala: Float
) {

    data class Paleta(
        val topo: String,
        val base: String,
        val borda: String,
        val sombra: String,
        val texto: String
    )

    val azul = Paleta(
        "#DDF4FF", "#B7E3FA",
        "#91C9E9", "#78B3D8", "#366C96"
    )

    val rosa = Paleta(
        "#FFE6F1", "#F8C8DF",
        "#ECA9C9", "#D88DB4", "#9C4E7C"
    )

    val amarelo = Paleta(
        "#FFF7D7", "#FFE6A3",
        "#ECCD7D", "#DAB35C", "#946C25"
    )

    val roxo = Paleta(
        "#F1E4FF", "#DCC4F5",
        "#C4A1E6", "#A986CE", "#704B9E"
    )

    val verde = Paleta(
        "#E4F8D7", "#BCE4A6",
        "#A1CE88", "#83B46D", "#46753B"
    )

    val pessego = Paleta(
        "#FFEBDD", "#FBD1B9",
        "#E7B49A", "#D09B7E", "#9A6041"
    )

    fun cor(valor: String): Int =
        Color.parseColor(valor)

    fun px(valor: Number): Int =
        (valor.toFloat() * escala).roundToInt()

    fun colocar(
        pai: FrameLayout,
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ) {
        pai.addView(
            view,
            FrameLayout.LayoutParams(
                px(largura).coerceAtLeast(1),
                px(altura).coerceAtLeast(1)
            ).apply {
                leftMargin = px(x)
                topMargin = px(y)
            }
        )
    }

    fun texto(
        conteudo: String,
        tamanho: Float,
        corTexto: String
    ): TextView = TextView(context).apply {

        text = conteudo
        gravity = Gravity.CENTER

        setTextColor(cor(corTexto))

        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            tamanho * escala
        )

        typeface = Typeface.create(
            "sans-serif-rounded",
            Typeface.BOLD
        )

        includeFontPadding = false
    }

    fun imagem(nome: String): ImageView =
        ImageView(context).apply {

            val id = resources.getIdentifier(
                nome,
                "drawable",
                context.packageName
            )

            if (id != 0) {
                setImageResource(id)
            }

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

    fun fundo(
        topo: String,
        base: String,
        raio: Float,
        borda: String,
        espessura: Int = 2
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(cor(topo), cor(base))
        ).apply {

            cornerRadius = raio * escala

            setStroke(
                px(espessura).coerceAtLeast(1),
                cor(borda)
            )
        }
    }

    // BOTÕES COM PROFUNDIDADE E BORDA DUPLA

    fun fundo3D(
        paleta: Paleta,
        raio: Float = 25f
    ): LayerDrawable {

        val sombra = GradientDrawable().apply {
            setColor(cor(paleta.sombra))
            cornerRadius = raio * escala
        }

        val superficie = fundo(
            paleta.topo,
            paleta.base,
            raio,
            paleta.borda,
            3
        )

        val brilho = GradientDrawable().apply {

            setColor(Color.TRANSPARENT)

            cornerRadius =
                (raio - 5f) * escala

            setStroke(
                px(2).coerceAtLeast(1),
                Color.argb(180, 255, 255, 255)
            )
        }

        return LayerDrawable(
            arrayOf(sombra, superficie, brilho)
        ).apply {

            setLayerInset(
                1, 0, 0, 0, px(7)
            )

            setLayerInset(
                2,
                px(6),
                px(6),
                px(6),
                px(13)
            )
        }
    }

    // ANIMAÇÃO DE PRESSIONAR

    fun toque(
        view: View,
        acao: () -> Unit
    ) {

        view.isClickable = true
        view.isFocusable = true
        view.isSoundEffectsEnabled = false

        view.setOnTouchListener { v, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    v.animate().cancel()

                    v.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .translationY(px(3).toFloat())
                        .setDuration(90)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationY(0f)
                        .setDuration(120)
                        .start()
                }
            }

            false
        }

        view.setOnClickListener {
            acao()
        }
    }

    // CARTÃO DE ALIMENTO OU BEBIDA

    fun cartao(
        pai: FrameLayout,
        nomeImagem: String,
        nome: String,
        paleta: Paleta,
        x: Int,
        y: Int,
        acao: () -> Unit
    ) {

        val cartao = FrameLayout(context).apply {

            background = fundo3D(paleta)

            elevation = px(5).toFloat()

            contentDescription = nome
        }

        colocar(pai, cartao, x, y, 228, 108)

        val circulo = View(context).apply {

            background = GradientDrawable().apply {

                shape = GradientDrawable.OVAL

                setColor(
                    Color.argb(125, 255, 255, 255)
                )
            }
        }

        colocar(cartao, circulo, 14, 12, 78, 78)

        colocar(
            cartao,
            imagem(nomeImagem),
            18, 15, 70, 72
        )

        val rotulo = texto(
            nome,
            if (nome.length > 10) 16f else 19f,
            paleta.texto
        )

        colocar(
            cartao,
            rotulo,
            96, 15, 120, 75
        )

        toque(cartao, acao)
    }

    // BOTÃO DE NAVEGAÇÃO

    fun botao(
        pai: FrameLayout,
        nome: String,
        paleta: Paleta,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        acao: () -> Unit
    ) {

        val botao = texto(
            nome,
            19f,
            paleta.texto
        ).apply {

            background = fundo3D(paleta)

            elevation = px(4).toFloat()

            setPadding(0, 0, 0, px(7))
        }

        colocar(
            pai, botao,
            x, y, largura, altura
        )

        toque(botao, acao)
    }

    // INDICADOR DE NÍVEIS

    fun progresso(
        pai: FrameLayout,
        nivel: Int
    ) {

        val painel = FrameLayout(context).apply {

            background = fundo(
                "#FFFFFF",
                "#F2F9FD",
                21f,
                "#B8DCEE"
            )

            elevation = px(4).toFloat()
        }

        colocar(
            pai, painel,
            307, 348, 260, 38
        )

        colocar(
            painel,
            texto(
                "Nível $nivel/3",
                14f,
                "#35658D"
            ),
            8, 3, 106, 30
        )

        for (i in 1..3) {

            val paleta = when {

                i < nivel -> verde

                i == nivel -> azul

                else -> Paleta(
                    "#F5F7F9",
                    "#E9EFF3",
                    "#D2E2EB",
                    "#C8D8E1",
                    "#8399AA"
                )
            }

            val etapa = texto(
                if (i < nivel) "✓" else "$i",
                14f,
                paleta.texto
            ).apply {

                background = fundo(
                    paleta.topo,
                    paleta.base,
                    20f,
                    paleta.borda
                )
            }

            colocar(
                painel,
                etapa,
                119 + (i - 1) * 43,
                5, 28, 28
            )
        }
    }

    // BALÃO DE PERGUNTA

    fun balao(
        pai: FrameLayout,
        pergunta: String,
        paleta: Paleta
    ): FrameLayout {

        val caixa = FrameLayout(context).apply {

            elevation = px(5).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            pai, caixa,
            29, 76, 267, 116
        )

        val desenho = object : View(context) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 267f,
                    height / 116f
                )

                val path = Path().apply {

                    moveTo(26f, 23f)

                    cubicTo(
                        25f, 4f,
                        75f, 0f,
                        91f, 13f
                    )

                    cubicTo(
                        111f, -1f,
                        153f, -1f,
                        174f, 13f
                    )

                    cubicTo(
                        215f, 0f,
                        252f, 12f,
                        249f, 34f
                    )

                    cubicTo(
                        269f, 53f,
                        260f, 82f,
                        237f, 86f
                    )

                    cubicTo(
                        207f, 102f,
                        174f, 88f,
                        151f, 96f
                    )

                    lineTo(113f, 110f)
                    lineTo(119f, 96f)

                    cubicTo(
                        78f, 105f,
                        48f, 93f,
                        29f, 84f
                    )

                    cubicTo(
                        1f, 77f,
                        0f, 39f,
                        26f, 23f
                    )

                    close()
                }

                paint.style = Paint.Style.FILL
                paint.color = cor("#FFF9EF")

                canvas.drawPath(path, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3.5f
                paint.strokeJoin = Paint.Join.ROUND
                paint.color = cor(paleta.borda)

                canvas.drawPath(path, paint)

                canvas.restore()
            }
        }

        colocar(caixa, desenho, 0, 0, 267, 116)

        colocar(
            caixa,
            texto(
                pergunta,
                18f,
                paleta.texto
            ),
            20, 16, 227, 76
        )

        return caixa
    }

    // ESTRELA DOURADA

    fun estrela(): View = object : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val cx = width / 2f
            val cy = height / 2f

            val raio =
                min(width, height) * 0.43f

            val path = Path()

            for (i in 0 until 10) {

                val angulo =
                    -PI / 2 + i * PI / 5

                val r =
                    if (i % 2 == 0)
                        raio
                    else
                        raio * 0.49f

                val x =
                    cx + cos(angulo).toFloat() * r

                val y =
                    cy + sin(angulo).toFloat() * r

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            path.close()

            paint.strokeJoin = Paint.Join.ROUND

            canvas.save()
            canvas.translate(0f, px(5).toFloat())

            paint.shader = null
            paint.style = Paint.Style.FILL
            paint.color = cor("#D5A02D")

            canvas.drawPath(path, paint)

            canvas.restore()

            paint.shader = LinearGradient(
                0f, 0f,
                0f, height.toFloat(),
                cor("#FFF5A3"),
                cor("#FFD34D"),
                Shader.TileMode.CLAMP
            )

            canvas.drawPath(path, paint)

            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = px(3).toFloat()
            paint.color = cor("#E4AC36")

            canvas.drawPath(path, paint)
        }
    }

    // LUPA DESENHADA EM KOTLIN

    fun lupa(): View = object : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            canvas.save()

            canvas.scale(
                width / 90f,
                height / 90f
            )

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 17f
            paint.color = cor("#315E86")

            canvas.drawLine(
                32f, 55f,
                12f, 78f,
                paint
            )

            paint.strokeWidth = 8f
            paint.color = cor("#8AC9F0")

            canvas.drawLine(
                32f, 55f,
                12f, 78f,
                paint
            )

            paint.style = Paint.Style.FILL
            paint.color = cor("#FFD66A")

            canvas.drawCircle(
                51f, 34f, 30f, paint
            )

            paint.color = cor("#4B8BB9")

            canvas.drawCircle(
                51f, 34f, 24f, paint
            )

            paint.color = cor("#BDEAFF")

            canvas.drawCircle(
                51f, 34f, 19f, paint
            )

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.WHITE

            canvas.drawArc(
                38f, 21f,
                64f, 47f,
                200f, 85f,
                false, paint
            )

            canvas.restore()
        }
    }

    // CABEÇALHO ONDULADO DO POP-UP ROXO

    fun cabecalhoRoxo(): View =
        object : View(context) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 510f,
                    height / 80f
                )

                val path = Path().apply {

                    moveTo(0f, 35f)

                    quadTo(
                        0f, 10f,
                        28f, 13f
                    )

                    quadTo(
                        65f, -3f,
                        100f, 10f
                    )

                    quadTo(
                        150f, -5f,
                        200f, 10f
                    )

                    quadTo(
                        255f, -5f,
                        310f, 10f
                    )

                    quadTo(
                        360f, -5f,
                        410f, 10f
                    )

                    quadTo(
                        450f, -3f,
                        482f, 13f
                    )

                    quadTo(
                        510f, 10f,
                        510f, 35f
                    )

                    lineTo(510f, 60f)

                    quadTo(
                        460f, 78f,
                        410f, 65f
                    )

                    quadTo(
                        360f, 80f,
                        310f, 65f
                    )

                    quadTo(
                        255f, 80f,
                        200f, 65f
                    )

                    quadTo(
                        150f, 80f,
                        100f, 65f
                    )

                    quadTo(
                        50f, 78f,
                        0f, 60f
                    )

                    close()
                }

                paint.style = Paint.Style.FILL

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, 80f,
                    cor("#EBD5FF"),
                    cor("#C6A0EF"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawPath(path, paint)

                paint.shader = null
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = cor("#AD86D7")

                canvas.drawPath(path, paint)

                canvas.restore()
            }
        }
}
