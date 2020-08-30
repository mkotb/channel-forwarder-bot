package com.mazenk.channelforwarder

import com.jtelegram.api.TelegramBotRegistry
import java.io.File
import java.io.FileReader
import java.nio.file.Files

data class BotConfig (
        val redisUrl: String = "redis://localhost",
        val apiKey: String = "Insert API key here",
        val adminUserIds: Set<Long> = setOf(123L, 1234L)
)

fun loadBotConfig(): BotConfig? {
    val configPath = System.getenv("CONFIG_PATH")

    if (configPath == null || configPath.isEmpty()) {
        println("Config path is not configured!")
        return null
    }

    val file = File(configPath)

    if (file.isDirectory) {
        println("Config path is not configured correctly!")
        return null
    }

    if (!file.exists()) {
        val config = BotConfig()

        file.createNewFile()
        Files.write(file.toPath(), TelegramBotRegistry.GSON.toJson(config).split("\n"))

        println("Default config written. Please configure it to proceed.")
        return null
    }

    return TelegramBotRegistry.GSON.fromJson (
            FileReader(file),
            BotConfig::class.java
    )
}