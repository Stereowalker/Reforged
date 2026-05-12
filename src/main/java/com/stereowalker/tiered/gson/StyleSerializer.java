package com.stereowalker.tiered.gson;

import java.lang.reflect.Type;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.IdentifierException;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class StyleSerializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
    @Override
    @Nullable
    public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject == null) {
                return null;
            }
            Boolean boolean_ = getOptionalFlag(jsonObject, "bold");
            Boolean boolean2 = getOptionalFlag(jsonObject, "italic");
            Boolean boolean3 = getOptionalFlag(jsonObject, "underlined");
            Boolean boolean4 = getOptionalFlag(jsonObject, "strikethrough");
            Boolean boolean5 = getOptionalFlag(jsonObject, "obfuscated");
            TextColor textColor = getTextColor(jsonObject);
            String string = getInsertion(jsonObject);
//            ClickEvent clickEvent = getClickEvent(jsonObject);
//            HoverEvent hoverEvent = getHoverEvent(jsonObject);
            Identifier resourceLocation = getFont(jsonObject);
            return Style.EMPTY
            		.withColor(textColor).withBold(boolean_).withItalic(boolean2)
            		.withUnderlined(boolean3).withStrikethrough(boolean4).withObfuscated(boolean5)
            		.withInsertion(string).withFont(new FontDescription.Resource(resourceLocation));
//            return new Style(textColor, boolean_, boolean2, boolean3, boolean4, boolean5, clickEvent, hoverEvent, string, resourceLocation);
        }
        return null;
    }

    @Nullable
    private static Identifier getFont(JsonObject json) {
        if (json.has("font")) {
            String string = GsonHelper.getAsString(json, "font");
            try {
                return VersionHelper.toLoc(string);
            }
            catch (IdentifierException resourceLocationException) {
                throw new JsonSyntaxException("Invalid font name: " + string);
            }
        }
        return null;
    }

//    @Nullable
//    private static HoverEvent getHoverEvent(JsonObject json) {
//        JsonObject jsonObject;
//        HoverEvent hoverEvent;
//        if (json.has("hoverEvent") && (hoverEvent = HoverEvent.deserialize(jsonObject = GsonHelper.getAsJsonObject(json, "hoverEvent"))) != null && hoverEvent.getAction().isAllowedFromServer()) {
//            return hoverEvent;
//        }
//        return null;
//    }

//    @Nullable
//    private static ClickEvent getClickEvent(JsonObject json) {
//        if (json.has("clickEvent")) {
//            JsonObject jsonObject = GsonHelper.getAsJsonObject(json, "clickEvent");
//            String string = GsonHelper.getAsString(jsonObject, "action", null);
//            ClickEvent.Action action = string == null ? null : ClickEvent.Action.getByName(string);
//            String string2 = GsonHelper.getAsString(jsonObject, "value", null);
//            if (action != null && string2 != null && action.isAllowedFromServer()) {
//                return new ClickEvent(action, string2);
//            }
//        }
//        return null;
//    }

    @Nullable
    private static String getInsertion(JsonObject json) {
        return GsonHelper.getAsString(json, "insertion", null);
    }

    @Nullable
    private static TextColor getTextColor(JsonObject json) {
        if (json.has("color")) {
            String string = GsonHelper.getAsString(json, "color");
            return TextColor.parseColor(string).getOrThrow();
        }
        return null;
    }

    @Nullable
    private static Boolean getOptionalFlag(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return json.get(memberName).getAsBoolean();
        }
        return null;
    }

    @Override
    @Nullable
    public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
        if (style.isEmpty()) {
            return null;
        }
        JsonObject jsonObject = new JsonObject();
//        if (style.bold != null) {
//            jsonObject.addProperty("bold", style.bold);
//        }
//        if (style.italic != null) {
//            jsonObject.addProperty("italic", style.italic);
//        }
//        if (style.underlined != null) {
//            jsonObject.addProperty("underlined", style.underlined);
//        }
//        if (style.strikethrough != null) {
//            jsonObject.addProperty("strikethrough", style.strikethrough);
//        }
//        if (style.obfuscated != null) {
//            jsonObject.addProperty("obfuscated", style.obfuscated);
//        }
        if (style.getColor() != null) {
            jsonObject.addProperty("color", style.getColor().serialize());
        }
//        if (style.insertion != null) {
//            jsonObject.add("insertion", jsonSerializationContext.serialize(style.insertion));
//        }
//        if (style.clickEvent != null) {
//            JsonObject jsonObject2 = new JsonObject();
//            jsonObject2.addProperty("action", style.clickEvent.getAction().getName());
//            jsonObject2.addProperty("value", style.clickEvent.getValue());
//            jsonObject.add("clickEvent", jsonObject2);
//        }
//        if (style.hoverEvent != null) {
//            jsonObject.add("hoverEvent", style.hoverEvent.serialize());
//        }
//        if (style.font != null) {
//            jsonObject.addProperty("font", style.font.toString());
//        }
        return jsonObject;
    }

//    @Override
//    @Nullable
//    public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
//        return this.serialize((Style)object, type, jsonSerializationContext);
//    }
//
//    @Override
//    @Nullable
//    public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//        return this.deserialize(jsonElement, type, jsonDeserializationContext);
//    }
}
