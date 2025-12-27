package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tmm:config")
                .requires(source -> source.hasPermission(2))
                .executes(ConfigCommand::showConfig)
                .then(Commands.literal("reload")
                        .executes(ConfigCommand::reloadConfig))
                .then(Commands.literal("reset")
                        .executes(ConfigCommand::resetConfig)));
    }
    
    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            TMMConfig.reload();
            source.sendSuccess(
                () -> Component.translatable("commands.tmm.config.reload")
                    .withStyle(style -> style.withColor(0x00FF00)),
                true
            );
            TMM.LOGGER.info("配置文件已由 {} 重载", source.getTextName());
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.tmm.config.reload.fail", e.getMessage()));
            TMM.LOGGER.error("配置重载失败", e);
            return 0;
        }
    }
    
    private static int resetConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            TMMConfig.reset();
            source.sendSuccess(
                () -> Component.translatable("commands.tmm.config.reset")
                    .withStyle(style -> style.withColor(0x00FF00)),
                true
            );
            TMM.LOGGER.info("配置文件已由 {} 重置为默认值", source.getTextName());
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.tmm.config.reset.fail", e.getMessage()));
            TMM.LOGGER.error("配置重置失败", e);
            return 0;
        }
    }

    private static int showConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.header"), false);
        
        // 商店价格
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.header"), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.knife", TMMConfig.knifePrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.revolver", TMMConfig.revolverPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.grenade", TMMConfig.grenadePrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.psycho_mode", TMMConfig.psychoModePrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.poison_vial", TMMConfig.poisonVialPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.scorpion", TMMConfig.scorpionPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.firecracker", TMMConfig.firecrackerPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.lockpick", TMMConfig.lockpickPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.crowbar", TMMConfig.crowbarPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.body_bag", TMMConfig.bodyBagPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.blackout", TMMConfig.blackoutPrice), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.shop_prices.note", TMMConfig.notePrice), false);
        
        // 物品冷却时间
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.header"), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.knife", TMMConfig.knifeCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.revolver", TMMConfig.revolverCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.derringer", TMMConfig.derringerCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.grenade", TMMConfig.grenadeCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.lockpick", TMMConfig.lockpickCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.crowbar", TMMConfig.crowbarCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.body_bag", TMMConfig.bodyBagCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.psycho_mode", TMMConfig.psychoModeCooldown), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.cooldowns.blackout", TMMConfig.blackoutCooldown), false);
        
        // 游戏设置
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.header"), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.starting_money", TMMConfig.startingMoney), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.passive_money_amount", TMMConfig.passiveMoneyAmount), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.passive_money_interval", TMMConfig.passiveMoneyInterval), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.money_per_kill", TMMConfig.moneyPerKill), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.psycho_mode_armor", TMMConfig.psychoModeArmor), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.psycho_mode_duration", TMMConfig.psychoModeDuration), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.firecracker_duration", TMMConfig.firecrackerDuration), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.blackout_min_duration", TMMConfig.blackoutMinDuration), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.game_settings.blackout_max_duration", TMMConfig.blackoutMaxDuration), false);
        
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.footer"), false);
        source.sendSuccess(() -> Component.translatable("commands.tmm.config.show.hint"), false);
        
        return 1;
    }
}