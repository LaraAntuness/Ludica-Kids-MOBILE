package com.example.mobilejogos

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.res.ColorStateList

class TelaResponsavelActivity : Activity() {

    // CORES DO LÚDICAKIDS

    private val azulMenu = Color.rgb(94, 145, 207)
    private val azulTitulo = Color.rgb(79, 137, 225)
    private val azulClaro = Color.rgb(221, 239, 251)

    private val fundo = Color.rgb(244, 244, 244)
    private val branco = Color.WHITE
    private val textoEscuro = Color.rgb(65, 65, 65)
    private val textoCinza = Color.rgb(100, 100, 100)

    private lateinit var areaConteudo: FrameLayout
    private lateinit var menuContainer: LinearLayout

    private var paginaAtual = "Jogos"

    private val botoesMenu = mutableMapOf<String, LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        montarEstrutura()
        abrirJogos()
    }

    // =====================================================
    // FUNÇÕES AUXILIARES
    // =====================================================

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private fun fundoArredondado(
        cor: Int,
        raio: Int = 18,
        corBorda: Int? = null
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raio).toFloat()

            if (corBorda != null) {
                setStroke(dp(1), corBorda)
            }
        }
    }

    private fun texto(
        conteudo: String,
        tamanho: Float = 14f,
        cor: Int = textoEscuro,
        negrito: Boolean = false
    ): TextView {

        return TextView(this).apply {
            text = conteudo
            textSize = tamanho
            setTextColor(cor)

            if (negrito) {
                setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun espacamento(altura: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                dp(altura)
            )
        }
    }

    // =====================================================
    // ESTRUTURA PRINCIPAL
    // =====================================================

    private fun montarEstrutura() {

        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(fundo)
        }

        val lateral = criarMenu()

        areaConteudo = FrameLayout(this).apply {
            setBackgroundColor(fundo)
        }

        raiz.addView(
            lateral,
            LinearLayout.LayoutParams(
                dp(175),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        raiz.addView(
            areaConteudo,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        setContentView(raiz)
    }

    // =====================================================
    // MENU LATERAL
    // =====================================================

    private fun criarMenu(): FrameLayout {

        val lateral = FrameLayout(this).apply {
            setBackgroundColor(azulMenu)
        }

        menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(12),
                dp(17),
                dp(12),
                dp(12)
            )
        }

        val voltar = texto(
            "‹     Voltar",
            14f,
            branco,
            true
        ).apply {

            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(13),
                0,
                0,
                0
            )

            background = fundoArredondado(
                Color.rgb(113, 160, 216),
                12
            )

            setOnClickListener {
                finish()
            }
        }

        menuContainer.addView(
            voltar,
            LinearLayout.LayoutParams(
                -1,
                dp(38)
            )
        )

        menuContainer.addView(espacamento(18))

        adicionarItemMenu("Perfil", "♙") {
            abrirPerfil()
        }

        adicionarItemMenu("Jogos", "▣") {
            abrirJogos()
        }

        adicionarItemMenu("Sobre nós", "ⓘ") {
            abrirSobre()
        }

        adicionarItemMenu("Configurações", "⚙") {
            abrirConfiguracoes()
        }

        adicionarItemMenu("FAQ", "?") {
            abrirFAQ()
        }

        lateral.addView(
            menuContainer,
            FrameLayout.LayoutParams(
                -1,
                -2,
                Gravity.TOP
            )
        )

        // ESPAÇO PARA A LOGO NA PARTE INFERIOR

        val espacoLogo = TextView(this).apply {

            text = "LOGO\nLÚDICAKIDS"

            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(branco)

            background = fundoArredondado(
                Color.rgb(113, 160, 216),
                12
            )
        }

        lateral.addView(
            espacoLogo,
            FrameLayout.LayoutParams(
                dp(125),
                dp(45),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = dp(14)
            }
        )

        return lateral
    }

    private fun adicionarItemMenu(
        nome: String,
        icone: String,
        acao: () -> Unit
    ) {

        val botao = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(12),
                0,
                dp(8),
                0
            )

            background = fundoArredondado(
                azulMenu,
                12
            )

            setOnClickListener {
                acao()
            }
        }

        val textoIcone = texto(
            icone,
            18f,
            branco
        ).apply {
            gravity = Gravity.CENTER
        }

        val nomeBotao = texto(
            nome,
            13f,
            branco
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        botao.addView(
            textoIcone,
            LinearLayout.LayoutParams(
                dp(28),
                -1
            )
        )

        botao.addView(nomeBotao)

        menuContainer.addView(
            botao,
            LinearLayout.LayoutParams(
                -1,
                dp(39)
            ).apply {
                bottomMargin = dp(7)
            }
        )

        botoesMenu[nome] = botao
    }

    private fun selecionarMenu(nome: String) {

        paginaAtual = nome

        botoesMenu.forEach { (titulo, botao) ->

            botao.background = fundoArredondado(
                if (titulo == nome)
                    Color.rgb(126, 175, 230)
                else
                    azulMenu,
                12
            )
        }
    }

    // =====================================================
    // PÁGINA COM ROLAGEM
    // =====================================================

    private fun criarPagina(
        tituloPagina: String
    ): LinearLayout {

        areaConteudo.removeAllViews()

        selecionarMenu(tituloPagina)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = true
            setBackgroundColor(fundo)

            clipToPadding = false
        }

        val coluna = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(27),
                dp(28),
                dp(27),
                dp(35)
            )

            clipChildren = false
            clipToPadding = false
        }

        scroll.addView(coluna)

        areaConteudo.addView(
            scroll,
            FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        return coluna
    }

    private fun tituloSecao(
        nome: String
    ): TextView {

        return texto(
            nome,
            19f,
            azulTitulo,
            true
        ).apply {

            setPadding(
                0,
                dp(8),
                0,
                dp(15)
            )
        }
    }

    // =====================================================
    // CARDS MELHORADOS
    // =====================================================

    private fun criarCard(
        titulo: String,
        descricao: String
    ): LinearLayout {

        val card = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(16),
                dp(18),
                dp(18)
            )

            background = fundoArredondado(
                branco,
                17
            )

            elevation = dp(4).toFloat()
        }

        card.addView(
            texto(
                titulo,
                15f,
                textoEscuro,
                true
            )
        )

        card.addView(espacamento(8))

        card.addView(
            texto(
                descricao,
                12.5f,
                textoCinza
            )
        )

        return card
    }

    private fun adicionarCard(
        coluna: LinearLayout,
        titulo: String,
        descricao: String
    ) {

        coluna.addView(
            criarCard(titulo, descricao),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin = dp(9)
                bottomMargin = dp(12)

                marginStart = dp(5)
                marginEnd = dp(5)
            }
        )
    }

    // =====================================================
    // ESPAÇO PARA IMAGENS
    // =====================================================

    private fun criarImagem(
        nome: String,
        altura: Int = 130
    ): TextView {

        return TextView(this).apply {

            text = "ESPAÇO PARA IMAGEM\n$nome"

            textSize = 11f

            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(117, 153, 177)
            )

            background = GradientDrawable().apply {

                setColor(
                    Color.rgb(229, 241, 250)
                )

                cornerRadius = dp(15).toFloat()

                setStroke(
                    dp(1),
                    Color.rgb(195, 219, 236)
                )
            }

            minHeight = dp(altura)
        }
    }

    // =====================================================
    // CARD HORIZONTAL COM IMAGEM
    // =====================================================

    private fun cardHorizontal(
        titulo: String,
        descricao: String,
        nomeImagem: String,
        imagemEsquerda: Boolean = true
    ): LinearLayout {

        val corCard = when (titulo) {
            "Identificando Emoções" -> Color.rgb(205, 232, 165)
            "Cinco Sentidos Mágicos" -> Color.rgb(255, 222, 133)
            "Respirando com o Dragão" -> Color.rgb(247, 183, 190)
            else -> Color.WHITE
        }

        val card = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(13),
                dp(12),
                dp(13),
                dp(12)
            )

            background = fundoArredondado(
                corCard,
                17
            )

            elevation = dp(4).toFloat()
        }

        val imagem = criarImagem(
            nomeImagem,
            125
        )

        val informacoes = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(15),
                dp(8),
                dp(10),
                dp(8)
            )
        }

        informacoes.addView(
            texto(
                titulo,
                16f,
                textoEscuro,
                true
            )
        )

        informacoes.addView(espacamento(9))

        informacoes.addView(
            texto(
                descricao,
                12f,
                textoCinza
            )
        )

        val paramsImagem = LinearLayout.LayoutParams(
            0,
            dp(125),
            0.43f
        )

        val paramsTexto = LinearLayout.LayoutParams(
            0,
            -2,
            0.57f
        )

        if (imagemEsquerda) {

            card.addView(
                imagem,
                paramsImagem
            )

            card.addView(
                informacoes,
                paramsTexto
            )

        } else {

            card.addView(
                informacoes,
                paramsTexto
            )

            card.addView(
                imagem,
                paramsImagem
            )
        }

        return card
    }

    private fun adicionarCardHorizontal(
        coluna: LinearLayout,
        titulo: String,
        descricao: String,
        imagem: String,
        imagemEsquerda: Boolean = true
    ) {

        coluna.addView(
            cardHorizontal(
                titulo,
                descricao,
                imagem,
                imagemEsquerda
            ),
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin = dp(12)
                bottomMargin = dp(12)

                marginStart = dp(5)
                marginEnd = dp(5)
            }
        )
    }

    // =====================================================
    // PERFIL
    // =====================================================

    private fun abrirPerfil() {

        val pagina = criarPagina("Perfil")

        pagina.addView(
            tituloSecao("Meu Perfil")
        )

        adicionarCardHorizontal(
            pagina,
            "Perfil do responsável",
            "Gerencie suas informações pessoais e acompanhe as atividades das crianças cadastradas.",
            "Foto do responsável"
        )

        pagina.addView(espacamento(15))

        adicionarCard(
            pagina,
            "Informações pessoais",
            "Aqui estarão os dados do responsável cadastrado."
        )

        adicionarCard(
            pagina,
            "Crianças cadastradas",
            "Visualize e gerencie os perfis das crianças vinculadas à sua conta."
        )
    }

    // =====================================================
    // JOGOS
    // =====================================================

    private fun abrirJogos() {

        val pagina = criarPagina("Jogos")

        pagina.addView(
            tituloSecao("Conheça nossos jogos")
        )

        adicionarCardHorizontal(
            pagina,
            "Identificando Emoções",
            "As crianças associam as expressões e sentimentos da girafa a emoções como raiva, tristeza, angústia e tédio, aprendendo a reconhecê-las e lidar com elas.",
            "Identificando Emoções",
            true
        )

        adicionarCardHorizontal(
            pagina,
            "Cinco Sentidos Mágicos",
            "A criança explora os cinco sentidos: visão, audição, olfato, paladar e tato por meio de atividades lúdicas.",
            "Cinco Sentidos Mágicos",
            true
        )

        adicionarCardHorizontal(
            pagina,
            "Respirando com o Dragão",
            "O dragão ajuda a criança a aprender técnicas de respiração e a desenvolver o autocontrole.",
            "Respirando com o Dragão",
            false
        )
    }

    // =====================================================
    // SOBRE NÓS
    // =====================================================

    private fun abrirSobre() {

        val pagina = criarPagina("Sobre nós")

        val tituloMissao = texto(
            "Nossa Missão",
            20f,
            textoEscuro,
            true
        ).apply {
            gravity = Gravity.CENTER
        }

        pagina.addView(tituloMissao)

        pagina.addView(espacamento(7))

        val frase = texto(
            "Brincar para acalmar, aprender para crescer!",
            14f,
            azulTitulo,
            true
        ).apply {
            gravity = Gravity.CENTER
        }

        pagina.addView(frase)

        adicionarCard(
            pagina,
            "Nossa missão",
            "Criamos o LúdicaKids para levar mais bem-estar e qualidade de vida às crianças que convivem, ou podem vir a conviver, com a ansiedade infantil. Nossos jogos terapêuticos ajudam a criança a identificar suas emoções e desenvolver o autocontrole em um ambiente digital seguro e sem anúncios."
        )

        pagina.addView(espacamento(15))

        pagina.addView(
            criarImagem(
                "Ilustração da missão",
                145
            )
        )

        pagina.addView(espacamento(27))

        pagina.addView(
            tituloSecao("O que nos guia")
        )

        // QUATRO CARDS LADO A LADO

        val linhaValores = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
        }

        val valores = listOf(
            "Ética e cuidado" to
                    "Cuidamos do bem-estar da criança.",

            "Segurança" to
                    "Sem anúncios e com proteção de dados.",

            "Aprendizado" to
                    "Jogos que ensinam sobre emoções.",

            "Inovação" to
                    "Soluções criativas baseadas em evidências."
        )

        valores.forEach { (titulo, descricao) ->

            linhaValores.addView(
                criarCard(titulo, descricao),
                LinearLayout.LayoutParams(
                    0,
                    dp(145),
                    1f
                ).apply {

                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            )
        }

        pagina.addView(linhaValores)

        pagina.addView(espacamento(30))

        pagina.addView(
            tituloSecao("Conheça nosso mascote")
        )

        adicionarCardHorizontal(
            pagina,
            "Bubba, a girafinha",
            "Para nossa mascote escolhemos a girafa por ser um animal calmo e amigável. A Bubba acompanha a criança em todos os jogos.",
            "Bubba",
            true
        )

        pagina.addView(espacamento(25))

        pagina.addView(
            tituloSecao("Feito com cuidado e ciência")
        )

        adicionarCard(
            pagina,
            "Validação profissional",
            "Todos os jogos são analisados por um profissional de psicologia antes de chegarem às crianças."
        )

        adicionarCard(
            pagina,
            "Base científica",
            "Técnicas de respiração, atenção aos sentidos e reconhecimento de emoções apoiadas em estudos sobre ansiedade infantil."
        )

        pagina.addView(espacamento(25))

        pagina.addView(
            tituloSecao("Conheça os colaboradores desse projeto")
        )

        val colaboradores = listOf(
            "Giovanna Montoro Barbosa",
            "Lara Antunes Soares Brito",
            "Maria Aparecida Bezerra de Noronha"
        )

        val linhaEquipe = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
        }

        colaboradores.forEach { nome ->

            val card = LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER

                setPadding(
                    dp(10),
                    dp(12),
                    dp(10),
                    dp(12)
                )

                background = fundoArredondado(
                    branco,
                    17
                )

                elevation = dp(4).toFloat()
            }

            card.addView(
                criarImagem(
                    nome,
                    85
                ),
                LinearLayout.LayoutParams(
                    -1,
                    dp(85)
                )
            )

            card.addView(espacamento(10))

            card.addView(
                texto(
                    nome,
                    12f,
                    textoEscuro,
                    true
                ).apply {
                    gravity = Gravity.CENTER
                }
            )

            linhaEquipe.addView(
                card,
                LinearLayout.LayoutParams(
                    0,
                    dp(155),
                    1f
                ).apply {

                    marginStart = dp(7)
                    marginEnd = dp(7)
                }
            )
        }

        pagina.addView(linhaEquipe)

        pagina.addView(espacamento(30))

        pagina.addView(
            tituloSecao("Privacidade e contato")
        )

        adicionarCard(
            pagina,
            "Privacidade",
            "De acordo com a LGPD e o ECA, só maiores de 18 anos criam contas. A proteção das informações é uma prioridade do projeto."
        )

        adicionarCard(
            pagina,
            "Fale conosco",
            "Dúvidas, ideias ou feedback? Entre em contato com a equipe LúdicaKids."
        )

        adicionarCard(
            pagina,
            "Importante",
            "O LúdicaKids é um apoio e não substitui o acompanhamento de um profissional de saúde."
        )
    }

    // =====================================================
    // CONFIGURAÇÕES
    // =====================================================

    private fun abrirConfiguracoes() {

        val pagina = criarPagina("Configurações")

        pagina.addView(
            tituloSecao("Configurações")
        )

        val preferencias = getSharedPreferences(
            "configuracoes_ludicakids",
            MODE_PRIVATE
        )

        val card = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(12),
                dp(18),
                dp(12)
            )

            background = fundoArredondado(
                branco,
                17
            )

            elevation = dp(4).toFloat()
        }

        fun opcao(
            nome: String,
            chave: String
        ) {

            val linha = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val nomeOpcao = texto(
                nome,
                14f,
                textoEscuro
            )

            val controle = Switch(this).apply {

                isChecked = preferencias.getBoolean(
                    chave,
                    true
                )

                thumbTintList = ColorStateList.valueOf(
                    azulMenu
                )

                setOnCheckedChangeListener { _, marcado ->

                    preferencias.edit()
                        .putBoolean(chave, marcado)
                        .apply()
                }
            }

            linha.addView(
                nomeOpcao,
                LinearLayout.LayoutParams(
                    0,
                    dp(43),
                    1f
                )
            )

            linha.addView(controle)

            card.addView(linha)
        }

        opcao("Música", "musica")

        val divisoria = View(this).apply {
            setBackgroundColor(
                Color.rgb(233, 233, 233)
            )
        }

        card.addView(
            divisoria,
            LinearLayout.LayoutParams(
                -1,
                dp(1)
            )
        )

        opcao("Efeitos Sonoros", "efeitos")

        pagina.addView(
            card,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin = dp(15)
                marginStart = dp(5)
                marginEnd = dp(5)
            }
        )

        val notificacoes = texto(
            "Permissão de notificações",
            13f,
            azulTitulo
        ).apply {

            paintFlags = paintFlags or
                    android.graphics.Paint.UNDERLINE_TEXT_FLAG

            setPadding(
                dp(15),
                dp(18),
                0,
                dp(10)
            )

            setOnClickListener {

                AlertDialog.Builder(this@TelaResponsavelActivity)
                    .setTitle("Notificações")
                    .setMessage(
                        "Você pode gerenciar as notificações nas configurações do dispositivo."
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        pagina.addView(notificacoes)
    }

    // =====================================================
    // FAQ
    // =====================================================

    private fun abrirFAQ() {

        val pagina = criarPagina("FAQ")

        pagina.addView(
            tituloSecao("FAQ")
        )

        adicionarGrupoFAQ(
            pagina,
            "Sobre o LúdicaKids",
            listOf(
                "O que é o LúdicaKids?",
                "Para qual idade os jogos são indicados?",
                "Quem é a Bubba?",
                "Existem acompanhamentos de especialistas no projeto?",
                "Além das crianças, o site também orienta os pais sobre a ansiedade infantil?"
            )
        )

        adicionarGrupoFAQ(
            pagina,
            "Jogos",
            listOf(
                "Quais jogos estão disponíveis?",
                "Os jogos têm violência, anúncios ou compras?",
                "Antes de jogar a criança recebe alguma explicação sobre o jogo?",
                "Serão lançados novos jogos no futuro?"
            )
        )

        adicionarGrupoFAQ(
            pagina,
            "Cadastro e conta",
            listOf(
                "Quem pode criar a conta?",
                "Como faço o cadastro?",
                "Posso cadastrar mais de uma criança?",
                "Esqueci minha senha. E agora?",
                "Posso editar meus dados ou excluir minha conta?"
            )
        )

        adicionarGrupoFAQ(
            pagina,
            "Segurança e privacidade",
            listOf(
                "Quais dados da criança a plataforma coleta?",
                "Os dados são compartilhados com terceiros?",
                "A criança pode usar o site sozinha, sem a supervisão do responsável?",
                "O site segue a LGPD?"
            )
        )

        adicionarGrupoFAQ(
            pagina,
            "Uso e acessibilidade",
            listOf(
                "Por que o site usa cores e sons suaves?",
                "O aplicativo pensa em acessibilidade e inclusão para todas as crianças?",
                "A interface é fácil para uma criança pequena usar?"
            )
        )

        pagina.addView(espacamento(20))

        val aviso = texto(
            "Não encontrou sua dúvida? Envie uma sugestão ou fale com a equipe LúdicaKids pelo nosso canal de contato.",
            13f,
            textoEscuro
        ).apply {

            gravity = Gravity.CENTER

            setPadding(
                dp(22),
                dp(20),
                dp(22),
                dp(20)
            )

            background = fundoArredondado(
                azulClaro,
                14
            )
        }

        pagina.addView(aviso)
    }

    private fun adicionarGrupoFAQ(
        pagina: LinearLayout,
        titulo: String,
        perguntas: List<String>
    ) {

        val subtitulo = texto(
            "▏ $titulo",
            15f,
            textoEscuro,
            true
        )

        pagina.addView(
            subtitulo,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin = dp(17)
                bottomMargin = dp(13)
            }
        )

        perguntas.forEach { pergunta ->

            val container = LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                setPadding(
                    dp(15),
                    dp(8),
                    dp(10),
                    dp(8)
                )

                background = fundoArredondado(
                    branco,
                    13
                )
            }

            val textoPergunta = texto(
                pergunta,
                12.5f,
                textoEscuro
            )

            val botaoMais = texto(
                "+",
                18f,
                azulMenu,
                true
            ).apply {

                gravity = Gravity.CENTER

                background = fundoArredondado(
                    azulClaro,
                    30
                )
            }

            container.addView(
                textoPergunta,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )

            container.addView(
                botaoMais,
                LinearLayout.LayoutParams(
                    dp(26),
                    dp(26)
                )
            )

            container.setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle(pergunta)
                    .setMessage(
                        "A resposta desta pergunta será adicionada aqui."
                    )
                    .setPositiveButton("Entendi", null)
                    .show()
            }

            pagina.addView(
                container,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {

                    bottomMargin = dp(8)
                    marginStart = dp(5)
                    marginEnd = dp(5)
                }
            )
        }
    }
}