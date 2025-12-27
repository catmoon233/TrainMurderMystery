package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.index.TMMProperties;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class WorldBlackoutComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<WorldBlackoutComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("blackout"), WorldBlackoutComponent.class);
    private final Level world;
    private final List<BlackoutDetails> blackouts = new ArrayList<>();
    private int ticks = 0;

    public WorldBlackoutComponent(Level world) {
        this.world = world;
    }

    public void reset() {
        for (BlackoutDetails detail : this.blackouts) detail.end(this.world);
        this.blackouts.clear();
    }

    @Override
    public void serverTick() {
        for (int i = 0; i < this.blackouts.size(); i++) {
            BlackoutDetails detail = this.blackouts.get(i);
            detail.tick(this.world);
            if (detail.time <= 0) {
                detail.end(this.world);
                this.blackouts.remove(i);
                i--;
            }
        }
        if (this.ticks > 0) this.ticks--;
    }

    public boolean isBlackoutActive() {
        return this.ticks > 0;
    }

    public boolean triggerBlackout() {
        AreasWorldComponent areas = AreasWorldComponent.KEY.get(world);

        AABB area = areas.playArea;
        if (this.ticks > 0) return false;
        for (int x = (int) area.minX; x <= (int) area.maxX; x++) {
            for (int y = (int) area.minY; y <= (int) area.maxY; y++) {
                for (int z = (int) area.minZ; z <= (int) area.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.world.getBlockState(pos);
                    if (!state.hasProperty(BlockStateProperties.LIT) || !state.hasProperty(TMMProperties.ACTIVE)) continue;
                    int duration = GameConstants.getBlackoutMinDuration() + this.world.random.nextInt(GameConstants.getBlackoutMaxDuration() - GameConstants.getBlackoutMinDuration());
                    if (duration > this.ticks) this.ticks = duration;
                    BlackoutDetails detail = new BlackoutDetails(pos, duration, state.getValue(BlockStateProperties.LIT));
                    detail.init(this.world);
                    this.blackouts.add(detail);
                }
            }
        }
        if (this.world instanceof ServerLevel serverWorld) for (ServerPlayer player : serverWorld.players()) {
            if ((!GameWorldComponent.KEY.get(player).getRole(player).canUseKiller())){
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, false));
            }
            player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.AMBIENT_BLACKOUT), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100f, 1f, player.getRandom().nextLong()));
        }
        return true;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        ListTag list = new ListTag();
        for (BlackoutDetails detail : this.blackouts) list.add(detail.writeToNbt());
        tag.put("blackouts", list);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.blackouts.clear();
        for (Tag element : tag.getList("blackouts", 10)) {
            BlackoutDetails detail = new BlackoutDetails((CompoundTag) element);
            detail.init(this.world);
            this.blackouts.add(detail);
        }
    }

    public static class BlackoutDetails {
        private final BlockPos pos;
        private final boolean original;
        private int time;

        public BlackoutDetails(BlockPos pos, int time, boolean original) {
            this.pos = pos;
            this.time = time;
            this.original = original;
        }

        public BlackoutDetails(@NotNull CompoundTag tag) {
            this.pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            this.time = tag.getInt("time");
            this.original = tag.getBoolean("original");
        }

        public void init(@NotNull Level world) {
            BlockState state = world.getBlockState(this.pos);
            if (!state.hasProperty(BlockStateProperties.LIT) || !state.hasProperty(TMMProperties.ACTIVE)) return;
            world.setBlockAndUpdate(this.pos, state.setValue(BlockStateProperties.LIT, false).setValue(TMMProperties.ACTIVE, false));
            world.playSound(null, this.pos, TMMSounds.BLOCK_LIGHT_TOGGLE, SoundSource.BLOCKS, 0.5f, 1f);
        }

        public void end(@NotNull Level world) {
            BlockState state = world.getBlockState(this.pos);
            if (!state.hasProperty(BlockStateProperties.LIT) || !state.hasProperty(TMMProperties.ACTIVE)) return;
            world.setBlockAndUpdate(this.pos, state.setValue(BlockStateProperties.LIT, this.original).setValue(TMMProperties.ACTIVE, true));
            world.playSound(null, this.pos, TMMSounds.BLOCK_LIGHT_TOGGLE, SoundSource.BLOCKS, 0.5f, 0.5f);
        }

        public void tick(Level world) {
            if (this.time > 0) this.time--;
            if (this.time > 4) return;
            BlockState state = world.getBlockState(this.pos);
            if (!state.hasProperty(BlockStateProperties.LIT) || !state.hasProperty(TMMProperties.ACTIVE)) return;
            switch (this.time) {
                case 0 -> this.end(world);
                case 1, 3 -> {
                    world.setBlockAndUpdate(this.pos, state.setValue(BlockStateProperties.LIT, false));
                    world.playSound(null, this.pos, TMMSounds.BLOCK_BUTTON_TOGGLE_NO_POWER, SoundSource.BLOCKS, 0.1f, 1f);
                }
                case 2, 5 -> {
                    world.setBlockAndUpdate(this.pos, state.setValue(BlockStateProperties.LIT, true));
                    world.playSound(null, this.pos, TMMSounds.BLOCK_BUTTON_TOGGLE_NO_POWER, SoundSource.BLOCKS, 0.1f, 1f);
                }
            }
        }

        public CompoundTag writeToNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", this.pos.getX());
            tag.putInt("y", this.pos.getY());
            tag.putInt("z", this.pos.getZ());
            tag.putInt("time", this.time);
            tag.putBoolean("original", this.original);
            return tag;
        }
    }
}