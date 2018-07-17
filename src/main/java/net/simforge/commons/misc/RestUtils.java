package net.simforge.commons.misc;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class RestUtils {
    public static Map<String, Object> success() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    public static Map<String, Object> success(Collection data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    public static Map<String, Object> success(Map<String, Object> data) {
        return success("data", data);
    }

    public static Map<String, Object> success(String name, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put(name, data);
        return result;
    }

    public static Map<String, Object> failure(String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", msg);
        return result;
    }

    public static class Response<T> {
        private boolean success;
        private String message;
        private List<T> data = new ArrayList<>();

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<T> getData() {
            return Collections.unmodifiableList(data);
        }
    }

    public static <T> Response<T> parseResponse(String content) throws IOException {
        //noinspection unchecked
        return parseResponse(content, STRING_READER);
    }

    public static <T> Response<T> parseResponse(String content, DataReader<T> dataReader) throws IOException {
        Response<T> result = new Response<>();

        JsonReader jsonReader = new JsonReader(new StringReader(content));
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("success")) {
                result.success = jsonReader.nextBoolean();
            } else if (name.equals("message")) {
                result.message = jsonReader.nextString();
            } else if (name.equals("data")) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    result.data.add(dataReader.read(jsonReader));
                }
                jsonReader.endArray();
            }
        }

        return result;
    }

    public interface DataReader<T> {
        T read(JsonReader jsonReader) throws IOException;
    }

    public static final DataReader STRING_READER = JsonReader::nextString;
}
