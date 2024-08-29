package org.blanketeconomy

import com.google.gson.GsonBuilder
import net.minecraft.text.Text
import java.io.File
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import org.blanketeconomy.utils.CustomColorParser

object Blanketconfig {

    fun saveConfig() {
        val configFile = Paths.get("config", "blanketeconomy", "config.json").toFile()
        configFile.writeText(GsonBuilder().setPrettyPrinting().create().toJson(config))
    }

    private val configFile = Paths.get("config","blanketeconomy", "config.json").toFile()

    data class Config(
        val economy: MutableList<EconomyConfig> = mutableListOf(),
        val messages: Messages,
    )

    data class EconomyConfig(
        val name: String,
        val lore: String,
        val material: String,
        val custommodeldata: Int,
        val currencyType: String,
        val balanceStart: BigDecimal
    )

    data class Messages(
        val balanceMessage: String,
        val paymentSuccess: String,
        val paymentReceived: String,
        val insufficientFunds: String,
        val addBalance: String,
        val withdrawSuccess: String,
        val withdrawFail: String,
        val reloadMessage: String,
        val removeBalance: String,
        val depositSuccess: String,
        val noValid: String,
        val playerNotFound: String,
        val currencyNotFound: String,
        val configReloadSuccess: String,
        val balanceInitialized: String
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()
    lateinit var config: Config

    fun loadConfig(): Config {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            val defaultConfig = Config(
                economy = mutableListOf(
                    EconomyConfig(
                        name = "&eCobble Coin",
                        lore = "&8This is a &bcurrency",
                        material = "minecraft:paper",
                        custommodeldata = 7381,
                        currencyType = "cobblecoins",
                        balanceStart = BigDecimal(500)
                    ),
                    EconomyConfig(
                        name = "&6Gold Coin",
                        lore = "&8This is a &epremium &8currency",
                        material = "minecraft:gold_ingot",
                        custommodeldata = 1234,
                        currencyType = "goldcoins",
                        balanceStart = BigDecimal(500)
                    )
                ),
                messages = Messages(
                    balanceMessage = "&6Your balance&7: &b%balance% &e%currencyType%",
                    paymentSuccess = "&aSuccessfully paid &e%amount% &ato &b%player%!",
                    paymentReceived = "&aYou have received &e%amount% &afrom &b%player%!",
                    insufficientFunds = "&cInsufficient funds!",
                    addBalance = "&aAdded &e%amount% &ato &b%player%!",
                    withdrawSuccess = "&aSuccessfully withdrew &e%amount% &b%currencyType%!",
                    withdrawFail = "&cInsufficient funds!",
                    reloadMessage = "&bCobbleEconomy configuration reloaded!",
                    removeBalance = "&cRemoved &e%amount% &cfrom &e%player%!",
                    depositSuccess = "&aSuccessfully deposited &e%amount% &b%currencyType%!",
                    noValid = "&cInvalid funds!",
                    playerNotFound = "&cPlayer &e%player% &cnot found.",
                    currencyNotFound = "&cCurrency configuration not found for &e%currencyType%!",
                    configReloadSuccess = "&aConfiguration reloaded successfully!",
                    balanceInitialized = "&aYour balance has been initialized to &e%balance% &b%currency%."
                )
            )
            configFile.writeText(gson.toJson(defaultConfig))
            return defaultConfig
        }
        val json = Files.readString(configFile.toPath())
        config = gson.fromJson(json, Config::class.java)
        return config
    }

    fun getBalance(playerId: UUID, currencyType: String): BigDecimal {
        val playerFile = File("config/blanketeconomy/user/${playerId}.json")
        if (!playerFile.exists()) {
            val startBalance = config.economy.find { it.currencyType == currencyType }?.balanceStart ?: BigDecimal.ZERO
            setBalance(playerId, startBalance, currencyType)
            return startBalance
        }
        val json = Files.readString(playerFile.toPath())
        val jsonObject = com.google.gson.JsonParser.parseString(json).asJsonObject
        return jsonObject.get(currencyType)?.asBigDecimal ?: BigDecimal.ZERO
    }

    fun setBalance(playerId: UUID, balance: BigDecimal, currencyType: String) {
        val playerFile = File("config/blanketeconomy/user/${playerId}.json")
        val jsonObject = if (playerFile.exists()) {
            com.google.gson.JsonParser.parseString(Files.readString(playerFile.toPath())).asJsonObject
        } else {
            com.google.gson.JsonObject()
        }

        jsonObject.addProperty(currencyType, balance)

        playerFile.parentFile.mkdirs()
        playerFile.writeText(gson.toJson(jsonObject))
    }

    fun initializeUserData(uuid: UUID, currencyType: String) {
        val playerFile = File("config/blanketeconomy/user/${uuid}.json")
        val jsonObject = if (playerFile.exists()) {
            com.google.gson.JsonParser.parseString(Files.readString(playerFile.toPath())).asJsonObject
        } else {
            com.google.gson.JsonObject()
        }

        if (!jsonObject.has(currencyType)) {
            val startBalance = config.economy.find { it.currencyType == currencyType }?.balanceStart ?: 0
            jsonObject.addProperty(currencyType, startBalance)
        }

        playerFile.parentFile.mkdirs()
        playerFile.writeText(gson.toJson(jsonObject))
    }

    fun hasUserData(uuid: UUID, currencyType: String): Boolean {
        val playerFile = File("config/blanketeconomy/user/${uuid}.json")
        if (!playerFile.exists()) {
            return false
        }
        val jsonObject = com.google.gson.JsonParser.parseString(Files.readString(playerFile.toPath())).asJsonObject
        return jsonObject.has(currencyType)
    }

    fun reloadConfig(): Config {
        return loadConfig()
    }

    fun getMessage(key: String, vararg args: Pair<String, String>): Text {
        var message = when (key) {
            "balanceMessage" -> config.messages.balanceMessage
            "paymentSuccess" -> config.messages.paymentSuccess
            "paymentReceived" -> config.messages.paymentReceived
            "insufficientFunds" -> config.messages.insufficientFunds
            "addBalance" -> config.messages.addBalance
            "withdrawSuccess" -> config.messages.withdrawSuccess
            "withdrawFail" -> config.messages.withdrawFail
            "reloadMessage" -> config.messages.reloadMessage
            "removeBalance" -> config.messages.removeBalance
            "depositSuccess" -> config.messages.depositSuccess
            "noValid" -> config.messages.noValid
            "playerNotFound" -> config.messages.playerNotFound
            "currencyNotFound" -> config.messages.currencyNotFound
            "configReloadSuccess" -> config.messages.configReloadSuccess
            "balanceInitialized" -> config.messages.balanceInitialized
            else -> ""
        }

        args.forEach { (placeholder, value) ->
            message = message.replace("%$placeholder%", value)
        }

        return CustomColorParser.parseCustomColoredText(message)
    }
}