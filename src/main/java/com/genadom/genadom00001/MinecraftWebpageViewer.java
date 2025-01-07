package com.genadom.genadom00001;

import com.genadom.genadom00001.mapcastcommands.add.add;
import com.genadom.genadom00001.mapcastcommands.load.load;
import com.genadom.genadom00001.mapcastcommands.remove.remove;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;


public class MinecraftWebpageViewer implements ModInitializer {

	@Override
	public void onInitialize() {
		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			add.register(dispatcher);
			load.register(dispatcher);
			remove.register(dispatcher);
		});
	}
}