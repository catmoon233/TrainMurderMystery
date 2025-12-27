package dev.doctor4t.trainmurdermystery.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Either;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.client.StaminaRenderer;
import dev.doctor4t.trainmurdermystery.event.AllowPlayerPunching;
import dev.doctor4t.trainmurdermystery.event.IsPlayerPunchable;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.item.CocktailItem;
import dev.doctor4t.trainmurdermystery.util.PlayerStaminaGetter;
import dev.doctor4t.trainmurdermystery.util.PoisonUtils;
import dev.doctor4t.trainmurdermystery.util.Scheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerStaminaGetter {


	@Shadow public abstract float getAttackStrengthScale(float baseTime);

	@Override
	public float trainmurdermystery$getStamina() {
		return sprintingTicks;
	}



	@Unique
	public float sprintingTicks;
	@Unique
	private Scheduler.ScheduledTask poisonSleepTask;

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}




	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	public float tmm$overrideMovementSpeed(float original) {
		if (GameFunctions.isPlayerAliveAndSurvival((Player) (Object) this)) {
			return this.isSprinting() ? 0.1f : 0.07f;
		} else {
			return original;
		}
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	public void tmm$limitSprint(CallbackInfo ci) {
		GameWorldComponent gameComponent = GameWorldComponent.KEY.get(this.level());
		if (GameFunctions.isPlayerAliveAndSurvival((Player) (Object) this) && gameComponent != null && gameComponent.isRunning()) {
			Role role = gameComponent.getRole((Player) (Object) this);
			if (role != null && role.getMaxSprintTime() >= 0) {
				if (this.isSprinting()) {
					sprintingTicks = Math.max(sprintingTicks - 1, 0);
				} else {
					sprintingTicks = Math.min(sprintingTicks + 0.4f, role.getMaxSprintTime());
				}

				if (sprintingTicks <= 0) {
					this.setSprinting(false);
				}
			}
		}

	}

	@WrapMethod(method = "attack")
	public void attack(Entity target, Operation<Void> original) {
		Player self = (Player) (Object) this;

		if (getMainHandItem().is(TMMItems.BAT) && target instanceof Player playerTarget && this.getAttackStrengthScale(0.5F) >= 1f) {
			GameFunctions.killPlayer(playerTarget, true, self, GameConstants.DeathReasons.BAT);
			self.getCommandSenderWorld().playSound(self,
					playerTarget.getX(), playerTarget.getEyeY(), playerTarget.getZ(),
					TMMSounds.ITEM_BAT_HIT, SoundSource.PLAYERS,
					3f, 1f);
			return;
		}

		if (!GameFunctions.isPlayerAliveAndSurvival(self) || this.getMainHandItem().is(TMMItems.KNIFE)
				|| IsPlayerPunchable.EVENT.invoker().gotPunchable(target) || AllowPlayerPunching.EVENT.invoker().allowPunching(self)) {
			original.call(target);
		}
	}

	@Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V", shift = At.Shift.AFTER))
	private void tmm$poisonedFoodEffect(@NotNull Level world, ItemStack stack, FoodProperties foodComponent, CallbackInfoReturnable<ItemStack> cir) {
		if (world.isClientSide) return;
		String poisoner = stack.getOrDefault(TMMDataComponentTypes.POISONER, null);
		if (poisoner != null) {
			int poisonTicks = PlayerPoisonComponent.KEY.get(this).poisonTicks;
			if (poisonTicks == -1) {
				PlayerPoisonComponent.KEY.get(this).setPoisonTicks(world.getRandom().nextIntBetweenInclusive(PlayerPoisonComponent.clampTime.getA(), PlayerPoisonComponent.clampTime.getB()), UUID.fromString(poisoner));
			} else {
				PlayerPoisonComponent.KEY.get(this).setPoisonTicks(Mth.clamp(poisonTicks - world.getRandom().nextIntBetweenInclusive(100, 300), 0, PlayerPoisonComponent.clampTime.getB()), UUID.fromString(poisoner));
			}
		}
	}

	@Inject(method = "stopSleepInBed(ZZ)V", at = @At("HEAD"))
	private void tmm$poisonSleep(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
		if (this.poisonSleepTask != null) {
			this.poisonSleepTask.cancel();
			this.poisonSleepTask = null;
		}
	}

	@Inject(method = "startSleepInBed", at = @At("TAIL"))
	private void tmm$poisonSleepMessage(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
		Player self = (Player) (Object) (this);
		if (cir.getReturnValue().right().isPresent() && self instanceof ServerPlayer serverPlayer) {
			if (this.poisonSleepTask != null) this.poisonSleepTask.cancel();

			this.poisonSleepTask = Scheduler.schedule(
					() -> PoisonUtils.bedPoison(serverPlayer),
					40
			);
		}
	}

	@Inject(method = "canEat(Z)Z", at = @At("HEAD"), cancellable = true)
	private void tmm$allowEatingRegardlessOfHunger(boolean ignoreHunger, @NotNull CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
	private void tmm$eat(Level world, ItemStack stack, FoodProperties foodComponent, @NotNull CallbackInfoReturnable<ItemStack> cir) {
		if (!(stack.getItem() instanceof CocktailItem)) {
			PlayerMoodComponent.KEY.get(this).eatFood();
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void tmm$saveData(CompoundTag nbt, CallbackInfo ci) {
		nbt.putFloat("sprintingTicks", this.sprintingTicks);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void tmm$readData(CompoundTag nbt, CallbackInfo ci) {
		this.sprintingTicks = nbt.getFloat("sprintingTicks");
	}
}