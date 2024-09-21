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
        val commands: List<String> = listOf("beco", "eco"),
    )

    data class EconomyConfig(
        val name: String,
        val lore: String,
        val material: String,
        val custommodeldata: Int,
        val currencyType: String,
        val balanceStart: BigDecimal,
        val symbol: String,
        var isPrimary: Boolean = false
    )

    data class Messages(
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
        val balanceHeader: String,
        val currencyLineFormat: String
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()
    lateinit var config: Config

    fun loadConfig(): Config {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            val defaultConfig = Config(
                commands = listOf(
                    "beco",
                    "eco"
                ),
                economy = mutableListOf(
                    EconomyConfig(
                        name = "&eCobble Coin",
                        lore = "&8This is a &bcurrency",
                        material = "minecraft:paper",
                        custommodeldata = 7381,
                        currencyType = "cobblecoins",
                        balanceStart = BigDecimal(500),
                        symbol = "$",
                        isPrimary = true
                    ),
                    EconomyConfig(
                        name = "&6Gold Coin",
                        lore = "&8This is a &epremium &8currency",
                        material = "minecraft:gold_ingot",
                        custommodeldata = 1234,
                        currencyType = "goldcoins",
                        balanceStart = BigDecimal(500),
                        symbol = "$",
                        isPrimary = false
                    )
                ),
                messages = Messages(

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
                    balanceHeader ="&6Balance&8:",
                    currencyLineFormat = "&8-> *7%currency%: &e%amount% &6%symbol%"
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

    fun getPrimaryCurrency(): EconomyConfig? {
        return config.economy.find { it.isPrimary }
    }

    fun getCurrencyTypeOrFallback(currencyType: String): String {
        val currency = config.economy.find { it.currencyType == currencyType }
        return currency?.currencyType ?: getPrimaryCurrency()?.currencyType ?: "default_currency"
    }

    fun getCurrencySymbol(currencyType: String): String {
        val currency = config.economy.find { it.currencyType == currencyType }
        return currency?.symbol ?: ""
    }

    fun getMessage(key: String, vararg args: Pair<String, String>): Text {
        var message = when (key) {
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
            "balanceHeader" -> config.messages.balanceHeader
            "currencyLineFormat" -> config.messages.currencyLineFormat
            else -> ""
        }

        args.forEach { (placeholder, value) ->
            message = message.replace("%$placeholder%", value)
        }

        return CustomColorParser.toNativeComponent(message)
    }
}