package com.mazenk.channelforwarder.messages.handler

import com.jtelegram.api.message.impl.PhotoMessage
import com.jtelegram.api.message.input.file.IdInputFile
import com.jtelegram.api.requests.message.framework.ParseMode
import com.jtelegram.api.requests.message.framework.req.SendableMessageRequest
import com.jtelegram.api.requests.message.send.SendPhoto
import com.mazenk.channelforwarder.link.LinkData

object PhotoMessageHandler: CaptionableMessageHandler<PhotoMessage>(PhotoMessage::class) {
    override fun forward(linkData: LinkData, message: PhotoMessage): SendableMessageRequest<PhotoMessage> {
        return SendPhoto.builder()
                .photo(IdInputFile.of(message.highestResolutionPhoto))
                .caption(createCaption(linkData, message))
                .parseMode(ParseMode.HTML)
                .build()
    }

    override fun shouldIgnoreMessage(message: PhotoMessage): Boolean {
        return super.shouldIgnoreMessage(message) || message.mediaGroupId != 0L
    }
}