package com.stereowalker.tiered.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stereowalker.reforged.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin implements DataComponentHolder {

	private static final ThreadLocal<ItemStack> CURRENT_STACK = new ThreadLocal<>();

	@Inject(method = "addAttributeTooltips", at = @At("HEAD"))
	private void captureStack(CallbackInfo ci) {
		CURRENT_STACK.set((ItemStack)(Object)this);
	}

	@Inject(method = "addAttributeTooltips", at = @At("RETURN"))
	private void releaseStack(CallbackInfo ci) {
		CURRENT_STACK.remove();
	}

	@Redirect(
			method = "lambda$addAttributeTooltips$0",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers$Display;apply(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V")
			)
	private static void redirectDisplayApply(ItemAttributeModifiers.Display display, Consumer<Component> consumer,
			Player player,
			Holder<Attribute> attribute,
			AttributeModifier modifier) {
		ItemStack stack = CURRENT_STACK.get();
		if (stack != null && modifier.id().toString().contains("tiered_") && Reforged.hasModifier(stack)) {
			Identifier tier = Reforged.ComponentsRegistry.MODIFIER_D.getData(stack);
			PotentialAttribute attr = Reforged.TIER_DATA.getTiers().get(tier);
			if (attr != null) {
				display.apply(component -> consumer.accept(component instanceof MutableComponent mc ? mc.setStyle(attr.getStyle()) : component), player, attribute, modifier);
				return;
			}
		}

		display.apply(consumer, player, attribute, modifier);
	}

    @Inject(
            method = "getHoverName",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyName(CallbackInfoReturnable<Component> cir) {
        if(this.get(DataComponents.CUSTOM_NAME) == null && Reforged.hasModifier((ItemStack)(Object)this)) {
            Identifier tier = Reforged.ComponentsRegistry.MODIFIER_D.getData((ItemStack)(Object)this);

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
