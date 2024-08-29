package org.blanketeconomy.utils

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Style
import java.util.regex.Pattern

object CustomColorParser {

    private val colorMap = mapOf(
        "&0" to TextColor.fromRgb(0x000000), // Black
        "&1" to TextColor.fromRgb(0x0000AA), // Dark Blue
        "&2" to TextColor.fromRgb(0x00AA00), // Dark Green
        "&3" to TextColor.fromRgb(0x00AAAA), // Dark Aqua
        "&4" to TextColor.fromRgb(0xAA0000), // Dark Red
        "&5" to TextColor.fromRgb(0xAA00AA), // Dark Purple
        "&6" to TextColor.fromRgb(0xFFAA00), // Gold
        "&7" to TextColor.fromRgb(0xAAAAAA), // Gray
        "&8" to TextColor.fromRgb(0x555555), // Dark Gray
        "&9" to TextColor.fromRgb(0x5555FF), // Blue
        "&a" to TextColor.fromRgb(0x55FF55), // Green
        "&b" to TextColor.fromRgb(0x55FFFF), // Aqua
        "&c" to TextColor.fromRgb(0xFF5555), // Red
        "&d" to TextColor.fromRgb(0xFF55FF), // Light Purple
        "&e" to TextColor.fromRgb(0xFFFF55), // Yellow
        "&f" to TextColor.fromRgb(0xFFFFFF)  // White
    )

    fun parseCustomColoredText(input: String): MutableText {
        val regex = Pattern.compile("&x(?:&[0-9A-Fa-f]){6}|&[0-9A-Fa-f]|&[k-oK-OrR]")
        val matcher = regex.matcher(input)

        val result = Text.literal("")
        var currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))
        var lastIndex = 0

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                result.append(Text.literal(input.substring(lastIndex, matcher.start())).setStyle(currentStyle))
            }

            val match = matcher.group()
            when {
                match.startsWith("&x") -> {
                    val hex = match.substring(2).replace("&", "")
                    currentStyle = currentStyle.withColor(TextColor.fromRgb(hex.toInt(16)))
                }
                match.startsWith("&") -> {
                    when (match) {
                        in colorMap.keys -> {
                            colorMap[match]?.let {
                                currentStyle = currentStyle.withColor(it)
                            }
                        }
                        "&k" -> currentStyle = currentStyle.withObfuscated(true)
                        "&l" -> currentStyle = currentStyle.withBold(true)
                        "&m" -> currentStyle = currentStyle.withStrikethrough(true)
                        "&n" -> currentStyle = currentStyle.withUnderline(true)
                        "&o" -> currentStyle = currentStyle.withItalic(true)
                        "&r" -> currentStyle = Style.EMPTY
                    }
                }
            }
            lastIndex = matcher.end()
        }

        if (lastIndex < input.length) {
            result.append(Text.literal(input.substring(lastIndex)).setStyle(currentStyle))
        }

        return result
    }
}
