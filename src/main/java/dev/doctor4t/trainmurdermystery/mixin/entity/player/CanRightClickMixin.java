package dev.doctor4t.trainmurdermystery.mixin.entity.player;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.upcraft.datasync.api.ext.DataSyncPlayerExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Player.class)
public abstract class CanRightClickMixin extends LivingEntity implements DataSyncPlayerExt {
    private static final Set<Block> ALLOWED_BLOCKS = new HashSet<>();

    static {
        // 允许的方块集合
        Collections.addAll(ALLOWED_BLOCKS,
                Blocks.LECTERN

        // 这里可以添加其他允许的方块
        );
    }

    // 原版工作方块集合
    private static final Set<Block> VANILLA_WORKSTATIONS = Set.of(
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE,
            Blocks.SMOKER,
            Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE,
            Blocks.SMITHING_TABLE,
            Blocks.GRINDSTONE,
            Blocks.STONECUTTER,
            Blocks.LOOM,
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,
            Blocks.BREWING_STAND,
            Blocks.CAULDRON,
            Blocks.BELL,
            Blocks.ENCHANTING_TABLE,
            Blocks.BEACON,
            Blocks.RESPAWN_ANCHOR,
            Blocks.ENDER_CHEST,
            Blocks.SHULKER_BOX,
            // 更多需要限制的方块...
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.BARREL,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.HOPPER,
            Blocks.COMPOSTER);

    protected CanRightClickMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "canInteractWithBlock", at = @At("TAIL"), cancellable = true)
    public void canInteractWithBlockAt(BlockPos pos, double additionalRange,
            CallbackInfoReturnable<Boolean> cir) {
        if (TMM.isLobby)
            return;
        if (!cir.getReturnValue())
            return;
        final var player = (Player) (Object) this;
        final var mainHandItem = player.getMainHandItem();
        if (TMM.canDropItem.contains(BuiltInRegistries.ITEM.getKey(mainHandItem.getItem()).toString())
                || TMM.canDrop.stream().anyMatch((p) -> {
                    return p.test(player);
                })) {
            if (player.isShiftKeyDown()) {
                final var drop = player.drop(mainHandItem.copy(), true);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                if (drop != null) {
                    drop.setGlowingTag(true);
                    drop.setPickUpDelay(20);
                }
            }
        }
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            return;
        }

        BlockState state = level().getBlockState(pos);
        Block block = state.getBlock();

        if (shouldPreventInteraction(block)) {
            cir.setReturnValue(false);
        }
    }

    private static List<String> cantClickItems = List.of(
            "supplementaries:item_shelf",
            "supplementaries:notice_board",
            "supplementaries:pedestal");

    /**
     * 判断是否应该阻止与方块的交互
     */
    private boolean shouldPreventInteraction(Block block) {
        if (TMM.isLobby)
            return false;
        return !isAllowedBlock(block) || cantClickItems.contains(BuiltInRegistries.BLOCK.getKey(block).toString());
    }

    /**
     * 检查方块是否在允许的列表中
     */
    private boolean isAllowedBlock(Block block) {
        // 如果在允许列表中，直接返回true
        if (ALLOWED_BLOCKS.contains(block)) {
            return true;
        }

        // 如果是原版工作方块，禁止交互
        if (VANILLA_WORKSTATIONS.contains(block)) {
            return false;
        }

        // 检查是否为TMM模组的方块
        // ResourceLocation blockId = level().registryAccess()
        // .registryOrThrow(Registries.BLOCK)
        // .getKey(block);
        //
        // String namespace = blockId.getNamespace();

        return true;
        // 允许TMM模组的方块（除了"minopp"命名空间）
        // return namespace.equals(TMM.MOD_ID) && !namespace.equals("minopp");
    }
}