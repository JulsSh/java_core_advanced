package oop.com.examle.vehicles;

public abstract class Vehicle  {
    protected String model;
    protected int speed;
    protected int x;

    public int getSpeed() {
        return speed;
    }

    public int getX() {
        return x;
    }

    public Vehicle(String model, int speed, int x) {
        this.model = model;
        this.speed = speed;
        this.x = x;
    }

    public void move(){
        this.x += speed;
    }

    public String getModel() {
        return model;
    }
}
