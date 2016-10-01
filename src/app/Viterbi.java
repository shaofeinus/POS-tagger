package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import data.ModelStatistics;
import data.POSTags;
import util.Constants;
import util.SetReader;
import util.HMMStatisticsCompute;

/**
 * This class runs the Viterbi algorithm on a line using the training statistics
 * contained in the Learner class
 * 
 * @author Shao Fei
 *
 */
public class Viterbi {

	private static final POSTags ALL_POS_TAGS = new POSTags();
	// Each element in the List in viterbiMatrix and backPointer represents a
	// value for a word in the line, in the order that the word appears in the
	// line. A LinkedList is used instead of using a Map with the words as keys
	// as
	// words may repeat in a line and Map can only store 1 value for the same
	// word.
	private Map<String, ArrayList<Double>> viterbiMatrix;
	private Map<String, ArrayList<String>> backPointer;
	private ModelStatistics trainedStatistics;
	// The the tag for the last word that is determined to be the best from the
	// Viterbi algorithm. This is the starting point to get the word/tag pair
	// for the rest of the words in the line using backPointer.
	private String bestLastTag;
	// The best probability of the line using the Viberti algorithm, in log form 
	private double bestLastStat;
	private SetReader reader;

	/**
	 * @param reader
	 *            Already set to the tokens from the current line that is bring
	 *            read. Just call nextToken(), getCurrTokenSplitWordTag() and
	 *            rewindToFirstToken() to access the tokens in the line.
	 * @param trainedStatistics
	 *            Model statistics that has been trained
	 */
	public Viterbi(SetReader reader, ModelStatistics trainedStatistics) {
		this.reader = reader;
		this.trainedStatistics = trainedStatistics;
		initViterbi();
	}

	public Map<String, ArrayList<String>> getBackPointer() {
		return backPointer;
	}

	public String getLastTag() {
		return bestLastTag;
	}

	public void runViterbi() {
		// Initialise statistics for first word
		viterbiFirstWord();

		// Compute statistics for the rest of the words
		vibertiRecursion();

		// Compute statistics for the last word to determine the last tag to
		// start the back trace
		vibertiEndOfLine();

		// TODO: remove debug
		if (reader.getCurrLineIndex() == 0) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter("back pointer", false));
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				bw.write(gson.toJson(backPointer));
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initialise the N x T viterbi matrix and backpointer.
	 * 
	 */
	private void initViterbi() {
		viterbiMatrix = new HashMap<String, ArrayList<Double>>();
		backPointer = new HashMap<String, ArrayList<String>>();
		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			// Skip the state <s> as <s> will not transit to <s> again
			// Skip the state </s> as all last states will transit to </s>
			if (tag.equals("<s>") || tag.equals("</s>"))
				continue;
			viterbiMatrix.put(tag, new ArrayList<Double>());
			backPointer.put(tag, new ArrayList<String>());
			while (reader.nextToken()) {
				viterbiMatrix.get(tag).add(Constants.MIN_VALUE);
				backPointer.get(tag).add(null);
			}
			reader.goToStartOfLine();
		}
	}

	/**
	 * Termination step of the Viberti algorithm to calculate the viberti
	 * statistics for the end of line tag </s>. Determine the best last tag
	 * based on the calculated statistics and update bestLastTag.
	 * 
	 * Precondition: Must be called only after vibertiRecursion() and
	 * vibertiFirstWord().
	 */
	private void vibertiEndOfLine() {
		int lastWordIndex = reader.getNumTokensInCurrLine() - 1; 

		// Get the maximum state statistics for </s> given a previous candidate
		// tag
		// The maximum statistics so far. Default value is MIN_VALUE.
		double bestLastStateStat = Constants.MIN_VALUE;
		// The previous candidate tag that is determined to give the
		// maximum statistics so far. Default value is set to TODO:_____, in case that
		// no previous candidate tag can give a better statistics than
		// MIN_VALUE.
		String bestPrevState = "NN";
		// Calculate statistics for each candidate tags except <s> and
		// </s> and update maximum stateStat and prevState associated
		// with it accordingly
		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext()) {
			String candidateTag = tagsIter.next();
			if (candidateTag.equals("<s>") || candidateTag.equals("</s>"))
				continue;
			double candidateStateStat = calculateLastStats(trainedStatistics.getTagGivenPrevTag(candidateTag, "</s>"),
					viterbiMatrix.get(candidateTag).get(lastWordIndex));
			if (candidateStateStat > bestLastStateStat) {
				bestLastStateStat = candidateStateStat;
				bestPrevState = candidateTag;
			}
		}
		// Update the best last tag as the tag that maximizes the stateStat
		bestLastTag = bestPrevState;
		// Update best probability of the line using the Viberti algorithm, in log form
		bestLastStat = viterbiMatrix.get(bestPrevState).get(lastWordIndex) + trainedStatistics.getTagGivenPrevTag(bestPrevState, "</s>");

		// TODO: remove debug
		// if (reader.getCurrLineIndex() == 0) {
		// System.out.println("Best last tag: " + bestLastTag);
		// System.out.println(backPointer);
		// }

	}

	/**
	 * Recursion step of the Viberti algorithm to calculate the viberti
	 * statistics for the 2nd word to the last word of the line.
	 * 
	 * Precondition: Reader must be currently pointing at the first word in the
	 * line. So that the first call of nextToken() will result in the second
	 * token of the line being processed.
	 */
	private void vibertiRecursion() {

		assert reader.isFirstToken();

		// This denotes the word (t-1) that precedes the current word being
		// processed. Start with t-1 = 0 where t-1 is the first word in the line
		int prevWordIndex = 0;

		// For each word from the 2nd word to the last word
		while (reader.nextToken()) {

			String word = reader.getCurrToken();

			// For each possible tag except <s> and </s>
			Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
			while (tagsIter.hasNext()) {

				String tag = tagsIter.next();

				// If tag is <s> or </s> skip, as these two tags should not be
				// associated with any word
				if (tag.equals("<s>") || tag.equals("</s>"))
					continue;

				// Get the maximum state statistics for the tag being currently
				// processed given a previous candidate tag.
				// The maximum statistics so far.
				// Default value is MIN_VALUE.
				double bestStateStat = Constants.MIN_VALUE;
				// The previous candidate tag that is determined to give the
				// maximum statistics so far.
				// Default value is just "NN", in case that
				// no previous candidate tag can give a better statistics than
				// MIN_VALUE.
				String bestPrevState = "NN";
				// Calculate statistics for each candidate tags except <s> and
				// </s> and update maximum stateStat and prevState associated
				// with it accordingly
				Iterator<String> tagsIterInner = ALL_POS_TAGS.getIterator();
				while (tagsIterInner.hasNext()) {
					String candidateTag = tagsIterInner.next();
					if (candidateTag.equals("<s>") || candidateTag.equals("</s>"))
						continue;
					double candidateStateStat = calculateStats(viterbiMatrix.get(candidateTag).get(prevWordIndex),
							trainedStatistics.getTagGivenPrevTag(candidateTag, tag),
							trainedStatistics.getWordGivenTag(tag, word));
					if (candidateStateStat > bestStateStat) {
						bestStateStat = candidateStateStat;
						bestPrevState = candidateTag;
					}
				}
				// Update viterbiMatrix and backPointer with the best stateStat
				// and prevState
				viterbiMatrix.get(tag).set(prevWordIndex + 1, bestStateStat);
				backPointer.get(tag).set(prevWordIndex + 1, bestPrevState);

				// TODO: remove debug
				// if (reader.getCurrLineIndex() == 0) {
				// System.out.println(word + "/" + tag + " " + prevState + " "
				// + (stateStat == Constants.MIN_VALUE ? "MIN" : stateStat));
				// }
			}
			prevWordIndex++;
		}
	}

	/**
	 * Initialisation step to calculate the viberti statistics for the first
	 * word in the line.
	 * 
	 * Precondition: Reader must be currently pointing at start of line. So that
	 * the first call of nextToken() will result in the first token of the line
	 * being processed.
	 * 
	 * @param viterbiMatrix
	 *            Initialised viterbiMatrix
	 * @param backPointer
	 *            Initialised backPointer
	 * @param firstWord
	 *            First word in the line
	 */
	private void viterbiFirstWord() {
		assert reader.getCurrTokenIndex() == -1;
		reader.nextToken();
		String firstWord = reader.getCurrToken();
		// For each possible tag except <s> and </s>
		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			// If tag is <s> or </s> skip, as these two tags should not be
			// associated with any word
			if (tag.equals("<s>") || tag.equals("</s>"))
				continue;
			viterbiMatrix.get(tag).set(0, calculateStats(trainedStatistics.getTagGivenPrevTag("<s>", tag),
					trainedStatistics.getWordGivenTag(tag, firstWord), 0.0));
			backPointer.get(tag).set(0, "<s>");

			// TODO: remove debug
			// double print =
			// calculateFirstStats(trainedStatistics.getTagGivenPrevTag("<s>",
			// tag),
			// trainedStatistics.getWordGivenTag(tag, firstWord));
			// System.out.println(
			// firstWord + "/" + tag + ": " + (print == Constants.MIN_VALUE ?
			// "MIN" : print));
		}
	}
	
	/**
	 * Calculate the statistic of viterbi(s',t-1) * as',s in log form
	 * 
	 * @param transitionProb
	 *            log(as',s)
	 * @param prevStateStat
	 *            log(viterbi(s',t-1))
	 * @return log(viterbi(s',t-1) * as',s)
	 */
	public static double calculateLastStats(double transitionProb, double prevStateStat) {
		if (transitionProb == Constants.MIN_VALUE || prevStateStat == Constants.MIN_VALUE)
			return Constants.MIN_VALUE;
		else
			// log(viterbi(s',t-1) * as',s) = log(viterbi(s',t-1)) + log(as',s)
			return prevStateStat + transitionProb;
	}
	
	/**
	 * Calculate the statistic of viterbi(s',t-1) * as',s * bs(t)in log form
	 * 
	 * @param transitionProb
	 *            log(as',s)
	 * @param emissionProb
	 *            log(bs(t))
	 * @param prevStateStat
	 *            log(viterbi(s',t-1))
	 * @return log(viterbi(s',t-1) * as',s * bs(t))
	 */
	public static double calculateStats(double transitionProb, double emissionProb, double prevStateStat) {
		if (transitionProb == Constants.MIN_VALUE || emissionProb == Constants.MIN_VALUE
				|| prevStateStat == Constants.MIN_VALUE)
			return Constants.MIN_VALUE;
		else
			return prevStateStat + transitionProb + emissionProb;
	}
}
