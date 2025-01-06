package com.genadom.genadom00001.mapcastcommands.remove;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;

public class remove {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    // Delete JSON file
                                    Path path = Paths.get("config/mapcast/savedprofiles", name + ".json");
                                    try {
                                        if (Files.exists(path)) {
                                            Files.delete(path);
                                            context.getSource().sendFeedback(() -> Text.of("JSON file deleted: " + path.toString()), false);
                                        } else {
                                            context.getSource().sendFeedback(() -> Text.of("Failed to delete mapcast: File not found!"), false);
                                        }
                                    } catch (IOException e) {
                                        context.getSource().sendFeedback(() -> Text.of("Error deleting mapcast: " + e.getMessage()), false);
                                        e.printStackTrace();
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}