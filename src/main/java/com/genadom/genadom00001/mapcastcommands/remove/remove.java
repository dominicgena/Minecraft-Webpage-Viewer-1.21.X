package com.genadom.genadom00001.mapcastcommands.remove;

import com.genadom.genadom00001.mapcastcommands.ProfileSuggestionProvider;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;

public class remove {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    ProfileSuggestionProvider.SUGGEST_PROFILES.getSuggestions(context, builder);
                                    builder.suggest("all");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    if ("all".equals(name)) {
                                        context.getSource().sendFeedback(() -> Text.of("Use -force to confirm removing all profiles."), false);
                                    } else {
                                        // Validate and sanitize the input path
                                        Path path = PROFILES_DIRECTORY.resolve(name + ".json").normalize();
                                        if (!path.startsWith(PROFILES_DIRECTORY)) {
                                            context.getSource().sendFeedback(() -> Text.of("Invalid file path!"), false);
                                            return 1;
                                        }

                                        try {
                                            if (Files.exists(path)) {
                                                Files.delete(path);
                                                removeFromSavedProfiles(name);
                                                context.getSource().sendFeedback(() -> Text.of("JSON file deleted: " + path.toString()), false);
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.of("Failed to delete mapcast: File not found!  (check your spelling)"), false);
                                            }
                                        } catch (IOException e) {
                                            context.getSource().sendFeedback(() -> Text.of("Error deleting mapcast: " + e.getMessage()), false);
                                            e.printStackTrace();
                                        }
                                    }
                                    return 1;
                                })
                                .then(literal("-force")
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");

                                            if ("all".equals(name)) {
                                                removeAllProfiles(context.getSource());
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

    private static void removeFromSavedProfiles(String name) {
        try {
            // Define the path to savedprofiles.json
            Path savedProfilesPath = Paths.get("config/mapcast/savedprofiles.json");

            // Read the existing JSON file
            String jsonContent = new String(Files.readAllBytes(savedProfilesPath));
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            // Get the profiles array and remove the specified profile name
            JsonArray profilesArray = jsonObject.getAsJsonArray("profiles");
            for (int i = 0; i < profilesArray.size(); i++) {
                if (profilesArray.get(i).getAsString().equals(name)) {
                    profilesArray.remove(i);
                    break;
                }
            }

            // Update the JSON object
            jsonObject.add("profiles", profilesArray);

            // Write the updated JSON back to the file
            Files.write(savedProfilesPath, new Gson().toJson(jsonObject).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeAllProfiles(ServerCommandSource source) {
        try {
            // Define the path to savedprofiles.json
            Path savedProfilesPath = Paths.get("config/mapcast/savedprofiles.json");

            // Delete all JSON files in the profiles directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(PROFILES_DIRECTORY, "*.json")) {
                for (Path entry : stream) {
                    Files.delete(entry);
                }
            }

            // Clear the profiles array in savedprofiles.json
            String jsonContent = new String(Files.readAllBytes(savedProfilesPath));
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
            jsonObject.add("profiles", new JsonArray());

            // Write the updated JSON back to the file
            Files.write(savedProfilesPath, new Gson().toJson(jsonObject).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

            source.sendFeedback(() -> Text.of("All mapcast profiles have been removed."), false);
        } catch (IOException e) {
            source.sendFeedback(() -> Text.of("Error removing all mapcast profiles: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }
}