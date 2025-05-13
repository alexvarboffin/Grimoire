package com.walhalla.network

import io.ktor.client.HttpClient

expect fun platform(): String

// expect функция для предоставления HttpClient
expect fun provideHttpClient(): HttpClient