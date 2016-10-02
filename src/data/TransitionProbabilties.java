package data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import util.Constants;

/**
 * Stores transition probability matrix, the probability of a next POS tag given
 * previous a POS tag, P(ti|ti-1).
 * 
 * All probability calculations are done in log to allow very small probability
 * values.
 * 
 * @author Shao Fei
 *
 */
public class TransitionProbabilties {

	// Set of all possible POS Tags
	transient private static final POSTags ALL_POS_TAGS = new POSTags();

	// C(t)
	private Map<String, Integer> tagCount;
	// C(ti,ti-1)
	transient private Map<String, Map<String, Integer>> prevTagAndTagCount;
	// log(P(ti|ti-1))
	private Map<String, Map<String, Double>> tagGivenPrevTag;

	/**
	 * @param tagCount
	 *            C(t), Managed by the parent class ModelStatistics
	 */
	public TransitionProbabilties(Map<String, Integer> tagCount) {
		this.tagCount = tagCount;
		prevTagAndTagCount = new HashMap<String, Map<String, Integer>>();
		tagGivenPrevTag = new HashMap<String, Map<String, Double>>();
		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			prevTagAndTagCount.put(tag, new HashMap<String, Integer>());
			tagGivenPrevTag.put(tag, new HashMap<String, Double>());
		}
	}

	/**
	 * 
	 * Add a count to a tag ti and tag t-1, updating C(ti, ti-1) and P(ti|ti-1)
	 * 
	 * @param prevTag
	 *            Previous POS tag
	 * @param tag
	 *            Current POS tag
	 */
	public void addPrevTagAndTagCount(String prevTag, String tag) {
		// System.out.println(prevTag + " " + tag);
		// // Create a new previous tag entry if tag is not found to be
		// associated
		// // with any tags
		// if (!prevTagAndTagCount.containsKey(prevTag))
		// prevTagAndTagCount.put(prevTag, new HashMap<String, Integer>());

		// Create a new tag entry for the tag if it is not yet associated
		// with the previous tag
		if (!prevTagAndTagCount.get(prevTag).containsKey(tag))
			prevTagAndTagCount.get(prevTag).put(tag, 0);

		// Update C(ti,ti-1)
		prevTagAndTagCount.get(prevTag).put(tag, prevTagAndTagCount.get(prevTag).get(tag) + 1);
	}

	/**
	 * Computes the transition probabilities log(P(ti|ti-1)) based on
	 * prevTagAndTagCount C(ti,ti-1) and C(ti-1). As such log(P(ti|ti-1)) can
	 * only be computed for words where C(ti,ti-1) > 0
	 * 
	 * @param tagCount
	 *            C(ti-1)
	 */
	public void computeTransitionProbabilities() {
		// For statistics gathering
		double total = 0.0;
		int count = 0;
		double min = 0;
		Iterator<String> prevTagsIter = tagCount.keySet().iterator();
		while (prevTagsIter.hasNext()) {
			String prevTag = prevTagsIter.next();
			Iterator<String> seenPrevTagsAndTagsIter = prevTagAndTagCount.get(prevTag).keySet().iterator();
			while (seenPrevTagsAndTagsIter.hasNext()) {
				String seenTag = seenPrevTagsAndTagsIter.next();
				double probability = prevTagAndTagCount.get(prevTag).get(seenTag).doubleValue()
						/ tagCount.get(prevTag).doubleValue();
				// log(P(ti|ti-1)) = log(C(ti|ti-1)/C(ti-1))
				tagGivenPrevTag.get(prevTag).put(seenTag, probability);

				// For statistics gathering
				min = Math.min(min, probability);
				total += probability;
				count++;
			}
		}
		System.out.println("Computed transition prob ave: " + total / count + " min: " + min);
	}

	/**
	 * Get log(P(ti|ti-1)) for a tag ti and tag ti-1
	 * 
	 * @param prevTag
	 *            Previous POS tag
	 * @param tag
	 *            Current POS tag
	 * @return log(P(ti|ti-1)) if C(t|ti-1) > 0, else return log(0) = MIN_VALUE
	 */
	public double getTagGivenPrevTag(String prevTag, String tag) {
		// C(ti,ti-1) = 0, transition probability = log(0) = MIN_NUMBER;
		if (!tagGivenPrevTag.get(prevTag).containsKey(tag))
			return 0.0;
		// C(ti,ti-1) > 0
		else
			return tagGivenPrevTag.get(prevTag).get(tag);

	}
}
