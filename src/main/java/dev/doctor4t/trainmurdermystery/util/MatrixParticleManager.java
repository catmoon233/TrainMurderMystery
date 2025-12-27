package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface MatrixParticleManager {
    static Vec3 getMuzzlePosForPlayer(Player playerEntity) {
        Vec3 pos = TMMClient.particleMap.getOrDefault(playerEntity, null);
        TMMClient.particleMap.remove(playerEntity);
        return pos;
    }

    static void setMuzzlePosForPlayer(Player playerEntity, Vec3 vec3d) {
        TMMClient.particleMap.put(playerEntity, vec3d);
    }
}
