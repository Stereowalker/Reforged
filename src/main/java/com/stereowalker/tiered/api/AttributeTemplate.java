package com.stereowalker.tiered.api;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.collect.Multimap;
import com.google.gson.annotations.SerializedName;
import com.stereowalker.reforged.Reforged;
import com.stereowalker.unionlib.util.RegistryHelper;
import com.stereowalker.unionlib.util.VersionHelper;
import com.stereowalker.unionlib.world.entity.AccessorySlot;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
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
    private final EquipmentSlot[] requiredEquipmentSlotTypes;

    @SerializedName("optional_equipment_slots")
    private final EquipmentSlot[] optionalEquipmentSlotTypes;

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
    		EquipmentSlot[]  requiredEquipmentSlotTypes, EquipmentSlot[]  optionalEquipmentSlotTypes, 
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

    public EquipmentSlot[]  getRequiredEquipmentSlot() {
        return requiredEquipmentSlotTypes;
    }

    public EquipmentSlot[]  getOptionalEquipmentSlot() {
        return optionalEquipmentSlotTypes;
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
    public void realize(BiConsumer<Attribute, AttributeModifier> actions, EquipmentSlot slot) {
        realize(actions, Reforged.MODIFIERS[slot.getFilterFlag()], slot.getName());
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Attribute, AttributeModifier> actions, AccessorySlot slot) {
        realize(actions, Reforged.MODIFIERS[slot.getIndex()+6], slot.getName());
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Attribute, AttributeModifier> actions, AccessorySlot.Group slot) {
        realize(actions, Reforged.MODIFIERS[slot.ordinal()+15], slot.getName());
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Attribute, AttributeModifier> actions, String slot, int index) {
        realize(actions, Reforged.CURIO_MODIFIERS.getOrDefault(slot, UUID.nameUUIDFromBytes((slot+index).getBytes())), slot);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    private void realize(BiConsumer<Attribute, AttributeModifier> actions, UUID id, String name) {
        AttributeModifier cloneModifier = new AttributeModifier(
                id,
                attributeModifier.getName() + "_tiered_" + name,
                attributeModifier.getAmount(),
                attributeModifier.getOperation()
        );

//        Optional<Reference<Attribute>> key = BuiltInRegistries.ATTRIBUTE.getHolder((VersionHelper.toLoc(attributeTypeID)));
        Attribute key = RegistryHelper.getAttribute(VersionHelper.AttributeHelper.backportAttribute(attributeTypeID));
        if(key == null/* || key.isEmpty()*/) {
            Reforged.LOGGER.warn(String.format("%s was referenced as an attribute type, but it does not exist! A data file in /tiered/item_attributes/ has an invalid type property.", attributeTypeID));
        } else {
            actions.accept(key/*.get()*/, cloneModifier);
        }
    }
}
