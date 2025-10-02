package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.cca.PlayerEndInfoComponent;
import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class RoundTextRenderer {
    private static final int WELCOME_DURATION = 200;
    private static final int END_DURATION = 200;
    private static RoleAnnouncementText role = RoleAnnouncementText.CIVILIAN;
    private static int welcomeTime = 0;
    private static int killers = 0;
    private static int targets = 0;
    private static int endTime = 0;

    public static void renderHud(TextRenderer renderer, ClientPlayerEntity player, @NotNull DrawContext context) {
        if (welcomeTime > 0) {
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f + 3.5, 0);
            context.getMatrices().push();
            context.getMatrices().scale(2.6f, 2.6f, 1f);
            if (welcomeTime <= 180) context.drawTextWithShadow(renderer, role.welcomeText, -renderer.getWidth(role.welcomeText) / 2, -12, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1.2f, 1.2f, 1f);
            if (welcomeTime <= 120) context.drawTextWithShadow(renderer, role.premiseText.apply(killers), -renderer.getWidth(role.premiseText.apply(killers)) / 2, 0, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1f, 1f, 1f);
            if (welcomeTime <= 60) context.drawTextWithShadow(renderer, role.goalText.apply(targets), -renderer.getWidth(role.goalText.apply(targets)) / 2, 14, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().pop();
        }
        var game = TMMComponents.GAME.get(player.getWorld());
        if (endTime > 0 && !game.isRunning()) {
            if (game.lastWinStatus == GameFunctions.WinStatus.NONE) return;
            var past = PlayerEndInfoComponent.KEY.get(player);
            if (past.role == RoleAnnouncementText.BLANK) return;
            var endText = role.getEndText(game.lastWinStatus);
            if (endText == null) return;
            var endMessage = game.lastWinStatus.name().toLowerCase();
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f - 40, 0);
            context.getMatrices().push();
            context.getMatrices().scale(2.6f, 2.6f, 1f);
            context.drawTextWithShadow(renderer, endText, -renderer.getWidth(endText) / 2, -12, 0xFFFFFF);
            context.getMatrices().pop();
            context.getMatrices().push();
            context.getMatrices().scale(1.2f, 1.2f, 1f);
            var winMessage = Text.translatable("game.win." + endMessage.toLowerCase());
            context.drawTextWithShadow(renderer, winMessage, -renderer.getWidth(winMessage) / 2, 0, 0xFFFFFF);
            context.getMatrices().pop();

            context.getMatrices().push();
            context.getMatrices().scale(1f, 1f, 1f);
            context.drawTextWithShadow(renderer, RoleAnnouncementText.CIVILIAN.titleText, -renderer.getWidth(RoleAnnouncementText.CIVILIAN.titleText) / 2 - 80, 14, 0xFFFFFF);
            context.drawTextWithShadow(renderer, RoleAnnouncementText.VIGILANTE.titleText, -renderer.getWidth(RoleAnnouncementText.VIGILANTE.titleText) / 2, 14, 0xFFFFFF);
            context.drawTextWithShadow(renderer, RoleAnnouncementText.KILLER.titleText, -renderer.getWidth(RoleAnnouncementText.KILLER.titleText) / 2 + 80, 14, 0xFFFFFF);
            context.getMatrices().pop();
            var civilians = 0;
            var vigilantes = 0;
            var killers = 0;
            for (var entry : player.networkHandler.getPlayerList()) {
                var pastPlayer = player.getWorld().getPlayerByUuid(entry.getProfile().getId());
                if (pastPlayer == null) continue;
                var name = pastPlayer.getDisplayName();
                if (name == null) continue;
                context.getMatrices().push();
                context.getMatrices().scale(1f, 1f, 1f);
                var endInfo = PlayerEndInfoComponent.KEY.get(pastPlayer);
                switch (endInfo.role) {
                    case CIVILIAN -> {
                        civilians++;
                        context.getMatrices().translate(-80, civilians * 14, 0);
                    }
                    case VIGILANTE -> {
                        vigilantes++;
                        context.getMatrices().translate(0, vigilantes * 14, 0);
                    }
                    case KILLER -> {
                        killers++;
                        context.getMatrices().translate(80, killers * 14, 0);
                    }
                }
                if (endInfo.wasDead) name = name.copy().styled((style) -> style.withStrikethrough(true).withColor(0x808080));
                context.drawTextWithShadow(renderer, name, -renderer.getWidth(name) / 2, 14, 0xFFFFFF);
                context.getMatrices().pop();
            }
            context.getMatrices().pop();
        }
    }

    public static void tick() {
        if (welcomeTime > 0) {
            switch (welcomeTime) {
                case 180 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 1.25f, player.getRandom().nextLong());
                }
                case 120 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 1.5f, player.getRandom().nextLong());
                }
                case 60 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 1.75f, player.getRandom().nextLong());
                }
                case 1 -> {
                    var player = MinecraftClient.getInstance().player;
                    if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1f, 1.2f, player.getRandom().nextLong());
                }
            }
            welcomeTime--;
        }
        if (endTime > 0) {
            if (endTime == END_DURATION) {
                var player = MinecraftClient.getInstance().player;
                if (player != null) player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1f, 1f, player.getRandom().nextLong());
            }
            endTime--;
        }
        var options = MinecraftClient.getInstance().options;
        if (options != null && options.playerListKey.isPressed()) endTime = Math.max(2, endTime);
        var player = MinecraftClient.getInstance().player;
        if (player != null) player.sendMessage(Text.literal("endTime: " + endTime), true);
    }

    public static void startWelcome(RoleAnnouncementText role, int killers, int targets) {
        RoundTextRenderer.role = role;
        welcomeTime = WELCOME_DURATION;
        RoundTextRenderer.killers = killers;
        RoundTextRenderer.targets = targets;
    }

    public static void startEnd() {
        endTime = END_DURATION;
    }
}