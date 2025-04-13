package org.jarec.data;

import org.jarec.data.creature.Splurg;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class Hive {
    private final Location location;
    private Color color;
    private final String name;
    private int energyReserve = 0;
    private int spawnCountdown = 0;

    public Hive(Location location, Color color, String name){
        this.location = location;
        this.color = color;
        this.name = name;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public void spawn() {
        var spawnEnergy = Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"));
        if (spawnCountdown > 0) {
            spawnCountdown--;
        } else {
            if (energyReserve >= spawnEnergy) {
                getEnergy(spawnEnergy);
                spawnCountdown = Integer.parseInt(PropertyHandler.get("hive.default.spawn.rate", "5"));
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

    public int getEnergyReserve() {
        return energyReserve;
    }

    public int getEnergy(int requestedAmount) {
        var energyTaken = requestedAmount;
        if (requestedAmount > energyReserve) {
            energyTaken = energyReserve;
        }
        energyReserve -= energyTaken;
        return energyTaken;
    }

    public void setEnergyReserve(int energyReserve) {
        this.energyReserve = energyReserve;
    }

    public void addEnergy(int energy) {
        energyReserve += energy;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"Hive\": {" +
                        "\"name\": \"%s\"," +
                        "\"location\": %s," +
                        "\"color\": {\"r\": %d, \"g\": %d, \"b\": %d}," +
                        "\"energyReserve\": %d," +
                        "\"spawnCountdown\": %d" +
                        "}}",
                name,
                location != null ? location.toString() : "null",
                color != null ? color.getRed() : 0,
                color != null ? color.getGreen() : 0,
                color != null ? color.getBlue() : 0,
                energyReserve,
                spawnCountdown
        );
    }
}
