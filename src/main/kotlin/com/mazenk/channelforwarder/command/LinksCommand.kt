package com.mazenk.channelforwarder.command

import com.jtelegram.api.chat.Chat
import com.jtelegram.api.chat.id.ChatId
import com.jtelegram.api.ex.TelegramException
import com.jtelegram.api.kotlin.commands.suspendCommand
import com.jtelegram.api.kotlin.events.message.replyWith
import com.jtelegram.api.kotlin.execute
import com.jtelegram.api.kotlin.util.textBuilder
import com.jtelegram.api.requests.chat.GetChat
import com.mazenk.channelforwarder.link.LinkController

val linksCommand = suspendCommand { event, command ->
    if (isNotAuthorized(event)) {
        return@suspendCommand
    }

    if (command.args.size < 1) {
        event.replyWith(textBuilder {
            bold("Command Format: ")
            +"/links @channel"
        })
        return@suspendCommand
    }

    val chatTag = command.args[0]
    val chat: Chat

    try {
        chat = bot.execute (
                GetChat.builder()
                        .chatId(ChatId.of(chatTag))
                        .build()
        )
    } catch (ex: TelegramException) {
        event.replyWith(textBuilder {
            +"There was an error in finding channel $chatTag."; newLines(2)

            +"Are you sure this bot has access to that channel?"
        })
        return@suspendCommand
    }

    val fullLinks = LinkController.findFullLinks(chat.id)
    val channelTags = HashMap<Long, String>()

    channelTags[chat.id] = chatTag

    if (fullLinks.isEmpty()) {
        event.replyWith("No links found for $chatTag!")
        return@suspendCommand
    }

    event.replyWith("Fetching chat info...")

    fullLinks.keys.forEach { tag ->
        if (channelTags.containsKey(tag)) {
            return@forEach
        }

        channelTags[tag] = try {
            bot.execute (
                    GetChat.builder()
                            .chatId(ChatId.of(tag))
                            .build()
            ).username
        } catch (ex: Exception) {
            "@ERROR_CHAT_UNAVAILABLE"
        }
    }

    event.replyWith(textBuilder {
        bold("Links for "); +chatTag; newLines(2)

        fullLinks.forEach { (origin, data) ->
            +"@${channelTags[origin]} ➡️  ${data.destinationTag} with tag "; italics(data.tag); newLine()
        }
    })
}
