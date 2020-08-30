package com.mazenk.channelforwarder.messages

import com.jtelegram.api.chat.Chat
import com.jtelegram.api.kotlin.util.KTextBuilder
import com.jtelegram.api.kotlin.util.textBuilder
import com.jtelegram.api.message.CaptionableMessage
import com.jtelegram.api.message.Message
import com.jtelegram.api.requests.message.framework.req.EditMessageRequest
import com.jtelegram.api.requests.message.framework.req.SendableMessageRequest
import com.jtelegram.api.util.TextBuilder
import com.mazenk.channelforwarder.link.LinkData
import kotlin.reflect.KClass

abstract class MessageHandler<M: Message<*>> (
        val clazz: KClass<M>
) {
    companion object {
        val FORWARD_PATTERN = "#forward".toRegex(RegexOption.IGNORE_CASE)
    }

    abstract fun forward(linkData: LinkData, message: M): SendableMessageRequest<M>
    abstract fun edit(linkData: LinkData, message: M, forwardedMessageId: Int): EditMessageRequest<*>

    open fun shouldIgnoreMessage(message: M): Boolean {
        return false
    }

    protected fun shouldIgnoreByText(text: String?): Boolean {
        return !(text?.toLowerCase()?.contains("#forward") ?: false)
    }

    protected fun buildText(linkData: LinkData, content: String?, message: Message<*>): TextBuilder {
        return textBuilder {
            +"${linkData.tag} "

            if (content != null) {
                +content.replace(FORWARD_PATTERN, "").trim()
            }

            footer(message.chat)
        }
    }

    protected fun <T> createCaption(linkData: LinkData, message: CaptionableMessage<T>): String {
        return buildText(linkData, message.caption, message).toHtml()
    }


    private fun KTextBuilder.footer(origin: Chat) {
        newLines(2)

        italics("Forwarded from ")
        link (
                "@${origin.username}",
                "https://t.me/${origin.username}"
        )
    }
}