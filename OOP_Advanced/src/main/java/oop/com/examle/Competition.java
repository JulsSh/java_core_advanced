package oop.com.examle;

import oop.com.examle.vehicles.Vehicle;
import java.util.Arrays;
import java.util.Comparator;

public class Competition {
    private final int distance;

    public Competition(int distance) { this.distance = distance; }

    public Vehicle race(Vehicle[] vehicles) {
        Vehicle winner = null;
        int lastMaxX = -1;
        int stagnationTicks = 0;

        while (true) {
            int movedThisTick = 0;

            for (Vehicle v : vehicles) {
                int before = v.getX();
                v.move();
                if (v.getX() >= distance) return v;
                if (v.getX() > before) movedThisTick++;
            }

            int maxX = Arrays.stream(vehicles).mapToInt(Vehicle::getX).max().orElse(0);
            if (maxX == lastMaxX) stagnationTicks++; else stagnationTicks = 0;
            lastMaxX = maxX;

            // If nothing is progressing for a while, pick the farthest one and stop.
            if (movedThisTick == 0 || stagnationTicks > 1000) {
                return Arrays.stream(vehicles)
                        .max(Comparator.comparingInt(Vehicle::getX))
                        .orElse(null);
            }
        }
    }
}
