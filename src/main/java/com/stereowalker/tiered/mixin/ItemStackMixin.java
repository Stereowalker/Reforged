package com.stereowalker.tiered.mixin;

import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stereowalker.reforged.Reforged;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
    private void go(EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> pAction, CallbackInfo ci) {
    	ItemStack thisStack = (ItemStack)(Object)this;
    	Reforged.AppendAttributesToOriginal(thisStack, slot, Reforged.isPreferredEquipmentSlot(thisStack, slot), "AttributeModifiers",
				template -> template.getRequiredEquipmentSlot(), 
				template -> template.getOptionalEquipmentSlot(), 
				(template) -> template.realize((attr, modi) -> pAction.accept(attr, modi, ItemAttributeModifiers.Display.attributeModifiers()), slot));
    }
}
