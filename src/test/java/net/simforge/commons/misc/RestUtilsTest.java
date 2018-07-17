package net.simforge.commons.misc;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RestUtilsTest {
    @Test
    public void test1() throws IOException {
        String json = new Gson().toJson(RestUtils.success());
        RestUtils.Response response = RestUtils.parseResponse(json);

        assertTrue(response.isSuccess());
        assertNull(response.getMessage());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    public void test2() throws IOException {
        String json = new Gson().toJson(RestUtils.failure("some error"));
        RestUtils.Response response = RestUtils.parseResponse(json);

        assertFalse(response.isSuccess());
        assertEquals("some error", response.getMessage());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    public void test3() throws IOException {
        List<String> someObjects = Arrays.asList("hello", "world");

        String json = new Gson().toJson(RestUtils.success(someObjects));
        RestUtils.Response<String> response = RestUtils.parseResponse(json);

        assertTrue(response.isSuccess());
        assertNull(response.getMessage());

        List<String> data = response.getData();
        assertFalse(data.isEmpty());
        assertEquals(2, data.size());
        assertEquals("hello", data.get(0));
        assertEquals("world", data.get(1));
    }
}
