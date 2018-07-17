package net.simforge.commons.io;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

public class TestCsv extends TestCase {
    public static void testEmpty() {
        Csv csv = Csv.empty();
        assertEquals(0, csv.columnCount());
        assertEquals(0, csv.rowCount());
    }

    public static void testEmptyFilling() {
        Csv csv = Csv.empty();

        csv.addColumn("column1");
        assertEquals(1, csv.columnCount());

        int rowIndex = csv.addRow();
        assertEquals(0, csv.rowWidth(0));

        csv.set(rowIndex, "column1", "value1");
        assertEquals(1, csv.rowCount());
        assertEquals(1, csv.rowWidth(rowIndex));
        assertEquals("value1", csv.value(rowIndex, "column1"));
    }

    public static void testGetContentFromContent() {
        Csv csv = Csv.empty();

        csv.addColumn("column1");
        int rowIndex = csv.addRow();
        csv.set(rowIndex, "column1", "value1");

        String content = csv.getContent();

        csv = Csv.fromContent(content);
        assertEquals(1, csv.columnCount());
        assertEquals(1, csv.rowCount());
        assertEquals(1, csv.rowWidth(rowIndex));
        assertEquals("value1", csv.value(rowIndex, "column1"));
    }

    public static void testFromContentFilling() {
        Csv csv = Csv.empty();

        csv.addColumn("column1");
        int rowIndex = csv.addRow();
        csv.set(rowIndex, "column1", "value1");

        String content = csv.getContent();

        csv = Csv.fromContent(content);

        csv.addColumn("column2");
        csv.set(rowIndex, "column2", "value2");
        csv.set(rowIndex, "column1", "newValue2");

        int secondRowIndex = csv.addRow();
        csv.set(secondRowIndex, "column2", "value22");

        assertEquals(2, csv.columnCount());
        assertEquals(2, csv.rowCount());
        assertEquals(2, csv.rowWidth(rowIndex));
        assertEquals(2, csv.rowWidth(secondRowIndex));
        assertEquals("newValue2", csv.value(rowIndex, "column1"));
        assertEquals("value2", csv.value(rowIndex, "column2"));
        assertEquals(null, csv.value(secondRowIndex, "column1"));
        assertEquals("value22", csv.value(secondRowIndex, "column2"));
    }

    public void testLoadTestCsv1() throws IOException {
        URL url = TestCsv.class.getResource("/net/simforge/commons/io/TestCsv1.csv");
        String content = IOHelper.readInputStream(url.openStream());

        Csv csv = Csv.fromContent(content);

        assertEquals(3, csv.columnCount());
        assertEquals(9, csv.rowCount());

        assertEquals(2, csv.rowWidth(0));
        assertEquals("Value11", csv.value(0, "Column1"));
        assertEquals("Value12", csv.value(0, "Column2"));

        assertEquals(3, csv.rowWidth(1));
        assertEquals("Value21", csv.value(1, "Column1"));
        assertEquals("Value22", csv.value(1, "Column2"));
        assertEquals("", csv.value(1, "Column3"));

        assertEquals(2, csv.rowWidth(2));
        assertEquals("", csv.value(2, "Column1"));
        assertEquals("Value32", csv.value(2, "Column2"));

        assertEquals(3, csv.rowWidth(3));
        assertEquals("Value41", csv.value(3, "Column1"));
        assertEquals("Value42", csv.value(3, "Column2"));
        assertEquals("Value43", csv.value(3, "Column3"));

        assertEquals(3, csv.rowWidth(4));
        assertEquals("Value51", csv.value(4, "Column1"));
        assertEquals("", csv.value(4, "Column2"));
        assertEquals("Value53", csv.value(4, "Column3"));

        assertEquals(0, csv.rowWidth(5));

        assertEquals(2, csv.rowWidth(6));
        assertEquals("Value71", csv.value(6, "Column1"));
        assertEquals("Value72", csv.value(6, "Column2"));

        assertEquals(2, csv.rowWidth(7));
        assertEquals("", csv.value(7, "Column1"));
        assertEquals("", csv.value(7, "Column2"));

        assertEquals(2, csv.rowWidth(8));
        assertEquals("Value91", csv.value(8, "Column1"));
        assertEquals("Value92", csv.value(8, "Column2"));
    }

    public void testLoadFile() throws IOException {
        String content = IOHelper.readInputStream(TestCsv.class.getResourceAsStream("/net/simforge/commons/io/TestCsv1.csv"));
        File tempFile = File.createTempFile("TestCsv", ".csv");
        IOHelper.saveFile(tempFile, content);

        Csv csv = Csv.load(tempFile);

        assertEquals(3, csv.columnCount());
        assertEquals(9, csv.rowCount());
    }

    public void testLoadFileNoHeaders() throws IOException {
        String content = IOHelper.readInputStream(TestCsv.class.getResourceAsStream("/net/simforge/commons/io/TestCsv1.csv"));
        File tempFile = File.createTempFile("TestCsv", ".csv");
        IOHelper.saveFile(tempFile, content);

        Csv csv = Csv.loadNoHeaders(tempFile);

        assertEquals(0, csv.columnCount());
        assertEquals(10, csv.rowCount());
        assertEquals(-1, csv.columnIndex("Column1"));
    }

    public void testGckls2comCsv() throws IOException {
        URL url = TestCsv.class.getResource("/net/simforge/commons/io/gckls2com.csv");
        String content = IOHelper.readInputStream(url.openStream());

        Csv csv = Csv.fromContent(content);

        assertEquals(8, csv.columnCount());
        assertEquals(2, csv.rowCount());

        assertEquals(8, csv.rowWidth(0));
        assertEquals("Heathrow", csv.value(0, "Name"));
        assertEquals("London, Middlesex, England, United Kingdom", csv.value(0, "City"));

        assertEquals(8, csv.rowWidth(1));
        assertEquals("Sofia International (Vrajdebna, Vrazhdebna)", csv.value(1, "Name"));
        assertEquals("Sofia, Sofia-Grad, Bulgaria", csv.value(1, "City"));
    }
}
