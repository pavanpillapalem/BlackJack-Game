import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

public class BlackjackNoGUI {

    static class Card {
        int value;
        String name;

        Card(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);

        ArrayList<Card> cardDeck = new ArrayList<>();
        String[] cardNames = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};
        int[] cardValues = {11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};

        for (int suitIndex = 0; suitIndex < 4; suitIndex++) {
            for (int cardIndex = 0; cardIndex < 13; cardIndex++) {
                cardDeck.add(new Card(cardValues[cardIndex], cardNames[cardIndex]));
            }
        }

        Collections.shuffle(cardDeck);

        System.out.println("=================================");
        System.out.println("   Welcome to Budget Blackjack   ");
        System.out.println("=================================");

        int playerCount = 0;

        while (playerCount < 1) {
            System.out.print("\nHow many players are playing (not including the dealer)? ");

            if (inputScanner.hasNextInt()) {
                playerCount = inputScanner.nextInt();
                inputScanner.nextLine();

                if (playerCount < 1) {
                    System.out.println("[!] Please enter at least 1 player.");
                }
            }
            else {
                System.out.println("[!] Please enter a valid number.");
                inputScanner.nextLine();
            }
        }

        // If more than 6 players, switch to an infinite randomized deck so we never run out of cards
        boolean infiniteDeck = playerCount > 6;

        if (infiniteDeck) {
            System.out.println("\n[Note: Large game detected. Switching to infinite randomized deck mode.]");
        }

        String[] playerNames = new String[playerCount];
        int[] playerBalances = new int[playerCount];

        for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {
            System.out.print("Enter name for Player " + (playerIndex + 1) + ": ");
            playerNames[playerIndex] = inputScanner.nextLine().trim();

            if (playerNames[playerIndex].isEmpty()) {
                playerNames[playerIndex] = "Player " + (playerIndex + 1);
            }

            playerBalances[playerIndex] = 2500;
        }

        System.out.println("\nGood luck to everyone!");

        boolean gameRunning = true;

        while (gameRunning) {

            // Reshuffle the deck if there isn't enough cards
            if (!infiniteDeck && cardDeck.size() < 10 * playerCount) {
                cardDeck.clear();

                for (int suitIndex = 0; suitIndex < 4; suitIndex++) {
                    for (int cardIndex = 0; cardIndex < 13; cardIndex++) {
                        cardDeck.add(new Card(cardValues[cardIndex], cardNames[cardIndex]));
                    }
                }

                Collections.shuffle(cardDeck);
                System.out.println("\n[Deck reshuffled]");
            }

            int[] playerBets = new int[playerCount];
            boolean[] playerActive = new boolean[playerCount];

            System.out.println("\n=== Betting Phase ===");

            // Make sure the bet isn't invalid from the players who still have money
            for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {

                if (playerBalances[playerIndex] <= 0) {
                    System.out.println(playerNames[playerIndex] + " is out of money and cannot play this round.");
                    playerActive[playerIndex] = false;
                    continue;
                }

                playerActive[playerIndex] = true;

                while (true) {

                    System.out.println(playerNames[playerIndex] + " has $" + playerBalances[playerIndex] + ".");
                    System.out.print(playerNames[playerIndex] + ", how much would you like to bet? ");

                    if (!inputScanner.hasNextInt()) {
                        System.out.println("[!] Please enter a valid number.");
                        inputScanner.nextLine();
                        continue;
                    }

                    int betAmount = inputScanner.nextInt();
                    inputScanner.nextLine();

                    if (betAmount <= 0 || betAmount > playerBalances[playerIndex]) {
                        System.out.println("[!] Invalid bet. Please bet between $1 and $" + playerBalances[playerIndex]);
                    }
                    else {
                        playerBets[playerIndex] = betAmount;
                        break;
                    }
                }
            }

            System.out.println("\n--- Dealing Cards ---");

            // Arrays to keep track of each player's hand total and cards
            int[] playerTotals = new int[playerCount];
            int[] playerAces = new int[playerCount];
            Card[] playerCardOne = new Card[playerCount];
            Card[] playerCardTwo = new Card[playerCount];

            // At the start deal two cards
            for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {

                if (!playerActive[playerIndex]) {
                    continue;
                }

                playerCardOne[playerIndex] = drawCard(cardDeck, cardNames, cardValues, infiniteDeck);

                if (playerCardOne[playerIndex].name.equals("Ace")) {
                    playerAces[playerIndex]++;
                }

                playerCardTwo[playerIndex] = drawCard(cardDeck, cardNames, cardValues, infiniteDeck);

                if (playerCardTwo[playerIndex].name.equals("Ace")) {
                    playerAces[playerIndex]++;
                }

                playerTotals[playerIndex] = playerCardOne[playerIndex].value + playerCardTwo[playerIndex].value;

                // Aces adjusting hardest part only runs if the hand would bust and has an ace
                while (playerTotals[playerIndex] > 21 && playerAces[playerIndex] > 0) {
                    playerTotals[playerIndex] -= 10;
                    playerAces[playerIndex]--;
                }
            }

            // Deal two cards to the dealer
            int dealerAceCount = 0;
            Card dealerCardOne = drawCard(cardDeck, cardNames, cardValues, infiniteDeck);

            if (dealerCardOne.name.equals("Ace")) {
                dealerAceCount++;
            }

            Card dealerCardTwo = drawCard(cardDeck, cardNames, cardValues, infiniteDeck);

            if (dealerCardTwo.name.equals("Ace")) {
                dealerAceCount++;
            }

            int dealerTotal = dealerCardOne.value + dealerCardTwo.value;

            while (dealerTotal > 21 && dealerAceCount > 0) {
                dealerTotal -= 10;
                dealerAceCount--;
            }

            // Hide second card and only print the first card
            System.out.println("Dealer shows: " + dealerCardOne.name);

            boolean[] naturalBlackjack = new boolean[playerCount];

            // Taking turns
            for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {

                if (!playerActive[playerIndex]) {
                    continue;
                }

                System.out.println("\n--- " + playerNames[playerIndex] + "'s Turn ---");
                System.out.println(playerNames[playerIndex] + " was dealt: " + playerCardOne[playerIndex].name + " and " + playerCardTwo[playerIndex].name);
                System.out.println(playerNames[playerIndex] + "'s total: " + playerTotals[playerIndex]);

                // If there's a blackjack at the start of the game let the player know and skip their turn since they don't need any more cards
                if (playerTotals[playerIndex] == 21) {
                    System.out.println("Blackjack!");
                    naturalBlackjack[playerIndex] = true;
                    continue;
                }

                // Hit or stand for each player this is looped
                while (playerTotals[playerIndex] < 21) {
                    System.out.print("\n" + playerNames[playerIndex] + ", do you want to (h)it or (s)tand? ");
                    String actionInput = inputScanner.nextLine().toLowerCase();

                    if (actionInput.equals("h")) {
                        Card newCard = drawCard(cardDeck, cardNames, cardValues, infiniteDeck);

                        if (newCard.name.equals("Ace")) {
                            playerAces[playerIndex]++;
                        }

                        playerTotals[playerIndex] += newCard.value;

                        // Fix the aces sends it back to the other loop if a new card causes a bust
                        while (playerTotals[playerIndex] > 21 && playerAces[playerIndex] > 0) {
                            playerTotals[playerIndex] -= 10;
                            playerAces[playerIndex]--;
                        }

                        System.out.println(playerNames[playerIndex] + " drew a " + newCard.name + ". New total: " + playerTotals[playerIndex]);
                    }
                    else if (actionInput.equals("s")) {
                        break;
                    }
                    else {
                        System.out.println("Please enter 'h' to hit or 's' to stand.");
                    }
                }
            }

            // Dealer adds a second card or hits until 17
            System.out.println("\n--- Dealer's Turn ---");
            System.out.println("Dealer reveals second card: " + dealerCardTwo.name);
            System.out.println("Dealer total: " + dealerTotal);

            while (dealerTotal < 17) {
                System.out.println("\nDealer chooses to hit...");
                Card newCard = drawCard(cardDeck, cardNames, cardValues, infiniteDeck);

                if (newCard.name.equals("Ace")) {
                    dealerAceCount++;
                }

                dealerTotal += newCard.value;

                while (dealerTotal > 21 && dealerAceCount > 0) {
                    dealerTotal -= 10;
                    dealerAceCount--;
                }

                System.out.println("Dealer draws a " + newCard.name + ". Dealer total: " + dealerTotal);
            }

            System.out.println("\nFinal Dealer total: " + dealerTotal);

            // Distribute and take away money based off who busted etc compare player hand to dealer
            System.out.println("\n=== Round Results ===");

            for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {

                if (!playerActive[playerIndex]) {
                    continue;
                }

                int playerTotal = playerTotals[playerIndex];
                int betAmount = playerBets[playerIndex];

                System.out.print(playerNames[playerIndex] + ": ");

                if (playerTotal > 21) {
                    System.out.println("Busted! Dealer wins. (-$" + betAmount + ")");
                    playerBalances[playerIndex] -= betAmount;
                }
                else if (naturalBlackjack[playerIndex]) {

                    if (dealerTotal == 21) {
                        System.out.println("Push! Dealer also has blackjack.");
                    }
                    else {
                        // Make it realistic if player gets a blackjack which is an ace and a face card you get 1.5x whatever you bet
                        int winnings = (int)(betAmount * 1.5);
                        System.out.println("Blackjack! Wins 3:2! (+$" + winnings + ")");
                        playerBalances[playerIndex] += winnings;
                    }
                }
                else if (dealerTotal > 21) {
                    System.out.println("Dealer busted! You win! (+$" + betAmount + ")");
                    playerBalances[playerIndex] += betAmount;
                }
                else if (playerTotal > dealerTotal) {
                    System.out.println("You win! (+$" + betAmount + ")");
                    playerBalances[playerIndex] += betAmount;
                }
                else if (playerTotal < dealerTotal) {
                    System.out.println("Dealer wins. (-$" + betAmount + ")");
                    playerBalances[playerIndex] -= betAmount;
                }
                else {
                    System.out.println("Push!");
                }

                System.out.println("  Balance: $" + playerBalances[playerIndex]);
            }

            // Check which players still have money to bet
            gameRunning = false;

            for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {
                if (playerBalances[playerIndex] > 0) {
                    gameRunning = true;
                }
            }

            if (!gameRunning) {
                System.out.println("\nAll players are out of money! Game over.");
                break;
            }

            System.out.print("\nPlay another round? (yes/no): ");
            String replayInput = inputScanner.nextLine().toLowerCase();

            if (replayInput.equals("no")) {
                break;
            }
        }

        System.out.println("\n=================================");
        System.out.println("         Final Balances          ");

        for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {
            System.out.println("  " + playerNames[playerIndex] + ": $" + playerBalances[playerIndex]);
        }

        System.out.println("   Thanks for playing!");
        System.out.println("=================================");
    }

    // Draws a card from the deck. In infinite mode picks a random card each time instead of from the deck
    static Card drawCard(ArrayList<Card> cardDeck, String[] cardNames, int[] cardValues, boolean infiniteDeck) {
        if (infiniteDeck) {
            int randomIndex = (int)(Math.random() * 13);
            return new Card(cardValues[randomIndex], cardNames[randomIndex]);
        }
        else {
            return cardDeck.remove(0);
        }
    }
}
