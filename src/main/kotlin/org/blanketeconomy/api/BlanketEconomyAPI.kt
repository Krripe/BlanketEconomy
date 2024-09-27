package org.blanketeconomy.api

import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.blanketeconomy.Blanketconfig
import java.math.BigDecimal
import java.util.*

data class Transaction(val playerId: UUID, val currencyType: String, val amount: BigDecimal, val type: String, val timestamp: Long)

interface EconomyEventListener {
    fun onBalanceChanged(playerId: UUID, currencyType: String, newBalance: BigDecimal)
}

class BlanketEconomyAPI(override val server: MinecraftServer) : EconomyAPI {
    private val listeners = mutableListOf<EconomyEventListener>()
    private val transactionHistory = mutableMapOf<UUID, MutableList<Transaction>>()

    override fun getBalance(playerId: UUID, currencyType: String): BigDecimal {
        val actualCurrencyType = if (currencyType.isBlank() || !currencyExists(currencyType)) {
            Blanketconfig.getPrimaryCurrency()?.currencyType ?: "default_currency"
        } else {
            currencyType
        }

        return Blanketconfig.getBalance(playerId, actualCurrencyType)
    }

    override fun getCurrencyList(): List<Blanketconfig.EconomyConfig> {
        return Blanketconfig.config.economy
    }

    override fun currencyExists(currencyType: String): Boolean {
        return BlanketEconomy.getAPI().getCurrencyList().any { it.currencyType == currencyType }
    }

    override fun setBalance(playerId: UUID, balance: BigDecimal, currencyType: String) {
        val actualCurrencyType = if (currencyType.isBlank() || !currencyExists(currencyType)) {
            Blanketconfig.getPrimaryCurrency()?.currencyType ?: "default_currency"
        } else {
            currencyType
        }

        Blanketconfig.setBalance(playerId, balance, actualCurrencyType)

        listeners.forEach { it.onBalanceChanged(playerId, actualCurrencyType, balance) }
    }

    override fun addBalance(playerId: UUID, amount: BigDecimal, currencyType: String) {
        val currentBalance = getBalance(playerId, currencyType)
        setBalance(playerId, currentBalance.add(amount), currencyType)
        logTransaction(playerId, currencyType, amount, "credit")
    }

    override fun subtractBalance(playerId: UUID, amount: BigDecimal, currencyType: String): Boolean {
        val currentBalance = getBalance(playerId, currencyType)
        return if (currentBalance >= amount) {
            setBalance(playerId, currentBalance.subtract(amount), currencyType)
            logTransaction(playerId, currencyType, amount, "debit")
            true
        } else {
            false
        }
    }

    override fun transfer(fromPlayerId: UUID, toPlayerId: UUID, amount: BigDecimal, currencyType: String): Boolean {
        return if (subtractBalance(fromPlayerId, amount, currencyType)) {
            addBalance(toPlayerId, amount, currencyType)
            logTransaction(fromPlayerId, currencyType, amount, "transfer_out")
            logTransaction(toPlayerId, currencyType, amount, "transfer_in")
            true
        } else {
            false
        }
    }

    override fun addEventListener(listener: EconomyEventListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: EconomyEventListener) {
        listeners.remove(listener)
    }

    override fun conditionalTransaction(playerId: UUID, amount: BigDecimal, currencyType: String, condition: (BigDecimal) -> Boolean): Boolean {
        val balance = getBalance(playerId, currencyType)
        return if (condition(balance)) {
            subtractBalance(playerId, amount, currencyType)
        } else {
            false
        }
    }

    override fun logTransaction(playerId: UUID, currencyType: String, amount: BigDecimal, type: String) {
        val transactions = transactionHistory.getOrPut(playerId) { mutableListOf() }
        transactions.add(Transaction(playerId, currencyType, amount, type, System.currentTimeMillis()))
    }

    override fun getTransactionHistory(playerId: UUID): List<Transaction> {
        return transactionHistory[playerId] ?: emptyList()
    }

    override fun createCurrency(
        name: String,
        lore: String,
        material: String,
        customModelData: Int,
        balanceStart: BigDecimal,
        symbol: String,
        isPrimary: Boolean
    ): Boolean {
        val newCurrency = Blanketconfig.EconomyConfig(
            name = name,
            lore = lore,
            material = material,
            custommodeldata = customModelData,
            currencyType = name.lowercase(Locale.getDefault()).replace(" ", "_"),
            balanceStart = balanceStart,
            symbol = symbol,
            isPrimary = isPrimary
        )
        Blanketconfig.config.economy.add(newCurrency)

        if (isPrimary) {
            Blanketconfig.config.economy.forEach {
                if (it.currencyType != newCurrency.currencyType) {
                    it.isPrimary = false
                }
            }
        }

        Blanketconfig.saveConfig()
        return true
    }

    override fun hasEnoughFunds(playerId: UUID, amount: BigDecimal, currencyType: String): Boolean {
        return getBalance(playerId, currencyType) >= amount
    }

    override fun manageInventory(playerId: UUID, itemStack: ItemStack, action: InventoryAction): Boolean {
        val player = getPlayerById(playerId)
        if (player != null) {
            when (action) {
                InventoryAction.ADD -> player.inventory.offerOrDrop(itemStack)
                InventoryAction.REMOVE -> player.inventory.removeStack(player.inventory.getSlotWithStack(itemStack))
            }
            return true
        }
        return false
    }

    override fun processBatchTransactions(transactions: List<Transaction>): Boolean {
        val successfulTransactions = mutableListOf<Transaction>()
        try {
            transactions.forEach { transaction ->
                if (!subtractBalance(transaction.playerId, transaction.amount, transaction.currencyType)) {
                    throw IllegalStateException("Failed to process transaction for ${transaction.playerId}")
                }
                successfulTransactions.add(transaction)
            }
            return true
        } catch (e: Exception) {
            successfulTransactions.forEach { transaction ->
                addBalance(transaction.playerId, transaction.amount, transaction.currencyType)
            }
            return false
        }
    }

    override fun addCurrencyItem(playerId: UUID, itemStack: ItemStack, amount: Int): Boolean {
        val player = getPlayerById(playerId) ?: return false
        val currencyItem = itemStack.copy()
        currencyItem.count = amount

        return manageInventory(playerId, currencyItem, InventoryAction.ADD)
    }

    override fun removeCurrencyItem(playerId: UUID, itemStack: ItemStack, amount: Int): Boolean {
        val player = getPlayerById(playerId) ?: return false
        val currentBalance = getCurrencyItemBalance(playerId, itemStack)

        if (currentBalance < amount) {
            return false
        }

        val currencyItem = itemStack.copy()
        currencyItem.count = amount

        return manageInventory(playerId, currencyItem, InventoryAction.REMOVE)
    }

    override fun transferCurrencyItem(fromPlayerId: UUID, toPlayerId: UUID, itemStack: ItemStack, amount: Int): Boolean {
        val sender = getPlayerById(fromPlayerId) ?: return false
        val receiver = getPlayerById(toPlayerId) ?: return false

        if (removeCurrencyItem(fromPlayerId, itemStack, amount)) {
            addCurrencyItem(toPlayerId, itemStack, amount)
            return true
        }
        return false
    }

    override fun getCurrencyItemBalance(playerId: UUID, itemStack: ItemStack): Int {
        val player = getPlayerById(playerId) ?: return 0
        var totalCount = 0

        player.inventory.main.forEach { stack ->
            if (stack.item == itemStack.item && stack.hasNbt() && stack.nbt == itemStack.nbt) {
                totalCount += stack.count
            }
        }
        return totalCount
    }

    override fun hasEnoughCurrencyItems(playerId: UUID, itemStack: ItemStack, amount: Int): Boolean {
        val currentBalance = getCurrencyItemBalance(playerId, itemStack)
        return currentBalance >= amount
    }

    private fun getPlayerById(playerId: UUID): ServerPlayerEntity? {
        return server.playerManager.getPlayer(playerId)
    }

    override fun getCurrencyItemType(itemStack: ItemStack): String? {
        Blanketconfig.config.economy.forEach { currencyConfig ->
            val item = Registries.ITEM.get(Identifier(currencyConfig.material))
            if (itemStack.item == item && itemStack.hasNbt()) {
                val nbtData = itemStack.nbt
                if (nbtData != null && nbtData.getInt("CustomModelData") == currencyConfig.custommodeldata) {
                    return currencyConfig.currencyType
                }
            }
        }
        return null
    }

    override fun getPrimaryCurrency(): Blanketconfig.EconomyConfig? {
        return Blanketconfig.getPrimaryCurrency()
    }

    override fun getCurrencyTypeOrFallback(currencyType: String): String {
        return Blanketconfig.getCurrencyTypeOrFallback(currencyType)
    }

    override fun getCurrencySymbol(currencyType: String): String {
        return Blanketconfig.getCurrencySymbol(currencyType)
    }
}