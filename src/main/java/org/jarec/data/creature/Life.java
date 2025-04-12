package org.jarec.data.creature;

import org.jarec.data.Heading;
import org.jarec.data.Location;

public abstract class Life {
    private int age = 0;
    private int health = 0;
    private int maxHealth = 0;
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
        if (age <= 0) {
            return false;
        }
        return true;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void setHealth(int health) {
        this.health = health;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
    }

    public int getHealth() { return health; }

    public void recoverHealth(int recovery) {
        setHealth(health + recovery);
    }

    public boolean reduceHealth(int reduction) {
        setHealth(health - reduction);
        if (health <= 0) {
            return false;
        }
        return true;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }
}
