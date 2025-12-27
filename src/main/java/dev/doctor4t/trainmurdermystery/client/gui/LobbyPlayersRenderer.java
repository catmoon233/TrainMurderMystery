package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.cca.AutoStartComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LobbyPlayersRenderer {
    public static void renderHud(Font renderer, @NotNull LocalPlayer player, @NotNull GuiGraphics context) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (!game.isRunning()) {
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2f, 6, 0);
            Level world = player.level();
            List<? extends Player> players = world.players();
            int count = players.size();
            int readyPlayerCount = GameFunctions.getReadyPlayerCount(world);
            MutableComponent playerCountText = Component.translatable("lobby.players.count", readyPlayerCount, count);
            context.drawString(renderer, playerCountText, -renderer.width(playerCountText) / 2, 0, 0xFFFFFFFF);

            AutoStartComponent autoStartComponent = AutoStartComponent.KEY.get(world);
            if (autoStartComponent.isAutoStartActive()) {
                MutableComponent autoStartText;
                int color = 0xFFAAAAAA;
                if (readyPlayerCount >= game.getGameMode().minPlayerCount) {
                    int seconds = autoStartComponent.getTime() / 20;
                    autoStartText = Component.translatable(seconds <= 0 ? "lobby.autostart.starting" : "lobby.autostart.time", seconds);
                    color = 0xFF00BC16;
                } else {
                    autoStartText = Component.translatable("lobby.autostart.active");
                }
                context.drawString(renderer, autoStartText, -renderer.width(autoStartText) / 2, 10, color);
            }

            context.pose().popPose();

            context.pose().pushPose();
            float scale = 0.75f;
            context.pose().translate(0, context.guiHeight(), 0);
            context.pose().scale(scale, scale, 1f);
            int i = 0;
            MutableComponent thanksText = Component.translatable("credits.trainmurdermystery.thank_you");

            String fallback = "Thank you for playing The Last Voyage of the Harpy Express!\nMe and my team spent a lot of time working\non this mod and we hope you enjoy it.\nIf you do and wish to make a video or stream\nplease make sure to credit my channel,\nvideo and the mod page!\n - RAT / doctor4t";
            if (!thanksText.getString().contains(" - RAT / doctor4t")) {
                thanksText = Component.literal(fallback);
            }

            for (Component text : TextUtils.getWithLineBreaks(thanksText)) {
                i++;
                context.drawString(renderer, text, 10, -90 + 10 * i, 0xFFFFFFFF);
            }
            context.pose().popPose();
        }
    }
}