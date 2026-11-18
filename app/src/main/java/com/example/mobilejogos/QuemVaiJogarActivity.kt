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
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class QuemVaiJogarActivity : Activity() {

    private lateinit var root: FrameLayout

    private val azul = Color.rgb(95, 145, 207)
    private val fundo = Color.rgb(233, 233, 233)

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

    private fun montarTela() {

        root.setBackgroundColor(fundo)

        // CONTEÚDO PRINCIPAL

        val conteudo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

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

        // TÍTULO

        val titulo = ImageView(this).apply {
            setImageResource(R.drawable.quemvaijogar)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Quem vai jogar?"
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

        // LISTA HORIZONTAL

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = true
        }

        val lista = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            setPadding(
                dp(15),
                dp(5),
                dp(15),
                dp(10)
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

        // PERFIS

        val criancas = carregarCriancas()

        criancas.forEach { crianca ->
            adicionarCrianca(lista, crianca)
        }

        // BOTÃO VOLTAR

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

        root.addView(botaoVoltar, parametrosVoltar)
    }

    // PERFIL DA CRIANÇA

    private fun adicionarCrianca(
        lista: LinearLayout,
        crianca: Crianca
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            isClickable = true
        }

        val parametrosCard = LinearLayout.LayoutParams(
            dp(170),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = dp(12)
            marginEnd = dp(12)
        }

        lista.addView(card, parametrosCard)

        // CÍRCULO AZUL

        val circulo = FrameLayout(this).apply {

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(azul)
            }

            clipToOutline = true
        }

        card.addView(
            circulo,
            LinearLayout.LayoutParams(
                dp(145),
                dp(145)
            )
        )

        // GIRAFINHA

        val girafa = ImageView(this).apply {
            setImageResource(R.drawable.girafapescoco)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Perfil de ${crianca.nome}"
        }

        val parametrosGirafa = FrameLayout.LayoutParams(
            dp(112),
            dp(127)
        ).apply {
            gravity = Gravity.CENTER
        }

        circulo.addView(girafa, parametrosGirafa)

        // NOME

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

        card.addView(nome, parametrosNome)

        // ANIMAÇÃO AO TOCAR

        card.setOnTouchListener { view, event ->

            when (event.action) {

                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(120)
                        .start()
                }

                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start()
                }
            }

            false
        }

        card.setOnClickListener {
            selecionarCrianca(crianca)
        }
    }

    // CARREGAR CRIANÇAS

    private fun carregarCriancas(): List<Crianca> {

        val preferencias = getSharedPreferences(
            "LudicaKids",
            Context.MODE_PRIVATE
        )

        val dados = preferencias.getString(
            "criancas",
            null
        )

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
                    objeto.optString("id", i.toString())
                )

                val nome = objeto.optString(
                    "nomeUsuario"
                ).ifBlank {
                    objeto.optString(
                        "nomeCompleto",
                        "Criança"
                    )
                }

                criancas.add(Crianca(id, nome))
            }

            criancas

        } catch (e: Exception) {
            emptyList()
        }
    }

    // SELECIONAR CRIANÇA

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

    // POP-UP INFANTIL PERSONALIZADO

    private fun mostrarPopup(nomeCrianca: String) {

        val dialog = Dialog(this)

        val azulEscuro = Color.rgb(45, 91, 150)
        val azulPastel = Color.rgb(235, 245, 255)
        val azulBotao = Color.rgb(92, 144, 209)

        // CARD PRINCIPAL

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(24),
                dp(12),
                dp(24),
                dp(20)
            )

            background = GradientDrawable().apply {
                cornerRadius = dp(30).toFloat()
                setColor(azulPastel)

                setStroke(
                    dp(2),
                    Color.rgb(190, 216, 241)
                )
            }

            elevation = dp(10).toFloat()
        }

        // GIRAFINHA SUPERIOR

        val girafa = ImageView(this).apply {
            setImageResource(R.drawable.girafapescoco)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Girafinha do Lúdica Kids"
        }

        card.addView(
            girafa,
            LinearLayout.LayoutParams(
                dp(85),
                dp(85)
            )
        )

        // TÍTULO

        val titulo = TextView(this).apply {
            text = "Oba! Você está pronto para brincar!"
            textSize = 20f

            setTextColor(azulEscuro)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER
        }

        val parametrosTitulo = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        }

        card.addView(titulo, parametrosTitulo)

        // SUBTÍTULO

        val subtitulo = TextView(this).apply {
            text = "Que bom ter você aqui!"
            textSize = 14f

            setTextColor(Color.rgb(105, 124, 147))

            gravity = Gravity.CENTER
        }

        val parametrosSubtitulo = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(6)
        }

        card.addView(subtitulo, parametrosSubtitulo)

        // NOME DA CRIANÇA

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
                dp(16),
                dp(8),
                dp(16),
                dp(8)
            )

            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.WHITE)
            }
        }

        val parametrosNome = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(12)
        }

        card.addView(nome, parametrosNome)

        // BOTÃO VAMOS BRINCAR

        val botaoBrincar = TextView(this).apply {
            text = "Vamos brincar!"
            textSize = 17f

            setTextColor(Color.WHITE)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER

            background = GradientDrawable().apply {
                cornerRadius = dp(24).toFloat()
                setColor(azulBotao)
            }

            elevation = dp(4).toFloat()

            isClickable = true
            isFocusable = true

            setOnClickListener {
                dialog.dismiss()

                // Aqui conectaremos a tela inicial
                // dos joguinhos futuramente.
            }
        }

        val parametrosBotao = LinearLayout.LayoutParams(
            dp(210),
            dp(46)
        ).apply {
            topMargin = dp(16)
        }

        card.addView(botaoBrincar, parametrosBotao)

        // CONFIGURAR DIALOG

        dialog.setContentView(card)

        dialog.window?.apply {
            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            setDimAmount(0.45f)
        }

        dialog.setCancelable(true)

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            val larguraTela =
                resources.displayMetrics.widthPixels

            val larguraPopup =
                minOf(dp(430), (larguraTela * 0.85f).toInt())

            setLayout(
                larguraPopup,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            setGravity(Gravity.CENTER)
            setDimAmount(0.45f)
        }

        // ANIMAÇÃO SUAVE DE ENTRADA

        card.scaleX = 0.88f
        card.scaleY = 0.88f
        card.alpha = 0f

        card.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(280)
            .start()

        // ANIMAÇÃO DA GIRAFINHA

        girafa.translationY = dp(12).toFloat()

        girafa.animate()
            .translationY(0f)
            .setDuration(450)
            .start()
    }
}