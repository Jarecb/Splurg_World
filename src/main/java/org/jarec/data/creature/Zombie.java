package org.jarec.data.creature;

import org.jarec.data.Location;
import org.jarec.data.creature.attributes.Size;
import org.jarec.data.creature.attributes.Speed;
import org.jarec.game.GameLoop;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.GameMath;
import org.jarec.util.PropertyHandler;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;

public class Zombie extends Splurg {
    private boolean active = false;
    private int activationDelay = Integer.parseInt(PropertyHandler.get("zombie.default.activation.delay", "20"));

    public Zombie(Splurg source) {
        JSONObject sourceJson = new JSONObject(source.toString());

        aggression.setValue(Integer.parseInt(PropertyHandler.get("zombie.default.aggression", "10")));

        int str = sourceJson.getInt("Str");
        strength.setValue(str + Integer.parseInt(PropertyHandler.get("zombie.default.strength.bonus", "1")));
        int tgh = sourceJson.getInt("Tgh");
        toughness.setValue(tgh);
        setMaxHealth(Integer.parseInt(PropertyHandler.get("zombie.default.health", "20")));

        JSONObject sourceLocation = sourceJson.getJSONObject("Location");
        setLocation(new Location(sourceLocation.getInt("x"), sourceLocation.getInt("y")));

        size = new Size(toughness, strength);
        speed = new Speed(size, Integer.parseInt(PropertyHandler.get("zombie.default.speed.multiplier", "2")));
        name = "Zombie " + source.getName();

        setHomeHive(Hives.getZombieHive());
        setZombie(true);
        setHeading(null);

        Splurgs.getInstance().addSplurg(this);

        var statusMessage = "A Zombie Splurg was created on turn " + GameLoop.getInstance().getTurn();
        WorldFrame.getInstance().updateStatus(statusMessage);
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

        int aggressionMultiplier = Integer.parseInt(PropertyHandler.get("splurg.default.aggression.multiplier", "5"));

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
}
