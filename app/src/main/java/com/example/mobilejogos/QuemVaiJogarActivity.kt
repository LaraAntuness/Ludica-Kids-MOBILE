package com.example.mobilejogos

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.*
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class QuemVaiJogarActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var lista: LinearLayout

    private var popupAtual: Dialog? = null
    private var carregando = false
    private var navegando = false
    private var telaAtiva = false

    private var responsavelAtual: String? = null

    private var criancas =
        listOf<ApiService.Crianca>()

    // ==========================================
    // CORES
    // ==========================================

    private val azul = Color.parseColor("#5F91CF")
    private val azulEscuro = Color.parseColor("#315B97")
    private val azulClaro = Color.parseColor("#E6F3FF")
    private val azulBorda = Color.parseColor("#9BCFFF")
    private val rosa = Color.parseColor("#EE8FBD")
    private val rosaClaro = Color.parseColor("#FFC9E4")
    private val amarelo = Color.parseColor("#FFD570")
    private val fundo = Color.parseColor("#E9E9E9")

    // ==========================================
    // AVATARES ORIGINAIS
    // ==========================================

    private val avatares = listOf(
        R.drawable.popupbubba,
        R.drawable.popupinverno,
        R.drawable.popuppirata,
        R.drawable.popuplaco,
        R.drawable.popupcowboy,
        R.drawable.popupgraduacao,
        R.drawable.popupaniversario,
        R.drawable.popupfada,
        R.drawable.popupflores,
        R.drawable.popupviking
    )

    private val preferencias by lazy {
        getSharedPreferences(
            "LudicaKids",
            MODE_PRIVATE
        )
    }

    // PROCESSAMENTO DAS IMAGENS FORA DA INTERFACE

    private val executorImagens =
        Executors.newSingleThreadExecutor()

    private val cacheAvatares =
        mutableMapOf<Int, Bitmap>()

    private val estrelasAtivas =
        mutableListOf<View>()

    // ==========================================
    // INICIAR TELA
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(
            R.layout.activity_quem_vai_jogar
        )

        root = findViewById(R.id.gameRoot)

        montarTela()
    }

    override fun onResume() {

        super.onResume()

        telaAtiva = true
        navegando = false

        if (::lista.isInitialized) {
            carregarCriancas()
        }
    }

    override fun onPause() {

        telaAtiva = false

        limparEstrelinhas()

        super.onPause()
    }

    private fun dp(valor: Int): Int {

        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }

    // ==========================================
    // FUNDOS
    // ==========================================

    private fun arredondado(
        cor: Int,
        raio: Int,
        borda: Int = Color.TRANSPARENT,
        larguraBorda: Int = 0
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            setColor(cor)

            cornerRadius = dp(raio).toFloat()

            if (larguraBorda > 0) {

                setStroke(
                    dp(larguraBorda),
                    borda
                )
            }
        }
    }

    private fun circulo(
        cor: Int,
        borda: Int = Color.WHITE,
        larguraBorda: Int = 0
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape = GradientDrawable.OVAL

            setColor(cor)

            if (larguraBorda > 0) {

                setStroke(
                    dp(larguraBorda),
                    borda
                )
            }
        }
    }

    // ==========================================
    // TEXTOS
    // ==========================================

    private fun texto(
        mensagem: String,
        tamanho: Float,
        cor: Int = azulEscuro,
        negrito: Boolean = true
    ): TextView {

        return TextView(this).apply {

            text = mensagem
            textSize = tamanho

            gravity = Gravity.CENTER

            setTextColor(cor)

            typeface = Typeface.create(
                "sans-serif-rounded",
                if (negrito) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )

            includeFontPadding = false
        }
    }

    // ==========================================
    // MONTAR TELA PRINCIPAL
    // ==========================================

    private fun montarTela() {

        root.removeAllViews()

        root.setBackgroundColor(fundo)

        root.clipChildren = false
        root.clipToPadding = false

        val conteudo = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(20),
                dp(35),
                dp(20),
                dp(12)
            )

            clipChildren = false
            clipToPadding = false
        }

        root.addView(
            conteudo,
            FrameLayout.LayoutParams(-1, -1)
        )

        // TÍTULO

        val titulo = ImageView(this).apply {

            setImageResource(
                R.drawable.quemvaijogar
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER
        }

        conteudo.addView(
            titulo,
            LinearLayout.LayoutParams(
                dp(330),
                dp(65)
            )
        )

        // LISTA DE CRIANÇAS

        val scroll = HorizontalScrollView(this).apply {

            isFillViewport = true

            isHorizontalScrollBarEnabled = false

            clipChildren = false
            clipToPadding = false
        }

        lista = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER

            clipChildren = false
            clipToPadding = false

            setPadding(
                dp(18),
                dp(10),
                dp(18),
                dp(10)
            )
        }

        scroll.addView(
            lista,
            FrameLayout.LayoutParams(-2, -1)
        )

        conteudo.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        // VOLTAR

        val voltar = ImageView(this).apply {

            setImageResource(
                R.drawable.botaoazul
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER

            contentDescription = "Voltar"

            setOnClickListener {

                popupAtual?.dismiss()

                finish()
            }
        }

        root.addView(
            voltar,
            FrameLayout.LayoutParams(
                dp(46),
                dp(46)
            ).apply {

                gravity =
                    Gravity.TOP or Gravity.START

                leftMargin = dp(12)
                topMargin = dp(12)
            }
        )
    }

    // ==========================================
    // CONSULTAR CRIANÇAS NO BACKEND
    // ==========================================

    private fun carregarCriancas() {

        val sessao = LudicaSessao.obter(this)

        if (sessao == null) {

            responsavelAtual = null

            criancas = emptyList()

            popupAtual?.dismiss()

            lista.removeAllViews()

            mostrarMensagem(
                "Entre na sua conta para acessar os perfis."
            )

            return
        }

        if (responsavelAtual != sessao.second) {

            responsavelAtual = sessao.second

            criancas = emptyList()

            popupAtual?.dismiss()

            lista.removeAllViews()
        }

        if (carregando) return

        carregando = true

        // Só mostra carregamento se ainda não houver perfis.

        if (criancas.isEmpty()) {

            lista.removeAllViews()

            lista.addView(
                ProgressBar(this),
                LinearLayout.LayoutParams(
                    dp(40),
                    dp(40)
                )
            )
        }

        ApiService.listarCriancas(
            idResponsavel = sessao.second,
            token = sessao.first
        ) { resultado ->

            runOnUiThread {

                carregando = false

                if (
                    isFinishing ||
                    isDestroyed
                ) return@runOnUiThread

                // Evita exibir dados de outra sessão.

                val sessaoAtual =
                    LudicaSessao.obter(this)

                if (sessaoAtual != sessao) {

                    if (telaAtiva) {
                        carregarCriancas()
                    }

                    return@runOnUiThread
                }

                resultado.onSuccess { dados ->

                    val mudou =
                        dados != criancas

                    criancas = dados

                    if (
                        mudou ||
                        criancas.isEmpty() ||
                        lista.childCount == 0
                    ) {

                        popupAtual?.dismiss()

                        atualizarPerfis()
                    }
                }

                resultado.onFailure { erro ->

                    val codigo =
                        (erro as? ApiService.ApiException)
                            ?.statusCode

                    if (
                        codigo == 401 ||
                        codigo == 403
                    ) {

                        popupAtual?.dismiss()

                        criancas = emptyList()

                        lista.removeAllViews()

                        mostrarMensagem(
                            "Sua sessão expirou. Entre novamente."
                        )

                    } else if (criancas.isEmpty()) {

                        mostrarErroCarregamento()

                    } else {

                        Toast.makeText(
                            this,
                            "Não foi possível atualizar os perfis.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun mostrarMensagem(
        mensagem: String
    ) {

        lista.addView(
            texto(mensagem, 18f)
        )
    }

    private fun mostrarErroCarregamento() {

        lista.removeAllViews()

        val areaErro = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER
        }

        areaErro.addView(
            texto(
                "Não foi possível carregar os perfis.",
                17f
            )
        )

        val tentar = criarBotao(
            "Tentar novamente"
        )

        areaErro.addView(
            tentar,
            LinearLayout.LayoutParams(
                dp(200),
                dp(45)
            ).apply {

                topMargin = dp(15)
            }
        )

        tentar.setOnClickListener {

            carregarCriancas()
        }

        lista.addView(areaErro)
    }

    private fun atualizarPerfis() {

        lista.removeAllViews()

        if (criancas.isEmpty()) {

            mostrarMensagem(
                "Nenhuma criança cadastrada."
            )

            return
        }

        criancas.forEach { crianca ->

            adicionarCrianca(crianca)
        }
    }

    private fun nomeCrianca(
        crianca: ApiService.Crianca
    ): String {

        return crianca.nomeUsuario.ifBlank {

            crianca.nomeCompleto.ifBlank {
                "Criança"
            }
        }
    }

    // ==========================================
    // RECORTE OTIMIZADO DAS IMAGENS
    // EXECUTADO EM THREAD SEPARADA
    // ==========================================

    private fun recortarAvatar(
        recurso: Int
    ): Bitmap {

        return cacheAvatares.getOrPut(recurso) {

            val medidas =
                BitmapFactory.Options().apply {

                    inJustDecodeBounds = true
                    inScaled = false
                }

            BitmapFactory.decodeResource(
                resources,
                recurso,
                medidas
            )

            // Limita o tamanho da imagem decodificada.

            var amostra = 1

            while (
                medidas.outWidth / amostra > 1024 ||
                medidas.outHeight / amostra > 1024
            ) {

                amostra *= 2
            }

            val opcoes =
                BitmapFactory.Options().apply {

                    inSampleSize = amostra

                    inScaled = false

                    inPreferredConfig =
                        Bitmap.Config.ARGB_8888
                }

            val bitmap =
                BitmapFactory.decodeResource(
                    resources,
                    recurso,
                    opcoes
                ) ?: throw IllegalStateException(
                    "Imagem não encontrada."
                )

            val largura = bitmap.width
            val altura = bitmap.height

            // Copia os pixels de uma vez.
            // Mais rápido que chamar getPixel repetidamente.

            val pixels = IntArray(
                largura * altura
            )

            bitmap.getPixels(
                pixels,
                0,
                largura,
                0,
                0,
                largura,
                altura
            )

            var esquerda = largura
            var direita = -1
            var topo = altura
            var baixo = -1

            for (y in 0 until altura) {

                for (x in 0 until largura) {

                    val pixel =
                        pixels[y * largura + x]

                    if ((pixel ushr 24) > 20) {

                        if (x < esquerda) {
                            esquerda = x
                        }

                        if (x > direita) {
                            direita = x
                        }

                        if (y < topo) {
                            topo = y
                        }

                        if (y > baixo) {
                            baixo = y
                        }
                    }
                }
            }

            val recortado =
                if (
                    direita >= esquerda &&
                    baixo >= topo
                ) {

                    Bitmap.createBitmap(
                        bitmap,
                        esquerda,
                        topo,
                        direita - esquerda + 1,
                        baixo - topo + 1
                    )

                } else {

                    bitmap
                }

            // Limita o avatar final a 384 pixels.

            val maiorLado = maxOf(
                recortado.width,
                recortado.height
            )

            val final =
                if (maiorLado > 384) {

                    val escala =
                        384f / maiorLado

                    Bitmap.createScaledBitmap(
                        recortado,
                        (
                                recortado.width * escala
                                ).toInt().coerceAtLeast(1),
                        (
                                recortado.height * escala
                                ).toInt().coerceAtLeast(1),
                        true
                    )

                } else {

                    recortado
                }

            if (bitmap !== final) {
                bitmap.recycle()
            }

            if (
                recortado !== bitmap &&
                recortado !== final
            ) {
                recortado.recycle()
            }

            final
        }
    }

    // ==========================================
    // EXIBIR IMAGEM SEM TRAVAR A INTERFACE
    // ==========================================

    private fun carregarAvatar(
        imagem: ImageView,
        recurso: Int
    ) {

        if (executorImagens.isShutdown) return

        imagem.tag = recurso

        imagem.scaleType =
            ImageView.ScaleType.CENTER_CROP

        executorImagens.execute {

            if (Thread.currentThread().isInterrupted) {
                return@execute
            }

            try {

                val bitmap =
                    recortarAvatar(recurso)

                runOnUiThread {

                    if (
                        !isFinishing &&
                        !isDestroyed &&
                        imagem.tag == recurso
                    ) {

                        imagem.setImageBitmap(bitmap)
                    }
                }

            } catch (_: Exception) {

                runOnUiThread {

                    if (
                        !isFinishing &&
                        !isDestroyed &&
                        imagem.tag == recurso
                    ) {

                        imagem.setImageDrawable(null)
                    }
                }
            }
        }
    }

    // ==========================================
    // RECUPERAR FOTO SALVA
    // BUBBA ORIGINAL COMO PADRÃO
    // ==========================================

    private fun obterAvatar(
        crianca: ApiService.Crianca
    ): Int {

        val fotoSalva = preferencias.getString(
            "fotoPerfil_${crianca.id}",
            "popupbubba"
        ) ?: "popupbubba"

        val recurso =
            resources.getIdentifier(
                fotoSalva,
                "drawable",
                packageName
            )

        return if (recurso in avatares) {

            recurso

        } else {

            R.drawable.popupbubba
        }
    }

    private fun salvarAvatar(
        crianca: ApiService.Crianca,
        recurso: Int
    ) {

        val nomeImagem =
            resources.getResourceEntryName(recurso)

        preferencias.edit()
            .putString(
                "fotoPerfil_${crianca.id}",
                nomeImagem
            )
            .apply()
    }

    // ==========================================
    // VERIFICAR SE PODE ABRIR POP-UP
    // ==========================================

    private fun podeAbrirPopup(): Boolean {

        if (popupAtual?.isShowing == false) {

            popupAtual = null
        }

        return telaAtiva &&
                popupAtual == null &&
                !navegando &&
                !isFinishing &&
                !isDestroyed
    }

    // ==========================================
    // CRIAR PERFIL DA CRIANÇA
    // ==========================================

    private fun adicionarCrianca(
        crianca: ApiService.Crianca
    ) {

        val card = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            clipChildren = false
            clipToPadding = false
        }

        lista.addView(
            card,
            LinearLayout.LayoutParams(
                dp(170),
                -2
            ).apply {

                marginStart = dp(12)
                marginEnd = dp(12)
            }
        )

        val areaFoto = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        card.addView(
            areaFoto,
            LinearLayout.LayoutParams(
                dp(155),
                dp(155)
            )
        )

        // FUNDO CIRCULAR

        val fundoFoto = FrameLayout(this).apply {

            background = circulo(azul)

            foreground = circulo(
                Color.TRANSPARENT,
                Color.WHITE,
                3
            )

            clipToOutline = true
        }

        areaFoto.addView(
            fundoFoto,
            FrameLayout.LayoutParams(
                dp(145),
                dp(145),
                Gravity.CENTER
            )
        )

        // FOTO GRANDE

        val imagem = ImageView(this).apply {

            scaleType =
                ImageView.ScaleType.CENTER_CROP

            contentDescription =
                "Perfil de ${nomeCrianca(crianca)}"
        }

        fundoFoto.addView(
            imagem,
            FrameLayout.LayoutParams(-1, -1)
        )

        carregarAvatar(
            imagem,
            obterAvatar(crianca)
        )

        // LÁPIS ROSA

        val editar = texto(
            "✎",
            26f,
            Color.WHITE
        ).apply {

            background = circulo(
                rosa,
                Color.WHITE,
                3
            )

            elevation = dp(5).toFloat()

            contentDescription =
                "Editar foto de ${nomeCrianca(crianca)}"

            setOnClickListener {

                if (podeAbrirPopup()) {

                    mostrarPopupFoto(
                        crianca,
                        imagem
                    )
                }
            }
        }

        areaFoto.addView(
            editar,
            FrameLayout.LayoutParams(
                dp(44),
                dp(44),
                Gravity.BOTTOM or Gravity.END
            )
        )

        // NOME

        val nome = texto(
            nomeCrianca(crianca),
            19f,
            Color.BLACK
        )

        card.addView(
            nome,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin = dp(12)
            }
        )

        // ABRIR POP-UP SEM ESPERAR ANIMAÇÕES

        fundoFoto.setOnClickListener {

            if (!podeAbrirPopup()) {
                return@setOnClickListener
            }

            animarEstrelinhas(fundoFoto)

            mostrarPopupBrincar(crianca)
        }

        nome.setOnClickListener {

            fundoFoto.performClick()
        }
    }

    // ==========================================
    // ESTRUTURA PADRÃO DOS POP-UPS
    // ==========================================

    private data class Popup(

        val dialog: Dialog,

        val container: FrameLayout,

        val conteudo: LinearLayout
    )

    private fun criarPopup(
        titulo: String
    ): Popup {

        val dialog = Dialog(this)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        val container = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false

            setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )
        }

        // BORDA AZUL

        val bordaExterna = FrameLayout(this).apply {

            background = arredondado(
                azulBorda,
                30
            )

            setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
            )
        }

        container.addView(
            bordaExterna,
            FrameLayout.LayoutParams(-1, -2)
        )

        // BORDA ROSA

        val bordaInterna = FrameLayout(this).apply {

            background = arredondado(
                rosaClaro,
                26
            )

            setPadding(
                dp(3),
                dp(3),
                dp(3),
                dp(3)
            )
        }

        bordaExterna.addView(
            bordaInterna,
            FrameLayout.LayoutParams(-1, -2)
        )

        val scroll = ScrollView(this).apply {

            isVerticalScrollBarEnabled = false

            overScrollMode = View.OVER_SCROLL_NEVER

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.WHITE,
                    azulClaro
                )
            ).apply {

                cornerRadius = dp(23).toFloat()
            }
        }

        bordaInterna.addView(
            scroll,
            FrameLayout.LayoutParams(-1, -2)
        )

        // CONTEÚDO

        val conteudo = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(18),
                dp(13),
                dp(18),
                dp(13)
            )
        }

        scroll.addView(
            conteudo,
            FrameLayout.LayoutParams(-1, -2)
        )

        // CABEÇALHO

        val cabecalho = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER

            setPadding(
                dp(10),
                dp(4),
                dp(25),
                dp(8)
            )
        }

        cabecalho.addView(
            texto("★", 22f, amarelo),
            LinearLayout.LayoutParams(
                dp(28),
                -2
            )
        )

        cabecalho.addView(
            texto(titulo, 23f),
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        cabecalho.addView(
            texto("★", 22f, rosa),
            LinearLayout.LayoutParams(
                dp(28),
                -2
            )
        )

        conteudo.addView(
            cabecalho,
            LinearLayout.LayoutParams(-1, -2)
        )

        // FECHAR

        val fechar = texto(
            "×",
            28f,
            Color.WHITE
        ).apply {

            background = circulo(
                rosa,
                Color.WHITE,
                3
            )

            elevation = dp(8).toFloat()

            contentDescription = "Fechar"

            setOnClickListener {

                fecharPopup(dialog)
            }
        }

        container.addView(
            fechar,
            FrameLayout.LayoutParams(
                dp(38),
                dp(38),
                Gravity.TOP or Gravity.END
            )
        )

        dialog.setContentView(container)

        dialog.setCancelable(true)

        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnDismissListener {

            container.animate().cancel()

            if (popupAtual === dialog) {

                popupAtual = null
            }
        }

        return Popup(
            dialog,
            container,
            conteudo
        )
    }

    // ==========================================
    // FECHAR POP-UP E LIBERAR NOVOS CLIQUES
    // ==========================================

    private fun fecharPopup(dialog: Dialog) {

        dialog.dismiss()

        if (popupAtual === dialog) {

            popupAtual = null
        }
    }

    // ==========================================
    // EXIBIR POP-UP
    // ==========================================

    private fun exibirPopup(
        popup: Popup,
        largura: Int
    ) {

        if (!podeAbrirPopup()) return

        try {

            popupAtual = popup.dialog

            popup.dialog.show()

            popup.dialog.window?.apply {

                setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )

                addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                )

                attributes = attributes.apply {

                    gravity = Gravity.CENTER

                    dimAmount = 0.45f
                }

                setLayout(
                    minOf(
                        dp(largura),
                        (root.width * 0.94f).toInt()
                    ),
                    -2
                )
            }

            popup.container.alpha = 0f

            popup.container.scaleX = 0.94f
            popup.container.scaleY = 0.94f

            popup.container.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180)
                .setInterpolator(
                    DecelerateInterpolator()
                )
                .start()

        } catch (_: Exception) {

            fecharPopup(popup.dialog)

            Toast.makeText(
                this,
                "Não foi possível abrir a janela.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ==========================================
    // BOTÃO PADRÃO
    // ==========================================

    private fun criarBotao(
        titulo: String
    ): TextView {

        return texto(
            titulo,
            18f,
            Color.WHITE
        ).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#83BDF5"),
                    Color.parseColor("#4788CD")
                )
            ).apply {

                cornerRadius = dp(25).toFloat()

                setStroke(
                    dp(3),
                    Color.WHITE
                )
            }

            elevation = dp(3).toFloat()

            setPadding(
                dp(12),
                dp(5),
                dp(12),
                dp(5)
            )

            isClickable = true
        }
    }

    // ==========================================
    // POP-UP ESCOLHER GIRAFINHA
    // ==========================================

    private fun mostrarPopupFoto(
        crianca: ApiService.Crianca,
        imagemPerfil: ImageView
    ) {

        if (!podeAbrirPopup()) return

        val popup = criarPopup(
            "Escolha sua girafinha!"
        )

        var avatarSelecionado =
            obterAvatar(crianca)

        popup.conteudo.addView(
            texto(
                "Escolha sua foto favorita!",
                14f,
                azulEscuro,
                false
            )
        )

        val botoesAvatar =
            mutableMapOf<Int, FrameLayout>()

        val marcadores =
            mutableMapOf<Int, TextView>()

        val densidade =
            resources.displayMetrics.density

        val larguraDp = root.width / densidade

        val alturaDp = root.height / densidade

        val tamanhoAvatar = minOf(
            83,
            (
                    (
                            minOf(620f, larguraDp * 0.94f)
                                    - 115f
                            ) / 5f
                    ).toInt(),
            (
                    (alturaDp - 170f) / 2f
                    ).toInt()
        ).coerceAtLeast(32)

        // MARCAR AVATAR SELECIONADO

        fun atualizarSelecao() {

            botoesAvatar.forEach { (recurso, area) ->

                val selecionado =
                    recurso == avatarSelecionado

                area.foreground = circulo(
                    Color.TRANSPARENT,
                    if (selecionado) {
                        rosa
                    } else {
                        Color.WHITE
                    },
                    if (selecionado) 4 else 2
                )

                marcadores[recurso]?.visibility =
                    if (selecionado) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }

        // 5 GIRAFINHAS POR FILEIRA

        avatares.chunked(5).forEach { linha ->

            val fileira = LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL

                gravity = Gravity.CENTER

                clipChildren = false
                clipToPadding = false
            }

            popup.conteudo.addView(
                fileira,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                ).apply {

                    topMargin = dp(6)
                }
            )

            linha.forEach { recurso ->

                val area = FrameLayout(this).apply {

                    clipChildren = false
                    clipToPadding = false
                }

                fileira.addView(
                    area,
                    LinearLayout.LayoutParams(
                        dp(tamanhoAvatar),
                        dp(tamanhoAvatar)
                    ).apply {

                        marginStart = dp(5)
                        marginEnd = dp(5)
                    }
                )

                val fundoAvatar =
                    FrameLayout(this).apply {

                        background = circulo(azul)

                        clipToOutline = true
                    }

                area.addView(
                    fundoAvatar,
                    FrameLayout.LayoutParams(-1, -1)
                )

                val imagem = ImageView(this).apply {

                    scaleType =
                        ImageView.ScaleType.CENTER_CROP

                    contentDescription =
                        "Girafinha ${avatares.indexOf(recurso) + 1}"
                }

                fundoAvatar.addView(
                    imagem,
                    FrameLayout.LayoutParams(-1, -1)
                )

                // CARREGAMENTO ASSÍNCRONO

                carregarAvatar(
                    imagem,
                    recurso
                )

                // SÍMBOLO DE SELEÇÃO

                val check = texto(
                    "✓",
                    15f,
                    Color.WHITE
                ).apply {

                    background = circulo(
                        rosa,
                        Color.WHITE,
                        2
                    )

                    elevation = dp(4).toFloat()

                    visibility = View.GONE
                }

                area.addView(
                    check,
                    FrameLayout.LayoutParams(
                        dp(24),
                        dp(24),
                        Gravity.BOTTOM or Gravity.END
                    )
                )

                botoesAvatar[recurso] =
                    fundoAvatar

                marcadores[recurso] =
                    check

                // SELEÇÃO LEVE E IMEDIATA

                area.setOnClickListener {

                    avatarSelecionado = recurso

                    atualizarSelecao()
                }
            }
        }

        atualizarSelecao()

        // BOTÃO SALVAR FOTO

        val salvar = criarBotao(
            "Salvar foto"
        )

        popup.conteudo.addView(
            salvar,
            LinearLayout.LayoutParams(
                dp(210),
                dp(43)
            ).apply {

                topMargin = dp(10)
            }
        )

        salvar.setOnClickListener {

            salvar.isEnabled = false

            salvarAvatar(
                crianca,
                avatarSelecionado
            )

            // Atualiza somente a foto modificada.
            // Não recria a lista inteira.

            carregarAvatar(
                imagemPerfil,
                avatarSelecionado
            )

            fecharPopup(popup.dialog)

            Toast.makeText(
                this,
                "Foto salva com sucesso!",
                Toast.LENGTH_SHORT
            ).show()
        }

        exibirPopup(
            popup,
            620
        )
    }

    // ==========================================
    // POP-UP VAMOS BRINCAR
    // ==========================================

    private fun mostrarPopupBrincar(
        crianca: ApiService.Crianca
    ) {

        if (!podeAbrirPopup()) return

        val popup = criarPopup(
            "Oba! Vamos brincar?"
        )

        val areaGirafa = FrameLayout(this).apply {

            background = circulo(azul)

            foreground = circulo(
                Color.TRANSPARENT,
                Color.WHITE,
                3
            )

            clipToOutline = true
        }

        popup.conteudo.addView(
            areaGirafa,
            LinearLayout.LayoutParams(
                dp(95),
                dp(95)
            ).apply {

                topMargin = dp(4)
            }
        )

        val girafa = ImageView(this).apply {

            scaleType =
                ImageView.ScaleType.CENTER_CROP
        }

        areaGirafa.addView(
            girafa,
            FrameLayout.LayoutParams(-1, -1)
        )

        carregarAvatar(
            girafa,
            obterAvatar(crianca)
        )

        // MENSAGEM DE BOAS-VINDAS

        val mensagem = texto(
            "Que alegria ter você aqui!",
            15f,
            azulEscuro,
            false
        )

        popup.conteudo.addView(
            mensagem,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {

                topMargin = dp(10)
            }
        )

        val nome = texto(
            nomeCrianca(crianca),
            19f
        ).apply {

            background = arredondado(
                Color.WHITE,
                22,
                azulBorda,
                2
            )

            setPadding(
                dp(25),
                dp(8),
                dp(25),
                dp(8)
            )
        }

        popup.conteudo.addView(
            nome,
            LinearLayout.LayoutParams(
                -2,
                -2
            ).apply {

                topMargin = dp(8)
            }
        )

        // BOTÃO VAMOS BRINCAR

        val brincar = criarBotao(
            "Vamos brincar!"
        )

        popup.conteudo.addView(
            brincar,
            LinearLayout.LayoutParams(
                dp(215),
                dp(44)
            ).apply {

                topMargin = dp(10)
            }
        )

        brincar.setOnClickListener {

            if (navegando) {
                return@setOnClickListener
            }

            val sessao =
                LudicaSessao.obter(this)

            if (
                sessao == null ||
                sessao.second != responsavelAtual
            ) {

                fecharPopup(popup.dialog)

                carregarCriancas()

                return@setOnClickListener
            }

            navegando = true

            brincar.isEnabled = false

            selecionarCrianca(crianca)

            fecharPopup(popup.dialog)

            try {

                startActivity(
                    Intent(
                        this,
                        TelaInicialJogosGeral::class.java
                    )
                )

            } catch (_: Exception) {

                navegando = false

                Toast.makeText(
                    this,
                    "Não foi possível abrir os jogos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        exibirPopup(
            popup,
            440
        )
    }

    // ==========================================
    // GUARDAR CRIANÇA SELECIONADA
    // ==========================================

    private fun selecionarCrianca(
        crianca: ApiService.Crianca
    ) {

        val avatar =
            resources.getResourceEntryName(
                obterAvatar(crianca)
            )

        val objeto = JSONObject().apply {

            put(
                "_id",
                crianca.id
            )

            put(
                "nomeUsuario",
                nomeCrianca(crianca)
            )

            put(
                "nomeCompleto",
                crianca.nomeCompleto
            )

            put(
                "avatar",
                avatar
            )
        }

        preferencias.edit()
            .putString("responsavelDaCriancaSelecionada", LudicaSessao.obter(this)?.second)
            .putString(
                "criancaSelecionada",
                objeto.toString()
            )
            .apply()
    }

    // ==========================================
    // ESTRELINHAS MAIS LEVES
    // ==========================================

    private fun animarEstrelinhas(
        perfil: View
    ) {

        if (!telaAtiva) return

        limparEstrelinhas()

        val posicaoPerfil = IntArray(2)
        val posicaoRoot = IntArray(2)

        perfil.getLocationOnScreen(
            posicaoPerfil
        )

        root.getLocationOnScreen(
            posicaoRoot
        )

        val centroX =
            posicaoPerfil[0] -
                    posicaoRoot[0] +
                    perfil.width / 2f

        val centroY =
            posicaoPerfil[1] -
                    posicaoRoot[1] +
                    perfil.height / 2f

        val cores = listOf(
            amarelo,
            rosa,
            azul,
            azulBorda,
            rosaClaro
        )

        val quantidade = 8

        repeat(quantidade) { indice ->

            val estrela = texto(
                "★",
                20f,
                cores.random()
            ).apply {

                isClickable = false

                isFocusable = false
            }

            estrelasAtivas.add(estrela)

            val tamanho = dp(28)

            root.addView(
                estrela,
                FrameLayout.LayoutParams(
                    tamanho,
                    tamanho
                ).apply {

                    leftMargin =
                        (
                                centroX - tamanho / 2f
                                ).toInt()

                    topMargin =
                        (
                                centroY - tamanho / 2f
                                ).toInt()
                }
            )

            val angulo =
                2.0 * Math.PI *
                        indice / quantidade

            val distancia = dp(
                Random.nextInt(45, 95)
            ).toDouble()

            estrela.animate()
                .translationX(
                    (cos(angulo) * distancia)
                        .toFloat()
                )
                .translationY(
                    (sin(angulo) * distancia)
                        .toFloat()
                )
                .rotation(120f)
                .alpha(0f)
                .setDuration(450)
                .withEndAction {

                    root.removeView(estrela)

                    estrelasAtivas.remove(estrela)
                }
                .start()
        }
    }

    private fun limparEstrelinhas() {

        if (!::root.isInitialized) return

        estrelasAtivas.toList().forEach {

            it.animate().cancel()

            root.removeView(it)
        }

        estrelasAtivas.clear()
    }

    // ==========================================
    // LIBERAR RECURSOS
    // ==========================================

    override fun onDestroy() {

        telaAtiva = false

        popupAtual?.dismiss()

        popupAtual = null

        limparEstrelinhas()

        // Encerra o processamento das imagens.

        executorImagens.shutdownNow()

        super.onDestroy()
    }
}