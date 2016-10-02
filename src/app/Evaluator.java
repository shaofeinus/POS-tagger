package app;

import java.io.File;

import data.Model;
import util.HMMStatisticsCompute;
import util.TaggedSetReader;
import util.Untagger;

/**
 * This class calculates the optimality of a POS tag model. It is used when
 * tuning parameters.
 * 
 * @author Shao Fei
 *
 */
public class Evaluator {

	private String developmentSetFileName;
	private Model modelStatistics;
	// Performance measures
	private double accuracy;
	private double perplexity;

	/**
	 * @param modelStatistics
	 *            The trained statistics for the model
	 * @param developmentSetFileName
	 *            The correctly tagged set of sentences
	 * 
	 */
	public Evaluator(Model modelStatistics, String developmentSetFileName) {
		this.developmentSetFileName = developmentSetFileName;
		this.modelStatistics = modelStatistics;
	}

	/**
	 * Evaluate the model statistics by computing various performance measures
	 */
	public void evaluate() {
		evaluateAccuracy();
		// evaluatePerplexity();
		System.out.println("PP: " + perplexity + " " + "Accuracy: " + accuracy);
	}

	/**
	 * Compute the perplexity PP of model based on the development set
	 * statistics. The PP of the model is computed as the average of PP of each
	 * line in the development set using the model statistics.
	 * 
	 * PP is computed in log form since all statistics in the model are in log
	 * form. log(PP) = log(modelStat^(-1/n)) = -1/n * log(modelStat)
	 */
	private void evaluatePerplexity() {
		System.out.println("Computing perplexity of model...");
		TaggedSetReader reader = new TaggedSetReader(developmentSetFileName);
		int numOfLines = 0;
		double totalPerplexity = 0.0;
		while (reader.nextLine()) {
			String prevTag = "<s>";
			double prevStateStat = 0.0;
			// Incrementally calculate state statistics
			while (reader.nextToken()) {
				String word = reader.getCurrTokenSplitWordTag()[0];
				String tag = reader.getCurrTokenSplitWordTag()[1];

				prevStateStat = Viterbi.calculateStats(modelStatistics.getTagGivenPrevTag(prevTag, tag),
						modelStatistics.getWordGivenTag(tag, word), prevStateStat);

				// Last word has an additional P(</s>|last tag)
				if (reader.isLastToken())
					prevStateStat = Viterbi.calculateLastStats(modelStatistics.getTagGivenPrevTag(tag, "</s>"),
							prevStateStat);

				prevTag = tag;
			}
			totalPerplexity += (-prevStateStat / reader.getNumTokensInCurrLine());
			numOfLines++;
			// System.out.println(linePerplexity);
		}
		perplexity = totalPerplexity / numOfLines;
	}

	/**
	 * Computes the accuracy of the model as the percentage similarity between
	 * the development set and the tagged set.
	 */
	private void evaluateAccuracy() {

		// Untag the development set
		System.out.println("Untagging development set...");
		new Untagger(developmentSetFileName, developmentSetFileName + ".untagged").untag();

		// Tag the untagged development set with the model statistics
		System.out.println("Tagging the untagged development set...");
		Tagger tagger = new Tagger(modelStatistics, developmentSetFileName + ".untagged",
				developmentSetFileName + ".tagged");
		tagger.tag();

		// Compute similarity with between the development set and the tagged
		// set
		System.out.println("Computing tagging accuracy...");
		int totalTagsCount = 0;
		int totalCorrectTagsCount = 0;
		TaggedSetReader devSetReader = new TaggedSetReader(developmentSetFileName);
		TaggedSetReader taggedSetReader = new TaggedSetReader(developmentSetFileName + ".tagged");
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
		new File(developmentSetFileName + ".untagged").delete();
		// new File(developmentSetFileName + ".tagged").delete();

		accuracy = (double) totalCorrectTagsCount / totalTagsCount;
	}

	public double getPerplexity() {
		return perplexity;
	}

	public double getAccuracy() {
		return accuracy;
	}

}
