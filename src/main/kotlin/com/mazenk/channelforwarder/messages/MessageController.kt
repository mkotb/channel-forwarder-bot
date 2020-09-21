package com.mazenk.channelforwarder.messages

import com.jtelegram.api.TelegramBot
import com.jtelegram.api.chat.ChatType
import com.jtelegram.api.chat.id.ChatId
import com.jtelegram.api.kotlin.execute
import com.jtelegram.api.message.Message
import com.mazenk.channelforwarder.GlobalContext
import com.mazenk.channelforwarder.link.LinkController
import com.mazenk.channelforwarder.messages.handler.PhotoMessageHandler
import com.mazenk.channelforwarder.messages.handler.TextMessageHandler
import com.mazenk.channelforwarder.messages.handler.VideoMessageHandler
import kotlin.reflect.KClass

object MessageController {
    private const val INVALID_LONG = Long.MIN_VALUE
    private val handlers: List<MessageHandler<*>> = listOf (
            TextMessageHandler,
            PhotoMessageHandler,
            VideoMessageHandler
    )

    fun <T> shouldIgnoreMessage(bot: TelegramBot, message: Message<T>): Boolean {
        if (message.chat.type != ChatType.CHANNEL) {
            return true
        }

        if (message.sender?.id == bot.botInfo.id) {
            return true
        }

        return findHandler(message::class)?.shouldIgnoreMessage(message) ?: false
    }

    suspend fun <T> send(bot: TelegramBot, message: Message<T>) {
        val links = LinkController.findLinks(message.chat.id)

        links.forEach { (dest, linkData) ->
            val handler = findHandler(message::class) ?: return@forEach
            val req = handler.forward(linkData, message)

            req.chatId = ChatId.of(dest)

            try {
                val forwardedMessage = bot.execute(req)

                saveMessage(message, forwardedMessage)
            } catch (ex: Exception) {
                println("An error occurred while trying to forward a message")
                ex.printStackTrace()
            }
        }
    }

    suspend fun <T> edit(bot: TelegramBot, editedMessage: Message<T>) {
        val linkDataMap = LinkController.findLinks(editedMessage.chat.id)

        findLinkedMessages(editedMessage).forEach { (forwardedChat, forwardedMessageId) ->
            val linkData = linkDataMap[forwardedChat] ?: return@forEach
            val handler = findHandler(editedMessage::class) ?: return@forEach
            val req = handler.edit(linkData, editedMessage, forwardedMessageId.toInt())

            req.chatId = ChatId.of(forwardedChat)

            try {
                bot.execute(req)
            } catch (ex: Exception) {
                println("An error occurred while trying to update a forwarded message")
                ex.printStackTrace()
            }
        }
    }

    suspend fun hasLinkedMessages(message: Message<*>): Boolean {
        return GlobalContext.useRedis {
            it.exists(formatKey(message))
        }
    }

    suspend fun saveMessage(message: Message<*>, forwarded: Message<*>) {
        GlobalContext.useRedis {
            it.hset(formatKey(message), forwarded.chat.id.toString(), forwarded.messageId.toString())
        }
    }

    suspend fun findLinkedMessages(message: Message<*>): Map<Long, Long> {
        return GlobalContext.useRedis {
            it.hgetAll(formatKey(message))
        }.mapKeys {
            it.key.toLongOrNull() ?: INVALID_LONG
        }.mapValues {
            it.value.toLongOrNull() ?: INVALID_LONG
        }.filter {
            it.key != INVALID_LONG && it.value != INVALID_LONG
        }
    }

    private fun <T> findHandler(clazz: KClass<out Message<T>>): MessageHandler<Message<T>>? {
        return handlers
                .filterIsInstance<MessageHandler<Message<T>>>()
                .firstOrNull { it.clazz == clazz }
    }

    private fun formatKey(message: Message<*>): String {
        return "channelforwarder:messages:${message.chat.id}:${message.messageId}"
    }
}