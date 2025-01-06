package com.genadom.genadom00001.mapcastcommands.load;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.minecraft.server.command.CommandManager.literal;

public class load {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mapcast")
                .then(literal("load")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    // Load JSON file
                                    Path path = Paths.get("config/mapcast/savedprofiles", name + ".json");
                                    if (Files.exists(path)) {
                                        context.getSource().sendFeedback(() -> Text.of("JSON file loaded at " + path.toString() + " with name \"" + name + ".json\""), false);
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.of("Failed to load mapcast!"), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}