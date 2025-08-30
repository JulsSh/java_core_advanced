package oop.com.examle.vehicles;

import oop.com.examle.types.*;



public class Car extends Vehicle  implements Loadable, Breakable, Acceleratable, Refuelled, Limitable {
    private int fuel;                 // current liters (abstract units)
    private final int maxFuel;        // tank size
    private final int consumption;    // per tick
    private int cargoKg;              // cargo affects speed
    private final double breakProbability;
    private final int kmLimit;
    private int kmTotal;

    public Car(String model, int speed, int x, int fuel, int maxFuel, int consumption, double breakProbability, int kmLimit) {
        super(model, speed, x);
        this.fuel= fuel;
        this.maxFuel = maxFuel;
        this.consumption = consumption;
        this.breakProbability = breakProbability;
        this.kmLimit = kmLimit;
    }

    @Override
    public void speedUp(int delta) {
        this.speed += Math.max(1, delta);
    }

    @Override
    public boolean isBroken() {
        return Math.random() < breakProbability;
    }

    @Override
    public void refuel() {
        this.fuel = maxFuel;
    }

    @Override
    public void move() {
        if (!hasResourceLeft()) return;
        if (isBroken()) return;

        if (fuel < consumption) {     // pit stop → skip moving this tick
            refuel();
            return;
        }

        // cargo penalty: -1 km/h per each 100 kg (never below 1)
        int penalty = cargoKg / 100;
        int effectiveSpeed = Math.max(1, speed - penalty);

        this.x += effectiveSpeed;
        kmTotal += effectiveSpeed;
        fuel -= consumption;
    }

    @Override
    public boolean hasResourceLeft() {
        return kmTotal < kmLimit;
    }


    @Override
    public void load(int kg) {
        this.cargoKg += Math.max(0, kg);
    }

    @Override
    public void unload(int kg) {
        this.cargoKg = Math.max(0, cargoKg - Math.max(0, kg));
    }

    @Override
    public int getCargoKg() {
        return cargoKg;
    }
}
