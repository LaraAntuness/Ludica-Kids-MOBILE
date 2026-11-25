
package com.example.mobilejogos

import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * LÚDICA KIDS
 *
 * Serviço de comunicação com o backend Railway.
 *
 * Funcionalidades:
 * - Login do responsável
 * - Cadastro do responsável e das crianças
 * - Consulta dos perfis infantis
 *
 * As requisições acontecem fora da thread principal
 * para não travar a interface do aplicativo.
 */
object ApiService {

    // ==========================================
    // ENDEREÇO DO BACKEND
    // ==========================================

    private const val BASE_URL =
        "https://ludicakids-backend.up.railway.app"

    private const val TEMPO_CONEXAO = 15000
    private const val TEMPO_RESPOSTA = 15000

    private val mainHandler = Handler(
        Looper.getMainLooper()
    )

    // ==========================================
    // MODELOS DE DADOS
    // ==========================================

    data class Crianca(
        val id: String = "",
        val nomeCompleto: String,
        val cpf: String,
        val nomeUsuario: String,
        val dataNascimento: String
    )

    data class Responsavel(
        val id: String,
        val nomeCompleto: String,
        val email: String,
        val cpf: String,
        val criancas: List<Crianca>
    )

    data class LoginResponse(
        val token: String,
        val responsavel: Responsavel,
        val mensagem: String
    )

    data class CadastroResponsavel(
        val nomeCompleto: String,
        val cpf: String,
        val rg: String,
        val email: String,
        val celular: String,
        val senhaResponsavel: String,
        val criancas: List<Crianca>
    )

    // ==========================================
    // EXCEÇÃO DA API
    // ==========================================

    class ApiException(
        val statusCode: Int,
        override val message: String
    ) : Exception(message)

    // ==========================================
    // EXECUTAR EM SEGUNDO PLANO
    // ==========================================

    private fun <T> executar(
        tarefa: () -> T,
        resultado: (Result<T>) -> Unit
    ) {

        Thread {

            val resposta = try {

                Result.success(tarefa())

            } catch (erro: Exception) {

                Result.failure(erro)
            }

            // Retorna à thread da interface.
            mainHandler.post {
                resultado(resposta)
            }

        }.start()
    }

    // ==========================================
    // REALIZAR REQUISIÇÃO HTTP
    // ==========================================

    private fun requisicao(
        caminho: String,
        metodo: String,
        corpo: JSONObject? = null,
        token: String? = null
    ): String {

        val url = URL(BASE_URL + caminho)

        val conexao = (
                url.openConnection() as HttpURLConnection
                )

        try {

            conexao.requestMethod = metodo

            conexao.connectTimeout = TEMPO_CONEXAO
            conexao.readTimeout = TEMPO_RESPOSTA

            conexao.setRequestProperty(
                "Accept",
                "application/json"
            )

            if (!token.isNullOrBlank()) {

                conexao.setRequestProperty(
                    "Authorization",
                    "Bearer $token"
                )
            }

            // Enviar JSON quando houver corpo.
            if (corpo != null) {

                conexao.doOutput = true

                conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )

                conexao.outputStream.use { saida ->

                    saida.write(
                        corpo.toString().toByteArray(
                            Charsets.UTF_8
                        )
                    )
                }
            }

            val codigo = conexao.responseCode

            val fluxo =
                if (codigo in 200..299) {
                    conexao.inputStream
                } else {
                    conexao.errorStream
                }

            val respostaTexto = fluxo?.use {
                it.bufferedReader(
                    Charsets.UTF_8
                ).readText()
            } ?: ""

            if (codigo !in 200..299) {

                var mensagem =
                    "Não foi possível concluir a solicitação."

                try {

                    val erroJson = JSONObject(
                        respostaTexto
                    )

                    mensagem = erroJson.optString(
                        "mensagem",
                        mensagem
                    )

                } catch (_: Exception) {
                    // Resposta sem JSON válido.
                }

                throw ApiException(
                    codigo,
                    mensagem
                )
            }

            return respostaTexto

        } finally {

            conexao.disconnect()
        }
    }

    // ==========================================
    // CONVERTER DADOS DE UMA CRIANÇA
    // ==========================================

    private fun converterCrianca(
        json: JSONObject
    ): Crianca {

        return Crianca(
            id = json.optString("_id"),
            nomeCompleto = json.optString(
                "nomeCompleto"
            ),
            cpf = json.optString("cpf"),
            nomeUsuario = json.optString(
                "nomeUsuario"
            ),
            dataNascimento = json.optString(
                "dataNascimento"
            )
        )
    }

    // ==========================================
    // CONVERTER LISTA DE CRIANÇAS
    // ==========================================

    private fun converterCriancas(
        array: JSONArray?
    ): List<Crianca> {

        if (array == null) {
            return emptyList()
        }

        val criancas = mutableListOf<Crianca>()

        for (i in 0 until array.length()) {

            val item = array.optJSONObject(i)

            if (item != null) {

                criancas.add(
                    converterCrianca(item)
                )
            }
        }

        return criancas
    }

    // ==========================================
    // CONVERTER RESPONSÁVEL
    // ==========================================

    private fun converterResponsavel(
        json: JSONObject
    ): Responsavel {

        return Responsavel(
            id = json.optString("_id"),
            nomeCompleto = json.optString(
                "nomeCompleto"
            ),
            email = json.optString("email"),
            cpf = json.optString("cpf"),
            criancas = converterCriancas(
                json.optJSONArray("criancas")
            )
        )
    }

    // ==========================================
    // CONVERTER RESPOSTA DE AUTENTICAÇÃO
    // ==========================================

    private fun converterAutenticacao(
        texto: String
    ): LoginResponse {

        val json = JSONObject(texto)

        val token = json.optString("token")

        val responsavelJson =
            json.optJSONObject("responsavel")

        if (
            token.isBlank() ||
            responsavelJson == null
        ) {

            throw ApiException(
                0,
                "O servidor retornou dados de autenticação incompletos."
            )
        }

        val responsavel = converterResponsavel(
            responsavelJson
        )

        if (responsavel.id.isBlank()) {

            throw ApiException(
                0,
                "Não foi possível identificar o responsável."
            )
        }

        return LoginResponse(
            token = token,
            responsavel = responsavel,
            mensagem = json.optString(
                "mensagem",
                "Operação realizada com sucesso."
            )
        )
    }

    // ==========================================
    // LOGIN DO RESPONSÁVEL
    // POST /responsaveis/login
    // ==========================================

    fun login(
        email: String,
        senha: String,
        callback: (Result<LoginResponse>) -> Unit
    ) {

        executar({

            if (
                email.isBlank() ||
                senha.isBlank()
            ) {

                throw ApiException(
                    400,
                    "Preencha o e-mail e a senha."
                )
            }

            val dados = JSONObject().apply {

                put(
                    "email",
                    email.trim().lowercase()
                )

                put(
                    "senhaResponsavel",
                    senha
                )
            }

            val resposta = requisicao(
                caminho = "/responsaveis/login",
                metodo = "POST",
                corpo = dados
            )

            converterAutenticacao(resposta)

        }, callback)
    }

    // ==========================================
    // CADASTRAR RESPONSÁVEL
    // POST /responsaveis
    // ==========================================

    fun cadastrar(
        dados: CadastroResponsavel,
        callback: (Result<LoginResponse>) -> Unit
    ) {

        executar({

            if (
                dados.nomeCompleto.isBlank() ||
                dados.cpf.isBlank() ||
                dados.rg.isBlank() ||
                dados.email.isBlank() ||
                dados.celular.isBlank()
            ) {

                throw ApiException(
                    400,
                    "Preencha todos os dados do responsável."
                )
            }

            if (dados.senhaResponsavel.length < 8) {

                throw ApiException(
                    400,
                    "A senha deve ter pelo menos 8 caracteres."
                )
            }

            if (dados.criancas.isEmpty()) {

                throw ApiException(
                    400,
                    "Cadastre pelo menos uma criança."
                )
            }

            val listaCriancas = JSONArray()

            dados.criancas.forEach { crianca ->

                val item = JSONObject().apply {

                    put(
                        "nomeCompleto",
                        crianca.nomeCompleto.trim()
                    )

                    put(
                        "cpf",
                        crianca.cpf.trim()
                    )

                    put(
                        "nomeUsuario",
                        crianca.nomeUsuario.trim()
                    )

                    put(
                        "dataNascimento",
                        crianca.dataNascimento
                    )
                }

                listaCriancas.put(item)
            }

            val corpo = JSONObject().apply {

                put(
                    "nomeCompleto",
                    dados.nomeCompleto.trim()
                )

                put(
                    "cpf",
                    dados.cpf.trim()
                )

                put(
                    "rg",
                    dados.rg.trim()
                )

                put(
                    "email",
                    dados.email.trim().lowercase()
                )

                put(
                    "celular",
                    dados.celular.trim()
                )

                put(
                    "senhaResponsavel",
                    dados.senhaResponsavel
                )

                put(
                    "criancas",
                    listaCriancas
                )
            }

            val resposta = requisicao(
                caminho = "/responsaveis",
                metodo = "POST",
                corpo = corpo
            )

            converterAutenticacao(resposta)

        }, callback)
    }

    // ==========================================
    // LISTAR CRIANÇAS DO RESPONSÁVEL
    // GET /criancas/:idResponsavel
    // ==========================================

    fun listarCriancas(
        idResponsavel: String,
        token: String,
        callback: (Result<List<Crianca>>) -> Unit
    ) {

        executar({

            if (
                idResponsavel.isBlank() ||
                token.isBlank()
            ) {

                throw ApiException(
                    401,
                    "Faça login para continuar."
                )
            }

            val resposta = requisicao(
                caminho = "/criancas/$idResponsavel",
                metodo = "GET",
                token = token
            )

            val array = JSONArray(resposta)

            converterCriancas(array)

        }, callback)
    }
}
