package com.mazenk.channelforwarder

import com.jtelegram.api.TelegramBotRegistry
import com.jtelegram.api.events.Event
import com.jtelegram.api.events.channel.ChannelPostEditEvent
import com.jtelegram.api.events.channel.ChannelPostEvent
import com.jtelegram.api.kotlin.events.KEventListener
import com.jtelegram.api.kotlin.events.on
import com.jtelegram.api.kotlin.registerBot
import com.jtelegram.api.update.PollingUpdateProvider
import com.mazenk.channelforwarder.command.linkCommand
import com.mazenk.channelforwarder.command.linksCommand
import com.mazenk.channelforwarder.command.unlinkCommand
import com.mazenk.channelforwarder.listeners.channelListener
import com.mazenk.channelforwarder.listeners.editListener
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val botRegistry = TelegramBotRegistry.builder()
            .updateProvider(PollingUpdateProvider.builder().build())
            .build()

    val bot = botRegistry.registerBot(GlobalContext.config.apiKey)

    bot.commandRegistry.registerCommand("link", linkCommand)
    bot.commandRegistry.registerCommand("links", linksCommand)
    bot.commandRegistry.registerCommand("unlink", unlinkCommand)

    bot.eventRegistry.on(ChannelPostEvent::class, wrapListener(channelListener))
    bot.eventRegistry.on(ChannelPostEditEvent::class, wrapListener(editListener))

    println("${bot.botInfo.username} has started")
}

private fun <T: Event> wrapListener(listener: KEventListener<T>): KEventListener<T> {
    return { event ->
        try {
            listener(event)
        } catch (ex: Exception) {
            println("There was an error from a listener!")
            ex.printStackTrace()
        }
    }
}
