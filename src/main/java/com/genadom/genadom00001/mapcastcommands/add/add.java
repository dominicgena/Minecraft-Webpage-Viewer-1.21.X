package com.genadom.genadom00001.mapcastcommands.add;

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
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

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

                                                                // Create JSON file
                                                                createJsonFile(name, aspectRatioDecimal, targetXLength, targetYLength);
                                                                context.getSource().sendFeedback(() -> Text.of("Mapcast added successfully!"), false);
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
            String jsonString = new com.google.gson.Gson().toJson(data);

            // Write JSON string to file
            Files.write(path, jsonString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}