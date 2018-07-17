package net.simforge.commons.misc;

import net.simforge.commons.io.IOHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class IOHelperTest {

    @Test
    public void testCopyStream() throws IOException {
        String sourceContent = Str.z(1234567890, 100000);
        byte[] sourceBytes = sourceContent.getBytes();

        ByteArrayInputStream bais = new ByteArrayInputStream(sourceBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOHelper.copyStream(bais, baos);

        byte[] resultedBytes = baos.toByteArray();
        assertEquals(sourceBytes.length, resultedBytes.length);
        assertArrayEquals(sourceBytes, resultedBytes);
    }

}
