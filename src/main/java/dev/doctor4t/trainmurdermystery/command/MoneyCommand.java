package dev.doctor4t.trainmurdermystery.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class MoneyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:money")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> executeSet(context.getSource(),
                                        ImmutableList.of(context.getSource().getEntityOrException()),
                                        IntegerArgumentType.getInteger(context, "amount")))
                                .then(
                                        Commands.argument("targets", EntityArgument.entities())
                                                .executes(context -> executeSet(context.getSource(),
                                                        EntityArgument.getEntities(context, "targets"),
                                                        IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("add").then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> executeSet(context.getSource(),
                                        ImmutableList.of(context.getSource().getEntityOrException()),
                                        IntegerArgumentType.getInteger(context, "amount")))
                                .then(
                                        Commands.argument("targets", EntityArgument.entities())
                                                .executes(context -> executeAdd(context.getSource(),
                                                        EntityArgument.getEntities(context, "targets"),
                                                        IntegerArgumentType.getInteger(context, "amount"))))))
                        .then(Commands.literal("get").executes(context -> executeGet(context.getSource(),
                                ImmutableList.of(context.getSource().getEntityOrException())))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> executeGet(context.getSource(),
                                                EntityArgument.getEntities(context, "targets"))))));
    }

    private static int executeSet(CommandSourceStack source, Collection<? extends Entity> targets, int amount) {
        int total = 0;

        for (Entity target : targets) {
            PlayerShopComponent.KEY.get(target).setBalance(amount);
            total += PlayerShopComponent.KEY.get(target).balance;
        }

        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            source.sendSuccess(
                    () -> Component
                            .translatable("commands.tmm.setmoney", target.getName().getString(), amount)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true);
        } else {
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.setmoney.multiple", targets.size(), amount)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true);
        }
        return total;
    }

    private static int executeAdd(CommandSourceStack source, Collection<? extends Entity> targets, int amount) {
        int total = 0;
        for (Entity target : targets) {
            PlayerShopComponent.KEY.get(target).addToBalance(amount);
            total += PlayerShopComponent.KEY.get(target).balance;
        }

        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            source.sendSuccess(
                    () -> Component
                            .translatable("commands.tmm.setmoney", target.getName().getString(), amount)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true);
        } else {
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.setmoney.multiple", targets.size(), amount)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true);
        }
        return total;
    }

    private static int executeGet(CommandSourceStack source, Collection<? extends Entity> targets) {
        final int total = targets.stream().mapToInt(target -> {
            var ba = PlayerShopComponent.KEY.maybeGet(target).orElse(null);
            if (ba != null) {
                return ba.balance;
            }
            return 0;
        }).sum();
        source.sendSuccess(
                () -> Component
                        .translatable("commands.tmm.getmoney", total)
                        .withStyle(style -> style.withColor(0x00FF00)),
                true);
        return total;
    }

}
