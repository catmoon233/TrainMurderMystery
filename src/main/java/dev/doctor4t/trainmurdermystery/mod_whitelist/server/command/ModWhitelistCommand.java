package dev.doctor4t.trainmurdermystery.mod_whitelist.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.trainmurdermystery.mod_whitelist.common.utils.MWLogger;
import dev.doctor4t.trainmurdermystery.mod_whitelist.server.config.MWServerConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Command handler for Mod Whitelist system
 * Supports commands like: mw:reload
 */
public class ModWhitelistCommand {

	/**
	 * Registers all mod whitelist commands
	 * Called during server initialization
	 */
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			CommandManager.literal("mw:reload")
				.requires(source -> source.hasPermissionLevel(3)) // OP only
				.executes(ModWhitelistCommand::reloadConfig)
		);

		MWLogger.LOGGER.debug("Mod Whitelist commands registered");
	}

	/**
	 * Handles the mw:reload command
	 * Reloads configuration from file
	 */
	private static int reloadConfig(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();

		try {
			MWServerConfig.reloadConfig();
			source.sendFeedback(
				() -> Text.literal("§aMod Whitelist configuration reloaded successfully!"),
				true
			);
			MWLogger.LOGGER.info("Config reloaded by: " + source.getEntity().getName().getString());
			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			source.sendError(
				Text.literal("§cFailed to reload Mod Whitelist configuration: " + e.getMessage())
			);
			MWLogger.LOGGER.error("Error reloading config", e);
			return 0;
		}
	}
}
