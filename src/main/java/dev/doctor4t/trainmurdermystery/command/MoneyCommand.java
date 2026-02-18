package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class MoneyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:money")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("get")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> executeGet(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> executeSet(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> executeAdd(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "amount"))))));
    }

    private static int executeGet(CommandSourceStack source, Entity target) {
        return TMM.executeSupporterCommand(source, () -> {
            PlayerShopComponent component = PlayerShopComponent.KEY.get(target);
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.money.get", target.getName().getString(), component.balance)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true
            );
        });
    }

    private static int executeSet(CommandSourceStack source, Entity target, int amount) {
        return TMM.executeSupporterCommand(source, () -> {
            PlayerShopComponent component = PlayerShopComponent.KEY.get(target);
            component.setBalance(amount);
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.money.set", target.getName().getString(), amount)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true
            );
        });
    }

    private static int executeAdd(CommandSourceStack source, Entity target, int amount) {
        return TMM.executeSupporterCommand(source, () -> {
            PlayerShopComponent component = PlayerShopComponent.KEY.get(target);
            int newBalance = component.balance + amount;
            component.addToBalance(amount);
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.money.add", amount, target.getName().getString(), newBalance)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true
            );
        });
    }
}
