package com.stereowalker.tiered.mixin.unionlib;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.stereowalker.reforged.Reforged;
import com.stereowalker.unionlib.hook.AccessoryStackCalls;
import com.stereowalker.unionlib.world.entity.AccessorySlot;
import com.stereowalker.unionlib.world.item.AccessoryItem;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

@Mixin(AccessoryStackCalls.class)
public abstract class AccessoryStackCallsMixin {
	@Redirect(remap = false, 
			method = "getAttributeModifiers",
			at = @At(value = "INVOKE", target = "Lcom/stereowalker/unionlib/world/item/AccessoryItem;getAttributeModifiers(Lcom/stereowalker/unionlib/world/entity/AccessorySlot;Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;")
			)
	private static Multimap<Attribute, AttributeModifier> go(AccessoryItem item, AccessorySlot slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> newMap = LinkedListMultimap.create();
		newMap.putAll(item.getAttributeModifiers(slot, stack));
		return Reforged.AppendAttributesToOriginal(stack, slot, Reforged.isPreferredAccessorySlot(stack, slot), "AccessoryAttributeModifiers",
				template -> template.getRequiredAccessorySlot(), 
				template -> template.getOptionalAccessorySlot(), 
				(template) -> template.realize(newMap::put, slot));
	}

	@Redirect(remap = false, 
			method = "getAttributeModifiersForGroup",
			at = @At(value = "INVOKE", target = "Lcom/stereowalker/unionlib/world/item/AccessoryItem;getAttributeModifiers(Lcom/stereowalker/unionlib/world/entity/AccessorySlot$Group;Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;")
			)
	private static Multimap<Attribute, AttributeModifier> go2(AccessoryItem item, AccessorySlot.Group group, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> newMap = LinkedListMultimap.create();
		newMap.putAll(item.getAttributeModifiers(group, stack));
		return Reforged.AppendAttributesToOriginal(stack, group, Reforged.isPreferredAccessorySlot(stack, group), "AccessoryAttributeModifiers",
				template -> template.getRequiredAccessoryGroup(), 
				template -> template.getOptionalAccessoryGroup(), 
				(template) -> template.realize(newMap::put, group));
	}
}
