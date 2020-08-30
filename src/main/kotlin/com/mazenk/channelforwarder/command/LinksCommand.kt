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

    val originTag = command.args[0]
    val chat: Chat

    try {
        chat = bot.execute (
                GetChat.builder()
                        .chatId(ChatId.of(originTag))
                        .build()
        )
    } catch (ex: TelegramException) {
        event.replyWith(textBuilder {
            +"There was an error in finding channel $originTag."; newLines(2)

            +"Are you sure this bot has access to that channel?"
        })
        return@suspendCommand
    }

    val links = LinkController.findLinks(chat.id).values

    if (links.isEmpty()) {
        event.replyWith("No links found for $originTag!")
        return@suspendCommand
    }

    event.replyWith(textBuilder {
        bold("Links for "); +originTag; newLines(2)

        links.forEach { link ->
            +originTag; +" ➡️ "; +link.destinationTag; +" with tag "; italics(link.tag); newLine()
        }
    })
}
