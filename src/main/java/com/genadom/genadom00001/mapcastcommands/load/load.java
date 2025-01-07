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
import java.util.stream.Stream;

import static net.minecraft.server.command.CommandManager.literal;

public class load {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");
    private static final Path FRAME_CONTAINER_DIRECTORY = Paths.get("config/mapcast/framecontainer");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("load")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests(ProfileSuggestionProvider.SUGGEST_PROFILES)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    // Validate and sanitize the input path
                                    Path profilePath = PROFILES_DIRECTORY.resolve(name + ".json").normalize();
                                    if (!profilePath.startsWith(PROFILES_DIRECTORY)) {
                                        context.getSource().sendFeedback(() -> Text.of("Invalid file path!"), false);
                                        return 1;
                                    }

                                    // Ensure profile directories exist
                                    initializeProfileDirectories();

                                    // Find the first empty profile directory
                                    final String[] emptyProfileHolder = {null};
                                    try {
                                        emptyProfileHolder[0] = findFirstEmptyProfile();
                                        if (emptyProfileHolder[0] == null) {
                                            context.getSource().sendFeedback(() -> Text.of("No empty profile directories available."), false);
                                            return 1;
                                        }
                                    } catch (IOException e) {
                                        context.getSource().sendFeedback(() -> Text.of("Failed to find an empty profile directory."), false);
                                        e.printStackTrace();
                                        return 1;
                                    }

                                    // Load JSON file
                                    if (Files.exists(profilePath)) {
                                        try {
                                            String content = new String(Files.readAllBytes(profilePath));
                                            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                                            boolean isActive = jsonObject.get("active").getAsBoolean();

                                            if (isActive) {
                                                context.getSource().sendFeedback(() -> Text.of("Can't load a profile that is already loaded!"), false);
                                                return 1;
                                            } else {
                                                jsonObject.add("active", new JsonPrimitive(true));
                                                Files.write(profilePath, jsonObject.toString().getBytes());

                                                // Update the empty profile directory with frames
                                                Path emptyProfilePath = FRAME_CONTAINER_DIRECTORY.resolve(emptyProfileHolder[0]);
                                                Path framesPath = emptyProfilePath.resolve("frames");
                                                Files.createDirectories(framesPath);

                                                context.getSource().sendFeedback(() -> Text.of("Profile " + name + " is now active in directory: " + emptyProfileHolder[0]), false);
                                            }
                                        } catch (IOException e) {
                                            context.getSource().sendFeedback(() -> Text.of("Failed to read or update the JSON file."), false);
                                            e.printStackTrace();
                                            return 1;
                                        }
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.of("Profile does not exist! Check your spelling."), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void initializeProfileDirectories() {
        try {
            if (!Files.exists(FRAME_CONTAINER_DIRECTORY)) {
                Files.createDirectories(FRAME_CONTAINER_DIRECTORY);
            }

            for (int i = 0; i < 10; i++) {
                Path profilePath = FRAME_CONTAINER_DIRECTORY.resolve("profile" + i);
                if (!Files.exists(profilePath)) {
                    Files.createDirectories(profilePath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize profile directories.", e);
        }
    }

    private static String findFirstEmptyProfile() throws IOException {
        for (int i = 0; i < 10; i++) {
            Path profilePath = FRAME_CONTAINER_DIRECTORY.resolve("profile" + i);
            if (Files.exists(profilePath) && Files.isDirectory(profilePath)) {
                try (Stream<Path> files = Files.list(profilePath)) {
                    if (files.findAny().isEmpty()) {
                        return "profile" + i;
                    }
                }
            }
        }
        return null;
    }
}
