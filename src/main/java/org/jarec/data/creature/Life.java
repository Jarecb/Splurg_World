package org.jarec.data.creature;

import org.jarec.data.Heading;
import org.jarec.data.Location;
import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public abstract class Life {
    private int age = 0;
    private int health = 0;
    private int maxHealth = 0;
    private int energy = 0;
    private Location location;
    private Heading heading = Heading.getRandomHeading();
    private boolean zombie = false;

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void age() {
        age--;
        if (age <= 0) {
            var deathChance = Integer.parseInt(PropertyHandler.get("splurg.default.death.chance", "500"));
            if (RandomInt.getRandomInt(deathChance) % deathChance == 0) {
                if (getEnergy() > 0) {
                    takeEnergy(1);
                } else {
                    reduceHealth(1);
                }
            }
        }
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
        if (!zombie) {
            setHealth(health + recovery);
        }
    }

    public boolean reduceHealth(int reduction) {
        if (zombie && reduction < 0) {
            return true;
        }
        setHealth(health - reduction);
        return health > 0;
    }

    public void heal() {
        if (!zombie) {
            if (health < maxHealth) {
                health += takeEnergy(1);
            }
        }
    }

    public void increaseEnergy(int bonus) {
        if (!zombie) {
            energy += bonus;
        }
    }

    public int takeEnergy(int drain) {
        if (!zombie) {
            if (drain <= energy) {
                energy -= drain;
                return drain;
            }
            drain = energy;
            energy = 0;
            return drain;
        }
        return 0;
    }

    public int getEnergy() {
        if (!zombie) {
            return energy;
        }
        return 0;
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

    public void setZombie(boolean isZombie) {
        zombie = isZombie;
    }
}
