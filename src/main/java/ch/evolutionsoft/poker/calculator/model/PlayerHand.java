package ch.evolutionsoft.poker.calculator.model;

import java.io.Serializable;

import org.pokersource.game.Deck;

public class PlayerHand implements Serializable{

	private static final long serialVersionUID = -5674194695738698954L;

	private Card[] hand;
	
	public PlayerHand(Calculator calculator, int numberOfHoleCards) {

		this.hand = new Card[numberOfHoleCards];
		for (int index = 0; index < this.hand.length; index++) {
			this.hand[index] = new Card();
			this.hand[index].setCalculator(calculator);
		}
	}

	
	public void setCards(Card[] cards) {
		
		this.hand = cards;
	}
	
	public Card[] getCards() {
		
		return this.hand;
	}

	public int getNumberOfCards() {
	  
	  return this.hand.length;
	}
	
	public String getCard(int i) {
		
		return this.hand[i].getValue();
	}
	
	public boolean containsCard(String value) {
		
		Card cardToCheck = new Card();
		cardToCheck.setValue(value);
		
		for (Card card : this.hand) {
			
			if (card.equals(cardToCheck)) {
				return true;
			}
			
		}
		return false;
	}
	
	public boolean isValid() {
		
		for (Card card : this.hand) {
			
			if (!card.isValid()) {
				
				return false;
			}
		}
		return true;
	}
	
	public boolean isEmpty() {
		
		for (int index = 0; index < this.hand.length; index++) {
			
			if (this.hand[index].getValue() != null) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isComplete() {

		for (int index = 0; index < this.hand.length; index++) {
			
			if (this.hand[index].getValue() == null) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isValidAndComplete() {

		return this.isValid() && this.isComplete();
	}
	
	public long getMask() {
		
		return Deck.parseCardMask(this.toString());
	}
	
	public String toString() {
		
		String result = "";
		for (Card card : this.hand) {
			
			if (card.getValue() == null || card.getValue().isEmpty()) {
				
				result += "null "; 
			} else {
				
				result += card.toString() + " ";
			}
		}
		return result.substring(0, result.length() - 1);
	}
}