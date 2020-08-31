package com.mazenk.channelforwarder.command

import com.jtelegram.api.chat.Chat
import com.jtelegram.api.chat.ChatType
import com.jtelegram.api.chat.id.ChatId
import com.jtelegram.api.events.message.TextMessageEvent
import com.jtelegram.api.ex.TelegramException
import com.jtelegram.api.kotlin.BotContext
import com.jtelegram.api.kotlin.events.message.replyWith
import com.jtelegram.api.kotlin.execute
import com.jtelegram.api.requests.chat.GetChat
import com.mazenk.channelforwarder.GlobalContext

suspend fun isNotAuthorized(event: TextMessageEvent): Boolean {
    if (!GlobalContext.config.adminUserIds.contains(event.message.sender.id)) {
        event.replyWith("You are not authorized to use this bot!")
        return true
    }

    return false
}

suspend fun BotContext.findChatByArg(event: TextMessageEvent, index: Int): Chat? {
    val name = event.message.content.split(" ")[index + 1]

    try {
        val chat = bot.execute(GetChat.builder().chatId(ChatId.of(name)).build())

        if (chat.type != ChatType.CHANNEL) {
            event.replyWith("$name must be a channel!")
            return null
        }

        return chat
    } catch (ex: TelegramException) {
        event.replyWith("There was a problem finding chat information for $name")
        return null
    }
}
