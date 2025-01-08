package com.genadom.genadom00001.mapcastcommands.remove;

import com.genadom.genadom00001.mapcastcommands.unload.unload;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static net.minecraft.server.command.CommandManager.literal;

public class remove {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");
    private static final Path FRAME_CONTAINER_DIRECTORY = Paths.get("config/mapcast/framecontainer");
    private static final Path SAVED_PROFILES_FILE = Paths.get("config/mapcast/savedprofiles.json");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
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
                                    builder.suggest("all");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    if ("all".equals(name)) {
                                        context.getSource().sendFeedback(() -> Text.of("Use -force to confirm removing all profiles."), false);
                                    } else {
                                        Path profilePath = PROFILES_DIRECTORY.resolve(name + ".json").normalize();
                                        if (!profilePath.startsWith(PROFILES_DIRECTORY)) {
                                            context.getSource().sendFeedback(() -> Text.of("Invalid file path!"), false);
                                            return 1;
                                        }

                                        if (Files.exists(profilePath)) {
                                            unloadProfileAndRemove(name, profilePath, context.getSource());
                                        } else {
                                            context.getSource().sendFeedback(() -> Text.of("Profile does not exist!"), false);
                                        }
                                    }
                                    return 1;
                                })
                                .then(literal("-force")
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");

                                            if ("all".equals(name)) {
                                                unloadAndRemoveAllProfiles(context.getSource());
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.of("The -force flag is only required for removing all profiles."), false);
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private static void unloadProfileAndRemove(String name, Path profilePath, ServerCommandSource source) {
        try {
            unload.unloadProfile(profilePath, source);
            waitForFramesCleanup(name);
            removeProfile(profilePath, name, source);
        } catch (Exception e) {
            source.sendFeedback(() -> Text.of("Error unloading and removing profile: " + name), false);
            e.printStackTrace();
        }
    }

    private static void waitForFramesCleanup(String name) {
        Path profileDir = FRAME_CONTAINER_DIRECTORY.resolve("profile" + name);
        Path framesDir = profileDir.resolve("frames");

        int retries = 10; // Retry for 10 seconds
        while (Files.exists(framesDir) && retries > 0) {
            try {
                Thread.sleep(1000); // Wait for 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            retries--;
        }

        if (Files.exists(framesDir)) {
            try {
                Files.walk(framesDir)
                        .sorted((a, b) -> b.compareTo(a)) // Delete files first, then directories
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("Error deleting path during cleanup: " + path);
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                System.err.println("Failed to clean up frames directory: " + framesDir);
                e.printStackTrace();
            }
        }
    }

    private static void removeProfile(Path profilePath, String name, ServerCommandSource source) {
        try {
            Files.delete(profilePath);
            removeFromSavedProfiles(name);
            source.sendFeedback(() -> Text.of("Removed profile and frames: " + name), false);
        } catch (IOException e) {
            source.sendFeedback(() -> Text.of("Error removing profile: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }

    private static void removeFromSavedProfiles(String name) {
        try {
            if (Files.exists(SAVED_PROFILES_FILE)) {
                String jsonContent = new String(Files.readAllBytes(SAVED_PROFILES_FILE));
                JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

                JsonArray profilesArray = jsonObject.getAsJsonArray("profiles");
                for (int i = 0; i < profilesArray.size(); i++) {
                    if (profilesArray.get(i).getAsString().equals(name)) {
                        profilesArray.remove(i);
                        break;
                    }
                }

                jsonObject.add("profiles", profilesArray);
                Files.write(SAVED_PROFILES_FILE, new Gson().toJson(jsonObject).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void unloadAndRemoveAllProfiles(ServerCommandSource source) {
        try {
            Files.list(PROFILES_DIRECTORY)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json"))
                    .forEach(file -> {
                        String name = file.getFileName().toString().replace(".json", "");
                        unloadProfileAndRemove(name, file, source);
                    });
            source.sendFeedback(() -> Text.of("All profiles and frames have been unloaded and removed."), false);
        } catch (IOException e) {
            source.sendFeedback(() -> Text.of("Error unloading and removing all profiles: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }
}
