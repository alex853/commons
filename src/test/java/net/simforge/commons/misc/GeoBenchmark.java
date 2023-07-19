package net.simforge.commons.misc;

import java.util.ArrayList;
import java.util.List;

// Avg time, nano
// Geo#distances  : 353
// Geo#degreeDiffs: 5
public class GeoBenchmark {

    public static void main(String[] args) {
        int calculationCount = 10000000;

        System.out.println("Preparing random coords");
        List<Geo.Coords> positions = new ArrayList<>(calculationCount);
        for (int i = 0; i < calculationCount + 1; i++) {
            positions.add(randomCoords());
        }

        System.out.println("Calculating Geo#distances");
        long started1 = System.currentTimeMillis();
        for (int i = 0; i < calculationCount; i++) {
            Geo.Coords p1 = positions.get(i);
            Geo.Coords p2 = positions.get(i);
            Geo.distance(p1, p2);
        }
        long finished1 = System.currentTimeMillis();

        System.out.println("Calculating Geo#degreeDiffs");
        long started2 = System.currentTimeMillis();
        for (int i = 0; i < calculationCount; i++) {
            Geo.Coords p1 = positions.get(i);
            Geo.Coords p2 = positions.get(i);
            Geo.degreeDifference(p1, p2);
        }
        long finished2 = System.currentTimeMillis();

        System.out.println("Avg time, nano");
        System.out.println("Geo#distances  : " + (finished1 - started1) * 1000000 / calculationCount);
        System.out.println("Geo#degreeDiffs: " + (finished2 - started2) * 1000000 / calculationCount);
    }

    private static Geo.Coords randomCoords() {
        return Geo.coords(
                180*Math.random() - 90,
                360*Math.random() - 180
        );
    }

}
