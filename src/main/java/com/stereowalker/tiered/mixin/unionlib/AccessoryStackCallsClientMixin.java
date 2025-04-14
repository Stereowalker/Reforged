package com.stereowalker.tiered.mixin.unionlib;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.stereowalker.tiered.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.unionlib.hook.AccessoryStackCalls;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(AccessoryStackCalls.class)
public abstract class AccessoryStackCallsClientMixin {

    private static boolean isTiered = false;
    @SuppressWarnings("rawtypes")
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getAmount()D"), method = "gatherAttributes", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void storeAttributeModifier(ItemStack arg0, Player arg1, Multimap multimap, List list, String name, CallbackInfo ci, Iterator var5, Map.Entry entry, AttributeModifier attributemodifier) {
        isTiered = attributemodifier.getName().contains("tiered_");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 1), method = "gatherAttributes")
    private static MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting, ItemStack stack, @Nullable Player pPlayer, Multimap<Attribute, AttributeModifier> multimap, List<Component> list, String name) {
    	if(Reforged.hasModifier(stack) && isTiered) {
        	ResourceLocation tier = VersionHelper.toLoc(stack.getOrCreateTagElement(Reforged.ComponentsRegistry.NBT_SUBTAG_KEY).getString(Reforged.ComponentsRegistry.NBT_SUBTAG_DATA_KEY));
            PotentialAttribute attribute = Reforged.TIER_DATA.getTiers().get(tier);

            return translatableText.setStyle(attribute.getStyle());
        } else {
            return translatableText.withStyle(formatting);
        }
    }

    @ModifyVariable(remap = false, 
            method = "gatherAttributes", argsOnly = true,
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z")
    )
    private static Multimap<Attribute, AttributeModifier> sort(Multimap<Attribute, AttributeModifier> map) {
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
}
