
package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class LoginDialog(
    private val activity: Activity,
    private val aoFechar: () -> Unit = {}
) {

    // ==========================================
    // CORES
    // ==========================================

    private val azul = Color.rgb(47, 128, 237)
    private val azulSombra = Color.rgb(27, 92, 190)
    private val azulEscuro = Color.rgb(20, 48, 96)
    private val azulClaro = Color.rgb(233, 244, 255)
    private val azulMedio = Color.rgb(150, 197, 245)
    private val azulBorda = Color.rgb(200, 224, 247)
    private val azulSuave = Color.rgb(98, 125, 160)

    private val campoFundo = Color.rgb(246, 250, 255)
    private val cartaoFundo = Color.rgb(243, 248, 255)

    private val lilas = Color.rgb(240, 226, 255)
    private val roxo = Color.rgb(125, 79, 182)

    // ==========================================
    // ELEMENTOS DECORATIVOS
    // ==========================================

    private val alturaFundoDp = 84
    private val larguraLogoDp = 120
    private val alturaLogoDp = 44

    // Círculo atrás da nuvem
    private val tamanhoCirculoSuperior = 125

    // Círculo ORIGINAL atrás da girafa
    private val tamanhoCirculoGirafa = 108

    // ==========================================
    // ROLAGEM
    // ==========================================

    // Valores que você escolheu
    private val rolagemEntrar = 200
    private val rolagemCriarConta = 180

    // ==========================================
    // COMPONENTES
    // ==========================================

    private lateinit var dialog: Dialog

    private lateinit var raiz: LinearLayout
    private lateinit var scrollPrincipal: ScrollView

    private lateinit var painelEsquerdo: FrameLayout
    private lateinit var painelDireito: LinearLayout

    private lateinit var tituloEsquerda: TextView
    private lateinit var subtituloEsquerda: TextView

    private lateinit var abaEntrar: TextView
    private lateinit var abaCadastro: TextView

    private lateinit var indicador: LinearLayout
    private lateinit var conteudo: LinearLayout
    private lateinit var painelLogin: LinearLayout

    private lateinit var botaoVoltar: TextView
    private lateinit var botaoPrincipal: TextView

    private lateinit var espacoRolagemLogin: View

    private lateinit var loginEmail: EditText
    private lateinit var loginSenha: EditText

    private lateinit var cadNome: EditText
    private lateinit var cadCpf: EditText
    private lateinit var cadRg: EditText
    private lateinit var cadEmail: EditText
    private lateinit var cadCelular: EditText
    private lateinit var cadSenha: EditText

    // ==========================================
    // LISTAS
    // ==========================================

    private val etapas = mutableListOf<LinearLayout>()

    private val camposCriancas =
        mutableListOf<List<EditText>>()

    private val animacoes =
        mutableListOf<ObjectAnimator>()

    private val sonsAtivos =
        mutableListOf<MediaPlayer>()

    // ==========================================
    // ESTADOS
    // ==========================================

    private var emCadastro = false
    private var passo = 0

    private val totalPassos = 3

    private var enviandoLogin = false
    private var sucessoVisivel = false
    private var dialogFechado = false

    private var tecladoAberto = false
    private var campoAtual: EditText? = null

    private var larguraNormal = 0
    private var alturaNormal = 0

    private val subtitulosCadastro = listOf(
        "Primeiro, conte quem é o responsável",
        "Agora, como podemos falar com você?",
        "Por fim, quem vai se divertir?"
    )

    // ==========================================
    // SESSÃO
    // ==========================================

    companion object {

        private var tokenAtual: String? = null
        private var idResponsavelAtual: String? = null

        fun sessaoAtiva(): Boolean {
            return !tokenAtual.isNullOrBlank() &&
                    !idResponsavelAtual.isNullOrBlank()
        }

        fun tokenDaSessao(): String? {
            return tokenAtual
        }

        fun idDaSessao(): String? {
            return idResponsavelAtual
        }

        fun encerrarSessao() {
            tokenAtual = null
            idResponsavelAtual = null
        }
    }

    // ==========================================
    // MEDIDAS
    // ==========================================

    private fun dp(valor: Int): Int {
        return (
                valor *
                        activity.resources.displayMetrics.density
                ).toInt()
    }

    private fun redonda(negrito: Boolean): Typeface {
        return Typeface.create(
            "sans-serif-rounded",
            if (negrito) Typeface.BOLD
            else Typeface.NORMAL
        )
    }

    private fun lp(
        largura: Int,
        altura: Int,
        peso: Float = 0f
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            largura,
            altura,
            peso
        )
    }

    // ==========================================
    // FUNDOS
    // ==========================================

    private fun fundo(
        cor: Int,
        raio: Int,
        borda: Int? = null,
        larguraBorda: Int = 1
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(cor)

            cornerRadius = dp(raio).toFloat()

            if (borda != null) {
                setStroke(
                    dp(larguraBorda),
                    borda
                )
            }
        }
    }

    private fun circulo(
        cor: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(cor)
        }
    }

    private fun fundoBotao3D(
        topo: Int,
        base: Int,
        raio: Int
    ): LayerDrawable {

        return LayerDrawable(
            arrayOf(
                fundo(base, raio),
                fundo(topo, raio)
            )
        ).apply {
            setLayerInset(
                1,
                0,
                0,
                0,
                dp(4)
            )
        }
    }

    private fun fundoCampo(
        focado: Boolean
    ): GradientDrawable {

        return if (focado) {
            fundo(
                Color.WHITE,
                22,
                azul,
                2
            )
        } else {
            fundo(
                campoFundo,
                22,
                azulBorda
            )
        }
    }

    // ==========================================
    // SONS
    // ==========================================

    private fun tocarSom(
        recurso: Int,
        volume: Float
    ) {

        val player = MediaPlayer.create(
            activity,
            recurso
        ) ?: return

        sonsAtivos.add(player)

        player.setVolume(
            volume,
            volume
        )

        player.setOnCompletionListener {
            sonsAtivos.remove(it)
            it.release()
        }

        player.start()
    }

    private fun tocarClique() {
        tocarSom(
            R.raw.clique_suave,
            0.45f
        )
    }

    private fun tocarSucesso() {
        tocarSom(
            R.raw.sominicio,
            0.55f
        )
    }

    // ==========================================
    // VALIDAÇÕES
    // ==========================================

    private fun vazio(
        vararg campos: EditText
    ): Boolean {

        return campos.any {
            it.text.toString().isBlank()
        }
    }

    private fun erro(
        mensagem: String
    ) {

        ObjectAnimator.ofFloat(
            botaoPrincipal,
            View.TRANSLATION_X,
            0f,
            -dp(8).toFloat(),
            dp(8).toFloat(),
            0f
        ).setDuration(380).start()

        LudicaAvisos.mostrarErro(
            activity,
            mensagem
        )
    }

    // ==========================================
    // ANIMAÇÕES
    // ==========================================

    private fun flutuar(
        alvo: View,
        propriedade: android.util.Property<View, Float>,
        inicio: Float,
        fim: Float,
        duracao: Long
    ) {

        val animacao = ObjectAnimator.ofFloat(
            alvo,
            propriedade,
            inicio,
            fim
        ).apply {

            duration = duracao

            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            interpolator =
                AccelerateDecelerateInterpolator()
        }

        animacoes.add(animacao)
        animacao.start()
    }

    private fun animarEntrada(
        painel: ViewGroup
    ) {

        for (i in 0 until painel.childCount) {

            val filho = painel.getChildAt(i)

            filho.alpha = 0f
            filho.translationY = dp(10).toFloat()

            filho.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(i * 45L)
                .setDuration(240)
                .start()
        }
    }

    // ==========================================
    // TECLADO
    // ==========================================

    private fun esconderTeclado() {

        if (!::dialog.isInitialized) return

        val gerenciador = activity.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as? InputMethodManager

        val foco = dialog.currentFocus

        val token = foco?.windowToken
            ?: dialog.window?.decorView?.windowToken

        if (token != null) {

            gerenciador?.hideSoftInputFromWindow(
                token,
                0
            )
        }

        campoAtual?.clearFocus()
        campoAtual = null

        if (::raiz.isInitialized) {
            raiz.isFocusableInTouchMode = true
            raiz.requestFocus()
        }
    }

    // ==========================================
    // ROLAGEM - COMPRIMENTO
    // ==========================================

    private fun atualizarRolagemLogin(
        aberto: Boolean
    ) {

        if (!::espacoRolagemLogin.isInitialized) return

        val alturaExtra = when {

            !aberto -> 0

            emCadastro -> dp(rolagemCriarConta)

            else -> dp(rolagemEntrar)
        }

        val parametros =
            espacoRolagemLogin.layoutParams
                    as LinearLayout.LayoutParams

        if (parametros.height != alturaExtra) {

            parametros.height = alturaExtra
            espacoRolagemLogin.layoutParams = parametros
        }

        espacoRolagemLogin.visibility =
            if (aberto) View.VISIBLE
            else View.GONE

        raiz.requestLayout()
    }

    // ==========================================
    // DETECTAR O TECLADO
    // ==========================================

    private fun configurarRolagemLogin() {

        val janela = dialog.window ?: return

        // A janela continua com tamanho fixo.
        // Somente o espaço interno é alterado.

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            janela.decorView.setOnApplyWindowInsetsListener {
                    view, insets ->

                val aberto = insets.isVisible(
                    WindowInsets.Type.ime()
                )

                view.post {

                    if (
                        dialogFechado ||
                        sucessoVisivel ||
                        !dialog.isShowing
                    ) {
                        return@post
                    }

                    if (tecladoAberto != aberto) {

                        tecladoAberto = aberto

                        atualizarRolagemLogin(aberto)

                        // IMPORTANTE:
                        // Não chamamos nenhuma função
                        // para deslocar o formulário.

                        if (!aberto) {

                            // Ao fechar o teclado,
                            // voltamos à posição inicial.

                            scrollPrincipal.scrollTo(0, 0)
                        }
                    }
                }

                insets
            }

            janela.decorView.requestApplyInsets()

        } else {

            janela.decorView.viewTreeObserver
                .addOnGlobalLayoutListener {

                    if (
                        dialogFechado ||
                        !dialog.isShowing ||
                        sucessoVisivel
                    ) {
                        return@addOnGlobalLayoutListener
                    }

                    val area = Rect()

                    janela.decorView
                        .getWindowVisibleDisplayFrame(area)

                    val alturaTela =
                        activity.resources
                            .displayMetrics.heightPixels

                    val diferenca =
                        alturaTela - area.bottom

                    val aberto = diferenca > dp(110)

                    if (tecladoAberto != aberto) {

                        tecladoAberto = aberto

                        atualizarRolagemLogin(aberto)

                        if (!aberto) {
                            scrollPrincipal.scrollTo(0, 0)
                        }
                    }
                }
        }
    }

    // ==========================================
    // FOCO DOS CAMPOS
    // ==========================================

    private fun configurarFoco(
        campo: EditText,
        pilula: LinearLayout
    ) {

        campo.setOnFocusChangeListener { _, focado ->

            // Somente altera a borda.
            // Não move o ScrollView.

            pilula.background = fundoCampo(focado)

            if (focado) {

                campoAtual = campo

            } else if (campoAtual === campo) {

                campoAtual = null
            }
        }

        campo.setOnClickListener {

            tocarClique()

            campoAtual = campo

            // Nenhuma rolagem automática.
        }
    }

    // ==========================================
    // ABRIR POP-UP
    // ==========================================

    fun mostrar() {

        dialogFechado = false
        sucessoVisivel = false
        tecladoAberto = false

        emCadastro = false
        passo = 0
        campoAtual = null

        dialog = Dialog(activity)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        // ======================================
        // TAMANHO ORIGINAL
        // ======================================

        larguraNormal = minOf(
            dp(760),
            (
                    activity.resources.displayMetrics.widthPixels *
                            0.94f
                    ).toInt()
        )

        alturaNormal = (
                activity.resources.displayMetrics.heightPixels *
                        0.87f
                ).toInt()

        // ======================================
        // CONTEÚDO
        // ======================================

        raiz = LinearLayout(activity).apply {

            orientation = LinearLayout.HORIZONTAL

            minimumHeight = alturaNormal

            background = fundo(
                Color.WHITE,
                28
            )

            isFocusableInTouchMode = true
        }

        painelEsquerdo = montarPainelEsquerdo()
        painelDireito = montarPainelDireito()

        raiz.addView(
            painelEsquerdo,
            lp(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.8f
            )
        )

        raiz.addView(
            painelDireito,
            lp(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.25f
            )
        )

        atualizarTela(false)

        // ======================================
        // SCROLLVIEW CORRIGIDO
        // ======================================

        scrollPrincipal = object : ScrollView(activity) {

            // Impede que o ScrollView desça
            // automaticamente ao selecionar
            // um campo de texto.

            override fun requestChildFocus(
                child: View,
                focused: View
            ) {
                // Não deslocar a tela automaticamente.
            }

            override fun requestChildRectangleOnScreen(
                child: View,
                rectangle: Rect,
                immediate: Boolean
            ): Boolean {

                // Ignora solicitações automáticas
                // de movimentação por foco.

                return false
            }

        }.apply {

            isFillViewport = true

            isVerticalScrollBarEnabled = true
            isSmoothScrollingEnabled = true

            overScrollMode =
                ScrollView.OVER_SCROLL_NEVER

            background = fundo(
                Color.WHITE,
                28
            )

            clipToOutline = true

            descendantFocusability =
                ViewGroup.FOCUS_AFTER_DESCENDANTS
        }

        scrollPrincipal.addView(
            raiz,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        dialog.setContentView(scrollPrincipal)

        dialog.setOnDismissListener {

            dialogFechado = true

            animacoes.forEach {
                it.cancel()
            }

            animacoes.clear()

            sonsAtivos.toList().forEach {

                try {
                    it.release()
                } catch (_: Exception) {
                }
            }

            sonsAtivos.clear()

            aoFechar()
        }

        dialog.show()

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            setDimAmount(0.5f)

            setGravity(Gravity.CENTER)

            // NÃO ALTERA O TAMANHO DO POP-UP

            setLayout(
                larguraNormal,
                alturaNormal
            )

            // Mantém o pop-up fixo.
            // A rolagem é feita manualmente.

            setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
        }

        configurarRolagemLogin()

        raiz.requestFocus()

        // ======================================
        // ANIMAÇÃO DE ABERTURA
        // ======================================

        scrollPrincipal.alpha = 0f
        scrollPrincipal.scaleX = 0.96f
        scrollPrincipal.scaleY = 0.96f

        scrollPrincipal.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(340)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()
    }

    // ==========================================
    // ESTRELINHAS
    // ==========================================

    private fun brilho(
        texto: String,
        tamanho: Float,
        gravidade: Int,
        esq: Int,
        topo: Int,
        dir: Int,
        base: Int
    ): TextView {

        return TextView(activity).apply {

            text = texto
            textSize = tamanho
            setTextColor(Color.WHITE)
            alpha = 0.85f

            layoutParams = FrameLayout.LayoutParams(
                -2,
                -2,
                gravidade
            ).apply {

                setMargins(
                    dp(esq),
                    dp(topo),
                    dp(dir),
                    dp(base)
                )
            }
        }
    }

    // ==========================================
    // PAINEL ESQUERDO ORIGINAL
    // ==========================================

    private fun montarPainelEsquerdo(): FrameLayout {

        val painel = FrameLayout(activity).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(166, 210, 248),
                    Color.rgb(74, 149, 235)
                )
            )

            clipChildren = true
            clipToPadding = true
        }

        // ======================================
        // CÍRCULO SUPERIOR
        // ======================================

        painel.addView(
            View(activity).apply {

                background = circulo(
                    Color.argb(60, 255, 255, 255)
                )
            },
            FrameLayout.LayoutParams(
                dp(tamanhoCirculoSuperior),
                dp(tamanhoCirculoSuperior),
                Gravity.TOP or Gravity.START
            ).apply {

                setMargins(
                    -dp(40),
                    -dp(35),
                    0,
                    0
                )
            }
        )

        // ======================================
        // ESTRELINHAS
        // ======================================

        painel.addView(
            brilho(
                "✦",
                18f,
                Gravity.TOP or Gravity.END,
                0,
                20,
                24,
                0
            )
        )

        painel.addView(
            brilho(
                "✦",
                12f,
                Gravity.CENTER_VERTICAL or Gravity.START,
                16,
                0,
                0,
                40
            )
        )

        painel.addView(
            brilho(
                "✦",
                15f,
                Gravity.CENTER_VERTICAL or Gravity.END,
                0,
                0,
                16,
                40
            )
        )

        // ======================================
        // NUVEM ESQUERDA
        // ======================================

        val nuvemA = ImageView(activity).apply {

            setImageResource(
                R.drawable.nuveminicio
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER

            alpha = 0.95f
        }

        painel.addView(
            nuvemA,
            FrameLayout.LayoutParams(
                dp(70),
                dp(58),
                Gravity.TOP or Gravity.START
            ).apply {

                setMargins(
                    dp(6),
                    dp(49),
                    0,
                    0
                )
            }
        )

        // ======================================
        // NUVEM DIREITA
        // ======================================

        val nuvemB = ImageView(activity).apply {

            setImageResource(
                R.drawable.nuveminicio
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER

            alpha = 0.9f
        }

        painel.addView(
            nuvemB,
            FrameLayout.LayoutParams(
                dp(62),
                dp(52),
                Gravity.TOP or Gravity.END
            ).apply {

                setMargins(
                    0,
                    dp(58),
                    dp(6),
                    0
                )
            }
        )

        flutuar(
            nuvemA,
            View.TRANSLATION_X,
            -dp(6).toFloat(),
            dp(6).toFloat(),
            3600L
        )

        flutuar(
            nuvemB,
            View.TRANSLATION_X,
            dp(6).toFloat(),
            -dp(6).toFloat(),
            4200L
        )

        // ======================================
        // ARCO-ÍRIS
        // ======================================

        painel.addView(
            ImageView(activity).apply {

                setImageResource(
                    R.drawable.rainbow
                )

                scaleType =
                    ImageView.ScaleType.FIT_CENTER

                alpha = 0.45f
            },
            FrameLayout.LayoutParams(
                dp(150),
                dp(92),
                Gravity.BOTTOM or Gravity.END
            ).apply {

                rightMargin = -dp(14)
                bottomMargin = dp(40)
            }
        )

        // ======================================
        // MONTANHAS E GRAMA
        // ======================================

        painel.addView(
            ImageView(activity).apply {

                setImageResource(
                    R.drawable.gramainicio
                )

                scaleType =
                    ImageView.ScaleType.FIT_XY
            },
            FrameLayout.LayoutParams(
                -1,
                dp(alturaFundoDp),
                Gravity.BOTTOM
            )
        )

        // ======================================
        // LOGO INFERIOR
        // ======================================

        painel.addView(
            ImageView(activity).apply {

                setImageResource(
                    R.drawable.logoinferior
                )

                scaleType =
                    ImageView.ScaleType.FIT_CENTER
            },
            FrameLayout.LayoutParams(
                dp(larguraLogoDp),
                dp(alturaLogoDp),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {

                bottomMargin = dp(1)
            }
        )

        // ======================================
        // GIRAFINHA
        // ======================================

        val coluna = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                dp(14),
                dp(28),
                dp(14),
                dp(alturaFundoDp)
            )
        }

        painel.addView(
            coluna,
            FrameLayout.LayoutParams(
                -1,
                -1
            )
        )

        val palco = FrameLayout(activity)

        // Círculo central original de 108dp

        palco.addView(
            View(activity).apply {

                background = circulo(
                    Color.argb(95, 255, 255, 255)
                )
            },
            FrameLayout.LayoutParams(
                dp(tamanhoCirculoGirafa),
                dp(tamanhoCirculoGirafa),
                Gravity.CENTER
            )
        )

        // Imagem da girafa

        palco.addView(
            ImageView(activity).apply {

                setImageResource(
                    R.drawable.girafamao
                )

                scaleType =
                    ImageView.ScaleType.FIT_CENTER
            },
            FrameLayout.LayoutParams(
                dp(124),
                dp(112),
                Gravity.CENTER
            )
        )

        coluna.addView(
            palco,
            lp(
                dp(136),
                dp(120)
            )
        )

        flutuar(
            palco,
            View.TRANSLATION_Y,
            0f,
            -dp(5).toFloat(),
            1500L
        )

        // ======================================
        // TÍTULO
        // ======================================

        tituloEsquerda = TextView(activity).apply {

            textSize = 24f

            gravity = Gravity.CENTER

            typeface = redonda(true)

            setTextColor(Color.WHITE)

            setShadowLayer(
                dp(3).toFloat(),
                0f,
                dp(1).toFloat(),
                Color.argb(80, 10, 40, 100)
            )
        }

        coluna.addView(
            tituloEsquerda,
            lp(-1, -2).apply {
                topMargin = dp(6)
            }
        )

        // ======================================
        // SUBTÍTULO
        // ======================================

        subtituloEsquerda = TextView(activity).apply {

            textSize = 12.5f

            gravity = Gravity.CENTER

            typeface = redonda(false)

            setTextColor(Color.WHITE)

            alpha = 0.92f
        }

        coluna.addView(
            subtituloEsquerda,
            lp(-1, -2).apply {
                topMargin = dp(2)
            }
        )

        return painel
    }

    // ==========================================
    // ABAS
    // ==========================================

    private fun criarAba(
        texto: String
    ): TextView {

        return TextView(activity).apply {

            this.text = texto

            textSize = 13.5f
            gravity = Gravity.CENTER
            typeface = redonda(true)

            isClickable = true
            isFocusable = true
        }
    }

    private fun estilizarAba(
        aba: TextView,
        ativa: Boolean
    ) {

        aba.background = fundo(
            if (ativa) azul
            else Color.TRANSPARENT,
            22
        )

        aba.setTextColor(
            if (ativa) Color.WHITE
            else azulSuave
        )

        aba.elevation =
            if (ativa) dp(2).toFloat()
            else 0f
    }

    // ==========================================
    // BOTÕES
    // ==========================================

    private fun criarBotao(
        titulo: String,
        secundario: Boolean,
        acao: () -> Unit
    ): TextView {

        return TextView(activity).apply {

            text = titulo

            textSize = 15f

            gravity = Gravity.CENTER

            typeface = redonda(true)

            setTextColor(
                if (secundario) azul
                else Color.WHITE
            )

            background =
                if (secundario) {

                    fundoBotao3D(
                        Color.WHITE,
                        azulMedio,
                        22
                    )

                } else {

                    fundoBotao3D(
                        azul,
                        azulSombra,
                        22
                    )
                }

            setPadding(
                0,
                0,
                0,
                dp(4)
            )

            isClickable = true
            isFocusable = true

            setOnClickListener {

                if (!isEnabled) {
                    return@setOnClickListener
                }

                isEnabled = false

                tocarClique()

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
    }

    // ==========================================
    // PAINEL DIREITO
    // ==========================================

    private fun montarPainelDireito(): LinearLayout {

        val painel = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(20),
                dp(12),
                dp(20),
                dp(18)
            )

            minimumHeight = alturaNormal

            background = ColorDrawable(Color.WHITE)
        }

        // ======================================
        // ABAS CENTRALIZADAS
        // ======================================

        val topo = FrameLayout(activity)

        val barraAbas = LinearLayout(activity).apply {

            orientation = LinearLayout.HORIZONTAL

            setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
            )

            background = fundo(
                azulClaro,
                25,
                azulBorda
            )
        }

        abaEntrar = criarAba("Entrar")
        abaCadastro = criarAba("Criar conta")

        barraAbas.addView(
            abaEntrar,
            lp(0, -1, 1f)
        )

        barraAbas.addView(
            abaCadastro,
            lp(0, -1, 1f)
        )

        topo.addView(
            barraAbas,
            FrameLayout.LayoutParams(
                dp(220),
                dp(40),
                Gravity.CENTER
            )
        )

        // ======================================
        // BOTÃO FECHAR
        // ======================================

        val fechar = TextView(activity).apply {

            text = "✕"

            textSize = 13f

            gravity = Gravity.CENTER

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(azulSuave)

            background = circulo(azulClaro)

            isClickable = true
            isFocusable = true

            contentDescription = "Fechar"

            setOnClickListener {

                if (
                    !enviandoLogin &&
                    !sucessoVisivel
                ) {

                    tocarClique()
                    esconderTeclado()
                    dialog.dismiss()
                }
            }
        }

        topo.addView(
            fechar,
            FrameLayout.LayoutParams(
                dp(36),
                dp(36),
                Gravity.END or Gravity.CENTER_VERTICAL
            )
        )

        painel.addView(
            topo,
            lp(-1, dp(44))
        )

        // ======================================
        // TROCAR ABAS
        // ======================================

        abaEntrar.setOnClickListener {

            if (
                emCadastro &&
                !enviandoLogin
            ) {

                tocarClique()
                esconderTeclado()

                emCadastro = false
                atualizarTela(true)
            }
        }

        abaCadastro.setOnClickListener {

            if (
                !emCadastro &&
                !enviandoLogin
            ) {

                tocarClique()
                esconderTeclado()

                emCadastro = true
                atualizarTela(true)
            }
        }

        // ======================================
        // INDICADOR DE ETAPAS
        // ======================================

        indicador = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        painel.addView(
            indicador,
            lp(-1, -2).apply {
                topMargin = dp(8)
            }
        )

        // ======================================
        // FORMULÁRIOS
        // ======================================

        conteudo = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                0,
                dp(6),
                0,
                dp(8)
            )
        }

        painel.addView(
            conteudo,
            lp(-1, -2).apply {
                topMargin = dp(14)
            }
        )

        montarLogin()
        montarEtapasCadastro()

        // ======================================
        // BOTÕES INFERIORES
        // ======================================

        val rodape = LinearLayout(activity).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER_VERTICAL
        }

        botaoVoltar = criarBotao(
            "← Voltar",
            true
        ) {

            esconderTeclado()

            if (passo > 0) {

                passo--
                atualizarTela(true)
            }
        }

        botaoPrincipal = criarBotao(
            "Entrar",
            false
        ) {

            acaoPrincipal()
        }

        rodape.addView(
            botaoVoltar,
            lp(0, dp(50), 0.7f).apply {

                rightMargin = dp(10)
            }
        )

        rodape.addView(
            botaoPrincipal,
            lp(0, dp(50), 1f)
        )

        painel.addView(
            rodape,
            lp(-1, -2).apply {

                topMargin = dp(18)
                bottomMargin = dp(12)
            }
        )

        // ======================================
        // ESPAÇO DE ROLAGEM
        // ======================================

        espacoRolagemLogin = View(activity)

        painel.addView(
            espacoRolagemLogin,
            lp(-1, 0)
        )

        return painel
    }

    // ==========================================
    // TÍTULOS DOS FORMULÁRIOS
    // ==========================================

    private fun textoEtapa(
        pai: LinearLayout,
        texto: String
    ) {

        pai.addView(
            TextView(activity).apply {

                this.text = texto

                textSize = 15f

                typeface = redonda(true)

                setTextColor(azulEscuro)
            },
            lp(-1, -2).apply {

                topMargin = dp(10)
                leftMargin = dp(4)
            }
        )
    }

    // ==========================================
    // LINHA DE CAMPOS
    // ==========================================

    private fun criarLinha(
        pai: LinearLayout
    ): LinearLayout {

        val linha = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        pai.addView(
            linha,
            lp(-1, -2).apply {
                topMargin = dp(10)
            }
        )

        return linha
    }

    // ==========================================
    // CAMPOS DE PREENCHIMENTO
    // ==========================================

    private fun criarCampo(
        pai: LinearLayout,
        icone: String,
        dica: String,
        tipo: Int = InputType.TYPE_CLASS_TEXT,
        ehSenha: Boolean = false
    ): EditText {

        val pilula = LinearLayout(activity).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            background = fundoCampo(false)
        }

        if (
            pai.orientation ==
            LinearLayout.HORIZONTAL
        ) {

            pai.addView(
                pilula,
                lp(0, dp(46), 1f).apply {

                    if (pai.childCount > 0) {
                        leftMargin = dp(8)
                    }
                }
            )

        } else {

            pai.addView(
                pilula,
                lp(-1, dp(46)).apply {
                    topMargin = dp(10)
                }
            )
        }

        // ======================================
        // ÍCONE
        // ======================================

        val emoji = TextView(activity).apply {

            text = icone
            textSize = 14f
            gravity = Gravity.CENTER

            background = circulo(azulClaro)
        }

        pilula.addView(
            emoji,
            lp(dp(29), dp(29)).apply {
                leftMargin = dp(7)
            }
        )

        // ======================================
        // CAMPO EDITÁVEL
        // ======================================

        val entrada = EditText(activity).apply {

            hint = dica

            textSize = 14f

            inputType = tipo
            typeface = redonda(false)

            setTextColor(azulEscuro)

            setHintTextColor(
                Color.rgb(150, 168, 190)
            )

            isSingleLine = true

            imeOptions =
                EditorInfo.IME_ACTION_NEXT or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI

            background = null

            setPadding(
                dp(6),
                0,
                dp(6),
                0
            )
        }

        pilula.addView(
            entrada,
            lp(0, -1, 1f)
        )

        configurarFoco(entrada, pilula)

        // ======================================
        // MOSTRAR / OCULTAR SENHA
        // ======================================

        if (ehSenha) {

            val olho = TextView(activity).apply {

                text = "👁"

                textSize = 16f

                gravity = Gravity.CENTER

                setPadding(
                    dp(8),
                    0,
                    dp(14),
                    0
                )

                isClickable = true

                contentDescription =
                    "Mostrar ou ocultar senha"
            }

            olho.setOnClickListener {

                tocarClique()

                val estavaOculta =
                    entrada.transformationMethod != null

                val posicao = entrada.selectionStart

                entrada.inputType =
                    if (estavaOculta) {

                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                    } else {

                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                entrada.typeface = redonda(false)

                olho.text =
                    if (estavaOculta) "🙈"
                    else "👁"

                entrada.setSelection(
                    posicao.coerceIn(
                        0,
                        entrada.text.length
                    )
                )
            }

            pilula.addView(olho)
        }

        return entrada
    }

    // ==========================================
    // FORMULÁRIO DE LOGIN
    // ==========================================

    private fun montarLogin() {

        painelLogin = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        conteudo.addView(painelLogin)

        textoEtapa(
            painelLogin,
            "Acesse sua conta"
        )

        loginEmail = criarCampo(
            painelLogin,
            "✉️",
            "voce@email.com",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        loginSenha = criarCampo(
            painelLogin,
            "🔒",
            "Sua senha",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
            true
        )

        // ======================================
        // ESQUECI A SENHA
        // ======================================

        val recuperar = TextView(activity).apply {

            text = "Esqueci a senha"

            textSize = 12f

            gravity = Gravity.END

            typeface = redonda(true)

            setTextColor(azul)

            paintFlags =
                paintFlags or
                        Paint.UNDERLINE_TEXT_FLAG

            setPadding(
                0,
                dp(4),
                dp(6),
                dp(4)
            )

            isClickable = true

            setOnClickListener {

                tocarClique()

                LudicaAvisos.mostrarErro(
                    activity,
                    "Recuperação de senha ainda não disponível."
                )
            }
        }

        painelLogin.addView(
            recuperar,
            lp(-1, -2).apply {
                topMargin = dp(10)
            }
        )
    }

    // ==========================================
    // CADASTRO EM TRÊS ETAPAS
    // ==========================================

    private fun novaEtapa(
        titulo: String
    ): LinearLayout {

        val etapa = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        textoEtapa(etapa, titulo)

        etapas.add(etapa)

        conteudo.addView(etapa)

        return etapa
    }

    private fun montarEtapasCadastro() {

        // ======================================
        // ETAPA 1
        // ======================================

        val e1 = novaEtapa(
            "Dados do responsável"
        )

        cadNome = criarCampo(
            e1,
            "👤",
            "Nome completo"
        )

        cadCpf = criarCampo(
            e1,
            "🪪",
            "CPF",
            InputType.TYPE_CLASS_NUMBER
        )

        cadRg = criarCampo(
            e1,
            "📄",
            "RG"
        )

        // ======================================
        // ETAPA 2
        // ======================================

        val e2 = novaEtapa(
            "Contato e senha"
        )

        cadEmail = criarCampo(
            e2,
            "✉️",
            "voce@email.com",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        cadCelular = criarCampo(
            e2,
            "📱",
            "Celular com DDD",
            InputType.TYPE_CLASS_PHONE
        )

        cadSenha = criarCampo(
            e2,
            "🔒",
            "Crie uma senha",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
            true
        )

        // ======================================
        // ETAPA 3
        // ======================================

        val e3 = novaEtapa(
            "Quem vai jogar?"
        )

        val areaCriancas = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        e3.addView(areaCriancas)

        adicionarCrianca(areaCriancas)

        val adicionar = criarBotao(
            "+ Adicionar outra criança",
            false
        ) {

            adicionarCrianca(areaCriancas)
        }

        e3.addView(
            adicionar,
            lp(-1, dp(46)).apply {
                topMargin = dp(16)
                bottomMargin = dp(14)
            }
        )

        etapas.forEach {
            it.visibility = View.GONE
        }
    }

    // ==========================================
    // CRIANÇAS
    // ==========================================

    private fun adicionarCrianca(
        area: LinearLayout
    ) {

        val cartao = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(14)
            )

            background = fundo(
                cartaoFundo,
                20,
                azulBorda
            )
        }

        area.addView(
            cartao,
            lp(-1, -2).apply {
                topMargin = dp(12)
            }
        )

        val selo = TextView(activity).apply {

            text = "Criança ${camposCriancas.size + 1}"

            textSize = 12f

            typeface = redonda(true)

            setTextColor(Color.WHITE)

            setPadding(
                dp(14),
                dp(4),
                dp(14),
                dp(4)
            )

            background = fundo(azul, 14)
        }

        cartao.addView(
            selo,
            lp(-2, -2)
        )

        val nome = criarCampo(
            cartao,
            "🧒",
            "Nome completo da criança"
        )

        val linha = criarLinha(cartao)

        val cpf = criarCampo(
            linha,
            "🪪",
            "CPF",
            InputType.TYPE_CLASS_NUMBER
        )

        val usuario = criarCampo(
            linha,
            "⭐",
            "Apelido"
        )

        val nascimento = criarCampo(
            cartao,
            "🎂",
            "Nascimento (DD/MM/AAAA)",
            InputType.TYPE_CLASS_DATETIME or
                    InputType.TYPE_DATETIME_VARIATION_DATE
        )

        camposCriancas.add(
            listOf(
                nome,
                cpf,
                usuario,
                nascimento
            )
        )

        cartao.alpha = 0f

        cartao.animate()
            .alpha(1f)
            .setDuration(260)
            .start()
    }

    // ==========================================
    // GLITTER
    // ==========================================

    private fun criarGlitter(
        camada: FrameLayout
    ) {

        val centroX = camada.width / 2f
        val centroY = camada.height / 2f

        val cores = listOf(
            Color.rgb(205, 154, 255),
            Color.rgb(250, 178, 229),
            Color.WHITE,
            Color.rgb(255, 221, 132),
            Color.rgb(176, 206, 255)
        )

        repeat(75) { indice ->

            val estrela = TextView(activity).apply {

                text =
                    if (indice % 3 == 0) {
                        "✦"
                    } else {
                        "✧"
                    }

                textSize =
                    Random.nextInt(
                        12,
                        26
                    ).toFloat()

                setTextColor(
                    cores.random()
                )

                gravity = Gravity.CENTER

                elevation = dp(15).toFloat()
            }

            val tamanho = dp(32)

            camada.addView(
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
                indice * Math.PI * 2.0 / 75 +
                        Random.nextDouble(-0.18, 0.18)

            val distancia =
                dp(
                    Random.nextInt(70, 255)
                ).toFloat()

            estrela.scaleX = 0.15f
            estrela.scaleY = 0.15f

            estrela.animate()
                .translationX(
                    (cos(angulo) * distancia).toFloat()
                )
                .translationY(
                    (sin(angulo) * distancia).toFloat()
                )
                .rotation(
                    Random.nextInt(-160, 160).toFloat()
                )
                .alpha(0f)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setStartDelay(
                    Random.nextLong(0, 330)
                )
                .setDuration(
                    Random.nextLong(850, 1750)
                )
                .withEndAction {
                    camada.removeView(estrela)
                }
                .start()
        }
    }

    // ==========================================
    // LOGIN REALIZADO COM SUCESSO
    // ==========================================

    private fun mostrarSucessoLogin() {

        if (
            sucessoVisivel ||
            !dialog.isShowing
        ) return

        sucessoVisivel = true

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        esconderTeclado()

        tocarSucesso()

        val camada = FrameLayout(activity).apply {

            background = ColorDrawable(
                Color.argb(
                    165,
                    32,
                    27,
                    67
                )
            )

            isClickable = true
            clipChildren = false
            clipToPadding = false
        }

        val cartao = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
            )

            background = fundo(
                lilas,
                24,
                Color.rgb(205, 170, 244),
                2
            )

            elevation = dp(12).toFloat()
        }

        cartao.addView(
            TextView(activity).apply {

                text = "✦  ✨  ✦"

                textSize = 25f
                gravity = Gravity.CENTER

                setTextColor(roxo)
            }
        )

        cartao.addView(
            TextView(activity).apply {

                text = "Login realizado com sucesso!"

                textSize = 19f

                typeface = redonda(true)
                gravity = Gravity.CENTER

                setTextColor(
                    Color.rgb(105, 65, 158)
                )
            }
        )

        cartao.addView(
            TextView(activity).apply {

                text = "Que bom ter você por aqui! 💜"

                textSize = 12f
                gravity = Gravity.CENTER

                setTextColor(
                    Color.rgb(137, 101, 175)
                )
            }
        )

        camada.addView(
            cartao,
            FrameLayout.LayoutParams(
                dp(300),
                dp(135),
                Gravity.CENTER
            )
        )

        dialog.setContentView(camada)

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            setGravity(Gravity.CENTER)

            setLayout(
                larguraNormal,
                alturaNormal
            )
        }

        camada.post {

            criarGlitter(camada)

            camada.postDelayed({

                if (
                    !activity.isFinishing &&
                    !activity.isDestroyed &&
                    dialog.isShowing
                ) {

                    dialog.dismiss()
                }

            }, 2100L)
        }
    }

    // ==========================================
    // LOGIN COM RAILWAY
    // ==========================================

    private fun realizarLogin() {

        if (
            vazio(
                loginEmail,
                loginSenha
            )
        ) {

            erro("Preencha o e-mail e a senha.")
            return
        }

        if (enviandoLogin) return

        enviandoLogin = true

        botaoPrincipal.text = "Entrando..."
        botaoPrincipal.isEnabled = false

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val email =
            loginEmail.text.toString().trim()

        val senha =
            loginSenha.text.toString()

        ApiService.login(
            email,
            senha
        ) { resultado ->

            enviandoLogin = false

            if (
                activity.isFinishing ||
                activity.isDestroyed ||
                !dialog.isShowing
            ) {

                return@login
            }

            botaoPrincipal.isEnabled = true
            botaoPrincipal.text = "Entrar"

            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(false)

            resultado.onSuccess { dados ->

                tokenAtual = dados.token

                idResponsavelAtual =
                    dados.responsavel.id

                mostrarSucessoLogin()
            }

            resultado.onFailure { falha ->

                erro(
                    falha.message
                        ?: "Não foi possível entrar. Tente novamente."
                )
            }
        }
    }

    // ==========================================
    // BOTÃO PRINCIPAL
    // ==========================================

    private fun acaoPrincipal() {

        if (!emCadastro) {

            realizarLogin()
            return
        }

        when (passo) {

            0 -> {

                if (
                    vazio(
                        cadNome,
                        cadCpf,
                        cadRg
                    )
                ) {

                    erro(
                        "Preencha todos os campos desta etapa."
                    )

                } else {

                    esconderTeclado()

                    passo = 1
                    atualizarTela(true)
                }
            }

            1 -> {

                if (
                    vazio(
                        cadEmail,
                        cadCelular,
                        cadSenha
                    )
                ) {

                    erro(
                        "Preencha todos os campos desta etapa."
                    )

                } else {

                    esconderTeclado()

                    passo = 2
                    atualizarTela(true)
                }
            }

            else -> {

                val incompleto =
                    camposCriancas.flatten().any {
                        it.text.toString().isBlank()
                    }

                if (incompleto) {

                    erro(
                        "Preencha todos os campos da criança."
                    )

                } else {

                    erro(
                        "Cadastro ainda não conectado à API."
                    )
                }
            }
        }
    }

    // ==========================================
    // INDICADOR DE ETAPAS
    // ==========================================

    private fun atualizarIndicador() {

        indicador.removeAllViews()

        val rotulo = TextView(activity).apply {

            text = "Etapa ${passo + 1} de $totalPassos"

            textSize = 11f

            typeface = redonda(true)

            setTextColor(azulSuave)
        }

        indicador.addView(
            rotulo,
            lp(-2, -2).apply {
                leftMargin = dp(4)
            }
        )

        val barras = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        for (i in 0 until totalPassos) {

            val barra = View(activity).apply {

                background = fundo(
                    if (i <= passo) azul
                    else azulBorda,
                    3
                )
            }

            barras.addView(
                barra,
                lp(0, dp(6), 1f).apply {

                    if (i > 0) {
                        leftMargin = dp(6)
                    }
                }
            )
        }

        indicador.addView(
            barras,
            lp(-1, -2).apply {
                topMargin = dp(5)
            }
        )
    }

    // ==========================================
    // ATUALIZAR INTERFACE
    // ==========================================

    private fun atualizarTela(
        animar: Boolean
    ) {

        estilizarAba(
            abaEntrar,
            !emCadastro
        )

        estilizarAba(
            abaCadastro,
            emCadastro
        )

        painelLogin.visibility =
            if (emCadastro) {
                View.GONE
            } else {
                View.VISIBLE
            }

        etapas.forEachIndexed { i, etapa ->

            etapa.visibility =
                if (
                    emCadastro &&
                    i == passo
                ) {

                    View.VISIBLE

                } else {

                    View.GONE
                }
        }

        indicador.visibility =
            if (emCadastro) {
                View.VISIBLE
            } else {
                View.GONE
            }

        atualizarIndicador()

        botaoVoltar.visibility =
            if (
                emCadastro &&
                passo > 0
            ) {

                View.VISIBLE

            } else {

                View.GONE
            }

        botaoPrincipal.text = when {

            !emCadastro -> "Entrar"

            passo < totalPassos - 1 ->
                "Próximo →"

            else -> "Cadastrar"
        }

        if (emCadastro) {

            tituloEsquerda.text =
                "Vamos criar sua conta!"

            subtituloEsquerda.text =
                subtitulosCadastro[passo]

        } else {

            tituloEsquerda.text =
                "Oi, que bom te ver!"

            subtituloEsquerda.text =
                "Entre para continuar a diversão"
        }

        if (::espacoRolagemLogin.isInitialized) {
            atualizarRolagemLogin(tecladoAberto)
        }

        painelDireito.requestLayout()
        raiz.requestLayout()

        if (::scrollPrincipal.isInitialized) {

            scrollPrincipal.post {
                scrollPrincipal.scrollTo(0, 0)
            }
        }

        if (animar) {

            val painel =
                if (emCadastro) {
                    etapas[passo]
                } else {
                    painelLogin
                }

            animarEntrada(painel)
        }
    }
}
