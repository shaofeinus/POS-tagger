package util;

/**
 * This class calculates the percentage similarity between two tagged sets
 * (development set and set tagged by tagger). It is used to calculate the
 * probability of a development set when tuning parameters.
 * 
 * Note that the development set and the tagged set must contain the same
 * sentences in order for valid comparison,
 * 
 * @author Shao Fei
 *
 */
public class Evaluator {

	/**
	 * Precondition: The development set and the tagged set must contain the
	 * same sentences in order for valid comparison,
	 * 
	 * @param developmentSetFileName
	 *            The correctly tagged set of sentences
	 * @param taggedSetFileName
	 *            The tagged set of sentences that is tagged by the tagger
	 * 
	 */
	public Evaluator(String developmentSetFileName, String taggedSetFileName) {
		// TODO Auto-generated constructor stub
	}

}
