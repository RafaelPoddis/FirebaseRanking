package com.example.firebaseranking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.firebaseranking.ui.theme.FirebaseRankingTheme
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : ComponentActivity() {

    private val reference: DatabaseReference = FirebaseDatabase
        .getInstance()
        .getReference("pontuacoes")

    private data class PontuacaoComChave(
        val chave: String,
        val pontuacao: Pontuacao
    )

    private var pontos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val botaoAumentarPontos = findViewById<Button>(R.id.botaoAumentarPontos)
        val botaoDiminuirPontos = findViewById<Button>(R.id.botaoDiminuirPontos)
        val botaoSalvaPontos = findViewById<Button>(R.id.botaoSalvarPontos)

        val nome = findViewById<EditText>(R.id.nome)
        val textoRanking = findViewById<TextView>(R.id.rankingText)
        val pontosText = findViewById<TextView>(R.id.textoPontos)

        botaoAumentarPontos.setOnClickListener {
            pontos++
            pontosText.text = "Pontuação: $pontos"
        }

        botaoDiminuirPontos.setOnClickListener {
            if (pontos <= 0){
                pontos = 0
                Toast.makeText(this, "Pontuação inválida!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pontos--
            pontosText.text = "Pontuação: $pontos"
        }
    }
}
