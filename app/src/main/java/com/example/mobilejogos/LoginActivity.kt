
package com.example.mobilejogos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class LoginActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var formulario: LinearLayout
    private lateinit var abaEntrar: TextView
    private lateinit var abaCadastro: TextView
    private lateinit var rolagem: ScrollView
    private lateinit var card: LinearLayout

    private val azulCeu = Color.rgb(205, 228, 242)
    private val verdeEscuro = Color.rgb(56, 75, 40)
    private val verdeClaro = Color.rgb(237, 245, 223)
    private val amarelo = Color.rgb(255, 186, 19)
    private val branco = Color.WHITE

    private val coresGlitter = listOf(
        Color.rgb(255, 211, 85),
        Color.rgb(255, 235, 165),
        Color.rgb(130, 187, 237),
        Color.rgb(183, 222, 255),
        Color.WHITE,
        Color.rgb(164, 211, 144)
    )

    private val sonsAtivos = mutableListOf<MediaPlayer>()

    private var modoCadastro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContentView(R.layout.activity_login)

        root = findViewById(R.id.loginRoot)

        montarTela()
    }

    // ==========================================
    // MEDIDAS
    // ==========================================

    private fun dp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    private fun fundo(
        cor: Int,
        raio: Int,
        borda: Int? = null
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(cor)

            cornerRadius = dp(raio).toFloat()

            if (borda != null) {
                setStroke(dp(1), borda)
            }
        }
    }

    private fun parametros(
        largura: Int = LinearLayout.LayoutParams.MATCH_PARENT,
        altura: Int = LinearLayout.LayoutParams.WRAP_CONTENT,
        margemSuperior: Int = 0
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            largura,
            altura
        ).apply {
            topMargin = dp(margemSuperior)
        }
    }

    // ==========================================
    // SONS
    // ==========================================

    private fun tocarSom(
        recurso: Int,
        volume: Float = 0.35f
    ) {

        val player = MediaPlayer.create(
            this,
            recurso
        ) ?: return

        sonsAtivos.add(player)

        player.setVolume(volume, volume)

        player.setOnCompletionListener {

            sonsAtivos.remove(it)
            it.release()
        }

        player.start()
    }

    // SOM DE CLIQUE DELICADO
    private fun somClique() {

        tocarSom(
            R.raw.clique_suave,
            0.45f
        )
    }

    // SOM DE GLITTER
    private fun somGlitter() {

        tocarSom(
            R.raw.brilho_suave,
            0.35f
        )
    }

    // ==========================================
    // MONTAR CENÁRIO
    // ==========================================

    private fun montarTela() {

        root.removeAllViews()

        root.setBackgroundColor(azulCeu)

        root.clipChildren = false
        root.clipToPadding = false

        // GRAMA ORIGINAL
        val grama = ImageView(this).apply {

            setImageResource(
                R.drawable.gramainicio
            )

            scaleType = ImageView.ScaleType.FIT_XY
        }

        root.addView(
            grama,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // NUVENS ORIGINAIS
        adicionarNuvem(0.17f, 0.08f, 0.12f)
        adicionarNuvem(0.73f, 0.11f, 0.14f)
        adicionarNuvem(0.05f, 0.04f, 0.07f)

        // ROLAGEM
        rolagem = ScrollView(this).apply {

            isVerticalScrollBarEnabled = false
            isFillViewport = true

            clipChildren = false
            clipToPadding = false
        }

        root.addView(
            rolagem,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val conteudo = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            clipChildren = false
            clipToPadding = false

            setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(20)
            )
        }

        rolagem.addView(
            conteudo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val larguraCard = minOf(
            dp(410),
            (resources.displayMetrics.widthPixels * 0.78f).toInt()
        )

        // ======================================
        // GIRAFINHA ORIGINAL
        // ======================================

        val girafa = ImageView(this).apply {

            setImageResource(
                R.drawable.girafamao
            )

            scaleType = ImageView.ScaleType.FIT_CENTER

            contentDescription = "Girafinha Lúdica Kids"
        }

        conteudo.addView(
            girafa,
            parametros(
                dp(125),
                dp(120)
            )
        )

        // ======================================
        // CARD BRANCO
        // ======================================

        card = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(22),
                dp(18),
                dp(22),
                dp(20)
            )

            background = fundo(
                branco,
                26,
                Color.rgb(215, 226, 198)
            )

            elevation = dp(5).toFloat()
        }

        conteudo.addView(
            card,
            parametros(
                larguraCard,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = -dp(8)
            }
        )

        // ======================================
        // ABAS ENTRAR E CRIAR CONTA
        // ======================================

        val abas = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            background = fundo(
                verdeClaro,
                25
            )

            setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
            )
        }

        card.addView(
            abas,
            parametros(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        abaEntrar = criarAba("Entrar")
        abaCadastro = criarAba("Criar conta")

        abas.addView(
            abaEntrar,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        abas.addView(
            abaCadastro,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        formulario = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL
        }

        card.addView(
            formulario,
            parametros(margemSuperior = 12)
        )

        // SOM AO TROCAR PARA ENTRAR
        abaEntrar.setOnClickListener {

            somClique()

            trocarFormulario(false)
        }

        // SOM AO TROCAR PARA CADASTRO
        abaCadastro.setOnClickListener {

            somClique()

            trocarFormulario(true)
        }

        trocarFormulario(false)
    }

    // ==========================================
    // NUVENS
    // ==========================================

    private fun adicionarNuvem(
        posX: Float,
        posY: Float,
        largura: Float
    ) {

        val nuvem = ImageView(this).apply {

            setImageResource(
                R.drawable.nuveminicio
            )

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        root.post {

            if (isFinishing || isDestroyed) {
                return@post
            }

            root.addView(
                nuvem,
                1,
                FrameLayout.LayoutParams(
                    (root.width * largura).toInt(),
                    (root.height * 0.22f).toInt()
                ).apply {

                    leftMargin =
                        (root.width * posX).toInt()

                    topMargin =
                        (root.height * posY).toInt()
                }
            )
        }
    }

    // ==========================================
    // ABAS
    // ==========================================

    private fun criarAba(
        nome: String
    ): TextView {

        return TextView(this).apply {

            text = nome
            textSize = 13f

            gravity = Gravity.CENTER

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(verdeEscuro)

            isClickable = true
            isFocusable = true
        }
    }

    private fun trocarFormulario(
        cadastro: Boolean
    ) {

        modoCadastro = cadastro

        abaEntrar.background = fundo(
            if (!cadastro) amarelo else verdeClaro,
            23
        )

        abaCadastro.background = fundo(
            if (cadastro) amarelo else verdeClaro,
            23
        )

        formulario.removeAllViews()

        if (cadastro) {
            montarCadastro()
        } else {
            montarLogin()
        }

        rolagem.post {
            rolagem.smoothScrollTo(0, 0)
        }
    }

    // ==========================================
    // TÍTULOS
    // ==========================================

    private fun titulo(
        texto: String,
        tamanho: Float = 22f
    ) {

        val titulo = TextView(this).apply {

            text = texto
            textSize = tamanho

            setTextColor(verdeEscuro)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER
        }

        formulario.addView(
            titulo,
            parametros(margemSuperior = 5)
        )
    }

    private fun descricao(
        texto: String
    ) {

        val descricao = TextView(this).apply {

            text = texto
            textSize = 12f

            setTextColor(
                Color.rgb(105, 116, 94)
            )

            gravity = Gravity.CENTER
        }

        formulario.addView(
            descricao,
            parametros(margemSuperior = 4)
        )
    }

    // ==========================================
    // CAMPOS
    // ==========================================

    private fun criarCampo(
        nome: String,
        dica: String,
        tipo: Int = InputType.TYPE_CLASS_TEXT,
        senha: Boolean = false
    ): EditText {

        val rotulo = TextView(this).apply {

            text = nome
            textSize = 12f

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(verdeEscuro)
        }

        formulario.addView(
            rotulo,
            parametros(margemSuperior = 10)
        )

        val linha = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER_VERTICAL

            background = fundo(
                branco,
                14,
                Color.rgb(202, 221, 187)
            )
        }

        formulario.addView(
            linha,
            parametros(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(43),
                4
            )
        )

        val campo = EditText(this).apply {

            hint = dica
            textSize = 13f

            setTextColor(verdeEscuro)

            setHintTextColor(
                Color.rgb(149, 160, 143)
            )

            inputType = tipo

            background = null

            isSingleLine = true

            setPadding(
                dp(12),
                0,
                dp(8),
                0
            )
        }

        linha.addView(
            campo,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        // MOSTRAR / OCULTAR SENHA
        if (senha) {

            val mostrar = TextView(this).apply {

                text = "Mostrar"
                textSize = 11f

                setTextColor(verdeEscuro)

                gravity = Gravity.CENTER

                setPadding(
                    dp(7),
                    0,
                    dp(10),
                    0
                )

                isClickable = true
            }

            mostrar.setOnClickListener {

                // SOM SUAVE
                somClique()

                val posicao = campo.selectionStart

                val oculto =
                    campo.inputType and
                            InputType.TYPE_TEXT_VARIATION_PASSWORD != 0

                campo.inputType =
                    if (oculto) {

                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                    } else {

                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                mostrar.text =
                    if (oculto) "Ocultar" else "Mostrar"

                campo.setSelection(
                    posicao.coerceIn(
                        0,
                        campo.text.length
                    )
                )
            }

            linha.addView(mostrar)
        }

        return campo
    }

    // ==========================================
    // BOTÕES COM SOM E ANIMAÇÃO
    // ==========================================

    private fun criarBotao(
        texto: String,
        acao: () -> Unit
    ) {

        val botao = TextView(this).apply {

            text = texto
            textSize = 15f

            setTextColor(verdeEscuro)

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            background = fundo(
                amarelo,
                22
            )

            elevation = dp(2).toFloat()

            isClickable = true
            isFocusable = true

            setOnClickListener {

                if (!isEnabled) {
                    return@setOnClickListener
                }

                isEnabled = false

                // SOM SUAVE EM TODOS OS BOTÕES
                somClique()

                animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(90)
                    .withEndAction {

                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(90)
                            .withEndAction {

                                isEnabled = true
                                acao()
                            }
                            .start()
                    }
                    .start()
            }
        }

        formulario.addView(
            botao,
            parametros(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44),
                16
            )
        )
    }

    // ==========================================
    // LOGIN
    // ==========================================

    private fun montarLogin() {

        titulo("Oi, que bom te ver!")

        descricao("Entre para continuar...")

        val email = criarCampo(
            "E-mail",
            "voce@email.com",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        val senha = criarCampo(
            "Senha",
            "Sua senha",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
            senha = true
        )

        // RECUPERAÇÃO DE SENHA
        val recuperar = TextView(this).apply {

            text = "Esqueci a senha"

            textSize = 12f

            paintFlags =
                paintFlags or
                        android.graphics.Paint.UNDERLINE_TEXT_FLAG

            setTextColor(
                Color.rgb(82, 122, 66)
            )

            gravity = Gravity.END

            setOnClickListener {

                somClique()

                LudicaAvisos.mostrarErro(
                    this@LoginActivity,
                    "Recuperação de senha ainda não disponível."
                )
            }
        }

        formulario.addView(
            recuperar,
            parametros(margemSuperior = 10)
        )

        criarBotao("Entrar") {

            val emailDigitado =
                email.text.toString().trim()

            val senhaDigitada =
                senha.text.toString()

            if (
                emailDigitado.isBlank() ||
                senhaDigitada.isBlank()
            ) {

                LudicaAvisos.mostrarErro(
                    this,
                    "Preencha seu e-mail e sua senha."
                )

            } else {

                // Integraremos ao Railway.
                LudicaAvisos.mostrarErro(
                    this,
                    "Login ainda não conectado ao servidor."
                )
            }
        }
    }

    // ==========================================
    // CADASTRO
    // ==========================================

    private fun montarCadastro() {

        titulo("Vamos criar sua conta!")

        descricao(
            "Primeiro, os dados do responsável."
        )

        val nome = criarCampo(
            "Nome completo",
            "Nome do responsável"
        )

        val cpf = criarCampo(
            "CPF",
            "000.000.000-00",
            InputType.TYPE_CLASS_NUMBER
        )

        val rg = criarCampo(
            "RG",
            "RG do responsável"
        )

        val email = criarCampo(
            "E-mail",
            "voce@email.com",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        val celular = criarCampo(
            "Celular",
            "Telefone com DDD",
            InputType.TYPE_CLASS_PHONE
        )

        val senhaResponsavel = criarCampo(
            "Senha do responsável",
            "Crie uma senha",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
            senha = true
        )

        val senhaCrianca = criarCampo(
            "Senha da criança",
            "Senha para as crianças",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
            senha = true
        )

        val tituloCrianca = TextView(this).apply {

            text = "Dados da criança"

            textSize = 17f

            setTextColor(verdeEscuro)

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER
        }

        formulario.addView(
            tituloCrianca,
            parametros(margemSuperior = 18)
        )

        val camposCriancas =
            mutableListOf<List<EditText>>()

        fun adicionarCrianca() {

            val numero = camposCriancas.size + 1

            descricao("Criança $numero")

            val nomeCrianca = criarCampo(
                "Nome completo da criança",
                "Nome completo"
            )

            val cpfCrianca = criarCampo(
                "CPF da criança",
                "000.000.000-00",
                InputType.TYPE_CLASS_NUMBER
            )

            val usuarioCrianca = criarCampo(
                "Nome de usuário",
                "Apelido da criança"
            )

            val nascimento = criarCampo(
                "Data de nascimento",
                "DD/MM/AAAA"
            )

            camposCriancas.add(
                listOf(
                    nomeCrianca,
                    cpfCrianca,
                    usuarioCrianca,
                    nascimento
                )
            )
        }

        adicionarCrianca()

        // BOTÃO ADICIONAR CRIANÇA
        criarBotao("+ Adicionar criança") {

            adicionarCrianca()
        }

        // BOTÃO CADASTRAR
        criarBotao("Cadastrar") {

            val camposResponsavel = listOf(
                nome,
                cpf,
                rg,
                email,
                celular,
                senhaResponsavel,
                senhaCrianca
            )

            val todosCampos =
                camposResponsavel +
                        camposCriancas.flatten()

            val incompleto = todosCampos.any {
                it.text.toString().trim().isBlank()
            }

            if (incompleto) {

                LudicaAvisos.mostrarErro(
                    this,
                    "Preencha todos os campos para continuar."
                )

            } else {

                // Quando integrarmos POST /responsaveis,
                // executaremos celebrarCadastroSucesso()
                // apenas após a confirmação da API.

                LudicaAvisos.mostrarErro(
                    this,
                    "Dados preenchidos. Falta conectar o cadastro ao servidor."
                )
            }
        }
    }

    // ==========================================
    // CELEBRAÇÃO APÓS CADASTRO REAL
    // ==========================================

    private fun celebrarCadastroSucesso() {

        // SOM DE GLITTER
        somGlitter()

        // ESTRELINHAS AO REDOR DO CARD
        animarGlitter()

        // ANIMAÇÃO DO CARD
        card.animate()
            .scaleX(1.035f)
            .scaleY(1.035f)
            .setDuration(180)
            .withEndAction {

                card.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .start()
            }
            .start()

        LudicaAvisos.mostrarSucesso(
            this,
            "Cadastro realizado com sucesso!"
        )
    }

    // ==========================================
    // GLITTER ANIMADO
    // ==========================================

    private fun animarGlitter() {

        val posicaoCard = IntArray(2)
        val posicaoRoot = IntArray(2)

        card.getLocationOnScreen(posicaoCard)
        root.getLocationOnScreen(posicaoRoot)

        val centroX =
            posicaoCard[0] - posicaoRoot[0] +
                    card.width / 2f

        val centroY =
            posicaoCard[1] - posicaoRoot[1] +
                    card.height / 2f

        repeat(32) { indice ->

            val estrela = TextView(this).apply {

                text = if (indice % 3 == 0) "✦" else "★"

                textSize =
                    Random.nextInt(12, 23).toFloat()

                setTextColor(
                    coresGlitter.random()
                )

                gravity = Gravity.CENTER

                elevation = dp(12).toFloat()
            }

            val tamanho = dp(34)

            root.addView(
                estrela,
                FrameLayout.LayoutParams(
                    tamanho,
                    tamanho
                ).apply {

                    leftMargin =
                        (centroX - tamanho / 2f).toInt()

                    topMargin =
                        (centroY - tamanho / 2f).toInt()
                }
            )

            val angulo =
                2.0 * Math.PI * indice / 32.0 +
                        Random.nextDouble(-0.2, 0.2)

            val distancia =
                dp(Random.nextInt(80, 210)).toDouble()

            estrela.alpha = 1f
            estrela.scaleX = 0.2f
            estrela.scaleY = 0.2f

            estrela.animate()
                .translationX(
                    (cos(angulo) * distancia).toFloat()
                )
                .translationY(
                    (sin(angulo) * distancia).toFloat()
                )
                .rotation(
                    Random.nextFloat() * 360f
                )
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(0f)
                .setDuration(
                    Random.nextLong(700, 1200)
                )
                .withEndAction {
                    root.removeView(estrela)
                }
                .start()
        }
    }

    // ==========================================
    // LIBERAR ÁUDIOS
    // ==========================================

    override fun onDestroy() {

        sonsAtivos.toList().forEach { player ->

            try {
                player.stop()
            } catch (_: Exception) {
            }

            try {
                player.release()
            } catch (_: Exception) {
            }
        }

        sonsAtivos.clear()

        super.onDestroy()
    }
}
