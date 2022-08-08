package ch.evolutionsoft.poker.calculator.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.pokersource.enumerate.Enumerate;
import org.pokersource.game.Deck;

import ch.evolutionsoft.poker.calculator.util.PokersourceLibraryLoader;

import static ch.evolutionsoft.poker.calculator.model.CalculatorConstants.*;

@Named("calculator")
@ViewScoped
public class Calculator implements Serializable {

	private static final long serialVersionUID = 6369666854255203618L;

	static {

		PokersourceLibraryLoader.init();
	}

	private double[] evValues = new double[0];
	private int[][][] orderKeys;
	private int[][] orderValues;

	private List<PlayerHand> hands = new LinkedList<PlayerHand>();

	private Board board;

	private Result result;
	private List<List<String>> cardPaths = new ArrayList<List<String>>();

	private static List<String> orderedGameTypes = new ArrayList<String>();
	static {
		orderedGameTypes.add(Integer.toString(Enumerate.GAME_OMAHA));
		orderedGameTypes.add(Integer.toString(Enumerate.GAME_OMAHA8));
		orderedGameTypes.add(Integer.toString(Enumerate.GAME_HOLDEM));
	}

	private static Map<String, SelectItem> gameTypes = new HashMap<String, SelectItem>();
	static {
		gameTypes.put(Integer.toString(Enumerate.GAME_OMAHA),
		    new SelectItem(Integer.valueOf(Enumerate.GAME_OMAHA), "Omaha High"));
		gameTypes.put(Integer.toString(Enumerate.GAME_OMAHA8),
		    new SelectItem(Integer.valueOf(Enumerate.GAME_OMAHA8), "Omaha Hi/Lo"));
		gameTypes.put(Integer.toString(Enumerate.GAME_HOLDEM),
		    new SelectItem(Integer.valueOf(Enumerate.GAME_HOLDEM), "Texas Hold'em"));
	}

	private int gameType = Enumerate.GAME_OMAHA;

	private int numberOfHoleCards = 4;
	private String selectedGameType = Integer.toString(Enumerate.GAME_OMAHA);

	@PostConstruct
	public void init() {

		this.clearResult();

		this.initPlayers();
		this.initBoard();
		this.updateCardPaths();
	}

	public String clearAll() throws IOException {

		this.init();
		
		return "success";
	}
	
	public String calculate() {

		this.result = null;

		long[] longPockets = new long[this.hands.size()];
		List<Long> tempPockets = new ArrayList<Long>();
		long longBoardValue = 0L;
		int numberOfValidHands = 0;
		List<String> validHandsStrings = new ArrayList<String>();

		Set<String> doubledCards = this.containsCardSeveralTimes();
		if (!doubledCards.isEmpty()) {

			FacesMessage facesMessage = new FacesMessage("Card(s) " + doubledCards.toString() + " used more than once");
			facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
			FacesContext.getCurrentInstance().addMessage(null, facesMessage);
			return "failure";
		}

		for (int i = 0; i < this.hands.size(); i++) {

			PlayerHand currentHand = this.hands.get(i);

			if (!currentHand.isValidAndComplete()) {
				FacesMessage facesMessage = new FacesMessage("Player " + (i + 1) + " has invalid or incomplete cards");
				facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
				FacesContext.getCurrentInstance().addMessage(null, facesMessage);

				return "failure";

			} else {

				numberOfValidHands++;
			}
		}

		if (numberOfValidHands <= 0) {

			FacesMessage facesMessage = new FacesMessage("Please enter valid cards for at least one player");
			facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
			FacesContext.getCurrentInstance().addMessage(null, facesMessage);

			return "failure";
		}

		for (int i = 0; i < numberOfValidHands; i++) {

			PlayerHand currentHand = this.hands.get(i);

			String handString = currentHand.toString();
			validHandsStrings.add(handString);
			long pocket = Deck.parseCardMask(handString);
			tempPockets.add(pocket);
		}

		this.evValues = new double[numberOfValidHands];
		this.orderKeys = new int[1][][];
		this.orderValues = new int[1][];

		longPockets = new long[numberOfValidHands];

		for (int n = 0; n < numberOfValidHands; n++) {

			longPockets[n] = tempPockets.get(n);
		}

		if (this.board.hasValidSize()) {

			longBoardValue = this.board.parseBoard();
		} else {

			FacesContext.getCurrentInstance().addMessage(null,
			    new FacesMessage("Board has invalid number of cards, supported are 0, 3, 4 or 5 cards"));
			return "failure";
		}

		try {
			synchronized (Enumerate.class) {
				Enumerate.PotEquity(this.gameType, 0, longPockets, longBoardValue, 0L, this.evValues, this.orderKeys,
				    this.orderValues);
			}

			boolean isHiLow = this.isHighLow();
			this.result = new Result(orderKeys[0], orderValues[0], evValues, validHandsStrings, isHiLow);
			return "success";

		} catch (UnsatisfiedLinkError ule) {

			FacesMessage msg = new FacesMessage("Calculator Engine currently unavaliable, please try again later.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "failure";

		} catch (Exception ex) {

			FacesMessage msg = new FacesMessage(
			    "An unexpected Exception occured:" + ex.getCause() + "hands: " + this.hands + "longHandValues: "
			        + Arrays.toString(longPockets) + "board: " + this.board + "longBoardValue: " + longBoardValue);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "failure";
		}
	}

	public boolean isHighLow() {

		if (this.gameType == Enumerate.GAME_OMAHA8) {

			return true;
		}
		return false;
	}

	public void addCard() {

		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();

		String path = (String) requestMap.get("currentPath");

		if (path != null && !path.equals(Card.EMPTY_IMAGE_PATH)) {

			int indexOfCardValueBegin = path.indexOf("card") + "card".length();
			String cardValue = path.substring(indexOfCardValueBegin, indexOfCardValueBegin + 2);

			updateAvailableCards(cardValue);
		}
	}

	public void removePlayerCard() {

		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();

		int playerIndex = Integer.parseInt(requestMap.get("playerParam"));
		int cardIndex = Integer.parseInt(requestMap.get("cardParam"));

		this.hands.get(playerIndex).getCards()[cardIndex].setValue(null);

		this.updateCardPaths();
	}

	public void removeBoardCard() {

		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();

		int index = Integer.parseInt(requestMap.get("cardParam"));

		this.board.removeCard(index);

		this.updateCardPaths();
	}

	public void addPlayer() {

		if (this.hands.size() < MAX_PLAYER_SIZE) {
			this.hands.add(new PlayerHand(this, this.numberOfHoleCards));
		}
	}

	public void removePlayer() {

		if (this.hands.size() > 0) {
			this.hands.remove(this.hands.size() - 1);
			this.updateCardPaths();
		}
	}

	public void clearResult() {

		this.result = null;
	}

	public String getSelectedGameType() {

		return this.selectedGameType;
	}

	public void setSelectedGameType(String type) {

		this.selectedGameType = type;
	}

	public void selectedGameTypeChange(ValueChangeEvent event) {

		String newValue = (String) event.getNewValue();
		int gameType = Integer.parseInt(newValue);
		this.setGameType(gameType);
		this.init();
	}

	public Collection<SelectItem> getGameTypes() {

		List<SelectItem> result = new ArrayList<SelectItem>();

		for (String gameType : orderedGameTypes) {

			result.add(gameTypes.get(gameType));
		}

		return result;
	}

	public int getGameType() {

		return gameType;
	}

	public void setGameType(int gameType) {

		this.gameType = gameType;
		if (gameType == Enumerate.GAME_HOLDEM) {
			this.numberOfHoleCards = TEXAS_HOLE_CARDS;
		} else {
			this.numberOfHoleCards = OMAHA_HOLE_CARDS;
		}
	}

	public int getNumberOfHoleCards() {

		return numberOfHoleCards;
	}

	public List<Card> getBoardCards() {

		return this.board.getCards();
	}

	public List<PlayerHand> getHands() {

		return this.hands;
	}

	public String getTotalSimulatedHands() {

		if (this.result != null) {

			return this.result.getTotalSimulatedHands().toString();
		}
		return null;
	}

	public String getTotalLowHands() {

		if (this.result != null) {

			return String.valueOf(this.result.getNumberOfLowPots());
		}
		return null;
	}

	public String getLowHandsPercentage() {

		if (this.result != null) {

			return this.result.getLowPotsPercentage();
		}
		return null;
	}

	public List<List<String>> getPlayerResults() {

		if (this.result == null) {

			return null;
		}

		if (this.isHighLow()) {

			return highLowResult();
		} else {

			return initHighResult();
		}
	}

	void updateAvailableCards(String cardValue) {

		for (PlayerHand hand : this.hands) {

			for (Card card : hand.getCards()) {

				if (card.getValue() == null) {

					card.setValue(cardValue);

					this.updateCardPaths();

					return;
				}
			}

		}

		for (Card card : this.board.getCards()) {

			if (card.getValue() == null) {

				card.setValue(cardValue);

				this.updateCardPaths();

				return;
			}
		}
	}

	List<List<String>> highLowResult() {

		List<List<String>> results = new ArrayList<List<String>>();

		int numberOfEvaluatedPlayers = this.result.getEVValues().size();

		List<Double> winningHighFactors = this.result.getWinningPercentages();
		List<Double> losingHighFactors = this.result.getLosingPercentages();
		List<Double> tyingHighFactors = this.result.getTyingPercentages();
		List<Double> scoopFactors = this.result.getWinningHighAndLow();
		List<Double> winningLowFactors = this.result.getWinningLowPercentages();
		List<Double> losingLowFactors = this.result.getLosingLowPercentages();
		List<Double> tyingLowFactors = this.result.getTyingLowPercentages();
		List<Double> evValues = this.result.getEVValues();
		List<String> evaluatedHandsStrings = this.result.getEvaluatedHandsString();

		for (int playerNumber = 0; playerNumber < numberOfEvaluatedPlayers; playerNumber++) {

			List<String> currentPlayerResults = new ArrayList<String>();

			double winBothRate = scoopFactors.get(playerNumber);
			double readableScoopValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * winBothRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableScoopValue));

			double winHighRate = winningHighFactors.get(playerNumber);
			double readableWinHighValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * winHighRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableWinHighValue));

			double loseHighRate = losingHighFactors.get(playerNumber);
			double readableLoseHighValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * loseHighRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableLoseHighValue));

			double tieHighRate = tyingHighFactors.get(playerNumber);
			double readableTieHighValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * tieHighRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableTieHighValue));

			double winLowRate = winningLowFactors.get(playerNumber);
			double readableWinLowValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * winLowRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableWinLowValue));

			double loseLowRate = losingLowFactors.get(playerNumber);
			double readableLoseLowValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * loseLowRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableLoseLowValue));

			double tieLowRate = tyingLowFactors.get(playerNumber);
			double readableTieLowValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * tieLowRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableTieLowValue));

			double ev = evValues.get(playerNumber);
			double readableEVValue = ((double) Math.round(MULTIPLIER_THOUSAND * ev)) / MULTIPLIER_THOUSAND;
			currentPlayerResults.add(Double.toString(readableEVValue));

			currentPlayerResults.add(evaluatedHandsStrings.get(playerNumber));

			results.add(currentPlayerResults);
		}
		return results;
	}

	List<List<String>> initHighResult() {
		List<List<String>> results = new ArrayList<List<String>>();

		int numberOfEvaluatedPlayers = this.result.getEVValues().size();

		List<Double> winningFactors = this.result.getWinningPercentages();
		List<Double> losingFactors = this.result.getLosingPercentages();
		List<Double> tyingFactors = this.result.getTyingPercentages();
		List<Double> evValues = this.result.getEVValues();
		List<String> evaluatedHandsStrings = this.result.getEvaluatedHandsString();

		for (int playerNumber = 0; playerNumber < numberOfEvaluatedPlayers; playerNumber++) {

			List<String> currentPlayerResults = new ArrayList<String>();

			double winRate = winningFactors.get(playerNumber);
			double readableWinValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * winRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableWinValue));

			double loseRate = losingFactors.get(playerNumber);
			double readableLoseValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * loseRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableLoseValue));

			double tieRate = tyingFactors.get(playerNumber);
			double readableTieValue = ((double) Math.round(MULTIPLIER_TEN_THOUSAND * tieRate)) / MULTIPLIER_HUNDRED;
			currentPlayerResults.add(Double.toString(readableTieValue));

			double ev = evValues.get(playerNumber);
			double readableEVValue = ((double) Math.round(MULTIPLIER_THOUSAND * ev)) / MULTIPLIER_THOUSAND;
			currentPlayerResults.add(Double.toString(readableEVValue));

			currentPlayerResults.add(evaluatedHandsStrings.get(playerNumber));

			results.add(currentPlayerResults);
		}
		return results;
	}

	public List<List<String>> getCardImagePaths() {

		return this.cardPaths;
	}

	public Set<String> containsCardSeveralTimes() {

		Set<String> chosenValues = new HashSet<String>();
		Set<String> doubleValues = new HashSet<String>();

		for (PlayerHand hand : this.hands) {
			for (Card card : hand.getCards()) {

				String currentValue = card.getValue();
				if (currentValue != null) {

					if (chosenValues.contains(currentValue)) {

						doubleValues.add(currentValue);
					} else {

						chosenValues.add(currentValue);
					}
				}
			}
		}
		for (Card card : this.board.getCards()) {

			String currentValue = card.getValue();
			if (currentValue != null) {

				if (chosenValues.contains(currentValue)) {

					doubleValues.add(currentValue);
				} else {

					chosenValues.add(currentValue);
				}
			}
		}
		return doubleValues;
	}

	public boolean containsCard(String value) {

		for (PlayerHand hand : this.hands) {

			if (hand.containsCard(value)) {
				return true;
			}
		}

		Card cardToCheck = new Card();
		cardToCheck.setValue(value);
		for (Card card : this.board.getCards()) {

			if (card.equals(cardToCheck)) {
				return true;
			}
		}
		return false;
	}

	void initPlayers() {

		this.hands = new LinkedList<>();
		this.hands.add(new PlayerHand(this, this.numberOfHoleCards));
		this.hands.add(new PlayerHand(this, this.numberOfHoleCards));
	}

	void initBoard() {

		this.board = new Board();

		for (Card card : this.board.getCards()) {

			card.setValue(null);
			card.setCalculator(this);
		}
	}

	void updateCardPaths() {

		List<List<String>> allCardValues = Card.getAllCardValues();
		this.cardPaths.clear();

		for (List<String> currentSuitList : allCardValues) {

			List<String> currentCardList = new ArrayList<String>();
			for (String currentCard : currentSuitList) {

				if (this.containsCard(currentCard)) {

					currentCardList.add(Card.EMPTY_IMAGE_PATH);

				} else {
					currentCardList.add("images/card" + currentCard.toLowerCase() + ".jpg");
				}
			}

			this.cardPaths.add(currentCardList);
		}
	}

	void initCardPaths() {

		List<List<String>> allCardValues = Card.getAllCardValues();
		this.cardPaths.clear();

		for (List<String> currentSuitList : allCardValues) {

			List<String> currentCardList = new ArrayList<String>();
			for (String currentCard : currentSuitList) {
				currentCardList.add("images/card" + currentCard.toLowerCase() + ".jpg");

			}

			this.cardPaths.add(currentCardList);
		}
	}
}
