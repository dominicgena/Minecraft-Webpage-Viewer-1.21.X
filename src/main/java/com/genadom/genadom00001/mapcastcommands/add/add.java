package com.genadom.genadom00001.mapcastcommands.add;

import com.google.gson.JsonArray;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class add {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("add")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .then(CommandManager.argument("aspectRatio", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("targetXLength", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("targetYLength", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            String name = StringArgumentType.getString(context, "name");
                                                            double aspectRatio = DoubleArgumentType.getDouble(context, "aspectRatio");
                                                            int targetXLength = IntegerArgumentType.getInteger(context, "targetXLength");
                                                            int targetYLength = IntegerArgumentType.getInteger(context, "targetYLength");

                                                            try {
                                                                // Convert aspect ratio to BigDecimal
                                                                BigDecimal aspectRatioDecimal = BigDecimal.valueOf(aspectRatio);

                                                                // Check for duplicate profile name
                                                                if (profileExists(name)) {
                                                                    context.getSource().sendFeedback(() -> Text.of("That map profile already exists!"), false);
                                                                } else {
                                                                    // Create JSON file
                                                                    createJsonFile(name, aspectRatioDecimal, targetXLength, targetYLength);
                                                                    context.getSource().sendFeedback(() -> Text.of("Mapcast added successfully!"), false);
                                                                }
                                                            } catch (Exception e) {
                                                                context.getSource().sendFeedback(() -> Text.of("Error adding mapcast: " + e.getMessage()), false);
                                                                e.printStackTrace();
                                                            }

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static boolean profileExists(String name) {
        try {
            // Define the path to savedprofiles.json
            Path savedProfilesPath = Paths.get("config/mapcast/savedprofiles.json");

            // Read the existing JSON file
            String jsonContent = new String(Files.readAllBytes(savedProfilesPath));
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            // Get the profiles array and check if the profile name exists
            JsonArray profilesArray = jsonObject.getAsJsonArray("profiles");
            for (int i = 0; i < profilesArray.size(); i++) {
                if (profilesArray.get(i).getAsString().equals(name)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void createJsonFile(String name, BigDecimal aspectRatio, int targetXLength, int targetYLength) {
        try {
            // Create directories if they do not exist
            Path directory = Paths.get("config/mapcast/savedprofiles");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Generate a unique file name based on the map name
            Path path = directory.resolve(name + ".json");
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("aspectRatio", aspectRatio);
            data.put("targetXLength", targetXLength);
            data.put("targetYLength", targetYLength);

            // Convert map to JSON string
            String jsonString = new Gson().toJson(data);

            // Write JSON string to file
            Files.write(path, jsonString.getBytes());

            // Append the name to savedprofiles.json
            appendToSavedProfiles(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void appendToSavedProfiles(String name) {
        try {
            // Define the path to savedprofiles.json
            Path savedProfilesPath = Paths.get("config/mapcast/savedprofiles.json");

            // Read the existing JSON file
            String jsonContent = new String(Files.readAllBytes(savedProfilesPath));
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            // Get the profiles array and add the new profile name
            JsonArray profilesArray = jsonObject.getAsJsonArray("profiles");
            profilesArray.add(name);

            // Update the JSON object
            jsonObject.add("profiles", profilesArray);

            // Write the updated JSON back to the file
            Files.write(savedProfilesPath, new Gson().toJson(jsonObject).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}