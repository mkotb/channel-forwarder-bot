package com.mazenk.channelforwarder.command

import com.jtelegram.api.events.message.TextMessageEvent
import com.jtelegram.api.kotlin.events.message.replyWith
import com.mazenk.channelforwarder.GlobalContext

suspend fun isNotAuthorized(event: TextMessageEvent): Boolean {
    if (!GlobalContext.config.adminUserIds.contains(event.message.sender.id)) {
        event.replyWith("You are not authorized to use this bot!")
        return true
    }

    return false
}
