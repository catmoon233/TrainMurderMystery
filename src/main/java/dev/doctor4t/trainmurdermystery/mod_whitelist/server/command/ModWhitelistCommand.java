package dev.doctor4t.trainmurdermystery.mod_whitelist.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.trainmurdermystery.mod_whitelist.common.utils.MWLogger;
import dev.doctor4t.trainmurdermystery.mod_whitelist.server.config.MWServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * Command handler for Mod Whitelist system and server management
 * Supports commands like: mw:reload, mw:maxplayers
 */
public class ModWhitelistCommand {

	/**
	 * Registers all mod whitelist commands
	 * Called during server initialization
	 */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("mw:reload")
				.requires(source -> source.hasPermission(2)) // OP only
				.executes(ModWhitelistCommand::reloadConfig)
		);

		dispatcher.register(
				Commands.literal("mw:maxplayers")
				.requires(source -> source.hasPermission(2)) // OP only
				.then(Commands.literal("get")
					.executes(ModWhitelistCommand::getMaxPlayers)
				)
				.then(Commands.literal("set")
					.then(Commands.argument("count", IntegerArgumentType.integer(1, 256))
						.executes(ModWhitelistCommand::setMaxPlayers)
					)
				)
		);

		MWLogger.LOGGER.debug("Mod Whitelist commands registered");
	}

	/**
	 * Handles the mw:reload command
	 * Reloads configuration from file
	 */
	private static int reloadConfig(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();

		try {
			MWServerConfig.reloadConfig();
			source.sendSuccess(
				() -> Component.literal("§aMod Whitelist configuration reloaded successfully!"),
				true
			);
			MWLogger.LOGGER.info("Config reloaded by: " + source.getEntity().getName().getString());
			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			source.sendFailure(
				Component.literal("§cFailed to reload Mod Whitelist configuration: " + e.getMessage())
			);
			MWLogger.LOGGER.error("Error reloading config", e);
			return 0;
		}
	}
	public static int maxPlayers = -404;
	/**
	 * Handles the mw:maxplayers get command
	 * Displays the current maximum player count
	 */
	private static int getMaxPlayers(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();

		if (server == null) {
			source.sendFailure(Component.literal("§cServer instance not available"));
			return 0;
		}

		int maxPlayers = server.getMaxPlayers();
		int currentPlayers = server.getPlayerList().getPlayers().size();

		source.sendSuccess(
			() -> Component.literal("§6Current Server Status: §f" + currentPlayers + "§6/§f" + maxPlayers + " players"),
			false
		);

		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Handles the mw:maxplayers set command
	 * Sets the maximum player count
	 */
	private static int setMaxPlayers(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		int newMaxPlayers = IntegerArgumentType.getInteger(context, "count");

		if (server == null) {
			source.sendFailure(Component.literal("§cServer instance not available"));
			return 0;
		}

		int oldMaxPlayers = server.getMaxPlayers();

		try {
			maxPlayers = newMaxPlayers;

			source.sendSuccess(
				() -> Component.literal("§aMax players changed from §f" + oldMaxPlayers + "§a to §f" + newMaxPlayers),
				true
			);

			String playerName = source.getEntity() != null ? source.getEntity().getName().getString() : "unknown";
			MWLogger.LOGGER.info("Max players changed from " + oldMaxPlayers + " to " + newMaxPlayers + " by " + playerName);

			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			source.sendFailure(
				Component.literal("§cFailed to set max players: " + e.getMessage())
			);
			MWLogger.LOGGER.error("Error setting max players", e);
			return 0;
		}
	}
}
