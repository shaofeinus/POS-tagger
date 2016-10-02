package data;

/**
 * This class implements an Add-N smoothing method to handle zero counts, where
 * a constant n is added to the counts of all observations C(a,b): P(a|b) =
 * [C(a,b) + n]/[C(b) + n*B] B = Number of distinct a
 * 
 * @author Shao Fei
 *
 */
public class ModelAddN extends Model {

	private static final double[] N_EMISSION_PROB_RANGE = { 0.0, 5.0 };
	private static final double[] N_TRANSITION_PROB_RANGE = { 0.0, 1.0 };
	private static final int TUNING_ITERATIONS_FOR_EACH_PARAM = 10;

	// Used during tuning
	// 1 for emission probability and 1 for transition probability
	transient private double nEmissionProbBest;
	transient private double nTransitionProbBest;

	// 1 for emission probability and 1 for transition probability
	transient private double nEmissionProb;
	transient private double nTransitionProb;

	public ModelAddN(String trainingFile) {
		super(trainingFile);
		setParametersToDefault();
	}

	public void setNEmissionProb(double n) {
		nEmissionProb = n;
	}

	public void setNTransitionProb(double n) {
		nTransitionProb = n;
	}

	@Override
	protected double nonZeroCountEmissionProb(String tag, String word) {
		// P(w|t) = [C(w,t) + n]/[C(t) + n*vocabulary size]
		return (tagAndWordCount.get(tag).get(word).doubleValue() + nEmissionProb)
				/ (tagCount.get(tag).doubleValue() + nEmissionProb * vocabulary.size());
	}

	@Override
	protected double nonZeroCountTransitionProb(String prevTag, String tag) {
		// P(ti|ti-1) = [C(ti,ti-1) + n]/[C(ti) + n*no of tags]
		return (prevTagAndTagCount.get(prevTag).get(tag).doubleValue() + nTransitionProb)
				/ (tagCount.get(prevTag).doubleValue() + nTransitionProb * ALL_POS_TAGS.size());
	}

	@Override
	protected double zeroCountEmissionProb(String tag, String word) {
		// P(w|t) = n/[C(t) + n*vocabulary size]
		return nEmissionProb / (tagCount.get(tag).doubleValue() + nEmissionProb * vocabulary.size());
	}

	@Override
	protected double zeroCountTransitionProb(String prevTag, String tag) {
		// P(w|t) = n/[C(ti) + n*no of tags]]
		return nTransitionProb / (tagCount.get(prevTag).doubleValue() + nTransitionProb * ALL_POS_TAGS.size());
	}

	@Override
	public boolean nextSetOfParameters() {
		double nEmissionProbInterval = (N_EMISSION_PROB_RANGE[1] - N_EMISSION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		double nTransitionProbInterval = (N_TRANSITION_PROB_RANGE[1] - N_TRANSITION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		// All parameters values are exhausted
		if (nEmissionProb == N_EMISSION_PROB_RANGE[1] && nTransitionProb == N_TRANSITION_PROB_RANGE[1])
			return false;
		// Increment nTransitionProb first then increment nEmissionProb when
		// nTransitionProb exhuasted its range
		if (nTransitionProb == N_TRANSITION_PROB_RANGE[1]) {
			nTransitionProb = N_TRANSITION_PROB_RANGE[0];
			nEmissionProb += nEmissionProbInterval;
			return true;
		} else {
			nTransitionProb += nTransitionProbInterval;
			return true;
		}
	}

	@Override
	public void rememberCurrentParametersAsBest() {
		nEmissionProbBest = nEmissionProb;
		nTransitionProbBest = nTransitionProb;
	}

	@Override
	public void setParametersToBest() {
		nEmissionProb = nEmissionProbBest;
		nTransitionProb = nTransitionProbBest;
	}

	@Override
	public void setParametersToDefault() {
		// Default value of n = 0. Equivalent to the unsmoothed method
		nEmissionProbBest = N_EMISSION_PROB_RANGE[0];
		nTransitionProbBest = N_TRANSITION_PROB_RANGE[0];
		nEmissionProb = N_EMISSION_PROB_RANGE[0];
		nTransitionProb = N_TRANSITION_PROB_RANGE[0];
	}

}
