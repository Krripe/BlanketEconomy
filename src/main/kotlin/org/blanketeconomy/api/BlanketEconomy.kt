package org.blanketeconomy.api

import net.minecraft.server.MinecraftServer

object BlanketEconomy {
    private lateinit var api: BlanketEconomyAPI

    fun initialize(server: MinecraftServer) {
        api = BlanketEconomyAPI(server)
    }

    fun getAPI(): EconomyAPI {
        return api
    }
}