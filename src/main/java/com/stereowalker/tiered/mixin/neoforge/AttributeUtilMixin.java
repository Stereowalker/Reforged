package com.stereowalker.tiered.mixin.neoforge;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.Multimap;
import com.stereowalker.reforged.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;

@Mixin(AttributeUtil.class)
public abstract class AttributeUtilMixin {

    @Redirect(at = @At(value = "INVOKE", target = "net/minecraft/world/entity/ai/attributes/Attribute.toComponent(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;Lnet/minecraft/world/item/TooltipFlag;)Lnet/minecraft/network/chat/MutableComponent;"), method = "applyTextFor")
    private static MutableComponent getTextFormatting(Attribute translatableText, AttributeModifier modif, TooltipFlag flag, ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx) {
    	boolean isTiered = modif.id().toString().contains("tiered_");
        if(Reforged.hasModifier(stack) && isTiered) {
            Identifier tier = stack.get(Reforged.ComponentsRegistry.MODIFIER);
            PotentialAttribute attribute = Reforged.TIER_DATA.getTiers().get(tier);

            return translatableText.toComponent(modif, flag).setStyle(attribute.getStyle());
        } else {
            return translatableText.toComponent(modif, flag);
        }
    }
}
