package dev.doctor4t.trainmurdermystery.index.tag;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface TMMBlockTags {

    TagKey<Block> BRANCHES = create("branches");
    TagKey<Block> VENT_SHAFTS = create("vent_shafts");
    TagKey<Block> VENT_HATCHES = create("vent_hatches");
    TagKey<Block> WALKWAYS = create("walkways");
    TagKey<Block> SPRINKLERS = create("sprinklers");

    private static TagKey<Block> create(String id) {
        return TagKey.create(Registries.BLOCK, TMM.id(id));
    }
}
