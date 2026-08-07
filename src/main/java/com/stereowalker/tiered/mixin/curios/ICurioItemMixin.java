package com.stereowalker.tiered.mixin.curios;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stereowalker.reforged.Reforged;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mixin(ICurioItem.class)
public interface ICurioItemMixin {

    @Inject(method = "forEachModifier(Lnet/minecraft/world/item/ItemStack;Ltop/theillusivec4/curios/api/SlotContext;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private static void go(ItemStack stack, SlotContext slotContext, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
    	Reforged.AppendAttributesToOriginal(
    			stack, 
    			slotContext.identifier(), 
				Reforged.isPreferredCurioSlot(stack, slotContext.identifier()), 
				"CurioAttributeModifiers",
				template -> template.getRequiredCurioSlot(), 
				template -> template.getOptionalCurioSlot(), 
				(template) -> template.realize(action, slotContext.identifier(), slotContext.index()));
    }
    
    @Inject(method = "forEachModifier(Lnet/minecraft/world/item/ItemStack;Ltop/theillusivec4/curios/api/type/ISlotType;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private static void go2(ItemStack stack, ISlotType slotContext, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
    	Reforged.AppendAttributesToOriginal(
    			stack, 
    			slotContext.getId(), 
				Reforged.isPreferredCurioSlot(stack, slotContext.getId()), 
				"CurioAttributeModifiers",
				template -> template.getRequiredCurioSlot(), 
				template -> template.getOptionalCurioSlot(), 
				(template) -> template.realize(action, slotContext.getId(), 0));
    }
}
