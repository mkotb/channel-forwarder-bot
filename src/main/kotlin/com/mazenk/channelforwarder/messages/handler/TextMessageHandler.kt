package com.mazenk.channelforwarder.messages.handler

import com.jtelegram.api.message.entity.MessageEntityType
import com.jtelegram.api.message.impl.TextMessage
import com.jtelegram.api.requests.message.edit.EditTextMessage
import com.jtelegram.api.requests.message.framework.req.EditMessageRequest
import com.jtelegram.api.requests.message.framework.req.SendableMessageRequest
import com.jtelegram.api.requests.message.send.SendText
import com.mazenk.channelforwarder.link.LinkData
import com.mazenk.channelforwarder.messages.MessageHandler

object TextMessageHandler: MessageHandler<TextMessage>(TextMessage::class) {
    private fun shouldDisablePreview(message: TextMessage): Boolean {
        return message.entities.none { it.type == MessageEntityType.TEXT_LINK }
    }

    override fun forward(linkData: LinkData, message: TextMessage): SendableMessageRequest<TextMessage> {
        return SendText.builder()
                .text(buildText(linkData, message.text, message))
                .disableWebPagePreview(shouldDisablePreview(message))
                .build()
    }

    override fun edit(linkData: LinkData, message: TextMessage, forwardedMessageId: Int): EditMessageRequest<TextMessage> {
        return EditTextMessage.builder()
                .messageId(forwardedMessageId)
                .text(buildText(linkData, message.text, message))
                .disableWebPagePreview(shouldDisablePreview(message))
                .build()
    }

    override fun shouldIgnoreMessage(message: TextMessage): Boolean {
        return shouldIgnoreByText(message.text)
    }
}