package com.stereowalker.tiered;

import com.stereowalker.tiered.config.Config;
import com.stereowalker.unionlib.client.gui.screens.config.ConfigScreen;
import com.stereowalker.unionlib.mod.ClientSegment;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class ReforgedClientSegment extends ClientSegment {

	@Override
	public ResourceLocation getModIcon() {
		return VersionHelper.toLoc("tiered", "textures/icon.png");
	}
	
	@Override
	public Screen getConfigScreen(Minecraft mc, Screen previousScreen) {
		return new ConfigScreen(previousScreen, Config.class);
	}

}
