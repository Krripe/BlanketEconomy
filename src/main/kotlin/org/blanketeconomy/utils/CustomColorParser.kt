package org.blanketeconomy.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object CustomColorParser {
    private val miniMessage = MiniMessage.miniMessage()

    private fun toNative(displayname: String): Text? {
        return toNative(miniMessage.deserialize(replaceNative(displayname)))
    }

    private fun toNative(component: Component): Text? {
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component))
    }

    private fun replaceNative(displayname: String): String {
        var processedDisplayName = displayname
        processedDisplayName = processedDisplayName.replace("&", "§")
            .replace("§0", "§r<black>")
            .replace("§1", "§r<dark_blue>")
            .replace("§2", "§r<dark_green>")
            .replace("§3", "§r<dark_aqua>")
            .replace("§4", "§r<dark_red>")
            .replace("§5", "§r<dark_purple>")
            .replace("§6", "§r<gold>")
            .replace("§7", "§r<gray>")
            .replace("§8", "§r<dark_gray>")
            .replace("§9", "§r<blue>")
            .replace("§a", "§r<green>")
            .replace("§b", "§r<aqua>")
            .replace("§c", "§r<red>")
            .replace("§d", "§r<light_purple>")
            .replace("§e", "§r<yellow>")
            .replace("§f", "§r<white>")
            .replace("§k", "<obfuscated>")
            .replace("§l", "<bold>")
            .replace("§m", "<strikethrough>")
            .replace("§n", "<underline>")
            .replace("§o", "<italic>")
            .replace("§r", "<reset>")
        return processedDisplayName
    }

    fun toNativeComponent(messageContent: String): MutableText {
        return Text.empty().append(toNative(messageContent))
    }
}