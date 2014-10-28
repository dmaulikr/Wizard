package com.wizardfight.recognition;

import java.io.Serializable;
import java.util.ArrayList;

public class KMeansQuantizer implements Serializable {// extends
    // FeatureExtraction{

    private static final long serialVersionUID = 4L;

    boolean trained = false;
    boolean featureDataReady = false;
    boolean initialized;
    int numClusters;
    int minNumEpochs = 0;
    int maxNumEpochs = 100;
    int numInputDimensions = 0;
    int numOutputDimensions = 0;
    double minChange = 1.0e-5;
    final MatrixDouble clusters = new MatrixDouble();
    private final String featureExtractionType;
    private String classType = "";
    private final ArrayList<Double> featureVector = new ArrayList<Double>();
    ArrayList<Double> quantizationDistances = new ArrayList<Double>();

    /**
     * Default constructor. Initalizes the KMeansQuantizer, setting the number
     * of input dimensions and the number of clusters to use in the quantization
     * model.
     *
     * @param int numClusters: the number of quantization clusters
     */
    public KMeansQuantizer(final int numClusters) {
        this.numClusters = numClusters;
        classType = "KMeansQuantizer";
        featureExtractionType = classType;

        featureVector.add(0.0);
    }

    public int quantize(ArrayList<Double> inputVector) {
        // Find the minimum cluster
        double minDist = Double.MAX_VALUE;
        int quantizedValue = 0;

        for (int k = 0; k < numClusters; k++) {
            // Compute the squared Euclidean distance
            quantizationDistances.add(k, 0.0);
            for (int i = 0; i < numInputDimensions; i++) {
                double val = quantizationDistances.get(k);
                val += Math.pow(inputVector.get(i) - clusters.dataPtr[k][i], 2);
                quantizationDistances.set(k, val);
            }

            if (quantizationDistances.get(k) < minDist) {
                minDist = quantizationDistances.get(k);
                quantizedValue = k;
            }
        }
        featureVector.set(0, (double) quantizedValue);
        featureDataReady = true;

        return quantizedValue;
    }

    public void refresh() {
        quantizationDistances = new ArrayList<Double>();
    }

    public ArrayList<Double> getFeatureVector() {
        return featureVector;
    }
}
