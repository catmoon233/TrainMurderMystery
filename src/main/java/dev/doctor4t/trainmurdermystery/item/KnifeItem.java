package dev.doctor4t.trainmurdermystery.item;

import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMCosmetics;
import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.KnifeStabPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import walksy.crosshairaddons.CrosshairAddons;

import java.util.List;
import java.util.Locale;
import java.util.Random;


public class KnifeItem extends Item implements ItemWithSkin {
    public KnifeItem(Properties settings) {
        super(settings);
    }

    public static final ResourceLocation ITEM_ID = TMM.id("knife");
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, @NotNull Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        user.playSound(TMMSounds.ITEM_KNIFE_PREPARE, 1.0f, 1.0f);
        return InteractionResultHolder.consume(itemStack);
    }
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType == ClickAction.SECONDARY && otherStack.isEmpty())  {
            // 使用玩家的CCA组件来获取和设置皮肤，而不是直接使用TMMCosmetics
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            String currentSkin = skinsComponent.getSkinFromDataSync(stack);
            Skin currentSkinEnum = Skin.fromString(currentSkin);
            String nextSkinName = Skin.getNext(currentSkinEnum).getName();
            
            // 更新玩家的皮肤组件
            skinsComponent.setEquippedSkinForItemType("knife", nextSkinName);
            // 同时更新物品的皮肤数据组件
            TMMCosmetics.setSkin(player, stack, nextSkinName);
            // 更新数据同步
            skinsComponent.setSkinInDataSync(stack, nextSkinName);

            return true;
        } else return false;
    }
    public enum Skin {
        DEFAULT(Colors.LIGHT_GRAY, "Kitchen Knife"),
        CEREMONIAL(0xFFD98C28, "Ceremonial Dagger"),
        PICK(0xFF8D4A51, "Ice Pick");

        public final int color;
        public final @Nullable String tooltipName;
        public final Random random;

        Skin(int color, @Nullable String tooltipName) {
            this.color = color;
            this.tooltipName = tooltipName;
            this.random = new Random();
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public int getColor() {
            return this.color;
        }

        public static Skin fromString(String name) {
            for (Skin skin : Skin.values()) if (skin.getName().equalsIgnoreCase(name)) return skin;
            return DEFAULT;
        }

        public static Skin getNext(Skin skin) {
            Skin[] values = Skin.values();
            return values[(skin.ordinal() + 1) % values.length];
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        super.inventoryTick(itemStack, level, entity, i, bl);
        if (entity instanceof Player player) {
            if (itemStack.get(TMMDataComponentTypes.OWNER) == null) {
                // 使用玩家的CCA组件来获取和设置皮肤
                PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
                String currentSkin = skinsComponent.getSkinFromDataSync(itemStack);
                Skin currentSkinEnum = Skin.fromString(currentSkin);
                String nextSkinName = Skin.getNext(currentSkinEnum).getName();
                
                // 更新玩家的皮肤组件
                skinsComponent.setEquippedSkinForItemType("knife", nextSkinName);
                // 同时更新物品的皮肤数据组件
                TMMCosmetics.setSkin(player, itemStack, nextSkinName);
                // 更新数据同步
                skinsComponent.setSkinInDataSync(itemStack, nextSkinName);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        // 从玩家的CCA组件获取皮肤名称
        Player player = null;
        if (tooltipContext instanceof net.minecraft.world.entity.player.Player) {
            player = (Player) tooltipContext;
        } else {
            player = net.minecraft.client.Minecraft.getInstance().player;
        }
        
        String skinName = "default";
        if (player != null) {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            skinName = skinsComponent.getSkinFromDataSync(itemStack);
        } else {
            // 回退到原来的实现
            skinName = TMMCosmetics.getSkin(itemStack);
        }
        Skin skin = Skin.fromString(skinName);

        if (skin != null) {
            list.add(Component.translatable("tip.skin").withStyle(style -> style.withColor(Colors.GRAY))
                    .append(Component.literal(TextUtils.formatValueString(skin.tooltipName)).withStyle(style -> style.withColor(skin.getColor()))));
        }

        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
    }


    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user.isSpectator()) {
            return;
        }
        if (remainingUseTicks >= this.getUseDuration(stack, user) - 8 || !(user instanceof Player attacker) || !world.isClientSide)
            return;
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        final var role = game.getRole(attacker);
        if (role != null){
            if (!role.onUseKnife(attacker)) {
                return;
            }
        }
        HitResult collision = getKnifeTarget(attacker);
        if (collision instanceof EntityHitResult entityHitResult) {
            Entity target = entityHitResult.getEntity();
            if (TMM.REPLAY_MANAGER != null) {
                TMM.REPLAY_MANAGER.recordItemUse(user.getUUID(), BuiltInRegistries.ITEM.getKey(this));
            }
            ClientPlayNetworking.send(new KnifeStabPayload(target.getId()));
            if (FabricLoader.getInstance().isModLoaded("crosshairaddons")){
                final var addonStateManager = CrosshairAddons.getStateManager();
                if (target instanceof LivingEntity livingEntity) {
                    addonStateManager.onAttackEntity(livingEntity);
                }
            }
        }
    }

    public static HitResult getKnifeTarget(Player user) {
        return ProjectileUtil.getHitResultOnViewVector(user, entity -> entity instanceof Player player && GameFunctions.isPlayerAliveAndSurvival(player), 4f);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 120;
    }
}