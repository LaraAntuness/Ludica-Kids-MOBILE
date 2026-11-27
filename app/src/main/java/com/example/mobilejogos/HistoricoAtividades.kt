package com.example.mobilejogos

import android.content.Context
import android.app.Activity
import android.os.SystemClock
import org.json.JSONArray
import org.json.JSONObject
import java.util.WeakHashMap

object HistoricoAtividades {
    data class Registro(val jogo: String, val nivel: String, val data: Long)
    private data class Sessao(val chave: String, var inicio: Long = 0L)
    private val sessoes = WeakHashMap<Activity, Sessao>()
    private fun prefs(c: Context) = c.getSharedPreferences("historico_ludicakids", Context.MODE_PRIVATE)
    private fun chave(r: String, c: String) = "${r.length}:$r:$c"
    private fun chaveAtual(c: Context): String? {
        val r = LudicaSessao.obter(c)?.second ?: return null
        val p = c.getSharedPreferences("LudicaKids", Context.MODE_PRIVATE)
        if (p.getString("responsavelDaCriancaSelecionada", null) != r) return null
        val id = runCatching { JSONObject(p.getString("criancaSelecionada", "") ?: "").optString("_id") }.getOrDefault("")
        return if (id.isBlank()) null else chave(r, id)
    }
    fun iniciar(a: Activity) {
        val k = chaveAtual(a) ?: return
        val s = sessoes.getOrPut(a) { Sessao(k) }
        if (s.chave == k && s.inicio == 0L) s.inicio = SystemClock.elapsedRealtime()
    }
    fun pausar(a: Activity) {
        val s = sessoes[a] ?: return
        if (s.inicio == 0L) return
        val ms = (SystemClock.elapsedRealtime() - s.inicio).coerceAtLeast(0L)
        s.inicio = 0L
        val p = prefs(a)
        val k = "tempo:${s.chave}"
        p.edit().putLong(k, p.getLong(k, 0L) + ms).apply()
    }
    fun registrar(a: Activity, jogo: String, nivel: String) {
        val k = chaveAtual(a) ?: return
        if (sessoes[a]?.chave != k) return
        val p = prefs(a)
        val atual = runCatching { JSONArray(p.getString(k, "[]") ?: "[]") }.getOrElse { JSONArray() }
        val nova = JSONArray().put(JSONObject().put("jogo", jogo).put("nivel", nivel).put("data", System.currentTimeMillis()))
        for (i in 0 until minOf(atual.length(), 199)) nova.put(atual.getJSONObject(i))
        val unicos = p.getStringSet("niveis:$k", emptySet())?.toMutableSet() ?: mutableSetOf()
        unicos.add("$jogo • $nivel")
        val jogos = p.getStringSet("jogos:$k", emptySet())?.toMutableSet() ?: mutableSetOf()
        jogos.add(jogo)
        p.edit().putString(k, nova.toString()).putStringSet("niveis:$k", unicos).putStringSet("jogos:$k", jogos).apply()
    }
    fun listar(c: Context, r: String, id: String): List<Registro> {
        if (LudicaSessao.obter(c)?.second != r) return emptyList()
        val a = runCatching { JSONArray(prefs(c).getString(chave(r,id), "[]") ?: "[]") }.getOrElse { JSONArray() }
        return (0 until a.length()).mapNotNull { i -> a.optJSONObject(i)?.let { Registro(it.optString("jogo"), it.optString("nivel"), it.optLong("data")) } }
    }
    fun resumo(c: Context, r: String, id: String): Triple<Int,Int,Long> {
        if (LudicaSessao.obter(c)?.second != r) return Triple(0,0,0L)
        val p = prefs(c); val k = chave(r,id)
        return Triple(p.getStringSet("jogos:$k", emptySet())?.size ?: 0, p.getStringSet("niveis:$k", emptySet())?.size ?: 0, p.getLong("tempo:$k",0L)/60000)
    }
}
