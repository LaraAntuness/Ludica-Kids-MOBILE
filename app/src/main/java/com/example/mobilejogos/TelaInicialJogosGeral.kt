
package com.example.mobilejogos

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TelaInicialJogosGeral : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define a tela na horizontal
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Abre o layout da tela dos jogos
        setContentView(R.layout.telainicialjogosgeral)

        // Botões da tela
        val botaoVoltar =
            findViewById<ImageButton>(R.id.botaoVoltar)

        val botaoEmocoes =
            findViewById<Button>(R.id.botaoEmocoes)

        val botaoDragao =
            findViewById<Button>(R.id.botaoDragao)

        val botaoSentidos =
            findViewById<Button>(R.id.botaoSentidos)

        // Botão para voltar à tela anterior
        botaoVoltar.setOnClickListener {
            finish()
        }

        // Jogo: Identificando as Emoções
        botaoEmocoes.setOnClickListener {
            Toast.makeText(
                this,
                "Identificando as Emoções",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Jogo: Respirando com o Dragão
        botaoDragao.setOnClickListener {
            Toast.makeText(
                this,
                "Respirando com o Dragão",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Jogo: Cinco Sentidos Mágicos
        botaoSentidos.isEnabled = false
    }
}
