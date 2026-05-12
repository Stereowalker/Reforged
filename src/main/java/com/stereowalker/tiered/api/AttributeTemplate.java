package com.stereowalker.tiered.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.collect.Multimap;
import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stereowalker.reforged.Reforged;
import com.stereowalker.unionlib.util.VersionHelper;
import com.stereowalker.unionlib.world.entity.AccessorySlot;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
	public static final Codec<EquipmentSlotGroup> LENIENT_SLOT_GROUP_CODEC = Codec.STRING.xmap(
			s -> {
				String lower = s.toLowerCase(Locale.ROOT);
				for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
					if (group.getSerializedName().equals(lower)) return group;
				}
				throw new IllegalArgumentException("Unknown EquipmentSlotGroup: " + s);
			},
			EquipmentSlotGroup::getSerializedName
			);
	private static final Codec<AttributeModifier.Operation> LENIENT_OPERATION_CODEC = Codec.STRING.xmap(
			s -> {
				String lower = s.toLowerCase(Locale.ROOT);
				for (AttributeModifier.Operation op : AttributeModifier.Operation.values()) {
					if (op.getSerializedName().equals(lower)) return op;
				}
				throw new IllegalArgumentException("Unknown AttributeModifier.Operation: " + s);
			},
			AttributeModifier.Operation::getSerializedName
			);
	public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(
	        i -> i.group(
	                Identifier.CODEC.fieldOf("id").forGetter(AttributeModifier::id),
	                Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount),
	                LENIENT_OPERATION_CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)
	            )
	            .apply(i, AttributeModifier::new)
	    );
	
	public static final Codec<AttributeTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		    Codec.STRING.fieldOf("type").forGetter(t -> t.attributeTypeID),
		    MAP_CODEC.codec().fieldOf("modifier").forGetter(t -> t.attributeModifier),
		    LENIENT_SLOT_GROUP_CODEC.listOf().optionalFieldOf("required_equipment_slots", List.of())
		    .forGetter(t -> t.requiredEquipmentSlotTypes == null ? List.of() : Arrays.asList(t.requiredEquipmentSlotTypes)),
		    LENIENT_SLOT_GROUP_CODEC.listOf().optionalFieldOf("optional_equipment_slots", List.of())
		    .forGetter(t -> t.optionalEquipmentSlotTypes == null ? List.of() : Arrays.asList(t.optionalEquipmentSlotTypes)),
		    AccessorySlot.CODEC.listOf().optionalFieldOf("required_accessory_slots", List.of())
		        .forGetter(t -> t.requiredAccessorySlotTypes == null ? List.of() : Arrays.asList(t.requiredAccessorySlotTypes)),
		    AccessorySlot.CODEC.listOf().optionalFieldOf("optional_accessory_slots", List.of())
		        .forGetter(t -> t.optionalAccessorySlotTypes == null ? List.of() : Arrays.asList(t.optionalAccessorySlotTypes)),
		    AccessorySlot.Group.CODEC.listOf().optionalFieldOf("required_accessory_groups", List.of())
		        .forGetter(t -> t.requiredAccessoryGroupTypes == null ? List.of() : Arrays.asList(t.requiredAccessoryGroupTypes)),
		    AccessorySlot.Group.CODEC.listOf().optionalFieldOf("optional_accessory_groups", List.of())
		        .forGetter(t -> t.optionalAccessoryGroupTypes == null ? List.of() : Arrays.asList(t.optionalAccessoryGroupTypes)),
		    Codec.STRING.listOf().optionalFieldOf("required_curio_slots", List.of())
		        .forGetter(t -> t.requiredCurioSlotTypes == null ? List.of() : Arrays.asList(t.requiredCurioSlotTypes)),
		    Codec.STRING.listOf().optionalFieldOf("optional_curio_slots", List.of())
		        .forGetter(t -> t.optionalCurioSlotTypes == null ? List.of() : Arrays.asList(t.optionalCurioSlotTypes))
		).apply(instance, (type, modifier, reqEquip, optEquip, reqAccSlot, optAccSlot, reqAccGroup, optAccGroup, reqCurio, optCurio) ->
		    new AttributeTemplate(
		        type, modifier,
		        reqEquip.toArray(new EquipmentSlotGroup[0]),
		        optEquip.toArray(new EquipmentSlotGroup[0]),
		        reqAccSlot.toArray(new AccessorySlot[0]),
		        optAccSlot.toArray(new AccessorySlot[0]),
		        reqAccGroup.toArray(new AccessorySlot.Group[0]),
		        optAccGroup.toArray(new AccessorySlot.Group[0]),
		        reqCurio.toArray(new String[0]),
		        optCurio.toArray(new String[0])
		    )
		));

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
    	List<EquipmentSlot> slots = new ArrayList<EquipmentSlot>();
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
    	List<EquipmentSlot> slots = new ArrayList<EquipmentSlot>();
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
        realize(actions, Reforged.MODIFIERS[slot.ordinal()]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, EquipmentSlot slot) {
        realize(actions, Reforged.MODIFIERS[slot.ordinal()]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, AccessorySlot slot) {
        realize(actions, Reforged.MODIFIERS[slot.getIndex()+6]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, AccessorySlot.Group slot) {
        realize(actions, Reforged.MODIFIERS[slot.ordinal()+15]);
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    public void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, String slot, int index) {
        realize(actions, Reforged.CURIO_MODIFIERS.getOrDefault(slot, VersionHelper.toLoc("tiered",slot).withSuffix("_"+index)));
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param actions  map to add {@link AttributeTemplate}
     * @param slot
     */
    private void realize(BiConsumer<Holder<Attribute>, AttributeModifier> actions, Identifier id) {
    	AttributeModifier cloneModifier = new AttributeModifier(
    			id.withPrefix("tiered_"+attributeModifier.id().getPath()),
                attributeModifier.amount(),
                attributeModifier.operation()
        );

    	Optional<Reference<Attribute>> key = BuiltInRegistries.ATTRIBUTE.get((VersionHelper.AttributeHelper.backportAttribute(attributeTypeID)));
//        Holder<Attribute> key = RegistryHelper.getAttribute(VersionHelper.toLoc(attributeTypeID));
        if(key == null || key.isEmpty()) {
            Reforged.LOGGER.warn(String.format("%s was referenced as an attribute type, but it does not exist! A data file in /tiered/item_attributes/ has an invalid type property.", attributeTypeID));
        } else {
            actions.accept(key.get(), cloneModifier);
        }
    }
}
