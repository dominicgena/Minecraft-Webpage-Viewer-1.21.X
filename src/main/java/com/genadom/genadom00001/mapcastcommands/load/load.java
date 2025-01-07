package com.genadom.genadom00001.mapcastcommands.load;

import com.genadom.genadom00001.mapcastcommands.ProfileSuggestionProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.minecraft.server.command.CommandManager.literal;

public class load {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("load")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests(ProfileSuggestionProvider.SUGGEST_PROFILES)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    // Validate and sanitize the input path
                                    Path path = PROFILES_DIRECTORY.resolve(name + ".json").normalize();
                                    if (!path.startsWith(PROFILES_DIRECTORY)) {
                                        context.getSource().sendFeedback(() -> Text.of("Invalid file path!"), false);
                                        return 1;
                                    }

                                    // Load JSON file
                                    if (Files.exists(path)) {
                                        context.getSource().sendFeedback(() -> Text.of("JSON file loaded at " + path.toString()), false);
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.of("Failed to load mapcast!  (check your spelling)"), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}