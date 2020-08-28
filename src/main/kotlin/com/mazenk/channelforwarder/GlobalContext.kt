package com.mazenk.channelforwarder

import redis.clients.jedis.Jedis
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
        redis = Jedis(config.redisUrl)

        val pass = config.redisPassword

        if (pass?.isNotEmpty() == true) {
            redis.auth(pass)
        }
    }
}