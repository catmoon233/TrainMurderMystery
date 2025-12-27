package dev.doctor4t.trainmurdermystery.item;

import dev.doctor4t.trainmurdermystery.entity.GrenadeEntity;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GrenadeItem extends Item {
	public static final int MAX_CHARGE_TIME = 20; // 最大蓄力时间（ticks），对应1秒
	
	public GrenadeItem(Item.Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		user.startUsingItem(hand);
		return InteractionResultHolder.consume(itemStack);
	}
	
	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
		if (!world.isClientSide) {
			// 计算蓄力时间
			int chargeTime = this.getUseDuration(stack, user) - remainingUseTicks;
			
			// 确保蓄力时间在合理范围内
			chargeTime = Math.max(0, Math.min(chargeTime, MAX_CHARGE_TIME));
			
			// 播放投掷声音
			world.playSound(null, user.getX(), user.getY(), user.getZ(), TMMSounds.ITEM_GRENADE_THROW, SoundSource.NEUTRAL, 0.5F, 1F + (world.random.nextFloat() - .5f) / 10f);
			
			// 创建手榴弹实体
			GrenadeEntity grenade = new GrenadeEntity(TMMEntities.GRENADE, world);
			grenade.setOwner(user);
			grenade.setPosRaw(user.getX(), user.getEyeY() - 0.1, user.getZ());
			
			// 根据蓄力时间计算投掷速度（最小速度0.3，最大速度1.2）
			float velocity = 0.4F + (0.75F * (float) chargeTime / MAX_CHARGE_TIME);
			
			// 设置手榴弹的速度和方向
			grenade.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, velocity, 1.0F);
			world.addFreshEntity(grenade);
		}
		

		
//		user.incrementStat(Stats.USED.getOrCreateStat(this));
		stack.consume(1, user);
	}
	
	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}
}