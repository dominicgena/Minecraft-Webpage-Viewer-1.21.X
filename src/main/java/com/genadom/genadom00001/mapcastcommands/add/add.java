package com.genadom.genadom00001.mapcastcommands.add;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import static net.minecraft.server.command.CommandManager.literal;

public class add {

    private static final Path PROFILES_DIRECTORY = Paths.get("config/mapcast/savedprofiles");
    private static final Path SAVED_PROFILES_FILE = Paths.get("config/mapcast/savedprofiles.json");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("add")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .then(CommandManager.argument("aspectRatio", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("targetYLength", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("targetXLength", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("imageLink", StringArgumentType.string())
                                                                .executes(context -> {
                                                                    String name = StringArgumentType.getString(context, "name");
                                                                    double aspectRatio = DoubleArgumentType.getDouble(context, "aspectRatio");
                                                                    int targetYLength = IntegerArgumentType.getInteger(context, "targetYLength");
                                                                    int targetXLength = IntegerArgumentType.getInteger(context, "targetXLength");
                                                                    String imageLink = StringArgumentType.getString(context, "imageLink");

                                                                    // Create JSON file with "active" flag set to false
                                                                    Path path = PROFILES_DIRECTORY.resolve(name + ".json").normalize();
                                                                    if (!path.startsWith(PROFILES_DIRECTORY)) {
                                                                        context.getSource().sendFeedback(() -> Text.of("Invalid file path!"), false);
                                                                        return 1;
                                                                    }

                                                                    try {
                                                                        createJsonFile(path, name, aspectRatio, targetYLength, targetXLength, imageLink);
                                                                        updateSavedProfiles(name);
                                                                        context.getSource().sendFeedback(() -> Text.of("Profile added: " + name), false);
                                                                    } catch (IOException e) {
                                                                        context.getSource().sendFeedback(() -> Text.of("Error creating profile: " + e.getMessage()), false);
                                                                        e.printStackTrace();
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

    private static void createJsonFile(Path path, String name, double aspectRatio, int targetYLength, int targetXLength, String imageLink) throws IOException {
        if (Files.exists(path)) {
            throw new IOException("Profile already exists!");
        }

        JsonObject profile = new JsonObject();
        profile.addProperty("name", name);
        profile.addProperty("aspectRatio", aspectRatio);
        profile.addProperty("targetYLength", targetYLength);
        profile.addProperty("targetXLength", targetXLength);
        profile.addProperty("imageLink", imageLink);
        profile.addProperty("active", false);

        Files.createDirectories(path.getParent());
        Files.write(path, profile.toString().getBytes(), StandardOpenOption.CREATE_NEW);
    }

    private static void updateSavedProfiles(String name) throws IOException {
        JsonObject savedProfiles;
        if (Files.exists(SAVED_PROFILES_FILE)) {
            String content = new String(Files.readAllBytes(SAVED_PROFILES_FILE));
            savedProfiles = JsonParser.parseString(content).getAsJsonObject();
        } else {
            savedProfiles = new JsonObject();
            savedProfiles.add("profiles", new JsonArray());
        }

        JsonArray profiles = savedProfiles.getAsJsonArray("profiles");
        profiles.add(name);

        Files.write(SAVED_PROFILES_FILE, savedProfiles.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}