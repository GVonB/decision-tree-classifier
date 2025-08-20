import java.io.*;
import java.util.*;

public class Classifier {

    // Add fields here
    

    public Classifier(Scanner sc) {
        // TODO: Remove the exception and implement this method
        throw new RuntimeException("Not yet implemented: Classifier(Scanner input)");
    }

    public Classifier(List<TextBlock> data, List<String> results) {
        // TODO: Remove the exception and implement this method
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

    // TODO: Implement your node class
    public static class ClassifierNode {

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
