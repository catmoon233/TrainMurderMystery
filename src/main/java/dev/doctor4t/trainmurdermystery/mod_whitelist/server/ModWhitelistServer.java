package dev.doctor4t.trainmurdermystery.mod_whitelist.server;

import dev.doctor4t.trainmurdermystery.mod_whitelist.server.config.MWServerConfig;
import dev.doctor4t.trainmurdermystery.mod_whitelist.server.network.ModWhitelistServerNetworkHandler;
import net.fabricmc.api.DedicatedServerModInitializer;

public class ModWhitelistServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		MWServerConfig.hello();
		
		// Initialize network handler for receiving mod info from clients
		ModWhitelistServerNetworkHandler.initializeServer();
	}
}
