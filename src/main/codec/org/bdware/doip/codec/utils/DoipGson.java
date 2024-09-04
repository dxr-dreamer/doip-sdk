/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.utils;

import com.google.gson.*;
import org.bdware.doip.codec.digitalObject.DoType;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;

import java.lang.reflect.Type;

public class DoipGson {
    private static Gson gson;

    public static Gson getDoipGson() {
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(DoType.class, new Json4DoType());
            gsonBuilder.registerTypeAdapter(DoipResponseCode.class, new Json4DoResponseCode());
            gson = gsonBuilder.create();
            return gson;
        }
        return gson;
    }


    static class Json4DoType implements JsonSerializer<DoType>, JsonDeserializer<DoType> {
        // 对象转为Json时调用,实现JsonSerializer<PackageState>接口
        @Override
        public JsonElement serialize(DoType type, Type arg1, JsonSerializationContext arg2) {
            return new JsonPrimitive(type.getName());
        }

        // json转为对象时调用,实现JsonDeserializer<PackageState>接口
        @Override
        public DoType deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            for (DoType t : DoType.values()) {
                if (t.getName().equals(json.getAsString())) {
                    return t;
                }
            }
            return DoType.UnKnown;
        }
    }

    static class Json4DoResponseCode implements JsonSerializer<DoipResponseCode>, JsonDeserializer<DoipResponseCode> {
        // 对象转为Json时调用,实现JsonSerializer<PackageState>接口
        @Override
        public JsonElement serialize(DoipResponseCode type, Type arg1, JsonSerializationContext arg2) {
            return new JsonPrimitive(type.getName());
        }

        // json转为对象时调用,实现JsonDeserializer<PackageState>接口
        @Override
        public DoipResponseCode deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            for (DoipResponseCode t : DoipResponseCode.values()) {
                if (t.getName().equals(json.getAsString())) {
                    return t;
                }
            }
            return DoipResponseCode.MoreThanOneErrors;
        }
    }
}
