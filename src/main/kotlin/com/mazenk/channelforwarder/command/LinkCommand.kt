package com.mazenk.channelforwarder.command

import com.jtelegram.api.chat.Chat
import com.jtelegram.api.chat.ChatType
import com.jtelegram.api.chat.id.ChatId
import com.jtelegram.api.ex.TelegramException
import com.jtelegram.api.kotlin.commands.suspendCommand
import com.jtelegram.api.kotlin.events.message.replyWith
import com.jtelegram.api.kotlin.execute
import com.jtelegram.api.kotlin.util.textBuilder
import com.jtelegram.api.requests.chat.GetChat
import com.jtelegram.api.requests.chat.GetChatMember
import com.mazenk.channelforwarder.link.LinkController
import com.mazenk.channelforwarder.link.LinkData

val linkCommand = suspendCommand { event, command ->
    if (isNotAuthorized(event)) {
        return@suspendCommand
    }

    if (command.args.size < 3) {
        event.replyWith(textBuilder {
            bold("Command Format: ")
            +"/link @originChannel @destinationChannel #Tag"
        })
        return@suspendCommand
    }

    suspend fun findChatByArg(index: Int): Chat? {
        val name = command.args[index]

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

    event.replyWith("Fetching chat info...")

    val origin = findChatByArg(0) ?: return@suspendCommand
    val destination = findChatByArg(1) ?: return@suspendCommand

    val destinationBotMember = bot.execute (
            GetChatMember.builder()
                    .chatId(destination.chatId)
                    .userId(bot.botInfo.id)
                    .build()
    )

    if (!destinationBotMember.isCanPostMessages) {
        event.replyWith("The bot does not have permissions to post in the destination channel!")
        return@suspendCommand
    }

    val tag = command.args[2]

    if (!tag.startsWith("#")) {
        event.replyWith("Tag must start with #!")
        return@suspendCommand
    }

    LinkController.createLink (
            origin.id,
            destination.id,
            LinkData (
                    tag,
                    command.args[1]
            )
    )

    event.replyWith(textBuilder {
        bold(origin.username)

        +" has been linked to "

        bold(destination.username)

        +" with tag "

        italics(tag)
    })
}
