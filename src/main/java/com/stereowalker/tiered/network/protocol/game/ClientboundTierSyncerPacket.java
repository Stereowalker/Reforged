package com.stereowalker.tiered.network.protocol.game;

import static com.stereowalker.reforged.Reforged.TIER_DATA;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.stereowalker.tiered.Reforged;
import com.stereowalker.tiered.api.PotentialAttribute;
import com.stereowalker.tiered.data.TierDataLoader;
import com.stereowalker.unionlib.network.protocol.game.ClientboundUnionPacket;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientboundTierSyncerPacket extends ClientboundUnionPacket {
    public int size;
    public Map<ResourceLocation, PotentialAttribute> attribute;
    public static final Map<ResourceLocation, PotentialAttribute> CACHED_ATTRIBUTES = new HashMap<>();

    public ClientboundTierSyncerPacket(Map<ResourceLocation, PotentialAttribute> attribute) {
    	super(Reforged.instance.channel);
    	this.attribute = attribute;
        this.size = attribute.size();
    }

	public ClientboundTierSyncerPacket(FriendlyByteBuf buf) {
		super(buf, Reforged.instance.channel);
		this.size = buf.readInt();
		this.attribute = Maps.newHashMap();
        for (int i = 0; i < this.size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            PotentialAttribute pa = TierDataLoader.GSON.fromJson(buf.readUtf(), PotentialAttribute.class);
            this.attribute.put(id, pa);
        }
	}

	@Override
	public void encode(final FriendlyByteBuf buf) {
        buf.writeInt(this.size);

        this.attribute.forEach((id, attribute) -> {
            buf.writeResourceLocation(id);
            buf.writeUtf(TierDataLoader.GSON.toJson(attribute));
        });
	}

	@Override
	public boolean handleOnClient(LocalPlayer player) {
		CACHED_ATTRIBUTES.putAll(TIER_DATA.getTiers());
		TIER_DATA.clear();

		TIER_DATA.replace(this.attribute);
        if (TIER_DATA.getTiers().size() == 0) {
        	TIER_DATA.replace(CACHED_ATTRIBUTES);
        }
		return true;
	}

	public static ResourceLocation id = VersionHelper.toLoc("tiered", "tier_sync");
	@Override
	public ResourceLocation id() {
		return id;
	}
}
