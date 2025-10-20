package com.stereowalker.tiered.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stereowalker.tiered.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin implements DataComponentHolder {

    private boolean isTiered = false;

    @SuppressWarnings("rawtypes")
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;amount()D"), method = "addModifierTooltip")
    private void storeAttributeModifier(Consumer arg0, Player arg1, Holder arg2, AttributeModifier pModfier, CallbackInfo ci) {
        isTiered = pModfier.id().toString().contains("tiered_");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 1), method = "addModifierTooltip")
    private MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting) {
        if(Reforged.hasModifier((ItemStack)(Object)this) && isTiered) {
            ResourceLocation tier = Reforged.ComponentsRegistry.MODIFIER_D.getData((ItemStack)(Object)this);
            PotentialAttribute attribute = Reforged.TIER_DATA.getTiers().get(tier);

            return translatableText.setStyle(attribute.getStyle());
        } else {
            return translatableText.withStyle(formatting);
        }
    }

    @Inject(
            method = "getHoverName",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyName(CallbackInfoReturnable<Component> cir) {
        if(this.get(DataComponents.CUSTOM_NAME) == null && Reforged.hasModifier((ItemStack)(Object)this)) {
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
