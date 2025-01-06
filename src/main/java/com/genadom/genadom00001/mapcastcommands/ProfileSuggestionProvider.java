package com.genadom.genadom00001.mapcastcommands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.CommandSource.suggestMatching;

public class ProfileSuggestionProvider {

    public static final SuggestionProvider<ServerCommandSource> SUGGEST_PROFILES = (context, builder) -> {
        try {
            // Define the path to savedprofiles.json
            Path savedProfilesPath = Paths.get("config/mapcast/savedprofiles.json");

            // Read the existing JSON file
            String jsonContent = new String(Files.readAllBytes(savedProfilesPath));
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            // Get the profiles array
            JsonArray profilesArray = jsonObject.getAsJsonArray("profiles");

            // Collect profile names
            List<String> profileNames = new ArrayList<>();
            profilesArray.forEach(profile -> profileNames.add(profile.getAsString()));

            // Suggest profile names
            return suggestMatching(profileNames, builder);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(builder.build());
        }
    };
}