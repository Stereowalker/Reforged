package draylar.tiered.mixin;

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

import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Shadow public abstract CompoundTag getOrCreateTagElement(String key);

    @Shadow public abstract boolean hasTag();

    @Shadow public abstract CompoundTag getTagElement(String key);

    private boolean isTiered = false;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getAmount()D"), method = "getTooltipLines", locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeAttributeModifier(Player player, TooltipFlag context, CallbackInfoReturnable<List> cir, List list, MutableComponent component, int i , EquipmentSlot var6[], int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap, Iterator var11, Map.Entry entry, AttributeModifier entityAttributeModifier) {
        isTiered = entityAttributeModifier.getName().contains("tiered:");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/TranslatableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 2), method = "getTooltipLines")
    private MutableComponent getTextFormatting(TranslatableComponent translatableText, ChatFormatting formatting) {
        if(this.hasTag() && this.getTagElement(Tiered.NBT_SUBTAG_KEY) != null && isTiered) {
            ResourceLocation tier = new ResourceLocation(this.getOrCreateTagElement(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));
            PotentialAttribute attribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);

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
        if(this.hasTag() && this.getTagElement("display") == null && this.getTagElement(Tiered.NBT_SUBTAG_KEY) != null) {
            ResourceLocation tier = new ResourceLocation(getOrCreateTagElement(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));

            // attempt to display attribute if it is valid
            PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);

            if(potentialAttribute != null) {
                cir.setReturnValue(new TranslatableComponent(potentialAttribute.getID() + ".label").append(" ").append(cir.getReturnValue()).setStyle(potentialAttribute.getStyle()));
            }
        }
    }
}
