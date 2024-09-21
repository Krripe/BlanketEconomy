package org.blanketeconomy.api

import net.minecraft.server.MinecraftServer

object BlanketEconomy {
    private var api: BlanketEconomyAPI? = null
    private var serverInstance: MinecraftServer? = null

    fun initialize(server: MinecraftServer) {
        if (api == null) {
            api = BlanketEconomyAPI(server)
            serverInstance = server
            println("BlanketEconomy API initialized with server!")
        }
    }

    fun isInitialized(): Boolean {
        return api != null
    }

    fun getAPI(): EconomyAPI {
        if (api == null) {
            if (serverInstance == null) {
                throw IllegalStateException("BlanketEconomy API is not initialized, and no server instance available!")
            }
            initialize(serverInstance!!)
        }
        return api ?: throw IllegalStateException("Failed to initialize BlanketEconomy API!")
    }

    fun getAPI(server: MinecraftServer): EconomyAPI {
        if (api == null) {
            initialize(server)
        }
        return api ?: throw IllegalStateException("Failed to initialize BlanketEconomy API!")
    }

    fun setServer(server: MinecraftServer) {
        if (serverInstance == null) {
            serverInstance = server
        }
    }
}
