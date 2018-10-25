package net.simforge.commons.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Csv {

    private char valueSeparator = ',';
    private boolean useHeaders = true;
    private List<String> headers = new ArrayList<>();
    private List<String[]> rows = new ArrayList<>();

    public static Csv empty() {
        return fromContent("");
    }

    public static Csv load(File file) throws IOException {
        return splitContent(IOHelper.loadFile(file), true);
    }

    public static Csv loadNoHeaders(File file) throws IOException {
        return splitContent(IOHelper.loadFile(file), false);
    }

    public static Csv fromContent(String content) {
        return splitContent(content, true);
    }

    private static Csv splitContent(String content, boolean useHeaders) {
        Csv csvFile = new Csv();
        csvFile.useHeaders = useHeaders;

        String[] strings = content.split("\n");

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.endsWith("\r")) {
                string = string.substring(0, string.length() - 1);
            }
            String[] values = csvFile.splitRow(string);
            csvFile.rows.add(values);
        }

        if (csvFile.useHeaders) {
            csvFile.headers = new ArrayList<>(Arrays.asList(csvFile.rows.remove(0)));
        }

        return csvFile;
    }

    private String[] splitRow(String string) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = null;
        boolean insideOfQuotes = false;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == valueSeparator) {
                if (!insideOfQuotes) {
                    result.add(sb != null ? sb.toString().trim() : "");
                    sb = new StringBuilder();
                    insideOfQuotes = false;
                } else {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(c);
                }
            } else if (c == '"') {
                if (sb.length() == 0) {
                    insideOfQuotes = true;
                } else {
                    result.add(sb != null ? sb.toString().trim() : "");
                    sb = new StringBuilder();
                    insideOfQuotes = false;
                    i++;
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(c);
            }
        }
        if (sb != null) {
            result.add(sb.toString().trim());
        }
        return result.toArray(new String[result.size()]);
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();
        if (useHeaders) {
            addRow(sb, headers.toArray(new String[headers.size()]));
        }
        for (String[] row : rows) {
            addRow(sb, row);
        }
        return sb.toString();
    }

    private void addRow(StringBuilder sb, String[] row) {
        for (int i = 0; i < row.length; i++) {
            String value = row[i];

            if (value != null) {
                boolean addQuotes = value.contains(",");

                if (addQuotes)
                    sb.append('"');
                sb.append(value);
                if (addQuotes)
                    sb.append('"');
            }

            if (i != row.length - 1)
                sb.append(valueSeparator);
            else
                sb.append("\r\n");
        }
    }

    public int rowCount() {
        return rows.size();
    }

    public int rowWidth(int rowIndex) {
        return rows.get(rowIndex).length;
    }

    public String value(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    public String value(int rowIndex, String column) {
        int columnIndex = columnIndex(column);
        if (columnIndex == -1) {
            return null;
        }
        return value(rowIndex, columnIndex);
    }

    public int columnCount() {
        return headers.size();
    }

    public int columnIndex(String column) {
        return headers.indexOf(column);
    }

    public void addColumn(String column) {
        int columnIndex = columnIndex(column);
        if (columnIndex != -1) {
            throw new IllegalArgumentException("Column '" + column + "' already exists");
        }
        headers.add(column);
    }

    public int addRow() {
        String[] row = new String[0];
        rows.add(row);
        return rows.indexOf(row);
    }

    public void set(int rowIndex, String column, String value) {
        String[] row = rows.get(rowIndex);
        int columnIndex = columnIndex(column);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column '" + column + "' is not found");
        }
        if (row.length <= columnIndex) {
            String[] newRow = new String[columnIndex + 1];
            System.arraycopy(row, 0, newRow, 0, row.length);
            rows.set(rowIndex, newRow);
            row = newRow;
        }
        row[columnIndex] = value;
    }
}
