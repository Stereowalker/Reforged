package com.stereowalker.tiered.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public class TextColorDeserializer implements JsonDeserializer<TextColor> {

    @Override
    public TextColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return TextColor.fromLegacyFormat(ChatFormatting.valueOf(json.getAsString().toUpperCase()));
    }
}
