import java.io.*;
import java.util.*;

public class Classifier {

    // Add fields here
    private ClassifierNode root;
    
    /**
     * Loads a classifier from a preorder-format file.
     * 
     * @param sc a scanner object reading a preorder-format file
     */
    public Classifier(Scanner sc) {
        if (sc == null) throw new IllegalArgumentException("Scanner object is null.");
        this.root = readPreorder(sc);
        if (this.root == null) throw new IllegalArgumentException("Tree is empty after read.");
    }

    public Classifier(List<TextBlock> data, List<String> results) {
        //  Remove the exception and implement this method
        throw new RuntimeException("Not yet implemented: Classifier(List<TextBlock> Data, List<String> results)");
    }

    public String classify(TextBlock input) {
        // TODO: Remove the exception and implement this method
        throw new RuntimeException("Not yet implemented: classify(TextBlock input)");
    }

    public void save(PrintStream ps) {
        // TODO: Remove the exception and implement this method
        throw new RuntimeException("Not yet implemented: save(PrintStream output)");
    }

    /**
     * Reads a preorder-format file to construct a classification tree.
     * 
     * @param sc a scanner reading the preorder-format file
     * @return a ClassifierNode for the current root of the tree, or null if empty.
     * @throws IllegalArgumentException if the preorder-format file is incorrectly formatted.
     */
    private ClassifierNode readPreorder(Scanner sc) {
        if (!sc.hasNextLine()) return null;
        String line = sc.nextLine();

        // Handle empty lines by going until non-empty, or early return
        while (line.isEmpty() && sc.hasNextLine()) {
            line = sc.nextLine();
        }
        if (line.isEmpty()) return null;

        if (!line.startsWith("Feature: ")) {
            throw new IllegalArgumentException("Invalid scanner file.");
        }
        String feature = line.substring("Feature: ".length());
        
        if (!sc.hasNextLine()) {
            throw new IllegalArgumentException("Invalid scanner file.");
        }

        String thresholdLine = sc.nextLine();
        if (!thresholdLine.startsWith("Threshold: ")) {
            throw new IllegalArgumentException("Invalid scanner file.");
        }
        double threshold = Double.parseDouble(thresholdLine.substring("Threshold: ".length()));
        
        ClassifierNode left = readPreorder(sc);
        ClassifierNode right = readPreorder(sc);
        
        return new ClassifierNode(feature, threshold, left, right);
    }

    /**
     * A class representing a node in a classification tree.
     */
    public static class ClassifierNode {
        // For decision node
        public final String feature;
        public final double threshold;

        // For leaf node
        public final String label;
        public final TextBlock initialBlock;

        public ClassifierNode left;
        public ClassifierNode right;

        /**
         * Constructs a decision node.
         * 
         * @param feature the feature being checked and decided upon
         * @param threshold the value which a choice is determined upon
         * @param left the left decision for values less than the threshold
         * @param right the right decision for values equal or greater than the threshold
         */
        ClassifierNode(String feature, double threshold, ClassifierNode left, ClassifierNode right) {
            this.feature = feature;
            this.threshold = threshold;
            this.left = left;
            this.right = right;
            this.label = "";
            this.initialBlock = null;
        }

        /**
         * Constructs a label node.
         * 
         * @param label the label of the resulting classification
         * @param initiaBlock the text block a decision was first made with
         */
        ClassifierNode(String label, TextBlock initiaBlock) {
            this.feature = "";
            this.threshold = 0.0;
            this.left = null;
            this.right = null;
            this.label = label;
            this.initialBlock = initiaBlock;
        }
    }


    ////////////////////////////////////////////////////////////////////
    // PROVIDED METHODS - **DO NOT MODIFY ANYTHING BELOW THIS LINE!** //
    ////////////////////////////////////////////////////////////////////

    // Helper method to calcualte the midpoint of two provided doubles.
    private static double midpoint(double one, double two) {
        return Math.min(one, two) + (Math.abs(one - two) / 2.0);
    }    

    // Behavior: Calculates the accuracy of this model on provided Lists of 
    //           testing 'data' and corresponding 'labels'. The label for a 
    //           datapoint at an index within 'data' should be found at the 
    //           same index within 'labels'.
    // Exceptions: IllegalArgumentException if the number of datapoints doesn't match the number 
    //             of provided labels
    // Returns: a map storing the classification accuracy for each of the encountered labels when
    //          classifying
    // Parameters: data - the list of TextBlock objects to classify. Should be non-null.
    //             labels - the list of expected labels for each TextBlock object. 
    //             Should be non-null.
    public Map<String, Double> calculateAccuracy(List<TextBlock> data, List<String> labels) {
        // Check to make sure the lists have the same size (each datapoint has an expected label)
        if (data.size() != labels.size()) {
            throw new IllegalArgumentException(
                    String.format("Length of provided data [%d] doesn't match provided labels [%d]",
                                  data.size(), labels.size()));
        }
        
        // Create our total and correct maps for average calculation
        Map<String, Integer> labelToTotal = new HashMap<>();
        Map<String, Double> labelToCorrect = new HashMap<>();
        labelToTotal.put("Overall", 0);
        labelToCorrect.put("Overall", 0.0);
        
        for (int i = 0; i < data.size(); i++) {
            String result = classify(data.get(i));
            String label = labels.get(i);

            // Increment totals depending on resultant label
            labelToTotal.put(label, labelToTotal.getOrDefault(label, 0) + 1);
            labelToTotal.put("Overall", labelToTotal.get("Overall") + 1);
            if (result.equals(label)) {
                labelToCorrect.put(result, labelToCorrect.getOrDefault(result, 0.0) + 1);
                labelToCorrect.put("Overall", labelToCorrect.get("Overall") + 1);
            }
        }

        // Turn totals into accuracy percentage
        for (String label : labelToCorrect.keySet()) {
            labelToCorrect.put(label, labelToCorrect.get(label) / labelToTotal.get(label));
        }
        return labelToCorrect;
    }
}
