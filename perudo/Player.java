package com.nikita.perudo;

import java.util.LinkedList;
import java.util.List;

import static com.nikita.perudo.Table.game;
import static com.nikita.perudo.Table.scanner;

public class Player {
    //TODO maybe add Spectator class and common interface
    public static final int dicesPerPlayer = 5;

    private final List<Dice> dice = new LinkedList<>();
    private String name;
    private int money = Table.classicBet;
    private boolean neverDeclaredMaputo = true;
    private boolean defeated = false;

    public Player(String name) {
        for (int i = 0; i < dicesPerPlayer; ++i) {
            dice.add(new Dice());
        }

        this.name = name;
    }

    public static void printlnHand(Player player) {
        for (Dice dice : player.getDice()) {
            System.out.print(dice.getValue() + " ");
        }
        System.out.println();
    }

    public void receiveFirstInstructions() {
        System.out.print("here is your hand: ");
        printlnHand(this);

        if (dice.size() == 1 && neverDeclaredMaputo && game.getCurrentRound().isNotFinalMode()) {
            System.out.print("do you want to play Maputo? (enter m / c): ");
            String mode = scanner.next();
            if ("m".equalsIgnoreCase(mode))
                declareMaputo();
            else if (!"c".equalsIgnoreCase(mode))
                receiveFirstInstructions();
        }

        System.out.print("let's bid!\n");
        getGuess(true);
    }

    public void receiveInstructions() {
        System.out.print("here is your hand: ");
        printlnHand(this);

        System.out.print("choose: let's raise! (press b) or I doubt! (press d) ");
        if (dice.size() < dicesPerPlayer && !game.getCurrentRound().isMaputoMode()) {
            System.out.print(" or exactly! (press c) ");
        }

        String choice = scanner.next();

        if ("b".equalsIgnoreCase(choice))
            getGuess(false);
        else if ("c".equalsIgnoreCase(choice)) {
            try {
                calza();
            } catch (IllegalCallerException ice) {
                System.out.println(ice.getMessage());
                receiveInstructions();
            }
        }
        else if ("d".equalsIgnoreCase(choice))
            dudo();
        else {
            System.out.println("you entered invalid command, please retry");
            receiveInstructions();
        }

    }

    private void getGuess(boolean isFirst) {
        System.out.print("number and value: ");
        int number = scanner.nextInt();
        byte value = scanner.nextByte();

        try {
            if (isFirst)
                startRound(number, value);
            else
                bid(number, value);

        } catch (IllegalArgumentException iae) {
            System.out.print(iae.getMessage() + ", please enter correct ");
            getGuess(isFirst);
        }
    }

    protected void startRound(int number, byte value) {
        if (value < 1 || value > 6 || number < 1) {
            throw new IllegalArgumentException("you entered wrong number or value");
        }
        if (value == 1 && !game.getCurrentRound().isMaputoMode()) {
            throw new IllegalArgumentException("you can start by bidding on ones only in a Maputo round");
        }

        game.continueRound(number, value);
    }

    protected void bid(int number, byte value) {
        if (value < 1 || value > 6 || number < 1) {
            throw new IllegalArgumentException("you entered wrong number or value");
        }
        if (game.getCurrentRound().isMaputoMode() && value != game.getCurrentRound().getCurrentValue()) {
            throw new IllegalArgumentException("you cannot change value in a Maputo round");
        }
        if (game.getCurrentRound().getCurrentValue() == 1 && value != 1 &&
                number < 2 * game.getCurrentRound().getCurrentNumber() + 1) {
            throw new IllegalArgumentException("when you bid from ones to another, min number is (2 * n + 1)");
        }
        if (game.getCurrentRound().getCurrentValue() != 1 && value == 1 &&
                number < (game.getCurrentRound().getCurrentNumber() + 1) / 2) {
            throw new IllegalArgumentException("when you bid from another to ones, min number is ((n + 1) / 2)");
        }
        if (value != 1) {
            if (number < game.getCurrentRound().getCurrentNumber()
                    || number == game.getCurrentRound().getCurrentNumber()
                    && value <= game.getCurrentRound().getCurrentValue())
                throw new IllegalArgumentException("you must raise either number or value / you cannot decrease number");
        }


        game.continueRound(number, value);
    }

    protected void dudo() {
        game.dudoRound();
    }

    protected void calza() {
        if (dice.size() == dicesPerPlayer || game.getCurrentRound().isMaputoMode()) {
            throw new IllegalCallerException("you cannot declare Calza");
        }

        game.calzaRound();
    }

    protected void declareMaputo() {
        game.getCurrentRound().setMaputoMode();
        neverDeclaredMaputo = false;
        System.out.println("Maputo mode enabled.");
    }

    public boolean hasNeverDeclaredMaputo() {
        return neverDeclaredMaputo;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Dice> getDice() {
        return dice;
    }

    boolean removeOneDiceAndCheck() {
        dice.remove(0);
        if (dice.isEmpty()) {
            defeated = true;
        }

        return defeated;
    }

    void gainOneDice() {
        if (dice.size() == dicesPerPlayer) {
            throw new IllegalCallerException("player already has got maximum number of dice");
        }

        dice.add(0, new Dice());
    }

    public int getMoney() {
        return money;
    }

    public void increaseMoney(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        money += amount;
    }

    public void decreaseMoney(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (money < amount) {
            throw new IllegalArgumentException("player does not have enough money");
        }

        money -= amount;
    }
}
