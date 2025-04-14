package com.stereowalker.tiered.compat;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.stereowalker.tiered.Reforged;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class CuriosCompat {
	public static void load() {
		Supplier<Consumer<? extends Event>> i = () -> 
		(Consumer<top.theillusivec4.curios.api.event.CurioAttributeModifierEvent>)(event)->{
			Reforged.AppendAttributesToOriginal(
					event.getItemStack(), 
					event.getSlotContext().identifier(), 
					Reforged.isPreferredCurioSlot(event.getItemStack(), event.getSlotContext().identifier()), 
					"CurioAttributeModifiers"/* , event.getModifiers() */,
					template -> template.getRequiredCurioSlot(), 
					template -> template.getOptionalCurioSlot(), 
					(template) -> template.realize(event::addModifier, event.getSlotContext().identifier()));
		}
		;
		MinecraftForge.EVENT_BUS.addListener(i.get());
	}
}
