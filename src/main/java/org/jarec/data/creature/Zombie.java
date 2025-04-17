package org.jarec.data.creature;

import org.jarec.data.Location;
import org.jarec.data.creature.attributes.*;
import org.jarec.game.GameLoop;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;
import org.json.JSONObject;

public class Zombie extends Splurg {
    private final Aggression aggression = new Aggression();
    private final Strength strength = new Strength();
    private final Toughness toughness = new Toughness();
    private Size size;
    private Speed speed;
    private boolean active = false;
    private int activationDelay = Integer.parseInt(PropertyHandler.get("zombie.default.activation.delay", "20"));

    public Zombie(Splurg source) {
        super();

        JSONObject sourceJson = new JSONObject(source.toString());

        aggression.setValue(Integer.parseInt(PropertyHandler.get("zombie.default.aggression", "10")));

        strength.setValue(sourceJson.getInt("Str"));
        toughness.setValue(sourceJson.getInt("Tgh"));
        setMaxHealth(Integer.parseInt(PropertyHandler.get("zombie.default.health", "20")));

        JSONObject sourceLocation = sourceJson.getJSONObject("Location");
        setLocation(new Location(sourceLocation.getInt("x"), sourceLocation.getInt("y")));

        size = new Size(toughness, strength);
        speed = new Speed(size, Integer.parseInt(PropertyHandler.get("zombie.default.speed.multiplier", "2")));
        name = "Zombie";

        setZombie(true);

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
    public boolean canBreed() {
        return false;
    }

    @Override
    public void depositEnergy() {
    }
}
