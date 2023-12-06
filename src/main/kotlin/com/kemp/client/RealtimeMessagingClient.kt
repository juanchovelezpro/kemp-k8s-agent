package com.kemp.client

interface RealtimeMessagingClient {

    suspend fun consume()
    suspend fun send(data: Any)
    suspend fun close()
}