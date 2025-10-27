package com.stereowalker.tiered.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.stereowalker.tiered.Reforged;
import com.stereowalker.unionlib.util.GeneralUtilities;
import com.stereowalker.unionlib.util.RegistryHelper;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModifierUtils {
	/**
	 * Returns the ID of a random attribute that is valid for the given {@link Item} in {@link ResourceLocation} form.
	 * <p> If there is no valid attribute for the given {@link Item}, null is returned.
	 *
	 * @param item  {@link Item} to generate a random attribute for
	 * @return  id of random attribute for item in {@link ResourceLocation} form, or null if there are no valid options
	 */
	public static ResourceLocation getRandomAttributeIDFor(Item item) {
		ResourceLocation itemKey = RegistryHelper.getItemKey(item);
		TierPool pool = GeneralUtilities.getRandomFrom(Reforged.POOL_DATA.getPools().values(), (p) -> p.isValid(itemKey));
		PotentialAttribute chosen_tier;
		if (pool == null)
			chosen_tier = GeneralUtilities.getRandomFrom(Reforged.TIER_DATA.getTiers().values(), (a) -> a.isValid(itemKey));
		else {
			List<PotentialAttribute> attris = new ArrayList<>();
			pool.getTiers().forEach((t) -> attris.add(Reforged.TIER_DATA.getTiers().get(VersionHelper.toLoc(t))));
			attris.removeIf((attr) -> attr == null);
			chosen_tier = GeneralUtilities.getRandomFrom(attris, null);
		}
		if (chosen_tier != null) {
			if (chosen_tier.isOld)
				return VersionHelper.toLoc(chosen_tier.getID());
			else for (Entry<ResourceLocation, PotentialAttribute> cho : Reforged.TIER_DATA.getTiers().entrySet())
				if (cho.getValue() == chosen_tier) return cho.getKey();
		}
		//Fallback if the main system fails
		List<ResourceLocation> potentialAttributes = new ArrayList<>();
		// collect all valid attributes for the given item
		Reforged.TIER_DATA.getTiers().forEach((id, attribute) -> {
			if(attribute.isValid(itemKey)) potentialAttributes.add(id);
		});
		if(potentialAttributes.size() > 0) return potentialAttributes.get(new Random().nextInt(potentialAttributes.size()));
		else return null;
	}
	
	public static ResourceLocation getBlankAttributeIDFor(Item item) {
		ResourceLocation itemKey = RegistryHelper.getItemKey(item);
		PotentialAttribute chosen_tier = null;
		for (TierPool pool : Reforged.POOL_DATA.getPools().values()){
			if (pool == null)
				chosen_tier = GeneralUtilities.getRandomFrom(Reforged.TIER_DATA.getTiers().values(), (a) -> a.isValid(itemKey) && a.getAttributes().size() == 0);
			else {
				List<PotentialAttribute> attris = new ArrayList<>();
				pool.getTiers().forEach((t) -> attris.add(Reforged.TIER_DATA.getTiers().get(VersionHelper.toLoc(t))));
				attris.removeIf((attr) -> attr == null || attr.getAttributes().size() > 0);
				chosen_tier = GeneralUtilities.getRandomFrom(attris, null);
			}
			if (chosen_tier != null) break;
		}
		if (chosen_tier != null) {
			if (chosen_tier.isOld)
				return VersionHelper.toLoc(chosen_tier.getID());
			else for (Entry<ResourceLocation, PotentialAttribute> cho : Reforged.TIER_DATA.getTiers().entrySet())
				if (cho.getValue() == chosen_tier) return cho.getKey();
		}
		//Fallback if the main system fails
		List<ResourceLocation> potentialAttributes = new ArrayList<>();
		// collect all valid attributes for the given item
		Reforged.TIER_DATA.getTiers().forEach((id, attribute) -> {
			if(attribute.isValid(itemKey)) potentialAttributes.add(id);
		});
		if(potentialAttributes.size() > 0) return potentialAttributes.get(new Random().nextInt(potentialAttributes.size()));
		else return null;
	}

	private ModifierUtils() {
		// no-op
	}
}