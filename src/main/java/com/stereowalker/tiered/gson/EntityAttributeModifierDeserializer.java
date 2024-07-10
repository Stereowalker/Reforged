package com.stereowalker.tiered.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;


public class EntityAttributeModifierDeserializer implements JsonDeserializer<AttributeModifier> {

    private static final String JSON_ID_KEY = "id";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_OPERATION_KEY = "operation";

    @Override
    public AttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement id = getJsonElement(jsonObject, JSON_ID_KEY, "Entity Attribute Modifier requires an id!");
        JsonElement amount = getJsonElement(jsonObject, JSON_AMOUNT_KEY, "Entity Attribute Modifier requires an amount!");
        JsonElement operation = getJsonElement(jsonObject, JSON_OPERATION_KEY, "Entity Attribute Modifier requires an operation!");

        return new AttributeModifier(VersionHelper.toLoc(id.getAsString()), amount.getAsFloat(), AttributeModifier.Operation.valueOf(operation.getAsString().toUpperCase()));
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
