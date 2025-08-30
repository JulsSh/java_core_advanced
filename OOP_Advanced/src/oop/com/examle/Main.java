package oop.com.examle;

import oop.com.examle.vehicles.Bicycle;
import oop.com.examle.vehicles.Car;
import oop.com.examle.vehicles.Train;
import oop.com.examle.vehicles.Vehicle;

public class Main{
    public static void main(String[] args) {
       //round 1 car will win

        Vehicle bike  = new Bicycle("Trek FX", 30, 0, 0.10, 1200, 0);
        Car car   = new Car("BMW 320i", 200, 0, 50, 50, 2, 0.05, 600);
        Train train = new Train("ICE 4",  40,  0, 300, 300, 2, 0.01, 3000);

        car.load(150);   // -1 or -2 km/h depending on rule
        train.load(2000);

        Competition comp = new Competition(300); // race distance 300 "km"
        Vehicle winner = comp.race(new Vehicle[]{bike, car, train});

        System.out.println("Winner: " + winner.getModel() + " — x=" + winner.getX());

        //Round 2 bicycle will win
        Vehicle bike2  = new Bicycle("Trek FX", 310, 0, 0.0, 1000, 0);
        Car car2   = new Car("BMW 320i", 200, 0, 0, 2, 5, 0.00, 600);
        Train train2 = new Train("ICE 4", 40, 0, 0, 0, 1, 0.0, 0);
   car2.load(150);
   train2.load(2000);

   Competition comp2 = new Competition(300);
    Vehicle winner2 = comp2.race(new Vehicle[]{bike2, car2, train2});
        System.out.println("Winner: " + winner2.getModel() + " — x=" + winner2.getX());
    }
}
