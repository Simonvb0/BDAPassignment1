/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved. Proprietary, do not copy or distribute
 * without permission. Written by Pieter Robberechts, 2021
 */

import java.util.*;

import static java.lang.Long.sum;
import static java.lang.Math.log;

/** This class is a stub for VFDT. */
public class VfdtNode {

  private VfdtNode[] children; /* child children (null if node is a leaf) */

  private final int[] possibleSplitFeatures; /* The features that this node can split on */

  private int splitFeature; /* splitting feature */

  private int[][][] nijk; /* instance counts (see paper) */

  private int[] nbFeatureValues;

  private int totalOnes;

  private int totalZeros;

  private int[] childIDs = null;

  /* FILL IN HERE */

  /**
   * Create and initialize a leaf node.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param nbFeatureValues are the nb of values for each feature in this node. If a feature has k
   *     values, then the values are [0:k-1].
   */
  public VfdtNode(int[] nbFeatureValues, int[] possibleSplitFeatures) {
    this.possibleSplitFeatures = possibleSplitFeatures;
    this.nbFeatureValues = nbFeatureValues;
    splitFeature = -1;
    children = null;
    int maxSizeFeature = Arrays.stream(nbFeatureValues).max().getAsInt();
    nijk = new int[nbFeatureValues.length][maxSizeFeature][2];
    totalZeros = 0; totalOnes = 0;
  }

  public int[][][] getNijk(){
    return nijk;
  }

  public int[] getPossibleSplitFeatures(){
    return possibleSplitFeatures;
  }

  public void setSplitFeature(int f){
    splitFeature = f;
  }

  /**
   * Turn a leaf node into a internal node.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param splitFeature is the feature to test on this node.
   * @param nodes are the children (the index of the node is the value of the splitFeature).
   */
  public void addChildren(int splitFeature, VfdtNode[] nodes) {
    if (nodes == null) throw new IllegalArgumentException("null children");
//    nbSplits++;

    // Add the feature on which was split.
    this.splitFeature = splitFeature;

    int n = nbFeatureValues[splitFeature];
    children = new VfdtNode[n];
    for (int i = 0; i < n; i++){
      //new VfdtNode(this.nbFeatureValues,possibleFeatures)
      children[i] = nodes[i];
    }
    nijk = null;
  }

  /**
   * Returns the leaf node corresponding to the test attributeValues.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param example is the test attributeValues to sort.
   */
  public VfdtNode sortExample(Integer[] example) {
    if (splitFeature == -1){
      return this;
    }else{
      return children[example[splitFeature]].sortExample(example);
    }
  }

  /**
   * Split evaluation method (function G in the paper)
   *
   * <p>Compute a splitting score for the feature featureId. For now, we'll use information gain,
   * but this may be changed. You can test your code with other split evaluations, but be sure to
   * change it back to information gain in the submitted code and for the experiments with default
   * values.
   *
   * @param featureId is the feature to be considered.
   */
  public double splitEval(int featureId) {
    return informationGain(featureId, nijk);
  }

  /**
   * Compute the information gain of a feature for this leaf node.
   *
   * <p>THIS METHOD IS REQUIRED.
   *
   * @param featureId is the feature to be considered.
   * @param nijk are the instance counts.
   */
  public static double informationGain(int featureId, int[][][] nijk) {
    double ig = 0;
    // Step 1: find amount of examples and amount belonging to class c.
    double totalEx = 0;
    int amountOfClasses = nijk[0][0].length;
    double[] amountPerClass = new double[amountOfClasses];
    for(int j = 0; j < nijk[featureId].length; j++){
      for(int k = 0; k < nijk[featureId][j].length; k++){
        totalEx += nijk[featureId][j][k];
        amountPerClass[k] += nijk[featureId][j][k];
      }
    }
    if(totalEx == 0){
      return 0;
    }
    double currentEntropy = calculateEntropy(amountPerClass, totalEx);

//    // Step 2: find all positions of the value of the and all possible values of featureID.
//    int[] allPositions = new int[totalEx];
//    int currentPosition = 0;
//    int prevPosition = 0;
//    HashSet<Integer> allValues = new HashSet<>();
//    for(int j = 0; j < nijk[featureId].length; j++){
//      if (nijk[featureId][j] != null){
//        for(int k = 0; k < nijk[featureId][j].length; k++){
//          currentPosition += nijk[featureId][j][k]-1;
//          for(int a = prevPosition; a <= currentPosition; a++){
//            allPositions[a] = j;
//            allValues.add(j);
//          }
//          prevPosition = currentPosition+1;
//        }
//      }
//    }


    // Step 3: iterate over all nijk and create the nijk of all subsets.
    double[][] allClassesOfSubSets = new double[nijk[featureId].length][amountOfClasses];
//    int index = 0;
    for(int j = 0; j < nijk[featureId].length; j++){
      for(int k = 0; k < nijk[featureId][j].length; k++){
        if(nijk[featureId][j][k] != 0){
          allClassesOfSubSets[j][k] += nijk[featureId][j][k];
//          index += nijk[featureId][j][k];
        }
      }
    }

//    allPositions = null;

    // Step 4: calculate information gain
//    double currentEntropy = calculateEntropy(amountPerClass, totalEx);
    double entropySubsets = 0;
    for (int i = 0; i < amountPerClass.length; i++){
      int amountSubset = 0;
      for(int o = 0; o < allClassesOfSubSets[i].length; o++){
        amountSubset += allClassesOfSubSets[i][o];
      }
      double cei = calculateEntropy(allClassesOfSubSets[i], amountSubset);
      entropySubsets -= cei*(amountSubset/totalEx);
    }
    ig = currentEntropy + entropySubsets;
    return ig;
  }

  private static double calculateEntropy(double[] amountPerClass, double S) {
    double ce = 0;
    for (int i = 0; i < amountPerClass.length; i++){
      double pi = amountPerClass[i]/S;
      if(pi != 0){
        ce -= pi* (log(pi)/log(2));
      }
    }
    return ce;
  }


  /**
   * Return the visualization of the tree.
   *
   * <p>DO NOT CHANGE THIS METHOD.
   *
   * @return Visualization of the tree
   */
  public String getVisualization(String indent) {
    if (children == null) {
      return indent + "Leaf\n";
    } else {
      String visualization = "";
      for (int v = 0; v < children.length; v++) {
        visualization += indent + splitFeature + "=" + v + ":\n";
        visualization += children[v].getVisualization(indent + "| ");
      }
      return visualization;
    }
  }


  public int getNijkSize() {
    int f = possibleSplitFeatures[0];
    int totalEx = 0;
    for(int j = 0; j < nijk[0].length; j++){
      for(int k = 0; k < nijk[0][j].length; k++){
        totalEx += nijk[f][j][k];
      }
    }
    return totalEx;
  }

  public int getSplitFeature() {
    return splitFeature;
  }

  public double getTotalOnes() {
    return totalOnes;
  }

  public double getTotalZeros() {
    return totalZeros;
  }

  public void update(int c) {
    if(c == 0){
      totalZeros += 1;
    }else{
      totalOnes += 1;
    }
  }

  public VfdtNode[] getChildren() {
    return children;
  }

  private int id;

  public void setID(int nbSplits) {
    id = nbSplits;
  }

  public int getID() {
    return id;
  }

  public void setNijk(int[][][] n) {
    nijk = n;
  }

  public void setChildIDs(int[] c) {
    childIDs = c;
  }

  public int[] getChildIDs() {
    return childIDs;
  }
}
