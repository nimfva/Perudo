package com.nikita.perudo;

import java.util.Objects;

import static com.nikita.perudo.Table.random;

public class Dice {
    private byte value;

    public Dice() {
        setRandomValue();
    }

    public void setRandomValue() {
        value = (byte) (random.nextInt(6) + 1);
    }

    public byte getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dice dice)) return false;
        return value == dice.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
