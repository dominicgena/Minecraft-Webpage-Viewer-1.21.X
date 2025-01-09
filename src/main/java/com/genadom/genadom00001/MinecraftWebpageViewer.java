package com.genadom.genadom00001;

import com.genadom.genadom00001.mapcastcommands.add.add;
import com.genadom.genadom00001.mapcastcommands.load.load;
import com.genadom.genadom00001.mapcastcommands.unload.unload;
import com.genadom.genadom00001.mapcastcommands.remove.remove;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.io.IOException;
import java.nio.file.*;

public class MinecraftWebpageViewer implements ModInitializer {

	private static final Path MINECRAFT_DIR = Paths.get("."); // Assumes the working directory is .minecraft
	private static final Path BIN_DIR = MINECRAFT_DIR.resolve("bin");

	@Override
	public void onInitialize() {
		System.out.println("Initializing Minecraft Webpage Viewer Mod...");

		// Ensure the 'bin' directory and Node.js binaries are set up
		try {
			setupBinDirectory();
		} catch (IOException e) {
			System.err.println("Failed to initialize bin directory: " + e.getMessage());
			e.printStackTrace();
		}

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			add.register(dispatcher);
			load.register(dispatcher);
			remove.register(dispatcher);
			unload.register(dispatcher);
		});
	}

	private void setupBinDirectory() throws IOException {
		// Ensure the 'bin' directory exists
		if (!Files.exists(BIN_DIR)) {
			Files.createDirectories(BIN_DIR);
			System.out.println("Created bin directory at: " + BIN_DIR.toAbsolutePath());
		}

		// Define binaries to copy
		String[] binaries = {"node.exe", "npm.cmd"}; // Add more if needed
		for (String fileName : binaries) {
			copyBinary(fileName);
		}
	}

	private void copyBinary(String fileName) throws IOException {
		Path targetFile = BIN_DIR.resolve(fileName);
		if (!Files.exists(targetFile)) {
			try {
				Files.copy(getClass().getResourceAsStream("/bin/" + fileName), targetFile, StandardCopyOption.REPLACE_EXISTING);
				System.out.println(fileName + " copied to bin directory: " + targetFile.toAbsolutePath());
			} catch (NullPointerException e) {
				System.err.println("Failed to locate " + fileName + " in the resources folder.");
			}
		} else {
			System.out.println(fileName + " already exists in bin directory: " + targetFile.toAbsolutePath());
		}
	}
}
