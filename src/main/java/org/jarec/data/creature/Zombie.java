package org.jarec.data.creature;

import org.jarec.data.Location;
import org.jarec.data.creature.attributes.Size;
import org.jarec.data.creature.attributes.Speed;
import org.jarec.game.SimulationState;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.GameEvents;
import org.jarec.util.GameMath;
import org.jarec.util.PropertyHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Zombie extends Splurg {
    private boolean active = false;
    private int activationDelay = PropertyHandler.getInt("zombie.default.activation.delay", 20);

    public Zombie(Splurg source) {
        aggression.setValue(PropertyHandler.getInt("zombie.default.aggression", 10));

        int str = source.getStrength().getValue();
        strength.setValue(str + PropertyHandler.getInt("zombie.default.strength.bonus", 1));
        int tgh = source.getToughness().getValue();
        toughness.setValue(tgh);
        setMaxHealth(PropertyHandler.getInt("zombie.default.health", 20));

        Location sourceLocation = source.getLocation();
        setLocation(new Location(sourceLocation.getX(), sourceLocation.getY()));

        size = new Size(toughness, strength);
        speed = new Speed(size, PropertyHandler.getInt("zombie.default.speed.multiplier", 2));
        name = "Zombie " + source.getName();

        setHomeHive(Hives.getZombieHive());
        setZombie(true);
        setHeading(null);

        Splurgs.getInstance().addSplurg(this);

        var statusMessage = "A Zombie Splurg was created on turn " + SimulationState.getTurn();
        GameEvents.publishStatus(statusMessage);
    }

    @Override
    public void move() {
        if (active) {
            var location = getLocation();
            var targetAcquired = findTarget();
            if (!targetAcquired) {
                setHeading(null);
            }
            setHeading(location.updateLocation(getHeading()));
            setLocation(location);
        } else {
            activationDelay--;
            if (activationDelay <= 0) {
                active = true;
            }
        }
    }

    @Override
    public boolean findTarget() {
        Location currentLocation = getLocation();

        int aggressionMultiplier = PropertyHandler.getInt("splurg.default.aggression.multiplier", 5);

        double aggressionThreshold = getAggression().getValue() * (double) aggressionMultiplier;

        // Fight enemy Splurgs
        List<Splurg> allSplurgs = Splurgs.getInstance().getSplurgs();
        synchronized (allSplurgs) {
            Splurg nearestEnemy = findNearestEnemySplurg(currentLocation, allSplurgs, aggressionThreshold);
            if (nearestEnemy != null) {
                handleEnemySplurgFound(nearestEnemy);
                return true;
            }
        }

        return false;
    }

    @Override
    Splurg findNearestEnemySplurg(Location currentLocation, List<Splurg> splurgs, double threshold) {
        return splurgs.stream()
                .filter(candidate -> candidate != this)
                .filter(candidate -> !(candidate instanceof Zombie))
                .filter(candidate -> isWithinThreshold(currentLocation, candidate.getLocation(), threshold))
                .min(Comparator.comparingDouble(candidate -> GameMath.calculateHypotenuse(currentLocation, candidate.getLocation())))
                .orElse(null);
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void depositEnergy() {
        // Zombies don't collect energy
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Zombie other)) return false;
        return Objects.equals(name, other.name)
                && Objects.equals(this.getLocation(), other.getLocation())
                && Objects.equals(getHomeHive(), other.getHomeHive());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, this.getLocation(), getHomeHive());
    }
}
