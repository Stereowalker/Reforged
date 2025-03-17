package com.stereowalker.tiered.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;


public class EntityAttributeModifierDeserializer implements JsonDeserializer<AttributeModifier> {

    private static final String JSON_NAME_KEY = "id";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_OPERATION_KEY = "operation";

    @Override
    public AttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement name = getJsonElement(jsonObject, JSON_NAME_KEY, "Entity Attribute Modifier requires a name!");
        JsonElement amount = getJsonElement(jsonObject, JSON_AMOUNT_KEY, "Entity Attribute Modifier requires an amount!");
        JsonElement operation = getJsonElement(jsonObject, JSON_OPERATION_KEY, "Entity Attribute Modifier requires an operation!");

        return new AttributeModifier(name.getAsString(), amount.getAsFloat(), AttributeModifier.Operation.valueOf(stToOp(operation.getAsString().toUpperCase())));
    }
    
    private String stToOp(String op) {
    	if (op.equals("ADD_VALUE")) return "ADDITION";
    	else if (op.equals("ADD_MULTIPLIED_TOTAL")) return "MULTIPLY_TOTAL";
    	else if (op.equals("ADD_MULTIPLIED_BASE")) return "MULTIPLY_BASE";
    	return op;
    }

    private JsonElement getJsonElement(JsonObject jsonObject, String jsonNameKey, String s) {
        JsonElement name;

        if (jsonObject.has(jsonNameKey)) {
            name = jsonObject.get(jsonNameKey);
        } else {
            throw new JsonParseException(s);
        }

        return name;
    }
}
