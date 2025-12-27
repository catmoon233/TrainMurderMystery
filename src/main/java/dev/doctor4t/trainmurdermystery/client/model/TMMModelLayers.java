package dev.doctor4t.trainmurdermystery.client.model;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.model.entity.PlayerSkeletonEntityModel;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.SmallDoorBlockEntityRenderer;
import dev.doctor4t.trainmurdermystery.client.render.block_entity.WheelBlockEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public interface TMMModelLayers {
    ModelLayerLocation SMALL_DOOR = layer("small_door");
    ModelLayerLocation PLAYER_BODY = layer("player_body");
    ModelLayerLocation PLAYER_BODY_SLIM = layer("player_body_slim");
    ModelLayerLocation WHEEL = layer("wheel");
    ModelLayerLocation PLAYER_SKELETON = layer("player_skeleton");

    static void initialize() {
        EntityModelLayerRegistry.registerModelLayer(SMALL_DOOR, SmallDoorBlockEntityRenderer::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(PLAYER_BODY, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64));
        EntityModelLayerRegistry.registerModelLayer(PLAYER_BODY_SLIM, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64));
        EntityModelLayerRegistry.registerModelLayer(WHEEL, WheelBlockEntityRenderer::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(PLAYER_SKELETON, PlayerSkeletonEntityModel::getTexturedModelData);
    }

    private static ModelLayerLocation layer(String id, String name) {
        return new ModelLayerLocation(TMM.id(id), name);
    }

    private static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(TMM.id(id), "main");
    }

}
