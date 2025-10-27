package com.stereowalker.tiered.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.tiered.gson.AccessoryGroupDeserializer;
import com.stereowalker.tiered.gson.AccessorySlotDeserializer;
import com.stereowalker.tiered.gson.EntityAttributeModifierDeserializer;
import com.stereowalker.tiered.gson.EntityAttributeModifierSerializer;
import com.stereowalker.tiered.gson.EquipmentSlotDeserializer;
import com.stereowalker.unionlib.resource.ReloadListener;
import com.stereowalker.unionlib.util.VersionHelper;
import com.stereowalker.unionlib.world.entity.AccessorySlot;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class TierDataLoader extends SimpleJsonResourceReloadListener implements ReloadListener {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(AttributeModifier.class, new EntityAttributeModifierDeserializer())
            .registerTypeAdapter(AttributeModifier.class, new EntityAttributeModifierSerializer())
            .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
            .registerTypeAdapter(AccessorySlot.class, new AccessorySlotDeserializer())
            .registerTypeAdapter(AccessorySlot.Group.class, new AccessoryGroupDeserializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .create();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading recipe {}";
    private static final String LOADED_RECIPES_MESSAGE = "Loaded {} item tiers";
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<ResourceLocation, PotentialAttribute> itemAttributes = new HashMap<>();

    public TierDataLoader() {
        super(GSON, "tiered_modifiers/tiers");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, PotentialAttribute> readItemAttributes = Maps.newHashMap();

        for (Map.Entry<ResourceLocation, JsonElement> entry : loader.entrySet()) {
            ResourceLocation identifier = entry.getKey();

            try {
                PotentialAttribute itemAttribute = GSON.fromJson(entry.getValue(), PotentialAttribute.class);
                readItemAttributes.put(identifier, itemAttribute);
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }

        itemAttributes = readItemAttributes;
        LOGGER.info(LOADED_RECIPES_MESSAGE, readItemAttributes.size());
    }

    /**
     * Returns a list of potential item attributes ({@link PotentialAttribute}) read from "data/modid/tiered_modifiers/tiers".
     *
     * @return  list of potential read item attributes
     */
    public Map<ResourceLocation, PotentialAttribute> getTiers() {
        return itemAttributes;
    }
    public void clear() {
        itemAttributes.clear();
        LOGGER.info("Tier Data CLEAR");
    }
    public void replace(Map<ResourceLocation, PotentialAttribute> i){
        itemAttributes = i;
        LOGGER.info("Tier Data REPLACE");
    }

	@Override
	public ResourceLocation id() {
		return VersionHelper.toLoc("tiered", "data_loader");
	}
}
