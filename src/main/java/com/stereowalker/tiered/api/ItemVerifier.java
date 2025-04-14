package com.stereowalker.tiered.api;

import com.stereowalker.tiered.Reforged;
import com.stereowalker.unionlib.util.RegistryHelper;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemVerifier {

    private final String id;
    private final String tag;

    public ItemVerifier(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    /**
     * Returns whether the given {@link ResourceLocation} is valid for this ItemVerifier, which may check direct against either a {@link ResourceLocation} or {@link Tag<Item>}.
     * <p>The given {@link ResourceLocation} should be the ID of an {@link Item} in {@link Registry#ITEM}.
     *
     * @param itemID  item registry ID to check against this verifier
     * @return  whether the check succeeded
     */
    public boolean isValid(ResourceLocation itemID) {
        return isValid(itemID.toString());
    }

    /**
     * Returns whether the given {@link String} is valid for this ItemVerifier, which may check direct against either a {@link ResourceLocation} or {@link Tag<Item>}.
     * <p>The given {@link String} should be the ID of an {@link Item} in {@link Registry#ITEM}.
     *
     * @param itemID  item registry ID to check against this verifier
     * @return  whether the check succeeded
     */
    public boolean isValid(String itemID) {
        if(id != null) {
            return itemID.equals(id);
        } else if(tag != null) {
            TagKey<Item> itemTag = TagKey.create(RegistryHelper.itemKey(), VersionHelper.toLoc(tag));

            if(itemTag != null) {
                return new ItemStack(RegistryHelper.getItem(VersionHelper.toLoc(itemID))).is(itemTag);
            } else {
                Reforged.LOGGER.error(tag + " was specified as an item verifier tag, but it does not exist!");
            }
        }

        return false;
    }
}
