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
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;

public class Zombie extends Splurg {
    private final Aggression aggression = new Aggression();
//    private final Foraging foraging = new Foraging();
    private final Strength strength = new Strength();
    private final Toughness toughness = new Toughness();
    private Size size;
    private Speed speed;

    public Zombie(Splurg source) {
        super();

        JSONObject sourceJson = new JSONObject(source.toString());

        aggression.setValue(10);

        strength.setValue(sourceJson.getInt("Str"));
        toughness.setValue(sourceJson.getInt("Tgh"));

        JSONObject sourceLocation = sourceJson.getJSONObject("Location");
        setLocation(new Location(sourceLocation.getInt("x"), sourceLocation.getInt("y")));

        size = new Size(toughness, strength);
        speed = new Speed(size);

        Splurgs.getInstance().addSplurg(this);

        var statusMessage = "A Zombie Splurg was created on turn " + GameLoop.getInstance().getTurn();
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

    private void randomisePath(){
        var randomness = Integer.parseInt(PropertyHandler.get("splurg.default.pathing.randomness", "5"));
        if (RandomInt.getRandomInt(randomness) % randomness == 0) {
            setHeading(getHeading().getRandomTurn());
        }

    }

    private void degradation() {
        var degradationChange =
                (Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")) - getSize().getValue()) *
                        Integer.parseInt(PropertyHandler.get("splurg.degradation", "0"));
        if (degradationChange > 0 && RandomInt.getRandomInt(degradationChange) % degradationChange == 0) {
            if (getEnergy() > getSize().getValue()){
                takeEnergy(getSize().getValue());
            } else {
                if (RandomInt.getRandomInt(2) % 2 == 0) {
                    var toughnessValue = toughness.getValue();
                    if (toughnessValue > 2) {
                        toughness.setValue(toughnessValue - 1);
                    }
                } else {
                    var strengthValue = strength.getValue();
                    if (strengthValue > 2) {
                        strength.setValue(strengthValue - 1);
                    }
                }
                size = new Size(toughness, strength);
                speed = new Speed(size);
            }
        }
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

    private Splurg findNearestEnemySplurg(Location currentLocation, List<Splurg> splurgs, double threshold) {
        return splurgs.stream()
                .filter(candidate -> candidate != this)
                .filter(candidate -> !candidate.getHomeHive().equals(homeHive))
                .filter(candidate -> isWithinThreshold(currentLocation, candidate.getLocation(), threshold))
                .min(Comparator.comparingDouble(candidate -> GameMath.calculateHypotenuse(currentLocation, candidate.getLocation())))
                .orElse(null);
    }

    private boolean isWithinThreshold(Location current, Location candidate, double threshold) {
        double dx = current.getX() - (double) candidate.getX();
        double dy = current.getY() - (double) candidate.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= threshold;
    }

    private void handleEnemySplurgFound(Splurg enemy) {
        Heading newHeading = HeadingUtils.getHeadingTo(getLocation(), enemy.getLocation());

        if (newHeading == null || GameMath.calculateHypotenuse(getLocation(),
                enemy.getLocation()) < (getSize().getValue() + enemy.getSize().getValue() - 1)) {
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

    public Toughness getToughness() {
        return toughness;
    }

    public Hive getHomeHive() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"Name\":\"").append(name).append("\",");
        sb.append("\"Hive\":\"").append(homeHive != null ? homeHive.getName() : "null").append("\",");
        sb.append("\"Agg\":").append(aggression.getValue()).append(",");
        sb.append("\"For\":").append(foraging.getValue()).append(",");
        sb.append("\"Str\":").append(strength.getValue()).append(",");
        sb.append("\"Tgh\":").append(toughness.getValue()).append(",");
        sb.append("\"Siz\":").append(size != null ? size.getValue() : 0).append(",");
        sb.append("\"Spd\":").append(speed != null ? speed.getValue() : 0).append(",");
        sb.append("\"Hth\":").append(getHealth()).append(",");
        sb.append("\"Eng\":").append(getEnergy()).append(",");
        sb.append("\"Location\":").append(getLocation() != null ? getLocation().toString() : "null");
        sb.append("}");
        return sb.toString();
    }
}
