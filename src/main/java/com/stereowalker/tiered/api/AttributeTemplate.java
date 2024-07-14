package com.stereowalker.tiered.api;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Multimap;
import com.google.gson.annotations.SerializedName;
import com.stereowalker.tiered.Tiered;
import com.stereowalker.unionlib.util.VersionHelper;
import com.stereowalker.unionlib.world.entity.AccessorySlot;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Stores information on an AttributeModifier template applied to an ItemStack.
 *
 * The ID of the AttributeTemplate is the logical ID used to determine what "type" of attribute of is.
 * An AttributeModifier has:
 *   - a UUID, which is a unique identifier to separate different attributes of the same type
 *   - a name, which is used for generating a non-specified UUID and displaying in tooltips in some context
 *   - an amount, which is used in combination with the operation to modify the final relevant value
 *   - a modifier, which can be something such as addition or subtraction
 *
 * The EquipmentSlot is used to only apply this template to certain items.
 */
public class AttributeTemplate {

    @SerializedName("type")
    private final String attributeTypeID;

    @SerializedName("modifier")
    private final AttributeModifier attributeModifier;

    @SerializedName("required_equipment_slots")
    private final EquipmentSlotGroup[] requiredEquipmentSlotTypes;

    @SerializedName("optional_equipment_slots")
    private final EquipmentSlotGroup[] optionalEquipmentSlotTypes;

    @SerializedName("required_accessory_slots")
    private final AccessorySlot[] requiredAccessorySlotTypes;

    @SerializedName("optional_accessory_slots")
    private final AccessorySlot[] optionalAccessorySlotTypes;

    @SerializedName("required_accessory_groups")
    private final AccessorySlot.Group[] requiredAccessoryGroupTypes;

    @SerializedName("optional_accessory_groups")
    private final AccessorySlot.Group[] optionalAccessoryGroupTypes;

    @SerializedName("required_curio_slots")
    private final String[] requiredCurioSlotTypes;

    @SerializedName("optional_curio_slots")
    private final String[] optionalCurioSlotTypes;

    public AttributeTemplate(String attributeTypeID, AttributeModifier AttributeModifier, 
    		EquipmentSlotGroup[]  requiredEquipmentSlotTypes, EquipmentSlotGroup[]  optionalEquipmentSlotTypes, 
    		AccessorySlot[] requiredAccessorySlotTypes, AccessorySlot[] optionalAccessorySlotTypes, 
    		AccessorySlot.Group[] requiredAccessoryGroupTypes, AccessorySlot.Group[] optionalAccessoryGroupTypes, 
    		String[] requiredCurioSlotTypes, String[] optionalCurioSlotTypes) {
        this.attributeTypeID = attributeTypeID;
        this.attributeModifier = AttributeModifier;
        this.requiredEquipmentSlotTypes = requiredEquipmentSlotTypes;
        this.optionalEquipmentSlotTypes = optionalEquipmentSlotTypes;
        this.requiredAccessorySlotTypes = requiredAccessorySlotTypes;
        this.optionalAccessorySlotTypes = optionalAccessorySlotTypes;
        this.requiredAccessoryGroupTypes = requiredAccessoryGroupTypes;
        this.optionalAccessoryGroupTypes = optionalAccessoryGroupTypes;
        this.requiredCurioSlotTypes = requiredCurioSlotTypes;
        this.optionalCurioSlotTypes = optionalCurioSlotTypes;
    }

    public EquipmentSlotGroup[]  getRequiredEquipmentSlot() {
        return requiredEquipmentSlotTypes;
    }

    public EquipmentSlotGroup[]  getOptionalEquipmentSlot() {
        return optionalEquipmentSlotTypes;
    }

    public EquipmentSlot[]  getRequiredLiteralEquipmentSlot() {
    	List<EquipmentSlot> slots = Lists.newArrayList();
    	if (requiredEquipmentSlotTypes != null)
        	for (EquipmentSlot slot : EquipmentSlot.values()) {
        		if (!slots.contains(slot)) {
        			for (EquipmentSlotGroup group : requiredEquipmentSlotTypes) {
        				if (group.test(slot)) slots.add(slot);
        			}
        		}
        	}
        return slots.toArray(new EquipmentSlot[0]);
    }

    public EquipmentSlot[]  getOptionalLiteralEquipmentSlot() {
    	List<EquipmentSlot> slots = Lists.newArrayList();
    	if (optionalEquipmentSlotTypes != null)
        	for (EquipmentSlot slot : EquipmentSlot.values()) {
        		if (!slots.contains(slot)) {
        			for (EquipmentSlotGroup group : optionalEquipmentSlotTypes) {
        				if (group.test(slot)) slots.add(slot);
        			}
        		}
        	}
        return slots.toArray(new EquipmentSlot[0]);
    }

    public AccessorySlot[] getRequiredAccessorySlot() {
		return requiredAccessorySlotTypes;
	}

	public AccessorySlot[] getOptionalAccessorySlot() {
		return optionalAccessorySlotTypes;
	}

	public AccessorySlot.Group[] getRequiredAccessoryGroup() {
		return requiredAccessoryGroupTypes;
	}

	public AccessorySlot.Group[] getOptionalAccessoryGroup() {
		return optionalAccessoryGroupTypes;
	}

	public String[]  getRequiredCurioSlot() {
        return requiredCurioSlotTypes;
    }

    public String[]  getOptionalCurioSlot() {
        return optionalCurioSlotTypes;
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, EquipmentSlotGroup slot) {
        realize(actions, Tiered.MODIFIERS[slot.ordinal()]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, EquipmentSlot slot) {
        realize(actions, Tiered.MODIFIERS[slot.ordinal()]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, AccessorySlot slot) {
        realize(actions, Tiered.MODIFIERS[slot.getIndex()+6]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, AccessorySlot.Group slot) {
        realize(actions, Tiered.MODIFIERS[slot.ordinal()+15]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, String slot) {
        realize(actions, Tiered.CURIO_MODIFIERS.getOrDefault(slot, VersionHelper.toLoc("tiered","curio_rings")));
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    private void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, ResourceLocation id) {
    	AttributeModifier cloneModifier = new AttributeModifier(
    			id.withPath("tiered_"+attributeModifier.id().getPath()),
                attributeModifier.amount(),
                attributeModifier.operation()
        );

        Optional<Reference<Attribute>> key = BuiltInRegistries.ATTRIBUTE.getHolder((VersionHelper.toLoc(attributeTypeID)));
//        Holder<Attribute> key = RegistryHelper.getAttribute(new ResourceLocation(attributeTypeID));
        if(key == null || key.isEmpty()) {
            Tiered.LOGGER.warn(String.format("%s was referenced as an attribute type, but it does not exist! A data file in /tiered/item_attributes/ has an invalid type property.", attributeTypeID));
        } else {
            actions.accept(key.get(), cloneModifier);
        }
    }
}
