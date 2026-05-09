package ch.evolutionsoft.poker.calculator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.pokersource.enumerate.Enumerate;
import org.pokersource.game.Deck;

import ch.evolutionsoft.poker.calculator.util.PokersourceLibraryLoader;

class HoleCardsTest {

  public static final String CLUBS = "c";
  public static final int DEUCE = 2;

  double[] evValues = new double[1];
  int[][][] orderKeys = new int[1][][];
  int[][] orderValues = new int[1][];
 
  @BeforeAll
  static void initializeLibrary() {
    PokersourceLibraryLoader.init();
  }

  @Test
  void testFourHoleCardsOmahaHiSinglePlayer() {
    
    final int holeCardsNumber = 4;
    final long pocket = parseHoleCards(holeCardsNumber);

    Enumerate.PotEquity(Enumerate.GAME_OMAHA, 0, new long[] {pocket}, 0L, 0L, evValues, orderKeys,
        orderValues);
    
    assertEquals(1, evValues.length);
  }

  @Disabled("Currently not supported by poker-eval library")
  @Test
  void testFiveHoleCardsOmahaHiSinglePlayer() {

    final int holeCardsNumber = 5;
    final long pocket = parseHoleCards(holeCardsNumber);

    Enumerate.PotEquity(Enumerate.GAME_OMAHA, 0, new long[] {pocket}, 0L, 0L, evValues, orderKeys,
        orderValues);
    
    assertEquals(1, evValues.length);
  }

  private long parseHoleCards(final int holeCardsNumber) {
 
    final PlayerHand currentHand = new PlayerHand(null, holeCardsNumber);
    currentHand.setCards(createHoleCards(holeCardsNumber));
    
    String handString = currentHand.toString();
    long pocket = Deck.parseCardMask(handString);
    return pocket;
  }
  
  private Card[] createHoleCards(final int holeCardsNumber) {
 
    final Card[] cards = new Card[holeCardsNumber];
    for (int n = 0; n < holeCardsNumber; n++) {
      cards[n] = new Card();
      cards[n].setValue((DEUCE + n) + CLUBS);
    }
    
    return cards;
  }
}
