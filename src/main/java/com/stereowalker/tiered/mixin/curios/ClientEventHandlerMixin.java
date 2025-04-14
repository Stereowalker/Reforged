package com.stereowalker.tiered.mixin.curios;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.ChatFormatting;
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
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getAmount()D"), method = "onTooltip", locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeAttributeModifier(ItemTooltipEvent evt, CallbackInfo ci, ItemStack stack, Player player, List tooltip, CompoundTag tag, int i, Map map, Set toRemove, Set curioTags, List slots, List tagTooltips, MutableComponent slotsTooltip, LazyOptional optionalCurio, List attributeTooltip, Iterator var15, String identifier, UUID uuid, Multimap multimap, Map collapsed, Iterator var20, Map.Entry entry, Attribute attribute, AttributeModifier attributemodifier) {
        isTiered = attributemodifier.getName().contains("tiered_");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 6), method = "onTooltip")
    private MutableComponent getTextFormatting(MutableComponent translatableText, ChatFormatting formatting, ItemTooltipEvent evt) {
    	if(evt.getItemStack().hasTag() && evt.getItemStack().getTagElement(Reforged.ComponentsRegistry.NBT_SUBTAG_KEY) != null && isTiered) {
            ResourceLocation tier = VersionHelper.toLoc(evt.getItemStack().getOrCreateTagElement(Reforged.ComponentsRegistry.NBT_SUBTAG_KEY).getString(Reforged.ComponentsRegistry.NBT_SUBTAG_DATA_KEY));
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
}
