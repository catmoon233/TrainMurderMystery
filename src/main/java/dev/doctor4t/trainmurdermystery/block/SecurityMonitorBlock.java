package dev.doctor4t.trainmurdermystery.block;

import com.mojang.serialization.MapCodec;
import dev.doctor4t.trainmurdermystery.block_entity.CameraBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SecurityMonitorBlockEntity;
import dev.doctor4t.trainmurdermystery.network.PacketTracker;
import dev.doctor4t.trainmurdermystery.network.SecurityCameraModePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SecurityMonitorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    
    // 添加监控模式相关字段
    private static BlockPos currentCameraPos = null;
    private static boolean isInSecurityMode = false;
    public static float lastCameraYaw;
    public static float lastCameraPitch;
    public static float yawIncrease;
    public static float pitchIncrease;
    public static float currentYaw = 0.0f; // 记录当前视角的yaw
    public static float currentPitch = 0.0f; // 记录当前视角的pitch


    public static boolean onPlayerRotated(double yawAdd, double pitchAdd) {
        if (isInSecurityMode()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return false;
            
            // 检查玩家是否在创造模式
            boolean isCreativeMode = player.isCreative();
            
            float scale = 0.2f ;
            yawIncrease += (float) (yawAdd * scale);
            pitchIncrease += (float) (pitchAdd * scale);

            if (isCreativeMode) {
                // 在创造模式下，允许自由调整视角
                currentYaw += (float) yawAdd * scale;
                currentPitch = Mth.clamp(currentPitch + (float) pitchAdd * scale, -90, 90);
                
                // 更新玩家朝向
                player.turn(Mth.wrapDegrees(currentYaw - player.yHeadRot),
                        Mth.wrapDegrees(currentPitch - player.getXRot()));
                player.yHeadRotO = player.yHeadRot;
                player.xRotO = player.getXRot();
            } else {
                // 在非创造模式下，保持固定视角，仅用于监控
                //make player face camera while maneuvering
                player.turn(Mth.wrapDegrees((lastCameraYaw + yawAdd) - player.yHeadRot),
                        Mth.wrapDegrees((lastCameraPitch + pitchAdd) - player.getXRot()));
                player.yHeadRotO = player.yHeadRot;
                player.xRotO = player.getXRot();
            }

            return true;
        }
        return false;
    }
    private static boolean preventShiftTillNextKeyUp = false;
    public static void onInputUpdate(Input input) {
        // resets input
        if (isInSecurityMode()) {
            input.down = false;
            input.up = false;
            input.left = false;
            input.right = false;
            input.forwardImpulse = 0;
            input.leftImpulse = 0;
        }
        input.shiftKeyDown = false;
        input.jumping = false;
    }
    public static void modifyInputUpdate(Input instance, LocalPlayer player) {
        if (isInSecurityMode()) {
            onInputUpdate(instance);
            preventShiftTillNextKeyUp = true;
        } else if (preventShiftTillNextKeyUp) {
            if (!instance.shiftKeyDown) {
                preventShiftTillNextKeyUp = false;
            } else {
                instance.shiftKeyDown = false;
            }
        }
    }
    public static boolean onEarlyKeyPress(int key, int scanCode, int action, int modifiers) {
        if (!isInSecurityMode()) return false;
        if (action != GLFW.GLFW_PRESS) return false;
        var options = Minecraft.getInstance().options;
        if (key == 256) {

            return true;
        } else if (options.keyInventory.matches(key, scanCode)) {

            return true;
        }
        if (options.keyJump.matches(key, scanCode)) {

            return true;
        }
        if (options.keyShift.matches(key, scanCode)) {

            return true;
        }
        return false;
    }
    public SecurityMonitorBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    private static final MapCodec<SecurityMonitorBlock> CODEC = simpleCodec(SecurityMonitorBlock::new);

    public static boolean setupCameraMod(Camera camera, BlockGetter level, Entity entity,
                                         boolean detached, boolean thirdPersonReverse, float partialTick) {

        if (!SecurityMonitorBlock.isInSecurityMode()) return false;
        BlockPos cameraPos = SecurityMonitorBlock.getCurrentCameraPos();

        float targetYRot;
        float targetXRot;
        
        LocalPlayer player = Minecraft.getInstance().player;
        boolean isCreativeMode = player != null && player.isCreative();
        
        // 获取摄像头的朝向 - 通过Minecraft实例获取世界
        Level actualLevel = player != null ? player.level() : null;
        if (actualLevel != null && cameraPos != null) {
            BlockState cameraState = actualLevel.getBlockState(cameraPos);
            if (cameraState.getBlock() instanceof CameraBlock) {
                Direction cameraFacing = cameraState.getValue(CameraBlock.FACING);
                
                // 根据摄像头方向计算基础旋转角度
                float baseYaw = getBaseYawFromDirection(cameraFacing);
                
                if (isCreativeMode) {
                    // 在创造模式下，使用玩家当前调整的视角
                    targetYRot = baseYaw + currentYaw + yawIncrease;
                    targetXRot = Mth.clamp(currentPitch + pitchIncrease, -90, 90);
                } else {
                    // 在非创造模式下，基于摄像头方向进行视角调整
                    targetYRot = baseYaw + currentYaw + yawIncrease;
                    targetXRot = Mth.clamp(currentPitch + pitchIncrease, -90, 90);
                }
            } else {
                // 如果不是摄像头，则使用默认行为
                if (isCreativeMode) {
                    // 在创造模式下，使用玩家当前调整的视角
                    targetYRot = currentYaw + yawIncrease;
                    targetXRot = Mth.clamp(currentPitch + pitchIncrease, -90, 90);
                } else {
                    // 在非创造模式下，固定视角
                    targetYRot = camera.getYRot() + yawIncrease;
                    targetXRot = Mth.clamp(camera.getXRot() + pitchIncrease -180, -180, 180);
                }
            }
        } else {
            // 如果无法获取世界或摄像头位置，则使用默认行为
            if (isCreativeMode) {
                targetYRot = currentYaw + yawIncrease;
                targetXRot = Mth.clamp(currentPitch + pitchIncrease, -90, 90);
            } else {
                targetYRot = camera.getYRot() + yawIncrease;
                targetXRot = Mth.clamp(camera.getXRot() + pitchIncrease -180, -180, 180);
            }
        }
        
        camera.setRotation(targetYRot, targetXRot);
        // lerp camera
        Vec3 targetCameraPos = cameraPos.getCenter().add(0.5, -1.2, 0.5);

        camera.setPosition(targetCameraPos);

        lastCameraYaw = camera.getYRot();
        lastCameraPitch = camera.getXRot();

        yawIncrease = 0;
        pitchIncrease = 0;

        float followSpeed = 1;

        return true;
    }
    
    /**
     * 根据方向获取基础偏航角
     * @param direction 摄像头方向
     * @return 对应的基础偏航角
     */
    private static float getBaseYawFromDirection(Direction direction) {
        switch (direction) {
            case NORTH: // -Z方向
                return 180.0f;
            case SOUTH: // +Z方向
                return 0.0f;
            case WEST:  // -X方向
                return 90.0f;
            case EAST:  // +X方向
                return -90.0f;
            default:
                return 0.0f;
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SecurityMonitorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS; // 客户端直接返回，主要逻辑在服务端
        }

        // 检查玩家是否按下了Shift键
        if (player.isShiftKeyDown()) {
            // 退出监控模式
            exitSecurityMode((net.minecraft.server.level.ServerPlayer) player);
            return InteractionResult.SUCCESS;
        } else {
            // 检查监控器上是否有摄像头位置数据
            SecurityMonitorBlockEntity monitorEntity = (SecurityMonitorBlockEntity) world.getBlockEntity(pos);
            if (monitorEntity != null) {
                List<BlockPos> cameraPositions = monitorEntity.getCameraPositions();
                if (cameraPositions.isEmpty()) {
                    player.displayClientMessage(Component.literal("此监控器未连接任何摄像头").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.SUCCESS;
                } else {
                    // 进入监控模式，循环切换摄像头
                    cycleToNextCamera(player, cameraPositions);
                    enterSecurityMode((net.minecraft.server.level.ServerPlayer) player);
                    return InteractionResult.SUCCESS;
                }
            } else {
                player.displayClientMessage(Component.literal("监控器数据错误").withStyle(ChatFormatting.RED), true);
                return InteractionResult.SUCCESS;
            }
        }
    }

    private static void cycleToNextCamera(Player player, List<BlockPos> cameraPositions) {
        if (cameraPositions.isEmpty()) return;

        int currentIndex = -1;
        if (currentCameraPos != null) {
            currentIndex = cameraPositions.indexOf(currentCameraPos);
        }

        int nextIndex = (currentIndex + 1) % cameraPositions.size();
        currentCameraPos = cameraPositions.get(nextIndex);

        // 重置视角为新摄像头的初始视角
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        
        // 获取摄像头的朝向信息
        if (player.level() != null && player.level().getBlockEntity(currentCameraPos) instanceof CameraBlockEntity cameraBlockEntity) {
            Direction cameraFacing = cameraBlockEntity.getFacing();
            player.displayClientMessage(Component.literal("切换到摄像头 " + (nextIndex + 1) + 
                    ": X=" + currentCameraPos.getX() + ", Y=" + currentCameraPos.getY() + 
                    ", Z=" + currentCameraPos.getZ() + ", 方向=" + cameraFacing.getName()).withStyle(ChatFormatting.AQUA), true);
        } else {
            player.displayClientMessage(Component.literal("切换到摄像头 " + (nextIndex + 1) + 
                    ": X=" + currentCameraPos.getX() + ", Y=" + currentCameraPos.getY() + 
                    ", Z=" + currentCameraPos.getZ()).withStyle(ChatFormatting.AQUA), true);
        }
    }

    private static void enterSecurityMode(net.minecraft.server.level.ServerPlayer player) {
        isInSecurityMode = true;
        player.displayClientMessage(Component.literal("已进入监控模式").withStyle(ChatFormatting.GREEN), true);

        // 发送网络包到客户端以更新视角
        ServerPlayNetworking.send(player, new SecurityCameraModePayload(true, currentCameraPos, currentYaw, currentPitch));
    }

    private static void exitSecurityMode(net.minecraft.server.level.ServerPlayer player) {
        isInSecurityMode = false;
        currentCameraPos = null;
        // 重置视角参数
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        player.displayClientMessage(Component.literal("已退出监控模式").withStyle(ChatFormatting.RED), true);

        // 发送网络包到客户端以更新视角
        PacketTracker.sendToClient(player, new SecurityCameraModePayload(false, BlockPos.ZERO, currentYaw, currentPitch));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
    
    // 提供公共方法供其他类使用
    public static boolean isInSecurityMode() {
        return isInSecurityMode;
    }

    public static BlockPos getCurrentCameraPos() {
        return currentCameraPos;
    }
    
    public static void setCurrentCameraPos(BlockPos pos) {
        currentCameraPos = pos;
    }
    
    public static void setSecurityMode(boolean mode) {
        isInSecurityMode = mode;
    }
}