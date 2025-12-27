package dev.doctor4t.trainmurdermystery.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class SetMoneyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:setMoney")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> execute(context.getSource(), ImmutableList.of(context.getSource().getEntityOrException()), IntegerArgumentType.getInteger(context, "amount")))
                                        .then(
                                                Commands.argument("targets", EntityArgument.entities())
                                                        .executes(context -> execute(context.getSource(), EntityArgument.getEntities(context, "targets"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                        )
        );
    }

    private static int execute(CommandSourceStack source, Collection<? extends Entity> targets, int amount) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    for (Entity target : targets) {
                        PlayerShopComponent.KEY.get(target).setBalance(amount);
                    }
                    
                    if (targets.size() == 1) {
                        Entity target = targets.iterator().next();
                        source.sendSuccess(
                            () -> Component.translatable("commands.tmm.setmoney", target.getName().getString(), amount)
                                .withStyle(style -> style.withColor(0x00FF00)),
                            true
                        );
                    } else {
                        source.sendSuccess(
                            () -> Component.translatable("commands.tmm.setmoney.multiple", targets.size(), amount)
                                .withStyle(style -> style.withColor(0x00FF00)),
                            true
                        );
                    }
                }
        );
    }

}
