package dev.doctor4t.trainmurdermystery.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.dimension.DimensionOptions;

public class TMMDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        // this is so that the dimension options can actually generate
        DynamicRegistries.register(RegistryKeys.DIMENSION, DimensionOptions.CODEC);

        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(TMMModelGen::new);
        pack.addProvider(TMMBlockTagGen::new);
        pack.addProvider(TMMItemTagGen::new);
        pack.addProvider(TMMLangGen::new);
        pack.addProvider(TMMBlockLootTableGen::new);
    }
}
