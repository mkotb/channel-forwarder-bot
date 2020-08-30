package com.mazenk.channelforwarder.link

import com.jtelegram.api.TelegramBotRegistry.GSON
import com.mazenk.channelforwarder.GlobalContext

object LinkController {
    private fun formatKey(origin: Long): String {
        return "channelforwarder:links:$origin"
    }

    fun createLink(origin: Long, destination: Long, data: LinkData) {
        GlobalContext.redis.hset(formatKey(origin), destination.toString(), GSON.toJson(data))
    }

    fun findLinks(origin: Long): Map<Long, LinkData> {
        return GlobalContext.redis.hgetAll(formatKey(origin))
                .mapKeys {
                    it.key.toLongOrNull() ?: Long.MIN_VALUE
                }
                .mapValues {
                    GSON.fromJson (
                            it.value,
                            LinkData::class.java
                    )
                }
                .filterKeys { it != Long.MIN_VALUE }
    }
}