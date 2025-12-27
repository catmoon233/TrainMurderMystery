package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SetBackfireChanceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:setBackfireChance")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("chance", FloatArgumentType.floatArg(0f, 1f))
                                        .executes(context -> execute(context.getSource(), FloatArgumentType.getFloat(context, "chance")))
                        )
        );
    }

    private static int execute(CommandSourceStack source, float chance) {
        return TMM.executeSupporterCommand(source,
                () -> GameWorldComponent.KEY.get(source.getLevel()).setBackfireChance(chance)
        );
    }

}
