package com.example.mobilejogos

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import java.io.File
import java.util.Locale
import kotlin.math.ceil

class RespirandoComDragaoActivity : Activity() {

    private lateinit var tela: FrameLayout
    private lateinit var videoDragao: VideoView

    private lateinit var textoRelogio: TextView
    private lateinit var textoInstrucao: TextView
    private lateinit var textoEtapa: TextView

    private lateinit var botaoAcao: TextView
    private lateinit var botaoInterromper: TextView
    private lateinit var botaoPular: TextView

    private lateinit var painelRespiracao: LinearLayout

    private val handler = Handler(Looper.getMainLooper())

    // ÁUDIOS

    private var musicaFundo: MediaPlayer? = null
    private var audioNarracao: MediaPlayer? = null
    private var audioClique: MediaPlayer? = null
    private var playerVideo: MediaPlayer? = null

    private var popupAtual: Dialog? = null
    private var botaoOuvir: TextView? = null

    // CONTROLE DO JOGO

    private var videoPreparado = false
    private var etapaAtual = 0
    private var etapaExecutando = false

    private var aguardandoBusca = false
    private var iniciarAposBusca = false

    private var saindo = false
    private var telaAtiva = false
    private var sessaoConcluida = false

    private var caminhoVideo: Uri? = null

    // TEMPOS DA RESPIRAÇÃO 4-7-8

    private val inicioEtapas = intArrayOf(
        0,
        4000,
        11000
    )

    private val fimEtapas = intArrayOf(
        4000,
        11000,
        19000
    )

    private val duracaoEtapas = intArrayOf(
        4,
        7,
        8
    )

    // VOLUMES DOS ÁUDIOS

    private val volumeFundo = 0.35f
    private val volumeFundoNarracao = 0.10f
    private val volumeNarracao = 0.65f
    private val volumeClique = 0.55f

    // CORES

    private val azul = Color.parseColor("#65B9E7")
    private val azulEscuro = Color.parseColor("#357DA5")
    private val verde = Color.parseColor("#B5E187")
    private val amarelo = Color.parseColor("#FFE39A")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        esconderBarras()

        criarTela()

        mostrarPopupInicial()
    }

    private fun esconderBarras() {

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    // LOCALIZAR ÁUDIOS NA PASTA RAW

    private fun localizarAudio(nome: String): Int {

        return resources.getIdentifier(
            nome,
            "raw",
            packageName
        )
    }

    // MÚSICA DE FUNDO CONTÍNUA

    private fun iniciarMusicaFundo() {

        if (saindo) return

        try {

            if (musicaFundo == null) {

                val idAudio = localizarAudio(
                    "fundodragao"
                )

                if (idAudio == 0) {

                    Toast.makeText(
                        this,
                        "Áudio fundodragao não encontrado.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return
                }

                val player = MediaPlayer.create(
                    this,
                    idAudio
                )

                if (player != null) {

                    player.isLooping = true

                    player.setVolume(
                        volumeFundo,
                        volumeFundo
                    )

                    musicaFundo = player
                }
            }

            if (musicaFundo?.isPlaying != true) {

                musicaFundo?.start()
            }

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Não foi possível carregar a música.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // DIMINUIR A MÚSICA DURANTE A NARRAÇÃO

    private fun diminuirMusicaFundo() {

        musicaFundo?.setVolume(
            volumeFundoNarracao,
            volumeFundoNarracao
        )
    }

    // RESTAURAR O VOLUME NORMAL

    private fun restaurarMusicaFundo() {

        musicaFundo?.setVolume(
            volumeFundo,
            volumeFundo
        )
    }

    private fun pausarMusicaFundo() {

        try {

            if (musicaFundo?.isPlaying == true) {

                musicaFundo?.pause()
            }

        } catch (_: Exception) {
        }
    }

    private fun encerrarMusicaFundo() {

        try {

            musicaFundo?.release()

        } catch (_: Exception) {
        }

        musicaFundo = null
    }

    // NARRAÇÃO OPCIONAL

    private fun ouvirInstrucoes() {

        if (
            saindo ||
            popupAtual?.isShowing != true ||
            botaoOuvir == null
        ) return

        if (audioNarracao?.isPlaying == true) {

            pararNarracao()

            return
        }

        try {

            pararNarracao()

            val idAudio = localizarAudio(
                "instrucoes_dragao"
            )

            if (idAudio == 0) {

                Toast.makeText(
                    this,
                    "Áudio de instruções não encontrado.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val player = MediaPlayer.create(
                this,
                idAudio
            ) ?: return

            audioNarracao = player

            diminuirMusicaFundo()

            player.setVolume(
                volumeNarracao,
                volumeNarracao
            )

            player.isLooping = false

            botaoOuvir?.text = "Parar áudio"

            player.setOnCompletionListener {

                if (audioNarracao === player) {

                    pararNarracao()
                }
            }

            player.setOnErrorListener { _, _, _ ->

                pararNarracao()

                true
            }

            player.start()

        } catch (e: Exception) {

            pararNarracao()

            Toast.makeText(
                this,
                "Não foi possível reproduzir a narração.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun pararNarracao() {

        try {

            audioNarracao?.release()

        } catch (_: Exception) {
        }

        audioNarracao = null

        restaurarMusicaFundo()

        botaoOuvir?.text =
            "🔊 Ouvir instruções"
    }

    // CRIAÇÃO DA TELA PRINCIPAL

    private fun criarTela() {

        tela = FrameLayout(this).apply {

            clipChildren = true

            clipToPadding = true

            setBackgroundColor(
                Color.parseColor("#DDF3FF")
            )
        }

        setContentView(tela)

        // VÍDEO EM TELA INTEIRA

        videoDragao = object :
            VideoView(this@RespirandoComDragaoActivity) {

            override fun onMeasure(
                widthMeasureSpec: Int,
                heightMeasureSpec: Int
            ) {

                setMeasuredDimension(
                    View.MeasureSpec.getSize(
                        widthMeasureSpec
                    ),
                    View.MeasureSpec.getSize(
                        heightMeasureSpec
                    )
                )
            }
        }

        tela.addView(
            videoDragao,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )

        // AJUSTAR O VÍDEO AO TAMANHO DA TELA

        tela.addOnLayoutChangeListener {
                _, esquerda, topo, direita, baixo,
                antigaEsquerda, antigoTopo,
                antigaDireita, antigoBaixo ->

            val largura = direita - esquerda
            val altura = baixo - topo

            val larguraAntiga =
                antigaDireita - antigaEsquerda

            val alturaAntiga =
                antigoBaixo - antigoTopo

            if (
                largura != larguraAntiga ||
                altura != alturaAntiga
            ) {

                playerVideo?.let {

                    ajustarVideoTelaInteira(it)
                }
            }
        }

        // BOTÃO VOLTAR

        val botaoVoltar = ImageButton(this).apply {

            setImageResource(
                R.drawable.botaoretornoazul
            )

            background =
                ColorDrawable(Color.TRANSPARENT)

            scaleType =
                ImageView.ScaleType.FIT_CENTER

            setPadding(0, 0, 0, 0)

            contentDescription = "Voltar"
        }

        val parametrosVoltar =
            FrameLayout.LayoutParams(
                dp(50),
                dp(50)
            ).apply {

                gravity =
                    Gravity.TOP or Gravity.START

                topMargin = dp(12)
                leftMargin = dp(15)
            }

        tela.addView(
            botaoVoltar,
            parametrosVoltar
        )

        botaoVoltar.setOnClickListener {

            tocarClique()

            voltarTela()
        }

        // RELÓGIO

        val painelRelogio = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER

            background = fundoArredondado(
                Color.WHITE,
                24,
                azul,
                3
            )

            elevation = dp(5).toFloat()

            setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(5)
            )
        }

        val tituloRelogio = TextView(this).apply {

            text = "TEMPO"

            textSize = 12f

            setTextColor(azulEscuro)

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER
        }

        textoRelogio = TextView(this).apply {

            text = "04"

            textSize = 38f

            setTextColor(azulEscuro)

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = false
        }

        val segundos = TextView(this).apply {

            text = "segundos"

            textSize = 11f

            setTextColor(azulEscuro)

            gravity = Gravity.CENTER
        }

        painelRelogio.addView(tituloRelogio)
        painelRelogio.addView(textoRelogio)
        painelRelogio.addView(segundos)

        val parametrosRelogio =
            FrameLayout.LayoutParams(
                dp(100),
                dp(105)
            ).apply {

                gravity =
                    Gravity.TOP or Gravity.END

                topMargin = dp(15)
                rightMargin = dp(20)
            }

        tela.addView(
            painelRelogio,
            parametrosRelogio
        )

        // PAINEL DE INSTRUÇÕES

        painelRespiracao =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity = Gravity.CENTER

                background = fundoArredondado(
                    Color.WHITE,
                    24,
                    azul,
                    3
                )

                elevation = dp(5).toFloat()

                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(8)
                )
            }

        textoEtapa = TextView(this).apply {

            text = "Etapa 1 de 3"

            textSize = 12f

            gravity = Gravity.CENTER

            setTextColor(azulEscuro)

            typeface = Typeface.DEFAULT_BOLD
        }

        textoInstrucao = TextView(this).apply {

            text = "Vamos puxar o ar?"

            textSize = 18f

            gravity = Gravity.CENTER

            setTextColor(azulEscuro)

            typeface = Typeface.DEFAULT_BOLD
        }

        // BOTÃO DE AÇÃO

        botaoAcao = criarBotao(
            "Puxar o ar",
            azul
        )

        botaoAcao.setOnClickListener {

            if (
                !etapaExecutando &&
                !aguardandoBusca &&
                videoPreparado &&
                !sessaoConcluida
            ) {

                tocarClique()

                iniciarEtapa()
            }
        }

        // PULAR ETAPA DE SEGURAR O AR

        botaoPular = TextView(this).apply {

            text = "Pular esta etapa"

            textSize = 13f

            gravity = Gravity.CENTER

            setTextColor(azulEscuro)

            setPadding(
                0,
                dp(8),
                0,
                dp(4)
            )

            visibility = View.GONE

            setOnClickListener {

                if (
                    etapaAtual == 1 &&
                    !aguardandoBusca
                ) {

                    tocarClique()

                    etapaExecutando = false

                    iniciarAposBusca = false

                    handler.removeCallbacks(
                        atualizarRelogio
                    )

                    videoDragao.pause()

                    prepararEtapa(2)
                }
            }
        }

        // PARAR EXERCÍCIO

        botaoInterromper = TextView(this).apply {

            text = "Parar exercício"

            textSize = 13f

            gravity = Gravity.CENTER

            setTextColor(azulEscuro)

            setPadding(
                0,
                dp(7),
                0,
                dp(4)
            )

            visibility = View.GONE

            setOnClickListener {

                tocarClique()

                interromperSessao()
            }
        }

        painelRespiracao.addView(textoEtapa)
        painelRespiracao.addView(textoInstrucao)
        painelRespiracao.addView(botaoAcao)
        painelRespiracao.addView(botaoPular)
        painelRespiracao.addView(botaoInterromper)

        val parametrosPainel =
            FrameLayout.LayoutParams(
                dp(280),
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                gravity =
                    Gravity.BOTTOM or Gravity.END

                bottomMargin = dp(15)
                rightMargin = dp(20)
            }

        tela.addView(
            painelRespiracao,
            parametrosPainel
        )

        botaoAcao.isEnabled = false

        botaoAcao.alpha = 0.5f
    }

    // AJUSTAR VÍDEO SEM BORDAS

    private fun ajustarVideoTelaInteira(
        player: MediaPlayer
    ) {

        tela.post {

            if (
                saindo ||
                playerVideo !== player
            ) return@post

            val larguraTela = tela.width
            val alturaTela = tela.height

            val larguraVideo = player.videoWidth
            val alturaVideo = player.videoHeight

            if (
                larguraTela > 0 &&
                alturaTela > 0 &&
                larguraVideo > 0 &&
                alturaVideo > 0
            ) {

                val escala = maxOf(
                    larguraTela.toFloat() / larguraVideo,
                    alturaTela.toFloat() / alturaVideo
                )

                val novaLargura =
                    ceil(larguraVideo * escala).toInt()

                val novaAltura =
                    ceil(alturaVideo * escala).toInt()

                videoDragao.layoutParams =
                    FrameLayout.LayoutParams(
                        novaLargura,
                        novaAltura,
                        Gravity.CENTER
                    )
            }
        }
    }

    // CARREGAR VÍDEO DA PASTA ASSETS

    private fun configurarVideo() {

        videoPreparado = false

        aguardandoBusca = false

        try {

            if (caminhoVideo == null) {

                val arquivoVideo = File(
                    cacheDir,
                    "videodragao.mp4"
                )

                assets.open(
                    "videodragao.mp4"
                ).use { entrada ->

                    arquivoVideo.outputStream().use { saida ->

                        entrada.copyTo(saida)
                    }
                }

                caminhoVideo =
                    Uri.fromFile(arquivoVideo)
            }

            videoDragao.setOnPreparedListener { player ->

                if (
                    saindo ||
                    !telaAtiva
                ) return@setOnPreparedListener

                playerVideo = player

                player.isLooping = false

                // VÍDEO SEM ÁUDIO PRÓPRIO

                player.setVolume(
                    0f,
                    0f
                )

                videoPreparado = true

                ajustarVideoTelaInteira(player)

                player.setOnSeekCompleteListener {

                    if (
                        saindo ||
                        !telaAtiva ||
                        playerVideo !== player
                    ) return@setOnSeekCompleteListener

                    aguardandoBusca = false

                    if (
                        iniciarAposBusca &&
                        etapaExecutando
                    ) {

                        iniciarAposBusca = false

                        videoDragao.start()

                        handler.removeCallbacks(
                            atualizarRelogio
                        )

                        handler.post(
                            atualizarRelogio
                        )

                    } else {

                        atualizarDisponibilidadeBotao()
                    }
                }

                if (!sessaoConcluida) {

                    posicionarVideo(
                        inicioEtapas[etapaAtual],
                        false
                    )
                }
            }

            // FINAL DO VÍDEO

            videoDragao.setOnCompletionListener {

                if (
                    etapaExecutando &&
                    etapaAtual == 2 &&
                    !saindo
                ) {

                    finalizarSessao()

                } else if (etapaExecutando) {

                    interromperSessao()
                }
            }

            videoDragao.setOnErrorListener { _, _, _ ->

                videoPreparado = false

                etapaExecutando = false

                aguardandoBusca = false

                iniciarAposBusca = false

                playerVideo = null

                handler.removeCallbacks(
                    atualizarRelogio
                )

                botaoAcao.isEnabled = false

                botaoInterromper.visibility =
                    View.VISIBLE

                Toast.makeText(
                    this,
                    "Não foi possível reproduzir o vídeo.",
                    Toast.LENGTH_LONG
                ).show()

                true
            }

            videoDragao.setVideoURI(
                caminhoVideo
            )

        } catch (e: Exception) {

            videoPreparado = false

            Toast.makeText(
                this,
                "Vídeo não encontrado na pasta assets.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // POSICIONAR O VÍDEO

    private fun posicionarVideo(
        tempo: Int,
        reproduzir: Boolean
    ) {

        val player = playerVideo ?: return

        if (
            !videoPreparado ||
            saindo
        ) return

        aguardandoBusca = true

        iniciarAposBusca = reproduzir

        botaoAcao.isEnabled = false

        botaoAcao.alpha = 0.5f

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                player.seekTo(
                    tempo.toLong(),
                    MediaPlayer.SEEK_CLOSEST
                )

            } else {

                player.seekTo(tempo)
            }

        } catch (e: Exception) {

            aguardandoBusca = false

            iniciarAposBusca = false

            etapaExecutando = false

            botaoAcao.visibility =
                View.VISIBLE

            atualizarDisponibilidadeBotao()

            Toast.makeText(
                this,
                "Não foi possível iniciar a etapa.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun atualizarDisponibilidadeBotao() {

        val disponivel =
            videoPreparado &&
                    !aguardandoBusca &&
                    !etapaExecutando &&
                    !sessaoConcluida &&
                    !saindo

        botaoAcao.isEnabled = disponivel

        botaoAcao.alpha =
            if (disponivel) 1f else 0.5f
    }

    // PREPARAR CADA ETAPA

    private fun prepararEtapa(etapa: Int) {

        if (
            saindo ||
            etapa !in 0..2
        ) return

        handler.removeCallbacks(
            atualizarRelogio
        )

        etapaAtual = etapa

        etapaExecutando = false

        iniciarAposBusca = false

        sessaoConcluida = false

        if (videoPreparado) {

            videoDragao.pause()
        }

        // MÚSICA CONTINUA TOCANDO

        textoEtapa.text =
            "Etapa ${etapa + 1} de 3"

        textoRelogio.text =
            String.format(
                Locale.getDefault(),
                "%02d",
                duracaoEtapas[etapa]
            )

        botaoAcao.visibility =
            View.VISIBLE

        botaoInterromper.visibility =
            View.VISIBLE

        botaoPular.visibility =
            if (etapa == 1) View.VISIBLE
            else View.GONE

        when (etapa) {

            0 -> {

                textoInstrucao.text =
                    "Vamos puxar o ar?"

                botaoAcao.text =
                    "Puxar o ar"

                alterarCorPainel(azul)
            }

            1 -> {

                textoInstrucao.text =
                    "Respire normalmente até estar pronto."

                botaoAcao.text =
                    "Segurar o ar"

                alterarCorPainel(amarelo)
            }

            2 -> {

                textoInstrucao.text =
                    "Pode soltar o ar. Toque para continuar."

                botaoAcao.text =
                    "Soltar o ar"

                alterarCorPainel(verde)
            }
        }

        if (videoPreparado) {

            posicionarVideo(
                inicioEtapas[etapa],
                false
            )

        } else {

            atualizarDisponibilidadeBotao()
        }
    }

    // INICIAR AÇÃO SOMENTE AO CLICAR

    private fun iniciarEtapa() {

        if (
            !videoPreparado ||
            etapaExecutando ||
            aguardandoBusca ||
            saindo ||
            !telaAtiva
        ) return

        pararNarracao()

        etapaExecutando = true

        botaoAcao.isEnabled = false

        botaoAcao.visibility =
            View.GONE

        botaoInterromper.visibility =
            View.VISIBLE

        when (etapaAtual) {

            0 -> {

                textoInstrucao.text =
                    "Inspire pelo nariz!"
            }

            1 -> {

                textoInstrucao.text =
                    "Segure o ar, sem forçar!"
            }

            2 -> {

                textoInstrucao.text =
                    "Solte o ar devagar pela boca!"
            }
        }

        posicionarVideo(
            inicioEtapas[etapaAtual],
            true
        )
    }

    // RELÓGIO SINCRONIZADO

    private val atualizarRelogio = object : Runnable {

        override fun run() {

            if (
                !etapaExecutando ||
                aguardandoBusca ||
                saindo ||
                !telaAtiva
            ) return

            val tempoAtual =
                videoDragao.currentPosition

            val tempoFinal =
                fimEtapas[etapaAtual]

            val restante =
                (tempoFinal - tempoAtual)
                    .coerceAtLeast(0)

            val segundosRestantes =
                ceil(restante / 1000.0)
                    .toInt()
                    .coerceIn(
                        0,
                        duracaoEtapas[etapaAtual]
                    )

            textoRelogio.text =
                String.format(
                    Locale.getDefault(),
                    "%02d",
                    segundosRestantes
                )

            if (tempoAtual >= tempoFinal) {

                finalizarEtapa()

            } else {

                handler.postDelayed(
                    this,
                    30
                )
            }
        }
    }

    // FINALIZAR ETAPA

    private fun finalizarEtapa() {

        if (
            !etapaExecutando ||
            saindo
        ) return

        etapaExecutando = false

        iniciarAposBusca = false

        handler.removeCallbacks(
            atualizarRelogio
        )

        videoDragao.pause()

        textoRelogio.text = "00"

        // MÚSICA NÃO É INTERROMPIDA

        if (etapaAtual == 2) {

            finalizarSessao()

        } else {

            prepararEtapa(
                etapaAtual + 1
            )
        }
    }

    // FINALIZAR SESSÃO

    private fun finalizarSessao() {

        if (
            saindo ||
            sessaoConcluida
        ) return

        etapaExecutando = false

        sessaoConcluida = true

        iniciarAposBusca = false

        handler.removeCallbacks(
            atualizarRelogio
        )

        videoDragao.pause()

        textoRelogio.text = "00"

        textoInstrucao.text =
            "Muito bem!"

        textoEtapa.text =
            "Sessão concluída"

        botaoAcao.visibility =
            View.GONE

        botaoPular.visibility =
            View.GONE

        botaoInterromper.visibility =
            View.GONE

        mostrarPopupFinal()
    }

    // INTERROMPER EXERCÍCIO

    private fun interromperSessao() {

        etapaExecutando = false

        iniciarAposBusca = false

        aguardandoBusca = false

        sessaoConcluida = false

        handler.removeCallbacks(
            atualizarRelogio
        )

        pararNarracao()

        videoDragao.stopPlayback()

        playerVideo = null

        videoPreparado = false

        prepararEtapa(0)

        mostrarPopupInicial()

        if (
            telaAtiva &&
            !saindo
        ) {

            configurarVideo()
        }
    }

    // POP-UP INICIAL

    private fun mostrarPopupInicial() {

        if (
            saindo ||
            isFinishing
        ) return

        pararNarracao()

        popupAtual?.dismiss()

        val dialog = Dialog(this)

        popupAtual = dialog

        val conteudo = criarConteudoPopup()

        adicionarDragao(
            conteudo,
            75
        )

        conteudo.addView(
            criarTitulo(
                "Vamos respirar juntos?"
            )
        )

        conteudo.addView(
            criarMensagem(
                "Toque nos botões e acompanhe o dragão!\n\n" +
                        "Inspire por 4 segundos.\n" +
                        "Segure por 7 segundos, se conseguir.\n" +
                        "Solte o ar por 8 segundos.\n\n" +
                        "Respire normalmente sempre que precisar."
            )
        )

        // BOTÃO OUVIR INSTRUÇÕES

        val ouvir = criarBotao(
            "🔊 Ouvir instruções",
            amarelo
        )

        botaoOuvir = ouvir

        ouvir.setOnClickListener {

            ouvirInstrucoes()
        }

        conteudo.addView(ouvir)

        // BOTÃO COMEÇAR

        val iniciar = criarBotao(
            "Vamos começar!",
            verde
        )

        iniciar.setOnClickListener {

            tocarClique()

            pararNarracao()

            dialog.dismiss()

            popupAtual = null

            botaoOuvir = null

            prepararEtapa(0)
        }

        conteudo.addView(iniciar)

        // BOTÃO VOLTAR

        val sair = criarBotao(
            "Voltar",
            azul
        )

        sair.setOnClickListener {

            tocarClique()

            voltarTela()
        }

        conteudo.addView(sair)

        exibirDialog(
            dialog,
            conteudo
        )
    }

    // POP-UP FINAL

    private fun mostrarPopupFinal() {

        if (
            saindo ||
            isFinishing
        ) return

        pararNarracao()

        popupAtual?.dismiss()

        botaoOuvir = null

        val dialog = Dialog(this)

        popupAtual = dialog

        val conteudo = criarConteudoPopup()

        adicionarDragao(
            conteudo,
            80
        )

        val estrelas = TextView(this).apply {

            text = "★ ★ ★"

            textSize = 32f

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#F5BD40")
            )
        }

        conteudo.addView(estrelas)

        conteudo.addView(
            criarTitulo("Parabéns!")
        )

        conteudo.addView(
            criarMensagem(
                "Você completou a sessão com o dragão!\n" +
                        "Que tal brincar novamente?"
            )
        )

        // BOTÃO REINICIAR

        val reiniciar = criarBotao(
            "Iniciar sessão novamente",
            verde
        )

        reiniciar.setOnClickListener {

            tocarClique()

            dialog.dismiss()

            popupAtual = null

            sessaoConcluida = false

            prepararEtapa(0)

            if (!videoPreparado) {

                configurarVideo()
            }
        }

        conteudo.addView(reiniciar)

        // BOTÃO SAIR

        val sair = criarBotao(
            "Sair",
            azul
        )

        sair.setOnClickListener {

            tocarClique()

            dialog.dismiss()

            voltarTela()
        }

        conteudo.addView(sair)

        exibirDialog(
            dialog,
            conteudo
        )
    }

    // IMAGEM DO DRAGÃO

    private fun adicionarDragao(
        conteudo: LinearLayout,
        tamanho: Int
    ) {

        val dragao = ImageView(this).apply {

            setImageResource(
                R.drawable.rostodragao
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER
        }

        conteudo.addView(
            dragao,
            LinearLayout.LayoutParams(
                dp(tamanho),
                dp(tamanho)
            )
        )
    }

    // CORES DAS ETAPAS

    private fun alterarCorPainel(cor: Int) {

        painelRespiracao.background =
            fundoArredondado(
                Color.WHITE,
                24,
                cor,
                3
            )

        botaoAcao.background =
            fundoArredondado(
                cor,
                18
            )

        textoRelogio.setTextColor(
            when (etapaAtual) {

                1 -> Color.parseColor("#B28B30")

                2 -> Color.parseColor("#589E45")

                else -> azulEscuro
            }
        )
    }

    // DESIGN DOS POP-UPS

    private fun criarConteudoPopup(): LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            gravity =
                Gravity.CENTER

            background =
                fundoArredondado(
                    Color.parseColor("#FFFDF9"),
                    28,
                    azul,
                    4
                )

            setPadding(
                dp(20),
                dp(12),
                dp(20),
                dp(12)
            )
        }
    }

    private fun criarTitulo(
        mensagem: String
    ): TextView {

        return TextView(this).apply {

            text = mensagem

            textSize = 24f

            gravity = Gravity.CENTER

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(azulEscuro)

            setPadding(
                0,
                dp(4),
                0,
                dp(5)
            )
        }
    }

    private fun criarMensagem(
        mensagem: String
    ): TextView {

        return TextView(this).apply {

            text = mensagem

            textSize = 15f

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#555555")
            )

            setPadding(
                0,
                dp(4),
                0,
                dp(10)
            )
        }
    }

    private fun criarBotao(
        titulo: String,
        cor: Int
    ): TextView {

        return TextView(this).apply {

            text = titulo

            textSize = 16f

            gravity = Gravity.CENTER

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                Color.parseColor("#355568")
            )

            background =
                fundoArredondado(
                    cor,
                    18
                )

            isClickable = true

            isFocusable = true

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(44)
                ).apply {

                    topMargin = dp(7)
                }

            elevation = dp(2).toFloat()
        }
    }

    private fun exibirDialog(
        dialog: Dialog,
        conteudo: View
    ) {

        val scroll = ScrollView(this).apply {

            isFillViewport = true

            isVerticalScrollBarEnabled = false

            addView(conteudo)
        }

        dialog.setContentView(scroll)

        dialog.setCancelable(false)

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        val larguraTela =
            resources.displayMetrics.widthPixels

        val alturaTela =
            resources.displayMetrics.heightPixels

        dialog.window?.setLayout(
            dp(375).coerceAtMost(
                (larguraTela * 0.9f).toInt()
            ),
            dp(430).coerceAtMost(
                (alturaTela * 0.94f).toInt()
            )
        )

        dialog.window?.addFlags(
            WindowManager.LayoutParams.FLAG_DIM_BEHIND
        )

        dialog.window?.setDimAmount(0.55f)

        dialog.window?.decorView?.systemUiVisibility =
            window.decorView.systemUiVisibility
    }

    // SOM DOS CLIQUES

    private fun tocarClique() {

        try {

            audioClique?.release()

            audioClique = null

            val idAudio = localizarAudio(
                "clique_suave"
            )

            if (idAudio == 0) return

            val player = MediaPlayer.create(
                this,
                idAudio
            ) ?: return

            audioClique = player

            player.setVolume(
                volumeClique,
                volumeClique
            )

            player.setOnCompletionListener {

                if (audioClique === player) {

                    audioClique = null

                    player.release()
                }
            }

            player.start()

        } catch (_: Exception) {
        }
    }

    // VOLTAR PARA TELA GERAL DOS JOGOS

    private fun voltarTela() {

        if (saindo) return

        saindo = true

        etapaExecutando = false

        iniciarAposBusca = false

        handler.removeCallbacksAndMessages(null)

        pararNarracao()

        encerrarMusicaFundo()

        popupAtual?.dismiss()

        videoDragao.stopPlayback()

        playerVideo = null

        val intent = Intent(
            this,
            TelaInicialJogosGeral::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)

        finish()
    }

    // FUNDOS ARREDONDADOS

    private fun fundoArredondado(
        cor: Int,
        raio: Int,
        corBorda: Int = Color.TRANSPARENT,
        tamanhoBorda: Int = 0
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(cor)

            cornerRadius =
                dp(raio).toFloat()

            if (tamanhoBorda > 0) {

                setStroke(
                    dp(tamanhoBorda),
                    corBorda
                )
            }
        }
    }

    private fun dp(valor: Int): Int {

        return (
                valor *
                        resources.displayMetrics.density
                ).toInt()
    }

    override fun onBackPressed() {

        voltarTela()
    }

    override fun onPause() {

        super.onPause()

        telaAtiva = false

        handler.removeCallbacks(
            atualizarRelogio
        )

        pararNarracao()

        pausarMusicaFundo()

        etapaExecutando = false

        iniciarAposBusca = false

        aguardandoBusca = false

        videoPreparado = false

        videoDragao.stopPlayback()

        playerVideo = null

        if (!sessaoConcluida) {

            etapaAtual = 0
        }
    }

    override fun onResume() {

        super.onResume()

        telaAtiva = true

        esconderBarras()

        if (!saindo) {

            iniciarMusicaFundo()

            if (!sessaoConcluida) {

                prepararEtapa(0)

                if (popupAtual?.isShowing != true) {

                    mostrarPopupInicial()
                }
            }

            configurarVideo()
        }
    }

    override fun onDestroy() {

        etapaExecutando = false

        saindo = true

        handler.removeCallbacksAndMessages(null)

        popupAtual?.dismiss()

        pararNarracao()

        audioClique?.release()

        audioClique = null

        encerrarMusicaFundo()

        videoDragao.stopPlayback()

        playerVideo = null

        super.onDestroy()
    }
}