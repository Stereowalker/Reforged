package com.stereowalker.reforged.config;

import com.stereowalker.unionlib.config.UnionConfig;
import com.stereowalker.unionlib.config.UnionConfig.Comment;
import com.stereowalker.unionlib.config.UnionConfig.Entry;

@UnionConfig(name = "reforged", translatableName = "gui.reforged.config")
public class Config {
	@UnionConfig.Entry(name = "Can Broken Items Be Reforged", translatable = "config.reforged.can_reforge_broken")
	public static boolean canReforgeBroken = false;
	@Entry(name = "Can Crafted Items Receive Tiers", translatable = "config.reforged.can_crafted_receive_tier")
	@Comment(translatable = "config.reforged.can_crafted_receive_tier.comment", 
			comment = {"Disabling this will force all crafted items to recieve tiers without attributes",
			"That would be the common tier for most items"})
	public static boolean canCraftedReceiveTier = true;
	@Entry(name = "Enable Reforge Experience Cost", translatable = "config.reforged.enable_reforge_experience_cost")
	@Comment(translatable = "config.reforged.enable_reforge_experience_cost.comment", 
			comment = {"Disabling this will remove the experience cost when attempting to reforge any item"})
	public static boolean enableReforgeExpCost = true;
}
