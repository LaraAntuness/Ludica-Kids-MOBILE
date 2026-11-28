package com.example.mobilejogos

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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

    private val azulMenu = Color.parseColor("#438FD1")
    private val azulTitulo = Color.parseColor("#18518E")
    private val azulClaro = Color.parseColor("#E5F1FC")

    private val fundo = Color.parseColor("#EAF5FE")
    private val branco = Color.WHITE
    private val textoEscuro = Color.parseColor("#183D68")
    private val textoCinza = Color.parseColor("#697586")

    private val rosa = Color.parseColor("#FBE1E7")
    private val amarelo = Color.parseColor("#FFF0C9")
    private val verde = Color.parseColor("#E5F3CE")

    private lateinit var areaConteudo: FrameLayout
    private lateinit var menuContainer: LinearLayout

    private val botoesMenu =
        mutableMapOf<String, LinearLayout>()

    private var paginaAtual = "Perfil"
    private var abaPerfil = 0
    private var criancaAtiva = 0
    private var perfilEmEdicao = false
    private var avisoAtual: View? = null
    private val selosRecortados = mutableMapOf<Int, android.graphics.Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                voltarInicio()
            }
        }

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        montarEstrutura()
        abrirPerfil()
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
            includeFontPadding = true

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

                if (circular || nome.startsWith("selo") || nome == "telajuntosresp") {
                    val bitmap = recortarSelo(idImagem)
                    if (bitmap != null) setImageBitmap(bitmap) else setImageResource(idImagem)
                } else setImageResource(idImagem)

                scaleType = if (circular) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER

                val margem = if (circular) 0 else 3

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

    private fun recortarSelo(id: Int): android.graphics.Bitmap? {
        selosRecortados[id]?.let { return it }
        val b = android.graphics.BitmapFactory.decodeResource(resources, id,
            android.graphics.BitmapFactory.Options().apply { inScaled = false; inSampleSize = 2 }) ?: return null
        var esquerda = b.width; var direita = -1; var topo = b.height; var base = -1
        val pixels = IntArray(b.width)
        for (y in 0 until b.height) {
            b.getPixels(pixels, 0, b.width, 0, y, b.width, 1)
            for (x in pixels.indices) if ((pixels[x] ushr 24) > 24) {
                esquerda = minOf(esquerda, x); direita = maxOf(direita, x)
                topo = minOf(topo, y); base = maxOf(base, y)
            }
        }
        val recorte = if (direita >= esquerda && base >= topo)
            android.graphics.Bitmap.createBitmap(b, esquerda, topo, direita-esquerda+1, base-topo+1)
        else b
        if (recorte !== b) b.recycle()
        selosRecortados[id] = recorte
        return recorte
    }

    private fun voltarInicio() {
        finish()
    }

    private fun montarEstrutura() {
        val raiz = linha().apply { setBackgroundColor(fundo) }
        raiz.addView(criarMenu(), LinearLayout.LayoutParams(dp(155), -1))
        areaConteudo = FrameLayout(this).apply {
            addView(CenarioPerfil(), FrameLayout.LayoutParams(-1, -1))
        }
        raiz.addView(areaConteudo, LinearLayout.LayoutParams(0, -1, 1f))
        setContentView(raiz)
    }

    private fun criarMenu(): LinearLayout {
        val lateral = coluna().apply {
            background = degradê(Color.parseColor("#A4D9FC"), Color.parseColor("#6CB9EE"), 0)
            setPadding(dp(9), dp(6), dp(9), dp(8))
        }
        val logo = ImageView(this).apply {
            val id = resources.getIdentifier("logoinferior", "drawable", packageName)
            if (id != 0) setImageResource(id)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "LúdicaKids"
        }
        lateral.addView(logo, LinearLayout.LayoutParams(-1, dp(49)))
        val scrollMenu = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        menuContainer = coluna()
        itemMenu("Perfil", "") { abrirPerfil() }
        itemMenu("Atividades", "") { abrirAtividades() }
        itemMenu("Jogos", "") { abrirJogos() }
        itemMenu("Sobre nós", "") { abrirSobre() }
        itemMenu("Configurações", "") { abrirConfiguracoes() }
        itemMenu("FAQ", "") { abrirFAQ() }
        scrollMenu.addView(menuContainer)
        lateral.addView(scrollMenu, LinearLayout.LayoutParams(-1, 0, 1f))
        val voltar = texto("‹  Voltar", 13f, azulTitulo, true).apply {
            gravity = Gravity.CENTER
            background = arredondado(Color.parseColor("#CBE8FC"), 30)
            setOnClickListener { voltarInicio() }
        }
        lateral.addView(voltar, LinearLayout.LayoutParams(-1, dp(34)))
        return lateral
    }

    // Cenário leve, desenhado sem carregar imagens grandes.
    private inner class CenarioPerfil : View(this@TelaResponsavelActivity) {
        private val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            p.color = Color.parseColor("#D9EEFC")
            canvas.drawRect(0f,0f,w,h,p)
            p.color = Color.parseColor("#F3F9FF")
            canvas.drawOval(-w*.15f,-h*.1f,w*.62f,h*.48f,p)
            canvas.drawOval(w*.25f,-h*.25f,w*.85f,h*.39f,p)
            canvas.drawOval(w*.65f,-h*.05f,w*1.2f,h*.46f,p)
            p.color = Color.parseColor("#D4EBAF")
            canvas.drawOval(-w*.13f,h*.88f,w*.23f,h*1.16f,p)
            canvas.drawOval(w*.83f,h*.86f,w*1.15f,h*1.18f,p)
            p.color = Color.parseColor("#A7D58B")
            canvas.drawCircle(w*.02f,h*.96f,h*.065f,p)
            canvas.drawCircle(w*.97f,h*.96f,h*.075f,p)
        }
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

        val icone = IconeMenu(nome)
        botao.addView(icone, LinearLayout.LayoutParams(dp(22), dp(22)))
        val rotulo = texto(nome, 12.5f, azulTitulo, true)
        botao.addView(rotulo, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(8) })

        botao.setOnClickListener { if (paginaAtual != nome || (nome == "Perfil" && perfilEmEdicao)) acao() }

        menuContainer.addView(
            botao,
            LinearLayout.LayoutParams(-1, dp(32)).apply {
                bottomMargin = dp(2)
            }
        )

        botoesMenu[nome] = botao
    }

    private fun selecionarMenu(nome: String) {

        paginaAtual = nome

        botoesMenu.forEach { (titulo, botao) ->

            val selecionado = titulo == nome
            botao.background = arredondado(if (selecionado) branco else Color.TRANSPARENT, 12)
            (botao.getChildAt(0) as IconeMenu).apply { cor = azulTitulo; invalidate() }
            (botao.getChildAt(1) as TextView).setTextColor(azulTitulo)

        }
    }

    // ==========================================
    // PÁGINAS E TRANSIÇÕES
    // ==========================================

    private fun criarPagina(nome: String): LinearLayout {

        while (areaConteudo.childCount > 1) areaConteudo.removeViewAt(1)

        selecionarMenu(nome)

        val scroll = ScrollView(this).apply {

            isFillViewport = true
            isVerticalScrollBarEnabled = false
            clipToPadding = false

            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val pagina = coluna().apply {

            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(20)
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
        scroll.translationX = dp(6).toFloat()

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

            background = arredondado(cor, 20, Color.parseColor("#D9E5F1"))

            elevation = dp(5).toFloat()

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
        imagemEsquerda: Boolean = true,
        alturaImagem: Int = 135,
        proporcaoImagem: Float = 0.43f
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
            alturaImagem,
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
            dp(alturaImagem),
            proporcaoImagem
        )

        val paramsTexto = LinearLayout.LayoutParams(
            0,
            -2,
            1f - proporcaoImagem
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

    private fun acao(titulo: String, executar: () -> Unit): TextView = texto(titulo, 13f, branco, true).apply {
        gravity = Gravity.CENTER
        background = arredondado(azulTitulo, 12)
        setPadding(dp(15), dp(12), dp(15), dp(12))
        setOnClickListener { executar() }
    }

    private fun painelPerfil(): LinearLayout = coluna().apply {
        background = arredondado(branco, 22, Color.parseColor("#D4E4F4"))
        elevation = dp(3).toFloat()
        setPadding(dp(12), dp(10), dp(12), dp(12))
    }

    // Mantém o cadastro compacto em celulares e tablets.
    private fun larguraPerfil(maximo: Int): Int {
        val largura = if (areaConteudo.width > 0)
            areaConteudo.width - dp(32)
        else
            dp(resources.configuration.screenWidthDp) - dp(155 + 32)
        return minOf(dp(maximo), largura.coerceAtLeast(dp(100)))
    }

    private fun adicionarPainelCadastro(pagina: LinearLayout): LinearLayout {
        val painel = painelPerfil().apply {
            setPadding(dp(16), dp(14), dp(16), dp(14))
            elevation = dp(5).toFloat()
        }
        pagina.addView(painel, LinearLayout.LayoutParams(-1, -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(8)
        })
        return painel
    }

    private fun topoPerfil(pagina: LinearLayout) {
        // Foto maior e título à esquerda, na mesma linha.
        val topo = linha().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(2), dp(4), dp(2))
        }
        topo.addView(
            imagem("selobubbasorrindo", 78, Color.parseColor("#ABD9FA"), true),
            LinearLayout.LayoutParams(dp(78), dp(78))
        )
        val titulos = coluna().apply {
            setPadding(dp(12), 0, 0, 0)
            addView(texto("Perfil", 24f, azulTitulo, true))
            addView(texto("Minha família", 12f, textoCinza))
        }
        topo.addView(titulos, LinearLayout.LayoutParams(0, -2, 1f))
        pagina.addView(topo, LinearLayout.LayoutParams(-1, -2))
        pagina.addView(espaco(5))

        // Controle único, compacto e centralizado, como Entrar/Criar conta.
        val abas = linha().apply {
            background = arredondado(Color.parseColor("#DCEEFF"), 24,
                Color.parseColor("#B9D8F2"))
            setPadding(dp(3), dp(3), dp(3), dp(3))
        }
        listOf("Responsável", "Crianças").forEachIndexed { i, nome ->
            val ativa = i == abaPerfil
            val corAba = if (ativa) branco else azulTitulo
            val tab = linha().apply {
                gravity = Gravity.CENTER
                background = arredondado(if (ativa) azulMenu else Color.TRANSPARENT, 20)
                contentDescription = nome
                isSelected = ativa
                addView(IconeMenu(if (i == 0) "Perfil" else "Crianças").apply {
                    this.cor = corAba
                }, LinearLayout.LayoutParams(dp(17), dp(17)))
                addView(texto(nome, 12f, corAba, true).apply {
                    includeFontPadding = false
                    gravity = Gravity.CENTER
                }, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(5) })
                setOnClickListener {
                    if (abaPerfil != i || perfilEmEdicao) {
                        abaPerfil = i
                        abrirPerfil()
                    }
                }
            }
            abas.addView(tab, LinearLayout.LayoutParams(0, dp(34), 1f))
        }
        pagina.addView(abas, LinearLayout.LayoutParams(larguraPerfil(280), -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        pagina.addView(espaco(9))
    }

    private fun abrirPerfil() {
        perfilEmEdicao = false
        val pagina = criarPagina("Perfil")
        topoPerfil(pagina)
        val sessao = LudicaSessao.obter(this)
        val painel = adicionarPainelCadastro(pagina)
        if (sessao == null) {
            painel.addView(texto("Entre na sua conta para visualizar os dados.", 14f))
            return
        }
        val espera = ProgressBar(this)
        painel.addView(espera, LinearLayout.LayoutParams(dp(30), dp(30)).apply { gravity = Gravity.CENTER_HORIZONTAL })
        ApiService.consultarResponsavel(sessao.first) { resultado ->
            if (isFinishing || isDestroyed || !pagina.isAttachedToWindow || LudicaSessao.obter(this)?.second != sessao.second) return@consultarResponsavel
            painel.removeView(espera)
            resultado.onSuccess { r ->
                if (r.id != sessao.second) {
                    painel.addView(texto("Não foi possível confirmar a conta. Entre novamente.")); return@onSuccess
                }
                when (abaPerfil) {
                    0 -> mostrarResponsavel(painel, r, sessao)
                    1 -> mostrarCriancas(painel, r.criancas, sessao)
                    else -> mostrarCriancas(painel, r.criancas, sessao)
                }
            }.onFailure {
                painel.addView(texto("Não foi possível carregar os dados.", 14f, textoEscuro, true))
                painel.addView(acao("Tentar novamente") { abrirPerfil() })
            }
        }
    }

    private fun dadoPerfil(p: LinearLayout, rotulo: String, valor: String) {
        val caixa = coluna().apply {
            background = arredondado(Color.parseColor("#EAF3FC"), 12,
                Color.parseColor("#D4E4F4"))
            setPadding(dp(12), dp(9), dp(12), dp(9))
            addView(texto(rotulo, 12f, textoCinza, true))
            addView(texto(valor.ifBlank { "Não informado" }, 14f, textoEscuro, true).apply {
                setTextIsSelectable(true)
            })
        }
        adicionarBloco(p, caixa, 7)
    }

    // Duas colunas para reduzir o comprimento e a altura das barras.
    private fun parDados(p: LinearLayout, primeiro: Pair<String, String>, segundo: Pair<String, String>) {
        val fila = linha().apply { gravity = Gravity.TOP }
        val esquerda = coluna()
        val direita = coluna()
        dadoPerfil(esquerda, primeiro.first, primeiro.second)
        dadoPerfil(direita, segundo.first, segundo.second)
        fila.addView(esquerda, LinearLayout.LayoutParams(0, -2, 1f).apply {
            marginEnd = dp(6)
        })
        fila.addView(direita, LinearLayout.LayoutParams(0, -2, 1f))
        p.addView(fila, LinearLayout.LayoutParams(-1, -2))
    }

    private fun botaoCadastro(p: LinearLayout, botao: TextView, largura: Int = 180) {
        botao.background = arredondado(azulMenu, 22)
        botao.setPadding(dp(12), dp(6), dp(12), dp(6))
        botao.minimumHeight = dp(40)
        p.addView(botao, LinearLayout.LayoutParams(minOf(dp(largura), (larguraPerfil(470) - dp(24)).coerceAtLeast(dp(80))), -2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
    }

    private fun mostrarResponsavel(p: LinearLayout, r: ApiService.Responsavel, sessao: Pair<String,String>) {
        p.addView(texto("Dados do responsável", 18f, azulTitulo, true).apply {
            includeFontPadding = false
        })
        p.addView(espaco(8))
        dadoPerfil(p, "Nome completo", r.nomeCompleto)
        dadoPerfil(p, "E-mail", r.email)
        dadoPerfil(p, "Celular", r.celular)
        parDados(p, "CPF" to r.cpf, "RG" to r.rg)
        p.addView(espaco(3))
        botaoCadastro(p, acao("Editar dados") { editarResponsavel(r, sessao) })
    }

    private fun mostrarCriancas(p: LinearLayout, criancas: List<ApiService.Crianca>, sessao: Pair<String,String>) {
        p.addView(texto("Minhas crianças", 18f, azulTitulo, true)); p.addView(espaco(8))
        if (criancas.isEmpty()) p.addView(texto("Nenhuma criança cadastrada."))
        criancas.forEach { c ->
            val bloco = linha().apply {
                background = arredondado(Color.parseColor("#EDF5FD"), 17)
                setPadding(dp(8),dp(8),dp(8),dp(8))
            }
            bloco.addView(imagem("selobubbasorrindo",56,Color.parseColor("#BFE2FA"),true), LinearLayout.LayoutParams(dp(56),dp(56)))
            val info = coluna().apply {
                setPadding(dp(10),0,dp(6),0)
                addView(texto(c.nomeUsuario.ifBlank { c.nomeCompleto }, 15f, azulTitulo,true))
                addView(texto(c.nomeCompleto,12f))
                addView(texto("Nascimento: ${dataBrasil(c.dataNascimento)}",11f,textoCinza))
            }
            bloco.addView(info, LinearLayout.LayoutParams(0,-2,1f))
            bloco.addView(acao("Editar") { editarCrianca(c,sessao) }, LinearLayout.LayoutParams(-2,-2))
            adicionarBloco(p,bloco,8)
        }
        botaoCadastro(p, acao("+  Adicionar criança") {
            editarCrianca(ApiService.Crianca(nomeCompleto = "", cpf = "", nomeUsuario = "", dataNascimento = ""), sessao)
        }, 210)
    }

    private fun abrirAtividades() {
        val pagina = criarPagina("Atividades")
        cabecalho(pagina, "Atividades das crianças", "Acompanhe as descobertas e conquistas de cada criança.")
        val sessao = LudicaSessao.obter(this)
        val painel = painelPerfil()
        adicionarBloco(pagina, painel)
        if (sessao == null) { painel.addView(texto("Entre na sua conta para consultar as atividades.")); return }
        val espera = ProgressBar(this)
        painel.addView(espera, LinearLayout.LayoutParams(dp(30),dp(30)))
        ApiService.consultarResponsavel(sessao.first) { resultado ->
            if (isFinishing || isDestroyed || !pagina.isAttachedToWindow || LudicaSessao.obter(this)?.second != sessao.second) return@consultarResponsavel
            painel.removeView(espera)
            resultado.onSuccess { r ->
                if (r.id == sessao.second) mostrarHistorico(painel,r.criancas,sessao.second)
                else painel.addView(texto("Não foi possível confirmar a conta."))
            }.onFailure {
                painel.addView(texto("Não foi possível carregar os perfis."))
                painel.addView(acao("Tentar novamente") { abrirAtividades() })
            }
        }
    }

    private fun mostrarHistorico(p: LinearLayout, criancas: List<ApiService.Crianca>, id: String) {
        if (criancas.isEmpty()) { p.addView(texto("Cadastre uma criança para acompanhar as atividades.")); return }
        criancaAtiva = criancaAtiva.coerceIn(0,criancas.lastIndex)
        val seletor = Spinner(this).apply {
            val nomes = criancas.map { it.nomeUsuario.ifBlank { it.nomeCompleto }.ifBlank { "Criança" } }
            adapter = object : ArrayAdapter<String>(this@TelaResponsavelActivity, android.R.layout.simple_spinner_item, nomes) {
                override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View =
                    texto(nomes[position] + "  ▾", 14f, azulTitulo, true).apply { gravity = Gravity.CENTER }
                override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View =
                    texto(nomes[position], 14f, azulTitulo, true).apply {
                        setBackgroundColor(branco); setPadding(dp(14),dp(10),dp(14),dp(10))
                    }
            }
            background = arredondado(Color.parseColor("#E3F1FF"),30,azulMenu)
            setPadding(dp(14),0,dp(10),0)
            setSelection(criancaAtiva)
            contentDescription = "Selecionar criança"
        }
        val navegacao = linha()
        fun seta(t: String, delta: Int): TextView = texto(t,25f,azulMenu,true).apply {
            gravity = Gravity.CENTER
            contentDescription = if(delta < 0) "Criança anterior" else "Próxima criança"
            setOnClickListener { seletor.setSelection((seletor.selectedItemPosition+delta+criancas.size)%criancas.size) }
        }
        navegacao.addView(seta("‹",-1),LinearLayout.LayoutParams(dp(42),dp(36)))
        navegacao.addView(seletor,LinearLayout.LayoutParams(0,dp(36),1f))
        navegacao.addView(seta("›",1),LinearLayout.LayoutParams(dp(42),dp(36)))
        p.addView(navegacao); p.addView(espaco(10))
        val detalhes = coluna(); p.addView(detalhes)
        fun atualizar(i: Int) {
            criancaAtiva = i
            detalhes.removeAllViews()
            preencherHistorico(detalhes,criancas[i],id)
        }
        atualizar(criancaAtiva)
        seletor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, itemId: Long) { atualizar(position) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun preencherHistorico(p: LinearLayout, c: ApiService.Crianca, id: String) {
        val (j,n,t) = HistoricoAtividades.resumo(this,id,c.id)
        val indicadores = linha().apply { gravity = Gravity.TOP }
        val valores = listOf(Triple(j.toString(),"Jogos com registro",rosa), Triple(n.toString(),"Níveis concluídos",amarelo), Triple("$t min","Tempo de jogo",verde))
        valores.forEachIndexed { i, (v,l,cor) ->
            val destaque = Color.parseColor(listOf("#CB456C", "#C38A13", "#33864E")[i])
            val bloco = linha().apply {
                gravity = Gravity.CENTER_VERTICAL
                background = arredondado(cor,18,destaque)
                elevation = dp(4).toFloat()
                setPadding(dp(10),dp(10),dp(8),dp(10))
                addView(IconeMenu(listOf("Jogos","Estrela","Relógio")[i]).apply { this.cor = destaque },LinearLayout.LayoutParams(dp(30),dp(30)))
                val info = coluna().apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    addView(texto(l,11f,textoEscuro,true).apply { gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(-1,-2) })
                    addView(texto(v,23f,destaque,true).apply { gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(-1,-2) })
                }
                addView(info,LinearLayout.LayoutParams(0,-2,1f).apply { marginStart = dp(6) })
            }
            indicadores.addView(bloco,LinearLayout.LayoutParams(0,dp(86),1f).apply { marginStart = dp(3); marginEnd = dp(3) })
        }
        p.addView(indicadores); p.addView(espaco(10))
        val historico = coluna().apply {
            background = arredondado(Color.parseColor("#E3F0FC"),18,Color.parseColor("#C5DEF5"))
            setPadding(dp(9),dp(9),dp(9),dp(9))
        }
        historico.addView(texto("Histórico de atividades recentes",15f,azulTitulo,true)); historico.addView(espaco(7))
        val registros = HistoricoAtividades.listar(this,id,c.id)
        if(registros.isEmpty()) historico.addView(texto("Ainda não há atividades registradas neste dispositivo.",12f,textoCinza))
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy • HH:mm",java.util.Locale("pt","BR"))
        registros.take(30).forEach { r ->
            val linhaRegistro = linha().apply {
                background = arredondado(branco,12)
                setPadding(dp(8),dp(7),dp(8),dp(7))
            }
            val asset = when {
                r.jogo.contains("Emoções",true) -> "nuvemfeliz"
                r.jogo.contains("Dragão",true) -> "rostodragao"
                else -> "selovideogame"
            }
            linhaRegistro.addView(imagem(asset,34,Color.TRANSPARENT),LinearLayout.LayoutParams(dp(36),dp(34)))
            val info = coluna().apply {
                setPadding(dp(8),0,0,0)
                addView(texto(r.jogo,12f,textoEscuro,true))
                addView(texto(r.nivel,11f,textoCinza))
                addView(texto(formato.format(java.util.Date(r.data)),10f,textoCinza))
            }
            linhaRegistro.addView(info,LinearLayout.LayoutParams(0,-2,1f))
            adicionarBloco(historico,linhaRegistro,5)
        }
        p.addView(historico)
        p.addView(espaco(6)); p.addView(texto("Dados registrados neste dispositivo.",10f,textoCinza))
    }

    private fun dataBrasil(valor: String): String {
        val partes = valor.take(10).split("-")
        return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else valor
    }

    private fun colunasCadastro(p: LinearLayout): Pair<LinearLayout, LinearLayout> {
        val fila = linha().apply { gravity = Gravity.TOP }
        val esquerda = coluna()
        val direita = coluna()
        fila.addView(esquerda, LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(8) })
        fila.addView(direita, LinearLayout.LayoutParams(0, -2, 1f))
        p.addView(fila, LinearLayout.LayoutParams(-1, -2))
        return Pair(esquerda, direita)
    }

    private fun campo(p: LinearLayout, titulo: String, valor: String, tipo: Int = 1): EditText {
        p.addView(texto(titulo, 11.5f, azulTitulo, true))
        val e = EditText(this).apply {
            setText(valor); textSize = 13f; inputType = tipo; setSingleLine(true)
            setTextColor(textoEscuro); setHintTextColor(textoCinza); hint = titulo
            setPadding(dp(10), dp(6), dp(10), dp(6))
            minHeight = dp(40)
            background = arredondado(Color.parseColor("#F7FAFE"), 10, Color.parseColor("#CBDCEC"))
        }
        p.addView(e, LinearLayout.LayoutParams(-1, -2)); p.addView(espaco(7)); return e
    }

    private fun editarResponsavel(r: ApiService.Responsavel, sessao: Pair<String,String>) {
        abaPerfil = 0
        perfilEmEdicao = true
        val pagina = criarPagina("Perfil")
        topoPerfil(pagina)
        val p = adicionarPainelCadastro(pagina)
        p.addView(texto("Editar meus dados", 17f, azulTitulo, true))
        p.addView(espaco(7))
        val nome = campo(p, "Nome completo", r.nomeCompleto)
        val (esquerda, direita) = colunasCadastro(p)
        val email = campo(esquerda, "E-mail", r.email, 33)
        val celular = campo(direita, "Celular", r.celular, 3)
        p.addView(texto("CPF e RG não podem ser alterados por esta tela.", 12f, textoCinza))
        p.addView(espaco(12))
        lateinit var salvar: TextView
        salvar = acao("Salvar alterações") {
            val n = nome.text.toString().trim(); val e = email.text.toString().trim(); val c = celular.text.toString().trim()
            if (n.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches() || c.isBlank()) {
                Toast.makeText(this, "Confira os campos.", Toast.LENGTH_SHORT).show()
            } else {
                salvar.isEnabled = false
                ApiService.atualizarResponsavel(r.id, sessao.first, n, e, c) { res -> finalizarEdicao(p, salvar, res) }
            }
        }
        botaoCadastro(p, salvar, 200); p.addView(espaco(5)); p.addView(texto("Cancelar",13f,azulTitulo,true).apply { gravity = Gravity.CENTER; setPadding(0,dp(10),0,dp(10)); setOnClickListener { abrirPerfil() } })
    }

    private fun editarCrianca(c: ApiService.Crianca, sessao: Pair<String,String>) {
        abaPerfil = 1
        perfilEmEdicao = true
        val pagina = criarPagina("Perfil")
        topoPerfil(pagina)
        val p = adicionarPainelCadastro(pagina)
        p.addView(texto(if(c.id.isBlank()) "Adicionar criança" else "Dados da criança", 17f, azulTitulo, true))
        p.addView(espaco(7))
        val (nomeColuna, usuarioColuna) = colunasCadastro(p)
        val nome = campo(nomeColuna, "Nome completo", c.nomeCompleto)
        val usuario = campo(usuarioColuna, "Nome de usuário", c.nomeUsuario)
        val (cpfColuna, dataColuna) = colunasCadastro(p)
        val cpf = campo(cpfColuna, "CPF", c.cpf, 3)
        val data = campo(dataColuna, "Nascimento (DD/MM/AAAA)", dataBrasil(c.dataNascimento))
        data.isFocusable = false
        data.setOnClickListener {
            val cal = java.util.Calendar.getInstance()
            val f = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR")).apply { isLenient = false }
            runCatching { f.parse(data.text.toString()) }.getOrNull()?.let { cal.time = it }
            DatePickerDialog(this, { _, a, m, d -> data.setText(String.format(java.util.Locale.US, "%02d/%02d/%04d", d,m+1,a)) }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        lateinit var salvar: TextView
        salvar = acao("Salvar alterações") {
            val f = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR")).apply { isLenient = false }
            val dia = runCatching { f.parse(data.text.toString()) }.getOrNull()
            if (nome.text.isBlank() || usuario.text.isBlank() || cpf.text.isBlank() || dia == null || dia.time > System.currentTimeMillis()) {
                Toast.makeText(this, "Confira os dados da criança.", Toast.LENGTH_SHORT).show()
            } else {
                val iso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(dia)
                salvar.isEnabled = false
                val dados = c.copy(nomeCompleto = nome.text.toString().trim(), nomeUsuario = usuario.text.toString().trim(), cpf = cpf.text.toString().trim(), dataNascimento = iso)
                if (c.id.isBlank()) {
                    ApiService.adicionarCrianca(sessao.second, sessao.first, dados) { res -> finalizarEdicao(p, salvar, res) }
                } else {
                    ApiService.atualizarCrianca(sessao.second, sessao.first, dados) { res -> finalizarEdicao(p, salvar, res) }
                }
            }
        }
        botaoCadastro(p, salvar, 200); p.addView(espaco(5))
        p.addView(texto("Cancelar", 13f, azulTitulo, true).apply {
            gravity = Gravity.CENTER; setPadding(0,dp(10),0,dp(10)); setOnClickListener { abrirPerfil() }
        })
        if(c.id.isNotBlank()) {
            val excluir = texto("Excluir perfil infantil", 12f, Color.parseColor("#B94251"), true).apply {
                gravity = Gravity.CENTER; setPadding(0,dp(9),0,dp(9))
            }
            excluir.setOnClickListener {
                if (!salvar.isEnabled) return@setOnClickListener
                confirmarExclusao(c.nomeCompleto) {
                    excluir.isEnabled = false; salvar.isEnabled = false
                    ApiService.excluirCrianca(sessao.second, sessao.first, c.id) { res ->
                        if (isFinishing || isDestroyed || !p.isAttachedToWindow) return@excluirCrianca
                        excluir.isEnabled = true; salvar.isEnabled = true
                        res.onSuccess { abrirPerfil(); mostrarConfirmacao("Perfil excluído com sucesso!") }
                            .onFailure { Toast.makeText(this, it.message ?: "Não foi possível excluir.", Toast.LENGTH_LONG).show() }
                    }
                }
            }
            p.addView(excluir)
        }
    }

    private fun confirmarExclusao(nome: String, confirmar: () -> Unit) {
        val dialog = android.app.Dialog(this)
        val painel = coluna().apply {
            background = arredondado(branco,24,Color.parseColor("#F4BCC8"))
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22),dp(16),dp(22),dp(16))
        }
        val vermelho = Color.parseColor("#C94557")
        val icone = FrameLayout(this).apply {
            background = arredondado(Color.parseColor("#FDE4E8"),40)
            addView(IconeMenu("Excluir").apply { cor = vermelho },FrameLayout.LayoutParams(dp(30),dp(30),Gravity.CENTER))
        }
        painel.addView(icone,LinearLayout.LayoutParams(dp(54),dp(54)))
        painel.addView(espaco(8))
        painel.addView(texto("Excluir perfil?",21f,textoEscuro,true).apply { gravity = Gravity.CENTER })
        painel.addView(espaco(6))
        painel.addView(texto("O perfil de $nome será removido da conta. Deseja continuar?",14f,textoCinza).apply { gravity = Gravity.CENTER })
        painel.addView(espaco(16))
        val botoes = linha()
        val cancelar = acao("Cancelar") { dialog.dismiss() }.apply {
            background = arredondado(Color.parseColor("#E5F1FC"),14); setTextColor(azulTitulo)
        }
        val excluir = acao("Excluir") { dialog.dismiss(); confirmar() }.apply { background = arredondado(vermelho,14) }
        botoes.addView(cancelar,LinearLayout.LayoutParams(0,dp(44),1f).apply { marginEnd = dp(8) })
        botoes.addView(excluir,LinearLayout.LayoutParams(0,dp(44),1f))
        painel.addView(botoes,LinearLayout.LayoutParams(-1,-2))
        dialog.setContentView(painel)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.window?.setLayout(minOf(dp(365),resources.displayMetrics.widthPixels-dp(32)),-2)
        dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.45f)
    }

    private fun finalizarEdicao(p: LinearLayout, salvar: TextView, res: Result<Unit>) {
        if (isFinishing || isDestroyed || !p.isAttachedToWindow) return
        salvar.isEnabled = true
        res.onSuccess { abrirPerfil(); mostrarConfirmacao("Alterações salvas com sucesso!") }
            .onFailure { Toast.makeText(this, it.message ?: "Não foi possível salvar.", Toast.LENGTH_LONG).show() }
    }

    // Aviso personalizado dentro da janela, sem depender de Toast customizado do sistema.
    private fun mostrarConfirmacao(mensagem: String) {
        val raiz = window.decorView.findViewById<FrameLayout>(android.R.id.content)
        avisoAtual?.let { raiz.removeView(it) }
        val aviso = texto("✓   $mensagem", 13f, Color.parseColor("#246A40"), true).apply {
            gravity = Gravity.CENTER
            background = arredondado(Color.parseColor("#DCF5E5"), 24, Color.parseColor("#A6DCB7"))
            setPadding(dp(16),dp(11),dp(16),dp(11))
            elevation = dp(7).toFloat()
            alpha = 0f
            translationY = dp(10).toFloat()
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        avisoAtual = aviso
        raiz.addView(aviso,FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply { bottomMargin = dp(12) })
        aviso.animate().alpha(1f).translationY(0f).setDuration(180).start()
        aviso.postDelayed({
            aviso.animate().alpha(0f).setDuration(180).withEndAction {
                raiz.removeView(aviso)
                if(avisoAtual === aviso) avisoAtual = null
            }.start()
        },2400)
    }

    private inner class IconeMenu(private val nome: String) : View(this@TelaResponsavelActivity) {
        var cor = azulTitulo
        private val pincel = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE; strokeWidth = 1.7f; strokeCap = android.graphics.Paint.Cap.ROUND; strokeJoin = android.graphics.Paint.Join.ROUND
        }
        override fun onDraw(c: android.graphics.Canvas) {
            super.onDraw(c); c.save(); c.scale(width/24f,height/24f); pincel.color = cor
            fun l(x:Float,y:Float,a:Float,b:Float) = c.drawLine(x,y,a,b,pincel)
            fun o(x:Float,y:Float,r:Float) = c.drawCircle(x,y,r,pincel)
            fun path(vararg v: Float) { val p = android.graphics.Path(); p.moveTo(v[0],v[1]); for(i in 2 until v.size step 2) p.lineTo(v[i],v[i+1]); c.drawPath(p,pincel) }
            when(nome) {
                "Excluir" -> { path(6f,7f,7f,21f,17f,21f,18f,7f); l(4f,7f,20f,7f); path(9f,7f,9f,3f,15f,3f,15f,7f); l(10f,10f,10f,18f); l(14f,10f,14f,18f) }
                "Crianças" -> { o(8f,7f,3f); o(17f,8f,2.6f); c.drawArc(2f,12f,14f,23f,180f,180f,false,pincel); c.drawArc(13f,13f,22f,22f,180f,180f,false,pincel) }
                "Atividades" -> { c.drawRoundRect(3f,14f,7f,21f,1f,1f,pincel); c.drawRoundRect(10f,9f,14f,21f,1f,1f,pincel); c.drawRoundRect(17f,3f,21f,21f,1f,1f,pincel) }
                "Relógio" -> { o(12f,12f,9f); path(12f,6f,12f,12f,16f,14f) }
                "Estrela" -> {
                    val pontos = android.graphics.Path()
                    for(i in 0..9) {
                        val a = -Math.PI/2 + i*Math.PI/5
                        val r = if(i%2==0) 10f else 4.5f
                        val x = 12f + r*kotlin.math.cos(a).toFloat()
                        val y = 12f + r*kotlin.math.sin(a).toFloat()
                        if(i==0) pontos.moveTo(x,y) else pontos.lineTo(x,y)
                    }
                    pontos.close(); c.drawPath(pontos,pincel)
                }
                "Perfil" -> { o(12f,7f,3.5f); c.drawArc(5f,13f,19f,25f,180f,180f,false,pincel) }
                "Jogos" -> { c.drawRoundRect(3f,7f,21f,18f,5f,5f,pincel); l(6f,12f,11f,12f); l(8.5f,9.5f,8.5f,14.5f); o(16f,11f,0.6f); o(18f,14f,0.6f) }
                "Sobre nós" -> { val p=android.graphics.Path(); p.moveTo(12f,20f); p.cubicTo(0f,12f,2f,3f,8f,5f); p.quadTo(11f,5f,12f,8f); p.quadTo(15f,3f,19f,5f); p.cubicTo(25f,9f,19f,16f,12f,20f); c.drawPath(p,pincel) }
                "Configurações" -> { o(12f,12f,7f); o(12f,12f,3f); for(i in 0..7) { val a=i*Math.PI/4; l(12f+7f*kotlin.math.cos(a).toFloat(),12f+7f*kotlin.math.sin(a).toFloat(),12f+10f*kotlin.math.cos(a).toFloat(),12f+10f*kotlin.math.sin(a).toFloat()) } }
                else -> { o(12f,12f,9f); c.drawArc(9f,6f,15f,12f,180f,250f,false,pincel); path(13f,12f,12f,14f); o(12f,17f,0.5f) }
            }; c.restore()
        }
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
                "telaimaginacao",
                verde,
                alturaImagem = 165,
                proporcaoImagem = 0.48f
            ),
            17
        )

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Cinco Sentidos Mágicos",
                "Atividades interativas que estimulam a percepção da visão, audição, olfato, paladar e tato.",
                "sentidosresp",
                amarelo,
                false,
                alturaImagem = 165,
                proporcaoImagem = 0.48f
            ),
            17
        )

        adicionarBloco(
            pagina,
            cardHorizontal(
                "Respirando com o Dragão",
                "Exercícios lúdicos de respiração que incentivam momentos de tranquilidade e autorregulação emocional.",
                "dragaoresp",
                rosa,
                false,
                alturaImagem = 165,
                proporcaoImagem = 0.48f
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
                "telajuntosresp",
                300,
                Color.TRANSPARENT
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(300)
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
            "selomaocoracao",
            Color.parseColor("#FFD784")
        )

        adicionarValor(
            linhaValores,
            "Segurança",
            "Experiência sem anúncios e com atenção à privacidade.",
            "seloseguranca",
            Color.parseColor("#FFC8CF")
        )

        adicionarValor(
            linhaValores,
            "Aprendizado",
            "Jogos que ensinam a reconhecer e lidar com emoções.",
            "selovideogame",
            Color.parseColor("#D8EF9B")
        )

        adicionarValor(
            linhaValores,
            "Inovação",
            "Soluções criativas inspiradas em evidências.",
            "selolampada",
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
                "selobubbasorrindo",
                branco,
                imagemEsquerda = true
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
                "Cuidado profissional",
                "O projeto considera conhecimentos de psicologia e educação para orientar a criação das atividades."
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
            "selogato"
        )

        adicionarColaborador(
            equipe,
            "Lara Antunes Soares Brito",
            "selopato"
        )

        adicionarColaborador(
            equipe,
            "Maria Aparecida Bezerra de Noronha",
            "seloesquilo"
        )

        pagina.addView(equipe)

        tituloSecao(
            pagina,
            "Privacidade e contato"
        )

        adicionarBloco(
            pagina,
            card(
                "Proteção das crianças",
                "A criação de contas é destinada aos responsáveis adultos. A privacidade e a proteção das informações infantis são prioridades do projeto."
            )
        )

        adicionarBloco(pagina, card("Fale conosco", "E-mail: ludicakidsFatec@gmail.com\nTelefone: (11) 94724-6510"))

        val importante = card(
            "Importante",
            "O LúdicaKids é uma ferramenta complementar de apoio ao desenvolvimento infantil e não substitui o acompanhamento de profissionais de saúde.",
            Color.parseColor("#B3FFFFFF")
        ).apply {
            background = arredondado(Color.parseColor("#B3FFFFFF"), 20,
                Color.parseColor("#A6C9EA"))
            elevation = dp(2).toFloat()
        }
        adicionarBloco(pagina, importante)
        pulsarAviso(importante)

    }

    private fun pulsarAviso(view: View) {
        val animacao = android.animation.ObjectAnimator.ofPropertyValuesHolder(
            view,
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.012f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.012f)
        ).apply {
            duration = 1800L
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.REVERSE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { animacao.start() }
            override fun onViewDetachedFromWindow(v: View) {
                animacao.cancel()
                v.scaleX = 1f
                v.scaleY = 1f
            }
        })
        if (view.isAttachedToWindow) animacao.start()
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

            background = arredondado(branco, 18, Color.parseColor("#D9E5F1"))
            elevation = dp(6).toFloat()

            minimumHeight = dp(158)

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
                55,
                corIcone,
                true
            ),
            LinearLayout.LayoutParams(
                dp(55),
                dp(55)
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
                -1,
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

            background = arredondado(branco, 20, Color.parseColor("#D9E5F1"))
            elevation = dp(6).toFloat()

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(10),
                dp(14),
                dp(10),
                dp(14)
            )

            minimumHeight = dp(156)
        }

        painel.addView(
            imagem(
                foto,
                86,
                azulClaro,
                true
            ),
            LinearLayout.LayoutParams(
                dp(86),
                dp(86)
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
                -1,
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
                        "Na aba Perfil, você pode atualizar seus dados de contato e os dados das crianças. Para solicitar a exclusão da conta, entre em contato com a equipe."
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
                pagina,
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
