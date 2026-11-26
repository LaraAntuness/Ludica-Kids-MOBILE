package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

/** Calculadora infantil de CPF. A confirmação é feita exclusivamente no backend. */
class CalculadoraPaisDialog(
    private val activity: Activity,
    private val aoFechar: () -> Unit = {},
    private val aoConfirmar: () -> Unit
) {
    // Use aqui a MESMA URL base HTTPS usada no ApiService.kt do aplicativo.
    private val urlBase = "https://ludicakids-backend.up.railway.app"

    // Paleta fofinha
    private val rosaClaro = Color.rgb(255, 240, 246)
    private val cremeSuave = Color.rgb(255, 248, 228)
    private val rosaBorda = Color.rgb(255, 184, 208)
    private val rosa = Color.rgb(255, 128, 171)
    private val rosaSombra = Color.rgb(222, 88, 138)
    private val pessego = Color.rgb(255, 214, 178)
    private val branco = Color.WHITE
    private val roxo = Color.rgb(98, 52, 120)
    private val roxoSuave = Color.rgb(150, 112, 170)
    private val azul = Color.rgb(96, 190, 255)
    private val cinzaPonto = Color.rgb(240, 226, 232)
    private val vermelho = Color.rgb(205, 55, 90)

    // Degradê do cabeçalho, usado também em todos os botões
    private val degradeEsq = Color.rgb(255, 205, 224)
    private val degradeDir = pessego
    private val baseBotao = Color.rgb(238, 163, 176)

    private lateinit var dialog: Dialog
    private lateinit var visor: TextView
    private lateinit var informacao: TextView
    private lateinit var botaoConfirmar: TextView
    private lateinit var superficie: FrameLayout
    private lateinit var mascote: TextView

    private val pontos = mutableListOf<View>()
    private val teclas = mutableListOf<View>()
    private val cpf = StringBuilder()
    private var conferindo = false
    private var confirmado = false
    private var som: MediaPlayer? = null
    private var balanco: ObjectAnimator? = null

    private fun dp(v: Int) = (v * activity.resources.displayMetrics.density).toInt()

    private fun fundo(cor: Int, raio: Int, borda: Int? = null, espessura: Int = 2) =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = dp(raio).toFloat()
            if (borda != null) setStroke(dp(espessura), borda)
        }

    private fun gradiente(
        c1: Int, c2: Int, raio: Int = 0, borda: Int? = null,
        orientacao: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM
    ) = GradientDrawable(orientacao, intArrayOf(c1, c2)).apply {
        cornerRadius = dp(raio).toFloat()
        if (borda != null) setStroke(dp(3), borda)
    }

    /** Botão "de doce" com efeito 3D: base + face com o degradê do cabeçalho. */
    private fun doce(raio: Int) =
        LayerDrawable(arrayOf(
            fundo(baseBotao, raio),
            gradiente(degradeEsq, degradeDir, raio, null, GradientDrawable.Orientation.LEFT_RIGHT)
        )).apply {
            setLayerInset(1, 0, 0, 0, dp(4))
        }

    private fun oval(cor: Int, borda: Int? = null) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(cor)
        if (borda != null) setStroke(dp(2), borda)
    }

    private fun texto(valor: String, tamanho: Float, cor: Int, negrito: Boolean = true) =
        TextView(activity).apply {
            text = valor
            textSize = tamanho
            gravity = Gravity.CENTER
            setTextColor(cor)
            typeface = Typeface.create(
                "sans-serif-rounded",
                if (negrito) Typeface.BOLD else Typeface.NORMAL
            )
            includeFontPadding = false
        }

    private fun clique() {
        try {
            if (som == null) som = MediaPlayer.create(activity, R.raw.clique_suave)
            som?.let { it.seekTo(0); it.start() }
        } catch (_: Exception) { }
    }

    private fun digitar(n: String) {
        if (conferindo || confirmado || cpf.length == 11) return
        clique()
        cpf.append(n)
        atualizarVisor()
        // Visor "pula" a cada número
        visor.animate().cancel()
        visor.animate().scaleX(1.08f).scaleY(1.08f).setDuration(70).withEndAction {
            visor.animate().scaleX(1f).scaleY(1f).setDuration(180)
                .setInterpolator(OvershootInterpolator(3f)).start()
        }.start()
        // Bolinha que acabou de acender dá um "pop"
        pontos.getOrNull(cpf.length - 1)?.let { p ->
            p.scaleX = 0.3f; p.scaleY = 0.3f
            p.animate().scaleX(1f).scaleY(1f).setDuration(260)
                .setInterpolator(OvershootInterpolator(4f)).start()
        }
        if (cpf.length == 11) pulsarBotao()
    }

    private fun atualizarVisor() {
        val s = cpf.toString()
        val mascarado = buildString {
            s.forEachIndexed { indice, c ->
                if (indice == 3 || indice == 6) append('.')
                if (indice == 9) append('-')
                append(c)
            }
        }
        visor.text = if (s.isEmpty()) "000.000.000-00" else mascarado
        visor.alpha = if (s.isEmpty()) 0.3f else 1f
        informacao.text = "${s.length} de 11 dígitos"
        informacao.setTextColor(roxoSuave)
        mascote.text = if (s.length == 11) "😍" else "🐻"
        pontos.forEachIndexed { i, p ->
            p.background = oval(if (i < s.length) rosa else cinzaPonto)
        }
    }

    private fun pulsarBotao() {
        botaoConfirmar.animate().scaleX(1.07f).scaleY(1.07f).setDuration(140).withEndAction {
            botaoConfirmar.animate().scaleX(1f).scaleY(1f).setDuration(280)
                .setInterpolator(OvershootInterpolator(3f)).start()
        }.start()
    }

    private fun animarPressao(v: View, aoTerminar: () -> Unit) {
        v.animate().scaleX(0.88f).scaleY(0.88f).translationY(dp(3).toFloat())
            .setDuration(80)
            .withEndAction {
                v.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(220)
                    .setInterpolator(OvershootInterpolator(3.2f))
                    .withEndAction(aoTerminar).start()
            }.start()
    }

    /** Coraçõezinhos e estrelinhas que sobem quando a tecla é tocada. */
    private fun brotinhos(origem: View) {
        val a = IntArray(2); val b = IntArray(2)
        origem.getLocationOnScreen(a); superficie.getLocationOnScreen(b)
        val cx = a[0] - b[0] + origem.width / 2
        val cy = a[1] - b[1] + origem.height / 2
        repeat(3) {
            val p = texto(listOf("💗", "✨", "⭐", "🌸").random(), 12f, roxo)
            superficie.addView(p, FrameLayout.LayoutParams(dp(22), dp(22)).apply {
                leftMargin = cx - dp(11); topMargin = cy - dp(11)
            })
            p.animate()
                .translationY(-Random.nextInt(dp(35), dp(70)).toFloat())
                .translationX(Random.nextInt(-dp(24), dp(24)).toFloat())
                .rotation(Random.nextInt(-40, 40).toFloat())
                .alpha(0f).setDuration(650)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { superficie.removeView(p) }.start()
        }
    }

    private fun criarTecla(simbolo: String, indice: Int): View {
        val apagar = simbolo == "⌫"
        return texto(simbolo, if (apagar) 21f else 25f, roxo).apply {
            background = doce(20)
            setPadding(0, 0, 0, dp(4))
            elevation = dp(3).toFloat()
            contentDescription = if (apagar) "Apagar último número" else "Número $simbolo"
            setOnClickListener {
                if (conferindo || confirmado) return@setOnClickListener
                brotinhos(this)
                animarPressao(this) {}
                if (apagar) {
                    clique()
                    if (cpf.isNotEmpty()) cpf.deleteCharAt(cpf.length - 1)
                    atualizarVisor()
                } else digitar(simbolo)
            }
        }
    }

    private fun criarGrade(): LinearLayout {
        val linhas = listOf(
            listOf("1", "2", "3"), listOf("4", "5", "6"),
            listOf("7", "8", "9"), listOf("", "0", "⌫")
        )
        val grade = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        var i = 0
        teclas.clear()
        linhas.forEach { linha ->
            val l = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
            linha.forEach { s ->
                val v = if (s.isEmpty()) texto("🐻", 22f, roxo).apply { alpha = 0.55f }
                else criarTecla(s, i)
                i++
                teclas.add(v)
                l.addView(v, LinearLayout.LayoutParams(0, -1, 1f).apply {
                    setMargins(dp(4), dp(4), dp(4), dp(4))
                })
            }
            grade.addView(l, LinearLayout.LayoutParams(-1, 0, 1f))
        }
        return grade
    }

    fun mostrar() {
        if (activity.isFinishing || activity.isDestroyed) return

        dialog = Dialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val metricas = activity.resources.displayMetrics
        val paisagem = metricas.widthPixels > metricas.heightPixels
        val largura = if (paisagem) minOf(dp(580), (metricas.widthPixels * 0.9f).toInt())
        else minOf(dp(380), (metricas.widthPixels * 0.92f).toInt())
        val altura = if (paisagem) minOf(dp(370), (metricas.heightPixels * 0.95f).toInt())
        else minOf(dp(590), (metricas.heightPixels * 0.93f).toInt())

        superficie = FrameLayout(activity).apply {
            clipChildren = false
            clipToPadding = false
        }

        // Orelhinhas de ursinho (atrás do cartão)
        listOf(Gravity.TOP or Gravity.START, Gravity.TOP or Gravity.END).forEach { posicao ->
            val orelha = FrameLayout(activity).apply { background = oval(rosaBorda) }
            orelha.addView(View(activity).apply { background = oval(Color.rgb(255, 222, 232)) },
                FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER))
            superficie.addView(orelha, FrameLayout.LayoutParams(dp(58), dp(58), posicao).apply {
                if ((posicao and Gravity.START) == Gravity.START) leftMargin = dp(36)
                else rightMargin = dp(36)
            })
        }

        // Cartão principal com degradê suave
        val cartao = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = gradiente(rosaClaro, cremeSuave, 30, rosaBorda)
            clipToOutline = true
            elevation = dp(8).toFloat()
        }
        superficie.addView(cartao, FrameLayout.LayoutParams(-1, -1).apply { topMargin = dp(24) })

        // Cabeçalho
        val cabecalho = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = gradiente(
                degradeEsq, degradeDir, 0, null,
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            setPadding(dp(14), dp(8), dp(10), dp(8))
        }
        cartao.addView(cabecalho, LinearLayout.LayoutParams(-1, -2))

        mascote = texto("🐻", 24f, roxo).apply { background = oval(branco, rosaBorda) }
        cabecalho.addView(mascote, LinearLayout.LayoutParams(dp(44), dp(44)))
        balanco = ObjectAnimator.ofFloat(mascote, "translationY", 0f, -dp(3).toFloat(), 0f).apply {
            duration = 1400
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        val titulos = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(6), 0)
        }
        titulos.addView(texto("Cantinho dos responsáveis", 15f, roxo).apply {
            gravity = Gravity.START
        })
        titulos.addView(texto("Área só para os grandes 🌷", 11f, roxo, false).apply {
            gravity = Gravity.START
            alpha = 0.8f
        })
        cabecalho.addView(titulos, LinearLayout.LayoutParams(0, -2, 1f))

        val fechar = texto("✕", 16f, roxo).apply {
            background = oval(branco, rosaBorda)
            contentDescription = "Fechar calculadora"
            setOnClickListener {
                if (!conferindo && !confirmado) {
                    clique()
                    animarPressao(this) { dialog.dismiss() }
                }
            }
        }
        cabecalho.addView(fechar, LinearLayout.LayoutParams(dp(34), dp(34)))

        // Visor + bolinhas + informação
        val blocoInfo = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val visorCaixa = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = fundo(branco, 22, rosaBorda, 3)
            elevation = dp(2).toFloat()
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }
        visorCaixa.addView(texto("CPF do responsável", 11f, roxoSuave, false))
        visor = texto("000.000.000-00", 25f, roxo).apply {
            letterSpacing = 0.04f
            setPadding(0, dp(4), 0, 0)
        }
        visorCaixa.addView(visor, LinearLayout.LayoutParams(-1, -2))
        blocoInfo.addView(visorCaixa, LinearLayout.LayoutParams(-1, -2))

        val linhaPontos = LinearLayout(activity).apply { gravity = Gravity.CENTER }
        pontos.clear()
        repeat(11) {
            val p = View(activity).apply { background = oval(cinzaPonto) }
            pontos.add(p)
            linhaPontos.addView(p, LinearLayout.LayoutParams(dp(10), dp(10)).apply {
                setMargins(dp(3), 0, dp(3), 0)
            })
        }
        blocoInfo.addView(linhaPontos, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(10)
        })

        informacao = texto("0 de 11 dígitos", 12f, roxoSuave, false)
        blocoInfo.addView(informacao, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(6); bottomMargin = dp(6)
        })

        // Botão confirmar (doce rosa)
        botaoConfirmar = texto("Confirmar CPF", 16f, roxo).apply {
            background = doce(24)
            setPadding(0, 0, 0, dp(4))
            elevation = dp(3).toFloat()
            setOnClickListener {
                if (!conferindo && !confirmado) animarPressao(this) { confirmarCpf() }
            }
        }

        val grade = criarGrade()

        if (paisagem) {
            val corpo = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(14), dp(10), dp(14), dp(12))
            }
            val esquerda = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
            }
            esquerda.addView(blocoInfo, LinearLayout.LayoutParams(-1, -2))
            esquerda.addView(botaoConfirmar,
                LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8) })
            corpo.addView(esquerda, LinearLayout.LayoutParams(0, -1, 0.9f).apply {
                rightMargin = dp(10)
            })
            corpo.addView(grade, LinearLayout.LayoutParams(0, -1, 1f))
            cartao.addView(corpo, LinearLayout.LayoutParams(-1, 0, 1f))
        } else {
            val corpo = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(16))
            }
            corpo.addView(blocoInfo, LinearLayout.LayoutParams(-1, -2))
            corpo.addView(grade, LinearLayout.LayoutParams(-1, 0, 1f))
            corpo.addView(botaoConfirmar,
                LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(8) })
            cartao.addView(corpo, LinearLayout.LayoutParams(-1, 0, 1f))
        }

        dialog.setContentView(superficie)
        dialog.setOnDismissListener {
            balanco?.cancel(); balanco = null
            som?.release(); som = null
            aoFechar()
        }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.55f)
            setGravity(Gravity.CENTER)
            setLayout(largura, altura)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
        atualizarVisor()

        // Entrada do cartão
        superficie.alpha = 0f
        superficie.scaleX = 0.9f
        superficie.scaleY = 0.9f
        superficie.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(300).setInterpolator(OvershootInterpolator(1.3f)).start()

        // Teclas "pipocando" uma por uma
        teclas.forEachIndexed { i, t ->
            t.alpha = 0f; t.scaleX = 0.3f; t.scaleY = 0.3f
            t.animate().alpha(if (t is TextView && t.text == "🌸") 0.55f else 1f)
                .scaleX(1f).scaleY(1f)
                .setStartDelay(160L + i * 40L).setDuration(380)
                .setInterpolator(OvershootInterpolator(2.5f)).start()
        }
    }

    private fun erro(mensagem: String) {
        informacao.text = mensagem
        informacao.setTextColor(vermelho)
        mascote.text = "🙈"
        visor.animate().translationX(-dp(7).toFloat()).setDuration(60)
            .withEndAction {
                visor.animate().translationX(dp(7).toFloat()).setDuration(60)
                    .withEndAction {
                        visor.animate().translationX(-dp(4).toFloat()).setDuration(50)
                            .withEndAction { visor.animate().translationX(0f).setDuration(50).start() }
                            .start()
                    }.start()
            }.start()
    }

    private fun confirmarCpf() {
        if (conferindo || confirmado) return
        clique()
        if (cpf.length != 11) { erro("Digite os 11 números do CPF."); return }
        val sessao = LudicaSessao.obter(activity)
        if (sessao == null) { erro("Faça login para continuar."); return }

        conferindo = true
        botaoConfirmar.text = "Verificando..."
        botaoConfirmar.alpha = 0.75f
        mascote.text = "🤔"
        dialog.setCancelable(false)
        val cpfInformado = cpf.toString()
        val token = sessao.first

        Thread {
            var codigo = -1
            try {
                val conexao = URL("$urlBase/responsaveis/confirmar-cpf")
                    .openConnection() as HttpURLConnection
                try {
                    conexao.requestMethod = "POST"
                    conexao.connectTimeout = 12000
                    conexao.readTimeout = 12000
                    conexao.doOutput = true
                    conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conexao.setRequestProperty("Authorization", "Bearer $token")
                    val corpo = JSONObject().put("cpf", cpfInformado).toString()
                    conexao.outputStream.use { it.write(corpo.toByteArray(Charsets.UTF_8)) }
                    codigo = conexao.responseCode
                } finally { conexao.disconnect() }
            } catch (_: Exception) { codigo = -1 }

            val resposta = codigo
            activity.runOnUiThread {
                if (activity.isFinishing || activity.isDestroyed || !dialog.isShowing) return@runOnUiThread
                conferindo = false
                dialog.setCancelable(true)
                botaoConfirmar.text = "Confirmar CPF"
                botaoConfirmar.alpha = 1f
                when (resposta) {
                    200 -> sucesso()
                    401 -> { LudicaSessao.limpar(activity); erro("Sessão expirada. Entre novamente.") }
                    403 -> erro("CPF incorreto para esta conta.")
                    429 -> erro("Muitas tentativas. Tente depois.")
                    -1 -> erro("Sem conexão com o servidor.")
                    else -> erro("Não foi possível confirmar o CPF.")
                }
            }
        }.start()
    }

    private fun sucesso() {
        confirmado = true
        dialog.setCancelable(false)
        informacao.text = "CPF confirmado! 🎉"
        informacao.setTextColor(roxo)
        botaoConfirmar.text = "Acesso liberado!"
        mascote.text = "🥳"

        // Teclas dão uma "dancinha" de comemoração
        teclas.forEachIndexed { i, t ->
            t.animate().scaleX(1.12f).scaleY(1.12f).setStartDelay(i * 25L).setDuration(140)
                .withEndAction {
                    t.animate().scaleX(1f).scaleY(1f).setStartDelay(0).setDuration(260)
                        .setInterpolator(OvershootInterpolator(3f)).start()
                }.start()
        }

        superficie.post {
            val alcanceX = (superficie.width / 2).coerceAtLeast(dp(150))
            val alcanceY = (superficie.height / 2).coerceAtLeast(dp(120))
            repeat(48) {
                val confete = texto(
                    listOf("💖", "⭐", "✨", "🌸", "💛", "🎈", "🎉").random(),
                    Random.nextInt(14, 24).toFloat(), roxo
                )
                superficie.addView(confete,
                    FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER))
                val x = Random.nextInt(-alcanceX, alcanceX).toFloat()
                val y = Random.nextInt(-alcanceY, alcanceY).toFloat()
                confete.animate().translationX(x).translationY(y).alpha(0f)
                    .rotation(Random.nextInt(-200, 200).toFloat()).setDuration(1150)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction { superficie.removeView(confete) }.start()
            }
            superficie.postDelayed({
                if (!activity.isFinishing && !activity.isDestroyed && dialog.isShowing) {
                    dialog.dismiss()
                    aoConfirmar()
                }
            }, 1500L)
        }
    }
}