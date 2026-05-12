package com.stereowalker.tiered.api;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stereowalker.unionlib.util.GeneralUtilities.WeightedObject;

import net.minecraft.resources.Identifier;

public class TierPool implements WeightedObject {
	public static final Codec<TierPool> CODEC = RecordCodecBuilder.create(
			i -> i.group(
					Codec.INT.optionalFieldOf("weight", 0).forGetter(TierPool::getWeight),
					ItemVerifier.CODEC.listOf().fieldOf("verifiers").forGetter(TierPool::getVerifiers),
					ItemVerifier.CODEC.listOf().optionalFieldOf("exclusions", List.of()).forGetter(TierPool::getExclusions),
					Codec.STRING.listOf().fieldOf("tiers").forGetter(TierPool::getTiers)
					)
			.apply(i, (weight, verifiers, exclusions, tiers) ->
					new TierPool(null, weight, verifiers, exclusions, tiers))
			);

	private final int weight;
	private final List<ItemVerifier> verifiers;
	private final List<ItemVerifier> exclusions;

	private final List<String> tiers;

	public TierPool(String id, int weight, List<ItemVerifier> verifiers, List<ItemVerifier> exclusions, List<String> tiers) {
		this.weight = weight;
		this.verifiers = verifiers;
		this.exclusions = exclusions;
		this.tiers = tiers;
	}

	public int getWeight() {
		return weight;
	}

	public List<ItemVerifier> getVerifiers() {
		return verifiers;
	}

	public List<ItemVerifier> getExclusions() {
		return exclusions;
	}

	public boolean isValid(Identifier id) {
		if (exclusions != null)
			for(ItemVerifier exclusion : exclusions)
				if(exclusion.isValid(id)) return false;
		for(ItemVerifier verifier : verifiers)
			if(verifier.isValid(id)) return true;
		return false;
	}


	public List<String> getTiers() {
		return tiers;
	}
}
