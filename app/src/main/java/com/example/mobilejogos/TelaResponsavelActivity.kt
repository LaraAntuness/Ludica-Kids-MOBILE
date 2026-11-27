package com.example.mobilejogos

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.*

class TelaResponsavelActivity : Activity() {

    // CORES

    private val azulMenu = Color.parseColor("#6498D5")
    private val azulTitulo = Color.parseColor("#4679B8")
    private val azulClaro = Color.parseColor("#E5F1FC")

    private val fundo = Color.parseColor("#F6F8FC")
    private val branco = Color.WHITE
    private val textoEscuro = Color.parseColor("#354458")
    private val textoCinza = Color.parseColor("#697586")

    private val rosa = Color.parseColor("#FBE1E7")
    private val amarelo = Color.parseColor("#FFF0C9")
    private val verde = Color.parseColor("#E5F3CE")

    private lateinit var areaConteudo: FrameLayout
    private lateinit var menuContainer: LinearLayout

    private val botoesMenu =
        mutableMapOf<String, LinearLayout>()

    private var paginaAtual = "Jogos"

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

    // ==========================================
    // FUNÇÕES AUXILIARES
    // ==========================================

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private fun arredondado(
        cor: Int,
        raio: Int = 20,
        borda: Int? = null
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raio).toFloat()

            borda?.let {
                setStroke(dp(1), it)
            }
        }
    }

    private fun degradê(
        primeira: Int,
        segunda: Int,
        raio: Int = 20
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(primeira, segunda)
        ).apply {
            cornerRadius = dp(raio).toFloat()
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

            typeface = Typeface.create(
                "sans-serif",
                if (negrito)
                    Typeface.BOLD
                else
                    Typeface.NORMAL
            )

            setLineSpacing(dp(3).toFloat(), 1f)
        }
    }

    private fun espaco(altura: Int): View {

        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                dp(altura)
            )
        }
    }

    private fun coluna(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
        }
    }

    private fun linha(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            clipChildren = false
            clipToPadding = false
        }
    }

    private fun adicionarBloco(
        destino: LinearLayout,
        bloco: View,
        margem: Int = 12
    ) {

        destino.addView(
            bloco,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(margem)
                marginStart = dp(3)
                marginEnd = dp(3)
            }
        )
    }

    // ==========================================
    // IMAGENS AUTOMÁTICAS
    // ==========================================

    private fun imagem(
        nome: String,
        altura: Int,
        corFundo: Int = azulClaro,
        circular: Boolean = false
    ): FrameLayout {

        val container = FrameLayout(this).apply {

            background = arredondado(
                corFundo,
                if (circular) altura else 18
            )

            minimumHeight = dp(altura)
            clipToOutline = true
        }

        val idImagem = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            val figura = ImageView(this).apply {

                setImageResource(idImagem)

                scaleType = ImageView.ScaleType.FIT_CENTER

                val margem = if (circular) 7 else 3

                setPadding(
                    dp(margem),
                    dp(margem),
                    dp(margem),
                    dp(margem)
                )
            }

            container.addView(
                figura,
                FrameLayout.LayoutParams(-1, -1)
            )

        } else if (!circular) {

            val placeholder = texto(
                nome.replace("_", " "),
                11f,
                azulTitulo
            ).apply {
                gravity = Gravity.CENTER
            }

            container.addView(
                placeholder,
                FrameLayout.LayoutParams(-1, -1)
            )
        }

        return container
    }

    // ==========================================
    // ESTRUTURA PRINCIPAL
    // ==========================================

    private fun montarEstrutura() {

        val raiz = linha().apply {
            setBackgroundColor(fundo)
        }

        raiz.addView(
            criarMenu(),
            LinearLayout.LayoutParams(
                dp(166),
                -1
            )
        )

        areaConteudo = FrameLayout(this).apply {
            setBackgroundColor(fundo)
        }

        raiz.addView(
            areaConteudo,
            LinearLayout.LayoutParams(
                0,
                -1,
                1f
            )
        )

        setContentView(raiz)
    }

    // ==========================================
    // MENU LATERAL
    // ==========================================

    private fun criarMenu(): LinearLayout {

        val lateral = coluna().apply {

            background = degradê(
                Color.parseColor("#78AFE7"),
                Color.parseColor("#558AC9"),
                0
            )

            setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
            )
        }

        val voltar = texto(
            "‹    Voltar",
            14f,
            branco,
            true
        ).apply {

            gravity = Gravity.CENTER_VERTICAL

            setPadding(dp(14), 0, 0, 0)

            background = arredondado(
                Color.parseColor("#33FFFFFF"),
                12
            )

            setOnClickListener {
                finish()
            }
        }

        lateral.addView(
            voltar,
            LinearLayout.LayoutParams(-1, dp(35))
        )

        lateral.addView(espaco(12))

        val scrollMenu = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
        }

        menuContainer = coluna()

        menuContainer.addView(
            texto(
                "ÁREA DO RESPONSÁVEL",
                9f,
                Color.parseColor("#E4F0FF"),
                true
            )
        )

        menuContainer.addView(espaco(10))

        itemMenu("Perfil", "♙") {
            abrirPerfil()
        }

        itemMenu("Jogos", "▣") {
            abrirJogos()
        }

        itemMenu("Sobre nós", "ⓘ") {
            abrirSobre()
        }

        itemMenu("Configurações", "⚙") {
            abrirConfiguracoes()
        }

        itemMenu("FAQ", "?") {
            abrirFAQ()
        }

        scrollMenu.addView(menuContainer)

        lateral.addView(
            scrollMenu,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val logoId = resources.getIdentifier(
            "logoludicakids",
            "drawable",
            packageName
        )

        if (logoId != 0) {

            val logo = ImageView(this).apply {
                setImageResource(logoId)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            lateral.addView(
                logo,
                LinearLayout.LayoutParams(-1, dp(42))
            )

        } else {

            lateral.addView(
                texto("LúdicaKids", 19f, branco, true).apply {
                    gravity = Gravity.CENTER
                }
            )
        }

        return lateral
    }

    private fun itemMenu(
        nome: String,
        simbolo: String,
        acao: () -> Unit
    ) {

        val botao = linha().apply {

            background = arredondado(
                Color.TRANSPARENT,
                12
            )

            setPadding(dp(10), 0, dp(6), 0)
        }

        val icone = texto(
            simbolo,
            19f,
            branco,
            true
        ).apply {
            gravity = Gravity.CENTER
        }

        botao.addView(
            icone,
            LinearLayout.LayoutParams(dp(29), -1)
        )

        botao.addView(
            texto(nome, 13f, branco, true)
        )

        botao.setOnClickListener {

            if (paginaAtual != nome) {

                botao.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(65)
                    .withEndAction {

                        botao.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(90)
                            .start()

                        acao()
                    }
                    .start()
            }
        }

        menuContainer.addView(
            botao,
            LinearLayout.LayoutParams(-1, dp(38)).apply {
                bottomMargin = dp(4)
            }
        )

        botoesMenu[nome] = botao
    }

    private fun selecionarMenu(nome: String) {

        paginaAtual = nome

        botoesMenu.forEach { (titulo, botao) ->

            botao.background = arredondado(
                if (titulo == nome)
                    Color.parseColor("#40FFFFFF")
                else
                    Color.TRANSPARENT,
                12
            )
        }
    }

    // ==========================================
    // PÁGINAS E TRANSIÇÕES
    // ==========================================

    private fun criarPagina(nome: String): LinearLayout {

        areaConteudo.removeAllViews()

        selecionarMenu(nome)

        val scroll = ScrollView(this).apply {

            isFillViewport = true
            isVerticalScrollBarEnabled = false
            clipToPadding = false

            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val pagina = coluna().apply {

            setPadding(
                dp(24),
                dp(20),
                dp(24),
                dp(30)
            )
        }

        scroll.addView(
            pagina,
            FrameLayout.LayoutParams(-1, -2)
        )

        areaConteudo.addView(
            scroll,
            FrameLayout.LayoutParams(-1, -1)
        )

        // Transição suave entre as abas

        scroll.alpha = 0f
        scroll.translationX = dp(12).toFloat()

        scroll.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(210)
            .start()

        return pagina
    }

    private fun cabecalho(
        pagina: LinearLayout,
        titulo: String,
        descricao: String
    ) {

        pagina.addView(
            texto(titulo, 26f, azulTitulo, true)
        )

        pagina.addView(espaco(4))

        pagina.addView(
            texto(descricao, 13f, textoCinza)
        )

        pagina.addView(espaco(18))
    }

    private fun tituloSecao(
        pagina: LinearLayout,
        titulo: String
    ) {

        pagina.addView(espaco(15))

        pagina.addView(
            texto(titulo, 19f, azulTitulo, true)
        )

        pagina.addView(espaco(12))
    }

    // ==========================================
    // MODELOS DE CARDS
    // ==========================================

    private fun card(
        titulo: String,
        descricao: String,
        cor: Int = branco
    ): LinearLayout {

        return coluna().apply {

            background = arredondado(cor, 20)

            elevation = dp(3).toFloat()

            setPadding(
                dp(17),
                dp(16),
                dp(17),
                dp(16)
            )

            addView(
                texto(titulo, 15f, textoEscuro, true)
            )

            addView(espaco(7))

            addView(
                texto(descricao, 13f, textoCinza)
            )
        }
    }

    private fun cardHorizontal(
        titulo: String,
        descricao: String,
        nomeImagem: String,
        cor: Int = branco,
        imagemEsquerda: Boolean = true
    ): LinearLayout {

        val painel = linha().apply {

            background = arredondado(cor, 22)
            elevation = dp(3).toFloat()

            setPadding(
                dp(13),
                dp(13),
                dp(13),
                dp(13)
            )
        }

        val ilustracao = imagem(
            nomeImagem,
            135,
            Color.TRANSPARENT
        )

        val informacoes = coluna().apply {

            setPadding(
                dp(16),
                dp(8),
                dp(12),
                dp(8)
            )

            addView(
                texto(titulo, 18f, textoEscuro, true)
            )

            addView(espaco(8))

            addView(
                texto(descricao, 13f, textoCinza)
            )
        }

        val paramsImagem = LinearLayout.LayoutParams(
            0,
            dp(135),
            0.43f
        )

        val paramsTexto = LinearLayout.LayoutParams(
            0,
            -2,
            0.57f
        )

        if (imagemEsquerda) {

            painel.addView(ilustracao, paramsImagem)
            painel.addView(informacoes, paramsTexto)

        } else {

            painel.addView(informacoes, paramsTexto)
            painel.addView(ilustracao, paramsImagem)
        }

        return painel
    }

    // ==========================================
    // PERFIL
    // ==========================================

    private fun abrirPerfil() {

        val pagina = criarPagina("Perfil")

        cabecalho(
            pagina,
            "Meu perfil",
            "Cuide da sua conta e dos pequenos que aprendem com a gente."
        )

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Olá, responsável!",
                "Este espaço foi criado para você acompanhar a experiência das crianças no LúdicaKids.",
                "fotoperfilresponsavel",
                azulClaro
            )
        )

        tituloSecao(pagina, "Minha conta")

        adicionarBloco(
            pagina,
            card(
                "Informações pessoais",
                "Consulte as informações da sua conta de responsável."
            )
        )

        adicionarBloco(
            pagina,
            card(
                "Crianças cadastradas",
                "Os perfis infantis vinculados à sua conta fazem parte de um espaço de aprendizagem individual."
            )
        )

        adicionarBloco(
            pagina,
            card(
                "Uma experiência em família",
                "Acompanhe a criança durante as atividades e incentive conversas sobre seus sentimentos.",
                verde
            )
        )
    }

    // ==========================================
    // JOGOS
    // ==========================================

    private fun abrirJogos() {

        val pagina = criarPagina("Jogos")

        cabecalho(
            pagina,
            "Conheça nossos jogos",
            "Pequenas brincadeiras para grandes descobertas."
        )

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Identificando as Emoções",
                "Com a ajuda da Bubba, a criança aprende a identificar expressões, reconhecer sentimentos e compreender suas emoções de maneira divertida.",
                "jogoemocoes",
                verde
            ),
            17
        )

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Cinco Sentidos Mágicos",
                "Atividades interativas que estimulam a percepção da visão, audição, olfato, paladar e tato.",
                "jogocincosentidos",
                amarelo,
                false
            ),
            17
        )

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Respirando com o Dragão",
                "Exercícios lúdicos de respiração que incentivam momentos de tranquilidade e autorregulação emocional.",
                "jogodragao",
                rosa
            ),
            17
        )

        adicionarBloco(
            pagina,
            card(
                "Aprender brincando",
                "As atividades respeitam o ritmo da criança e incentivam a descoberta sem pressão.",
                branco
            )
        )
    }

    // ==========================================
    // SOBRE NÓS
    // ==========================================

    private fun abrirSobre() {

        val pagina = criarPagina("Sobre nós")

        // NOSSA MISSÃO

        val missao = coluna().apply {

            background = degradê(
                branco,
                Color.parseColor("#EAF4FF"),
                25
            )

            elevation = dp(3).toFloat()

            setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(15)
            )

            gravity = Gravity.CENTER_HORIZONTAL
        }

        missao.addView(
            texto("Nossa missão", 26f, azulTitulo, true)
                .apply {
                    gravity = Gravity.CENTER
                }
        )

        missao.addView(espaco(5))

        missao.addView(
            texto(
                "Brincar para acalmar, aprender para crescer!",
                14f,
                azulTitulo,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        missao.addView(espaco(10))

        // IMAGEM GERADA PARA A MISSÃO

        missao.addView(
            imagem(
                "nossamissao",
                205,
                Color.TRANSPARENT
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(205)
            )
        )

        missao.addView(espaco(10))

        missao.addView(
            texto(
                "O LúdicaKids nasceu para promover o bem-estar infantil por meio da tecnologia, da ludicidade e da educação emocional. Criamos experiências que incentivam as crianças a reconhecer seus sentimentos, explorar seus sentidos e desenvolver habilidades de autorregulação.",
                13f,
                textoCinza
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        adicionarBloco(pagina, missao, 20)

        // ======================================
        // O QUE NOS GUIA
        // ======================================

        tituloSecao(pagina, "O que nos guia")

        val linhaValores = linha().apply {
            gravity = Gravity.TOP
        }

        adicionarValor(
            linhaValores,
            "Ética e cuidado",
            "Trabalhamos para cuidar do bem-estar da criança.",
            "iconeetica",
            Color.parseColor("#FFD784")
        )

        adicionarValor(
            linhaValores,
            "Segurança",
            "Experiência sem anúncios e com atenção à privacidade.",
            "iconeseguranca",
            Color.parseColor("#FFC8CF")
        )

        adicionarValor(
            linhaValores,
            "Aprendizado",
            "Jogos que ensinam a reconhecer e lidar com emoções.",
            "iconeaprendizado",
            Color.parseColor("#D8EF9B")
        )

        adicionarValor(
            linhaValores,
            "Inovação",
            "Soluções criativas inspiradas em evidências.",
            "iconeinovacao",
            Color.parseColor("#B9DEEF")
        )

        pagina.addView(linhaValores)

        pagina.addView(espaco(12))

        // MASCOTE

        tituloSecao(pagina, "Conheça nossa mascote")

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Bubba, a girafinha",
                "Carinhosa, curiosa e acolhedora, a Bubba representa o cuidado que desejamos transmitir às crianças. Ela acompanha os pequenos em suas descobertas e aprendizados.",
                "bubba",
                amarelo
            )
        )

        // CIÊNCIA

        tituloSecao(
            pagina,
            "Feito com cuidado e ciência"
        )

        adicionarBloco(
            pagina,
            card(
                "Educação emocional",
                "As atividades incentivam o reconhecimento dos sentimentos e a construção de habilidades emocionais desde a infância."
            )
        )

        adicionarBloco(
            pagina,
            card(
                "Base científica",
                "O desenvolvimento do projeto considera estudos sobre ludicidade, ansiedade infantil, percepção sensorial e técnicas de respiração."
            )
        )

        // COLABORADORAS

        tituloSecao(
            pagina,
            "Conheça nossa equipe"
        )

        val equipe = linha().apply {
            gravity = Gravity.TOP
        }

        adicionarColaborador(
            equipe,
            "Giovanna Montoro Barbosa",
            "fotogiovanna"
        )

        adicionarColaborador(
            equipe,
            "Lara Antunes Soares Brito",
            "fotolara"
        )

        adicionarColaborador(
            equipe,
            "Maria Aparecida Bezerra de Noronha",
            "fotomaria"
        )

        pagina.addView(equipe)

        tituloSecao(
            pagina,
            "Privacidade e cuidado"
        )

        adicionarBloco(
            pagina,
            card(
                "Proteção das crianças",
                "A criação de contas é destinada aos responsáveis adultos. A privacidade e a proteção das informações infantis são prioridades do projeto."
            )
        )

        adicionarBloco(
            pagina,
            card(
                "Importante",
                "O LúdicaKids é uma ferramenta complementar de apoio ao desenvolvimento infantil e não substitui o acompanhamento de profissionais de saúde.",
                rosa
            )
        )
    }

    // ==========================================
    // CARDS DOS QUATRO VALORES
    // ==========================================

    private fun adicionarValor(
        destino: LinearLayout,
        titulo: String,
        descricao: String,
        nomeIcone: String,
        corIcone: Int
    ) {

        val painel = coluna().apply {

            background = arredondado(branco, 18)
            elevation = dp(3).toFloat()

            minimumHeight = dp(185)

            setPadding(
                dp(11),
                dp(12),
                dp(11),
                dp(12)
            )
        }

        // ESPAÇO CIRCULAR PARA SEU ÍCONE

        painel.addView(
            imagem(
                nomeIcone,
                43,
                corIcone,
                true
            ),
            LinearLayout.LayoutParams(
                dp(43),
                dp(43)
            )
        )

        painel.addView(espaco(10))

        painel.addView(
            texto(titulo, 13f, textoEscuro, true)
        )

        painel.addView(espaco(5))

        painel.addView(
            texto(descricao, 11.5f, textoCinza)
        )

        destino.addView(
            painel,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            ).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            }
        )
    }

    // ==========================================
    // COLABORADORES
    // ==========================================

    private fun adicionarColaborador(
        destino: LinearLayout,
        nome: String,
        foto: String
    ) {

        val painel = coluna().apply {

            background = arredondado(branco, 20)
            elevation = dp(3).toFloat()

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(10),
                dp(14),
                dp(10),
                dp(14)
            )

            minimumHeight = dp(170)
        }

        painel.addView(
            imagem(
                foto,
                78,
                azulClaro,
                true
            ),
            LinearLayout.LayoutParams(
                dp(78),
                dp(78)
            )
        )

        painel.addView(espaco(12))

        painel.addView(
            texto(nome, 12f, textoEscuro, true)
                .apply {
                    gravity = Gravity.CENTER
                }
        )

        painel.addView(espaco(4))

        painel.addView(
            texto(
                "Equipe LúdicaKids",
                10f,
                textoCinza
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        destino.addView(
            painel,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            ).apply {
                marginStart = dp(5)
                marginEnd = dp(5)
            }
        )
    }

    // ==========================================
    // CONFIGURAÇÕES
    // ==========================================

    private fun abrirConfiguracoes() {

        val pagina = criarPagina("Configurações")

        cabecalho(
            pagina,
            "Configurações",
            "Personalize a experiência do LúdicaKids."
        )

        val preferencias = getSharedPreferences(
            "configuracoes_ludicakids",
            MODE_PRIVATE
        )

        val painel = coluna().apply {

            background = arredondado(branco, 20)

            elevation = dp(3).toFloat()

            setPadding(
                dp(17),
                dp(12),
                dp(17),
                dp(12)
            )
        }

        fun adicionarOpcao(
            nome: String,
            descricao: String,
            chave: String
        ) {

            val linhaOpcao = linha().apply {

                setPadding(
                    0,
                    dp(12),
                    0,
                    dp(12)
                )
            }

            val informacoes = coluna()

            informacoes.addView(
                texto(nome, 15f, textoEscuro, true)
            )

            informacoes.addView(
                texto(descricao, 12f, textoCinza)
            )

            val controle = Switch(this).apply {

                isChecked = preferencias.getBoolean(
                    chave,
                    true
                )

                thumbTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        azulMenu,
                        Color.LTGRAY
                    )
                )

                setOnCheckedChangeListener { _, marcado ->

                    preferencias.edit()
                        .putBoolean(chave, marcado)
                        .apply()
                }
            }

            linhaOpcao.addView(
                informacoes,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            linhaOpcao.addView(controle)

            painel.addView(linhaOpcao)
        }

        adicionarOpcao(
            "Música ambiente",
            "Ativar músicas durante as atividades.",
            "musica"
        )

        painel.addView(
            View(this).apply {
                setBackgroundColor(fundo)
            },
            LinearLayout.LayoutParams(-1, dp(1))
        )

        adicionarOpcao(
            "Efeitos sonoros",
            "Ativar sons de interação nos jogos.",
            "efeitos"
        )

        adicionarBloco(pagina, painel)

        tituloSecao(pagina, "Notificações")

        val notificacoes = card(
            "Permissões do aplicativo",
            "Gerencie as permissões de notificações do dispositivo."
        )

        notificacoes.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Notificações")
                .setMessage(
                    "Acesse as configurações do seu dispositivo, selecione LúdicaKids e consulte as permissões de notificações."
                )
                .setPositiveButton("Entendi", null)
                .show()
        }

        adicionarBloco(pagina, notificacoes)
    }

    // ==========================================
    // FAQ COMPLETO
    // ==========================================

    private fun abrirFAQ() {

        val pagina = criarPagina("FAQ")

        cabecalho(
            pagina,
            "Como podemos ajudar?",
            "Encontre respostas para as principais dúvidas sobre o LúdicaKids."
        )

        adicionarBloco(
            pagina,
            card(
                "Central de ajuda",
                "Toque em uma pergunta para visualizar sua resposta.",
                azulClaro
            )
        )

        grupoFAQ(
            pagina,
            "Sobre o LúdicaKids",
            listOf(

                "O que é o LúdicaKids?" to
                        "O LúdicaKids é uma plataforma educativa web e mobile que utiliza jogos lúdicos para incentivar o desenvolvimento emocional, a percepção sensorial e a autorregulação infantil.",

                "Para qual idade os jogos são indicados?" to
                        "As atividades foram desenvolvidas especialmente para crianças de 4 a 8 anos, respeitando características dessa etapa do desenvolvimento infantil.",

                "Quem é a Bubba?" to
                        "A Bubba é a girafinha mascote do LúdicaKids. Ela representa o acolhimento e acompanha as crianças em diferentes atividades educativas.",

                "O projeto possui acompanhamento de especialistas?" to
                        "O projeto considera conhecimentos da psicologia, educação e desenvolvimento infantil. As atividades possuem finalidade educativa e não substituem avaliações ou tratamentos realizados por profissionais.",

                "O LúdicaKids também orienta os responsáveis?" to
                        "Sim. A área do responsável apresenta informações sobre a proposta dos jogos, educação emocional e formas de acompanhar a experiência da criança.",

                "O aplicativo substitui um psicólogo?" to
                        "Não. O LúdicaKids é uma ferramenta complementar de apoio. Caso a criança apresente sintomas persistentes de ansiedade ou sofrimento emocional, procure orientação profissional."
            )
        )

        grupoFAQ(
            pagina,
            "Jogos e atividades",
            listOf(

                "Quais jogos estão disponíveis?" to
                        "Identificando as Emoções, Cinco Sentidos Mágicos e Respirando com o Dragão. Cada atividade trabalha diferentes habilidades relacionadas ao desenvolvimento infantil.",

                "Os jogos possuem violência ou anúncios?" to
                        "A proposta do LúdicaKids é oferecer atividades educativas sem conteúdos violentos e sem anúncios publicitários durante as brincadeiras.",

                "Todos os jogos são gratuitos?" to
                        "A disponibilidade das atividades pode variar conforme o acesso oferecido pela plataforma. Qualquer opção paga deve ser apresentada ao responsável antes da contratação.",

                "A criança recebe instruções antes de jogar?" to
                        "Os jogos utilizam orientações visuais e instruções simples para ajudar a criança a compreender as atividades propostas.",

                "Quanto tempo a criança deve jogar?" to
                        "Recomendamos sessões curtas, acompanhadas pelo responsável e intercaladas com brincadeiras fora das telas, atividades físicas e momentos de descanso.",

                "Serão lançados novos jogos?" to
                        "O projeto poderá receber novas atividades e melhorias conforme seu desenvolvimento e as necessidades identificadas pela equipe."
            )
        )

        grupoFAQ(
            pagina,
            "Cadastro e conta",
            listOf(

                "Quem pode criar uma conta?" to
                        "A conta deve ser criada por um responsável adulto. Os perfis infantis ficam vinculados à conta desse responsável.",

                "Como faço o cadastro?" to
                        "Na tela inicial, selecione a opção Criar conta e preencha as informações solicitadas. Após o cadastro, utilize suas credenciais para entrar na plataforma.",

                "Posso cadastrar mais de uma criança?" to
                        "A plataforma foi planejada para permitir perfis infantis individuais vinculados à conta do responsável.",

                "Esqueci minha senha. O que fazer?" to
                        "Caso a recuperação de senha esteja disponível na tela de login, utilize essa opção. Se não estiver, procure o canal de suporte da plataforma.",

                "Posso editar ou excluir meus dados?" to
                        "As opções disponíveis podem variar conforme a versão do aplicativo. O responsável pode solicitar orientações à equipe sobre atualização ou exclusão das informações pessoais."
            )
        )

        grupoFAQ(
            pagina,
            "Segurança e privacidade",
            listOf(

                "Quais dados da criança são solicitados?" to
                        "O cadastro infantil deve solicitar apenas as informações necessárias para a criação e identificação do perfil. Consulte os campos apresentados durante o cadastro.",

                "Os dados são compartilhados com terceiros?" to
                        "O tratamento e eventual compartilhamento de dados devem respeitar as finalidades informadas na política de privacidade da plataforma.",

                "A criança pode utilizar o aplicativo sozinha?" to
                        "A interface foi desenvolvida para facilitar a interação infantil. Mesmo assim, recomendamos que um responsável acompanhe a utilização das atividades.",

                "O aplicativo considera a LGPD?" to
                        "A proteção de dados pessoais, especialmente de crianças, é uma preocupação do projeto. O tratamento dessas informações deve respeitar a legislação aplicável e o melhor interesse infantil."
            )
        )

        grupoFAQ(
            pagina,
            "Uso e acessibilidade",
            listOf(

                "Por que o aplicativo utiliza cores suaves?" to
                        "As cores suaves, os elementos arredondados e os sons delicados foram escolhidos para criar uma experiência visual acolhedora e reduzir estímulos excessivos.",

                "O aplicativo considera a acessibilidade?" to
                        "O projeto busca facilitar a interação por meio de elementos visuais, instruções simples e botões de fácil identificação. Melhorias de acessibilidade podem ser incorporadas continuamente.",

                "A interface é fácil para crianças pequenas?" to
                        "As telas utilizam ilustrações, personagens e elementos interativos que procuram facilitar a compreensão das atividades pelas crianças.",

                "Como desativar os sons?" to
                        "Na área do responsável, abra Configurações. Nessa página estão disponíveis as preferências de música ambiente e efeitos sonoros."
            )
        )

        pagina.addView(espaco(12))

        adicionarBloco(
            pagina,
            card(
                "Ainda tem dúvidas?",
                "Entre em contato com a equipe responsável pelo LúdicaKids para enviar dúvidas, sugestões ou comentários sobre sua experiência.",
                azulClaro
            )
        )
    }

    // ==========================================
    // GRUPOS DO FAQ
    // ==========================================

    private fun grupoFAQ(
        pagina: LinearLayout,
        titulo: String,
        perguntas: List<Pair<String, String>>
    ) {

        tituloSecao(pagina, titulo)

        perguntas.forEach { (pergunta, resposta) ->

            adicionarPergunta(
                pagina,
                pergunta,
                resposta
            )
        }
    }

    // ==========================================
    // FAQ EXPANSÍVEL COM ANIMAÇÃO
    // ==========================================

    private fun adicionarPergunta(
        pagina: LinearLayout,
        pergunta: String,
        resposta: String
    ) {

        val painel = coluna().apply {

            background = arredondado(
                branco,
                16,
                Color.parseColor("#E6EDF5")
            )

            elevation = dp(2).toFloat()

            setPadding(
                dp(15),
                dp(12),
                dp(12),
                dp(12)
            )
        }

        val cabecalho = linha()

        val perguntaText = texto(
            pergunta,
            13f,
            textoEscuro,
            true
        )

        cabecalho.addView(
            perguntaText,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            ).apply {
                marginEnd = dp(12)
            }
        )

        val botao = texto(
            "+",
            20f,
            azulTitulo,
            true
        ).apply {

            gravity = Gravity.CENTER

            background = arredondado(
                azulClaro,
                30
            )
        }

        cabecalho.addView(
            botao,
            LinearLayout.LayoutParams(
                dp(29),
                dp(29)
            )
        )

        val areaResposta = coluna().apply {

            visibility = View.GONE

            addView(espaco(12))

            addView(
                View(this@TelaResponsavelActivity).apply {

                    setBackgroundColor(
                        Color.parseColor("#E7EDF4")
                    )
                },
                LinearLayout.LayoutParams(-1, dp(1))
            )

            addView(espaco(12))

            addView(
                texto(
                    resposta,
                    13f,
                    textoCinza
                )
            )

            addView(espaco(4))
        }

        painel.addView(cabecalho)
        painel.addView(areaResposta)

        var aberto = false

        cabecalho.setOnClickListener {

            aberto = !aberto

            TransitionManager.beginDelayedTransition(
                painel,
                AutoTransition().apply {
                    duration = 180
                }
            )

            areaResposta.visibility =
                if (aberto)
                    View.VISIBLE
                else
                    View.GONE

            painel.background = arredondado(
                branco,
                16,
                if (aberto)
                    Color.parseColor("#9CC6F3")
                else
                    Color.parseColor("#E6EDF5")
            )

            botao.animate()
                .rotation(
                    if (aberto) 45f else 0f
                )
                .setDuration(160)
                .start()

            perguntaText.setTextColor(
                if (aberto)
                    azulTitulo
                else
                    textoEscuro
            )
        }

        adicionarBloco(
            pagina,
            painel,
            9
        )
    }
}