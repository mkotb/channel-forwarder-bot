package com.mazenk.channelforwarder

import redis.clients.jedis.Jedis
import java.net.URI
import kotlin.system.exitProcess

object GlobalContext {
    val config: BotConfig
    val redis: Jedis

    init {
        val loaded: BotConfig?

        try {
            loaded = loadBotConfig()
        } catch (ex: Exception) {
            println("An unexpected error occured during config load")
            ex.printStackTrace()
            exitProcess(-127)
        }

        config = loaded ?: exitProcess(0)
        redis = resolveRedis()
    }

    private fun resolveRedis(): Jedis {
        var localRedis: Jedis? = null

        while (localRedis == null) {
            try {
                localRedis = Jedis(URI(config.redisUrl))
            } catch (ex: Exception) {
                println("Unable to connect to redis")
                ex.printStackTrace()

                println("Trying again in 5 seconds...")
                Thread.sleep(5000L)
            }
        }

        return localRedis
    }
}