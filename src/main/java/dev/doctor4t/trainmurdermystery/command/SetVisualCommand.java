package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.TrainWorldComponent;
import dev.doctor4t.trainmurdermystery.command.argument.TimeOfDayArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetVisualCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tmm:setVisual")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("snow")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> executeSnow(context.getSource(), BoolArgumentType.getBool(context, "enabled")))))
                .then(Commands.literal("fog")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> executeFog(context.getSource(), BoolArgumentType.getBool(context, "enabled")))))
                .then(Commands.literal("hud")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> executeHud(context.getSource(), BoolArgumentType.getBool(context, "enabled")))))
                .then(Commands.literal("trainSpeed")
                        .then(Commands.argument("speed", IntegerArgumentType.integer(0))
                                .executes(context -> executeSpeed(context.getSource(), IntegerArgumentType.getInteger(context, "speed")))))
                .then(Commands.literal("time")
                        .then(Commands.argument("timeOfDay", TimeOfDayArgumentType.timeofday())
                                .executes(context -> executeTimeOfDay(context.getSource(), TimeOfDayArgumentType.getTimeofday(context, "timeOfDay")))))
                .then(Commands.literal("reset")
                        .executes(context -> reset(context.getSource())))
        );
    }

    private static int reset(CommandSourceStack source) {
        TrainWorldComponent trainWorldComponent = TrainWorldComponent.KEY.get(source.getLevel());
        trainWorldComponent.reset();
        source.sendSuccess(
            () -> Component.translatable("commands.tmm.setvisual.reset")
                .withStyle(style -> style.withColor(0x00FF00)),
            true
        );
        return 1;
    }

    private static int executeSnow(CommandSourceStack source, boolean enabled) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    TrainWorldComponent.KEY.get(source.getLevel()).setSnow(enabled);
                    source.sendSuccess(
                        () -> Component.translatable("commands.tmm.setvisual.snow", enabled)
                            .withStyle(style -> style.withColor(0x00FF00)),
                        true
                    );
                }
        );
    }
    
    private static int executeFog(CommandSourceStack source, boolean enabled) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    TrainWorldComponent.KEY.get(source.getLevel()).setFog(enabled);
                    source.sendSuccess(
                        () -> Component.translatable("commands.tmm.setvisual.fog", enabled)
                            .withStyle(style -> style.withColor(0x00FF00)),
                        true
                    );
                }
        );
    }
    
    private static int executeHud(CommandSourceStack source, boolean enabled) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    TrainWorldComponent.KEY.get(source.getLevel()).setHud(enabled);
                    source.sendSuccess(
                        () -> Component.translatable("commands.tmm.setvisual.hud", enabled)
                            .withStyle(style -> style.withColor(0x00FF00)),
                        true
                    );
                }
        );
    }
    
    private static int executeSpeed(CommandSourceStack source, int speed) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    TrainWorldComponent.KEY.get(source.getLevel()).setSpeed(speed);
                    source.sendSuccess(
                        () -> Component.translatable("commands.tmm.setvisual.trainspeed", speed)
                            .withStyle(style -> style.withColor(0x00FF00)),
                        true
                    );
                }
        );
    }
    
    private static int executeTimeOfDay(CommandSourceStack source, TrainWorldComponent.TimeOfDay timeOfDay) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    TrainWorldComponent.KEY.get(source.getLevel()).setTimeOfDay(timeOfDay);
                    source.sendSuccess(
                        () -> Component.translatable("commands.tmm.setvisual.time", timeOfDay)
                            .withStyle(style -> style.withColor(0x00FF00)),
                        true
                    );
                }
        );
    }
}
