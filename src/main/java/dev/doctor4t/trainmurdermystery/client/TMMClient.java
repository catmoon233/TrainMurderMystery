package dev.doctor4t.trainmurdermystery.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import dev.doctor4t.ratatouille.client.util.OptionLocker;
import dev.doctor4t.ratatouille.client.util.ambience.AmbienceUtil;
import dev.doctor4t.ratatouille.client.util.ambience.BackgroundAmbience;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.block.SecurityMonitorBlock;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.TrainWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.*;
import dev.doctor4t.trainmurdermystery.client.gui.screen.MapSelectorScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.PlayerStatsScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.SkinManagementScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.WaypointHUD;
import dev.doctor4t.trainmurdermystery.client.model.KnifeModelLoadingPlugin;
import dev.doctor4t.trainmurdermystery.client.model.TMMModelLayers;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.PlateBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.SmallDoorBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.WheelBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.entity.FirecrackerEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.entity.HornBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.entity.NoteEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.util.TMMItemTooltips;
import dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin;
import dev.doctor4t.trainmurdermystery.data.MapConfig;
import dev.doctor4t.trainmurdermystery.entity.FirecrackerEntity;
import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import dev.doctor4t.trainmurdermystery.event.AllowOtherCameraType;
import dev.doctor4t.trainmurdermystery.event.OnGetInstinctHighlight;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.LooseEndsGameMode;
import dev.doctor4t.trainmurdermystery.index.*;
import dev.doctor4t.trainmurdermystery.item.GrenadeItem;
import dev.doctor4t.trainmurdermystery.item.KnifeItem;
import dev.doctor4t.trainmurdermystery.mod_whitelist.client.ModWhitelistClient;
import dev.doctor4t.trainmurdermystery.network.*;
import dev.doctor4t.trainmurdermystery.network.packet.ModVersionPacket;
import dev.doctor4t.trainmurdermystery.network.packet.SyncRoomToPlayerPayload;
import dev.doctor4t.trainmurdermystery.network.packet.SyncSpecificWaypointVisibilityPacket;
import dev.doctor4t.trainmurdermystery.network.packet.SyncWaypointVisibilityPacket;
import dev.doctor4t.trainmurdermystery.network.packet.SyncWaypointsPacket;

import dev.doctor4t.trainmurdermystery.util.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.CameraType;
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
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.gson.JsonSyntaxException;

import java.util.*;
import java.util.function.Predicate;

public class TMMClient implements ClientModInitializer {
    private static float soundLevel = 0f;
    public static HandParticleManager handParticleManager;
    public static Map<Player, Vec3> particleMap;
    private static boolean prevGameRunning;
    public static GameWorldComponent gameComponent;
    public static TrainWorldComponent trainComponent;
    public static PlayerMoodComponent moodComponent;
    public static int intervalTime = 0;
    public static boolean isInLobby = false;
    public static final Map<UUID, PlayerInfo> PLAYER_ENTRIES_CACHE = Maps.newHashMap();

    public static KeyMapping instinctKeybind;
    public static KeyMapping statsKeybind; // 新增统计面板热键
    public static KeyMapping skinsKeybind; // 新增皮肤管理热键
    public static boolean isInstinctToggleEnabled = false; // 新增变量用于跟踪切换状态
    public static boolean prevInstinctKeyDown = false; // 用于检测按键按下事件
    public static float prevInstinctLightLevel = -.04f;
    public static float instinctLightLevel = -.04f;

    public static boolean shouldDisableHudAndDebug() {
        Minecraft client = Minecraft.getInstance();
        return (client == null
                || (client.player != null && !client.player.isCreative() && !client.player.isSpectator()));
    }

    public static boolean isPlayerCreative() {
        Minecraft client = Minecraft.getInstance();
        return (client != null && client.player != null && (client.player.isCreative()));
    }

    @Override
    public void onInitializeClient() {
        // Load config
        // TMMConfig.init(TMM.MOD_ID, TMMConfig.class);
        ModWhitelistClient.onInitializeClient();
        // ModVersionPacket

        // Initialize ScreenParticle
        handParticleManager = new HandParticleManager();
        particleMap = new HashMap<>();
        // Custom Baked Models
        ModelLoadingPlugin.register(new KnifeModelLoadingPlugin());
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
                TMMBlocks.HORN);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(),
                TMMBlocks.RHOMBUS_GLASS,
                TMMBlocks.PRIVACY_GLASS_PANEL,
                TMMBlocks.CULLING_BLACK_HULL,
                TMMBlocks.CULLING_WHITE_HULL,
                TMMBlocks.HULL_GLASS,
                TMMBlocks.RHOMBUS_HULL_GLASS);

        // Custom block models
        CustomModelProvider customModelProvider = new CustomModelProvider();
        ModelLoadingPlugin.register(customModelProvider);

        // Block Entity Renderers
        BlockEntityRenderers.register(
                TMMBlockEntities.SMALL_GLASS_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/small_glass_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.SMALL_WOOD_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/small_wood_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.ANTHRACITE_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/anthracite_steel_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.KHAKI_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/khaki_steel_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.MAROON_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/maroon_steel_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.MUNTZ_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/muntz_steel_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.NAVY_STEEL_DOOR,
                ctx -> new SmallDoorBlockEntityRenderer(TMM.id("textures/entity/navy_steel_door.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.WHEEL,
                ctx -> new WheelBlockEntityRenderer(TMM.id("textures/entity/wheel.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.RUSTED_WHEEL,
                ctx -> new WheelBlockEntityRenderer(TMM.id("textures/entity/rusted_wheel.png"), ctx));
        BlockEntityRenderers.register(
                TMMBlockEntities.BEVERAGE_PLATE,
                PlateBlockEntityRenderer::new);
        BlockEntityRenderers.register(TMMBlockEntities.HORN, HornBlockEntityRenderer::new);

        // Ambience
        // AmbienceUtil.registerBackgroundAmbience(new
        // BackgroundAmbience(TMMSounds.AMBIENT_TRAIN_INSIDE, player -> isTrainMoving()
        // && !TMM.isSkyVisibleAdjacent(player), 20));
        // AmbienceUtil.registerBackgroundAmbience(new
        // BackgroundAmbience(TMMSounds.AMBIENT_TRAIN_OUTSIDE, player -> isTrainMoving()
        // && TMM.isSkyVisibleAdjacent(player), 20));
        AmbienceUtil.registerBackgroundAmbience(
                new BackgroundAmbience(TMMSounds.AMBIENT_PSYCHO_DRONE, player -> gameComponent.isPsychoActive(), 20));
        // AmbienceUtil.registerBlockEntityAmbience(TMMBlockEntities.SPRINKLER, new
        // BlockEntityAmbience(TMMSounds.BLOCK_SPRINKLER_RUN, 0.5f, blockEntity ->
        // blockEntity instanceof SprinklerBlockEntity sprinklerBlockEntity &&
        // sprinklerBlockEntity.isPowered(), 20));

        // Caching components
        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            gameComponent = GameWorldComponent.KEY.get(clientWorld);
            trainComponent = TrainWorldComponent.KEY.get(clientWorld);
            moodComponent = PlayerMoodComponent.KEY.get(Minecraft.getInstance().player);
        });

        // Lock options
        OptionLocker.overrideOption("gamma", 0d);
        if (getLockedRenderDistance(TMMConfig.isUltraPerfMode()) != null) {
            OptionLocker.overrideOption("renderDistance", getLockedRenderDistance(TMMConfig.isUltraPerfMode()));
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
        ClientPlayNetworking.registerGlobalReceiver(SecurityCameraModePayload.ID,
                new SecurityCameraModePayload.ClientReceiver());
        ClientPlayNetworking.registerGlobalReceiver(IsLobbyConfigPayload.ID, (payload, context) -> {
            TMMClient.isInLobby = payload.isLobby();
            TMM.isLobby = payload.isLobby();
            LoggerFactory.getLogger(this.getClass())
                    .info("Is Lobby status: " + (TMMClient.isInLobby ? "Yes" : "No"));
        });
        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
            packetSender.sendPacket(new ModVersionPacket(TMM.modPacketVersion));
            TMM.LOGGER.info("Send client version {} to verify.", TMM.modPacketVersion);

        });
        // Item tooltips
        TMMItemTooltips.addTooltips();
        AllowOtherCameraType.EVENT.register((original, localPlayer) -> {
            if (SecurityMonitorBlock.isInSecurityMode()) {
                return AllowOtherCameraType.ReturnCameraType.THIRD_PERSON_BACK;
            }
            return AllowOtherCameraType.ReturnCameraType.NO_CHANGE;
        });
        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                boolean keycode = Minecraft.getInstance().options.keyShift.consumeClick();
                if (keycode) {
                    if (SecurityMonitorBlock.isInSecurityMode()) {
                        SecurityMonitorBlock.setSecurityMode(false);
                        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                    }
                }
            }

            prevInstinctLightLevel = instinctLightLevel;
            // 检测按键按下事件，只在按键状态从释放变为按下时切换
            boolean isKeyDown = instinctKeybind.isDown();
            if (isKeyDown && !prevInstinctKeyDown) {
                isInstinctToggleEnabled = !isInstinctToggleEnabled; // 切换状态
            }
            prevInstinctKeyDown = isKeyDown;

            // instinct night vision - 现在基于切换状态而不是按键按下来判断
            if (TMMClient.isInstinctEnabled()) {
                instinctLightLevel += .2f;
            } else {
                instinctLightLevel -= .2f;
            }
            instinctLightLevel = Mth.clamp(instinctLightLevel, -.04f, 0.75f);

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
                Minecraft.getInstance().getSoundManager().updateSourceVolume(SoundSource.MASTER,
                        Mth.map(component.getFade(), 0, GameConstants.FADE_TIME, soundLevel, 0));
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

        });
        intervalTime = new Random().nextInt(0, 200);
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (gameComponent != null) {
                if (gameComponent.isRunning()) {
                    if (client != null && client.player != null) {
                        if (client.player.isSpectator()) {
                            intervalTime++;
                            if (intervalTime >= 30 * 10) { // 30s
                                if (TrainVoicePlugin.CLIENT_API != null) {
                                    if (!TrainVoicePlugin.CLIENT_API.isDisconnected()) {
                                        if (TrainVoicePlugin.CLIENT_API.getGroup() == null) {
                                            ClientPlayNetworking.send(new JoinSpecGroupPayload(true));
                                        }
                                    }
                                }
                                intervalTime = 0;
                            }
                        }
                    }

                }
            }
            TMMClient.handParticleManager.tick();
            RoundTextRenderer.tick();
        });

        SyncMapConfigPayload.registerReceiver();
        TriggerScreenEdgeEffectPayload.registerReceiver();
        RemoveStatusBarPayload.registerReceiver();
        TriggerStatusBarPayload.registerReceiver();
        ClientPlayNetworking.registerGlobalReceiver(ShootMuzzleS2CPayload.ID, new ShootMuzzleS2CPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(PoisonUtils.PoisonOverlayPayload.ID,
                new PoisonUtils.PoisonOverlayPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(GunDropPayload.ID, new GunDropPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(AnnounceWelcomePayload.ID, new AnnounceWelcomePayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(AnnounceEndingPayload.ID, new AnnounceEndingPayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(TaskCompletePayload.ID, new TaskCompletePayload.Receiver());
        ClientPlayNetworking.registerGlobalReceiver(ShowStatsPayload.ID, (payload, context) -> {
            UUID targetPlayerUuid = payload.targetPlayerUuid();
            context.client().execute(() -> {
                context.client().setScreen(new PlayerStatsScreen(targetPlayerUuid));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncRoomToPlayerPayload.ID, (payload, context) -> {
            Map<UUID, Integer> data = payload.data();
            if (Minecraft.getInstance().isSingleplayer()) {
                TMM.LOGGER.info("Singleplayer. No need to sync info.");
                return;
            } else {
                TMM.LOGGER.info("Sync RoomToPlayer info from server.");
            }
            GameFunctions.roomToPlayer.clear();
            GameFunctions.roomToPlayer.putAll(data);
        });
        ClientPlayNetworking.registerGlobalReceiver(ShowSelectedMapUIPayload.ID, (payload, context) -> {
            var str = payload.serverConfig();

            // @SuppressWarnings("unchecked")
            try {
                var a = MapConfig.gson.fromJson(str, MapConfig.class);
                MapConfig.getInstance().maps.clear();
                MapConfig.getInstance().maps.addAll(a.maps);
            } catch (JsonSyntaxException e) {
                LoggerFactory.getLogger("TMMClient").error(e.getMessage());
                e.printStackTrace();
            }
            context.client().execute(() -> {
                context.client().setScreen(new MapSelectorScreen());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(MapVotingResultsPayload.TYPE, (payload, context) -> {
            MapDetailsRenderer.triggerMapDetails(
                    payload.result);
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenSkinScreenPaylod.ID, (payload, context) -> {

            context.client().execute(() -> {
                context.client().setScreen(new SkinManagementScreen());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(CloseUiPayload.ID, (payload, context) -> {

            context.client().execute(() -> {
                context.client().setScreen(null);
            });
        });

        // Instinct keybind
        instinctKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + TMM.MOD_ID + ".instinct",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category." + TMM.MOD_ID + ".keybinds"));

        // Register stats keybind
        statsKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + TMM.MOD_ID + ".stats",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O, // 默认热键 'O'
                "category." + TMM.MOD_ID + ".keybinds"));

        // Register skins keybind
        skinsKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + TMM.MOD_ID + ".skins",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N, // 默认热键 'N'
                "category." + TMM.MOD_ID + ".keybinds"));
        // Initialize Command UI system
        // TMMCommandUI.init();
        // KeyPressHandler.register();
        InputHandler.initialize();

        // Register HUD rendering for security camera
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((guiGraphics, deltaTick) -> {
            SecurityCameraHUD.render(guiGraphics, Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                    Minecraft.getInstance().getWindow().getGuiScaledHeight());
            SecurityCameraHUD.renderCameraFeed(guiGraphics, Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                    Minecraft.getInstance().getWindow().getGuiScaledHeight());
            WaypointHUD.renderHUD(guiGraphics, deltaTick.getRealtimeDeltaTicks());
            AFKRenderer.renderAFKEffects(guiGraphics, deltaTick.getRealtimeDeltaTicks());

            // // 添加地图详情渲染
            // Font font = Minecraft.getInstance().font;
            // LocalPlayer player = Minecraft.getInstance().player;
            // if (font != null && player != null) {
            // MapDetailsRenderer.renderHud(font, player, guiGraphics,
            // deltaTick.getRealtimeDeltaTicks());
            // }
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncWaypointsPacket.ID, SyncWaypointsPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SyncWaypointVisibilityPacket.ID,
                SyncWaypointVisibilityPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(SyncSpecificWaypointVisibilityPacket.ID,
                SyncSpecificWaypointVisibilityPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(BreakArmorPayload.ID, (payload, context) -> {
            LocalPlayer player = context.player();
            if (player != null && player.level() != null) {
                player.level().playLocalSound(payload.x(), payload.y(), payload.z(),
                        TMMSounds.ITEM_PSYCHO_ARMOUR, SoundSource.MASTER, 5.0F, 1.0F, false);
            }
        });

        // Register client tick event for stats keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player != null && player.level() != null) {
                dev.doctor4t.trainmurdermystery.api.RoleMethodDispatcher.callClientTick(player);
            }
            if (statsKeybind.consumeClick()) {
                if (client.screen instanceof PlayerStatsScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new PlayerStatsScreen(client.player.getUUID()));
                }
            }

            if (skinsKeybind.consumeClick()) {
                if (client.screen instanceof SkinManagementScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new SkinManagementScreen());
                }
            }
        });
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
        int invokerColor = OnGetInstinctHighlight.EVENT.invoker().GetInstinctHighlight(target, isInstinctEnabled());
        if (invokerColor != -1) {
            if (invokerColor == -2)
                return -1;
            return invokerColor;
        }
        if (!isInstinctEnabled()) {
            // TMM.LOGGER.info("instinct not enable");
            return -1;
        }
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                .get(Minecraft.getInstance().player.level());
        // if (target instanceof PlayerBodyEntity) return 0x606060;
        if (target instanceof ItemEntity || target instanceof NoteEntity || target instanceof FirecrackerEntity)
            return 0xDB9D00;
        if (target instanceof Player player) {
            if (!(player).isSpectator()) {
                if (GameFunctions.isPlayerSpectatingOrCreative(Minecraft.getInstance().player)) {
                    Role role = gameWorldComponent.getRole(player);
                    if (role == null) {
                        return (TMMRoles.CIVILIAN.color());
                    } else {
                        return (role.color());
                    }
                }

            }
        }
        return -1;
    }

    static Predicate<Player> isHoldSpecialItem = (player) -> {
        if (player.getMainHandItem().getItem() instanceof KnifeItem)
            return true;
        if (player.getMainHandItem().getItem() instanceof GrenadeItem)
            return true;
        return false;
    };

    public static boolean isInstinctEnabled() {
        boolean canUseInstinct = isKiller();
        final var player = Minecraft.getInstance().player;
        if (TMMClient.gameComponent != null) {
            var role = TMMClient.gameComponent.getRole(player);
            if (role != null) {
                if (role==TMMRoles.LOOSE_END){
                    return !(gameComponent.getGameMode() instanceof LooseEndsGameMode);
                }
                canUseInstinct = role.canUseInstinct();
            }
        }
        return (isInstinctToggleEnabled
                && ((canUseInstinct && isPlayerAliveAndInSurvival()) || isPlayerSpectatingOrCreative()))
                || (canUseInstinct && isHoldSpecialItem.test(player));
    }

    public static Object getLockedRenderDistance(boolean ultraPerfMode) {
        return null;
    }
}
