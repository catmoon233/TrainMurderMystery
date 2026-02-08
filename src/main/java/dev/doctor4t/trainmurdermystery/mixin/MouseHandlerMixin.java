package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.block.SecurityMonitorBlock;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    public void TMM$turnPlayer(double d, CallbackInfo ci) {
        // 在监控模式下，阻止正常的玩家旋转，但传递给SecurityMonitorBlock处理
        if (SecurityMonitorBlock.isInSecurityMode()) {
            ci.cancel();
        }
    }

    // 捕获鼠标移动并传递给SecurityMonitorBlock
    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    public void TMM$onMove(long window, double x, double y, CallbackInfo ci) {
        if (SecurityMonitorBlock.isInSecurityMode()) {
            // 传递鼠标移动给监控视角控制
            // double xOffset = x;
            // double yOffset = y;
            // 注意：xOffset是水平旋转(yaw)，yOffset是垂直旋转(pitch)
            // 这里的参数值是原始鼠标移动量，需要进行适当的缩放
            // SecurityMonitorBlock.onPlayerRotated(xOffset, yOffset);
            ci.cancel();
        }
    }
}
