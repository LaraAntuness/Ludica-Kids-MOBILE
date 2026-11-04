package com.example.mobilejogos

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TelaInicialJogosGeral : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContentView(R.layout.fundo_botao_jogos)

        val botaoVoltar =
            findViewById<android.widget.ImageButton>(
                R.id.botaoVoltar
            )

        val botaoEmocoes =
            findViewById<android.widget.Button>(
                R.id.botaoEmocoes
            )

        val botaoDragao =
            findViewById<android.widget.Button>(
                R.id.botaoDragao
            )

        botaoVoltar.setOnClickListener {
            finish()
        }

        botaoEmocoes.setOnClickListener {
            Toast.makeText(
                this,
                "Identificando as Emoções",
                Toast.LENGTH_SHORT
            ).show()
        }

        botaoDragao.setOnClickListener {
            Toast.makeText(
                this,
                "Respirando com o Dragão",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
