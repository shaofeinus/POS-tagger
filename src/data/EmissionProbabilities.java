package data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.Constants;

/**
 * Stores emission probabilities, the probability of an word given a POS tag,
 * P(wi|ti) and other statistics associated with emission probabilities.
 * 
 * All probability calculations are done in log to allow very small probability
 * values.
 * 
 * @author Shao Fei A0102015H
 * 
 */
public class EmissionProbabilities {

	// Set of suffixes, for use during the unknown word model
	transient private static final Suffixes SUFFIXES = new Suffixes();

	// C(w,t)
	transient private Map<String, Map<String, Integer>> tagAndWordCount;
	// Count of capitalization and suffixes in words in training set. Used to
	// calculate emission probabilities for unknown words
	// C(cap,t)
	private Map<String, Integer> tagAndCapCount;
	// C(suf,t)
	private Map<String, Integer> tagAndSufCount;
	// log(P(w|t))
	private Map<String, Map<String, Double>> wordGivenTag;

	public EmissionProbabilities(POSTags allTags) {
		initEmissionProbabilities(allTags);
	}

	/**
	 * Initialise data structures
	 * 
	 * @param allTags
	 *            List of all POS tags
	 */
	public void initEmissionProbabilities(POSTags allTags) {
		tagAndWordCount = new HashMap<String, Map<String, Integer>>();
		tagAndCapCount = new HashMap<String, Integer>();
		tagAndSufCount = new HashMap<String, Integer>();
		wordGivenTag = new HashMap<String, Map<String, Double>>();
		Iterator<String> tagsIter = allTags.getIterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			tagAndWordCount.put(tag, new HashMap<String, Integer>());
			tagAndCapCount.put(tag, 0);
			tagAndSufCount.put(tag, 0);
			wordGivenTag.put(tag, new HashMap<String, Double>());
		}
	}

	/**
	 * Computes the emission probabilities log(P(w|t)) based on the current
	 * tagAndWordCount C(w,t) and C(t). As such log(P(w|t)) can only be computed
	 * for words where C(w,t) > 0
	 * 
	 * @param tagCount
	 *            C(t)
	 */
	public void computeEmissionProbabilities(Map<String, Integer> tagCount) {
		// For statistics gathering
		double total = 0.0;
		int count = 0;
		double min = 0;
		
		Iterator<String> tagsIter = tagCount.keySet().iterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			Iterator<String> seenTagAndWordsIter = tagAndWordCount.get(tag).keySet().iterator();
			while (seenTagAndWordsIter.hasNext()) {
				String seenWord = seenTagAndWordsIter.next();
				double probability = Math
						.log(tagAndWordCount.get(tag).get(seenWord).doubleValue() / tagCount.get(tag).doubleValue());
				// log(P(w|t)) = log(C(w|t)/C(t))
				wordGivenTag.get(tag).put(seenWord, probability);
				
				// For statistics gathering
				min = Math.min(min, probability);
				total += probability;
				count++;
			}
		}
		System.out.println("Computed emission prob ave: " + total/count + " min: " + min);
	}

	/**
	 * Add a count to C(w, t) and update C(cap, t), C(suf, t) if necessary
	 * 
	 * @param tag
	 *            The POS tag associated with the word
	 * @param word
	 *            The word to be associated with the POS tag
	 */
	public void addWordAndTagCount(String tag, String word) {

		// // Create a new tag entry if tag is not found to be associated with
		// any
		// // words
		// if (!tagAndWordCount.containsKey(tag))
		// tagAndWordCount.put(tag, new HashMap<String, Integer>());
		//
		// // Create a new tag entry if tag is not found to be associated with
		// any
		// // capital or suffixes
		// if (!tagAndCapCount.containsKey(tag))
		// tagAndCapCount.put(tag, 0);
		// if (!tagAndSufCount.containsKey(tag))
		// tagAndSufCount.put(tag, 0);

		// Create a new word entry for the tag if word is not yet associated
		// with the tag
		if (!tagAndWordCount.get(tag).containsKey(word))
			tagAndWordCount.get(tag).put(word, 0);

		// Update C(w,t)
		tagAndWordCount.get(tag).put(word, tagAndWordCount.get(tag).get(word) + 1);
		// System.out.println(tagAndWordCount.get(tag).get(word));
		// System.out.println(tagCount.get(tag));

		// Update C(cap, t)
		if (containsCapital(word))
			tagAndCapCount.put(tag, tagAndCapCount.get(tag) + 1);

		// Update C(suf, t)
		if (containsSuffix(word))
			tagAndSufCount.put(tag, tagAndSufCount.get(tag) + 1);
	}

	/**
	 * Get log(P(w|t)) for a word w and tag t.
	 * 
	 * @param tag
	 *            The query POS tag
	 * @param word
	 *            The query word
	 * @param tagCount
	 *            C(t)
	 * @param vocabulary
	 *            The set of known vocabulary
	 * @return log(P(w|t)) if word exists in vocabulary and C(w,t) > 0, log(0) =
	 *         MIN_VALUE if word exists in vocabulary but C(w,t) =0, log(P(w|t))
	 *         using unknown word model if word does not exist in vocabulary
	 */
	public double getWordGivenTag(String tag, String word, Map<String, Integer> tagCount, Set<String> vocabulary) {
		// Word is in vocabulary
		if (vocabulary.contains(word)) {
			// C(w,t) = 0, emission probability = log(0) = MIN_NUMBER;
			if (!wordGivenTag.get(tag).containsKey(word))
				return Constants.MIN_VALUE;
			// C(w,t) > 0, return emission probability
			else
				return wordGivenTag.get(tag).get(word);
		}
		// Word is not in vocabulary, estimate P(w|t) using unknown word model
		else
			return emissionProbUnknownWordModel(tag, tagCount);
	}

	/**
	 * Estimates emission probability for an unknown word using the Unknown word
	 * model P(w|t) = P(unknown word|t)*P(capital|t)*P(suffixes|t)
	 * 
	 * @param tag
	 *            The query POS tag
	 * @param tagCount
	 *            C(t)
	 * @return log of the estimated emission probability, log(P(w|t))
	 */
	private double emissionProbUnknownWordModel(String tag, Map<String, Integer> tagCount) {
		// System.out.println(tagAndCapCount.get(tag).doubleValue() + " " +
		// tagAndSufCount.get(tag).doubleValue());
		// C(cap|t) or C(suf|t) = 0, emission probability = log(0) = MIN_NUMBER;
		if (tagAndCapCount.get(tag) == 0 || tagAndSufCount.get(tag) == 0)
			return Constants.MIN_VALUE;
		else
			return Math.log(1.0 / tagCount.get(tag)) + // P(unknown word|t)
					Math.log(tagAndCapCount.get(tag).doubleValue() / tagCount.get(tag).doubleValue()) + // P(capital|t)
					Math.log(tagAndSufCount.get(tag).doubleValue() / tagCount.get(tag).doubleValue()); // P(suffixes|t)
	}

	/**
	 * @param word
	 * @return true if word contains 1 or more capital letter
	 */
	private boolean containsCapital(String word) {
		return !word.equals(word.toLowerCase());
	}

	/**
	 * @param word
	 * @return true if word contains any suffix as specified in SUFFIXES
	 */
	private boolean containsSuffix(String word) {
		Iterator<String> iter = SUFFIXES.getIterator();
		while (iter.hasNext()) {
			String suffix = iter.next();
			if (word.length() > suffix.length() // First check if word
												// length bigger than
												// suffix length to
												// avoid unnecessary
												// check
					&& suffix.equals(word.substring(word.length() - suffix.length())))
				return true;
		}
		return false;
	}

}
