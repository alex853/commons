package net.simforge.commons.gckls2com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Arrays;

import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.commons.legacy.html.Html;

public class GC {
    public static final File CSV = new File("./gckls2com.csv");

    public static GCAirport findAirport(String code) throws IOException {
        GCAirport airport = loadAirport(code);
        if (airport != null) {
            return airport;
        }

        airport = downloadAirport(code);
        if (airport != null) {
            saveAirport(airport);
        }

        return airport;
    }

    private static GCAirport loadAirport(String code) throws IOException {
        Csv csv = CSV.exists() ? Csv.load(CSV) : new Csv();

        GCAirport airport = findAirport(csv, "ICAO", code);
        if (airport != null) {
            return airport;
        }

        airport = findAirport(csv, "IATA", code);
        if (airport != null) {
            return airport;
        }

        return null;
    }

    private static GCAirport findAirport(Csv csv, String field, String code) {
        for (int row = 0; row < csv.rowCount(); row++) {
            if (code.equals(csv.value(row, field))) {
                GCAirport airport = new GCAirport();

                airport.setIcao(csv.value(row, "ICAO"));
                airport.setIata(csv.value(row, "IATA"));
                airport.setName(csv.value(row, "Name"));
                airport.setCity(csv.value(row, "City"));
                airport.setLat(Double.parseDouble(csv.value(row, "Latitude")));
                airport.setLon(Double.parseDouble(csv.value(row, "Longitude")));
                airport.setTimezone1(csv.value(row, "Timezone1"));
                airport.setTimezone2(csv.value(row, "Timezone2"));

                return airport;
            }
        }

        return null;
    }

    private static void saveAirport(GCAirport airport) throws IOException {
        File file = new File("./gckls2com.csv");
        Csv csv = file.exists() ? Csv.load(file) : new Csv();

        if (!file.exists()) {
            csv.addColumn("ICAO");
            csv.addColumn("IATA");
            csv.addColumn("Name");
            csv.addColumn("City");
            csv.addColumn("Latitude");
            csv.addColumn("Longitude");
            csv.addColumn("Timezone1");
            csv.addColumn("Timezone2");
        }

        int row = csv.addRow();

        csv.set(row, "ICAO", airport.getIcao());
        csv.set(row, "IATA", airport.getIata());
        csv.set(row, "Name", airport.getName());
        csv.set(row, "City", airport.getCity());
        csv.set(row, "Latitude", String.valueOf(airport.getLat()));
        csv.set(row, "Longitude", String.valueOf(airport.getLon()));
        csv.set(row, "Timezone1", airport.getTimezone1());
        csv.set(row, "Timezone2", airport.getTimezone2());

        IOHelper.saveFile(file, csv.getContent());
    }

    public static GCAirport downloadAirport(String code) throws IOException {
        String urlStr = "http://gc.kls2.com/airport/" + code;
        URL url = new URL(urlStr);
        URLConnection urlConnx = url.openConnection();
        InputStream is = urlConnx.getInputStream();
        URL actualUrl = urlConnx.getURL();
        // if we've been redirected then site could not find airport
        if (!urlStr.equals(actualUrl.toString())) {
            is.close();
            throw new IllegalStateException("Could not find strict airport by code '" + code + "'");
        }
        String content = IOHelper.readInputStream(is);
        is.close();

        String str = Html.toPlainText(content);
        String[] strsArr = str.split("[\n|\r\n]");
        List<String> strs = Arrays.asList(strsArr);
        int i = strs.indexOf("Location");
        if (i == -1) {
            throw new IllegalStateException("Could not find 'Location' header");
        }
        i++;
        GCAirport airport = new GCAirport();
        for (; i < strs.size(); i++) {
            String s = strs.get(i);
            if (s.startsWith("City:")) {
                airport.setCity(r(s.substring("City:".length())).trim());
            } else if (s.startsWith("Name:")) {
                airport.setName(r(s.substring("Name:".length())).trim());
            } else if (s.startsWith("ICAO / IATA:")) {
                s = r(s.substring("ICAO / IATA:".length())).trim();
                int j = s.indexOf('/');
                if (j == -1) {
                    throw new IllegalStateException("Could not separate codes to ICAO and IATA");
                }
                airport.setIcao(s.substring(0, j).trim());
                airport.setIata(s.substring(j + 1).trim());
            } else if (s.startsWith("ICAO:")) {
                airport.setIcao(r(s.substring("ICAO:".length())).trim());
            } else if (s.startsWith("IATA:")) {
                airport.setIata(r(s.substring("IATA:".length())).trim());
            } else if (s.startsWith("Latitude:")) {
                String v = r(s.substring("Latitude:".length())).trim();
                int j = v.indexOf('(');
                int k = v.indexOf(')');
                if (j >= 0 && k >= 0) {
                    airport.setLat(Double.parseDouble(v.substring(j + 1, k).trim()));
                } else {
                    throw new IllegalStateException("Could not parse latitude '" + v + "'");
                }
            } else if (s.startsWith("Longitude:")) {
                String v = r(s.substring("Longitude:".length())).trim();
                int j = v.indexOf('(');
                int k = v.indexOf(')');
                if (j >= 0 && k >= 0) {
                    airport.setLon(Double.parseDouble(v.substring(j + 1, k).trim()));
                } else {
                    throw new IllegalStateException("Could not parse longitude '" + v + "'");
                }
            } else if (s.startsWith("Timezone:")) {
                String v = r(s.substring("Timezone:".length())).trim();
                String tz1;
                String tz2;
                int j = v.indexOf('(');
                int k = v.indexOf(')');
                if (j == -1 && k == -1) {
                    tz1 = v;
                    tz2 = null;
                } else if (j >= 0 && k >= 0) {
                    tz1 = v.substring(0, j).trim();
                    tz2 = v.substring(j + 1, k);
                } else {
                    throw new IllegalStateException("Could not parse timezones '" + v + "'");
                }
                airport.setTimezone1(tz1);
                airport.setTimezone2(tz2);
            }
        }

        return airport;
    }

    private static String r(String s) {
        return s.replace(';', ' ');
    }
}
