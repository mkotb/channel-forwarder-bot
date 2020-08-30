package com.mazenk.channelforwarder.listeners

import com.jtelegram.api.events.channel.ChannelPostEvent
import com.jtelegram.api.kotlin.events.KEventListener
import com.mazenk.channelforwarder.messages.MessageController

val channelListener: KEventListener<ChannelPostEvent> = listener@ { event ->
    val message = event.post

    if (MessageController.shouldIgnoreMessage(bot, message)) {
        return@listener
    }

    MessageController.send(bot, message)
}

