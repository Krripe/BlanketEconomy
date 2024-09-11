package org.blanketeconomy

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import org.blanketeconomy.Blanketconfig.config
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.CustomModelDataComponent
import net.minecraft.component.type.LoreComponent
import org.blanketeconomy.utils.CustomColorParser
import java.math.BigDecimal

class Blanketeconomy : ModInitializer {

    override fun onInitialize() {
        println("Initializing BlanketEconomy...")
        Blanketconfig.loadConfig()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerCommands(dispatcher)
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            initializePlayerData(player)
        }
    }

    private fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("beco")
                .then(CommandManager.literal("balance")
                    .then(CommandManager.argument("currencyType", StringArgumentType.string())
                        .suggests { _, builder ->
                            config.economy.map { it.currencyType }.forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .requires { source ->
                            val player = source.player
                            player != null && hasPermission(player, "blanketeconomy.balance", 2)
                        }
                        .executes { ctx -> showBalance(ctx) }
                    )
                )
                .then(CommandManager.literal("pay")
                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("player", StringArgumentType.string())
                            .then(CommandManager.argument("currencyType", StringArgumentType.string())
                                .suggests { _, builder ->
                                    config.economy.map { it.currencyType }.forEach { builder.suggest(it) }
                                    builder.buildFuture()
                                }
                                .requires { source ->
                                    val player = source.player
                                    player != null && hasPermission(player, "blanketeconomy.pay", 2)
                                }
                                .executes { ctx -> pay(ctx) }
                            )
                        )
                    )
                )
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("player", StringArgumentType.string())
                            .then(CommandManager.argument("currencyType", StringArgumentType.string())
                                .suggests { _, builder ->
                                    config.economy.map { it.currencyType }.forEach { builder.suggest(it) }
                                    builder.buildFuture()
                                }
                                .requires { source ->
                                    val player = source.player
                                    player != null && hasPermission(player, "blanketeconomy.add", 4)
                                }
                                .executes { ctx -> add(ctx) }
                            )
                        )
                    )
                )
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("amount", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("player", StringArgumentType.string())
                            .then(CommandManager.argument("currencyType", StringArgumentType.string())
                                .suggests { _, builder ->
                                    config.economy.map { it.currencyType }.forEach { builder.suggest(it) }
                                    builder.buildFuture()
                                }
                                .requires { source ->
                                    val player = source.player
                                    player != null && hasPermission(player, "blanketeconomy.remove", 4)
                                }
                                .executes { ctx -> remove(ctx) }
                            )
                        )
                    )
                )
                .then(CommandManager.literal("deposit")
                    .then(CommandManager.argument("currencyType", StringArgumentType.string())
                        .suggests { _, builder ->
                            config.economy.map { it.currencyType }.forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .requires { source ->
                            val player = source.player
                            player != null && hasPermission(player, "blanketeconomy.deposit", 2)
                        }
                        .executes { ctx -> deposit(ctx) }
                    )
                )
                .then(CommandManager.literal("withdraw")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                        .then(CommandManager.argument("currencyType", StringArgumentType.string())
                            .suggests { _, builder ->
                                config.economy.map { it.currencyType }.forEach { builder.suggest(it) }
                                builder.buildFuture()
                            }
                            .requires { source ->
                                val player = source.player
                                player != null && hasPermission(player, "blanketeconomy.withdraw", 2)
                            }
                            .executes { ctx -> withdraw(ctx) }
                        )
                    )
                )
                .then(CommandManager.literal("reload")
                    .requires { source ->
                        val player = source.player
                        player != null && hasPermission(player, "blanketeconomy.reload", 4)
                    }
                    .executes { ctx -> reloadConfig(ctx) }
                )
        )
    }

    private fun showBalance(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return 0
        val currencyType = StringArgumentType.getString(ctx, "currencyType")
        val balance = Blanketconfig.getBalance(player.uuid, currencyType)
        val message = Blanketconfig.getMessage("balanceMessage", "balance" to balance.toString(), "currencyType" to currencyType)
        player.sendMessage(message, false)
        return 1
    }

    private fun pay(ctx: CommandContext<ServerCommandSource>): Int {
        val amount = DoubleArgumentType.getDouble(ctx, "amount")
        val playerName = StringArgumentType.getString(ctx, "player")
        val currencyType = StringArgumentType.getString(ctx, "currencyType")
        val amountBigDecimal = BigDecimal(amount.toString())

        val sender = ctx.source.player ?: return 0
        val receiver = ctx.source.server.playerManager.getPlayer(playerName) ?: return 0

        val senderBalance = Blanketconfig.getBalance(sender.uuid, currencyType)
        if (senderBalance >= amountBigDecimal) {
            Blanketconfig.setBalance(sender.uuid, senderBalance.subtract(amountBigDecimal), currencyType)

            val receiverBalance = Blanketconfig.getBalance(receiver.uuid, currencyType)
            Blanketconfig.setBalance(receiver.uuid, receiverBalance.add(amountBigDecimal), currencyType)

            val paymentSuccessMessage = Blanketconfig.getMessage("paymentSuccess", "amount" to amount.toString(), "player" to playerName, "currencyType" to currencyType)
            val paymentReceivedMessage = Blanketconfig.getMessage("paymentReceived", "amount" to amount.toString(), "player" to sender.name.string, "currencyType" to currencyType)

            sender.sendMessage((paymentSuccessMessage), false)
            receiver.sendMessage((paymentReceivedMessage), false)
        } else {
            sender.sendMessage(Blanketconfig.getMessage("insufficientFunds", "currencyType" to currencyType))
        }
        return 1
    }

    private fun add(ctx: CommandContext<ServerCommandSource>): Int {
        val source = ctx.source
        val amount = DoubleArgumentType.getDouble(ctx, "amount")
        val amountBigDecimal = BigDecimal(amount.toString())
        val playerName = StringArgumentType.getString(ctx, "player")
        val currencyType = StringArgumentType.getString(ctx, "currencyType")

        val player = source.server.playerManager.getPlayer(playerName)
        if (player == null) {
            source.sendMessage(Blanketconfig.getMessage("playerNotFound", "player" to playerName))
            return 0
        }

        val balance = Blanketconfig.getBalance(player.uuid, currencyType)
        Blanketconfig.setBalance(player.uuid, balance.add(amountBigDecimal), currencyType)
        val message = Blanketconfig.getMessage("addBalance", "amount" to amount.toString(), "player" to playerName, "currencyType" to currencyType)
        source.sendMessage(message)
        return 1
    }

    private fun remove(ctx: CommandContext<ServerCommandSource>): Int {
        val source = ctx.source
        val amount = DoubleArgumentType.getDouble(ctx, "amount")
        val amountBigDecimal = BigDecimal(amount.toString())
        val playerName = StringArgumentType.getString(ctx, "player")
        val currencyType = StringArgumentType.getString(ctx, "currencyType")

        val player = source.server.playerManager.getPlayer(playerName)
        if (player == null) {
            source.sendMessage(Blanketconfig.getMessage("playerNotFound", "player" to playerName))
            return 0
        }

        val balance = Blanketconfig.getBalance(player.uuid, currencyType)
        if (balance >= amountBigDecimal) {
            Blanketconfig.setBalance(player.uuid, balance.subtract(amountBigDecimal), currencyType)
            val message = Blanketconfig.getMessage("removeBalance", "amount" to amount.toString(), "player" to playerName, "currencyType" to currencyType)
            source.sendMessage(message)
        } else {
            source.sendMessage(Blanketconfig.getMessage("insufficientFunds", "currencyType" to currencyType))
        }
        return 1
    }

    private fun withdraw(ctx: CommandContext<ServerCommandSource>): Int {
        val amount = IntegerArgumentType.getInteger(ctx, "amount")
        val player = ctx.source.player ?: return 0
        val currencyType = StringArgumentType.getString(ctx, "currencyType")
        val balance = Blanketconfig.getBalance(player.uuid, currencyType)
        val amountBigDecimal = BigDecimal(amount)

        if (balance >= amountBigDecimal) {
            Blanketconfig.setBalance(player.uuid, balance.subtract(amountBigDecimal), currencyType)
            val currencyConfig = config.economy.find { it.currencyType == currencyType }

            if (currencyConfig != null) {
                val paperStack = ItemStack(Items.PAPER, amount).apply {
                    set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent(currencyConfig.custommodeldata))

                    set(DataComponentTypes.CUSTOM_NAME, CustomColorParser.toNativeComponent(currencyConfig.name))

                    val loreList: List<Text> = listOf(Text.literal(currencyConfig.lore))
                    set(DataComponentTypes.LORE, LoreComponent(loreList))
                }

                player.inventory.offerOrDrop(paperStack)
                player.sendMessage(Blanketconfig.getMessage("withdrawSuccess", "amount" to amount.toString(), "currencyType" to currencyType))

            } else {
                player.sendMessage(Blanketconfig.getMessage("currencyNotFound", "currencyType" to currencyType))
            }
        } else {
            player.sendMessage(Blanketconfig.getMessage("withdrawFail", "currencyType" to currencyType))
        }
        return 1
    }


    private fun deposit(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return 0
        val currencyType = StringArgumentType.getString(ctx, "currencyType")
        val balance = Blanketconfig.getBalance(player.uuid, currencyType)
        val paperStacks = player.inventory.main.filter { stack ->
            stack.item == Items.PAPER && isCobbleCoin(stack, currencyType)
        }

        var totalAmount = BigDecimal.ZERO
        paperStacks.forEach { stack ->
            totalAmount = totalAmount.add(BigDecimal(stack.count))
            player.inventory.removeStack(player.inventory.main.indexOf(stack))
        }

        if (totalAmount > BigDecimal.ZERO) {
            Blanketconfig.setBalance(player.uuid, balance.add(totalAmount), currencyType)
            player.sendMessage(Blanketconfig.getMessage("depositSuccess", "amount" to totalAmount.toString(), "currencyType" to currencyType))
        } else {
            player.sendMessage(Blanketconfig.getMessage("noValid"))
        }

        return 1
    }

    private fun isCobbleCoin(stack: ItemStack, currencyType: String): Boolean {
        val data = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA)?.value
        return data == config.economy.find { it.currencyType == currencyType }?.custommodeldata
    }

    private fun initializePlayerData(player: ServerPlayerEntity) {
        config.economy.forEach { currency ->
            val currencyType = currency.currencyType
            if (!Blanketconfig.hasUserData(player.uuid, currencyType)) {
                Blanketconfig.initializeUserData(player.uuid, currencyType)
                player.sendMessage(Blanketconfig.getMessage("balanceInitialized", "balance" to currency.balanceStart.toString(), "currency" to currency.name))
            }
        }
    }

    private fun reloadConfig(ctx: CommandContext<ServerCommandSource>): Int {
        Blanketconfig.reloadConfig()
        ctx.source.sendMessage(Blanketconfig.getMessage("configReloadSuccess"))
        return 1
    }

    private fun hasPermission(player: ServerPlayerEntity, permission: String, level: Int): Boolean {
        return if (hasLuckPermsPermission(player, permission, level)) {
            true
        } else {
            player.hasPermissionLevel(level)
        }
    }

    private fun hasLuckPermsPermission(player: ServerPlayerEntity, permission: String, level: Int): Boolean {
        return try {
            Permissions.check(player, permission, level)
        } catch (e: NoClassDefFoundError) {
            false
        }
    }
}