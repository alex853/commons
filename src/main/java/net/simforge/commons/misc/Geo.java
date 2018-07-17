package net.simforge.commons.misc;

/**
 * All distances are measured in nautical miles.
 * All bearings are measured in degrees.
 * <p>
 * See also http://movable-type.co.uk/scripts/latlong.html
 */
public class Geo {
    private static double KMtoNM_coeff = 1.852;

    private static double R_earth_KM = 6371;
    private static double R_earth_NM = R_earth_KM / KMtoNM_coeff;

    public static double distance(Geo.Coords p1, Geo.Coords p2) {
        double lat1 = Math.toRadians(p1.lat);
        double long1 = Math.toRadians(p1.lon);

        double lat2 = Math.toRadians(p2.lat);
        double long2 = Math.toRadians(p2.lon);

        double x1 = Math.cos(long1) * Math.cos(lat1);
        double y1 = Math.sin(long1) * Math.cos(lat1);
        double z1 = Math.sin(lat1);

        double x2 = Math.cos(long2) * Math.cos(lat2);
        double y2 = Math.sin(long2) * Math.cos(lat2);
        double z2 = Math.sin(lat2);

        double l1 = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
        double l2 = Math.sqrt(x2 * x2 + y2 * y2 + z2 * z2);

        double scalarmul = (x1 * x2 + y1 * y2 + z1 * z2) / l1 / l2;

        if (scalarmul > 1.0) {
            scalarmul = 1.0;
        }

        if (scalarmul < -1.0) {
            scalarmul = -1.0;
        }

        double alpha = Math.acos(scalarmul);
        //noinspection UnnecessaryLocalVariable
        double distance = alpha * R_earth_NM;

        return distance;
    }

    public static double bearing(Geo.Coords p1, Geo.Coords p2) {
        double fi1 = Math.toRadians(p1.lat);
        double fi2 = Math.toRadians(p2.lat);

        double dLambda = Math.toRadians(p2.lon - p1.lon);

        double y = Math.sin(dLambda) * Math.cos(fi2);
        double x = Math.cos(fi1) * Math.sin(fi2) - Math.sin(fi1) * Math.cos(fi2) * Math.cos(dLambda);

        double tetta = Math.atan2(y, x);

        return (Math.toDegrees(tetta) + 360) % 360;
    }

    public static Coords destination(Geo.Coords p, double bearing, double distance) {
        double lat1 = Math.toRadians(p.lat);
        double lon1 = Math.toRadians(p.lon);
        double brng = Math.toRadians(bearing);

        double dR = distance / R_earth_NM;
        double lat2 = Math.asin(
                Math.sin(lat1) * Math.cos(dR)
                        + Math.cos(lat1) * Math.sin(dR) * Math.cos(brng));
        double lon2 = lon1
                + Math.atan2(
                Math.sin(brng) * Math.sin(dR) * Math.cos(lat1),
                Math.cos(dR) - Math.sin(lat1) * Math.sin(lat2));

        return new Coords(lat2 / Math.PI * 180, lon2 / Math.PI * 180);
    }

    public static double reverseBearing(double bearing) {
        return (bearing + 180) % 360;
    }

    public static double nmToMeters(double distanceInNM) {
        return distanceInNM * KMtoNM_coeff * 1000;
    }

    public static double nmToKm(double distanceInNM) {
        return distanceInNM * KMtoNM_coeff;
    }

    public static double kmToNm(double distanceInKM) {
        return distanceInKM / KMtoNM_coeff;
    }

    public static class Coords {
        private double lat;
        private double lon;

        public Coords(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public boolean isSame(Coords coords) {
            double maxDelta = 0.0000005;
            double latDelta = coords.getLat() - lat;
            double lonDelta = coords.getLon() - lon;
            return Math.abs(latDelta) <= maxDelta && Math.abs(lonDelta) <= maxDelta;
        }
    }
}
