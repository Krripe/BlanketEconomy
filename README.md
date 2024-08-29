# BlanketEconomy Mod

![Minecraft](https://img.shields.io/badge/Minecraft-Mod-blue.svg) ![Kotlin](https://img.shields.io/badge/Kotlin-%23664b99.svg?style=flat&logo=kotlin&logoColor=white) ![License](https://img.shields.io/badge/License-MIT-green.svg)

## Overview

**BlanketEconomy** is a flexible and customizable economy mod for Minecraft, providing players and server admins with powerful tools to manage in-game currencies. The mod supports both virtual currencies and item-based currencies, allowing for dynamic transactions, shops, trades, and much more.

With BlanketEconomy, you can:
- Manage multiple currencies (both virtual and item-based).
- Perform transactions between players.
- Create and manage player balances.
- Use item-based currencies for shops, trades, and other interactive gameplay features.
- Configure messages and currency settings directly from the config file.

## Features

- **Multi-Currency Support**: Manage both virtual and item-based currencies.
- **Commands for Transactions**: Commands like `/beco add`, `/beco remove`, `/beco pay`, etc., to manage player balances.
- **Event Listeners and API Hooks**: Easy integration with other mods or plugins for extended economy functionality.
- **Customizable Messages and Configurations**: Easily configurable messages and settings to suit your server's needs.
- **Advanced Permission System**: Integrated with LuckPerms or Minecraft's native permission system.

## Commands & Permissions

| Command                   | Description                                             | Permission                     |
|---------------------------|---------------------------------------------------------|---------------------------------|
| `/beco balance <currency>`| Shows the player's balance for a specific currency.     | `blanketeconomy.balance`       |
| `/beco pay <amount> <player> <currency>` | Pay another player a specified amount of currency. | `blanketeconomy.pay`           |
| `/beco add <amount> <player> <currency>` | Adds a specified amount of currency to a player. | `blanketeconomy.add`           |
| `/beco remove <amount> <player> <currency>` | Removes a specified amount of currency from a player. | `blanketeconomy.remove`        |
| `/beco deposit <currency>` | Deposits item-based currency to the player's balance.  | `blanketeconomy.deposit`       |
| `/beco withdraw <amount> <currency>` | Withdraws a specified amount of currency into item form. | `blanketeconomy.withdraw`      |
| `/beco reload`            | Reloads the configuration files.                        | `blanketeconomy.reload`        |

## Configuration

The mod's configuration file allows you to:
- Add or remove different types of currencies.
- Customize messages for various economy-related events.
- Define properties for item-based currencies, including custom model data, name, lore, and more.

### Example Configuration (config/blanketeconomy/config.json)

```json
{
  "economy": [
    {
      "name": "&6Cobble Coin",
      "lore": "&eThis is a &6currency",
      "material": "minecraft:paper",
      "custommodeldata": 7381,
      "currencyType": "cobble_coin",
      "balanceStart": 500
    },
    {
      "name": "&6Gold Coin",
      "lore": "&eThis is a &cpremium &6currency",
      "material": "minecraft:gold_ingot",
      "custommodeldata": 1234,
      "currencyType": "gold_coin",
      "balanceStart": 500
    }
  ],
  "messages": {
    "balanceMessage": "&aYour balance: &e%balance% &7%currencyType%",
    "paymentSuccess": "&aSuccessfully paid &e%amount% &7to %player%!",
    "paymentReceived": "&aYou have received &e%amount% &7from %player%!",
    "insufficientFunds": "&cInsufficient funds!",
    "addBalance": "&aAdded &e%amount% &ato &7%player%!",
    "withdrawSuccess": "&aSuccessfully withdrew &e%amount% &7%currencyType%!",
    "withdrawFail": "&cInsufficient funds!",
    "reloadMessage": "&cCobbleEconomy configuration reloaded!",
    "removeBalance": "&cRemoved &e%amount% &cfrom %player%!",
    "depositSuccess": "&aSuccessfully deposited &e%amount% &7%currencyType%!",
    "noValid": "&cInvalid funds!",
    "playerNotFound": "&cPlayer %player% not found.",
    "currencyNotFound": "&cCurrency configuration not found for %currencyType%.",
    "configReloadSuccess": "&aConfiguration reloaded successfully.",
    "balanceInitialized": "&aYour balance has been initialized to &e%balance% &7%currency%."
  }
}
