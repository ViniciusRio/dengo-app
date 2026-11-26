package com.viniciusrio.dengo.data.net

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * O cliente de todas as chamadas ao backend. Sem repetição silenciosa: um comando que perdeu a
 * conexão depois de enviado fica incerto, e só a pessoa decide reenviar (spec `couple-sync`).
 */
internal fun dengoHttpClient(timeoutMillis: Int): OkHttpClient = OkHttpClient.Builder()
    .retryOnConnectionFailure(false)
    .followRedirects(false)
    .connectTimeout(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
    .readTimeout(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
    .writeTimeout(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
    .build()
