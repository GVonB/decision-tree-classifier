// Gunnar Von Bergen
// 08/19/2025
// CSE 123
// P3: Spam Classifier
// TA: Trien
import java.io.*;
import java.util.*;

/**
 * TODO:
 */
public class Classifier {

    private ClassifierNode root;
    
    /**
     * Loads a classifier from a preorder-format file.
     * The valid format is as follows (label has no prefix):
     * Feature: here
     * Threshold: 0.125
     * Label
     * 
     * @param sc a scanner object reading a preorder-format file
     * @throws IllegalArgumentException if sc is null.
     * @throws IllegalStateException if the tree is empty after reading the scanner.
     */
    public Classifier(Scanner sc) {
        if (sc == null) throw new IllegalArgumentException("Scanner object is null.");
        this.root = readPreorder(sc);
        if (this.root == null) throw new IllegalStateException("Tree is empty after read.");
    }

    public Classifier(List<TextBlock> data, List<String> labels) {
        if (data == null) throw new IllegalArgumentException("Data is null.");
        if (labels == null) throw new IllegalArgumentException("Labels is null.");

        if (data.isEmpty()) throw new IllegalArgumentException("Data is empty.");
        if (labels.isEmpty()) throw new IllegalArgumentException("Labels is empty.");

        if (data.size() != labels.size()) {
            throw new IllegalArgumentException("Data and Labels are not equal size.");
        }

        this.root = null;

        for (int i = 0; i < data.size(); i++) {
            this.root = buildTree(this.root, data.get(i), labels.get(i));
        }
    }

    private ClassifierNode buildTree(ClassifierNode node, TextBlock tb, String label) {
        if (node == null) return new ClassifierNode(label, tb);

        // If leaf, handle whether decision was accurate or not and update if needed
        if (node.isLeaf()) {
            // Correctly classified already
            if (node.label.equals(label)) {
                return node;
            }
            // Incorrectly classified, need to update decision node
            if (node.initialBlock == null) {
                throw new IllegalStateException("Node missing text block.");
            }
            String newFeature = tb.findBiggestDifference(node.initialBlock);
            double newThreshold = midpoint(tb.get(newFeature), node.initialBlock.get(newFeature));

            ClassifierNode newLeaf = new ClassifierNode(label, tb);
            ClassifierNode oldLeaf = new ClassifierNode(node.label, node.initialBlock);

            boolean newLeft = newLeaf.initialBlock.get(newFeature) < newThreshold;

            ClassifierNode leftChild = newLeft ? newLeaf : oldLeaf;
            ClassifierNode rightChild = newLeft ? oldLeaf : newLeaf;

            return new ClassifierNode(newFeature, newThreshold, leftChild, rightChild);
        }

        // Not at a leaf node, just make a decision based on threshold.
        if (tb.get(node.feature) < node.threshold) {
            node.left = buildTree(node.left, tb, label);
        } else {
            node.right = buildTree(node.right, tb, label);
        }
        return node;
    }

    /**
     * Classifies a text block by returning the determined label.
     * 
     * @param input the text block to be classified
     * @return the classification of the text block as a string.
     * @throws IllegalArgumentException if input is null.
     * @throws IllegalStateException if root node is null.
     */
    public String classify(TextBlock input) {
        if (input == null) throw new IllegalArgumentException("Input is null.");
        return classify(root, input);
    }

    /**
     * Returns a string representing what label the provided text block
     * is classified as based on the given decision tree root node.
     * 
     * @param node the current node of the decision tree
     * @param tb the text block being classified
     * @return the classification of the text block as a string.
     * @throws IllegalStateException if node is null.
     */
    private String classify(ClassifierNode node, TextBlock tb) {
        if (node == null) throw new IllegalStateException("Empty tree");
        if (node.isLeaf()) return node.label;

        double nodeThresholdValue = tb.get(node.feature);
        
        if (nodeThresholdValue < node.threshold) {
            return classify(node.left, tb);
        }

        return classify(node.right, tb);
    }

    /**
     * Saves a classifier in a preorder-format file.
     * Format is:
     * Feature: here
     * Threshold: 0.125
     * Label
     * 
     * @param ps the PrintStream to output to
     * @throws IllegalArgumentException if ps is null.
     */
    public void save(PrintStream ps) {
        if (ps == null) {
            throw new IllegalArgumentException("PrintStream object is null");
        }
        savePreorder(root, ps);
    }

    /**
     * Reads a preorder-format file to construct a classification tree.
     * The valid format is as follows (label has no prefix):
     * Feature: here
     * Threshold: 0.125
     * Label
     * 
     * @param sc a scanner reading the preorder-format file
     * @return a ClassifierNode for the current root of the tree, or null if empty.
     * @throws IllegalStateException if the preorder-format file is incorrectly formatted.
     */
    private ClassifierNode readPreorder(Scanner sc) {
        if (!sc.hasNextLine()) return null;
            String line = sc.nextLine();

            // Handle empty lines by going until non-empty, or early return
            while (line.isEmpty() && sc.hasNextLine()) {
                line = sc.nextLine();
            }
            if (line.isEmpty()) return null;

            // Handle feature and threshold at once, so if no, node must be a label
            if (line.startsWith("Feature: ")) {
                String feature = line.substring("Feature: ".length());
            
            if (!sc.hasNextLine()) {
                throw new IllegalStateException("Invalid scanner file.");
            }

            String thresholdLine = sc.nextLine();
            if (!thresholdLine.startsWith("Threshold: ")) {
                throw new IllegalStateException("Invalid scanner file.");
            }
            double threshold = Double.parseDouble(thresholdLine.substring("Threshold: ".length()));
            
            ClassifierNode left = readPreorder(sc);
            ClassifierNode right = readPreorder(sc);
            
            return new ClassifierNode(feature, threshold, left, right);
        } else {
            TextBlock initialNode = null;
            return new ClassifierNode(line, initialNode);
        }
    }

    /**
     * Saves a preorder-format output to a PrintStream representing the classifier.
     * The format follows:
     * Feature: here
     * Threshold: 0.125
     * Label
     * 
     * @param node the current node being handled
     * @param ps the output PrintStream object
     */
    private void savePreorder(ClassifierNode node, PrintStream ps) {
        // Another argument for allowing void returns in cse123
        if (node != null) {
            if (node.isLeaf()) {
                ps.println(node.label);
            } else {
                ps.println("Feature: " + node.feature);
                ps.println("Threshold: " + node.threshold);
                savePreorder(node.left, ps);
                savePreorder(node.right, ps);
            }
        }
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

        /**
         * Checks if a node is a leaf or not.
         * 
         * @return true if node is a leaf, false if not.
         */
        private boolean isLeaf() {
            return left == null && right == null;
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
