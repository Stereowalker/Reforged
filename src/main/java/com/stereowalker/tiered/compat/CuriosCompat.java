package com.stereowalker.tiered.compat;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.stereowalker.reforged.Reforged;


public class CuriosCompat {
	public static void load() {
//		Supplier<Consumer<? extends Event>> i = () -> 
//		(Consumer<top.theillusivec4.curios.api.event.CurioAttributeModifierEvent>)(event)->{
//			Tiered.AppendAttributesToOriginal(
//					event.getItemStack(), 
//					event.getSlotContext().identifier(), 
//					Tiered.isPreferredCurioSlot(event.getItemStack(), event.getSlotContext().identifier()), 
//					"CurioAttributeModifiers"/* , event.getModifiers() */,
//					template -> template.getRequiredCurioSlot(), 
//					template -> template.getOptionalCurioSlot(), 
//					(template) -> template.realize(event::addModifier, event.getSlotContext().identifier(), event.getSlotContext().index()));
//		}
//		;
//		NeoForge.EVENT_BUS.addListener(i.get());
	}
}
