package com.mazenk.channelforwarder.link

import com.jtelegram.api.TelegramBotRegistry.GSON
import com.mazenk.channelforwarder.GlobalContext

object LinkController {
    private fun formatKey(origin: Long): String {
        return "channelforwarder:links:$origin"
    }

    suspend fun createLink(origin: Long, destination: Long, data: LinkData) {
        GlobalContext.useRedis {
            it.hset(formatKey(origin), destination.toString(), GSON.toJson(data))
        }
    }

    suspend fun deleteLink(origin: Long, destination: Long): Boolean {
        return GlobalContext.useRedis {
            it.hdel(formatKey(origin), destination.toString()) == 1L
        }
    }

    suspend fun findFullLinks(chat: Long): Map<Long, LinkData> {
        val all = HashMap<Long, LinkData>()
        val linkKeys = GlobalContext.useRedis {
            it.keys("channelforwarder:links:*")
        }

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

    suspend fun findLinks(origin: Long): Map<Long, LinkData> {
        return GlobalContext.useRedis { redis ->
            redis.hgetAll(formatKey(origin))
        }.mapKeys {
            it.key.toLongOrNull() ?: Long.MIN_VALUE
        }.mapValues {
            GSON.fromJson(
                    it.value,
                    LinkData::class.java
            )
        }.filterKeys { it != Long.MIN_VALUE }
    }
}