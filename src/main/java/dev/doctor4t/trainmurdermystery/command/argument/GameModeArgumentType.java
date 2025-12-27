package dev.doctor4t.trainmurdermystery.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.api.TMMGameModes;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GameModeArgumentType implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Stream.of(TMMGameModes.MURDER, TMMGameModes.DISCOVERY, TMMGameModes.LOOSE_ENDS)
            .map(key -> key.identifier.toString())
            .collect(Collectors.toList());
    private static final DynamicCommandExceptionType INVALID_GAME_MODE_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("argument.game_mode.invalid", id)
    );

    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
        return ResourceLocation.read(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider
                ? SharedSuggestionProvider.suggestResource(TMMGameModes.GAME_MODES.keySet().stream().toList(), builder)
                : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static GameModeArgumentType gameMode() {
        return new GameModeArgumentType();
    }

    public static GameMode getGameModeArgument(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        ResourceLocation identifier = context.getArgument(name, ResourceLocation.class);
        GameMode gameMode = TMMGameModes.GAME_MODES.get(identifier);
        if (gameMode == null) {
            throw INVALID_GAME_MODE_EXCEPTION.create(identifier);
        } else {
            return gameMode;
        }
    }
}
