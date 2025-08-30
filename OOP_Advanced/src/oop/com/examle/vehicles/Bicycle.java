package oop.com.examle.vehicles;

import oop.com.examle.types.Acceleratable;
import oop.com.examle.types.Breakable;
import oop.com.examle.types.Limitable;

public class Bicycle extends Vehicle implements Breakable, Acceleratable, Limitable {
    private final double breakProbability; // 0..1 per tick
    private final int kmLimit;
    private int kmTotal;

    public Bicycle(String model, int speed, int x, double breakProbability, int kmLimit, int kmTotal) {
        super(model, speed, x);
        this.breakProbability = breakProbability;
        this.kmLimit = kmLimit;
        this.kmTotal = kmTotal;
    }

    @Override
    public boolean isBroken() {
        return Math.random() <=breakProbability;
    }


    @Override
    public void speedUp(int delta) {
        this.speed += Math.max(1, delta / 5);
    }

    @Override
    public boolean hasResourceLeft() {
        return kmTotal < kmLimit;
    }

    @Override
    public int getRemainingKm() {
        return Math.max(0, kmLimit - kmTotal);
    }

    @Override
    public void move() {
        if (!hasResourceLeft()) return;               // lifetime over
        if (isBroken()) return;                        // broken this tick
        super.move();
        kmTotal += speed;
    }
}
