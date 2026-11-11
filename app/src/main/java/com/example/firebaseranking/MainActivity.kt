package com.example.firebaseranking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : ComponentActivity() {

    private val reference: DatabaseReference = FirebaseDatabase
        .getInstance()
        .getReference("pontuacoes")

    private data class PontuacaoComChave(
        val chave: String,
        val pontuacao: Pontuacao
    )

    private var pontos: Int = 0

    private var pontuacoesCarregadas = emptyList<PontuacaoComChave>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val botaoAumentarPontos = findViewById<Button>(R.id.botaoAumentarPontos)
        val botaoDiminuirPontos = findViewById<Button>(R.id.botaoDiminuirPontos)
        val botaoSalvarPontos = findViewById<Button>(R.id.botaoSalvarPontos)

        val campoNome = findViewById<EditText>(R.id.nome)
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

        botaoSalvarPontos.setOnClickListener {
            val nome = campoNome.text.toString().trim()

            if (nome.isEmpty()){
                campoNome.error = "DIGITE UM NOME!"
            } else {
                salvarPontos(nome)
            }
        }

        textoRanking.setOnClickListener {
            abrirSeletorRanking()
        }

        carregarRanking(textoRanking)
    }

    private fun salvarPontos(nome: String) {
        val pontuacao = Pontuacao(nome, pontos)

        reference.push()
            .setValue(pontuacao)
            .addOnSuccessListener {
                Toast.makeText(this, "Pontuação salva", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { erro -> errorMsg(erro.message) }
    }

    private fun carregarRanking(textoRanking: TextView){
        reference
            .orderByChild("pontos")
            .limitToLast(10)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<PontuacaoComChave>()

                    for (item in snapshot.children) {
                        val chave = item.key
                        val pontuacao = item.getValue(Pontuacao::class.java)

                        if (chave != null && pontuacao != null) {
                            lista.add(
                                PontuacaoComChave(
                                    chave = chave,
                                    pontuacao = pontuacao
                                )
                            )
                        }
                    }
                    lista.reverse()
                    pontuacoesCarregadas = lista

                    val linhas = lista.mapIndexed { index, item ->
                        "${index+1}. ${item.pontuacao.nome} - ${item.pontuacao.pontos} pontos"
                    }
                    textoRanking.text = if(linhas.isEmpty()) {
                        "Ainda não há pontuações!"
                    } else {
                        "Ranking:\n"+linhas.joinToString("\n")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    textoRanking.text = "Erro ao carregar ranking!"
                    errorMsg(error.message)
                }
            })
    }

    private fun abrirSeletorRanking() {
        if (pontuacoesCarregadas.isEmpty()) {
            Toast.makeText(this, "Não há pontuações salvas!", Toast.LENGTH_SHORT).show()
            return
        }

        val opcoes = pontuacoesCarregadas.map { item ->
            "${item.pontuacao.nome} - ${item.pontuacao.pontos} pontos"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Escolha uma pontuacao:")
            .setItems(opcoes) { _, index ->
                val selecionada = pontuacoesCarregadas[index]
                abrirAcoesPontuacao(selecionada)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirAcoesPontuacao(pontuacao: PontuacaoComChave) {
        AlertDialog.Builder(this)
            .setTitle("O que deseja fazer?")
            .setItems(arrayOf("Editar pontos", "Excluir pontuação")) {_, escolha ->
                when(escolha){
                    0 -> abrirEdicao(pontuacao)
                    1 -> abrirExclusao(pontuacao)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirEdicao(pontuacao: PontuacaoComChave) {
        val campoPontos = EditText(this).apply {
            hint = "${pontuacao.pontuacao.pontos}"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(pontuacao.pontuacao.pontos.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Editar pontos")
            .setMessage("Digite a nova pontuação")
            .setView(campoPontos)
            .setPositiveButton("Salvar") {_, _ ->
                val novosPontos = campoPontos.text.toString().toIntOrNull()

                if (novosPontos == null || novosPontos < 0) {
                    Toast.makeText(this, "Digite um valor maior ou igual a zero!", Toast.LENGTH_SHORT).show()
                } else {
                    salvarEdicao(pontuacao, novosPontos)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarEdicao(pontuacao: PontuacaoComChave, novosPontos: Int) {
        reference
            .child(pontuacao.chave)
            .child("pontos")
            .setValue(novosPontos)
            .addOnSuccessListener {
                Toast.makeText(this, "Pontuação editada com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { erro ->
                errorMsg(erro.message)
            }
    }

    private fun abrirExclusao(pontuacao: PontuacaoComChave){
        AlertDialog.Builder(this)
            .setTitle("Tem certeza que deseja excluir?")
            .setMessage("${pontuacao.pontuacao.nome} - ${pontuacao.pontuacao.pontos} pontos")
            .setPositiveButton("Sim") {_, _ ->
                excluirPontos(pontuacao)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirPontos(pontuacao: PontuacaoComChave) {
        reference
            .child(pontuacao.chave)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Pontuação removida com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { erro ->
                errorMsg(erro.message)
            }
    }
    private fun errorMsg(mensagem: String?) {
        android.util.Log.e("Firebase", mensagem ?: "Ocorreu um erro")
        Toast.makeText(this, "Erro no firebase", Toast.LENGTH_SHORT).show()
    }
}
