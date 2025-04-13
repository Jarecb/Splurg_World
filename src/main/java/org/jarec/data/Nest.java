package org.jarec.data;

import org.jarec.data.creature.Splurg;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class Nest {
    private final Location location;
    private Color color;
    private final String name;
    private int foodReserve = 0;
    private int spawnCountdown = 0;

    public Nest(Location location, Color color, String name){
        this.location = location;
        this.color = color;
        this.name = name;
    }

    public void setColor(Color color){
        this.color = color;
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

    @Override
    public String toString() {
        return String.format(
                "{\"Nest\": {" +
                        "\"name\": \"%s\"," +
                        "\"location\": %s," +
                        "\"color\": {\"r\": %d, \"g\": %d, \"b\": %d}," +
                        "\"foodReserve\": %d," +
                        "\"spawnCountdown\": %d" +
                        "}}",
                name,
                location != null ? location.toString() : "null",
                color != null ? color.getRed() : 0,
                color != null ? color.getGreen() : 0,
                color != null ? color.getBlue() : 0,
                foodReserve,
                spawnCountdown
        );
    }
}
