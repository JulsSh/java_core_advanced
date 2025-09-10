package oop.com.examle.vehicles;


import oop.com.examle.types.*;


public class Train extends Vehicle implements Loadable, Acceleratable, Breakable, Refuelled, Limitable {
    private int energy;             // abstract fuel/energy units
    private final int maxEnergy;
    private final int usePerTick;
    private int cargoKg;
    private final double breakProbability;
    private final int kmLimit;
    private int kmTotal;

    public Train(String model, int speed, int x, int fuel, int maxEnergy, int usePerTick, double breakProbability, int kmLimit) {
        super(model, speed, x);
        this.maxEnergy = maxEnergy;
        this.usePerTick = usePerTick;
        this.breakProbability = breakProbability;
        this.kmLimit = kmLimit;
    }


    @Override
    public void speedUp(int delta) {
        this.speed += Math.max(1, delta * 2);
    }

    @Override
    public boolean isBroken() {
        return Math.random() < breakProbability;
    }

    @Override
    public void refuel() {
        this.energy = maxEnergy;
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

    @Override
    public void move() {
        if (!hasResourceLeft()) return;
        if (isBroken()) return;

        if (energy < usePerTick) { // pit stop
            refuel();
            return;
        }
        int penalty = cargoKg / 500;
        int effectiveSpeed = Math.max(1, speed - penalty);

        this.x += effectiveSpeed;
        kmTotal += effectiveSpeed;
        energy -= usePerTick;
    }
}

