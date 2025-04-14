package com.stereowalker.tiered.config;

import com.stereowalker.unionlib.config.UnionConfig;
import com.stereowalker.unionlib.config.UnionConfig.Comment;
import com.stereowalker.unionlib.config.UnionConfig.Entry;

@UnionConfig(name = "reforged", translatableName = "gui.reforged.config")
public class Config {
	@UnionConfig.Entry(name = "Can Broken Items Be Reforged")
	public static boolean canReforgeBroken = false;
	@Entry(name = "Can Crafted Items Receive Tiers")
	@Comment(comment = {"Disabling this will force all crafted items to recieve tiers without attributes",
			"That would be the common tier for most items"})
	public static boolean canCraftedReceiveTier = true;
}
