package net.simforge.commons.misc;

import junit.framework.TestCase;

public class TestGeo extends TestCase {

    private static final Geo.Coords BIG_BEN = new Geo.Coords(51.500613, -0.124551);
    private static final Geo.Coords EIFFEL = new Geo.Coords(48.857749, 2.294852);
    private static final Geo.Coords CAMBRIDGE = new Geo.Coords(52.205, 0.119);
    private static final Geo.Coords CENTRO_DE_PARIS = new Geo.Coords(48.857, 2.351);

    public void testDistance_BigBang_Eiffel() {
        assertEquals(183.9, Geo.distance(BIG_BEN, EIFFEL), 0.1);
    }

    public void testBearing_BigBan_Eiffel() {
        assertEquals(148.7, Geo.bearing(BIG_BEN, EIFFEL), 0.1);
    }

    public void testDestination_BigBan_Eiffel() {
        Geo.Coords calculatedEiffelPosition = Geo.destination(BIG_BEN, 148.7, 183.9);
        assertEquals(0, Geo.distance(EIFFEL, calculatedEiffelPosition), 0.1);
    }

    public void testBearing_Cambridge_CentroDeParis() {
        assertEquals(156.2, Geo.bearing(CAMBRIDGE, CENTRO_DE_PARIS), 0.1);
    }

    public void testBearing_CentroDeParis_Cambridge() {
        assertEquals(337.9, Geo.bearing(CENTRO_DE_PARIS, CAMBRIDGE), 0.1);
    }

    public void testReverseBearing() {
        assertEquals(101.23, Geo.reverseBearing(281.23), 0.001);
    }

    public void testNmToMeters() {
        assertEquals(1853, Geo.nmToMeters(1.0), 1);
    }

    public void testNmToKm() {
        assertEquals(1.853, Geo.nmToKm(1.0), 0.001);
    }

    public void testNmToKmToKm() {
        assertEquals(1.0, Geo.kmToNm(Geo.nmToKm(1.0)), 0.000001);
    }

    public void testDestination_BigBen_100nm() {
        Geo.Coords destination = Geo.destination(BIG_BEN, 123, 100);
        assertEquals(0, Geo.distance(new Geo.Coords(50.574637, 2.070103), destination), 0.25);
    }

    public void testGeoCoords_equals() {
        assertTrue(new Geo.Coords(50, 50).isSame(new Geo.Coords(50, 50)));
    }

    public void testGeoCoords_equalsAlmostSame() {
        assertTrue(new Geo.Coords(50, 50).isSame(new Geo.Coords(50.0000001, 50.00000001)));
    }

    public void testGeoCoords_notEquals() {
        assertFalse(new Geo.Coords(50, 50).isSame(new Geo.Coords(30, 30)));
    }

    public void testBearing_EGLLtoEFHK() {
        Geo.Coords egll = new Geo.Coords(51.4775, -0.461388);
        Geo.Coords efhk = new Geo.Coords(60.317222, 24.963333);

        double distance = Geo.distance(egll, efhk);
        double bearing = Geo.bearing(egll, efhk);

        Geo.Coords calculatedEfhk = Geo.destination(egll, bearing, distance);
        double delta = Geo.distance(efhk, calculatedEfhk);

        assertTrue(delta < 0.1);
    }
}
