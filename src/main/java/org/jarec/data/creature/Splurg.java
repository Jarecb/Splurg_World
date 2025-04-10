package org.jarec.data.creature;

import com.google.common.annotations.VisibleForTesting;
import org.jarec.data.Heading;
import org.jarec.data.Location;
import org.jarec.data.creature.attributes.*;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Splurg extends Life {
    private static final Logger log = LoggerFactory.getLogger(Splurg.class);

    private final Aggression aggression = new Aggression();
    private final Foraging foraging = new Foraging();
    private final Strength strength = new Strength();
    private final Toughness toughness = new Toughness();
    private Size size;
    private Speed speed;
    private Location location;
    private Heading heading = Heading.getRandomHeading();

    public Splurg() {
        location = new Location(0, 0); // TODO needs to be nest location
        commonSetup();
    }

    public Splurg (Splurg parent1, Splurg parent2){
        if (parent1.getAggression().getValue() == parent2.getAggression().getValue()){
            aggression.setValue(parent1.getAggression().getValue());
        }

        if (parent1.getForaging().getValue() == parent2.getForaging().getValue()){
            foraging.setValue(parent1.getForaging().getValue());
        }

        if (parent1.getStrength().getValue() == parent2.getStrength().getValue()){
            strength.setValue(parent1.getStrength().getValue());
        }

        if (parent1.getToughness().getValue() == parent2.getToughness().getValue()){
            toughness.setValue(parent1.getToughness().getValue());
        }

        location = new Location((parent1.getLocation().getX() + parent2.getLocation().getX()) / 2,
                (parent1.getLocation().getY() + parent2.getLocation().getY()) / 2);

        commonSetup();
    }

    private void commonSetup() {
        size = new Size(toughness, strength);
        speed = new Speed(size);

        setAgeAtBirth();
        setMaxHealth();
        log.info("A Splurg is spawned {}", this);
    }

    private void setAgeAtBirth() {
        setAge(Integer.parseInt((PropertyHandler.get("splurg.default.spawn.age", "100"))));
    }

    private void setMaxHealth() {
        setMaxHealth(Integer.parseInt((PropertyHandler.get("splurg.default.base.health", "10")))
            + toughness.getValue());
    }

    public void move(){
        // TODO make it move
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

    Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append("Agg:");
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
        sb.append("]");

        return sb.toString();
    }
}
