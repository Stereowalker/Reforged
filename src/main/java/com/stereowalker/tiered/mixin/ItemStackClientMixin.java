package com.stereowalker.tiered.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.stereowalker.reforged.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin/* implements DataComponentHolder */ {

    @Shadow public abstract CompoundTag getOrCreateTagElement(String key);

    @Shadow public abstract boolean hasTag();

    @Shadow public abstract CompoundTag getTagElement(String key);
    
    private boolean isTiered = false;

    @SuppressWarnings("rawtypes")
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getAmount()D"), method = "getTooltipLines", locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeAttributeModifier(Player player, TooltipFlag context, CallbackInfoReturnable<List> cir, List list, MutableComponent component, int i, EquipmentSlot var6[], int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap, Iterator var11, Map.Entry entry, AttributeModifier entityAttributeModifier) {
        isTiered = entityAttributeModifier.getName().contains("tiered_");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 5), method = "getTooltipLines")
    private MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting) {
        if(Reforged.hasModifier((ItemStack)(Object)this) && isTiered) {
            ResourceLocation tier = Reforged.ComponentsRegistry.MODIFIER_D.getData((ItemStack)(Object)this);
            PotentialAttribute attribute = Reforged.TIER_DATA.getTiers().get(tier);

            return translatableText.setStyle(attribute.getStyle());
        } else {
            return translatableText.withStyle(formatting);
        }
    }

    @ModifyVariable(
            method = "getTooltipLines",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"),
            index = 10
    )
    private Multimap<Attribute, AttributeModifier> sort(Multimap<Attribute, AttributeModifier> map) {
        Multimap<Attribute, AttributeModifier> vanillaFirst = LinkedListMultimap.create();
        Multimap<Attribute, AttributeModifier> remaining = LinkedListMultimap.create();

        map.forEach((entityAttribute, entityAttributeModifier) -> {
            if (!entityAttributeModifier.getName().contains("tiered")) {
                vanillaFirst.put(entityAttribute, entityAttributeModifier);
            } else {
                remaining.put(entityAttribute, entityAttributeModifier);
            }
        });

        vanillaFirst.putAll(remaining);
        return vanillaFirst;
    }

    @Inject(
            method = "getHoverName",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyName(CallbackInfoReturnable<Component> cir) {
        if(this.hasTag() && this.getTagElement("display") == null && Reforged.hasModifier((ItemStack)(Object)this)) {
            ResourceLocation tier = Reforged.ComponentsRegistry.MODIFIER_D.getData((ItemStack)(Object)this);

            // attempt to display attribute if it is valid
            PotentialAttribute potentialAttribute = Reforged.TIER_DATA.getTiers().get(tier);

            if(potentialAttribute != null) {
            	MutableComponent title;
            	if (potentialAttribute.getLiteralName() != null) title = Component.literal(potentialAttribute.getLiteralName());
            	else title = Component.translatable(Util.makeDescriptionId("tier", Reforged.getKey(potentialAttribute)));
                cir.setReturnValue(title.append(" ").append(cir.getReturnValue()).setStyle(potentialAttribute.getStyle()));
            }
        }
    }
}
