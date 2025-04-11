package org.jarec.data;

import org.jarec.data.creature.Splurg;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class Nest {
    private final Location location;
    private final Color color;
    private final String name;
    private int foodReserve = 0;
    private int spawnCountdown = 0;

    public Nest(Location location, Color color, String name){
        this.location = location;
        this.color = color;
        this.name = name;
    }

    public void spawn() {
        var spawnFood = Integer.parseInt(PropertyHandler.get("nest.default.spawn.food", "10"));
        if (spawnCountdown > 0) {
            spawnCountdown--;
        } else {
            if (foodReserve >= spawnFood) {
                getFood(spawnFood);
                spawnCountdown = Integer.parseInt(PropertyHandler.get("nest.default.spawn.rate", "5"));
                var splurgSpawn = new Splurg(this);
                splurgSpawn.setLocation(location);
                Splurgs.getInstance().addSplurg(splurgSpawn);
            }
        }
    }

    public Location getLocation() {
        return location;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getFoodReserve() {
        return foodReserve;
    }

    public int getFood(int requestedAmount) {
        var foodTaken = requestedAmount;
        if (requestedAmount > foodReserve) {
            foodTaken = foodReserve;
        }
        foodReserve -= foodTaken;
        return foodTaken;
    }

    public void setFoodReserve(int foodReserve) {
        this.foodReserve = foodReserve;
    }

    public void addFood(int food) {
        foodReserve += food;
    }
}
