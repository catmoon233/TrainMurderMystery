package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.TMMGameConstants;
import dev.doctor4t.trainmurdermystery.util.Carriage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Objects;

public class PlayerMoodComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final Identifier MOOD = TMM.id("mood");
    public static final ComponentKey<PlayerMoodComponent> KEY = ComponentRegistry.getOrCreate(MOOD, PlayerMoodComponent.class);
    public static final Identifier ARROW_UP = TMM.id("hud/arrow_up");
    public static final Identifier ARROW_DOWN = TMM.id("hud/arrow_down");
    public static final Identifier MOOD_HAPPY = TMM.id("hud/mood_happy");
    public static final Identifier MOOD_MID = TMM.id("hud/mood_mid");
    public static final Identifier MOOD_DEPRESSIVE = TMM.id("hud/mood_depressive");
    private final PlayerEntity player;
    private TrainPreference currentPreference = TrainPreference.TRUE;
    private int nextPreferenceTimer = 0;
    public float mood = 1f;
    private boolean fulfilled = false;
    private String previousPreferenceText = "";
    private String preferenceText = "";
    private float preferenceTextAlpha = 0f;
    private float arrowProgress = 1f;

    public PlayerMoodComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Environment(EnvType.CLIENT)
    public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isRunning()) return;
        if (!Objects.equals(this.previousPreferenceText, this.preferenceText)) {
            this.preferenceTextAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, this.preferenceTextAlpha, 0f);
            if (this.preferenceTextAlpha <= 0.01f) this.previousPreferenceText = this.preferenceText;
        } else {
            this.preferenceTextAlpha = MathHelper.lerp(tickCounter.getTickDelta(true) / 4, this.preferenceTextAlpha, 1f);
        }
        if (this.previousPreferenceText.isEmpty()) return;
        var renderer = MinecraftClient.getInstance().textRenderer;
        var textWidth = renderer.getWidth(this.previousPreferenceText);
        context.getMatrices().push();
        context.getMatrices().translate(-(24 + textWidth) * (1f - this.preferenceTextAlpha), 0, 0);
        var mood = MOOD_HAPPY;
        if (this.mood < 0.2f) {
            mood = MOOD_DEPRESSIVE;
        } else if (this.mood < 0.55f) {
            mood = MOOD_MID;
        }
        context.drawGuiTexture(mood, 5, 6, 14, 17);
        this.arrowProgress = MathHelper.lerp(tickCounter.getTickDelta(true) / 16, this.arrowProgress, 1f);
        if (this.arrowProgress < 0.99f) {
            var arrow = this.fulfilled ? ARROW_UP : ARROW_DOWN;
            context.getMatrices().push();
            context.getMatrices().translate(0, this.fulfilled ? 4 - this.arrowProgress * 4 : this.arrowProgress * 4, 0);
            context.drawSprite(7, 6, 0, 10, 13, context.guiAtlasManager.getSprite(arrow), 1f, 1f, 1f, (float) Math.sin(this.arrowProgress * Math.PI));
            context.getMatrices().pop();
        }
        context.drawTextWithShadow(renderer, this.previousPreferenceText, 22, 8, MathHelper.packRgb(1f, 1f, 1f) | ((int) (this.preferenceTextAlpha * 255) << 24));
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(26, 10 + renderer.fontHeight, 0);
        context.getMatrices().translate(-(24 + textWidth) * (1f - this.preferenceTextAlpha), 0, 0);
        context.getMatrices().scale((textWidth - 8) * this.mood, 1, 1);
        context.fill(0, 0, 1, 1, MathHelper.hsvToRgb(this.mood / 3.0F, 1.0F, 1.0F) | ((int) (this.preferenceTextAlpha * 255) << 24));
        context.getMatrices().pop();
    }

    public void reset() {
        this.currentPreference = TrainPreference.TRUE;
        this.nextPreferenceTimer = TMMGameConstants.TIME_TO_FIRST_TASK;
        this.fulfilled = false;
        this.setMood(1f);
        this.sync();
    }

    @Override
    public void clientTick() {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isRunning()) return;
        this.tickMood();
        if (this.player.age % 80 == 0) this.arrowProgress = 0f;
    }

    @Override
    public void serverTick() {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isRunning()) return;
        var shouldSync = false;
        if (this.nextPreferenceTimer > 0) {
            this.nextPreferenceTimer--;
            if (this.nextPreferenceTimer <= 0) {
                this.generatePreference();
                this.preferenceText = this.currentPreference.getString();
                this.nextPreferenceTimer = (int) (this.player.getRandom().nextFloat() * (TMMGameConstants.MAX_PREFERENCE_COOLDOWN - TMMGameConstants.MIN_PREFERENCE_COOLDOWN) + TMMGameConstants.MIN_PREFERENCE_COOLDOWN);
                shouldSync = true;
            }
        }
        if (this.currentPreference.isFulfilled(this.player)) {
            if (!this.fulfilled) shouldSync = true;
            this.fulfilled = true;
        } else {
            if (this.fulfilled) shouldSync = true;
            this.fulfilled = false;
        }
        this.tickMood();
        if (shouldSync) this.sync();
    }

    public void tickMood() {
        if (this.fulfilled) {
            this.setMood(this.mood + TMMGameConstants.MOOD_GAIN);
        } else {
            this.setMood(this.mood - TMMGameConstants.MOOD_DRAIN);
        }
    }

    public void setMood(float mood) {
        this.mood = Math.clamp(mood, 0, 1);
    }

    private void generatePreference() {
        if (!TMMComponents.GAME.get(this.player.getWorld()).isCivilian(this.player)) {
            this.currentPreference = TrainPreference.TRUE;
            return;
        }

        var random = this.player.getRandom();

        // 5% chance to be happy with anything
        if (random.nextFloat() < 0.05f) {
            this.currentPreference = TrainPreference.TRUE;
            return;
        }

        // 30% chance to want to be outside
        if (random.nextFloat() < 0.3f) {
            this.currentPreference = TrainPreference.OUTSIDE;
            return;
        }

        // 10% chance to be tired
        if (random.nextFloat() < 0.1f) {
            this.currentPreference = TrainPreference.SLEEP;
            return;
        }

        var carriages = TMMGameConstants.CARRIAGES;
        if (carriages.isEmpty()) {
            this.currentPreference = TrainPreference.TRUE;
        } else {
            this.currentPreference = new CarriageTrainPreference(carriages.get(this.player.getRandom().nextInt(carriages.size())));
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putFloat("Mood", this.mood);
        tag.putBoolean("Fulfilled", this.fulfilled);
        tag.putString("Preference", this.preferenceText);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.mood = tag.contains("Mood") ? tag.getFloat("Mood") : 1f;
        this.fulfilled = tag.contains("Fulfilled") && tag.getBoolean("Fulfilled");
        this.preferenceText = tag.contains("Preference") ? tag.getString("Preference") : "";
    }

    public interface TrainPreference {
        TrainPreference TRUE = new TrainPreference() {
            @Override
            public boolean isFulfilled(@NotNull PlayerEntity player) {
                return true;
            }

            @Override
            public @NotNull String getString() {
                return "";
            }
        };
        TrainPreference OUTSIDE = new TrainPreference() {
            @Override
            public boolean isFulfilled(@NotNull PlayerEntity player) {
                return player.getWorld().isSkyVisible(player.getBlockPos());
            }

            @Override
            public @NotNull String getString() {
                return "You should get some fresh air";
            }
        };
        TrainPreference SLEEP = new TrainPreference() {
            @Override
            public boolean isFulfilled(@NotNull PlayerEntity player) {
                return player.isSleeping();
            }

            @Override
            public @NotNull String getString() {
                return "You should get some sleep";
            }
        };

        boolean isFulfilled(PlayerEntity player);

        String getString();
    }

    public record CarriageTrainPreference(Carriage carriage) implements TrainPreference {
        @Override
        public boolean isFulfilled(@NotNull PlayerEntity player) {
            return this.carriage.areas().stream().anyMatch(box -> box.contains(player.getPos()));
        }

        @Override
        public @NotNull String getString() {
            return "You feel like visiting %s %s carriage".formatted(this.carriage.areas().size() == 1 ? "the" : "a", this.carriage.name());
        }
    }
}