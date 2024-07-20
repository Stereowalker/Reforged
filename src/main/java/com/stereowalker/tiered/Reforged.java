package com.stereowalker.tiered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.stereowalker.tiered.api.AttributeTemplate;
import com.stereowalker.tiered.api.ModifierUtils;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.tiered.compat.CuriosCompat;
import com.stereowalker.tiered.config.Config;
import com.stereowalker.tiered.data.PoolDataLoader;
import com.stereowalker.tiered.data.TierAffixer;
import com.stereowalker.tiered.data.TierDataLoader;
import com.stereowalker.tiered.network.protocol.game.ClientboundTierSyncerPacket;
import com.stereowalker.unionlib.UnionLib;
import com.stereowalker.unionlib.api.collectors.ConfigCollector;
import com.stereowalker.unionlib.api.collectors.InsertCollector;
import com.stereowalker.unionlib.api.collectors.PacketCollector;
import com.stereowalker.unionlib.api.collectors.ReloadListeners;
import com.stereowalker.unionlib.api.creativetabs.CreativeTabPopulator;
import com.stereowalker.unionlib.api.registries.RegistryCollector;
import com.stereowalker.unionlib.core.registries.RegistryHolder;
import com.stereowalker.unionlib.core.registries.RegistryObject;
import com.stereowalker.unionlib.insert.Inserts;
import com.stereowalker.unionlib.insert.ServerInserts;
import com.stereowalker.unionlib.mod.MinecraftMod;
import com.stereowalker.unionlib.mod.PacketHolder;
import com.stereowalker.unionlib.mod.ServerSegment;
import com.stereowalker.unionlib.util.ModHelper;
import com.stereowalker.unionlib.util.RegistryHelper;
import com.stereowalker.unionlib.util.VersionHelper;
import com.stereowalker.unionlib.world.entity.AccessorySlot;
import com.stereowalker.unionlib.world.item.AccessoryItem;

import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.fml.common.Mod;

@Mod("tiered")
public class Reforged extends MinecraftMod implements PacketHolder {

	public static final TierDataLoader TIER_DATA = new TierDataLoader();
	public static final PoolDataLoader POOL_DATA = new PoolDataLoader();
	public static ResourceLocation getKey(PotentialAttribute tier) {
		return TIER_DATA.getTiers().entrySet().stream()
	      .filter(entry -> tier.equals(entry.getValue()))
	      .map(Map.Entry::getKey).findFirst().get();
	}

	public static final ResourceLocation[] MODIFIERS = new ResourceLocation[] {
			//Equipment
			VersionHelper.toLoc("tiered","any"),
			VersionHelper.toLoc("tiered","mainhand"),
			VersionHelper.toLoc("tiered","offhand"),
			VersionHelper.toLoc("tiered","hand"),
			VersionHelper.toLoc("tiered","boots"),
			VersionHelper.toLoc("tiered","leggings"),
			VersionHelper.toLoc("tiered","chestplates"),
			VersionHelper.toLoc("tiered","helmets"),
			VersionHelper.toLoc("tiered","armor"),
			VersionHelper.toLoc("tiered","body"),
			//Accessory slots 
			VersionHelper.toLoc("tiered","accessory1"),
			VersionHelper.toLoc("tiered","accessory2"),
			VersionHelper.toLoc("tiered","accessory3"),
			VersionHelper.toLoc("tiered","accessory4"),
			VersionHelper.toLoc("tiered","accessory5"),
			VersionHelper.toLoc("tiered","accessory6"),
			VersionHelper.toLoc("tiered","accessory7"),
			VersionHelper.toLoc("tiered","accessory8"),
			VersionHelper.toLoc("tiered","accessory9"),
			//Accessory groups
			VersionHelper.toLoc("tiered","neclaces"),
			VersionHelper.toLoc("tiered","backs"),
			VersionHelper.toLoc("tiered","rings")
	};

	public static final Map<String, ResourceLocation> CURIO_MODIFIERS = Util.make(Maps.newHashMap(), (map) -> {
		map.put("ring", VersionHelper.toLoc("tiered","curio_rings"));
	});

	public static final Logger LOGGER = LogManager.getLogger();

	public static Reforged instance;
	public Reforged() 
	{
		super("tiered", () -> new TieredClientSegment(), () -> new ServerSegment());
		instance = this;
		UnionLib.Modulo.Default_Bow_Draw_Speed.enable();
	}
	
	@Override
	public void setupConfigs(ConfigCollector collector) {
		collector.registerConfig(Config.class);
	}
	
	//TODO: Copy this over to 1.20.1 >
	public static boolean hasModifier(ItemStack stack) {
		//return left.getTagElement(NBT_SUBTAG_KEY) != null;
		return stack.has(ComponentsRegistry.MODIFIER);
	}

	@Override
	public void onModConstruct() {
		if (ModHelper.isCuriosLoaded()) {
			boolean useCurios = false;
			try {Class.forName("top.theillusivec4.curios.api.event.CurioAttributeModifierEvent"); useCurios = true;} 
			catch (Exception e) {System.err.println("Curios support was disabled because the modifier event was not present");}
			if (useCurios) CuriosCompat.load();
		}
	}
	
	@Override
	public void registerServerRelaodableResources(ReloadListeners reloadListener) {
		reloadListener.listenTo(TIER_DATA);
		reloadListener.listenTo(POOL_DATA);
	}

	@SuppressWarnings("resource")
	@Override
	public void registerInserts(InsertCollector collector) {
		collector.addInsert(Inserts.LOGGED_IN, (player -> {
	        if(player.level().isClientSide) return;
	        new ClientboundTierSyncerPacket(TIER_DATA.getTiers()).send(((ServerPlayer)player));
		}));
		collector.addInsert(Inserts.MENU_OPEN, (player, menu) -> {
			menu.getItems().forEach(Reforged::attemptToAffixTier);
		});
		collector.addInsert(ServerInserts.VILLAGER_TRADES, (profession, trades, experimental) -> {
			if (profession == VillagerProfession.ARMORER)
				trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ItemRegistries.ARMORERS_HAMMER, 64, 1, 1, 10));
			if (profession == VillagerProfession.TOOLSMITH)
				trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ItemRegistries.TOOLSMITHS_HAMMER, 64, 1, 1, 10));
			if (profession == VillagerProfession.WEAPONSMITH)
				trades.get(4).add(new VillagerTrades.ItemsForEmeralds(ItemRegistries.WEAPONSMITHS_HAMMER, 64, 1, 1, 10));
		});
		collector.addInsert(Inserts.LIVING_TICK, (living) -> {
			if (living instanceof TierAffixer affixer) {
				 // if items copy is null, set it to player inventory and check each stack
		        if(affixer.InvCopy() == null) {
		            affixer.SetInvCopy(affixer.copyDefaultedList(affixer.player().inventory.items));
		            affixer.player().inventory.items.forEach(Reforged::attemptToAffixTier);
		        }

		        // if items copy =/= inventory, run check and set mainCopy to inventory
		        if (!affixer.player().inventory.items.equals(affixer.InvCopy())) {
		        	affixer.SetInvCopy(affixer.copyDefaultedList(affixer.player().inventory.items));
		            affixer.player().inventory.items.forEach(Reforged::attemptToAffixTier);
		        }
			}
		});
		collector.addInsert(Inserts.ANVIL_CONTENT_CHANGE, (left,right,name,player,output,cost,materialCost,cancel)->{
			if ((Config.canReforgeBroken || !left.isDamaged()) && hasModifier(left)) {
				PotentialAttribute reforgedAttribute = Reforged.TIER_DATA.getTiers().get(left.get(ComponentsRegistry.MODIFIER));
				if (reforgedAttribute.getReforgeItem() != null) {
					if (RegistryHelper.getItemKey(right.getItem()).equals(VersionHelper.toLoc(reforgedAttribute.getReforgeItem())) && (right.getMaxDamage() - right.getDamageValue()) >= reforgedAttribute.getReforgeDurabilityCost()) {
						ItemStack copy = left.copy();
						copy.remove(ComponentsRegistry.MODIFIER);
						output.set(copy);
						cost.set(reforgedAttribute.getReforgeExperienceCost());
					}
				} else {
					LOGGER.info(Reforged.getKey(reforgedAttribute)+" cannot be reforged because it either does not provide any reforging info or the info it provides is not complete");
				}
			}
		});
	}

	@Override
	public void setupRegistries(RegistryCollector collector) {
		collector.addRegistryHolder(ComponentsRegistry.class);
		collector.addRegistryHolder(ItemRegistries.class);
	}

	@Override
	public void populateCreativeTabs(CreativeTabPopulator populator) {
		if (populator.isToolTab()) {
			populator.addItems(ItemRegistries.ARMORERS_HAMMER);
			populator.addItems(ItemRegistries.TOOLSMITHS_HAMMER);
			populator.addItems(ItemRegistries.WEAPONSMITHS_HAMMER);
		}
	}


	@RegistryHolder(namespace = "tiered", registry = DataComponentType.class)
	public class ComponentsRegistry {
		@RegistryObject("tiered_modifier")
		public static final DataComponentType<ResourceLocation> MODIFIER = register(
				p_333150_ -> p_333150_.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC)
				);

		private static <T> DataComponentType<T> register(UnaryOperator<DataComponentType.Builder<T>> pBuilder) {
			return pBuilder.apply(DataComponentType.builder()).build();
		}
	}

	@RegistryHolder(registry = Item.class, namespace = "tiered")
	public class ItemRegistries {
		@RegistryObject("armorers_hammer")
		public static final Item ARMORERS_HAMMER = new Item(new Item.Properties().durability(20));
		@RegistryObject("toolsmiths_hammer")
		public static final Item TOOLSMITHS_HAMMER = new Item(new Item.Properties().durability(20));
		@RegistryObject("weaponsmiths_hammer")
		public static final Item WEAPONSMITHS_HAMMER = new Item(new Item.Properties().durability(20));
	}
	
	public static void attemptToAffixTier(ItemStack stack) {
		if(!hasModifier(stack) && !stack.isEmpty()) {
			ResourceLocation potentialAttributeID = ModifierUtils.getRandomAttributeIDFor(stack.getItem());
			if(potentialAttributeID != null) {
				stack.set(ComponentsRegistry.MODIFIER, potentialAttributeID);
			}
		}
	}

	/**
	 * Returns an {@link ResourceLocation} namespaced with this mod's modid ("tiered").
	 *
	 * @param path  path of identifier (eg. apple in "minecraft:apple")
	 * @return  ResourceLocation created with a namespace of this mod's modid ("tiered") and provided path
	 */
	public static ResourceLocation id(String path) {
		return VersionHelper.toLoc("tiered", path);
	}

	public static boolean isPreferredEquipmentSlot(ItemStack stack, EquipmentSlotGroup slot) {
		if(stack.getItem() instanceof ArmorItem) {
			ArmorItem item = (ArmorItem) stack.getItem();
			//TODO: Use version helper to make this compatible with older versions
			return slot.test(item.getEquipmentSlot());
//			return VersionHelper.isEquippableInSlot(item, slot);
		}

		if(stack.getItem() instanceof ShieldItem) {
			return slot.test(EquipmentSlot.MAINHAND) || slot.test(EquipmentSlot.OFFHAND);
		}

		return slot.test(EquipmentSlot.MAINHAND);
	}

	public static boolean isPreferredEquipmentSlot(ItemStack stack, EquipmentSlot slot) {
		if(stack.getItem() instanceof ArmorItem) {
			ArmorItem item = (ArmorItem) stack.getItem();
			//TODO: Use version helper to make this compatible with older versions
			return VersionHelper.isEquippableInSlot(item, slot);
		}

		if(stack.getItem() instanceof ShieldItem) {
			return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
		}

		return slot == EquipmentSlot.MAINHAND;
	}

	public static boolean isPreferredAccessorySlot(ItemStack stack, AccessorySlot slot) {
		if(stack.getItem() instanceof AccessoryItem) {
			AccessoryItem item = (AccessoryItem) stack.getItem();
			return item.getAccessorySlots().contains(slot);
		}

		return false;
	}

	public static boolean isPreferredAccessorySlot(ItemStack stack, AccessorySlot.Group group) {
		for (AccessorySlot slot : AccessorySlot.values()) {
			if (slot.getGroup() == group && !isPreferredAccessorySlot(stack, slot)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isPreferredCurioSlot(ItemStack stack, String slot) {
		return stack.is(TagKey.create(RegistryHelper.itemKey(), VersionHelper.toLoc("curios", slot)));
	}
	
	@Override
	public void registerPackets(PacketCollector collector) {
		collector.registerClientboundPacket(ClientboundTierSyncerPacket.id, ClientboundTierSyncerPacket.class, ClientboundTierSyncerPacket::new);
	}

	public static <T> void AppendAttributesToOriginal(ItemStack stack, T slot, boolean isPreferredSlot, String customAttributes, 
			Function<AttributeTemplate,T[]> requiredSlotsArray, 
			Function<AttributeTemplate,T[]> optionalSlotsArray, Consumer<AttributeTemplate> realize) {
		//		Multimap<Attribute, AttributeModifier> newMap = LinkedListMultimap.create();
		if(hasModifier(stack)) {
			ResourceLocation tier = stack.get(ComponentsRegistry.MODIFIER);

//			if(!stack.hasTag() || !stack.getTag().contains(customAttributes, 9)) {
				PotentialAttribute potentialAttribute = Reforged.TIER_DATA.getTiers().get(tier);

				if(potentialAttribute != null) {
					potentialAttribute.getAttributes().forEach(template -> {
						// get required equipment slots
						if(requiredSlotsArray.apply(template) != null) {
							List<T> requiredSlots = new ArrayList<>(Arrays.asList(requiredSlotsArray.apply(template)));
							if(requiredSlots.contains(slot))
								realize.accept(template);
						}

						// get optional equipment slots
						if(optionalSlotsArray.apply(template) != null) {
							List<T> optionalSlots = new ArrayList<>(Arrays.asList(optionalSlotsArray.apply(template)));
							// optional equipment slots are valid ONLY IF the equipment slot is valid for the thing
							if(optionalSlots.contains(slot) && isPreferredSlot)
								realize.accept(template);
						}
					});
				}
//			}
		}
	}
}
