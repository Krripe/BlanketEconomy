package org.blanketeconomy.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object CustomColorParser {
    private val miniMessage = MiniMessage.miniMessage()

    fun toNativeComponent(messageContent: String): MutableText? {
        val miniMessageString = replaceMinecraftCodesWithMiniMessage(messageContent)
        val component: Component = miniMessage.deserialize(miniMessageString)
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component))
    }

    private fun replaceMinecraftCodesWithMiniMessage(text: String): String {
        return text.replace("&", "§")
            .replace("§0", "<black>")
            .replace("§1", "<dark_blue>")
            .replace("§2", "<dark_green>")
            .replace("§3", "<dark_aqua>")
            .replace("§4", "<dark_red>")
            .replace("§5", "<dark_purple>")
            .replace("§6", "<gold>")
            .replace("§7", "<gray>")
            .replace("§8", "<dark_gray>")
            .replace("§9", "<blue>")
            .replace("§a", "<green>")
            .replace("§b", "<aqua>")
            .replace("§c", "<red>")
            .replace("§d", "<light_purple>")
            .replace("§e", "<yellow>")
            .replace("§f", "<white>")
            .replace("§k", "<obfuscated>")
            .replace("§l", "<bold>")
            .replace("§m", "<strikethrough>")
            .replace("§n", "<underline>")
            .replace("§o", "<italic>")
            .replace("§r", "<reset>")
    }
}
