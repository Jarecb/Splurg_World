package org.jarec.data.creature;

import org.jarec.data.Heading;
import org.jarec.data.Location;

public abstract class Life {
    private int age = 0;
    private int health = 0;
    private int maxHealth = 0;
    private int energy = 0;
    private Location location;
    private Heading heading = Heading.getRandomHeading();

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public boolean age() {
        age--;
        return age > 0;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void setHealth(int health) {
        this.health = health;
        if (this.health > maxHealth) {
            increaseEnergy(health - maxHealth);
            this.health = maxHealth;
        }
    }

    public int getHealth() {
        return health;
    }

    public void recoverHealth(int recovery) {
        setHealth(health + recovery);
    }

    public boolean reduceHealth(int reduction) {
        setHealth(health - reduction);
        return health > 0;
    }

    public void heal() {
        if (health < maxHealth) {
            health += takeEnergy(1);
        }
    }

    public void increaseEnergy(int bonus) {
        energy += bonus;
    }

    public int takeEnergy(int drain) {
        if (drain <= energy) {
            energy -= drain;
            return drain;
        }
        drain = energy;
        energy = 0;
        return drain;
    }

    public int getEnergy() {
        return energy;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Heading getHeading() {
        if (heading == null) {
            heading = Heading.getRandomHeading();
        }
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }
}
