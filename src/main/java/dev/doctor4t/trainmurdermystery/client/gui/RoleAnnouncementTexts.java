package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.doctor4t.trainmurdermystery.api.Role;

public class RoleAnnouncementTexts {
    public static final Map<ResourceLocation, RoleAnnouncementText> ROLE_ANNOUNCEMENT_TEXTS = new HashMap<>();

    public static RoleAnnouncementText registerRoleAnnouncementText(ResourceLocation roleId,
            RoleAnnouncementText role) {
        ROLE_ANNOUNCEMENT_TEXTS.put(roleId, role);
        LoggerFactory.getLogger(RoleAnnouncementTexts.class)
                .info("Register Harpy Job: " + role.getId().getPath());
        return role;
    }

    public static RoleAnnouncementText getFromName(String name) {
        for (var t : ROLE_ANNOUNCEMENT_TEXTS.entrySet()) {
            if (t.getValue().getId().getPath().toLowerCase().equals(name.toLowerCase())) {
                return t.getValue();
            }
        }
        return null;
    }

    // 为现有职业注册公告文本
    static {
        // 为每个注册的角色创建对应的公告文本
        for (Role role : TMMRoles.ROLES.values()) {
            ResourceLocation roleId = role.identifier();
            registerRoleAnnouncementText(roleId, new RoleAnnouncementText(roleId, role.getColor()));
        }
    }

    public static class RoleAnnouncementText {
        public ResourceLocation getId() {
            if (id == null) {
                return ResourceLocation.fromNamespaceAndPath("", "");
            }
            return id;
        }

        private final ResourceLocation id;
        public final int colour;
        public final Component roleText;
        public final Component titleText;
        public final Component welcomeText;
        public final Function<Integer, Component> premiseText;
        public final Function<Integer, Component> goalText;
        public final Component winText;

        public RoleAnnouncementText(ResourceLocation id, int colour) {
            this.id = id;
            this.colour = colour;
            this.roleText = Component.translatable("announcement.role." + this.id.getPath().toLowerCase())
                    .withColor(this.colour);
            this.titleText = Component.translatable("announcement.title." + this.id.getPath().toLowerCase())
                    .withColor(this.colour);
            this.welcomeText = Component.translatable("announcement.welcome", this.roleText).withColor(0xF0F0F0);
            this.premiseText = (count) -> Component
                    .translatable(count == 1 ? "announcement.premise" : "announcement.premises", count);
            this.goalText = (count) -> Component
                    .translatable(
                            (count == 1 ? "announcement.goals." : "announcement.goals.")
                                    + this.id.getPath().toLowerCase(),
                            count)
                    .withColor(this.colour);
            this.winText = Component.translatable("announcement.win." + this.id.getPath().toLowerCase())
                    .withColor(this.colour);
        }

        public RoleAnnouncementText(String name, int colour) {
            this(ResourceLocation.tryParse(name), colour);
        }

        public Component getLoseText() {
            ResourceLocation killerRoleId = ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "killer");
            ResourceLocation civilianRoleId = ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "civilian");

            if ("killer".equals(this.id.getPath())) {
                RoleAnnouncementText civilianText = ROLE_ANNOUNCEMENT_TEXTS.get(civilianRoleId);
                return civilianText != null ? civilianText.winText
                        : Component.literal("Passengers Win!").withColor(0xFFFFFF);
            } else {
                RoleAnnouncementText killerText = ROLE_ANNOUNCEMENT_TEXTS.get(killerRoleId);
                return killerText != null ? killerText.winText : Component.literal("Killers Win!").withColor(0xFF0000);
            }
        }

        public @Nullable Component getEndText(GameFunctions.@NotNull WinStatus status, Component winner) {
            return switch (status) {
                case NONE -> null;
                case PASSENGERS, TIME -> this.id.getPath().equals("killer") ? this.getLoseText() : this.winText;
                case KILLERS -> this.id.getPath().equals("killer") ? this.winText : this.getLoseText();
                case GAMBLER ->
                    Component.translatable("announcement.win.gambler", winner)
                            .withColor(new Color(128, 0, 128).getRGB());
                case RECORDER ->
                    Component.translatable("announcement.win.recorder", winner)
                            .withColor(new Color(128, 128, 128).getRGB());
                case LOOSE_END -> {
                    ResourceLocation looseEndRoleId = ResourceLocation.fromNamespaceAndPath("trainmurdermystery",
                            "loose_end");
                    RoleAnnouncementText looseEndText = ROLE_ANNOUNCEMENT_TEXTS.get(looseEndRoleId);
                    int looseEndColor = looseEndText != null ? looseEndText.colour : 0x9F0000;
                    yield Component.translatable("announcement.win.loose_end", winner).withColor(looseEndColor);
                }
                case NO_PLAYER ->
                    Component.translatable("announcement.win.noplayer", winner).withColor(Color.LIGHT_GRAY.getRGB());
                default -> Component.translatable("announcement.win.unknown", winner).withColor(Color.ORANGE.getRGB());
            };
        }
    }

    /**
     * 根据角色ID获取对应的公告文本
     * 
     * @param roleId 角色的ResourceLocation标识符
     * @return 对应的公告文本，如果不存在则返回null
     */
    public static @Nullable RoleAnnouncementText getRoleAnnouncementText(ResourceLocation roleId) {
        return ROLE_ANNOUNCEMENT_TEXTS.get(roleId);
    }

    /**
     * 根据角色ID获取对应的公告文本
     * 
     * @param roleId 角色ID字符串
     * @return 对应的公告文本，如果不存在则返回null
     */
    public static @Nullable RoleAnnouncementText getRoleAnnouncementText(String roleId) {
        return ROLE_ANNOUNCEMENT_TEXTS.get(ResourceLocation.tryParse(roleId));
    }

    // 保留原有的静态常量访问方法以兼容旧代码
    public static final RoleAnnouncementText BLANK = new RoleAnnouncementText("", 0xFFFFFF);
    public static final RoleAnnouncementText CIVILIAN = getRoleAnnouncementText(
            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "civilian")) != null
                    ? getRoleAnnouncementText(ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "civilian"))
                    : registerRoleAnnouncementText(
                            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "civilian"),
                            new RoleAnnouncementText(
                                    ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "civilian"), 0x36E51B));
    public static final RoleAnnouncementText VIGILANTE = getRoleAnnouncementText(
            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "vigilante")) != null
                    ? getRoleAnnouncementText(ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "vigilante"))
                    : registerRoleAnnouncementText(
                            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "vigilante"),
                            new RoleAnnouncementText(
                                    ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "vigilante"),
                                    Color.CYAN.getRGB()));

    public static final RoleAnnouncementText KILLER = getRoleAnnouncementText(
            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "killer")) != null
                    ? getRoleAnnouncementText(ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "killer"))
                    : registerRoleAnnouncementText(
                            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "killer"),
                            new RoleAnnouncementText(
                                    ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "killer"), 0xC13838));
    public static final RoleAnnouncementText LOOSE_END = getRoleAnnouncementText(
            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "loose_end")) != null
                    ? getRoleAnnouncementText(ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "loose_end"))
                    : registerRoleAnnouncementText(
                            ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "loose_end"),
                            new RoleAnnouncementText(
                                    ResourceLocation.fromNamespaceAndPath("trainmurdermystery", "loose_end"),
                                    0x9F0000));
}