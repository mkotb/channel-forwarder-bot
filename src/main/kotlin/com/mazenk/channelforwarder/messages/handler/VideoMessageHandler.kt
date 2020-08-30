package com.mazenk.channelforwarder.messages.handler

import com.jtelegram.api.message.impl.VideoMessage
import com.jtelegram.api.message.input.file.IdInputFile
import com.jtelegram.api.requests.message.framework.ParseMode
import com.jtelegram.api.requests.message.framework.req.SendableMessageRequest
import com.jtelegram.api.requests.message.send.SendVideo
import com.mazenk.channelforwarder.link.LinkData

object VideoMessageHandler: CaptionableMessageHandler<VideoMessage>(VideoMessage::class) {
    override fun forward(linkData: LinkData, message: VideoMessage): SendableMessageRequest<VideoMessage> {
        return SendVideo.builder()
                .video(IdInputFile.of(message.video))
                .caption(createCaption(linkData, message))
                .parseMode(ParseMode.HTML)
                .build()
    }

    override fun shouldIgnoreMessage(message: VideoMessage): Boolean {
        return super.shouldIgnoreMessage(message) || message.mediaGroupId != 0L
    }
}