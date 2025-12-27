package dev.doctor4t.trainmurdermystery.index.tag;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface TMMItemTags {

    TagKey<Item> GUNS = create("guns");
    TagKey<Item> PSYCHOSIS_ITEMS = create("psychosis_items");

    private static TagKey<Item> create(String id) {
        return TagKey.create(Registries.ITEM, TMM.id(id));
    }
}
