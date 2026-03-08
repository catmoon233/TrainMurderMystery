package dev.doctor4t.trainmurdermystery.cca.network;

import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 皮肤网络同步初始化器
 * 在玩家加入服务器时初始化皮肤同步
 */
public class SkinsNetworkSyncInitializer {
    private static final Logger logger = LoggerFactory.getLogger(SkinsNetworkSyncInitializer.class);
    
    // 网络服务器配置
    private static String NETWORK_HOST = "rm.aurorian.cn";
    private static int NETWORK_PORT = 25569;
    
    /**
     * 注册服务器连接事件
     */
    public static void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            onPlayerJoin(player);
        });
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            onPlayerDisconnect(player);
        });
    }
    
    /**
     * 玩家加入服务器时的处理
     */
    private static void onPlayerJoin(ServerPlayer player) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            if (skinsComponent != null) {
                // 初始化网络同步，连接到TCP服务器
                skinsComponent.initializeNetworkSync(NETWORK_HOST, NETWORK_PORT);
                
                // 尝试从网络拉取之前保存的皮肤数据
                skinsComponent.pullSkinsFromNetwork();
                
                logger.info("玩家 {} 的皮肤网络同步已初始化", player.getName().getString());
            }
        } catch (Exception e) {
            logger.error("初始化玩家 {} 的皮肤网络同步时出错", player.getName().getString(), e);
        }
    }
    
    /**
     * 玩家断开连接时的处理
     */
    private static void onPlayerDisconnect(ServerPlayer player) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            if (skinsComponent != null && skinsComponent.isNetworkSyncEnabled()) {
                // 异步执行最后一次同步和断开连接
                skinsComponent.pullSkinsFromNetwork();
                skinsComponent.disableNetworkSync();
                
                logger.info("玩家 {} 的皮肤网络同步已断开", player.getName().getString());
            }
        } catch (Exception e) {
            logger.error("处理玩家 {} 的皮肤网络同步断开时出错", player.getName().getString(), e);
        }
    }
    
    /**
     * 设置网络服务器地址
     */
    public static void setNetworkServer(String host, int port) {
        NETWORK_HOST = host;
        NETWORK_PORT = port;
        logger.info("皮肤网络服务器已设置: {}:{}", host, port);
    }
    
    /**
     * 获取网络服务器主机
     */
    public static String getNetworkHost() {
        return NETWORK_HOST;
    }
    
    /**
     * 获取网络服务器端口
     */
    public static int getNetworkPort() {
        return NETWORK_PORT;
    }
}
