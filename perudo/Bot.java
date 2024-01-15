package com.nikita.perudo;

import java.util.Arrays;

import static com.nikita.perudo.Table.game;
import static com.nikita.perudo.Table.random;

/**
 * <h2>Логика действий бота:</h2>
 * <p>
 * 1) Если у бота 3 и более одинаковых значений ИЛИ кубиков еще много (> 2/3), то он
 * основывается на теории вероятностей с учетом погрешностей;
 * <p>
 * 2) Иначе пытается блефовать (с шансом bluffChance%, иначе выполняет пункт 1):
 * пытается выдать, что у него хорошая комбинация (если кубик всего один, то врет о его значении), при этом
 * соблюдая пункт 1;
 * <p>
 * 3) Мапуто играет с шансом (100 - bluffChance)%;
 * <p>
 * 4) Кальза играет ближе к концу игры (кубиков < 1/3) c шансом bluffChance%
 **/

public class Bot extends Player {
    //TODO maybe add levels to bot
    private final int bluffChance;
    private final int[] valueCount = new int[6+1];
    private byte pocketValue;
    private int bluffNumber;
    private int rememberedRoundId = 0;

    public Bot(String name) {
        super(name);
        bluffChance = random.getRandomInRange(20, 50);
    }

    public static void thinkForNSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {}
    }

    public static int fitInto(int number, int low, int high) {
        return Math.max(low, Math.min(high, number));
    }

    @Override
    public void receiveFirstInstructions() {
        thinkForNSeconds(2);

        if (checkForNewRound())
            analyze(true);

        if (getDice().size() == 1 && hasNeverDeclaredMaputo() && game.getCurrentRound().isNotFinalMode()) {
            if (random.getRandom(100 - bluffChance))
                declareMaputo();
        }

        play(true);
    }

    @Override
    public void receiveInstructions() {
        thinkForNSeconds(1);

        if (checkForNewRound())
            analyze(false);

        play(false);
    }

    private void analyze(boolean isFirst) {
        precountValues(isFirst);

        int initialDiceCount = Player.dicesPerPlayer * game.getTotalPlayerCount();

        //TODO потом условие пересмотреть
        if (game.getTotalDiceCount() < (2.0 / 3) * initialDiceCount
                && valueCount[pocketValue] < 3
                && random.getRandom(bluffChance))
            bluffNumber = bluff(isFirst);
    }

    private int bluff(boolean isFirst) {
        if (getDice().size() == 1) {
            int low = !isFirst || game.getCurrentRound().isMaputoMode() ? 1 : 2;

            pocketValue = (byte) random.getRandomInRangeExclusive(low, 6, pocketValue);
        }

        return Math.min(getDice().size() - valueCount[pocketValue], 2);
    }

    private void play(boolean isFirst) {
        if (isFirst) {
            int high = Math.min(getProbablyLimit(pocketValue), 5);
            startRound(random.getRandomInRange(1, high), pocketValue);
            return;
        }

        byte currentValue = game.getCurrentRound().getCurrentValue();
        int currentNumber = game.getCurrentRound().getCurrentNumber();
        int limit = getProbablyLimit(currentValue);

        if (game.getCurrentRound().isMaputoMode()) {
            if (currentNumber >= limit)
                dudo();
            else
                prepareAndBid(currentValue);
        }

        else {
            if (currentNumber == limit
                    && getDice().size() < Player.dicesPerPlayer
                    && game.getTotalDiceCount() < (1.0 / 3) * Player.dicesPerPlayer * game.getTotalPlayerCount()
                    && random.getRandom(bluffChance)) //TODO mb bluff here is redundant
                calza();
            else if (currentNumber > limit)
                dudo();
            else if (getMinimum(pocketValue) <= getProbablyLimit(pocketValue))
                prepareAndBid(pocketValue);
            else if (currentNumber < limit)
                prepareAndBid(currentValue);
            else
                dudo();
        }
    }

    //TODO ТУТ СЛОЖНОВАТО
    private void precountValues(boolean isFirst) {
        pocketValue = 0;
        bluffNumber = 0;
        Arrays.fill(valueCount, 0);

        for (Dice dice : getDice()) {
            byte value = dice.getValue();
            ++valueCount[value];

            if (valueCount[value] >= valueCount[pocketValue]
                    && (game.getCurrentRound().isMaputoMode() || value != 1 || !isFirst)
                    && (pocketValue == 0 || random.getRandom(50))) //TODO ХУЁВОЕ УСЛОВИЕ, ШАНСЫ ЗНАЧЕНИЙ НЕ РАВНЫ
                pocketValue = value;
        }

        if (!game.getCurrentRound().isMaputoMode()) {
            for (byte value = 2; value <= 6; ++value) {
                valueCount[value] += valueCount[1];

                if (valueCount[value] >= valueCount[pocketValue]
                        && (pocketValue == 0 || random.getRandom(50))) //TODO ТУТ ТА ЖЕ ХУЙНЯ
                    pocketValue = value;
            }
        }
    }

    public int getMinimum(byte value) {
        byte currentValue = game.getCurrentRound().getCurrentValue();
        int currentNumber = game.getCurrentRound().getCurrentNumber();

        int minimum = value <= currentValue ? currentNumber + 1
                                            : currentNumber;

        if (value != currentValue)
            if (value == 1)
                minimum = (currentNumber + 1) / 2;
            else if (currentValue == 1)
                minimum = currentNumber * 2 + 1;

        return minimum;
    }

    public int getProbablyLimit(byte value) {
        int unknownDices = game.getTotalDiceCount() - getDice().size();
        int inaccuracy = 1; //TODO mb 2

        int diceLimit = valueCount[value];
        if (game.getCurrentRound().isMaputoMode() || value == 1)
            diceLimit += (int) Math.round(unknownDices / 6.0);
        else
            diceLimit += (int) Math.round(unknownDices / 3.0);

        diceLimit = fitInto(diceLimit + random.getRandomInRange(-inaccuracy, inaccuracy),
                            1, game.getTotalDiceCount());

        return diceLimit;
    }

    private boolean checkForNewRound() {
        boolean isNewRound = game.getCurrentRound().getId() != rememberedRoundId;

        rememberedRoundId = game.getCurrentRound().getId();

        return isNewRound;
    }

    @Override
    protected void startRound(int number, byte value) {
        System.out.printf("player %s bidded %d of %d's%n", getName(), number, value);

        super.startRound(number, value);
    }

    @Override
    protected void dudo() {
        System.out.printf("player %s doubted the bid%n", getName());

        super.dudo();
    }

    public void prepareAndBid(byte value) {
        int min = getMinimum(value);
        int max = getProbablyLimit(value);

        if (value == pocketValue)
            max += bluffNumber;

        if (max < min)
            System.out.println("какого хуя?"); //TODO ЗАТЫЧКА В ЖОПЕ, ЧЕКНИ В play() ПОЧЕМУ ТАК СЛУЧАЕТСЯ

        int high = fitInto(max - min, 0, 2);
        bid(min + random.getRandomInRange(0, high), value);
    }

    @Override
    protected void bid(int number, byte value) {
        System.out.printf("player %s bidded %d of %d's%n", getName(), number, value);

        super.bid(number, value);
    }

    @Override
    protected void calza() {
        System.out.printf("player %s allowed the bid%n", getName());

        super.calza();
    }
}
