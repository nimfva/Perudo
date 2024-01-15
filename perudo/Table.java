package com.nikita.perudo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Table {
    //TODO do parallel
    //TODO add work with exceptions and change methods' accessible
    //TODO add equals and hashcode to classes (mb by id)
    public static final Table game = new Table();
    public static final PerudoRandom random = new PerudoRandom();
    public static final Scanner scanner = new Scanner(System.in);
    public static final int classicBet = 100;


    private final List<Player> players = new ArrayList<>();
    private final List<Player> spectators = new ArrayList<>();
    private int firstPlayerIndex; //TODO replace with iterator
    private int currentPlayerIndex;
    private final int bet;
    private int monetaryBank; //TODO release as class
    private Round currentRound;
    private int totalDiceCount;

    public Table() {
        monetaryBank = 0;
        bet = classicBet;
    }

    public static void main(String[] args) {
        System.out.print("enter your name: ");
        String playerName = scanner.next();
        game.players.add(new Player(playerName));

        System.out.print("enter player count: ");
        int playerCount = scanner.nextInt();
        for (int i = 0; i < playerCount - 1; ++i) {
            game.players.add(new Bot("Bot-" + i));
        }

        game.start();
    }

    public void start() {
        prepareMonetaryBank();
        totalDiceCount = players.size() * Player.dicesPerPlayer;

        firstPlayerIndex = random.nextInt(players.size());
        while (players.size() > 1) {
            currentRound = new Round();

            players.get(firstPlayerIndex).receiveFirstInstructions();

            currentPlayerIndex = firstPlayerIndex;
            incrementCurrentPlayerIndex();

            while (!currentRound.isOver()) {
                players.get(currentPlayerIndex).receiveInstructions();
                incrementCurrentPlayerIndex();
            }

        }

        //TODO change bet system
        Player winner = players.get(0);

        payPrize(winner);

        System.out.printf("%n%s win! He left %d dice and won %d money.%n",
                winner.getName(),
                winner.getDice().size(),
                bet * getTotalPlayerCount());
    }

    private int countDice() {
        int valueCount = 0;

        for (Player player : players) {
            for (Dice dice : player.getDice()) {
                byte value = dice.getValue();
                if (value == currentRound.getCurrentValue() || value == 1 && !currentRound.isMaputoMode())
                    ++valueCount;
            }
        }

        return valueCount;
    }

    private void updateDice() {
        for (Player player : players) {
            for (Dice dice : player.getDice()) {
                dice.setRandomValue();
            }
        }
    }

    private void incrementCurrentPlayerIndex() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private void decrementCurrentPlayerIndex() {
        currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
    }

    private void prepareMonetaryBank() {
        //TODO release as a transaction
        for (Player player: players) {
            player.decreaseMoney(bet);
        }

        addToMonetaryBank(players.size() * bet);

        System.out.println("the stake is: " + game.getMonetaryBank());
    }

    public void continueRound(int number, byte value) {
        currentRound.setCurrentNumber(number);
        currentRound.setCurrentValue(value);
    }

    public void dudoRound() {
        showHands();

        if (countDice() < currentRound.getCurrentNumber())
            decrementCurrentPlayerIndex();

        removeDice();
        cancelRound();
    }

    public void calzaRound() {
        showHands();

        if (countDice() == currentRound.getCurrentNumber())
            gainDice();
        else
            removeDice();

        cancelRound();
    }

    private void gainDice() {
        try {
            Player calzaPlayer = players.get(currentPlayerIndex);
            calzaPlayer.gainOneDice();
            ++totalDiceCount;

            System.out.printf("player %s gains one dice, now he has %d dice%n",
                    calzaPlayer.getName(),
                    calzaPlayer.getDice().size());
        } catch (IllegalCallerException ice) {
            System.out.println(ice.getMessage());
        }
    }
    private void removeDice() {
        Player losingPlayer = players.get(currentPlayerIndex);

        --totalDiceCount;
        if (losingPlayer.removeOneDiceAndCheck()) {
            players.remove(currentPlayerIndex);
            spectators.add(losingPlayer);

            currentPlayerIndex %= players.size();

            System.out.printf("player %s is out of the game, now he is spectator%n", losingPlayer.getName());
        }

        if (!losingPlayer.isDefeated())
            System.out.printf("player %s lost his dice, now he has %d dice%n",
                    losingPlayer.getName(),
                    losingPlayer.getDice().size());
    }
    private void cancelRound() {
        currentRound.over();
        firstPlayerIndex = currentPlayerIndex;

        updateDice();

        if (players.size() > 1)
            System.out.printf("player %s starts a new round%n%n", players.get(firstPlayerIndex).getName());
    }

    public void showHands() {
        for (Player player : players) {
            System.out.printf("%s's hand: ", player.getName());
            Player.printlnHand(player);
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    public int getFirstPlayerIndex() {
        return firstPlayerIndex;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public int getMonetaryBank() {
        return monetaryBank;
    }

    private void addToMonetaryBank(int stake) {
        if (stake <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        monetaryBank += stake;
    }

    private void payPrize(Player winner) {
        winner.increaseMoney(monetaryBank);
        monetaryBank = 0;
    }

    public int getBet() {
        return bet;
    }

    public int getTotalDiceCount() {
        return totalDiceCount;
    }

    public int getTotalPlayerCount() {
        return players.size() + spectators.size();
    }
}
