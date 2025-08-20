// Gunnar Von Bergen
// 08/19/2025
// CSE 123
// P3: Spam Classifier
// TA: Trien
import java.io.*;
import java.util.*;

/**
 * A binary classification tree that can be trained on labeled TextBlocks or
 * loaded from a previously saved tree file. The classifier can be saved into
 * a file, classify inputs, and report accuracy scores on test sets.
 * 
 * An instance can be trained, optionally saved, then used to classify inputs.
 * Alternatively, an instance can be constructed by loading a saved tree and
 * then used.
 */
public class Classifier {

    // Root of the classification tree
    private ClassifierNode root;

    /**
     * Constructs a classifier from a preorder-format file from the provided
     * scanner.
     * The valid format is:
     * Feature: <featureName>
     * Threshold: <doubleValue>
     * <left subtree>
     * <right subtree>
     * 
     * Leaf nodes appear as a single line containing just the label.
     * 
     * @param sc a scanner object reading a preorder-format file from the start
     * @throws IllegalArgumentException if sc is null.
     * @throws IllegalStateException    if the tree is empty after reading the
     *                                  scanner, or the
     *                                  tree is malformed due to file format.
     */
    public Classifier(Scanner sc) {
        if (sc == null)
            throw new IllegalArgumentException("Scanner object is null.");
        this.root = readPreorder(sc);
        if (this.root == null)
            throw new IllegalStateException("Tree is empty after read.");
    }

    /**
     * Constructs and trains a classifier from parallel lists of TextBlocks and
     * labels.
     * 
     * @param data   a list of text blocks to represent the data values associated
     *               with labels to train a classification tree
     * @param labels a list of label strings corresponding to text blocks that
     *               will be used to train a classification tree
     * @throws IllegalArgumentException if data or labels is null or empty, or
     *                                  if they are not equal in size.
     */
    public Classifier(List<TextBlock> data, List<String> labels) {
        if (data == null)
            throw new IllegalArgumentException("Data is null.");
        if (labels == null)
            throw new IllegalArgumentException("Labels is null.");

        if (data.isEmpty())
            throw new IllegalArgumentException("Data is empty.");
        if (labels.isEmpty())
            throw new IllegalArgumentException("Labels is empty.");

        if (data.size() != labels.size()) {
            throw new IllegalArgumentException("Data and Labels are not equal size.");
        }

        this.root = null;

        for (int i = 0; i < data.size(); i++) {
            this.root = buildTree(this.root, data.get(i), labels.get(i));
        }
    }

    /**
     * Classifies a text block by returning the determined label.
     * 
     * @param input the text block to be classified
     * @return the classification of the text block as a string.
     * @throws IllegalArgumentException if input is null.
     * @throws IllegalStateException    if root node is null/tree is empty.
     */
    public String classify(TextBlock input) {
        if (input == null)
            throw new IllegalArgumentException("Input is null.");
        return classify(root, input);
    }

    /**
     * Saves a classifier to a provided PrintStream in preorder format.
     * Decision nodes are written as two lines:
     * 
     * Feature: <featureName>
     * Threshold: <doubleValue>
     * 
     * followed by the left subtree, then the right subtree.
     * 
     * Leaf nodes are written on a single line containing only the label.
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
     * Builds a classification tree by adding a labeled text block to an existing
     * tree,
     * or creates a new one if the passed node is empty.
     * 
     * @param node  the current node being reviewed in the tree
     * @param tb    the text block containing the data for training
     * @param label the label of the data to be used for classification
     * @return a ClassifierNode that has been changed to reflect an updated tree.
     * @throws IllegalStateException if a leaf node is missing a text block.
     */
    private ClassifierNode buildTree(ClassifierNode node, TextBlock tb, String label) {
        if (node == null)
            return new ClassifierNode(label, tb);

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
     * Returns a string representing what label the provided text block
     * is classified as based on the given decision tree root node.
     * 
     * @param node the current node of the decision tree
     * @param tb   the text block being classified
     * @return the classification of the text block as a string.
     * @throws IllegalStateException if node is null.
     */
    private String classify(ClassifierNode node, TextBlock tb) {
        if (node == null)
            throw new IllegalStateException("Empty tree");
        if (node.isLeaf())
            return node.label;

        double nodeThresholdValue = tb.get(node.feature);

        if (nodeThresholdValue < node.threshold) {
            return classify(node.left, tb);
        }

        return classify(node.right, tb);
    }

    /**
     * Reads a preorder-format file to construct a classification tree.
     * The scanner source must have the correct format.
     * Decision nodes are written as two lines:
     * 
     * Feature: <featureName>
     * Threshold: <doubleValue>
     * 
     * followed by the left subtree, then the right subtree.
     * 
     * Leaf nodes are written on a single line containing only the label.
     * 
     * @param sc a scanner reading the preorder-format file
     * @return a ClassifierNode for the current root of the tree, or null if empty.
     * @throws IllegalStateException if the preorder-format file is incorrectly
     *                               formatted.
     */
    private ClassifierNode readPreorder(Scanner sc) {
        if (!sc.hasNextLine())
            return null;
        String line = sc.nextLine();

        // Handle empty lines by going until non-empty, or early return
        while (line.isEmpty() && sc.hasNextLine()) {
            line = sc.nextLine();
        }
        if (line.isEmpty())
            return null;

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
     * The output is formatted following specific rules.
     * Decision nodes are written as two lines:
     * 
     * Feature: <featureName>
     * Threshold: <doubleValue>
     * 
     * followed by the left subtree, then the right subtree.
     * 
     * Leaf nodes are written on a single line containing only the label.
     * 
     * @param node the current node being handled
     * @param ps   the output PrintStream object
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
    private static class ClassifierNode {
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
         * @param feature   the feature being checked and decided upon
         * @param threshold the value which a choice is determined upon
         * @param left      the left decision for values less than the threshold
         * @param right     the right decision for values equal or greater than the
         *                  threshold
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
         * @param label        the label of the resulting classification
         * @param initialBlock the text block a decision was first made with
         */
        ClassifierNode(String label, TextBlock initialBlock) {
            this.feature = "";
            this.threshold = 0.0;
            this.left = null;
            this.right = null;
            this.label = label;
            this.initialBlock = initialBlock;
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
    // testing 'data' and corresponding 'labels'. The label for a
    // datapoint at an index within 'data' should be found at the
    // same index within 'labels'.
    // Exceptions: IllegalArgumentException if the number of datapoints doesn't
    // match the number
    // of provided labels
    // Returns: a map storing the classification accuracy for each of the
    // encountered labels when
    // classifying
    // Parameters: data - the list of TextBlock objects to classify. Should be
    // non-null.
    // labels - the list of expected labels for each TextBlock object.
    // Should be non-null.
    public Map<String, Double> calculateAccuracy(List<TextBlock> data, List<String> labels) {
        // Check to make sure the lists have the same size (each datapoint has an
        // expected label)
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
