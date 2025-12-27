package dev.doctor4t.trainmurdermystery.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMGameModes;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameReplay;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class RoundTextRenderer {
    private static final Map<String, Optional<GameProfile>> failCache = new HashMap<>();
    private static final int WELCOME_DURATION = 200 + GameConstants.FADE_TIME * 2;
    private static final int END_DURATION = 200;
    private static RoleAnnouncementTexts.RoleAnnouncementText role = RoleAnnouncementTexts.CIVILIAN;
    private static int welcomeTime = 0;
    private static int killers = 0;
    private static int targets = 0;
    private static int endTime = 0;

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static void renderHud(Font renderer, LocalPlayer player, @NotNull GuiGraphics context) {
        boolean isLooseEnds = GameWorldComponent.KEY.get(player.level()).getGameMode() == TMMGameModes.LOOSE_ENDS;

        if (welcomeTime > 0) {
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2f, context.guiHeight() / 2f + 3.5, 0);
            context.pose().pushPose();
            context.pose().scale(2.6f, 2.6f, 1f);
            int color = isLooseEnds ? 0x9F0000 : 0xFFFFFF;
            if (welcomeTime <= 180) {
                Component welcomeText = isLooseEnds ? Component.translatable("announcement.loose_ends.welcome") : role.welcomeText;
                context.drawString(renderer, welcomeText, -renderer.width(welcomeText) / 2, -12, color);
            }
            context.pose().popPose();
            context.pose().pushPose();
            context.pose().scale(1.2f, 1.2f, 1f);
            if (welcomeTime <= 120) {
                Component premiseText = isLooseEnds ? Component.translatable("announcement.loose_ends.premise") : role.premiseText.apply(killers);
                context.drawString(renderer, premiseText, -renderer.width(premiseText) / 2, 0, color);
            }
            context.pose().popPose();
            context.pose().pushPose();
            context.pose().scale(1f, 1f, 1f);
            if (welcomeTime <= 60) {
                Component goalText = isLooseEnds ? Component.translatable("announcement.loose_ends.goal") : role.goalText.apply(targets);
                context.drawString(renderer, goalText, -renderer.width(goalText) / 2, 14, color);
            }
            context.pose().popPose();
            context.pose().popPose();
        }
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (endTime > 0 && endTime < END_DURATION - (GameConstants.FADE_TIME * 2) && !game.isRunning() && game.getGameMode() != TMMGameModes.DISCOVERY) {
            GameRoundEndComponent roundEnd = GameRoundEndComponent.KEY.get(player.level());
            if (roundEnd.getWinStatus() == GameFunctions.WinStatus.NONE) return;
            Player winner = player.level().getPlayerByUUID(game.getLooseEndWinner() == null ? UUID.randomUUID() : game.getLooseEndWinner());
            Component endText = role.getEndText(roundEnd.getWinStatus(), winner == null ? Component.empty() : winner.getDisplayName());
            if (endText == null) return;
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2f, context.guiHeight() / 2f - 40, 0);
            context.pose().pushPose();
            context.pose().scale(2.6f, 2.6f, 1f);
            context.drawString(renderer, endText, -renderer.width(endText) / 2, -12, 0xFFFFFF);
            context.pose().popPose();
            context.pose().pushPose();
            context.pose().scale(1.2f, 1.2f, 1f);
            MutableComponent winMessage = Component.translatable("game.win." + roundEnd.getWinStatus().name().toLowerCase().toLowerCase());
            context.drawString(renderer, winMessage, -renderer.width(winMessage) / 2, -4, 0xFFFFFF);
            context.pose().popPose();
            if (isLooseEnds) {
                context.drawString(renderer, RoleAnnouncementTexts.LOOSE_END.titleText, -renderer.width(RoleAnnouncementTexts.LOOSE_END.titleText) / 2, 14, 0xFFFFFF);
                int looseEnds = 0;
                for (GameRoundEndComponent.RoundEndData entry : roundEnd.getPlayers()) {
                    context.pose().pushPose();
                    context.pose().scale(2f, 2f, 1f);
                    context.pose().translate(((looseEnds % 6) - 3.5) * 12, 14 + (looseEnds / 6) * 12, 0);
                    looseEnds++;
                    PlayerInfo playerEntry = TMMClient.PLAYER_ENTRIES_CACHE.get(entry.player().getId());
                    if (playerEntry != null && playerEntry.getSkin().texture() != null) {
                        ResourceLocation texture = playerEntry.getSkin().texture();


                        RenderSystem.enableBlend();
                        context.pose().pushPose();
                        context.pose().translate(8, 0, 0);
                        float offColour = entry.wasDead() ? 0.4f : 1f;
                        context.innerBlit(texture, 0, 8, 0, 8, 0, 8 / 64f, 16 / 64f, 8 / 64f, 16 / 64f, 1f, offColour, offColour, 1f);
                        context.pose().translate(-0.5, -0.5, 0);
                        context.pose().scale(1.125f, 1.125f, 1f);
                        context.innerBlit(texture, 0, 8, 0, 8, 0, 40 / 64f, 48 / 64f, 8 / 64f, 16 / 64f, 1f, offColour, offColour, 1f);
                        context.pose().popPose();
                    }
                    if (entry.wasDead()) {
                        context.pose().translate(13, 0, 0);
                        context.pose().scale(2f, 1f, 1f);
                        context.drawString(renderer, "x", -renderer.width("x") / 2, 0, 0xE10000, false);
                        context.drawString(renderer, "x", -renderer.width("x") / 2, 1, 0x550000, false);
                    }
                    context.pose().popPose();
                }
                context.pose().popPose();
            } else {
                int vigilanteTotal = 1;
                for (GameRoundEndComponent.RoundEndData entry : roundEnd.getPlayers())
                    if (entry.role() == RoleAnnouncementTexts.VIGILANTE) vigilanteTotal += 1;
                context.drawString(renderer, RoleAnnouncementTexts.CIVILIAN.titleText, -renderer.width(RoleAnnouncementTexts.CIVILIAN.titleText) / 2 - 60, 14, 0xFFFFFF);
                context.drawString(renderer, RoleAnnouncementTexts.VIGILANTE.titleText, -renderer.width(RoleAnnouncementTexts.VIGILANTE.titleText) / 2 + 50, 14, 0xFFFFFF);
                context.drawString(renderer, RoleAnnouncementTexts.KILLER.titleText, -renderer.width(RoleAnnouncementTexts.KILLER.titleText) / 2 + 50, 14 + 16 + 24 * ((vigilanteTotal) / 2), 0xFFFFFF);
                int civilians = 0;
                int vigilantes = 0;
                int killers = 0;
                for (GameRoundEndComponent.RoundEndData entry : roundEnd.getPlayers()) {
                    context.pose().pushPose();
                    context.pose().scale(2f, 2f, 1f);



                    if (entry.role() == RoleAnnouncementTexts.CIVILIAN) {
                        context.pose().translate(-60 + (civilians % 4) * 12, 14 + (civilians / 4) * 12, 0);
                        civilians++;
                    } else if (entry.role() == RoleAnnouncementTexts.VIGILANTE) {
                        context.pose().translate(7 + (vigilantes % 2) * 12, 14 + (vigilantes / 2) * 12, 0);
                        vigilantes++;
                    } else if (entry.role() == RoleAnnouncementTexts.KILLER) {
                        context.pose().translate(0, 8 + ((vigilanteTotal) / 2) * 12, 0);
                        context.pose().translate(7 + (killers % 2) * 12, 14 + (killers / 2) * 12, 0);
                        killers++;
                    }
                    final var role1 = TMMClient.gameComponent.getRole(entry.player().getId());
                    //final var first = TMM.REPLAY_MANAGER.getCurrentReplay().players().stream().filter(replayPlayerInfo -> replayPlayerInfo.uuid().equals(entry.player().getId())).findFirst();
                    if (role1 !=null) {
                        context.drawString(renderer, Component.translatable("announcement.role."+role1.getIdentifier().getPath()), 0, 4, role1.getColor(), false);
                    }else {
                   //     context.drawText(renderer, player.getName(), 0, 4, 0xFFFFFF, false);
                    }
                    PlayerInfo playerListEntry = TMMClient.PLAYER_ENTRIES_CACHE.get(entry.player().getId());
                    if (playerListEntry != null) {

                        ResourceLocation texture = playerListEntry.getSkin().texture();
                        if (texture != null) {
                            RenderSystem.enableBlend();
                            context.pose().pushPose();
                            context.pose().translate(8, 0, 0);
                            float offColour = entry.wasDead() ? 0.4f : 1f;
                            context.innerBlit(texture, 0, 8, 0, 8, 0, 8 / 64f, 16 / 64f, 8 / 64f, 16 / 64f, 1f, offColour, offColour, 1f);
                            context.pose().translate(-0.5, -0.5, 0);
                            context.pose().scale(1.125f, 1.125f, 1f);
                            context.innerBlit(texture, 0, 8, 0, 8, 0, 40 / 64f, 48 / 64f, 8 / 64f, 16 / 64f, 1f, offColour, offColour, 1f);
                            context.pose().popPose();
                        }
                        if (entry.wasDead()) {
                            context.pose().translate(13, 0, 0);
                            context.pose().scale(2f, 1f, 1f);
                            context.drawString(renderer, "x", -renderer.width("x") / 2, 0, 0xE10000, false);
                            context.drawString(renderer, "x", -renderer.width("x") / 2, 1, 0x550000, false);
                        }
                    }
                    context.pose().popPose();
                }
                context.pose().popPose();
            }
        }
    }

    public static void tick() {
        if (Minecraft.getInstance().level != null && GameWorldComponent.KEY.get(Minecraft.getInstance().level).getGameMode() != TMMGameModes.DISCOVERY) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (welcomeTime > 0) {
                switch (welcomeTime) {
                    case 200 -> {
                        if (player != null)
                            player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_RISER, SoundSource.MASTER, 10f, 1f, player.getRandom().nextLong());
                    }
                    case 180 -> {
                        if (player != null)
                            player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO, SoundSource.MASTER, 10f, 1.25f, player.getRandom().nextLong());
                    }
                    case 120 -> {
                        if (player != null)
                            player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO, SoundSource.MASTER, 10f, 1.5f, player.getRandom().nextLong());
                    }
                    case 60 -> {
                        if (player != null)
                            player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO, SoundSource.MASTER, 10f, 1.75f, player.getRandom().nextLong());
                    }
                    case 1 -> {
                        if (player != null)
                            player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(), TMMSounds.UI_PIANO_STINGER, SoundSource.MASTER, 10f, 1f, player.getRandom().nextLong());
                    }
                }
                welcomeTime--;
            }
            if (endTime > 0) {
                if (endTime == END_DURATION - (GameConstants.FADE_TIME * 2)) {
                    if (player != null)
                        player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(), GameRoundEndComponent.KEY.get(player.level()).didWin(player.getUUID()) ? TMMSounds.UI_PIANO_WIN : TMMSounds.UI_PIANO_LOSE, SoundSource.MASTER, 10f, 1f, player.getRandom().nextLong());
                }
                endTime--;
            }
            Options options = Minecraft.getInstance().options;
            if (options != null && options.keyPlayerList.isDown()) endTime = Math.max(2, endTime);
        }
    }

    public static void startWelcome(RoleAnnouncementTexts.RoleAnnouncementText role, int killers, int targets) {
        RoundTextRenderer.role = role;
        welcomeTime = WELCOME_DURATION;
        RoundTextRenderer.killers = killers;
        RoundTextRenderer.targets = targets;
    }

    public static void startEnd() {
        welcomeTime = 0;
        endTime = END_DURATION;
    }

    public static GameProfile getGameProfile(String disguise) {
        Optional<GameProfile> optional = SkullBlockEntity.fetchGameProfile(disguise).getNow(failCache(disguise));
        return optional.orElse(failCache(disguise).get());
    }

    public static PlayerSkin getSkinTextures(String disguise) {
        return Minecraft.getInstance().getSkinManager().getInsecureSkin(getGameProfile(disguise));
    }

    public static Optional<GameProfile> failCache(String name) {
        return failCache.computeIfAbsent(name, (d) -> Optional.of(new GameProfile(UUID.randomUUID(), name)));
    }

}