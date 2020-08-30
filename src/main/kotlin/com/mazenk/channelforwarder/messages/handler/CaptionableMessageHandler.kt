package com.mazenk.channelforwarder.messages.handler

import com.jtelegram.api.message.CaptionableMessage
import com.jtelegram.api.requests.message.edit.EditMessageCaption
import com.jtelegram.api.requests.message.framework.ParseMode
import com.jtelegram.api.requests.message.framework.req.EditMessageRequest
import com.mazenk.channelforwarder.link.LinkData
import com.mazenk.channelforwarder.messages.MessageHandler
import kotlin.reflect.KClass

abstract class CaptionableMessageHandler<M: CaptionableMessage<*>>(
        clazz: KClass<M>
): MessageHandler<M>(clazz) {
    override fun edit(linkData: LinkData, message: M, forwardedMessageId: Int): EditMessageRequest<*> {
        return EditMessageCaption.builder()
                .messageId(forwardedMessageId)
                .caption(createCaption(linkData, message))
                .parseMode(ParseMode.HTML)
                .build()
    }

    override fun shouldIgnoreMessage(message: M): Boolean {
        return shouldIgnoreByText(message.caption)
    }
}