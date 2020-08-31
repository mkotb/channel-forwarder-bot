package com.mazenk.channelforwarder.command

import com.jtelegram.api.kotlin.commands.suspendCommand
import com.jtelegram.api.kotlin.events.message.replyWith
import com.jtelegram.api.kotlin.execute
import com.jtelegram.api.kotlin.util.textBuilder
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

    event.replyWith("Fetching chat info...")

    val origin = findChatByArg(event, 0) ?: return@suspendCommand
    val destination = findChatByArg(event, 1) ?: return@suspendCommand

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
