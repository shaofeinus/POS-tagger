

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements the Kneser-Ney smoothing method to handle zero counts.
 * 
 * @author Shao Fei
 *
 */
public class ModelKneserNey extends Model {

	private static final long serialVersionUID = -7594308161525627350L;
	
	private static final double[] D_EMISSION_PROB_RANGE = TUNING_SETTINGS.D_EMISSION_PROB_RANGE;
	private static final double[] D_TRANSITION_PROB_RANGE = TUNING_SETTINGS.D_TRANSITION_PROB_RANGE;
	private static final int TUNING_ITERATIONS_FOR_EACH_PARAM = TUNING_SETTINGS.NUM_TRIALS;

	// D is used to obtain P(a|b) when C(a,b) > 0
	// 1 for emission probability and 1 for transition probability
	transient private double DEmissionProb;
	transient private double DTransitionProb;
	// Used during tuning
	// 1 for emission probability and 1 for transition probability
	transient private double DEmissionProbBest;
	transient private double DTransitionProbBest;
	// alpha is used to obtain P(a|b) when C(a,b) = 0
	// 1 for emission probability and 1 for transition probability
	transient private Map<String, Double> alphaEmissionProb;
	transient private Map<String, Double> alphaTransitionProb;
	// |{w-1: C(w-1, w)>0}| for each w. I.e. Number of distinct pairs (w-1, w)
	// for given a w
	// 1 for emission probability and 1 for transition probability
	transient private Map<String, Integer> distinctPairsEmissionProb;
	transient private Map<String, Integer> distinctPairsTransitionProb;
	// Sum(|{w-1: C(w-1, w)>0}|) over all w. I.e. Total number of distinct
	// pairs (w-1, w) in corpus
	// 1 for emission probability and 1 for transition probability
	transient private int totalDistinctPairsEmissionProb;
	transient private int totalDistinctPairsTransitionProb;
	
	transient boolean isEmissionProbInitialised, isTransitionProbInitialised;
	
	public ModelKneserNey(String trainingFile) {
		super(trainingFile);
		setParametersToDefault();
	}

	/**
	 * Set the value for D for emission probability. Alpha must be recalculated
	 * as it contains the value of D
	 * 
	 * @param D
	 */
	public void setDEmissionProb(double D) {
		this.DEmissionProb = D;
		isEmissionProbInitialised = false;
	}

	/**
	 * Set the value for D for transition probability. Alpha must be
	 * recalculated as it contains the value of D
	 * 
	 * @param D
	 */
	public void setDTransitionProb(double D) {
		this.DTransitionProb = D;
		isTransitionProbInitialised = false;
	}

	@Override
	protected double nonZeroEmissionProb(String tag, String word) {
		return (tagAndWordCount.get(tag).get(word).doubleValue() - DEmissionProb) / tagCount.get(tag);
	}

	@Override
	protected double nonZeroTransitionProb(String prevTag, String tag) {
		return (prevTagAndTagCount.get(prevTag).get(tag).doubleValue() - DTransitionProb)
				/ tagCount.get(prevTag).doubleValue();
	}

	@Override
	protected double zeroEmissionProb(String tag, String word) {
		if (!isEmissionProbInitialised)
			initParamsEmissionProb();
		return alphaEmissionProb.get(tag) * distinctPairsEmissionProb.get(word).doubleValue()
				/ totalDistinctPairsEmissionProb;
	}

	@Override
	protected double zeroTransitionProb(String prevTag, String tag) {
		if (!isTransitionProbInitialised)
			initParamsTransitionProb();
		return alphaTransitionProb.get(prevTag) * distinctPairsTransitionProb.get(tag).doubleValue()
				/ totalDistinctPairsTransitionProb;
	}

	private void initParamsEmissionProb() {
		alphaEmissionProb = new HashMap<String, Double>();
		distinctPairsEmissionProb = new HashMap<String, Integer>();
		totalDistinctPairsEmissionProb = 0;

		// For each word find the distinct number of pairs of (word, tag)
		Iterator<String> tagIter = ALL_POS_TAGS.getIterator();
		while (tagIter.hasNext()) {
			String tag = tagIter.next();
			// <s> and </s> is not associated with any word
			if (tag.equals("<s>") || tag.equals("</s>"))
				continue;
			Iterator<String> wordIter = tagAndWordCount.get(tag).keySet().iterator();
			while (wordIter.hasNext()) {
				String word = wordIter.next();
				if (!distinctPairsEmissionProb.containsKey(word))
					distinctPairsEmissionProb.put(word, 0);
				distinctPairsEmissionProb.put(word, distinctPairsEmissionProb.get(word) + 1);
				// Increment the total number of distinct pairs of (word, tag)
				// at the same time
				totalDistinctPairsEmissionProb++;
			}
		}

		// Find alpha
		tagIter = ALL_POS_TAGS.getIterator();
		while (tagIter.hasNext()) {
			String tag = tagIter.next();
			// <s> and </s> is not associated with any word
			if (tag.equals("<s>") || tag.equals("</s>"))
				continue;
			double alpha = alphaNumerator(tagAndWordCount, tagCount, tag, DEmissionProb)
					/ alphaDenominator(tagAndWordCount, distinctPairsEmissionProb, totalDistinctPairsEmissionProb, tag);
			// System.out.println("alpha emission prob for " + tag + ": " +
			// alpha);
			alphaEmissionProb.put(tag, alpha);
		}

		isEmissionProbInitialised = true;
	}

	private void initParamsTransitionProb() {
		alphaTransitionProb = new HashMap<String, Double>();
		distinctPairsTransitionProb = new HashMap<String, Integer>();
		totalDistinctPairsTransitionProb = 0;

		// For each tag find the distinct number of pairs of (prevTag, tag)
		Iterator<String> prevTagIter = ALL_POS_TAGS.getIterator();
		while (prevTagIter.hasNext()) {
			String prevTag = prevTagIter.next();
			// </s> cannot be a previous tag
			if (prevTag.equals("</s>"))
				continue;
			Iterator<String> tagIter = prevTagAndTagCount.get(prevTag).keySet().iterator();
			while (tagIter.hasNext()) {
				String tag = tagIter.next();
				if (!distinctPairsTransitionProb.containsKey(tag))
					distinctPairsTransitionProb.put(tag, 0);
				distinctPairsTransitionProb.put(tag, distinctPairsTransitionProb.get(tag) + 1);
				// Increment the total number of distinct pairs of (prevTag,
				// tag)
				// at the same time
				totalDistinctPairsTransitionProb++;
			}
		}

		// Find alpha
		prevTagIter = ALL_POS_TAGS.getIterator();
		while (prevTagIter.hasNext()) {
			String prevTag = prevTagIter.next();
			// </s> cannot be a previous tag
			if (prevTag.equals("</s>"))
				continue;
			double alpha = alphaNumerator(prevTagAndTagCount, tagCount, prevTag, DTransitionProb) / alphaDenominator(
					prevTagAndTagCount, distinctPairsTransitionProb, totalDistinctPairsTransitionProb, prevTag);
			// System.out.println("alpha transition prob for " + prevTag + ": "
			// + alpha);
			alphaTransitionProb.put(prevTag, alpha);
		}

		isTransitionProbInitialised = true;
	}

	/**
	 * Compute numerator of alpha = 1 - {sum[(C(w-1, w0) - D)/C(w-1)] over all
	 * w-1 where C(w-1, w0) > 0 }
	 * 
	 * @param wMinus1AndW0Count
	 *            C(w-1, w0)
	 * @param wMinus1Count
	 *            C(w-1)
	 * @param wMinus1
	 *            (w-1)
	 * @param D
	 * @return numerator of alpha
	 */
	private double alphaNumerator(Map<String, Map<String, Integer>> wMinus1AndW0Count,
			Map<String, Integer> wMinus1Count, String wMinus1, double D) {
		// {sum[(C(w-1, w0) - D)/C(w-1)] over all w-1 where C(w-1, w0) > 0 }
		double sumOfw0GivenWMinus1 = 0;
		Iterator<String> w0Iter = wMinus1AndW0Count.get(wMinus1).keySet().iterator();
		while (w0Iter.hasNext()) {
			String w0 = w0Iter.next();
			sumOfw0GivenWMinus1 += (wMinus1AndW0Count.get(wMinus1).get(w0).doubleValue() - D)
					/ wMinus1Count.get(wMinus1).doubleValue();
		}
		// System.out.println("Numer: " + sumOfw0GivenWMinus1);
		return 1.0 - sumOfw0GivenWMinus1;
	}

	/**
	 * Compute denominator of alpha = 1 - {sum[|{w-1: C(w-1, w)>0}| / sum(|{w-1:
	 * C(w-1, w)>0}|)] over all w-1 where C(w-1, w0) > 0 }
	 * 
	 * @param wMinus1AndW0Count
	 * @param distinctPairs
	 * @param totalDistinctPairs
	 * @param wMinus1
	 * @return denominator of alpha
	 */
	private double alphaDenominator(Map<String, Map<String, Integer>> wMinus1AndW0Count,
			Map<String, Integer> distinctPairs, int totalDistinctPairs, String wMinus1) {
		// {sum[|{w-1: C(w-1, w)>0}| / sum(|{w-1: C(w-1, w)>0}|)] over all w-1
		// where C(w-1, w0) > 0 }
		double sumOfDistinctPairsOverTotalDistinctPairs = 0;
		Iterator<String> w0Iter = wMinus1AndW0Count.get(wMinus1).keySet().iterator();
		while (w0Iter.hasNext()) {
			String w0 = w0Iter.next();
			sumOfDistinctPairsOverTotalDistinctPairs += distinctPairs.get(w0).doubleValue() / totalDistinctPairs;
			// if(wMinus1.equals("IN"))
			// System.out.println("w0: " + distinctPairs.get(w0));
		}

		return 1.0 - sumOfDistinctPairsOverTotalDistinctPairs;
	}

	@Override
	public boolean nextSetOfParameters() {
		double DEmissionProbInterval = (D_EMISSION_PROB_RANGE[1] - D_EMISSION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		double DTransitionProbInterval = (D_TRANSITION_PROB_RANGE[1] - D_TRANSITION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		// All parameters values are exhausted
		if (DEmissionProb + DEmissionProbInterval > D_EMISSION_PROB_RANGE[1]
				&& DTransitionProb + DTransitionProbInterval > D_TRANSITION_PROB_RANGE[1])
			return false;
		// Increment lambda1TransitionProb first then increment
		// lambda1EmissionProb when
		// lambda1TransitionProb exhuasted its range
		if (DTransitionProb + DTransitionProbInterval > D_TRANSITION_PROB_RANGE[1]) {
			setDTransitionProb(D_TRANSITION_PROB_RANGE[0]);
			setDEmissionProb(DEmissionProb + DEmissionProbInterval);
			return true;
		} else {
			setDTransitionProb(DTransitionProb + DTransitionProbInterval);
			return true;
		}
	}

	@Override
	public void rememberCurrentParametersAsBest() {
		DEmissionProbBest = DEmissionProb;
		DTransitionProbBest = DTransitionProb;
	}

	@Override
	public void setParametersToBest() {
		setDEmissionProb(DEmissionProbBest);
		setDTransitionProb(DTransitionProbBest);
	}

	@Override
	public void setParametersToDefault() {
		DEmissionProbBest = D_EMISSION_PROB_RANGE[0];
		DTransitionProbBest = D_TRANSITION_PROB_RANGE[0];
		setDEmissionProb(D_EMISSION_PROB_RANGE[0]);
		setDTransitionProb(D_TRANSITION_PROB_RANGE[0]);
	}

	@Override
	public String getParamtersValues() {
		return DEmissionProb + "," + DTransitionProb;
	}

}
