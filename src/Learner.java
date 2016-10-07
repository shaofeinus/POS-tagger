
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class calculates the optimality of a POS tag model. It is used when
 * tuning parameters.
 * 
 * @author Shao Fei
 *
 */
public class Learner {

	private String id; // For use during multithreading to identify saved file
	private String developmentSetFileName;
	private String tuningStatsFileName;
	private Model modelStatistics;
	private boolean saveTuningStatistics;

	// Performance measure
	private double accuracy;

	/**
	 * @param modelStatistics
	 *            The model statistics to be trained
	 * @param developmentSetFileName
	 *            The correctly tagged set of sentences for tuning purposes.
	 * 
	 */
	public Learner(Model modelStatistics, String developmentSetFileName, String id) {
		this.id = id;
		this.developmentSetFileName = developmentSetFileName;
		this.modelStatistics = modelStatistics;
		saveTuningStatistics = false;
	}

	public Learner(Model modelStatistics, String developmentSetFileName, String tuningStatsFileName, String id) {
		this(modelStatistics, developmentSetFileName, id);
		this.tuningStatsFileName = tuningStatsFileName + "_" + id + ".csv";
		saveTuningStatistics = true;
	}

	/**
	 * Train the Model object with just the training set. Any smoothing
	 * parameters will be set to default values.
	 * 
	 * @throws NoSuchFieldException
	 *             when a unknown POS tag from the training set is encountered
	 */
	public void learnNoTune() throws NoSuchFieldException {
		// Load the count statistics first from the training set
		System.out.println("Collecting count statistics from training set for " + id + "...");
		modelStatistics.loadCountStatistics();
		// Compute the model statistics
		modelStatistics.setParametersToDefault();
		System.out.println("Computing accuracy for " + id + "...");
		evaluateAccuracy();

		// Output accuracy on console and save if needed
		String output = id + ",Accuracy," + accuracy;
		System.out.println(id + " parameters: " + modelStatistics.getParamtersValues() + " Accuracy: " + accuracy);
		if (saveTuningStatistics)
			try {
				BufferedWriter bf = new BufferedWriter(new FileWriter(tuningStatsFileName, false));
				bf.write(output + "\n");
				bf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Train the Model object with the training set, and tune the smoothing
	 * parameters with the dev set.
	 * 
	 * @throws NoSuchFieldException
	 *             when a unknown POS tag from the training set is encountered
	 */
	public void learnAndTune() throws NoSuchFieldException {

		// For saving tuning statistics
		if (saveTuningStatistics) {
			System.out.println("Tuning statistics file will be saved to: " + tuningStatsFileName);
			new File(tuningStatsFileName).delete();
		}

		double bestAccuracy = 0;
		boolean isFirstIteration = true;

		// Load the count statistics first from the training set
		System.out.println("Collecting count statistics from training for " + id + "...");
		modelStatistics.loadCountStatistics();

		// Vary parameters
		System.out.println("Tuning smoothing parameters using dev set for " + modelStatistics.getNumTuningIterations()
				+ " iterations...");
		while (modelStatistics.nextSetOfParameters()) {
			// Use default parameters if this is the first iteration
			if (isFirstIteration) {
				System.out.println("Computing accuracy for default parameters for " + id + "...");
				modelStatistics.setParametersToDefault();
				isFirstIteration = false;
			} else
				System.out.println("Computing accuracy for next set of parameters for " + id + "...");

			// Compute the model statistics
			// Update the best accuracy
			evaluateAccuracy();
			if (accuracy > bestAccuracy) {
				modelStatistics.rememberCurrentParametersAsBest();
				bestAccuracy = accuracy;
			}

			// Output tuning statistics on console and save if needed
			String output = id + "," + modelStatistics.getParamtersValues() + "," + accuracy;
			System.out.println(id + " parameters: " + modelStatistics.getParamtersValues() + " Accuracy: " + accuracy);
			if (saveTuningStatistics)
				try {
					BufferedWriter bf = new BufferedWriter(new FileWriter(tuningStatsFileName, true));
					bf.write(output + "\n");
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		modelStatistics.setParametersToBest();
		System.out
				.println("Best parameters for " + id + "," + modelStatistics.getParamtersValues() + "," + bestAccuracy);
	}

	/**
	 * Computes the accuracy of the model as the percentage similarity between
	 * the development set and the tagged set.
	 */
	public void evaluateAccuracy() {

		String taggedFileName = developmentSetFileName + "_" + id + ".retagged";
		String untaggedFileName = developmentSetFileName + "_" + id + ".untagged";

		// Untag the development set
		new Untagger(developmentSetFileName, untaggedFileName).untag();

		// Tag the untagged development set with the model statistics
		Tagger tagger = new Tagger(modelStatistics, untaggedFileName, taggedFileName);
		tagger.tag();

		// Compute similarity with between the development set and the tagged
		// set
		int totalTagsCount = 0;
		int totalCorrectTagsCount = 0;
		TaggedSetReader devSetReader = new TaggedSetReader(developmentSetFileName);
		TaggedSetReader taggedSetReader = new TaggedSetReader(taggedFileName);
		while (devSetReader.nextLine() && taggedSetReader.nextLine()) {
			while (devSetReader.nextToken() && taggedSetReader.nextToken()) {
				totalTagsCount++;
				if (devSetReader.getCurrTokenSplitWordTag()[1].equals(taggedSetReader.getCurrTokenSplitWordTag()[1]))
					totalCorrectTagsCount++;
			}
		}
		devSetReader.close();
		taggedSetReader.close();

		// Remove the temp .untagged and .tagged files
		new File(untaggedFileName).delete();
		new File(taggedFileName).delete();

		accuracy = (double) totalCorrectTagsCount / totalTagsCount;
	}

	public double getAccuracy() {
		return accuracy;
	}

}
