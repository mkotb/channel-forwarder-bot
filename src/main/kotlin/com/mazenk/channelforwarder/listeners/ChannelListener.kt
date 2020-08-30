package com.mazenk.channelforwarder.listeners

import com.jtelegram.api.TelegramBot
import com.jtelegram.api.chat.Chat
import com.jtelegram.api.chat.ChatType
import com.jtelegram.api.chat.id.ChatId
import com.jtelegram.api.chat.id.LongChatId
import com.jtelegram.api.events.channel.ChannelPostEvent
import com.jtelegram.api.kotlin.events.KEventListener
import com.jtelegram.api.kotlin.execute
import com.jtelegram.api.kotlin.util.KTextBuilder
import com.jtelegram.api.kotlin.util.textBuilder
import com.jtelegram.api.message.CaptionableMessage
import com.jtelegram.api.message.Message
import com.jtelegram.api.message.impl.PhotoMessage
import com.jtelegram.api.message.impl.TextMessage
import com.jtelegram.api.message.impl.VideoMessage
import com.jtelegram.api.message.input.file.IdInputFile
import com.jtelegram.api.requests.message.framework.ParseMode
import com.jtelegram.api.requests.message.framework.req.SendableMessageRequest
import com.jtelegram.api.requests.message.send.SendPhoto
import com.jtelegram.api.requests.message.send.SendText
import com.jtelegram.api.requests.message.send.SendVideo
import com.mazenk.channelforwarder.link.LinkController
import com.mazenk.channelforwarder.link.LinkData
import kotlin.reflect.KClass

private class MessageSender<C: Any, M: Message<C>> (
        val clazz: KClass<M>,
        val fn: suspend (LinkData, M) -> SendableMessageRequest<M>?
) {
    suspend fun run(bot: TelegramBot, dest: LongChatId, linkData: LinkData, message: Message<*>) {
        if (message::class != clazz) {
            return
        }

        val req = fn(linkData, clazz.java.cast(message))?.apply {
            chatId = dest
        } ?: return

        try {
            bot.execute(req)
        } catch (ex: Exception) {
            println("An error occurred while trying to forward a message")
            ex.printStackTrace()
        }
    }
}

fun KTextBuilder.footer(origin: Chat) {
    newLines(2)

    italics("Forwarded from ")
    link (
            "@${origin.username}",
            "https://t.me/${origin.username}"
    )
}

fun <T> createCaption(linkData: LinkData, message: CaptionableMessage<T>): String {
    return textBuilder {
        +"${linkData.tag} "

        val caption = message.caption

        if (caption != null) {
            +caption
        }

        footer(message.chat)
    }.toHtml()
}

private val senders= listOf (
        MessageSender(TextMessage::class) { linkData, message ->
            SendText.builder()
                    .text(textBuilder {
                        +"${linkData.tag} "

                        +message.content

                        footer(message.chat)
                    })
                    // todo detect whether there is a link in message.content
                    .disableWebPagePreview(true)
                    .build()
        },
        MessageSender(PhotoMessage::class) { linkData, message ->
            if (message.mediaGroupId != 0L) {
                return@MessageSender null
            }

            SendPhoto.builder()
                    .photo(IdInputFile.of(message.highestResolutionPhoto))
                    .caption(createCaption(linkData, message))
                    .parseMode(ParseMode.HTML)
                    .build()
        },
        MessageSender(VideoMessage::class) { linkData, message ->
            SendVideo.builder()
                    .video(IdInputFile.of(message.video))
                    .caption(createCaption(linkData, message))
                    .parseMode(ParseMode.HTML)
                    .build()
        }
)

val channelListener: KEventListener<ChannelPostEvent> = listener@ { event ->
    val message = event.post

    if (message.chat.type != ChatType.CHANNEL) {
        return@listener
    }

    if (message.sender?.id == bot.botInfo.id) {
        return@listener
    }

    val links = LinkController.findLinks(message.chat.id)

    links.forEach { (dest, linkData) ->
        val destId = ChatId.of(dest)

        senders.forEach {
            it.run(bot, destId, linkData, message)
        }
    }
}

