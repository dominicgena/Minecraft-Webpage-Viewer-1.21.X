package com.genadom.genadom00001.mapcastcommands.load;

import com.genadom.genadom00001.mapcastcommands.ProfileSuggestionProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static net.minecraft.server.command.CommandManager.literal;

public class load {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");
    private static final Path FRAME_CONTAINER_DIRECTORY = Paths.get("config/mapcast/framecontainer");
    private static final Path NODE_EXECUTABLE = Paths.get("bin/node-v22.12.0-win-x64/node.exe");
    private static final Path SCRIPT_PATH = Paths.get("bin/nodescripts/framerenderer.js");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("load")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests(ProfileSuggestionProvider.SUGGEST_PROFILES)
                                .then(CommandManager.argument("originx", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("originy", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("originz", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("facing", StringArgumentType.string())
                                                                .suggests((context, builder) -> {
                                                                    builder.suggest("north");
                                                                    builder.suggest("south");
                                                                    builder.suggest("east");
                                                                    builder.suggest("west");
                                                                    builder.suggest("up");
                                                                    builder.suggest("down");
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(context -> {
                                                                    // Retrieve arguments
                                                                    String name = StringArgumentType.getString(context, "name");
                                                                    int originx = IntegerArgumentType.getInteger(context, "originx");
                                                                    int originy = IntegerArgumentType.getInteger(context, "originy");
                                                                    int originz = IntegerArgumentType.getInteger(context, "originz");
                                                                    String facing = StringArgumentType.getString(context, "facing");

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

                                                                    // Load and update JSON file
                                                                    if (Files.exists(profilePath)) {
                                                                        try {
                                                                            String content = new String(Files.readAllBytes(profilePath));
                                                                            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                                                                            boolean isActive = jsonObject.get("active").getAsBoolean();

                                                                            if (isActive) {
                                                                                context.getSource().sendFeedback(() -> Text.of("Can't load a profile that is already loaded!"), false);
                                                                                return 1;
                                                                            } else {
                                                                                // Update the JSON file with provided arguments
                                                                                jsonObject.add("active", new JsonPrimitive(true));
                                                                                jsonObject.add("originx", new JsonPrimitive(originx));
                                                                                jsonObject.add("originy", new JsonPrimitive(originy));
                                                                                jsonObject.add("originz", new JsonPrimitive(originz));
                                                                                jsonObject.add("facing", new JsonPrimitive(facing));
                                                                                Files.write(profilePath, jsonObject.toString().getBytes());

                                                                                // Update the empty profile directory with frames
                                                                                Path emptyProfilePath = FRAME_CONTAINER_DIRECTORY.resolve(emptyProfileHolder[0]);
                                                                                Path framesPath = emptyProfilePath.resolve("frames");
                                                                                Files.createDirectories(framesPath);

                                                                                // Execute the framerenderer.js script with target directory
                                                                                executeScreenshotScript(name, framesPath.toString());

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
                                        )
                                )
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

    private static void executeScreenshotScript(String profileName, String targetDirectory) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                NODE_EXECUTABLE.toString(),
                SCRIPT_PATH.toString(),
                profileName,
                targetDirectory
        );

        // Add unique identifiers to the environment or arguments for tracking
        processBuilder.environment().put("MAPCAST_PROFILE", profileName);

        processBuilder.redirectErrorStream(true); // Combine error and output streams
        try {
            Process process = processBuilder.start(); // Start the process
            System.out.println("Started framerenderer.js for profile: " + profileName);

            // Create a thread to handle and log process output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line); // Log each line of output
                    }
                } catch (IOException e) {
                    System.err.println("Error reading output from framerenderer.js: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Failed to execute framerenderer.js: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
