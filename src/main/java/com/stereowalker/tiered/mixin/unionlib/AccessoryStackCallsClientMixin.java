package com.stereowalker.tiered.mixin.unionlib;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stereowalker.reforged.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.unionlib.hook.AccessoryStackCalls;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(AccessoryStackCalls.class)
public abstract class AccessoryStackCallsClientMixin {

    private static boolean isTiered = false;

    @SuppressWarnings("rawtypes")
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;amount()D"), method = "gatherAttributes")
    private static void storeAttributeModifier(ItemStack stack, Consumer arg0, Player arg1, Holder arg2, AttributeModifier pModfier, CallbackInfo ci) {
        isTiered = pModfier.id().toString().contains("tiered_");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 1), method = "gatherAttributes")
    private static MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting, ItemStack stack, Consumer<Component> pTooltipAdder, @Nullable Player pPlayer, Holder<Attribute> pAttribute, AttributeModifier pModfier) {
        if(Reforged.hasModifier(stack) && isTiered) {
            Identifier tier = stack.get(Reforged.ComponentsRegistry.MODIFIER);
            PotentialAttribute attribute = Reforged.TIER_DATA.getTiers().get(tier);

            return translatableText.setStyle(attribute.getStyle());
        } else {
            return translatableText.withStyle(formatting);
        }
    }
}
