package org.jarec.data.creature;

public abstract class Life {
    private int age = 0;
    private int health = 0;
    private int maxHealth = 0;

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
        health = health - reduction;
        if (health <= 0) {
            return false;
        }
        return true;
    }
}
