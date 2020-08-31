package com.mazenk.channelforwarder.command

import com.jtelegram.api.kotlin.commands.suspendCommand
import com.jtelegram.api.kotlin.events.message.replyWith
import com.jtelegram.api.kotlin.util.textBuilder
import com.mazenk.channelforwarder.link.LinkController

val unlinkCommand = suspendCommand { event, command ->
    if (isNotAuthorized(event)) {
        return@suspendCommand
    }

    if (command.args.size < 2) {
        event.replyWith(textBuilder {
            bold("Command Format: ")
            +"/unlink @originChannel @destinationChannel"
        })
        return@suspendCommand
    }

    event.replyWith("Fetching chat info...")

    val origin = findChatByArg(event, 0) ?: return@suspendCommand
    val destination = findChatByArg(event, 1) ?: return@suspendCommand
    val result = LinkController.deleteLink(origin.id, destination.id)

    if (!result) {
        event.replyWith(textBuilder {
            +"No link found between "

            bold(origin.username)

            +" and "

            bold(destination.username)
        })
        return@suspendCommand
    }

    event.replyWith(textBuilder {
        +"Link between "

        bold(origin.username)

        +" and "

        bold(destination.username)

        +" has been deleted."
    })
}
