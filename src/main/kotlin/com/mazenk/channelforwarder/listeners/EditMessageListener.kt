package com.mazenk.channelforwarder.listeners

import com.jtelegram.api.events.channel.ChannelPostEditEvent
import com.jtelegram.api.kotlin.events.KEventListener
import com.mazenk.channelforwarder.messages.MessageController

val editListener: KEventListener<ChannelPostEditEvent> = listener@ { event ->
    val message = event.post

    if (MessageController.shouldIgnoreMessage(bot, message)) {
        return@listener
    }

    if (!MessageController.hasLinkedMessages(message)) {
        MessageController.send(bot, message)
        return@listener
    }

    MessageController.edit(bot, message)
}