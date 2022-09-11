package ch.evolutionsoft.poker.calculator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pokersource.game.Deck;

public class Board implements Serializable {

  private static final long serialVersionUID = -5358787319613157298L;
  private List<Card> cards = new ArrayList<>();

  public Board() {

    this.init();
  }

  public void init() {

    cards.add(new Card());
    cards.add(new Card());
    cards.add(new Card());
    cards.add(new Card());
    cards.add(new Card());
  }

  public List<Card> getCards() {

    return this.cards;
  }

  public boolean hasValidSize() {

    int numberOfBoardCards = this.getValidCardsNumber();

    return numberOfBoardCards == 0 || numberOfBoardCards >= 3 && numberOfBoardCards <= 5;
  }

  public int getValidCardsNumber() {

    int number = 0;

    for (Card card : this.cards) {

      String cardValue = card.getValue();
      if (cardValue != null && Card.isValidValue(cardValue)) {

        number++;
      }
    }
    return number;
  }

  public long parseBoard() {

    String stringValue = this.toString();

    if (!stringValue.isEmpty()) {

      return Deck.parseCardMask(stringValue);
    }
    return 0L;
  }

  public void removeCard(int index) {

    this.cards.get(index).setValue(null);
  }

  public String toString() {

    StringBuilder result = new StringBuilder();
    for (Card card : this.cards) {
      if (card.getValue() != null && card.isValid()) {
        result.append(card.toString()).append(" ");
      }
    }
    if (result.length() > 1) {
      return result.substring(0, result.length() - 1);
    }
    
    return result.toString();
  }
}
