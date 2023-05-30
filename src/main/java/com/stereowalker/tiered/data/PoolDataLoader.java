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
import com.stereowalker.tiered.api.TierPool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class PoolDataLoader extends SimpleJsonResourceReloadListener {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading recipe {}";
    private static final String LOADED_RECIPES_MESSAGE = "Loaded {} item attributes";
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<ResourceLocation, TierPool> itemPools = new HashMap<>();

    public PoolDataLoader() {
        super(GSON, "tier_pools");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, TierPool> readItemPools = Maps.newHashMap();

        for (Map.Entry<ResourceLocation, JsonElement> entry : loader.entrySet()) {
            ResourceLocation identifier = entry.getKey();

            try {
            	TierPool itemPool = GSON.fromJson(entry.getValue(), TierPool.class);
            	if (!itemPool.getTiers().isEmpty())
            		readItemPools.put(new ResourceLocation(itemPool.getID()), itemPool);
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }

        itemPools = readItemPools;
        LOGGER.info(LOADED_RECIPES_MESSAGE, readItemPools.size());
    }

    /**
     * Returns a list of potential item attributes ({@link PotentialAttribute}) read from "data/modid/item_attributes".
     *
     * @return  list of potential read item attributes
     */
    public Map<ResourceLocation, TierPool> getPools() {
        return itemPools;
    }
    public void clear() {
        itemPools.clear();
    }
    public void replace(Map<ResourceLocation, TierPool> i){
        itemPools = i;
    }
}
