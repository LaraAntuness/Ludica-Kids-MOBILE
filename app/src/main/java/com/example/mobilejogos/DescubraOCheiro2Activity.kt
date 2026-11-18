package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class DescubraOCheiro2Activity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var jogoFinalizado = false
    private var popupAberto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_descubra_o_cheiro2)

        tela = findViewById(R.id.telaDescubraCheiro2)

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // CORREÇÃO DOS TOQUES NOS ALIMENTOS
    // =====================================================

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        if (
            ::tela.isInitialized &&
            tela.width > 0 &&
            tela.height > 0 &&
            !jogoFinalizado &&
            !popupAberto
        ) {

            val localizacao = IntArray(2)

            tela.getLocationOnScreen(localizacao)

            val sx = tela.width.toFloat() / larguraBase
            val sy = tela.height.toFloat() / alturaBase

            val toqueX = (event.rawX - localizacao[0]) / sx
            val toqueY = (event.rawY - localizacao[1]) / sy

            val chocolate = dentroDaArea(
                toqueX, toqueY,
                395f, 105f, 155f, 100f
            )

            val pao = dentroDaArea(
                toqueX, toqueY,
                600f, 105f, 155f, 100f
            )

            val maracuja = dentroDaArea(
                toqueX, toqueY,
                395f, 245f, 155f, 100f
            )

            val batataFrita = dentroDaArea(
                toqueX, toqueY,
                600f, 245f, 155f, 100f
            )

            val tocouAlimento =
                chocolate || pao || maracuja || batataFrita

            if (tocouAlimento) {

                if (event.actionMasked == MotionEvent.ACTION_UP) {

                    if (maracuja) {
                        mostrarResposta(true)
                    } else {
                        mostrarResposta(false)
                    }
                }

                return true
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun dentroDaArea(
        toqueX: Float,
        toqueY: Float,
        x: Float,
        y: Float,
        largura: Float,
        altura: Float
    ): Boolean {

        return toqueX >= x &&
                toqueX <= x + largura &&
                toqueY >= y &&
                toqueY <= y + altura
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        jogoFinalizado = false
        popupAberto = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // =================================================
        // PLANO DE FUNDO
        // =================================================

        val fundo = ImageView(this)

        fundo.setImageResource(R.drawable.planodescubracomocheiro)

        fundo.scaleType = ImageView.ScaleType.FIT_XY

        fundo.isClickable = false

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornorosa",
            18, 25, 40, 39,
            sx, sy
        )

        voltar.isClickable = true
        voltar.contentDescription = "Voltar"

        voltar.setOnClickListener {
            finish()
        }

        // =================================================
        // TÍTULO
        // =================================================

        adicionarImagem(
            "titulodescubracomocheirorosa",
            207, 10, 465, 80,
            sx, sy
        )

        // =================================================
        // MENINA ROSA
        // =================================================

        adicionarImagem(
            "meninarosa",
            55, 190, 150, 210,
            sx, sy
        )

        // =================================================
        // BALÃO ROSA
        // =================================================

        criarBalao(sx, sy)

        // =================================================
        // CHOCOLATE
        // =================================================

        criarOpcao(
            "chocolate",
            395, 105,
            sx, sy
        )

        // =================================================
        // PÃO
        // =================================================

        criarOpcao(
            "pao",
            600, 105,
            sx, sy
        )

        // =================================================
        // MARACUJÁ - CORRETO
        // =================================================

        criarOpcao(
            "maracuja",
            395, 245,
            sx, sy
        )

        // =================================================
        // BATATA FRITA
        // =================================================

        criarOpcao(
            "batatafrita",
            600, 245,
            sx, sy
        )
    }

    // =====================================================
    // BALÃO ROSA
    // =====================================================

    private fun criarBalao(
        sx: Float,
        sy: Float
    ) {

        val balao = adicionarImagem(
            "balaopergunta",
            85, 80, 340, 135,
            sx, sy
        )

        // MUDA O BALÃO PARA ROSA-CLARO

        balao.setColorFilter(
            Color.parseColor("#F4BBD3"),
            PorterDuff.Mode.SRC_IN
        )

        // PERGUNTA

        val pergunta = TextView(this)

        pergunta.text = "Qual alimento tem um\ncheiro azedinho?"

        pergunta.gravity = Gravity.CENTER

        pergunta.setTextColor(
            Color.parseColor("#503344")
        )

        pergunta.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            18f * sy
        )

        pergunta.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        pergunta.maxLines = 2

        adicionarView(
            pergunta,
            125, 108, 235, 48,
            sx, sy
        )
    }

    // =====================================================
    // CRIAR CAIXINHA DOS ALIMENTOS
    // =====================================================

    private fun criarOpcao(
        nomeImagem: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float
    ) {

        // SOMBRA

        val sombra = View(this)

        sombra.background = GradientDrawable().apply {

            setColor(
                Color.parseColor("#E8E7E7")
            )

            cornerRadius = 17f * sx
        }

        adicionarView(
            sombra,
            x + 13, y + 13,
            155, 100,
            sx, sy
        )

        // CARTÃO ROSA

        val cartao = FrameLayout(this)

        cartao.background = GradientDrawable().apply {

            setColor(
                Color.parseColor("#F3B8D0")
            )

            cornerRadius = 17f * sx
        }

        adicionarView(
            cartao,
            x, y,
            155, 100,
            sx, sy
        )

        // IMAGEM

        val alimento = ImageView(this)

        val idImagem = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            alimento.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro2",
                "Imagem não encontrada: $nomeImagem"
            )
        }

        alimento.scaleType =
            ImageView.ScaleType.FIT_CENTER

        alimento.contentDescription = nomeImagem

        alimento.isClickable = false

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt().coerceAtLeast(1),
            (84 * sy).toInt().coerceAtLeast(1),
            Gravity.CENTER
        )

        cartao.addView(
            alimento,
            parametrosImagem
        )

        // TOQUE ADICIONAL NO CARTÃO
        // O dispatchTouchEvent TAMBÉM RECONHECE A ÁREA

        cartao.isClickable = true

        cartao.setOnClickListener {

            if (!jogoFinalizado && !popupAberto) {

                mostrarResposta(
                    nomeImagem == "maracuja"
                )
            }
        }
    }

    // =====================================================
    // RESPOSTA DA CRIANÇA
    // =====================================================

    private fun mostrarResposta(
        correta: Boolean
    ) {

        if (jogoFinalizado || popupAberto) {
            return
        }

        if (correta) {

            jogoFinalizado = true

            mostrarPopupParabens()

        } else {

            Toast.makeText(
                this,
                "Tente novamente!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // POP-UP DE PARABÉNS
    // =====================================================

    private fun mostrarPopupParabens() {

        if (popupAberto) {
            return
        }

        popupAberto = true

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // CAMADA ESCURA

        val camadaEscura = FrameLayout(this)

        camadaEscura.setBackgroundColor(
            Color.parseColor("#99000000")
        )

        camadaEscura.isClickable = true

        tela.addView(
            camadaEscura,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // CAIXA DO POP-UP

        val popup = FrameLayout(this)

        popup.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#FFF9F3"),
                Color.parseColor("#FFE7EF")
            )
        ).apply {

            cornerRadius = 28f * sx

            setStroke(
                (4 * sx).toInt().coerceAtLeast(1),
                Color.parseColor("#F5B6D0")
            )
        }

        popup.elevation = 15f * sx

        val parametrosPopup = FrameLayout.LayoutParams(
            (540 * sx).toInt(),
            (270 * sy).toInt(),
            Gravity.CENTER
        )

        camadaEscura.addView(
            popup,
            parametrosPopup
        )

        // ANIMAÇÃO

        popup.scaleX = 0.5f
        popup.scaleY = 0.5f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(450)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .start()

        // =================================================
        // DECORAÇÃO
        // =================================================

        val estrela1 = TextView(this).apply {

            text = "★"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#F4BE63")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                38f * sy
            )
        }

        adicionarViewNoPopup(
            popup,
            estrela1,
            35, 20, 45, 45,
            sx, sy
        )

        val estrela2 = TextView(this).apply {

            text = "★"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#F4BE63")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                38f * sy
            )
        }

        adicionarViewNoPopup(
            popup,
            estrela2,
            460, 20, 45, 45,
            sx, sy
        )

        // =================================================
        // TÍTULO
        // =================================================

        val titulo = TextView(this).apply {

            text = "Parabéns, explorador!"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#D56B99")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                31f * sy
            )

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )
        }

        adicionarViewNoPopup(
            popup,
            titulo,
            35, 58, 470, 50,
            sx, sy
        )

        // =================================================
        // MENSAGEM ANIMADA
        // =================================================

        val mensagem = TextView(this).apply {

            text = "Muito bem! O maracujá tem\num cheirinho azedinho!"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#604423")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                23f * sy
            )

            typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

            alpha = 0f

            translationY = 15f * sy
        }

        adicionarViewNoPopup(
            popup,
            mensagem,
            40, 111, 460, 63,
            sx, sy
        )

        mensagem.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(350)
            .setDuration(650)
            .start()

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        criarBotaoPopup(
            popup,
            "← Voltar",
            48, 195, 195, 52,
            "#83BDEB",
            sx, sy
        ) {

            finish()
        }

        // =================================================
        // BOTÃO PRÓXIMA FASE
        // =================================================

        criarBotaoPopup(
            popup,
            "Próxima fase →",
            265, 195, 225, 52,
            "#8ACF8B",
            sx, sy
        ) {

            abrirProximaFase()
        }
    }

    // =====================================================
    // BOTÕES DO POP-UP
    // =====================================================

    private fun criarBotaoPopup(
        popup: FrameLayout,
        texto: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        cor: String,
        sx: Float,
        sy: Float,
        acao: () -> Unit
    ) {

        val botao = TextView(this)

        botao.text = texto

        botao.gravity = Gravity.CENTER

        botao.setTextColor(Color.WHITE)

        botao.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            19f * sy
        )

        botao.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        botao.background = GradientDrawable().apply {

            setColor(Color.parseColor(cor))

            cornerRadius = 18f * sx
        }

        botao.isClickable = true

        botao.setOnClickListener {
            acao()
        }

        adicionarViewNoPopup(
            popup,
            botao,
            x, y, largura, altura,
            sx, sy
        )
    }

    // =====================================================
    // ABRIR NÍVEL 3
    // =====================================================

    private fun abrirProximaFase() {

        try {

            val classeProximaFase = Class.forName(
                "$packageName.DescubraOCheiro3Activity"
            )

            val intent = Intent(
                this,
                classeProximaFase
            )

            startActivity(intent)

            finish()

        } catch (erro: Exception) {

            Log.e(
                "DescubraOCheiro2",
                "Erro ao abrir o nível 3",
                erro
            )

            Toast.makeText(
                this,
                "A próxima fase ainda não está disponível!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =====================================================
    // ADICIONAR IMAGENS
    // =====================================================

    private fun adicionarImagem(
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ): ImageView {

        val imagem = ImageView(this)

        val idImagem = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            imagem.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro2",
                "Imagem não encontrada: $nome"
            )

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_LONG
            ).show()
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        imagem.isClickable = false

        adicionarView(
            imagem,
            x, y, largura, altura,
            sx, sy
        )

        return imagem
    }

    // =====================================================
    // ADICIONAR ELEMENTOS NA TELA
    // =====================================================

    private fun adicionarView(
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(
            view,
            parametros
        )
    }

    // =====================================================
    // ADICIONAR ELEMENTOS NO POP-UP
    // =====================================================

    private fun adicionarViewNoPopup(
        popup: FrameLayout,
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        popup.addView(
            view,
            parametros
        )
    }
}