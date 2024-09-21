package org.blanketeconomy.api

import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import java.math.BigDecimal
import java.util.UUID
import org.blanketeconomy.Blanketconfig

interface EconomyAPI {
    /**
     * The instance of the Minecraft server this API is operating on.
     */
    val server: MinecraftServer

    /**
     * Retrieves the balance of a player for a specific currency type.
     * @param playerId The UUID of the player.
     * @param currencyType The type of currency to check.
     * @return The balance as a BigDecimal.
     */
    fun getBalance(playerId: UUID, currencyType: String): BigDecimal

    /**
     * Sets the balance of a player for a specific currency type.
     * @param playerId The UUID of the player.
     * @param balance The new balance to set.
     * @param currencyType The type of currency to set the balance for.
     */
    fun setBalance(playerId: UUID, balance: BigDecimal, currencyType: String)

    /**
     * Adds an amount to a player's balance for a specific currency type.
     * @param playerId The UUID of the player.
     * @param amount The amount to add.
     * @param currencyType The type of currency to add to.
     */
    fun addBalance(playerId: UUID, amount: BigDecimal, currencyType: String)

    /**
     * Subtracts an amount from a player's balance for a specific currency type.
     * @param playerId The UUID of the player.
     * @param amount The amount to subtract.
     * @param currencyType The type of currency to subtract from.
     * @return True if the operation was successful, false if the player has insufficient funds.
     */
    fun subtractBalance(playerId: UUID, amount: BigDecimal, currencyType: String): Boolean

    /**
     * Transfers an amount of currency from one player to another.
     * @param fromPlayerId The UUID of the player sending the currency.
     * @param toPlayerId The UUID of the player receiving the currency.
     * @param amount The amount to transfer.
     * @param currencyType The type of currency to transfer.
     * @return True if the transfer was successful, false otherwise.
     */
    fun transfer(fromPlayerId: UUID, toPlayerId: UUID, amount: BigDecimal, currencyType: String): Boolean

    /**
     * Registers an event listener for economy-related events.
     * @param listener The event listener to add.
     */
    fun addEventListener(listener: EconomyEventListener)

    /**
     * Unregisters an event listener for economy-related events.
     * @param listener The event listener to remove.
     */
    fun removeEventListener(listener: EconomyEventListener)

    /**
     * Performs a transaction only if a specified condition is met.
     * @param playerId The UUID of the player.
     * @param amount The amount for the transaction.
     * @param currencyType The type of currency to transact.
     * @param condition A lambda function that defines the condition.
     * @return True if the transaction is completed, false otherwise.
     */
    fun conditionalTransaction(playerId: UUID, amount: BigDecimal, currencyType: String, condition: (BigDecimal) -> Boolean): Boolean

    /**
     * Logs a transaction for a player in a specific currency type.
     * @param playerId The UUID of the player.
     * @param currencyType The type of currency.
     * @param amount The amount involved in the transaction.
     * @param type The type of transaction (e.g., "credit", "debit").
     */
    fun logTransaction(playerId: UUID, currencyType: String, amount: BigDecimal, type: String)

    /**
     * Retrieves the transaction history for a player.
     * @param playerId The UUID of the player.
     * @return A list of transactions made by the player.
     */
    fun getTransactionHistory(playerId: UUID): List<Transaction>

    /**
     * Creates a new currency with specific properties.
     * @param name The name of the currency.
     * @param lore The lore (description) of the currency.
     * @param material The material type of the currency item.
     * @param customModelData The custom model data ID for the item.
     * @param balanceStart The starting balance for this currency.
     * @return True if the currency was created successfully, false otherwise.
     */
    fun createCurrency(name: String, lore: String, material: String, customModelData: Int, balanceStart: BigDecimal, symbol: String, isPrimary: Boolean): Boolean

    /**
     * Checks if a player has enough funds for a transaction.
     * @param playerId The UUID of the player.
     * @param amount The amount to check.
     * @param currencyType The type of currency.
     * @return True if the player has enough funds, false otherwise.
     */
    fun hasEnoughFunds(playerId: UUID, amount: BigDecimal, currencyType: String): Boolean

    /**
     * Manages a player's inventory by adding or removing a specified item.
     * @param playerId The UUID of the player.
     * @param itemStack The item stack to add or remove.
     * @param action The action to perform (ADD or REMOVE).
     * @return True if the action was successful, false otherwise.
     */
    fun manageInventory(playerId: UUID, itemStack: ItemStack, action: InventoryAction): Boolean

    /**
     * Processes a batch of transactions.
     * @param transactions A list of transactions to process.
     * @return True if all transactions were processed successfully, false otherwise.
     */
    fun processBatchTransactions(transactions: List<Transaction>): Boolean

    /**
     * Adds a specified amount of currency items to a player's inventory.
     * @param playerId The UUID of the player.
     * @param itemStack The currency item stack.
     * @param amount The amount to add.
     * @return True if the currency items were added successfully, false otherwise.
     */
    fun addCurrencyItem(playerId: UUID, itemStack: ItemStack, amount: Int): Boolean

    /**
     * Removes a specified amount of currency items from a player's inventory.
     * @param playerId The UUID of the player.
     * @param itemStack The currency item stack.
     * @param amount The amount to remove.
     * @return True if the currency items were removed successfully, false otherwise.
     */
    fun removeCurrencyItem(playerId: UUID, itemStack: ItemStack, amount: Int): Boolean

    /**
     * Transfers a specified amount of currency items from one player to another.
     * @param fromPlayerId The UUID of the player sending the currency items.
     * @param toPlayerId The UUID of the player receiving the currency items.
     * @param itemStack The currency item stack.
     * @param amount The amount to transfer.
     * @return True if the transfer was successful, false otherwise.
     */
    fun transferCurrencyItem(fromPlayerId: UUID, toPlayerId: UUID, itemStack: ItemStack, amount: Int): Boolean

    /**
     * Gets the balance of a specific currency item in a player's inventory.
     * @param playerId The UUID of the player.
     * @param itemStack The currency item stack to check.
     * @return The total amount of the currency item in the player's inventory.
     */
    fun getCurrencyItemBalance(playerId: UUID, itemStack: ItemStack): Int

    /**
     * Checks if a player has enough of a specific currency item.
     * @param playerId The UUID of the player.
     * @param itemStack The currency item stack to check.
     * @param amount The amount required.
     * @return True if the player has enough currency items, false otherwise.
     */
    fun hasEnoughCurrencyItems(playerId: UUID, itemStack: ItemStack, amount: Int): Boolean

    /**
     * Gets the type of a currency item based on its properties.
     * @param itemStack The currency item stack.
     * @return The currency type as a string, or null if it is not a currency item.
     */
    fun getCurrencyItemType(itemStack: ItemStack): String?

    /**
     * Retrieves the primary currency from the configuration.
     * @return The primary currency as EconomyConfig.
     */
    fun getPrimaryCurrency(): Blanketconfig.EconomyConfig?

    /**
     * Returns the valid currency type or falls back to the primary currency if the type is incorrect.
     * @param currencyType The requested currency type.
     * @return The valid currency type or the primary one.
     */
    fun getCurrencyTypeOrFallback(currencyType: String): String

    /**
     * Retrieves the symbol for a specific currency type.
     * @param currencyType The type of currency.
     * @return The symbol of the currency as a string, or empty string if not found.
     */
    fun getCurrencySymbol(currencyType: String): String

    /**
     * Retrieves the list of all configured currencies.
     *
     * @return A list of `EconomyConfig` representing all available currencies.
     */
    fun getCurrencyList(): List<Blanketconfig.EconomyConfig>

    /**
     * Checks if a currency with the specified type exists.
     *
     * @param currencyType The type of currency to check.
     * @return True if the currency exists, false otherwise.
     */
    fun currencyExists(currencyType: String): Boolean
}