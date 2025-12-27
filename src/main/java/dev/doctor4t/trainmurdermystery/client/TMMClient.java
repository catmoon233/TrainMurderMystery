package dev.doctor4t.trainmurdermystery.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import dev.doctor4t.ratatouille.client.util.OptionLocker;
import dev.doctor4t.ratatouille.client.util.ambience.AmbienceUtil;
import dev.doctor4t.ratatouille.client.util.ambience.BackgroundAmbience;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.TrainWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoundTextRenderer;
import dev.doctor4t.trainmurdermystery.client.gui.StoreRenderer;
import dev.doctor4t.trainmurdermystery.client.gui.TimeRenderer;
import dev.doctor4t.trainmurdermystery.client.model.TMMModelLayers;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.PlateBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.SmallDoorBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.WheelBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.entity.FirecrackerEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.entity.HornBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.entity.NoteEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.util.TMMItemTooltips;
import dev.doctor4t.trainmurdermystery.entity.FirecrackerEntity;
import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.*;
import dev.doctor4t.trainmurdermystery.ui.TMMCommandUI;
import dev.doctor4t.trainmurdermystery.ui.event.KeyPressHandler;
import dev.doctor4t.trainmurdermystery.util.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class TMMClient implements ClientModInitializer {
    private static float soundLevel = 0f;
    public static HandParticleManager handParticleManager;
    public static Map<Player, Vec3> particleMap;
    private static boolean prevGameRunning;
    public static GameWorldComponent gameComponent;
    public static TrainWorldComponent trainComponent;
    public static PlayerMoodComponent moodComponent;

    public static final Map<UUID, PlayerInfo> PLAYER_ENTRIES_CACHE = Maps.newHashMap();

    public static KeyMapping instinctKeybind;
    public static float prevInstinctLightLevel = -.04f;
    public static float instinctLightLevel = -.04f;

    public static boolean shouldDisableHudAndDebug() {
        Minecraft client = Minecraft.getInstance();
        return (client == null || (client.player != null && !client.player.isCreative() && !client.player.isSpectator()));
    }

    @Override
    public void onInitializeClient() {
        // Load config
        // TMMConfig.init(TMM.MOD_ID, TMMConfig.class);

        // Initialize ScreenParticle
        handParticleManager = new HandParticleManager();
        particleMap = new HashMap<>();

        // Register particle factories
        TMMParticles.registerFactories();

        // Entity renderer registration
        EntityRendererRegistry.register(TMMEntities.SEAT, NoopRenderer::new);
        EntityRendererRegistry.register(TMMEntities.FIRECRACKER, FirecrackerEntityRenderer::new);
        EntityRendererRegistry.register(TMMEntities.GRENADE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(TMMEntities.NOTE, NoteEntityRenderer::new);

        // Register entity model layers
        TMMModelLayers.initialize();

        // Block render layers
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                TMMBlocks.STAINLESS_STEEL_VENT_HATCH,
                TMMBlocks.DARK_STEEL_VENT_HATCH,
                TMMBlocks.TARNISHED_GOLD_VENT_HATCH,
                TMMBlocks.METAL_SHEET_WALKWAY,
                TMMBlocks.STAINLESS_STEEL_LADDER,
                TMMBlocks.COCKPIT_DOOR,
                TMMBlocks.METAL_SHEET_DOOR,
                TMMBlocks.GOLDEN_GLASS_PANEL,
                TMMBlocks.CULLING_GLASS,
                TMMBlocks.STAINLESS_STEEL_WALKWAY,
                TMMBlocks.DARK_STEEL_WALKWAY,
                TMMBlocks.PANEL_STRIPES,
                TMMBlocks.RAIL_BEAM,
                TMMBlocks.TRIMMED_RAILING_POST,
                TMMBlocks.DIAGONAL_TRIMMED_RAILING,
                TMMBlocks.TRIMMED_RAILING,
                TMMBlocks.TRIMMED_EBONY_STAIRS,
                TMMBlocks.WHITE_LOUNGE_COUCH,
                TMMBlocks.WHITE_OTTOMAN,
                TMMBlocks.WHITE_TRIMMED_BED,
                TMMBlocks.BLUE_LOUNGE_COUCH,
                TMMBlocks.GREEN_LOUNGE_COUCH,
                TMMBlocks.BAR_STOOL,
                TMMBlocks.WALL_LAMP,
                TMMBlocks.SMALL_BUTTON,
                TMMBlocks.ELEVATOR_BUTTON,
                TMMBlocks.STAINLESS_STEEL_SPRINKLER,
                TMMBlocks.GOLD_SPRINKLER,
                TMMBlocks.GOLD_ORNAMENT,
                TMMBlocks.WHEEL,
                TMMBlocks.RUSTED_WHEEL,
                TMMBlocks.BARRIER_PANEL,
                TMMBlocks.FOOD_PLATTER,
                TMMBlocks.DRINK_TRAY,
                TMMBlocks.LIGHT_BARRIER,
                TMMBlocks.HORN
        );
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(),
                TMMBlocks.RHOMBUS_GLASS,
                TMMBlocks.PRIVACY_GLASS_PANEL,
                TMMBlocks.CULLING_BLACK_HULL,
                TMMBlocks.CULLING_WHITE_HULL,
                TMMBlocks.HULL_GLASS,
                TMMBlocks.RHOMBUS_HULL_GLASS
        );

        // Custom block models
        CustomModelProvider customModelProvider = new CustomModelProvider();
        ModelLoadingPlugin.register(customModelProvider);

        // Block Entity Renderers
        BlockEntityRenderers.register(
                TMMBlockEntities.SMALL_GLASS_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/small_glass_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.SMALL_WOOD_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/small_wood_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.ANTHRACITE_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/anthracite_steel_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.KHAKI_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/khaki_steel_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.MAROON_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/maroon_steel_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.MUNTZ_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/muntz_steel_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.NAVY_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/navy_steel_door.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.WHEEL,
                ctx -> new WheelBlockEntityRenderer(TMM.id("textures/entity/wheel.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.RUSTED_WHEEL,
                ctx -> new WheelBlockEntityRenderer(TMM.id("textures/entity/rusted_wheel.png"), ctx)
        );
        BlockEntityRenderers.register(
                TMMBlockEntities.BEVERAGE_PLATE,
                PlateBlockEntityRenderer::new
        );
        BlockEntityRenderers.register(TMMBlockEntities.HORN, HornBlockEntityRenderer::new);

        // Ambience
        //AmbienceUtil.registerBackgroundAmbience(new BackgroundAmbience(TMMSounds.AMBIENT_TRAIN_INSIDE, player -> isTrainMoving() && !TMM.isSkyVisibleAdjacent(player), 20));
        //AmbienceUtil.registerBackgroundAmbience(new BackgroundAmbience(TMMSounds.AMBIENT_TRAIN_OUTSIDE, player -> isTrainMoving() && TMM.isSkyVisibleAdjacent(player), 20));
        AmbienceUtil.registerBackgroundAmbience(new BackgroundAmbience(TMMSounds.AMBIENT_PSYCHO_DRONE, player -> gameComponent.isPsychoActive(), 20));
//        AmbienceUtil.registerBlockEntityAmbience(TMMBlockEntities.SPRINKLER, new BlockEntityAmbience(TMMSounds.BLOCK_SPRINKLER_RUN, 0.5f, blockEntity -> blockEntity instanceof SprinklerBlockEntity sprinklerBlockEntity && sprinklerBlockEntity.isPowered(), 20));

        // Caching components
        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            gameComponent = GameWorldComponent.KEY.get(clientWorld);
            trainComponent = TrainWorldComponent.KEY.get(clientWorld);
            moodComponent = PlayerMoodComponent.KEY.get(Minecraft.getInstance().player);
        });

        // Lock options
        OptionLocker.overrideOption("gamma", 0d);
        if (getLockedRenderDistance(TMMConfig.isUltraPerfMode()) != null) {
            OptionLocker.overrideOption("renderDistance", getLockedRenderDistance(TMMConfig.isUltraPerfMode())); // mfw 15 fps on a 3050 - Cup // haha 🫵 brokie - RAT // buy me a better one then - Cup // okay nvm I fixed it I was actually rendering a lot of empty chunks we didn't need my bad LMAO - RAT
        }
        OptionLocker.overrideOption("showSubtitles", false);
        OptionLocker.overrideOption("autoJump", false);
        OptionLocker.overrideOption("renderClouds", CloudStatus.OFF);
        OptionLocker.overrideSoundCategoryVolume("music", 0.0);
        OptionLocker.overrideSoundCategoryVolume("record", 0.1);
        OptionLocker.overrideSoundCategoryVolume("weather", 1.0);
        OptionLocker.overrideSoundCategoryVolume("block", 1.0);
        OptionLocker.overrideSoundCategoryVolume("hostile", 1.0);
        OptionLocker.overrideSoundCategoryVolume("neutral", 1.0);
        OptionLocker.overrideSoundCategoryVolume("player", 1.0);
        OptionLocker.overrideSoundCategoryVolume("ambient", 1.0);
        OptionLocker.overrideSoundCategoryVolume("voice", 1.0);


        // Item tooltips
        TMMItemTooltips.addTooltips();

        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            prevInstinctLightLevel = instinctLightLevel;
            // instinct night vision
            if (TMMClient.isInstinctEnabled()) {
                instinctLightLevel += .1f;
            } else {
                instinctLightLevel -= .1f;
            }
            instinctLightLevel = Mth.clamp(instinctLightLevel, -.04f, .5f);

            // Cache player entries
            for (AbstractClientPlayer player : clientWorld.players()) {
                ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
                if (networkHandler != null) {
                    PLAYER_ENTRIES_CACHE.put(player.getUUID(), networkHandler.getPlayerInfo(player.getUUID()));
                }
            }
            if (!prevGameRunning && gameComponent.isRunning()) {
                Minecraft.getInstance().player.getInventory().selected = 8;
            }
            prevGameRunning = gameComponent.isRunning();

            // Fade sound with game start / stop fade
            GameWorldComponent component = GameWorldComponent.KEY.get(clientWorld);
            if (component.getFade() > 0) {
                Minecraft.getInstance().getSoundManager().updateSourceVolume(SoundSource.MASTER, Mth.map(component.getFade(), 0, GameConstants.FADE_TIME, soundLevel, 0));
            } else {
                Minecraft.getInstance().getSoundManager().updateSourceVolume(SoundSource.MASTER, soundLevel);
                soundLevel = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            }

            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                StoreRenderer.tick();
                TimeRenderer.tick();
                StaminaRenderer.tick();
            }

            // TODO: Remove LMAO
//            if (clientWorld.getTime() % 200 == 0) {
//                if (TMMClient.PLAYER_ENTRIES_CACHE.get(MinecraftClient.getInstance().player.getUuid()).getSkinTextures().texture().hashCode() != 2024189164) {
//                    MinecraftClient client = MinecraftClient.getInstance();
//                    boolean bl = client.isInSingleplayer();
//                    ServerInfo serverInfo = client.getCurrentServerEntry();
//                    client.world.disconnect();
//                    if (bl) {
//                        client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
//                    } else {
//                        client.disconnect();
//                    }
//
//                    TitleScreen titleScreen = new TitleScreen();
//                    if (bl) {
//                        client.setScreen(titleScreen);
//                    } else if (serverInfo != null && serverInfo.isRealm()) {
//                        client.setScreen(new RealmsMainScreen(titleScreen));
//                    } else {
//                        client.setScreen(new MultiplayerScreen(titleScreen));
//                    }
//                }
//            }
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            TMMClient.handParticleManager.tick();
            RoundTextRenderer.tick();
        });

        ClientPlayNetworking.registerGlobalReceiver(ShootMuzzleS2CPayload.ID, new ShootMuzzleS2CPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(PoisonUtils.PoisonOverlayPayload.ID, new PoisonUtils.PoisonOverlayPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(GunDropPayload.ID, new GunDropPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(AnnounceWelcomePayload.ID, new AnnounceWelcomePayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(AnnounceEndingPayload.ID, new AnnounceEndingPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(TaskCompletePayload.ID, new TaskCompletePayload.Receiver());

        // Instinct keybind
        instinctKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + TMM.MOD_ID + ".instinct",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category." + TMM.MOD_ID + ".keybinds"
        ));
        
        // Initialize Command UI system
        TMMCommandUI.init();
        KeyPressHandler.register();
    }

    public static TrainWorldComponent getTrainComponent() {
        return trainComponent;
    }

    public static float getTrainSpeed() {
        return trainComponent.getSpeed();
    }

    public static boolean isTrainMoving() {
        return trainComponent != null && trainComponent.getSpeed() > 0;
    }

    public static class CustomModelProvider implements ModelLoadingPlugin {

        private final Map<ResourceLocation, UnbakedModel> modelIdToBlock = new Object2ObjectOpenHashMap<>();
        private final Set<ResourceLocation> withInventoryVariant = new HashSet<>();

        public void register(Block block, UnbakedModel model) {
            this.register(BuiltInRegistries.BLOCK.getKey(block), model);
        }

        public void register(ResourceLocation id, UnbakedModel model) {
            this.modelIdToBlock.put(id, model);
        }

        public void markInventoryVariant(Block block) {
            this.markInventoryVariant(BuiltInRegistries.BLOCK.getKey(block));
        }

        public void markInventoryVariant(ResourceLocation id) {
            this.withInventoryVariant.add(id);
        }

        @Override
        public void onInitializeModelLoader(Context ctx) {
            ctx.modifyModelOnLoad().register((model, context) -> {
                ModelResourceLocation topLevelId = context.topLevelId();
                if (topLevelId == null) {
                    return model;
                }
                ResourceLocation id = topLevelId.id();
                if (topLevelId.getVariant().equals("inventory") && !this.withInventoryVariant.contains(id)) {
                    return model;
                }
                if (this.modelIdToBlock.containsKey(id)) {
                    return this.modelIdToBlock.get(id);
                }
                return model;
            });
        }
    }

    public static boolean isPlayerAliveAndInSurvival() {
        return GameFunctions.isPlayerAliveAndSurvival(Minecraft.getInstance().player);
    }

    public static boolean isPlayerSpectatingOrCreative() {
        return GameFunctions.isPlayerSpectatingOrCreative(Minecraft.getInstance().player);
    }

    public static boolean isKiller() {
        return gameComponent != null && gameComponent.canUseKillerFeatures(Minecraft.getInstance().player);
    }

    public static int getInstinctHighlight(Entity target) {
        if (!isInstinctEnabled()) return -1;
//        if (target instanceof PlayerBodyEntity) return 0x606060;
        if (target instanceof ItemEntity || target instanceof NoteEntity || target instanceof FirecrackerEntity)
            return 0xDB9D00;
        if (target instanceof Player player) {
            if (GameFunctions.isPlayerSpectatingOrCreative(player)) return -1;
            if (isKiller() && gameComponent.canUseKillerFeatures(player)) return Mth.hsvToRgb(0F, 1.0F, 0.6F);
            if (gameComponent.isInnocent(player)) {
                float mood = PlayerMoodComponent.KEY.get(target).getMood();
                if (mood < GameConstants.DEPRESSIVE_MOOD_THRESHOLD) {
                    return 0x171DC6;
                } else if (mood < GameConstants.MID_MOOD_THRESHOLD) {
                    return 0x1FAFAF;
                } else {
                    return 0x4EDD35;
                }
            }
            if (isPlayerSpectatingOrCreative()) return 0xFFFFFF;
        }
        return -1;
    }

    public static boolean isInstinctEnabled() {
        return instinctKeybind.isDown() && ((isKiller() && isPlayerAliveAndInSurvival()) || isPlayerSpectatingOrCreative());
    }

    public static Object getLockedRenderDistance(boolean ultraPerfMode) {
        return null;
    }
}
