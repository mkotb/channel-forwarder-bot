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

    fun deleteLink(origin: Long, destination: Long): Boolean {
        return GlobalContext.redis.hdel(formatKey(origin), destination.toString()) == 1L
    }

    fun findFullLinks(chat: Long): Map<Long, LinkData> {
        val all = HashMap<Long, LinkData>()
        val linkKeys = GlobalContext.redis.keys("channelforwarder:links:*")

        for (key in linkKeys) {
            val origin = key.substring(23).toLongOrNull() ?: continue
            val destData = findLinks(origin).entries.toMutableSet()

            if (origin != chat) {
                destData.removeIf {
                    it.key != chat
                }
            }

            destData.forEach { destination ->
                all[origin] = destination.value
            }
        }

        return all
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