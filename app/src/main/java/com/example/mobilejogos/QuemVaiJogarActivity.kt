package com.example.mobilejogos

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class QuemVaiJogarActivity : Activity() {

    private lateinit var root: FrameLayout

    // CORES DO LÚDICA KIDS

    private val azul = Color.rgb(95, 145, 207)
    private val azulEscuro = Color.rgb(49, 91, 151)
    private val azulClaro = Color.rgb(230, 242, 255)
    private val fundo = Color.rgb(233, 233, 233)
    private val amareloPastel = Color.rgb(255, 221, 139)

    private val coresEstrelas = listOf(
        Color.rgb(255, 199, 70),
        Color.rgb(255, 225, 116),
        Color.rgb(113, 170, 233),
        Color.rgb(255, 156, 176),
        Color.rgb(174, 207, 255)
    )

    data class Crianca(
        val id: String,
        val nome: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_quem_vai_jogar)

        root = findViewById(R.id.gameRoot)

        montarTela()
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    // FUNÇÃO PARA CRIAR FUNDOS ARREDONDADOS

    private fun fundoArredondado(
        cor: Int,
        raio: Int,
        corBorda: Int? = null,
        larguraBorda: Int = 0
    ): GradientDrawable {

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(cor)
            cornerRadius = dp(raio).toFloat()

            if (corBorda != null) {
                setStroke(dp(larguraBorda), corBorda)
            }
        }
    }

    // ==========================================
    // MONTAGEM DA TELA PRINCIPAL
    // ==========================================

    private fun montarTela() {

        root.setBackgroundColor(fundo)

        root.clipChildren = false
        root.clipToPadding = false

        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            gravity =
                Gravity.TOP or Gravity.CENTER_HORIZONTAL

            // Permite mostrar a animação fora dos limites
            clipChildren = false
            clipToPadding = false

            setPadding(
                dp(20),
                dp(42),
                dp(20),
                dp(10)
            )
        }

        root.addView(
            conteudo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // TÍTULO "QUEM VAI JOGAR?"

        val titulo = ImageView(this).apply {
            setImageResource(R.drawable.quemvaijogar)

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        conteudo.addView(
            titulo,
            LinearLayout.LayoutParams(
                dp(330),
                dp(65)
            )
        )

        // ESPAÇAMENTO

        conteudo.addView(
            View(this),
            LinearLayout.LayoutParams(
                1,
                dp(20)
            )
        )

        // ======================================
        // LISTA HORIZONTAL DOS PERFIS
        // ======================================

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = true

            // CORREÇÃO:
            // Não corta os perfis quando aumentam
            clipChildren = false
            clipToPadding = false
        }

        val lista = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            // CORREÇÃO:
            // Permite que os círculos cresçam livremente
            clipChildren = false
            clipToPadding = false

            // Espaço extra para a animação
            setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(18)
            )
        }

        scroll.addView(
            lista,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        conteudo.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // CARREGAR CRIANÇAS

        val criancas = carregarCriancas()

        criancas.forEach { crianca ->
            adicionarCrianca(lista, crianca)
        }

        // ======================================
        // BOTÃO DE VOLTAR
        // ======================================

        val botaoVoltar = ImageView(this).apply {
            setImageResource(R.drawable.botaoazul)

            scaleType = ImageView.ScaleType.FIT_CENTER

            contentDescription = "Voltar"

            setOnClickListener {
                finish()
            }
        }

        val parametrosVoltar = FrameLayout.LayoutParams(
            dp(46),
            dp(46)
        ).apply {
            gravity = Gravity.TOP or Gravity.START

            leftMargin = dp(12)
            topMargin = dp(12)
        }

        root.addView(
            botaoVoltar,
            parametrosVoltar
        )
    }

    // ==========================================
    // CARD DE CADA CRIANÇA
    // ==========================================

    private fun adicionarCrianca(
        lista: LinearLayout,
        crianca: Crianca
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            isClickable = true

            // CORREÇÃO:
            // Evita cortar a bolinha durante o zoom
            clipChildren = false
            clipToPadding = false
        }

        val parametrosCard = LinearLayout.LayoutParams(
            dp(170),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = dp(12)
            marginEnd = dp(12)
        }

        lista.addView(
            card,
            parametrosCard
        )

        // ======================================
        // CÍRCULO AZUL
        // ======================================

        val circulo = FrameLayout(this).apply {

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(azul)
            }

            // Mantém a girafinha dentro do círculo
            clipToOutline = true
        }

        card.addView(
            circulo,
            LinearLayout.LayoutParams(
                dp(145),
                dp(145)
            )
        )

        // ======================================
        // GIRAFINHA
        // ======================================

        val girafa = ImageView(this).apply {
            setImageResource(R.drawable.girafapescoco)

            scaleType = ImageView.ScaleType.FIT_CENTER

            contentDescription =
                "Perfil de ${crianca.nome}"
        }

        val parametrosGirafa = FrameLayout.LayoutParams(
            dp(112),
            dp(127)
        ).apply {
            gravity = Gravity.CENTER
        }

        circulo.addView(
            girafa,
            parametrosGirafa
        )

        // ======================================
        // NOME DA CRIANÇA
        // ======================================

        val nome = TextView(this).apply {
            text = crianca.nome

            textSize = 19f

            setTextColor(Color.BLACK)

            typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER
        }

        val parametrosNome = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(15)
        }

        card.addView(
            nome,
            parametrosNome
        )

        // ======================================
        // CLIQUE NO PERFIL + ESTRELINHAS
        // ======================================

        card.setOnClickListener {

            card.isEnabled = false

            // Girafinha dá um pulinho
            circulo.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(180)
                .withEndAction {

                    circulo.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220)
                        .start()
                }
                .start()

            // Explosão de estrelinhas
            animarEstrelinhas(circulo)

            // Mostra o pop-up após a animação
            root.postDelayed({

                selecionarCrianca(crianca)

                card.isEnabled = true

            }, 650)
        }
    }

    // ==========================================
    // ANIMAÇÃO DAS ESTRELINHAS
    // ==========================================

    private fun animarEstrelinhas(perfil: View) {

        val posicaoPerfil = IntArray(2)
        perfil.getLocationOnScreen(posicaoPerfil)

        val posicaoRoot = IntArray(2)
        root.getLocationOnScreen(posicaoRoot)

        val centroX =
            posicaoPerfil[0] -
                    posicaoRoot[0] +
                    perfil.width / 2f

        val centroY =
            posicaoPerfil[1] -
                    posicaoRoot[1] +
                    perfil.height / 2f

        // Cria 22 estrelinhas
        repeat(22) { indice ->

            val estrela = TextView(this).apply {

                text = "★"

                textSize = Random.nextInt(17, 30).toFloat()

                setTextColor(
                    coresEstrelas.random()
                )

                gravity = Gravity.CENTER

                elevation = dp(15).toFloat()
            }

            val tamanho = dp(36)

            val parametros = FrameLayout.LayoutParams(
                tamanho,
                tamanho
            )

            parametros.leftMargin =
                (centroX - tamanho / 2f).toInt()

            parametros.topMargin =
                (centroY - tamanho / 2f).toInt()

            root.addView(
                estrela,
                parametros
            )

            val angulo =
                (2.0 * Math.PI * indice / 22.0) +
                        Random.nextDouble(-0.25, 0.25)

            val distancia =
                dp(Random.nextInt(95, 210)).toDouble()

            val movimentoX =
                (cos(angulo) * distancia).toFloat()

            val movimentoY =
                (sin(angulo) * distancia).toFloat()

            estrela.scaleX = 0.2f
            estrela.scaleY = 0.2f
            estrela.alpha = 1f

            val duracao =
                Random.nextLong(650, 1050)

            estrela.animate()
                .translationX(movimentoX)
                .translationY(movimentoY)
                .rotation(Random.nextFloat() * 360f)
                .scaleX(1.3f)
                .scaleY(1.3f)
                .alpha(0f)
                .setDuration(duracao)
                .setInterpolator(
                    DecelerateInterpolator()
                )
                .withEndAction {

                    root.removeView(estrela)
                }
                .start()
        }
    }

    // ==========================================
    // CARREGAR CRIANÇAS CADASTRADAS
    // ==========================================

    private fun carregarCriancas(): List<Crianca> {

        val preferencias = getSharedPreferences(
            "LudicaKids",
            Context.MODE_PRIVATE
        )

        val dados = preferencias.getString(
            "criancas",
            null
        )

        // Crianças de demonstração
        if (dados.isNullOrBlank()) {

            return listOf(
                Crianca("1", "Criança 1"),
                Crianca("2", "Criança 2"),
                Crianca("3", "Criança 3")
            )
        }

        return try {

            val array = JSONArray(dados)

            val criancas = mutableListOf<Crianca>()

            for (i in 0 until array.length()) {

                val objeto = array.getJSONObject(i)

                val id = objeto.optString(
                    "_id",
                    objeto.optString(
                        "id",
                        i.toString()
                    )
                )

                val nome = objeto.optString(
                    "nomeUsuario"
                ).ifBlank {

                    objeto.optString(
                        "nomeCompleto",
                        "Criança"
                    )
                }

                criancas.add(
                    Crianca(id, nome)
                )
            }

            criancas

        } catch (e: Exception) {

            emptyList()
        }
    }

    // ==========================================
    // SELECIONAR CRIANÇA
    // ==========================================

    private fun selecionarCrianca(crianca: Crianca) {

        val preferencias = getSharedPreferences(
            "LudicaKids",
            Context.MODE_PRIVATE
        )

        val objeto = JSONObject().apply {
            put("_id", crianca.id)
            put("nomeUsuario", crianca.nome)
        }

        preferencias.edit()
            .putString(
                "criancaSelecionada",
                objeto.toString()
            )
            .apply()

        mostrarPopup(crianca.nome)
    }

    // ==========================================
    // POP-UP INFANTIL
    // ==========================================

    private fun mostrarPopup(nomeCrianca: String) {

        val dialog = Dialog(this)

        val container = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
        }

        // ======================================
        // CARTÃO DO POP-UP
        // ======================================

        val cartao = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(26),
                dp(65),
                dp(26),
                dp(23)
            )

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.WHITE,
                    azulClaro
                )
            ).apply {

                cornerRadius = dp(30).toFloat()

                setStroke(
                    dp(2),
                    Color.rgb(188, 214, 244)
                )
            }

            elevation = dp(8).toFloat()
        }

        val parametrosCartao = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {

            topMargin = dp(52)

            leftMargin = dp(8)
            rightMargin = dp(8)
            bottomMargin = dp(10)
        }

        container.addView(
            cartao,
            parametrosCartao
        )

        // ======================================
        // ESTRELINHAS DECORATIVAS
        // ======================================

        val estrelaEsquerda = TextView(this).apply {

            text = "✦"

            textSize = 30f

            setTextColor(amareloPastel)

            gravity = Gravity.CENTER

            elevation = dp(18).toFloat()
        }

        val parametrosEstrelaEsquerda =
            FrameLayout.LayoutParams(
                dp(45),
                dp(45)
            ).apply {

                gravity = Gravity.TOP or Gravity.START

                topMargin = dp(70)
                leftMargin = dp(35)
            }

        container.addView(
            estrelaEsquerda,
            parametrosEstrelaEsquerda
        )

        val estrelaDireita = TextView(this).apply {

            text = "✦"

            textSize = 25f

            setTextColor(azul)

            gravity = Gravity.CENTER

            elevation = dp(18).toFloat()
        }

        val parametrosEstrelaDireita =
            FrameLayout.LayoutParams(
                dp(40),
                dp(40)
            ).apply {

                gravity = Gravity.TOP or Gravity.END

                topMargin = dp(80)
                rightMargin = dp(35)
            }

        container.addView(
            estrelaDireita,
            parametrosEstrelaDireita
        )

        // ======================================
        // GIRAFINHA NO TOPO
        // ======================================

        val fundoGirafa = FrameLayout(this).apply {

            background = GradientDrawable().apply {

                shape = GradientDrawable.OVAL

                setColor(
                    Color.rgb(255, 244, 211)
                )

                setStroke(
                    dp(4),
                    Color.WHITE
                )
            }

            elevation = dp(20).toFloat()
        }

        val parametrosFundoGirafa =
            FrameLayout.LayoutParams(
                dp(112),
                dp(112)
            ).apply {

                gravity =
                    Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }

        container.addView(
            fundoGirafa,
            parametrosFundoGirafa
        )

        val imagemGirafa = ImageView(this).apply {

            setImageResource(
                R.drawable.girafapescoco
            )

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val parametrosImagemGirafa =
            FrameLayout.LayoutParams(
                dp(94),
                dp(100)
            ).apply {

                gravity = Gravity.CENTER
            }

        fundoGirafa.addView(
            imagemGirafa,
            parametrosImagemGirafa
        )

        fundoGirafa.bringToFront()

        // ======================================
        // TÍTULO DO POP-UP
        // ======================================

        val tituloPopup = TextView(this).apply {

            text = "Oba! Vamos brincar?"

            textSize = 24f

            setTextColor(azulEscuro)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER
        }

        cartao.addView(
            tituloPopup,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // ======================================
        // SUBTÍTULO
        // ======================================

        val subtitulo = TextView(this).apply {

            text = "Que alegria ter você aqui!"

            textSize = 15f

            setTextColor(
                Color.rgb(106, 119, 137)
            )

            gravity = Gravity.CENTER
        }

        val parametrosSubtitulo =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = dp(7)
            }

        cartao.addView(
            subtitulo,
            parametrosSubtitulo
        )

        // ======================================
        // NOME DA CRIANÇA
        // ======================================

        val nome = TextView(this).apply {

            text = nomeCrianca

            textSize = 19f

            setTextColor(azulEscuro)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER

            setPadding(
                dp(28),
                dp(10),
                dp(28),
                dp(10)
            )

            background = fundoArredondado(
                Color.WHITE,
                25,
                Color.rgb(205, 226, 248),
                1
            )
        }

        val parametrosNome =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = dp(16)
            }

        cartao.addView(
            nome,
            parametrosNome
        )

        // ======================================
        // BOTÃO "VAMOS BRINCAR!"
        // ======================================

        val botaoBrincar = TextView(this).apply {

            text = "Vamos brincar!"

            textSize = 18f

            setTextColor(Color.WHITE)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(111, 166, 228),
                    Color.rgb(70, 126, 197)
                )
            ).apply {

                cornerRadius = dp(25).toFloat()
            }

            elevation = dp(5).toFloat()

            isClickable = true

            setOnClickListener {

                animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(100)
                    .withEndAction {

                        dialog.dismiss()

                        // Futuramente colocaremos aqui
                        // a navegação para o perfil infantil
                    }
                    .start()
            }
        }

        val parametrosBotao =
            LinearLayout.LayoutParams(
                dp(225),
                dp(49)
            ).apply {

                topMargin = dp(19)
            }

        cartao.addView(
            botaoBrincar,
            parametrosBotao
        )

        // ======================================
        // CONFIGURAÇÕES DO POP-UP
        // ======================================

        dialog.setContentView(container)

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        dialog.show()

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            val parametros = attributes

            parametros.width = minOf(
                dp(465),
                (resources.displayMetrics.widthPixels * 0.85f).toInt()
            )

            parametros.height =
                WindowManager.LayoutParams.WRAP_CONTENT

            parametros.gravity = Gravity.CENTER

            parametros.dimAmount = 0.48f

            attributes = parametros
        }

        // ======================================
        // ANIMAÇÃO DE ENTRADA DO POP-UP
        // ======================================

        container.scaleX = 0.85f
        container.scaleY = 0.85f
        container.alpha = 0f

        container.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(
                DecelerateInterpolator()
            )
            .start()

        // ANIMAÇÃO DA GIRAFINHA

        fundoGirafa.translationY = dp(10).toFloat()
        fundoGirafa.alpha = 0f

        fundoGirafa.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(100)
            .start()

        // ANIMAÇÃO DAS ESTRELINHAS DO POP-UP

        estrelaEsquerda.alpha = 0f
        estrelaEsquerda.rotation = -45f

        estrelaEsquerda.animate()
            .alpha(1f)
            .rotation(0f)
            .setDuration(600)
            .setStartDelay(180)
            .start()

        estrelaDireita.alpha = 0f
        estrelaDireita.rotation = 45f

        estrelaDireita.animate()
            .alpha(1f)
            .rotation(0f)
            .setDuration(600)
            .setStartDelay(250)
            .start()
    }
}