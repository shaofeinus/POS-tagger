

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the final model used in the POS tagger. Kneyser-Ney smoothing is used
 * for emission probabilities and Interpolation is used for transition
 * probabilites.
 * 
 * @author Shao Fei
 *
 */
public class ModelFinal extends Model {

	private static final long serialVersionUID = -6793069242017642575L;
	
	private static final double[] D_EMISSION_PROB_RANGE = TUNING_SETTINGS.D_EMISSION_PROB_RANGE;
	private static final double[] LAMDA_1_TRANSITION_PROB_RANGE = TUNING_SETTINGS.LAMBDA_1_TRANSITION_PROB_RANGE;
	private static final int TUNING_ITERATIONS_FOR_EACH_PARAM = TUNING_SETTINGS.NUM_TRIALS;

	// D is used to obtain P(w|t) when C(w,t) > 0
	transient private double DEmissionProb;
	// Used during tuning
	transient private double DEmissionProbBest;
	// alpha is used to obtain P(w|t) when C(w,t) = 0
	// 1 for emission probability and 1 for transition probability
	transient private Map<String, Double> alphaEmissionProb;
	// |{t: C(t, w)>0}| for each w. I.e. Number of distinct pairs (t, w)
	// for given a w
	transient private Map<String, Integer> distinctPairsEmissionProb;
	// Sum(|{t: C(t, w)>0}|) over all w. I.e. Total number of distinct
	// pairs (t, w) in corpus
	transient private int totalDistinctPairsEmissionProb;
	transient boolean isEmissionProbInitialised;

	// Lambda is used to obtain P(ti|ti-1)
	transient private double lambda1TransitionProb, lambda2TransitionProb;
	// Used during tuning
	transient private double lambda1TransitionProbBest;
	

	public ModelFinal(String trainingFile) {
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
	 * Set the lambda1 value. lambda2 is automatically set based on the
	 * constraint lambda1 + lambda2 == 1
	 * 
	 * @param value
	 *            The value to be set for lambda1
	 */
	public void setLambda1TransitionProb(double value) {
		lambda1TransitionProb = value;
		lambda2TransitionProb = 1 - lambda1TransitionProb;
	}

	@Override
	public boolean nextSetOfParameters() {
		double DEmissionProbInterval = (D_EMISSION_PROB_RANGE[1] - D_EMISSION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		double lambda1TransitionProbInterval = (LAMDA_1_TRANSITION_PROB_RANGE[1] - LAMDA_1_TRANSITION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		// All parameters values are exhausted
		if (DEmissionProb + DEmissionProbInterval > D_EMISSION_PROB_RANGE[1]
				&& lambda1TransitionProb - lambda1TransitionProbInterval< LAMDA_1_TRANSITION_PROB_RANGE[0])
			return false;
		// Increment lambda1TransitionProb first then increment
		// DEmissionProb when
		// lambda1TransitionProb exhausted its range
		if (lambda1TransitionProb - lambda1TransitionProbInterval < LAMDA_1_TRANSITION_PROB_RANGE[0]) {
			setLambda1TransitionProb(LAMDA_1_TRANSITION_PROB_RANGE[1]);
			setDEmissionProb(DEmissionProb + DEmissionProbInterval);
			return true;
		} else {
			setLambda1TransitionProb(lambda1TransitionProb - lambda1TransitionProbInterval);
			return true;
		}
	}

	@Override
	public void rememberCurrentParametersAsBest() {
		DEmissionProbBest = DEmissionProb;
		lambda1TransitionProbBest = lambda1TransitionProb;
	}

	@Override
	public void setParametersToBest() {
		setDEmissionProb(DEmissionProbBest);
		setLambda1TransitionProb(lambda1TransitionProbBest);
	}

	@Override
	public void setParametersToDefault() {
		DEmissionProbBest = D_EMISSION_PROB_RANGE[0];
		setDEmissionProb(D_EMISSION_PROB_RANGE[0]);
		lambda1TransitionProbBest = LAMDA_1_TRANSITION_PROB_RANGE[1];
		lambda1TransitionProb = LAMDA_1_TRANSITION_PROB_RANGE[1];
		lambda2TransitionProb = 1 - lambda1TransitionProb;
	}

	@Override
	public String getParamtersValues() {
		return DEmissionProb + "," + lambda1TransitionProb;
	}

	@Override
	protected double nonZeroEmissionProb(String tag, String word) {
		return (tagAndWordCount.get(tag).get(word).doubleValue() - DEmissionProb) / tagCount.get(tag);
	}

	@Override
	protected double nonZeroTransitionProb(String prevTag, String tag) {
		assert lambda1TransitionProb + lambda2TransitionProb == 1;
		// P(ti|ti-1) = lambda1 * P(ti|ti-1) + lambda2 * P(ti)
		return lambda1TransitionProb * prevTagAndTagCount.get(prevTag).get(tag).doubleValue()
				/ tagCount.get(prevTag).doubleValue()
				+ lambda2TransitionProb * tagCount.get(tag).doubleValue() / totalTokensCount;
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
		assert lambda1TransitionProb + lambda2TransitionProb == 1;
		// P(ti|ti-1) = lambda1 * 0 + lambda2 * P(ti) = lambda2 * P(ti)
		return lambda2TransitionProb * tagCount.get(tag).doubleValue() / totalTokensCount;
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
			alphaEmissionProb.put(tag, alpha);
		}

		isEmissionProbInitialised = true;
	}

	/**
	 * Compute numerator of alpha = 1 - {sum[(C(t, w) - D)/C(t)] over all t
	 * where C(t, w) > 0 }
	 * 
	 * @param wMinus1AndW0Count
	 *            C(t, w)
	 * @param wMinus1Count
	 *            C(t)
	 * @param wMinus1
	 *            (t)
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
		return 1.0 - sumOfw0GivenWMinus1;
	}

	/**
	 * Compute denominator of alpha = 1 - {sum[|{t: C(t, w)>0}| / sum(|{t: C(t,
	 * w)>0}|)] over all t where C(t, w) > 0 }
	 * 
	 * @param wMinus1AndW0Count
	 *            C(t, w)
	 * @param distinctPairs
	 * @param totalDistinctPairs
	 * @param wMinus1
	 *            (t)
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
		}

		return 1.0 - sumOfDistinctPairsOverTotalDistinctPairs;
	}

}
