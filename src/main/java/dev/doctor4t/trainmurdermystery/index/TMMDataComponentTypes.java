package dev.doctor4t.trainmurdermystery.index;

import com.mojang.serialization.Codec;
import dev.doctor4t.trainmurdermystery.TMM;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public interface TMMDataComponentTypes {
    DataComponentType<String> POISONER = register("poisoner", stringBuilder -> stringBuilder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    DataComponentType<Boolean> USED = register("used", stringBuilder -> stringBuilder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private static <T> DataComponentType<T> register(String name, @NotNull UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, TMM.id(name), builderOperator.apply(DataComponentType.builder()).build());
    }
}
