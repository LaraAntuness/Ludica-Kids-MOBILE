package com.example.mobilejogos

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TelaInicialCincoSentidosPago : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantém a tela horizontal
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Abre o XML da tela
        setContentView(R.layout.telainicialcincosentidospago)

        // Botão voltar
        val botaoVoltar =
            findViewById<ImageButton>(R.id.botaoVoltar)

        // Botão Vamos lá
        val botaoVamosLa =
            findViewById<Button>(R.id.botaoVamosLa)

        // Retorna para a tela anterior
        botaoVoltar.setOnClickListener {
            finish()
        }

        // Futuramente abrirá outra página
        botaoVamosLa.setOnClickListener {

            Toast.makeText(
                this,
                "Em breve!",
                Toast.LENGTH_SHORT
            ).show()

            // Quando a próxima tela estiver pronta:
            //
            // val intent = Intent(
            //     this,
            //     ProximaTelaActivity::class.java
            // )
            //
            // startActivity(intent)
        }
    }
}
