package com.stereowalker.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stereowalker.tiered.Reforged;
import com.stereowalker.tiered.api.ModifierUtils;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.unionlib.util.RegistryHelper;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
	ResourceLocation reforgedAttribute = null;
	public AnvilMenuMixin(MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
		super(p_39773_, p_39774_, p_39775_, p_39776_);
	}

	@Inject(method = "onTake", at =@At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
	private void saveReforgedAttribute(Player p_150474_, ItemStack p_150475_, CallbackInfo ci) {
		if (Reforged.hasModifier(this.inputSlots.getItem(0))) {
//			reforgedAttribute = this.inputSlots.getItem(0).get(Reforged.ComponentsRegistry.MODIFIER);
			reforgedAttribute = VersionHelper.toLoc(this.inputSlots.getItem(0).getTagElement(Reforged.ComponentsRegistry.NBT_SUBTAG_KEY).getString("Tier"));
		}
	}

	/**
	 * This prevents the hammer from being deleted when a reforge is taking place
	 */
	@Redirect(method = "onTake", at =@At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 3))
	private void onTake_redirect(Container container, int pIndex, ItemStack pStack, Player p_150474_, ItemStack p_150475_) {
		boolean deleteItem = true;
		if (this.reforgedAttribute != null) {
			PotentialAttribute potential = Reforged.TIER_DATA.getTiers().get(this.reforgedAttribute);
			if (RegistryHelper.getItemKey(container.getItem(pIndex).getItem()).equals(VersionHelper.toLoc(potential.getReforgeItem()))) {
				deleteItem = false;
				ItemStack hammer = container.getItem(pIndex);
				// attempt to get a random tier
				ResourceLocation potentialAttributeID = this.reforgedAttribute;
				int i = 0;
				while ((potentialAttributeID == null || this.reforgedAttribute.equals(potentialAttributeID)) && i < 2) {
					potentialAttributeID = ModifierUtils.getRandomAttributeIDFor(p_150475_.getItem());
					i++;
				}
				// found an ID
				if(potentialAttributeID != null) {
//					p_150475_.set(Reforged.ComponentsRegistry.MODIFIER, potentialAttributeID);
					p_150475_.getOrCreateTagElement(Reforged.ComponentsRegistry.NBT_SUBTAG_KEY).putString(Reforged.ComponentsRegistry.NBT_SUBTAG_DATA_KEY, potentialAttributeID.toString());
				}
				else {
					Reforged.LOGGER.info("Failed to find an appropriate modifier for this item");
				}

				if ((hammer.getMaxDamage() - hammer.getDamageValue()) == potential.getReforgeDurabilityCost())
					deleteItem = true;
				else
					hammer.setDamageValue(hammer.getDamageValue()+potential.getReforgeDurabilityCost());
			}
		}
		if (deleteItem)
			container.setItem(pIndex, pStack);
	}
}
