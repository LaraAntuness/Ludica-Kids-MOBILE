
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

        val superficie = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                cor(paleta.topo),
                cor(paleta.base)
            )
        ).apply {
            cornerRadius = raio * escala
        }

        return LayerDrawable(
            arrayOf(sombra, superficie)
        ).apply {
            setLayerInset(1, 0, 0, 0, px(6))
        }
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
