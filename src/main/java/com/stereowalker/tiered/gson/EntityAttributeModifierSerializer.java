package com.stereowalker.tiered.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

import java.lang.reflect.Type;

public class EntityAttributeModifierSerializer implements JsonSerializer<AttributeModifier> {

    @Override
    public JsonElement serialize(AttributeModifier src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("amount", src.getAmount());
        obj.addProperty("operation", opToSt(src.getOperation()));
        obj.addProperty("id", src.getName());
        return obj;
    }
    
    private String opToSt(Operation op) {
    	if (op == Operation.ADDITION) return "ADD_VALUE";
    	else if (op == Operation.MULTIPLY_TOTAL) return "ADD_MULTIPLIED_TOTAL";
    	else if (op == Operation.MULTIPLY_BASE) return "ADD_MULTIPLIED_BASE";
    	return "";
    }
}