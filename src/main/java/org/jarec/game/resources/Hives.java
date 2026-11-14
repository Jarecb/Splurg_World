package org.jarec.game.resources;

import org.jarec.data.Hive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hives {
    private static final Hives INSTANCE = new Hives();
    private static Hive zombieHive;

    private static final List<Hive> hiveList = Collections.synchronizedList(new ArrayList<>());

    private static int activeHiveCount;

    private Hives() {
    }

    public static Hives getInstance() {
        return INSTANCE;
    }

    public static Hive getZombieHive() {
        return zombieHive;
    }

    public static void addHive(Hive hive) {
        if (hive != null) {
            if (hive.isZombie()) {
                zombieHive = hive;
                return;
            }
            hiveList.add(hive);
            activeHiveCount++;
        }
    }

    public List<Hive> getHives() {
        synchronized (hiveList) {
            return hiveList.stream()
                    .filter(hive -> !hive.isZombie())
                    .toList();
        }
    }


    public void clearHives() {
        synchronized (hiveList) {
            hiveList.clear();
            zombieHive = null;
            activeHiveCount = 0;
        }
    }

    public void spawnHives() {
        synchronized (hiveList) {
            for (Hive hive : hiveList) {
                hive.spawn();
            }
        }
    }

    public int getHiveCount() {
        return activeHiveCount;
    }
}
