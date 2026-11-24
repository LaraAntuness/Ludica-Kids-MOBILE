
package com.example.mobilejogos

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
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

    private val amarelo = Color.rgb(255, 186, 19)
    private val verdeEscuro = Color.rgb(56, 75, 40)
    private val verdeClaro = Color.rgb(237, 245, 223)
    private val verdeBorda = Color.rgb(208, 224, 190)

    private lateinit var dialog: Dialog
    private lateinit var formulario: LinearLayout
    private lateinit var abaEntrar: TextView
    private lateinit var abaCadastro: TextView
    private lateinit var scroll: ScrollView

    private var emCadastro = false

    private fun dp(valor: Int): Int {
        return (
                valor * activity.resources.displayMetrics.density
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

    private fun tocarClique() {
        val player = MediaPlayer.create(
            activity,
            R.raw.clique_suave
        ) ?: return

        player.setVolume(0.45f, 0.45f)
        player.setOnCompletionListener {
            it.release()
        }
        player.start()
    }

    // ======================================
    // ABRIR POP-UP
    // ======================================

    fun mostrar() {

        dialog = Dialog(activity)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val principal = FrameLayout(activity).apply {
            clipChildren = false
            clipToPadding = false
        }

        scroll = ScrollView(activity).apply {
            isVerticalScrollBarEnabled = false
            isFillViewport = false
            clipToPadding = false
            clipChildren = false
        }

        principal.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val conteudo = FrameLayout(activity).apply {
            clipChildren = false
            clipToPadding = false
        }

        scroll.addView(
            conteudo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // ======================================
        // CARD BRANCO CENTRAL
        // ======================================

        // ======================================
// MOLDURA ARREDONDADA ATRÁS DO POP-UP
// ======================================

        val moldura = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(7),
                dp(7),
                dp(7),
                dp(9)
            )

            background = GradientDrawable().apply {

                setColor(
                    Color.rgb(207, 229, 238)
                )

                cornerRadius = dp(34).toFloat()

                setStroke(
                    dp(2),
                    Color.rgb(174, 207, 215)
                )
            }

            elevation = dp(6).toFloat()
        }

// POSICIONAMENTO DA MOLDURA
        conteudo.addView(
            moldura,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = dp(95)
                bottomMargin = dp(10)
                leftMargin = dp(10)
                rightMargin = dp(10)
            }
        )

// ======================================
// CARD BRANCO DA FRENTE
// ======================================

        val card = LinearLayout(activity).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(20),
                dp(25),
                dp(20),
                dp(20)
            )

            background = GradientDrawable().apply {

                setColor(Color.WHITE)

                cornerRadius = dp(28).toFloat()
            }
        }

// COLOCA O CARD DENTRO DA MOLDURA
        moldura.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        // ======================================
        // ABAS
        // ======================================

        val barraAbas = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            background = fundo(verdeClaro, 25)
        }

        card.addView(
            barraAbas,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        abaEntrar = criarAba("Entrar")
        abaCadastro = criarAba("Criar conta")

        barraAbas.addView(
            abaEntrar,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        barraAbas.addView(
            abaCadastro,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        formulario = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        card.addView(
            formulario,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        // ======================================
        // GIRAFINHA ORIGINAL SOBRE O CARD
        // ======================================

        val girafa = ImageView(activity).apply {
            setImageResource(R.drawable.girafamao)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Girafinha Lúdica Kids"
            elevation = dp(8).toFloat()
        }

        conteudo.addView(
            girafa,
            FrameLayout.LayoutParams(
                dp(135),
                dp(125),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            )
        )

        // ======================================
        // CLIQUES DAS ABAS
        // ======================================

        abaEntrar.setOnClickListener {
            tocarClique()
            trocarFormulario(false)
        }

        abaCadastro.setOnClickListener {
            tocarClique()
            trocarFormulario(true)
        }

        trocarFormulario(false)

        dialog.setContentView(principal)

        dialog.setOnDismissListener {
            aoFechar()
        }

        dialog.show()

        val largura = activity.resources.displayMetrics.widthPixels
        val altura = activity.resources.displayMetrics.heightPixels

        dialog.window?.apply {
            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            setDimAmount(0.48f)
            setGravity(Gravity.CENTER)

            setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )

            setLayout(
                minOf(dp(460), (largura * 0.86f).toInt()),
                (altura * 0.90f).toInt()
            )
        }

        // ENTRADA SUAVE
        principal.alpha = 0f
        principal.scaleX = 0.94f
        principal.scaleY = 0.94f

        principal.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(260)
            .start()
    }

    // ======================================
    // COMPONENTES REUTILIZÁVEIS
    // ======================================

    private fun criarAba(textoAba: String): TextView {
        return TextView(activity).apply {
            text = textoAba
            textSize = 13f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(verdeEscuro)
            isClickable = true
        }
    }

    private fun trocarFormulario(cadastro: Boolean) {
        emCadastro = cadastro

        abaEntrar.background = fundo(
            if (!cadastro) amarelo else verdeClaro,
            22
        )

        abaCadastro.background = fundo(
            if (cadastro) amarelo else verdeClaro,
            22
        )

        formulario.removeAllViews()

        if (cadastro) {
            montarCadastro()
        } else {
            montarLogin()
        }

        scroll.post {
            scroll.scrollTo(0, 0)
        }
    }

    private fun textoCentral(
        mensagem: String,
        tamanho: Float,
        negrito: Boolean = false
    ) {
        val texto = TextView(activity).apply {
            text = mensagem
            textSize = tamanho
            setTextColor(verdeEscuro)
            gravity = Gravity.CENTER

            if (negrito) {
                typeface = Typeface.create(
                    "sans-serif-rounded",
                    Typeface.BOLD
                )
            }
        }

        formulario.addView(
            texto,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(7)
            }
        )
    }

    private fun criarCampo(
        rotulo: String,
        dica: String,
        tipo: Int = InputType.TYPE_CLASS_TEXT,
        ehSenha: Boolean = false
    ): EditText {

        val tituloCampo = TextView(activity).apply {
            text = rotulo
            textSize = 12f
            setTextColor(verdeEscuro)
            typeface = Typeface.DEFAULT_BOLD
        }

        formulario.addView(
            tituloCampo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        val linha = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = fundo(Color.WHITE, 13, verdeBorda)
        }

        formulario.addView(
            linha,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(43)
            ).apply {
                topMargin = dp(4)
            }
        )

        val entrada = EditText(activity).apply {
            hint = dica
            textSize = 13f
            setTextColor(verdeEscuro)
            setHintTextColor(Color.rgb(145, 158, 139))
            inputType = tipo
            isSingleLine = true
            background = null
            setPadding(dp(12), 0, dp(8), 0)
        }

        linha.addView(
            entrada,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        if (ehSenha) {
            val mostrar = TextView(activity).apply {
                text = "Mostrar"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(verdeEscuro)
                setPadding(dp(7), 0, dp(10), 0)
                isClickable = true
            }

            mostrar.setOnClickListener {
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

                mostrar.text =
                    if (estavaOculta) "Ocultar" else "Mostrar"

                entrada.setSelection(
                    posicao.coerceIn(0, entrada.text.length)
                )
            }

            linha.addView(mostrar)
        }

        return entrada
    }

    private fun criarBotao(
        titulo: String,
        acao: () -> Unit
    ) {
        val botao = TextView(activity).apply {
            text = titulo
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(verdeEscuro)
            background = fundo(amarelo, 22)
            isClickable = true
            elevation = dp(2).toFloat()

            setOnClickListener {
                tocarClique()
                acao()
            }
        }

        formulario.addView(
            botao,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
            ).apply {
                topMargin = dp(15)
            }
        )
    }

    // ======================================
    // FORMULÁRIO DE LOGIN
    // ======================================

    private fun montarLogin() {

        textoCentral("Oi, que bom te ver!", 22f, true)
        textoCentral("Entre para continuar...", 12f)

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
            true
        )

        val recuperar = TextView(activity).apply {
            text = "Esqueci a senha"
            textSize = 12f
            gravity = Gravity.END
            setTextColor(verdeEscuro)
            paintFlags =
                paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

            setOnClickListener {
                tocarClique()

                LudicaAvisos.mostrarErro(
                    activity,
                    "Recuperação de senha ainda não disponível."
                )
            }
        }

        formulario.addView(
            recuperar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        criarBotao("Entrar") {
            if (
                email.text.toString().trim().isBlank() ||
                senha.text.toString().isBlank()
            ) {
                LudicaAvisos.mostrarErro(
                    activity,
                    "Preencha o e-mail e a senha."
                )
            } else {
                LudicaAvisos.mostrarErro(
                    activity,
                    "Login ainda não conectado à API."
                )
            }
        }
    }

    // ======================================
    // FORMULÁRIO DE CADASTRO
    // ======================================

    private fun montarCadastro() {

        textoCentral("Vamos criar sua conta!", 21f, true)
        textoCentral("Dados do responsável", 12f)

        val nome = criarCampo("Nome completo", "Nome do responsável")
        val cpf = criarCampo(
            "CPF", "CPF do responsável",
            InputType.TYPE_CLASS_NUMBER
        )
        val rg = criarCampo("RG", "RG do responsável")
        val email = criarCampo(
            "E-mail", "voce@email.com",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        val celular = criarCampo(
            "Celular", "Telefone com DDD",
            InputType.TYPE_CLASS_PHONE
        )
        val senha = criarCampo(
            "Senha do responsável", "Crie uma senha",
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
            true
        )

        // A senha da criança será adicionada após
        // confirmarmos sua implementação no backend.

        textoCentral("Dados da criança", 18f, true)

        val camposCriancas = mutableListOf<List<EditText>>()

        fun adicionarCrianca() {
            textoCentral(
                "Criança ${camposCriancas.size + 1}",
                13f,
                true
            )

            val nomeCrianca = criarCampo(
                "Nome completo", "Nome da criança"
            )
            val cpfCrianca = criarCampo(
                "CPF", "CPF da criança",
                InputType.TYPE_CLASS_NUMBER
            )
            val usuario = criarCampo(
                "Nome de usuário", "Apelido"
            )
            val nascimento = criarCampo(
                "Data de nascimento", "DD/MM/AAAA"
            )

            camposCriancas.add(
                listOf(
                    nomeCrianca, cpfCrianca,
                    usuario, nascimento
                )
            )
        }

        adicionarCrianca()

        criarBotao("+ Adicionar criança") {
            adicionarCrianca()
        }

        criarBotao("Cadastrar") {
            val campos = listOf(
                nome, cpf, rg, email, celular, senha
            ) + camposCriancas.flatten()

            if (campos.any {
                    it.text.toString().trim().isBlank()
                }) {
                LudicaAvisos.mostrarErro(
                    activity,
                    "Preencha todos os campos."
                )
            } else {
                LudicaAvisos.mostrarErro(
                    activity,
                    "Cadastro ainda não conectado à API."
                )
            }
        }
    }
}
