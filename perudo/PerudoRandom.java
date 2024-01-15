package com.nikita.perudo;

import java.util.Random;

public class PerudoRandom extends Random {
    public int getRandomInRange(int low, int high) {
        return nextInt(high+1 - low) + low;
    }

    public int getRandomInRangeExclusive(int low, int high, int overlook) {
        if (overlook == high)
            return getRandomInRange(low, overlook - 1);
        else if (overlook == low || nextBoolean())
            return getRandomInRange(overlook + 1, high);
        else
            return getRandomInRange(low, overlook - 1);
    }

    public boolean getRandom(int chance) {
        if (chance < 0 || chance > 100) {
            throw new IllegalArgumentException("chance must be in [0; 100]");
        }

        int randNum = nextInt(100) + 1;
        return randNum <= chance;
    }
}
