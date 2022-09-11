package ch.evolutionsoft.poker.calculator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Result implements Serializable {

  private Integer totalSimulatedHands = Integer.valueOf(0);
  private List<Double> winningPercentages = new ArrayList<>();
  private List<Double> losingPercentages = new ArrayList<>();
  private List<Double> tyingPercentages = new ArrayList<>();
  private List<Double> evValues = new ArrayList<>();
  private List<String> evaluatedHandsString;

  /**
   * High / Low additional
   */
  private List<Double> winningLowPercentages = new ArrayList<>();
  private List<Double> losingLowPercentages = new ArrayList<>();
  private List<Double> tyingLowPercentages = new ArrayList<>();
  private List<Double> winningHighAndLow = new ArrayList<>();
  private int numberOfLowPots = 0;

  public Result(int[][] relativeRanks, int[] values, double[] evValues, List<String> evaluatedHandsStrings, boolean isHighLow) {
    
    if (isHighLow) {
      this.initHighLow(relativeRanks, values, evValues, evaluatedHandsStrings);
    } else {
      this.initHigh(relativeRanks, values, evValues, evaluatedHandsStrings);
    }
  }

  private void initHighLow(int[][] relativeRanks, int[] values,
      double[] evValues, List<String> evaluatedHandsStrings) {
    
    this.initCommon(values, evValues, evaluatedHandsStrings);
    this.initHigh(relativeRanks, values, relativeRanks[0].length / 2);
    this.initLow(relativeRanks, values);
  }

  private void initHigh(int[][] relativeRanks, int[] values, double[] evValues, List<String> evaluatedHandsStrings) {
    
    this.initCommon(values, evValues, evaluatedHandsStrings);
    this.initHigh(relativeRanks, values, relativeRanks[0].length);
  }
  
  protected void initCommon(int[] values, double[] evValues,
      List<String> evaluatedHandsStrings) {
    this.evaluatedHandsString = evaluatedHandsStrings;
    
    for (double currentEV : evValues) {
      
      this.evValues.add(currentEV);
    }
    
    this.totalSimulatedHands = this.getNumberOfSimulatedHands(values);
  }

  private void initHigh(int[][] relativeRanks, int[] values, int numberOfPlayers) {
    
    int[] totalPlayerWins = new int[numberOfPlayers];
    int[] totalPlayerLoses = new int[numberOfPlayers]; 
    int[] totalPlayerTies = new int[numberOfPlayers]; 
    
    for (int n = 0; n < relativeRanks.length; n++) {
      
      List<Integer> winningIndices = this.getWinningPlayerIndices(relativeRanks[n], numberOfPlayers);
      
      if (winningIndices.size() == 1) {
        
          totalPlayerWins[winningIndices.get(0)] += values[n];
        
      } else {
        for (int currentWinningIndex : winningIndices) {
          
          totalPlayerTies[currentWinningIndex] += values[n];
        }
      }
      for (int currentLosingIndex = 0; currentLosingIndex < numberOfPlayers; currentLosingIndex++) {
        
        if ( !winningIndices.contains(currentLosingIndex) ) {
          totalPlayerLoses[currentLosingIndex] += values[n];
        }
      }
    }
    
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {
      
      this.winningPercentages.add(totalPlayerWins[playerNumber] / (double) this.totalSimulatedHands);
      this.losingPercentages.add(totalPlayerLoses[playerNumber] / (double) this.totalSimulatedHands);
      this.tyingPercentages.add(totalPlayerTies[playerNumber] / (double) this.totalSimulatedHands);
    }
  }
  
  private void initLow(int[][] relativeRanksHighAndLow, int[] values) {

    int numberOfPlayers = relativeRanksHighAndLow[0].length / 2;

    int[] totalPlayerWinsLow = new int[numberOfPlayers];
    int[] totalPlayerLosesLow = new int[numberOfPlayers]; 
    int[] totalPlayerTiesLow = new int[numberOfPlayers];
    
    int[] totalPlayerScoops = new int[numberOfPlayers];
    
    for (int n = 0; n < relativeRanksHighAndLow.length; n++) {
      
      int[] relativeRanksLow = new int[numberOfPlayers];
      System.arraycopy(relativeRanksHighAndLow[n], numberOfPlayers, relativeRanksLow, 0, numberOfPlayers);
      List<Integer> winningLowIndices = this.getWinningPlayerIndices(relativeRanksLow, numberOfPlayers);
      
      if (!winningLowIndices.isEmpty()) {
        // Low results
        if (winningLowIndices.size() == 1) {
  
            totalPlayerWinsLow[winningLowIndices.get(0)] += values[n];
            
        } else {
          for (int currentWinningIndex : winningLowIndices) {
            
            totalPlayerTiesLow[currentWinningIndex] += values[n];
          }
        }
        for (int currentLosingIndex = 0; currentLosingIndex < numberOfPlayers; currentLosingIndex++) {
          
          if ( relativeRanksLow[currentLosingIndex] != numberOfPlayers && !winningLowIndices.contains(currentLosingIndex) ) {
            totalPlayerLosesLow[currentLosingIndex] += values[n];
          }
        }
        this.numberOfLowPots += values[n];
      }
      List<Integer> winningHighIndices = this.getWinningPlayerIndices(relativeRanksHighAndLow[n], numberOfPlayers);
      // Scoop result
      if (winningHighIndices.size() == 1 && (winningLowIndices.isEmpty() || (winningLowIndices.size() == 1 && winningLowIndices.get(0).equals(winningHighIndices.get(0))) ) ) {
          
        totalPlayerScoops[winningHighIndices.get(0)] += values[n];
      }
    }   
    for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++) {

      this.winningLowPercentages.add(totalPlayerWinsLow[playerNumber] / (double) numberOfLowPots);
      this.losingLowPercentages.add(totalPlayerLosesLow[playerNumber] / (double) numberOfLowPots);
      this.tyingLowPercentages.add(totalPlayerTiesLow[playerNumber] / (double) numberOfLowPots);

      this.winningHighAndLow.add(totalPlayerScoops[playerNumber] / (double) this.totalSimulatedHands);
    }
  }

  public Integer getTotalSimulatedHands() {
    return totalSimulatedHands;
  }
  
  public List<String> getEvaluatedHandsString() {
    return evaluatedHandsString;
  }

  public List<Double> getWinningPercentages() {
    return winningPercentages;
  }

  public List<Double> getLosingPercentages() {
    return losingPercentages;
  }

  public List<Double> getTyingPercentages() {
    return tyingPercentages;
  }

  public List<Double> getEVValues() {
    return this.evValues;
  }

  public List<Double> getWinningLowPercentages() {
    return winningLowPercentages;
  }

  public List<Double> getLosingLowPercentages() {
    return losingLowPercentages;
  }

  public List<Double> getTyingLowPercentages() {
    return tyingLowPercentages;
  }

  public List<Double> getWinningHighAndLow() {
    return winningHighAndLow;
  }
  
  public int getNumberOfLowPots() {
    return numberOfLowPots;
  }

  public String getLowPotsPercentage() {
    
    double lowPotsRatio = (double) this.numberOfLowPots / this.totalSimulatedHands;
    double lowPotsPercentage = (double) Math.round( lowPotsRatio * 10000) / 100;
    return Double.toString(lowPotsPercentage);
  }
  
  private List<Integer> getWinningPlayerIndices(int[] orderKeys, int numberOfPlayers) {
    
        List<Integer> winningIndices = new ArrayList<>();
    
    for (int index = 0; index < numberOfPlayers; index++) {
      
      if (orderKeys[index] == 0) {
        winningIndices.add(index);
      }
    }
    return winningIndices;
  }
  
  private int getNumberOfSimulatedHands(int[] values) {
    
    int total = 0;
    
    for (int currentValue : values) {
      
      total += currentValue;
    }
    
    return total;
  }
}
