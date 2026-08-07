package com.stereowalker.tiered.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.tiered.api.TierPool;
import com.stereowalker.unionlib.resource.ReloadListener;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class PoolDataLoader extends SimpleJsonResourceReloadListener<TierPool> implements ReloadListener {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading recipe {}";
    private static final String LOADED_RECIPES_MESSAGE = "Loaded {} item pools";
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<Identifier, TierPool> itemPools = new HashMap<>();

    public PoolDataLoader() {
        super(TierPool.CODEC, FileToIdConverter.json("tiered_modifiers/pools"));
    }

    @Override
    protected void apply(Map<Identifier, TierPool> loader, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, TierPool> readItemPools = Maps.newHashMap();

//        for (Entry<Identifier, TierPool> entry : loader.entrySet()) {
//            Identifier identifier = entry.getKey();
//
//            try {
//            	TierPool itemPool = GSON.fromJson(entry.getValue(), TierPool.class);
//            	if (!itemPool.getTiers().isEmpty())
//            		readItemPools.put(identifier, itemPool);
//            } catch (IllegalArgumentException | JsonParseException exception) {
//                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
//            }
//        }

        itemPools = loader;
        LOGGER.info(LOADED_RECIPES_MESSAGE, loader.size());
    }

    /**
     * Returns a list of potential item attributes ({@link PotentialAttribute}) read from "data/modid/item_attributes".
     *
     * @return  list of potential read item attributes
     */
    public Map<Identifier, TierPool> getPools() {
        return itemPools;
    }
    public void clear() {
        itemPools.clear();
    }
    public void replace(Map<Identifier, TierPool> i){
        itemPools = i;
    }

	@Override
	public Identifier id() {
		return VersionHelper.toLoc("tiered", "pool_data");
	}
}
