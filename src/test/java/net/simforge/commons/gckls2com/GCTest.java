package net.simforge.commons.gckls2com;

import junit.framework.TestCase;

public class GCTest extends TestCase {
    public void testEGLL() throws Exception {
        // remove CSV to force downloading
        //noinspection ResultOfMethodCallIgnored
        GC.CSV.delete();

        GCAirport egll = GC.findAirport("EGLL");
        assertNotNull(egll);
        assertEquals("EGLL", egll.getIcao());
        assertEquals("LHR", egll.getIata());
        assertEquals("Heathrow Airport", egll.getName());
        assertEquals("London, Middlesex, England, United Kingdom", egll.getCity());
        assertEquals(51.4775, egll.getLat());
        assertEquals(-0.461388, egll.getLon());
    }

    public void testEGLLfromCSV() throws Exception {
        // it downloads and saves to CSV
        GCAirport egll = GC.findAirport("EGLL");
        // it also loads/saves CSV file
        GC.findAirport("EGKK");

        egll = GC.findAirport("EGLL");
        assertNotNull(egll);
        assertEquals("EGLL", egll.getIcao());
        assertEquals("LHR", egll.getIata());
        assertEquals("Heathrow Airport", egll.getName());
        assertEquals("London, Middlesex, England, United Kingdom", egll.getCity());
        assertEquals(51.4775, egll.getLat());
        assertEquals(-0.461388, egll.getLon());
    }
}
