package com.nikita.perudo;

import java.util.Objects;

import static com.nikita.perudo.Table.game;

public class Round {
    private static int roundCounter = 0;

    private final int id;
    private final boolean finalMode;
    private int currentNumber;
    private byte currentValue;
    private boolean maputoMode = false;
    private boolean over = false;


    public Round() {
        finalMode = game.getPlayers().size() == 2;
        id = ++roundCounter;
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(int currentNumber) {
        this.currentNumber = currentNumber;
    }

    public byte getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(byte currentValue) {
        this.currentValue = currentValue;
    }

    public boolean isMaputoMode() {
        return maputoMode;
    }

    public void setMaputoMode() {
        this.maputoMode = true;
    }

    public boolean isOver() {
        return over;
    }

    public void over() {
        this.over = true;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Round round)) return false;
        return id == round.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isNotFinalMode() {
        return !finalMode;
    }
}
