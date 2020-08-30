package com.mazenk.channelforwarder.messages.handler

import com.jtelegram.api.message.impl.TextMessage
import com.jtelegram.api.requests.message.edit.EditTextMessage
import com.jtelegram.api.requests.message.framework.req.EditMessageRequest
import com.jtelegram.api.requests.message.framework.req.SendableMessageRequest
import com.jtelegram.api.requests.message.send.SendText
import com.mazenk.channelforwarder.link.LinkData
import com.mazenk.channelforwarder.messages.MessageHandler

object TextMessageHandler: MessageHandler<TextMessage>(TextMessage::class) {
    override fun forward(linkData: LinkData, message: TextMessage): SendableMessageRequest<TextMessage> {
        return SendText.builder()
                .text(buildText(linkData, message.text, message))
                .disableWebPagePreview(true)
                .build()
    }

    override fun edit(linkData: LinkData, message: TextMessage, forwardedMessageId: Int): EditMessageRequest<TextMessage> {
        return EditTextMessage.builder()
                .messageId(forwardedMessageId)
                .text(buildText(linkData, message.text, message))
                .disableWebPagePreview(true)
                .build()
    }

    override fun shouldIgnoreMessage(message: TextMessage): Boolean {
        return shouldIgnoreByText(message.text)
    }
}