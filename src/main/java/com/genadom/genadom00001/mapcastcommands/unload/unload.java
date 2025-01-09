package com.genadom.genadom00001.mapcastcommands.unload;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static net.minecraft.server.command.CommandManager.literal;

public class unload {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");
    private static final Path FRAME_CONTAINER_DIRECTORY = Paths.get("config/mapcast/framecontainer");
    private static final Path SAVED_PROFILES_FILE = Paths.get("config/mapcast/savedprofiles.json");
    private static final Path NODE_EXECUTABLE = Paths.get("bin/node-v22.12.0-win-x64/node.exe");
    private static final Path SCRIPT_PATH = Paths.get("bin/nodescripts/framerenderer.js");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("unload")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    // Load saved profiles for suggestions
                                    try {
                                        if (Files.exists(SAVED_PROFILES_FILE)) {
                                            String content = new String(Files.readAllBytes(SAVED_PROFILES_FILE));
                                            JsonObject savedProfiles = JsonParser.parseString(content).getAsJsonObject();
                                            JsonArray profiles = savedProfiles.getAsJsonArray("profiles");
                                            profiles.forEach(profile -> builder.suggest(profile.getAsString()));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    builder.suggest("all"); // Add "all" as a suggestion
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String profileName = StringArgumentType.getString(context, "name");

                                    if (profileName.equals("all")) {
                                        // Unload all profiles and clean up frames folders
                                        try {
                                            Files.list(PROFILES_DIRECTORY)
                                                    .filter(Files::isRegularFile)
                                                    .filter(file -> file.toString().endsWith(".json"))
                                                    .forEach(file -> unloadProfile(file, context.getSource()));

                                            deleteAllFramesFolders(context.getSource());
                                            context.getSource().sendFeedback(() -> Text.of("All profiles have been unloaded and frames folders cleaned up."), false);
                                        } catch (IOException e) {
                                            context.getSource().sendFeedback(() -> Text.of("Failed to unload all profiles."), false);
                                            e.printStackTrace();
                                            return 1;
                                        }
                                    } else {
                                        // Unload a specific profile
                                        Path profilePath = PROFILES_DIRECTORY.resolve(profileName + ".json").normalize();
                                        if (!profilePath.startsWith(PROFILES_DIRECTORY)) {
                                            context.getSource().sendFeedback(() -> Text.of("Invalid file path!"), false);
                                            return 1;
                                        }

                                        if (!Files.exists(profilePath)) {
                                            context.getSource().sendFeedback(() -> Text.of("Profile does not exist!"), false);
                                            return 1;
                                        }

                                        unloadProfile(profilePath, context.getSource());
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    public static void unloadProfile(Path profilePath, ServerCommandSource source) {
        try {
            String content = new String(Files.readAllBytes(profilePath));
            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
            boolean isActive = jsonObject.get("active").getAsBoolean();

            if (!isActive) {
                source.sendFeedback(() -> Text.of("Profile " + profilePath.getFileName() + " is already inactive."), false);
                return;
            }

            // Set "active" to false
            jsonObject.addProperty("active", false);
            Files.write(profilePath, jsonObject.toString().getBytes());

            // Stop the screenshot process
            String profileName = profilePath.getFileName().toString().replace(".json", "");
            executeStopScript(profileName);

            source.sendFeedback(() -> Text.of("Profile " + profileName + " has been unloaded."), false);
        } catch (IOException e) {
            source.sendFeedback(() -> Text.of("Failed to unload profile: " + profilePath.getFileName()), false);
            e.printStackTrace();
        }
    }

    private static void deleteAllFramesFolders(ServerCommandSource source) {
        try {
            Files.walkFileTree(FRAME_CONTAINER_DIRECTORY, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName().toString().equals("frames")) {
                        try {
                            deleteDirectoryRecursively(dir);
                            source.sendFeedback(() -> Text.of("Deleted frames folder: " + dir), false);
                        } catch (IOException e) {
                            source.sendFeedback(() -> Text.of("Failed to delete frames folder: " + dir), false);
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            source.sendFeedback(() -> Text.of("Failed to clean up frames folders."), false);
            e.printStackTrace();
        }
    }

    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void executeStopScript(String profileName) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                NODE_EXECUTABLE.toString(),
                SCRIPT_PATH.toString(),
                profileName,
                "stop" // The "stop" argument for the framerenderer.js script
        );

        processBuilder.redirectErrorStream(true); // Combine error and output streams
        try {
            Process process = processBuilder.start();
            System.out.println("Executed stop command for profile: " + profileName);

            // Log output of the process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to execute stop command: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
