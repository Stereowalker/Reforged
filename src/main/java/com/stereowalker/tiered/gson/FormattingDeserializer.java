package com.stereowalker.tiered.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.ChatFormatting;


public class FormattingDeserializer implements JsonDeserializer<ChatFormatting> {

    @Override
    public ChatFormatting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ChatFormatting.valueOf(json.getAsString().toUpperCase());
    }
}
