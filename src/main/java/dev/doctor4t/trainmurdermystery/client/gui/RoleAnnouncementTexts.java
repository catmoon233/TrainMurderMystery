package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class RoleAnnouncementTexts {
    public static final ArrayList<RoleAnnouncementTexts.RoleAnnouncementText> ROLE_ANNOUNCEMENT_TEXTS = new ArrayList<>();

    public static RoleAnnouncementTexts.RoleAnnouncementText registerRoleAnnouncementText(RoleAnnouncementTexts.RoleAnnouncementText role) {
        ROLE_ANNOUNCEMENT_TEXTS.add(role);
        return role;
    }

    public static final RoleAnnouncementText BLANK = registerRoleAnnouncementText(new RoleAnnouncementText("", 0xFFFFFF));
    public static final RoleAnnouncementText CIVILIAN = registerRoleAnnouncementText(new RoleAnnouncementText("civilian", 0x36E51B));
    public static final RoleAnnouncementText VIGILANTE = registerRoleAnnouncementText(new RoleAnnouncementText("vigilante", 0x1B8AE5));
    public static final RoleAnnouncementText KILLER = registerRoleAnnouncementText(new RoleAnnouncementText("killer", 0xC13838));
    public static final RoleAnnouncementText LOOSE_END = registerRoleAnnouncementText(new RoleAnnouncementText("loose_end", 0x9F0000));

    public static class RoleAnnouncementText {
        private final String name;
        public final int colour;
        public final Component roleText;
        public final Component titleText;
        public final Component welcomeText;
        public final Function<Integer, Component> premiseText;
        public final Function<Integer, Component> goalText;
        public final Component winText;

        public RoleAnnouncementText(String name, int colour) {
            this.name = name;
            this.colour = colour;
            this.roleText = Component.translatable("announcement.role." + this.name.toLowerCase()).withColor(this.colour);
            this.titleText = Component.translatable("announcement.title." + this.name.toLowerCase()).withColor(this.colour);
            this.welcomeText = Component.translatable("announcement.welcome", this.roleText).withColor(0xF0F0F0);
            this.premiseText = (count) -> Component.translatable(count == 1 ? "announcement.premise" : "announcement.premises", count);
            this.goalText = (count) -> Component.translatable((count == 1 ? "announcement.goal." : "announcement.goals.") + this.name.toLowerCase(), count).withColor(this.colour);
            this.winText = Component.translatable("announcement.win." + this.name.toLowerCase()).withColor(this.colour);
        }

        public Component getLoseText() {
            return this == KILLER ? CIVILIAN.winText : KILLER.winText;
        }

        public @Nullable Component getEndText(GameFunctions.@NotNull WinStatus status, Component winner) {
            return switch (status) {
                case NONE -> null;
                case PASSENGERS, TIME -> this == KILLER ? this.getLoseText() : this.winText;
                case KILLERS -> this == KILLER ? this.winText : this.getLoseText();
                case GAMBLER ->
                        Component.translatable("announcement.win.gambler" , winner).withColor(new Color(128, 0, 128).getRGB());
                case LOOSE_END ->
                        Component.translatable("announcement.win." + LOOSE_END.name.toLowerCase(), winner).withColor(LOOSE_END.colour);
            };
        }
    }
}
