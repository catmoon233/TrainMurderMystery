package dev.doctor4t.trainmurdermystery;

import dev.doctor4t.trainmurdermystery.command.*;
import dev.doctor4t.trainmurdermystery.game.TMMGameLoop;
import dev.doctor4t.trainmurdermystery.index.*;
import dev.doctor4t.trainmurdermystery.util.KnifeStabPayload;
import dev.doctor4t.trainmurdermystery.util.ShootMuzzleS2CPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMM implements ModInitializer {
    public static final String MOD_ID = "trainmurdermystery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        // Registry initializers
        TMMDataComponentTypes.initialize();
        TMMSounds.initialize();
        TMMEntities.initialize();
        TMMBlocks.initialize();
        TMMItems.initialize();
        TMMBlockEntities.initialize();
        TMMParticles.initialize();

        // Register commands
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            GiveRoomKeyCommand.register(dispatcher);
            SetTrainSpeedCommand.register(dispatcher);
            StartGameCommand.register(dispatcher);
            StopGameCommand.register(dispatcher);
            ResetTrainCommand.register(dispatcher);
        }));

        // Game loop tick
        ServerTickEvents.START_WORLD_TICK.register(TMMGameLoop::tick);

        PayloadTypeRegistry.playS2C().register(ShootMuzzleS2CPayload.ID, ShootMuzzleS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(KnifeStabPayload.ID, KnifeStabPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KnifeStabPayload.ID, new KnifeStabPayload.Receiver());
    }
}

// TODO: Add tasks (2-3 hours)
// TODO: System that remembers previous roles and allows cycling of roles (3h)
// TODO: Train chimney smoke + ringable horn?
// TODO: - Sync scenery
// TODO: - Up sleep chances
// TODO: - Fix vents
// TODO: - Sleep chat fix
// TODO: - Fix spectators being shot by guns
// TODO: - Fix doors not being blastable
// TODO: - Better tasks
// TODO: - Target system only for bonus
// TODO: - Cabin button from inside
// TODO: - Detective drops gun on innocent kill
// TODO: - Players collide with each other
// TODO: - Hitmen know each other
// TODO: - Make the detective drop the gun on killed
// TODO: - Louder footsteps
// TODO: - Username when hovering over them
// TODO: Hitman item shop
// TODO: - Explosive for clumped up people
// TODO: - Poison
// TODO: - Scorpion
// TODO: - Gun with one bullet
// TODO: - Psycho mode
// TODO: - Light turn off item + true darkness
// TODO: - Crowbar
// TODO: - Firecracker