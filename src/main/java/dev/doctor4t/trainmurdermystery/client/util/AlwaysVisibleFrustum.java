package dev.doctor4t.trainmurdermystery.client.util;

import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class AlwaysVisibleFrustum extends Frustum {
    public AlwaysVisibleFrustum(Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        super(positionMatrix, projectionMatrix);
    }

    public AlwaysVisibleFrustum(Frustum frustum) {
        super(frustum);
    }

    @Override
    public boolean isVisible(AABB box) {
        if (TMMClient.isTrainMoving()) {
            if (TMMConfig.isUltraPerfMode()) {
                return super.isVisible(box) && box.getCenter().y() < 148 && box.getCenter().y() > 112;
            }

            return box.getCenter().y() < 148 && box.getCenter().y() > -64;
        }

        return super.isVisible(box);
    }
}
