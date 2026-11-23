
package com.example.mobilejogos

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class TelaInicialJogosGeral : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TELA HORIZONTAL
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // TELA CHEIA
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // CARREGAR O XML
        setContentView(R.layout.telainicialjogosgeral)

        // IDENTIFICAR BOTÕES
        val botaoVoltar =
            findViewById<ImageButton>(R.id.botaoVoltar)

        val botaoEmocoes =
            findViewById<AppCompatButton>(R.id.botaoEmocoes)

        val botaoDragao =
            findViewById<AppCompatButton>(R.id.botaoDragao)

        val botaoSentidos =
            findViewById<AppCompatButton>(R.id.botaoSentidos)

        // BOTÃO VOLTAR
        botaoVoltar.setOnClickListener {
            finish()
        }

        // IDENTIFICANDO AS EMOÇÕES
        botaoEmocoes.setOnClickListener {
            val intent = Intent(
                this@TelaInicialJogosGeral,
                IdentificandoEmocoesActivity::class.java
            )

            startActivity(intent)
        }

        // RESPIRANDO COM O DRAGÃO
        botaoDragao.setOnClickListener {
            val intent = Intent(
                this@TelaInicialJogosGeral,
                InicialRespirandoComODragaoActivy::class.java
            )

            startActivity(intent)
        }

        // CINCO SENTIDOS MÁGICOS
        // MANTIDO BLOQUEADO COMO NO PROTÓTIPO
        botaoSentidos.isEnabled = false
    }
}
