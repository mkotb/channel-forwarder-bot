package com.mazenk.channelforwarder.command

import com.jtelegram.api.kotlin.commands.suspendCommand
import com.jtelegram.api.kotlin.events.message.replyWith
import com.jtelegram.api.kotlin.util.textBuilder
import com.mazenk.channelforwarder.link.LinkController

val unlinkCommand = suspendCommand(wrapCommand { event, command ->
    if (isNotAuthorized(event)) {
        return@wrapCommand
    }

    if (command.args.size < 2) {
        event.replyWith(textBuilder {
            bold("Command Format: ")
            +"/unlink @originChannel @destinationChannel"
        })
        return@wrapCommand
    }

    event.replyWith("Fetching chat info...")

    val origin = findChatByArg(event, 0) ?: return@wrapCommand
    val destination = findChatByArg(event, 1) ?: return@wrapCommand
    val result = LinkController.deleteLink(origin.id, destination.id)

    if (!result) {
        event.replyWith(textBuilder {
            +"No link found between "

            bold(origin.username)

            +" and "

            bold(destination.username)
        })
        return@wrapCommand
    }

    event.replyWith(textBuilder {
        +"Link between "

        bold(origin.username)

        +" and "

        bold(destination.username)

        +" has been deleted."
    })
})
