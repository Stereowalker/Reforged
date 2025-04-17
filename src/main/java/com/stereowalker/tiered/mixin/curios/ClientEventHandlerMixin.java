package com.stereowalker.tiered.mixin.curios;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import top.theillusivec4.curios.client.ClientEventHandler;

@Mixin(ClientEventHandler.class)
public abstract class ClientEventHandlerMixin {

    private static boolean isTiered = false;

    @SuppressWarnings("rawtypes")
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;amount()D"), method = "onTooltip", locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeAttributeModifier(ItemTooltipEvent evt, CallbackInfo ci, ItemStack stack, Player player, List tooltip, Map map, Set toRemove, Set curioTags, List slots, List tagTooltips, MutableComponent slotsTooltip, Optional optionalCurio, List attributeTooltip, Iterator var13, String identifier, Multimap multimap, boolean init, Iterator var17, Map.Entry entry, AttributeModifier attributemodifier) {
    	isTiered = attributemodifier.name().contains("tiered_");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 6), method = "onTooltip")
    private MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting, ItemTooltipEvent evt) {
    	if(Reforged.hasModifier(evt.getItemStack()) && isTiered) {
            ResourceLocation tier = evt.getItemStack().get(Reforged.ComponentsRegistry.MODIFIER);
            PotentialAttribute attribute = Reforged.TIER_DATA.getTiers().get(tier);

            return translatableText.setStyle(attribute.getStyle());
        } else {
            return translatableText.withStyle(formatting);
        }
    }

    @ModifyVariable(remap = false,
            method = "onTooltip",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"),
            ordinal = 0
    )
    private Multimap<Holder<Attribute>, AttributeModifier> sort(Multimap<Holder<Attribute>, AttributeModifier> map) {
        Multimap<Holder<Attribute>, AttributeModifier> vanillaFirst = LinkedListMultimap.create();
        Multimap<Holder<Attribute>, AttributeModifier> remaining = LinkedListMultimap.create();

        map.forEach((entityAttribute, entityAttributeModifier) -> {
            if (!entityAttributeModifier.name().contains("tiered")) {
                vanillaFirst.put(entityAttribute, entityAttributeModifier);
            } else {
                remaining.put(entityAttribute, entityAttributeModifier);
            }
        });

        vanillaFirst.putAll(remaining);
        return vanillaFirst;
    }
}
