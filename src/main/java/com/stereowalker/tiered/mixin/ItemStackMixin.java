package com.stereowalker.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Multimap;
import com.stereowalker.tiered.Reforged;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	//NOTE: This doesn't work on fabric at all. Resolve this before considering a forge port
//	@Redirect(
//            method = "getAttributeModifiers",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;")
//    )
//    private Multimap<Attribute, AttributeModifier> go(Item item, EquipmentSlot slot) {
//		ItemStack thisStack = (ItemStack)(Object)this;
//    	return Reforged.AppendAttributesToOriginal(thisStack, slot, Reforged.isPreferredEquipmentSlot(thisStack, slot), "AttributeModifiers", item.getAttributeModifiers(thisStack, slot),
//				template -> template.getRequiredEquipmentSlot(), 
//				template -> template.getOptionalEquipmentSlot(), 
//				(template, newMap) -> template.realize(newMap::put, slot));
//    }
}
