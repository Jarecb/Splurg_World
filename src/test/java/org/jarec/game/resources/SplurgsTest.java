package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.Zombie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SplurgsTest {

    @BeforeEach
    void clearDownForTest() {
        Splurgs.getInstance().clearSplurgs();
        Hives.getInstance().clearHives();
    }

    @Test
    void getInstance() {
        assertInstanceOf(Splurgs.class, Splurgs.getInstance());
    }

    @Test
    void addSplurg() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hives.getInstance().addHive(hive);
        Splurgs.getInstance().addSplurg(new Splurg(hive));
        assertEquals(1, Splurgs.getInstance().getSpawns());
    }

    @Test
    void addZombie() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives.getInstance().addHive(zombieHive);
        Zombie zombie = new Zombie(new Splurg(hive));
        Splurgs.getInstance().addSplurg(zombie);
        assertEquals(2, Splurgs.getInstance().getSpawns());
    }

    @Test
    void getTotalSplurgs() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive);
        hives.addHive(zombieHive);
        new Splurg(hive);
        new Splurg(hive);
        new Zombie(new Splurg(hive));
        assertEquals(3, Splurgs.getInstance().getTotalSplurgs());
    }

    @Test
    void getTotalInfectedSplurgs() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive);
        hives.addHive(zombieHive);
        Splurg splurg1 = new Splurg(hive);
        splurg1.setInfectedByZombie(false);
        Splurg splurg2 = new Splurg(hive);
        splurg2.setInfectedByZombie(true);
        new Zombie(new Splurg(hive));
        assertEquals(1, Splurgs.getInstance().getTotalInfectedSplurgs());
    }

    @Test
    void getDeaths() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive);
        hives.addHive(zombieHive);
        Splurg splurg1 = new Splurg(hive);
        splurg1.takeWound(20);
        new Splurg(hive);
        Zombie zombie = new Zombie(new Splurg(hive));
        zombie.takeWound(100);
        Splurgs.getInstance().removeDeadSplurgs();
        assertEquals(1, Splurgs.getInstance().getDeaths());
    }

    @Test
    void getSpawns() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive);
        hives.addHive(zombieHive);
        Splurg splurg1 = new Splurg(hive);
        new Splurg(hive);
        new Zombie(splurg1);
        assertEquals(3, Splurgs.getInstance().getSpawns());
    }

    @Test
    void getZombieSpawns() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive);
        hives.addHive(zombieHive);
        new Splurg(hive);
        new Splurg(hive);
        new Zombie(new Splurg(hive));
        assertEquals(1, Splurgs.getInstance().getZombieSpawns());
    }

    @Test
    void getZombieDeaths() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive one", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie hive", true);
        Hives.getInstance().addHive(hive);
        Hives.getInstance().addHive(zombieHive);
        new Splurg(hive);
        Splurg splurg = new Splurg(hive);
        Zombie zombie1 = new Zombie(new Splurg(hive));
        Zombie zombie2 = new Zombie(new Splurg(hive));
        splurg.takeWound(50);
        zombie1.takeWound(100);
        zombie2.takeWound(100);
        Splurgs.getInstance().removeDeadSplurgs();
        assertEquals(2, Splurgs.getInstance().getZombieDeaths());
    }

    @Test
    void getSplurgs() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        Hive hive2 = new Hive(new Location(1, 1), null, "Hive two", false);
        Hives.getInstance().addHive(hive1);
        Hives.getInstance().addHive(hive2);
        new Splurg(hive1);
        new Splurg(hive2);
        assertEquals(2, Splurgs.getInstance().getSplurgs().size());
    }

    @Test
    void depositEnergy() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive one", false);
        Splurg splurg = new Splurg(hive);
        var startingEnergy = splurg.getEnergy();
        var bonusEnergy = 6;
        splurg.increaseEnergy(bonusEnergy);
        assertEquals(startingEnergy + bonusEnergy, splurg.getEnergy());
    }

    @Test
    void healSplurgs() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive one", false);
        Splurg splurg = new Splurg(hive);
        splurg.setMaxHealth(10);
        splurg.takeWound(4);
        splurg.increaseEnergy(6);
        Splurgs.getInstance().healSplurgs();
        Splurgs.getInstance().healSplurgs();
        assertEquals(8, splurg.getHealth());
    }

    @Test
    void zombieSpawned() {
        Splurgs.zombieSpawned();
        assertEquals(1, Splurgs.getInstance().getZombieSpawns());
    }

    @Test
    void removeDeadSplurgs() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        Splurg splurg1 = new Splurg(hive1);
        Splurg splurg2 = new Splurg(hive1);
        Splurg splurg3 = new Splurg(hive1);
        splurg3.takeWound(50);
        Splurgs.getInstance().removeDeadSplurgs();
        var splurgs = Splurgs.getInstance().getSplurgs();
        assertTrue(splurgs.contains(splurg1));
        assertTrue(splurgs.contains(splurg2));
        assertTrue(!splurgs.contains(splurg3));
    }

    @Test
    void getSplurgsPerHive() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        Hive hive2 = new Hive(new Location(1, 1), null, "Hive two", false);
        new Splurg(hive1);
        new Splurg(hive2);
        new Splurg(hive2);
        var counts = Splurgs.getInstance().getSplurgsPerHive();

        int hiveOneValue = counts.entrySet().stream()
                .filter(entry -> "Hive one".equals(entry.getKey().getName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Hive one not found"));
        assertEquals(1, hiveOneValue);

        int hiveTwoValue = counts.entrySet().stream()
                .filter(entry -> "Hive two".equals(entry.getKey().getName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Hive two not found"));
        assertEquals(2, hiveTwoValue);
    }

    @Test
    void getTotalSplurgEnergyPerHive() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        hive1.setEnergyReserve(10);
        Hive hive2 = new Hive(new Location(1, 1), null, "Hive two", false);
        hive2.setEnergyReserve(5);
        Splurg splurg1 = new Splurg(hive1);
        splurg1.increaseEnergy(5);
        Splurg splurg2 = new Splurg(hive2);
        Splurg splurg3 = new Splurg(hive2);
        splurg2.increaseEnergy(3);
        splurg3.increaseEnergy(3);
        var counts = Splurgs.getInstance().getTotalSplurgEnergyPerHive();

        int hiveOneValue = counts.entrySet().stream()
                .filter(entry -> "Hive one".equals(entry.getKey().getName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Hive one not found"));
        assertEquals(5, hiveOneValue);

        int hiveTwoValue = counts.entrySet().stream()
                .filter(entry -> "Hive two".equals(entry.getKey().getName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Hive two not found"));
        assertEquals(6, hiveTwoValue);
    }

    @Test
    void getLiveHiveCount() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        Hive hive2 = new Hive(new Location(1, 1), null, "Hive two", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive1);
        hives.addHive(hive2);
        hives.addHive(zombieHive);
        new Splurg(hive1);
        assertEquals(1, Splurgs.getInstance().getLiveHiveCount());

        Splurg splurg2 = new Splurg(hive2);
        splurg2.reduceHealth(50);
        new Zombie(splurg2);
        Splurgs.getInstance().removeDeadSplurgs();
        assertEquals(2, Splurgs.getInstance().getLiveHiveCount());
    }

    @Test
    void getWinningHive() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        Hive hive2 = new Hive(new Location(1, 1), null, "Hive two", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive1);
        hives.addHive(hive2);
        hives.addHive(zombieHive);
        new Splurg(hive1);
        assertEquals(hive1, Splurgs.getInstance().getWinningHive());
    }

    @Test
    void getWinningZombieHive() {
        Hive hive1 = new Hive(new Location(1, 1), null, "Hive one", false);
        Hive hive2 = new Hive(new Location(1, 1), null, "Hive two", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive1);
        hives.addHive(hive2);
        hives.addHive(zombieHive);
        Splurg splurg = new Splurg(hive2);
        splurg.reduceHealth(50);
        new Zombie(splurg);
        Splurgs.getInstance().removeDeadSplurgs();
        assertEquals(zombieHive, Splurgs.getInstance().getWinningHive());
    }

    @Test
    void getZombieCount() {
        Hive hive = new Hive(new Location(1, 1), null, "Hive", false);
        Hive zombieHive = new Hive(new Location(0, 0), null, "Zombie", true);
        Hives hives = Hives.getInstance();
        hives.addHive(hive);
        hives.addHive(zombieHive);
        new Splurg(hive);
        new Zombie(new Splurg(hive));
        new Zombie(new Splurg(hive));
        assertEquals(2, Splurgs.getInstance().getZombieCount());
    }
}