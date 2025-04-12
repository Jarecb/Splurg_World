package org.jarec.data.creature;

import com.google.common.annotations.VisibleForTesting;
import org.jarec.data.Heading;
import org.jarec.data.HeadingUtils;
import org.jarec.data.Location;
import org.jarec.data.Nest;
import org.jarec.data.creature.attributes.*;
import org.jarec.game.Combat;
import org.jarec.game.GameLoop;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;
import org.jarec.util.RandomNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Splurg extends Life {
    private static final Logger log = LoggerFactory.getLogger(Splurg.class);

    private final Aggression aggression = new Aggression();
    private final Foraging foraging = new Foraging();
    private final Strength strength = new Strength();
    private final Toughness toughness = new Toughness();
    private Size size;
    private Speed speed;
    private Nest homeNest;
    private String name = RandomNameGenerator.generateName();
    private int breedingDelay = 0;
    private boolean inCombat = false;

    public Splurg(Nest nest) {
        var homeLocation = nest.getLocation();
        var Location = new Location(homeLocation.getX(), homeLocation.getY());
        setLocation(Location);

        commonSetup(nest);
    }

    public Splurg(Splurg parent1, Splurg parent2) {
        if (parent1.getAggression().getValue() == parent2.getAggression().getValue()) {
            aggression.setValue(parent1.getAggression().getValue());
        }

        if (parent1.getForaging().getValue() == parent2.getForaging().getValue()) {
            foraging.setValue(parent1.getForaging().getValue());
        }

        if (parent1.getStrength().getValue() == parent2.getStrength().getValue()) {
            strength.setValue(parent1.getStrength().getValue());
        }

        if (parent1.getToughness().getValue() == parent2.getToughness().getValue()) {
            toughness.setValue(parent1.getToughness().getValue());
        }

        setLocation(new Location((parent1.getLocation().getX() + parent2.getLocation().getX()) / 2,
                (parent1.getLocation().getY() + parent2.getLocation().getY()) / 2));


        var spawnEnergyCost = Integer.parseInt(PropertyHandler.get("nest.default.spawn.food", "10"));
        parent1.takeEnergy(spawnEnergyCost);
        parent1.takeEnergy(spawnEnergyCost);

        parent1.resetBreedingDelay();
        parent2.resetBreedingDelay();

        commonSetup(parent1.getHomeNest());
    }

    private void commonSetup(Nest nest) {
        size = new Size(toughness, strength);
        speed = new Speed(size);

        setAgeAtBirth();
        setMaxHealth();

        resetBreedingDelay();

        homeNest = nest;

        var statusMessage = name + " of " + homeNest.getName() + " was spawned on turn " + GameLoop.getInstance().getTurn();
        log.info(statusMessage + " " + this);
        WorldFrame.getInstance().updateStatus(statusMessage);
    }

    public void resetBreedingDelay(){
        breedingDelay = Integer.parseInt(PropertyHandler.get("splurg.default.breeding.delay", "5"));
    }

    private void setAgeAtBirth() {
        setAge(Integer.parseInt((PropertyHandler.get("splurg.default.spawn.age", "100"))));
    }

    private void setMaxHealth() {
        setMaxHealth(Integer.parseInt((PropertyHandler.get("splurg.default.base.health", "10")))
                + toughness.getValue());
    }

    public void move() {
        breedingDelay--;
        if (breedingDelay < 0){ breedingDelay = 0;}

        var location = getLocation();
        var targetAcquired = findNearest();
        if (!targetAcquired) {
            var randomness = Integer.parseInt(PropertyHandler.get("splurg.default.pathing.randomness", "5"));
            if (RandomInt.getRandomInt(randomness) % randomness == 0) {
                setHeading(getHeading().getRandomTurn());
            }
        } else if (getEnergy() > getSize().getValue()) {
            setHeading(HeadingUtils.getHeadingTo(location, homeNest.getLocation()));
        }
        setHeading(location.updateLocation(getHeading()));
        setLocation(location);
    }

    public boolean canBreed(){
        return breedingDelay == 0;
    }

    public void depositEnergy(){
        if (getEnergy() > getSize().getValue()
                && getLocation().getX() == homeNest.getLocation().getX()
                && getLocation().getY() == homeNest.getLocation().getY()) {
            var energyTransfer = getEnergy() - getSize().getValue();
            homeNest.addFood(energyTransfer);
            takeEnergy(energyTransfer);
            setHeading(Heading.getRandomHeading());
            log.info("{} deposited {} energy at {}", name, energyTransfer, homeNest.getName());
        }
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public boolean isInCombat(){
        return this.inCombat;
    }

    public boolean findNearest() {
        Location currentLocation = getLocation();

        double distanceThreshold = getForaging().getValue() *
                Integer.parseInt(PropertyHandler.get("splurg.default.foraging.multiplier", "5"));

        List<Splurg> splurgs = Splurgs.getInstance().getSplurgs();

        // Find the nearest Splurg within the distance threshold
        synchronized (splurgs) {
            Splurg nearestSplurg = splurgs.stream()
                    // Exclude the same splurg
                    .filter(candidate -> candidate != this)
                    // Exclude same Nest
                    .filter(candidate -> !candidate.getHomeNest().equals(homeNest))
                    // Calculate the distance and check if it's within the threshold
                    .filter(candidate -> {
                        double dx = currentLocation.getX() - candidate.getLocation().getX();
                        double dy = currentLocation.getY() - candidate.getLocation().getY();
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        return distance <= distanceThreshold;
                    })
                    // Find the closest Splurg by distance
                    .min((splurg1, splurg2) -> {
                        double distance1 = calculateDistance(currentLocation, splurg1.getLocation());
                        double distance2 = calculateDistance(currentLocation, splurg2.getLocation());
                        return Double.compare(distance1, distance2);
                    })
                    .orElse(null);

            if (nearestSplurg != null) {
                Heading newHeading = HeadingUtils.getHeadingTo(getLocation(), nearestSplurg.getLocation());
                if (newHeading == null) {
                    Combat.attack(this, nearestSplurg);
                } else {
                    setInCombat(false);
                    setHeading(newHeading);
                    setHeading(getHeading().getRandomTurn());
                }
                return true;
            }
        }
        return false;
    }


    // Helper method to calculate Euclidean distance
    private double calculateDistance(Location loc1, Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dy = loc1.getY() - loc2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Aggression getAggression() {
        return aggression;
    }

    @VisibleForTesting
    void setAggression(int value) {
        aggression.setValue(value);
    }

    public Foraging getForaging() {
        return foraging;
    }

    @VisibleForTesting
    void setForaging(int value) {
        foraging.setValue(value);
    }

    public String getName() {
        return name;
    }

    public Size getSize() {
        return size;
    }

    public Speed getSpeed() {
        return speed;
    }

    public Strength getStrength() {
        return strength;
    }

    @VisibleForTesting
    void setStrength(int value) {
        strength.setValue(value);
    }

    public Toughness getToughness() {
        return toughness;
    }

    @VisibleForTesting
    void setToughness(int value) {
        toughness.setValue(value);
    }

    public Nest getHomeNest() {
        return homeNest;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append("Name:");
        sb.append(name);
        sb.append("; Nest:");
        sb.append(homeNest.getName());
        sb.append("; Agg:");
        sb.append(aggression.getValue());
        sb.append("; For:");
        sb.append(foraging.getValue());
        sb.append("; Str:");
        sb.append(strength.getValue());
        sb.append("; Tgh:");
        sb.append(toughness.getValue());
        sb.append("; Siz:");
        sb.append(size.getValue());
        sb.append("; Spd:");
        sb.append(speed.getValue());
        sb.append("; Hth:");
        sb.append(getHealth());
        sb.append("; Eng:");
        sb.append(getEnergy());
        var location = getLocation();
        sb.append("; Loc.X:");
        sb.append(location.getX());
        sb.append("; Loc.Y:");
        sb.append(location.getY());
        sb.append("]");

        return sb.toString();
    }
}
