BlanketEconomy is a powerful and customizable economy mod designed for Minecraft servers that provides a flexible system for managing currencies, player balances, and economic transactions. Whether you're running a small community server or a large-scale network, BlanketEconomy can be tailored to suit your needs with its easy-to-use commands, flexible configuration options, and support for multiple currencies.

Key Features
Multiple Currency Support: Create and manage multiple currencies within your server's economy. Each currency can have its own custom name, lore, material, and unique identifier.
Flexible Economy System: Set starting balances for new players, manage transactions, and keep track of players' balances across different currencies.
Server Console Compatibility: All commands can be executed from the server console, providing server administrators with complete control.
Permissions Integration: Fine-grained permission control allows you to manage who can perform specific actions in the economy.
Commands & Permissions
The BlanketEconomy mod provides a range of commands for managing the economy. Below is a list of commands with a brief description and their associated permissions:

/beco balance <currencyType>

Permission: blanketeconomy.balance
Displays the current balance of the player for the specified currency type.
/beco pay <amount> <player> <currencyType>

Permission: blanketeconomy.pay
Transfers a specified amount of currency from the executing player to another player.
/beco add <amount> <player> <currencyType>

Permission: blanketeconomy.add
Adds a specified amount of currency to a player's balance.
/beco remove <amount> <player> <currencyType>

Permission: blanketeconomy.remove
Removes a specified amount of currency from a player's balance.
/beco deposit <currencyType>

Permission: blanketeconomy.deposit
Allows a player to deposit items (e.g., custom coins) of the specified currency type into their balance.
/beco withdraw <amount> <currencyType>

Permission: blanketeconomy.withdraw
Withdraws a specified amount of currency as an item stack for the player to hold or trade.
/beco reload

Permission: blanketeconomy.reload
Reloads the mod's configuration file, applying any changes made to the settings without needing to restart the server.
Configuration
The configuration for BlanketEconomy is highly customizable and can be easily adjusted to fit the needs of your server. The config file (config/blanketeconomy/config.json) allows you to set up various settings for the economy, including messages, starting balances, and available currencies.

Adding New Currencies To add a new currency to the economy system, follow these steps:

Locate the Configuration File: Open the config/blanketeconomy/config.json file located in your server's config folder.

Define a New Currency: Add a new entry in the economy list within the config file.

Each currency entry must include:

name: The display name of the currency.
lore: A description or lore for the currency item.
material: The Minecraft item type that represents the currency (e.g., minecraft:gold_ingot).
custommodeldata: Custom model data for the item, if applicable.
currencyType: A unique identifier for the currency (e.g., gold_coin).
balanceStart: The starting balance for new players for this currency.
Example:

{
  "name": "Emerald Coin",
  "lore": "A rare and valuable currency",
  "material": "minecraft:emerald",
  "custommodeldata": 5678,
  "currencyType": "emerald_coin",
  "balanceStart": 100
}
Save and Reload: After adding the new currency, save the config file and use the /beco reload command to apply the changes.
Benefits of BlanketEconomy
Seamless Integration: Works smoothly with popular permissions plugins like LuckPerms for advanced permission management.
Player Engagement: Encourages player interaction and trade by providing a robust in-game economy.
Easy to Use: Commands are straightforward, and the configuration file is well-documented, making it simple for server admins to set up and customize.
