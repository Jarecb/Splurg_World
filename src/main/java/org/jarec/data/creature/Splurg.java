package org.jarec.data.creature;

import com.google.common.annotations.VisibleForTesting;
import org.jarec.data.Heading;
import org.jarec.data.HeadingUtils;
import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.creature.attributes.*;
import org.jarec.game.Combat;
import org.jarec.game.GameLoop;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.GameMath;
import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;
import org.jarec.util.RandomNameGenerator;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Splurg extends Life {
    Aggression aggression = new Aggression();
    Foraging foraging = new Foraging();
    Strength strength = new Strength();
    Toughness toughness = new Toughness();
    Size size;
    Speed speed;
    Charisma charisma;
    Loner loner = new Loner();
    private Hive homeHive;
    String name = RandomNameGenerator.generateName();
    private int breedingDelay = 0;
    private boolean infectedByZombie = false;

    Splurg() {
    }

    public Splurg(Hive hive) {
        var homeLocation = hive.getLocation();
        var location = new Location(homeLocation.getX(), homeLocation.getY());
        setLocation(location);

        commonSetup(hive);
    }

    public Splurg(Splurg parent1, Splurg parent2) {
        JSONObject parent1Json = new JSONObject(parent1.toString());
        JSONObject parent2Json = new JSONObject(parent2.toString());

        if (parent1Json.getInt("Agg") == parent2Json.getInt("Agg")) {
            aggression.setValue(parent1Json.getInt("Agg"));
        }
        if (parent1Json.getInt("For") == parent2Json.getInt("For")) {
            foraging.setValue(parent1Json.getInt("For"));
        }
        if (parent1Json.getInt("Str") == parent2Json.getInt("Str")) {
            strength.setValue(parent1Json.getInt("Str"));
        }
        if (parent1Json.getInt("Tgh") == parent2Json.getInt("Tgh")) {
            toughness.setValue(parent1Json.getInt("Tgh"));
        }
        if (parent1Json.getInt("Lnr") == parent2Json.getInt("Lnr")) {
            toughness.setValue(parent1Json.getInt("Lnr"));
        }

        JSONObject location1 = parent1Json.getJSONObject("Location");
        JSONObject location2 = parent2Json.getJSONObject("Location");
        setLocation(new Location((location1.getInt("x") + location2.getInt("x")) / 2,
                (location1.getInt("y") + location2.getInt("y")) / 2));

        var spawnEnergyCost = Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"));
        parent1.takeEnergy(spawnEnergyCost);
        parent1.takeEnergy(spawnEnergyCost);

        parent1.resetBreedingDelay();
        parent2.resetBreedingDelay();

        commonSetup(parent1.getHomeHive());
    }

    private void commonSetup(Hive hive) {
        size = new Size(toughness, strength);
        speed = new Speed(size);
        charisma = new Charisma();
        calculateCharisma();

        setAgeAtBirth();
        setMaxHealth();

        resetBreedingDelay();

        homeHive = hive;

        Splurgs.getInstance().addSplurg(this);

        var statusMessage = name + " of " + homeHive.getName() + " was spawned on turn " + GameLoop.getInstance().getTurn();
        WorldFrame.getInstance().updateStatus(statusMessage);
    }

    public void resetBreedingDelay() {
        breedingDelay = Integer.parseInt(PropertyHandler.get("splurg.default.breeding.delay", "5"));
    }

    private void setAgeAtBirth() {
        setAge(Integer.parseInt((PropertyHandler.get("splurg.default.spawn.age", "100"))));
    }

    private void setMaxHealth() {
        setMaxHealth(Integer.parseInt((PropertyHandler.get("splurg.default.base.health", "10")))
                + toughness.getValue());
    }

    public void calculateCharisma() {
        charisma.setValue(size, getEnergy());
    }

    void setHomeHive(Hive hive) {
        homeHive = hive;
    }

    public void setInfectedByZombie(boolean zombieAttack) {
        infectedByZombie = zombieAttack;
    }

    public boolean isInfected() {
        return infectedByZombie;
    }

    public void move() {
        degradation();
        age();
        breedingDelay--;
        if (breedingDelay < 0) {
            breedingDelay = 0;
        }

        var location = getLocation();
        var targetAcquired = findTarget();
        if (!targetAcquired) {
            randomisePath();
        } else if (getEnergy() > getSize().getValue()) {
            setHeading(HeadingUtils.getHeadingTo(location, homeHive.getLocation()));
        }
        setHeading(location.updateLocation(getHeading()));
        setLocation(location);
    }

    private void randomisePath() {
        var randomness = Integer.parseInt(PropertyHandler.get("splurg.default.pathing.randomness", "5"));
        if (RandomInt.getRandomInt(randomness) % randomness == 0) {
            setHeading(getHeading().getRandomTurn());
        }
    }

    private void degradation() {
        int maxAttribute = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));
        int degradationRate = Integer.parseInt(PropertyHandler.get("splurg.degradation", "0"));
        int sizeValue = getSize().getValue();

        int degradationChance = (maxAttribute - sizeValue) * degradationRate;

        if (degradationChance <= 0 || RandomInt.getRandomInt(degradationChance) % degradationChance != 0) {
            return;
        }

        if (getEnergy() > sizeValue) {
            takeEnergy(sizeValue);
            return;
        }

        if (RandomInt.getRandomInt(2) % 2 == 0) {
            int toughnessValue = toughness.getValue();
            if (toughnessValue > 2) {
                toughness.setValue(toughnessValue - 1);
            }
        } else {
            int strengthValue = strength.getValue();
            if (strengthValue > 2) {
                strength.setValue(strengthValue - 1);
            }
        }

        size = new Size(toughness, strength);
        speed = new Speed(size);
    }

    public boolean canBreed() {
        return breedingDelay == 0;
    }

    public void depositEnergy() {
        if (getEnergy() > getSize().getValue()
                && GameMath.calculateHypotenuse(getLocation(), homeHive.getLocation()) <= 2) {
            var energyTransfer = getEnergy() - getSize().getValue();
            homeHive.addEnergy(energyTransfer);
            takeEnergy(energyTransfer);
            setHeading(Heading.getRandomHeading());
            var statusMessage = name + " deposited " + energyTransfer + " energy at " + homeHive.getName();
            WorldFrame.getInstance().updateStatus(statusMessage);
        }
    }

    public boolean findTarget() {
        Location currentLocation = getLocation();

        int aggressionMultiplier = Integer.parseInt(PropertyHandler.get("splurg.default.aggression.multiplier", "5"));
        int foragingMultiplier = Integer.parseInt(PropertyHandler.get("splurg.default.foraging.multiplier", "5"));

        double foragingThreshold = getForaging().getValue() * (double) foragingMultiplier;
        double aggressionThreshold = getAggression().getValue() * (double) aggressionMultiplier;

        // Raid enemy Hive
        List<Hive> allHives = Hives.getInstance().getHives();
        synchronized (allHives) {
            Hive nearestHive = findNearestEnemyHive(currentLocation, allHives, foragingThreshold);
            if (nearestHive != null) {
                handleEnemyHiveFound(nearestHive);
                return true;
            }
        }

        // Fight enemy Splurgs
        List<Splurg> allSplurgs = Splurgs.getInstance().getSplurgs();
        synchronized (allSplurgs) {
            Splurg nearestEnemy = findNearestEnemySplurg(currentLocation, allSplurgs, aggressionThreshold);
            if (nearestEnemy != null) {
                handleEnemySplurgFound(nearestEnemy);
                return true;
            }
        }

        // Dump energy at Home Hive
        if (getEnergy() > getSize().getValue() * Integer.parseInt(PropertyHandler.get("splurg.default.energy.capacity", "5"))) {
            returnHome();
            return true;
        }

        if (getHeading() == null) {
            setHeading(Heading.getRandomHeading());
        }

        return false;
    }

    private void returnHome() {
        if (RandomInt.getRandomInt(3) % 3 == 0) {
            setHeading(getHeading().getRandomTurn());
        } else {
            Heading newHeading = HeadingUtils.getHeadingTo(getLocation(), homeHive.getLocation());
            setHeading(newHeading);
        }
    }

    private Hive findNearestEnemyHive(Location currentLocation, List<Hive> hives, double threshold) {
        return hives.stream()
                .filter(candidate -> !candidate.equals(homeHive))
                .filter(candidate -> candidate.getEnergyReserve() > 0)
                .filter(candidate -> isWithinThreshold(currentLocation, candidate.getLocation(), threshold))
                .min(Comparator.comparingDouble(candidate -> GameMath.calculateHypotenuse(currentLocation, candidate.getLocation())))
                .orElse(null);
    }

    Splurg findNearestEnemySplurg(Location currentLocation, List<Splurg> splurgs, double threshold) {
        return splurgs.stream()
                .filter(candidate -> candidate != this)
                .filter(candidate -> !candidate.getHomeHive().equals(homeHive))
                .filter(candidate -> isWithinThreshold(currentLocation, candidate.getLocation(), threshold))
                .min(Comparator.comparingDouble(candidate -> GameMath.calculateHypotenuse(currentLocation, candidate.getLocation())))
                .orElse(null);
    }

    boolean isWithinThreshold(Location current, Location candidate, double threshold) {
        double dx = current.getX() - (double) candidate.getX();
        double dy = current.getY() - (double) candidate.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= threshold;
    }

    void handleEnemySplurgFound(Splurg enemy) {
        Heading newHeading = HeadingUtils.getHeadingTo(getLocation(), enemy.getLocation());

        if (!(enemy instanceof Zombie) && (newHeading == null || GameMath.calculateHypotenuse(getLocation(),
                enemy.getLocation()) < (getSize().getValue() + enemy.getSize().getValue() - 1))) {
            var combatBreak = Integer.parseInt(PropertyHandler.get("splurg.default.stuck.break", "10"));
            if (RandomInt.getRandomInt(combatBreak) % combatBreak == 0) {
                setHeading(getHeading().getRandomTurn());
            } else {
                Combat.attack(this, enemy);
            }
        } else {
            setHeading(newHeading);
            setHeading(getHeading().getRandomTurn());
        }
    }

    private void handleEnemyHiveFound(Hive enemyHive) {
        Heading newHeading = HeadingUtils.getHeadingTo(getLocation(), enemyHive.getLocation());

        if (newHeading == null || GameMath.calculateHypotenuse(getLocation(),
                enemyHive.getLocation()) < getSize().getValue()) {
            var combatBreak = Integer.parseInt(PropertyHandler.get("splurg.default.stuck.break", "10"));
            if (RandomInt.getRandomInt(combatBreak) % combatBreak == 0) {
                setHeading(getHeading().getRandomTurn());
            } else {
                this.increaseEnergy(enemyHive.getEnergy(Integer.parseInt(PropertyHandler.get("splurg.default.feeding.volume", "5"))));
                var statusMessage = getHomeHive().getName() + " is feeding from " + enemyHive.getName();
                WorldFrame.getInstance().updateStatus(statusMessage);
            }
        } else {
            setHeading(newHeading);
            setHeading(getHeading().getRandomTurn());
        }
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

    public Charisma getCharisma() {
        return charisma;
    }

    public Loner getLoner() {
        return loner;
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

    public Hive getHomeHive() {
        return homeHive;
    }

    @Override
    public String toString() {
        return "{" + "\"Name\":\"" + name + "\"," +
                "\"Hive\":\"" + (homeHive != null ? homeHive.getName() : "null") + "\"," +
                "\"Agg\":" + aggression.getValue() + "," +
                "\"For\":" + foraging.getValue() + "," +
                "\"Str\":" + strength.getValue() + "," +
                "\"Tgh\":" + toughness.getValue() + "," +
                "\"Siz\":" + (size != null ? size.getValue() : 0) + "," +
                "\"Spd\":" + (speed != null ? speed.getValue() : 0) + "," +
                "\"Cha\":" + charisma.getValue() + "," +
                "\"Lnr\":" + loner.getValue() + "," +
                "\"Hth\":" + getHealth() + "," +
                "\"Eng\":" + getEnergy() + "," +
                "\"Location\":" + (getLocation() != null ? getLocation().toString() : "null") +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Splurg other)) return false;
        return Objects.equals(name, other.name)
                && Objects.equals(this.getLocation(), other.getLocation())
                && Objects.equals(homeHive, other.homeHive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, this.getLocation(), homeHive);
    }
}