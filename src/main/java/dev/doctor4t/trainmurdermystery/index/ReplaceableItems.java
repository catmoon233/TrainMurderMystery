package dev.doctor4t.trainmurdermystery.index;

import java.util.function.Consumer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class ReplaceableItems {
    public Item LETTER;
    public Consumer<ServerPlayer> LETTER_UpdateItemFunc;
}
