
package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class LoginDialog(
    private val activity: Activity,
    private val aoFechar: () -> Unit = {}
) {

    // CORES
    private val azul = Color.rgb(47, 128, 237)
    private val azulSombra = Color.rgb(27, 92, 190)
    private val azulEscuro = Color.rgb(20, 48, 96)
    private val azulClaro = Color.rgb(233, 244, 255)
    private val azulMedio = Color.rgb(150, 197, 245)
    private val azulBorda = Color.rgb(200, 224, 247)
    private val azulSuave = Color.rgb(98, 125, 160)
    private val campoFundo = Color.rgb(246, 250, 255)
    private val cartaoFundo = Color.rgb(243, 248, 255)

    // IMAGENS INFERIORES
    private val alturaFundoDp = 84
    private val larguraLogoDp = 120
    private val alturaLogoDp = 44

    // POSIÇÃO DA LOGO
    private val margemInferiorLogoDp = 8

    // ELEMENTOS
    private lateinit var dialog: Dialog
    private lateinit var tituloEsquerda: TextView
    private lateinit var subtituloEsquerda: TextView
    private lateinit var abaEntrar: TextView
    private lateinit var abaCadastro: TextView
    private lateinit var indicador: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var conteudo: LinearLayout
    private lateinit var painelLogin: LinearLayout
    private lateinit var botaoVoltar: TextView
    private lateinit var botaoPrincipal: TextView

    private val etapas = mutableListOf<LinearLayout>()
    private val camposCriancas = mutableListOf<List<EditText>>()
    private val animacoes = mutableListOf<ObjectAnimator>()
    private val sonsAtivos = mutableListOf<MediaPlayer>()

    private lateinit var loginEmail: EditText
    private lateinit var loginSenha: EditText

    private lateinit var cadNome: EditText
    private lateinit var cadCpf: EditText
    private lateinit var cadRg: EditText
    private lateinit var cadEmail: EditText
    private lateinit var cadCelular: EditText
    private lateinit var cadSenha: EditText

    private var emCadastro = false
    private var passo = 0
    private val totalPassos = 3

    private val subtitulosCadastro = listOf(
        "Primeiro, conte quem é o responsável",
        "Agora, como podemos falar com você?",
        "Por fim, quem vai se divertir?"
    )

    // ======================================
    // UTILIDADES
    // ======================================

    private fun dp(valor: Int): Int =
        (valor * activity.resources.displayMetrics.density).toInt()

    private fun redonda(negrito: Boolean): Typeface =
        Typeface.create(
            "sans-serif-rounded",
            if (negrito) Typeface.BOLD else Typeface.NORMAL
        )

    private fun fonteTitulo(): Typeface = redonda(true)

    private fun lp(
        largura: Int,
        altura: Int,
        peso: Float = 0f
    ) = LinearLayout.LayoutParams(largura, altura, peso)

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
                setStroke(dp(larguraBorda), borda)
            }
        }
    }

    private fun circulo(cor: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(cor)
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
            setLayerInset(1, 0, 0, 0, dp(4))
        }
    }

    private fun fundoCampo(foco: Boolean): GradientDrawable =
        if (foco) {
            fundo(Color.WHITE, 22, azul, 2)
        } else {
            fundo(campoFundo, 22, azulBorda, 1)
        }

    // ======================================
    // SOM SUAVE
    // ======================================

    private fun tocarClique() {
        val player = MediaPlayer.create(
            activity,
            R.raw.clique_suave
        ) ?: return

        sonsAtivos.add(player)
        player.setVolume(0.45f, 0.45f)

        player.setOnCompletionListener {
            sonsAtivos.remove(it)
            it.release()
        }

        player.start()
    }

    private fun vazio(vararg campos: EditText): Boolean =
        campos.any { it.text.toString().isBlank() }

    private fun balancar(alvo: View) {
        val distancia = dp(8).toFloat()

        ObjectAnimator.ofFloat(
            alvo,
            View.TRANSLATION_X,
            0f,
            -distancia,
            distancia,
            -distancia * 0.6f,
            distancia * 0.6f,
            0f
        ).setDuration(380).start()
    }

    private fun erro(mensagem: String) {
        balancar(botaoPrincipal)
        LudicaAvisos.mostrarErro(activity, mensagem)
    }

    private fun painelAtual(): ViewGroup =
        if (!emCadastro) painelLogin else etapas[passo]

    private fun animarEntrada(painel: ViewGroup) {
        for (i in 0 until painel.childCount) {
            val filho = painel.getChildAt(i)

            filho.alpha = 0f
            filho.translationY = dp(12).toFloat()

            filho.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(i * 55L)
                .setDuration(260)
                .start()
        }
    }

    // ======================================
    // ABRIR POP-UP
    // ======================================

    fun mostrar() {

        dialog = Dialog(activity)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val raiz = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            background = fundo(Color.WHITE, 28)
            clipToOutline = true
        }

        raiz.addView(
            montarPainelEsquerdo(),
            lp(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.8f
            )
        )

        raiz.addView(
            montarPainelDireito(),
            lp(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.25f
            )
        )

        atualizarTela(animar = false)

        dialog.setContentView(raiz)

        dialog.setOnDismissListener {
            animacoes.forEach { it.cancel() }
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

        val largura =
            activity.resources.displayMetrics.widthPixels

        val altura =
            activity.resources.displayMetrics.heightPixels

        dialog.window?.apply {
            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            setDimAmount(0.5f)
            setGravity(Gravity.CENTER)

            setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )

            setLayout(
                minOf(
                    dp(760),
                    (largura * 0.94f).toInt()
                ),
                (altura * 0.92f).toInt()
            )
        }

        raiz.alpha = 0f
        raiz.scaleX = 0.92f
        raiz.scaleY = 0.92f

        raiz.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(340)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()

        scroll.post {
            animarEntrada(painelAtual())
        }
    }

    // ======================================
    // BRILHOS
    // ======================================

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
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
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

    private fun flutuar(
        alvo: View,
        propriedade: android.util.Property<View, Float>,
        de: Float,
        ate: Float,
        duracao: Long
    ) {

        val animacao = ObjectAnimator.ofFloat(
            alvo,
            propriedade,
            de,
            ate
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

    // ======================================
    // PAINEL ESQUERDO
    // ======================================

    private fun montarPainelEsquerdo(): FrameLayout {

        val painel = FrameLayout(activity).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(166, 210, 248),
                    Color.rgb(74, 149, 235)
                )
            )
        }

        // LUZ SUAVE
        val halo = View(activity).apply {
            background = circulo(
                Color.argb(60, 255, 255, 255)
            )
        }

        painel.addView(
            halo,
            FrameLayout.LayoutParams(
                dp(190),
                dp(190),
                Gravity.TOP or Gravity.START
            ).apply {
                setMargins(
                    -dp(70),
                    -dp(70),
                    0,
                    0
                )
            }
        )

        // BRILHINHOS DECORATIVOS
        painel.addView(
            brilho(
                "✦", 18f,
                Gravity.TOP or Gravity.END,
                0, 20, 24, 0
            )
        )

        painel.addView(
            brilho(
                "✦", 12f,
                Gravity.CENTER_VERTICAL or Gravity.START,
                16, 0, 0, 40
            )
        )

        painel.addView(
            brilho(
                "✦", 15f,
                Gravity.CENTER_VERTICAL or Gravity.END,
                0, 0, 16, 40
            )
        )

        // NUVEM ESQUERDA
        val nuvemA = ImageView(activity).apply {
            setImageResource(R.drawable.nuveminicio)
            scaleType = ImageView.ScaleType.FIT_CENTER
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
                    dp(76),
                    0,
                    0
                )
            }
        )

        // NUVEM DIREITA
        val nuvemB = ImageView(activity).apply {
            setImageResource(R.drawable.nuveminicio)
            scaleType = ImageView.ScaleType.FIT_CENTER
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
        // GRAMA E MORRINHOS
        // ======================================

        val fundoInicio = ImageView(activity).apply {
            setImageResource(R.drawable.gramainicio)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        painel.addView(
            fundoInicio,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(alturaFundoDp),
                Gravity.BOTTOM
            )
        )

        // ======================================
        // LOGO INFERIOR — DESCIDA
        // ======================================

        val logo = ImageView(activity).apply {
            setImageResource(R.drawable.logoinferior)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Lúdica Kids"
        }

        painel.addView(
            logo,
            FrameLayout.LayoutParams(
                dp(larguraLogoDp),
                dp(alturaLogoDp),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {

                // Quanto menor, mais perto da borda inferior.
                bottomMargin = dp(margemInferiorLogoDp)
            }
        )

        // ======================================
        // CONTEÚDO CENTRAL
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
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // FUNDO CIRCULAR DA GIRAFA
        val palco = FrameLayout(activity)

        val luz = View(activity).apply {
            background = circulo(
                Color.argb(95, 255, 255, 255)
            )
        }

        palco.addView(
            luz,
            FrameLayout.LayoutParams(
                dp(108),
                dp(108),
                Gravity.CENTER
            )
        )

        // GIRAFINHA ORIGINAL
        val girafa = ImageView(activity).apply {
            setImageResource(R.drawable.girafamao)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Girafinha Lúdica Kids"
        }

        palco.addView(
            girafa,
            FrameLayout.LayoutParams(
                dp(124),
                dp(112),
                Gravity.CENTER
            )
        )

        coluna.addView(
            palco,
            lp(dp(136), dp(120))
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
            typeface = fonteTitulo()
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
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        subtituloEsquerda = TextView(activity).apply {
            textSize = 12.5f
            gravity = Gravity.CENTER
            typeface = redonda(false)
            setTextColor(Color.WHITE)
            alpha = 0.92f
        }

        coluna.addView(
            subtituloEsquerda,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(2)
            }
        )

        return painel
    }

    // ======================================
    // PAINEL DIREITO
    // ======================================

    private fun montarPainelDireito(): LinearLayout {

        val painel = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(20),
                dp(16),
                dp(20),
                dp(16)
            )
        }

        // ABAS E FECHAR
        val topo = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

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
            lp(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        barraAbas.addView(
            abaCadastro,
            lp(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        topo.addView(
            barraAbas,
            lp(0, dp(42), 1f)
        )

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
                tocarClique()
                dialog.dismiss()
            }
        }

        topo.addView(
            fechar,
            lp(dp(36), dp(36)).apply {
                leftMargin = dp(10)
            }
        )

        painel.addView(
            topo,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        abaEntrar.setOnClickListener {
            if (emCadastro) {
                tocarClique()
                emCadastro = false
                atualizarTela(true)
            }
        }

        abaCadastro.setOnClickListener {
            if (!emCadastro) {
                tocarClique()
                emCadastro = true
                atualizarTela(true)
            }
        }

        // INDICADOR DE ETAPAS
        indicador = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        painel.addView(
            indicador,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        // ROLAGEM
        scroll = ScrollView(activity).apply {
            isVerticalScrollBarEnabled = false
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
        }

        conteudo = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(
            conteudo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        painel.addView(
            scroll,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        montarLogin()
        montarEtapasCadastro()

        // RODAPÉ FIXO
        val rodape = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        botaoVoltar = criarBotao(
            "← Voltar",
            secundario = true
        ) {
            if (passo > 0) {
                passo--
                atualizarTela(true)
            }
        }

        botaoPrincipal = criarBotao(
            "Entrar",
            secundario = false
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
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        return painel
    }

    // ======================================
    // ABAS E BOTÕES
    // ======================================

    private fun criarAba(texto: String): TextView =
        TextView(activity).apply {
            this.text = texto
            textSize = 13.5f
            gravity = Gravity.CENTER
            typeface = redonda(true)
            isClickable = true
            isFocusable = true
        }

    private fun estilizarAba(
        aba: TextView,
        ativa: Boolean
    ) {
        aba.background = fundo(
            if (ativa) azul else Color.TRANSPARENT,
            22
        )

        aba.setTextColor(
            if (ativa) Color.WHITE else azulSuave
        )

        aba.elevation =
            if (ativa) dp(2).toFloat() else 0f
    }

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
                if (secundario) azul else Color.WHITE
            )

            background = if (secundario) {
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

            setPadding(0, 0, 0, dp(4))

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

    // ======================================
    // TEXTOS E CAMPOS
    // ======================================

    private fun textoEtapa(
        pai: LinearLayout,
        texto: String
    ) {

        val titulo = TextView(activity).apply {
            this.text = texto
            textSize = 15f
            typeface = redonda(true)
            setTextColor(azulEscuro)
        }

        pai.addView(
            titulo,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
                leftMargin = dp(4)
            }
        )
    }

    private fun criarLinha(
        pai: LinearLayout
    ): LinearLayout {

        val linha = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        pai.addView(
            linha,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        return linha
    }

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

        if (pai.orientation == LinearLayout.HORIZONTAL) {
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
                lp(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(46)
                ).apply {
                    topMargin = dp(10)
                }
            )
        }

        val emoji = TextView(activity).apply {
            text = icone
            textSize = 14f
            gravity = Gravity.CENTER
            background = circulo(azulClaro)
        }

        pilula.addView(
            emoji,
            lp(dp(32), dp(32)).apply {
                leftMargin = dp(7)
            }
        )

        val entrada = EditText(activity).apply {
            hint = dica
            textSize = 14f
            typeface = redonda(false)

            setTextColor(azulEscuro)
            setHintTextColor(
                Color.rgb(150, 168, 190)
            )

            inputType = tipo
            isSingleLine = true

            imeOptions =
                EditorInfo.IME_ACTION_NEXT or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI

            background = null

            setPadding(
                dp(10),
                0,
                dp(10),
                0
            )
        }

        pilula.addView(
            entrada,
            lp(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        entrada.setOnFocusChangeListener { _, temFoco ->
            pilula.background = fundoCampo(temFoco)
        }

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
                contentDescription = "Mostrar ou ocultar senha"
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
                    if (estavaOculta) "🙈" else "👁"

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

    // ======================================
    // FORMULÁRIO DE LOGIN
    // ======================================

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

        val recuperar = TextView(activity).apply {
            text = "Esqueci a senha"
            textSize = 12f
            gravity = Gravity.END
            typeface = redonda(true)
            setTextColor(azul)

            paintFlags =
                paintFlags or Paint.UNDERLINE_TEXT_FLAG

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
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )
    }

    // ======================================
    // CADASTRO EM ETAPAS
    // ======================================

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

        // ETAPA 1: RESPONSÁVEL
        val e1 = novaEtapa(
            "Dados do responsável"
        )

        cadNome = criarCampo(
            e1, "👤", "Nome completo"
        )

        cadCpf = criarCampo(
            e1,
            "🪪",
            "CPF",
            InputType.TYPE_CLASS_NUMBER
        )

        cadRg = criarCampo(
            e1, "📄", "RG"
        )

        // ETAPA 2: CONTATO E SENHA
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

        // ETAPA 3: CRIANÇAS
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
            secundario = true
        ) {

            adicionarCrianca(areaCriancas)

            val ultimo = areaCriancas.getChildAt(
                areaCriancas.childCount - 1
            )

            scroll.post {
                scroll.smoothScrollTo(
                    0,
                    ultimo.top
                )
            }
        }

        e3.addView(
            adicionar,
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                topMargin = dp(14)
                bottomMargin = dp(4)
            }
        )

        etapas.forEach {
            it.visibility = View.GONE
        }
    }

    // ======================================
    // ADICIONAR CRIANÇA
    // ======================================

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
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
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
            lp(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val nome = criarCampo(
            cartao,
            "🧒",
            "Nome completo da criança"
        )

        val linha1 = criarLinha(cartao)

        val cpf = criarCampo(
            linha1,
            "🪪",
            "CPF",
            InputType.TYPE_CLASS_NUMBER
        )

        val usuario = criarCampo(
            linha1,
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

    // ======================================
    // AÇÃO DO BOTÃO PRINCIPAL
    // ======================================

    private fun acaoPrincipal() {

        if (!emCadastro) {

            if (vazio(loginEmail, loginSenha)) {
                erro("Preencha o e-mail e a senha.")
            } else {
                erro("Login ainda não conectado à API.")
            }

            return
        }

        when (passo) {

            0 -> {
                if (vazio(cadNome, cadCpf, cadRg)) {
                    erro(
                        "Preencha todos os campos desta etapa."
                    )
                } else {
                    passo = 1
                    atualizarTela(true)
                }
            }

            1 -> {
                if (vazio(
                        cadEmail,
                        cadCelular,
                        cadSenha
                    )
                ) {
                    erro(
                        "Preencha todos os campos desta etapa."
                    )
                } else {
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
                    // Integração com Railway pendente.
                    erro(
                        "Cadastro ainda não conectado à API."
                    )
                }
            }
        }
    }

    // ======================================
    // INDICADOR DAS ETAPAS
    // ======================================

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
            lp(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = dp(4)
            }
        )

        val barras = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        for (i in 0 until totalPassos) {

            val barra = View(activity).apply {
                background = fundo(
                    if (i <= passo) azul else azulBorda,
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
            lp(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        )
    }

    // ======================================
    // ATUALIZAR TELA
    // ======================================

    private fun atualizarTela(animar: Boolean) {

        estilizarAba(
            abaEntrar,
            !emCadastro
        )

        estilizarAba(
            abaCadastro,
            emCadastro
        )

        painelLogin.visibility =
            if (!emCadastro) View.VISIBLE else View.GONE

        etapas.forEachIndexed { i, etapa ->
            etapa.visibility =
                if (emCadastro && i == passo) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        indicador.visibility =
            if (emCadastro) View.VISIBLE else View.GONE

        atualizarIndicador()

        botaoVoltar.visibility =
            if (emCadastro && passo > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

        botaoPrincipal.text = when {
            !emCadastro -> "Entrar"
            passo < totalPassos - 1 -> "Próximo →"
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

        scroll.post {
            scroll.scrollTo(0, 0)
        }

        if (animar) {
            animarEntrada(painelAtual())
        }
    }
}
