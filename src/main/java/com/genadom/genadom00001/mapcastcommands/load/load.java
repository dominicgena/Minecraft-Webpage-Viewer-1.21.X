package com.genadom.genadom00001.mapcastcommands.load;

import com.genadom.genadom00001.mapcastcommands.ProfileSuggestionProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
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
                                        try {
                                            String content = new String(Files.readAllBytes(path));
                                            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                                            boolean isActive = jsonObject.get("active").getAsBoolean();

                                            if (isActive) {
                                                context.getSource().sendFeedback(() -> Text.of("Can't load a profile that is already loaded!"), false);
                                            } else {
                                                jsonObject.add("active", new JsonPrimitive(true));
                                                Files.write(path, jsonObject.toString().getBytes());
                                                context.getSource().sendFeedback(() -> Text.of("Profile is now active."), false);
                                            }
                                        } catch (IOException e) {
                                            context.getSource().sendFeedback(() -> Text.of("Failed to read the JSON file."), false);
                                        }
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.of("Failed to load mapcast! (check your spelling)"), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}