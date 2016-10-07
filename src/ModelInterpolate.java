

/**
 * This class implements the interpolation model where P(a|b) = lambda1 * P(a|b)
 * + lambda2 * P(a), and lambda1 + lambda2 == 1 Hence for both the non-zero and
 * zero count cases, the formula for the emission and transitional probabilities
 * are the same
 * 
 * @author Shao Fei
 *
 */
public class ModelInterpolate extends Model {

	private static final long serialVersionUID = -3264495346224277868L;
	
	private static final double[] LAMBDA_1_EMISSION_PROB_RANGE = TUNING_SETTINGS.LAMBDA_1_EMISSION_PROB_RANGE;
	private static final double[] LAMBDA_1_TRANSITION_PROB_RANGE = TUNING_SETTINGS.LAMBDA_1_TRANSITION_PROB_RANGE;
	private static final int TUNING_ITERATIONS_FOR_EACH_PARAM = TUNING_SETTINGS.NUM_TRIALS;

	// Used during tuning
	// 1 for emission probability and 1 for transition probability
	transient private double lambda1EmissionProbBest;
	transient private double lambda1TransitionProbBest;

	// 1 for emission probability and 1 for transition probability
	transient private double lambda1EmissionProb, lambda2EmissionProb;
	transient private double lambda1TransitionProb, lambda2TransitionProb;

	public ModelInterpolate(String trainingFile) {
		super(trainingFile);
		setParametersToDefault();
	}

	/**
	 * Set the lambda1 value. lambda2 is automatically set based on the
	 * constraint lambda1 + lambda2 == 1
	 * 
	 * @param value
	 *            The value to be set for lambda1
	 */
	public void setLambda1EmissionProb(double value) {
		lambda1EmissionProb = value;
		lambda2EmissionProb = 1 - lambda1EmissionProb;
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
	protected double nonZeroEmissionProb(String tag, String word) {
		assert lambda1EmissionProb + lambda2EmissionProb == 1;
		// P(w|t) = lambda1 * P(w|t) + lambda2 * P(w)
		return lambda1EmissionProb * tagAndWordCount.get(tag).get(word).doubleValue() / tagCount.get(tag).doubleValue()
				+ lambda2EmissionProb * wordCount.get(word).doubleValue() / totalTokensCount;
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
		assert lambda1EmissionProb + lambda2EmissionProb == 1;
		// P(w|t) = lambda1 * 0 + lambda2 * P(w) = lambda2 * P(w)
		return lambda2EmissionProb * wordCount.get(word).doubleValue() / totalTokensCount;
	}

	@Override
	protected double zeroTransitionProb(String prevTag, String tag) {
		assert lambda1TransitionProb + lambda2TransitionProb == 1;
		// P(ti|ti-1) = lambda1 * 0 + lambda2 * P(ti) = lambda2 * P(ti)
		return lambda2TransitionProb * tagCount.get(tag).doubleValue() / totalTokensCount;
	}

	@Override
	public boolean nextSetOfParameters() {
		double lambda1EmissionProbInterval = (LAMBDA_1_EMISSION_PROB_RANGE[1] - LAMBDA_1_EMISSION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		double lambda1TransitionProbInterval = (LAMBDA_1_TRANSITION_PROB_RANGE[1] - LAMBDA_1_TRANSITION_PROB_RANGE[0])
				/ TUNING_ITERATIONS_FOR_EACH_PARAM;
		// All parameters values are exhausted
		if (lambda1EmissionProb - lambda1EmissionProbInterval < LAMBDA_1_EMISSION_PROB_RANGE[0]
				&& lambda1TransitionProb - lambda1TransitionProbInterval< LAMBDA_1_TRANSITION_PROB_RANGE[0])
			return false;
		// Increment lambda1TransitionProb first then increment
		// lambda1EmissionProb when
		// lambda1TransitionProb exhuasted its range
		if (lambda1TransitionProb - lambda1TransitionProbInterval < LAMBDA_1_TRANSITION_PROB_RANGE[0]) {
			setLambda1TransitionProb(LAMBDA_1_TRANSITION_PROB_RANGE[1]);
			setLambda1EmissionProb(lambda1EmissionProb - lambda1EmissionProbInterval);
			return true;
		} else {
			setLambda1TransitionProb(lambda1TransitionProb - lambda1TransitionProbInterval);
			return true;
		}
	}

	@Override
	public void rememberCurrentParametersAsBest() {
		lambda1EmissionProbBest = lambda1EmissionProb;
		lambda1TransitionProbBest = lambda1TransitionProb;
	}

	@Override
	public void setParametersToBest() {
		setLambda1EmissionProb(lambda1EmissionProbBest);
		setLambda1TransitionProb(lambda1TransitionProbBest);
	}

	@Override
	public void setParametersToDefault() {
		// Default value of lambda1 = 1 and lambda2 = 0. Equivalent to
		// unsmoothed model
		lambda1EmissionProbBest = LAMBDA_1_EMISSION_PROB_RANGE[1];
		lambda1TransitionProbBest = LAMBDA_1_TRANSITION_PROB_RANGE[1];
		lambda1EmissionProb = LAMBDA_1_EMISSION_PROB_RANGE[1];
		lambda2EmissionProb = 1 - lambda1EmissionProb;
		lambda1TransitionProb = LAMBDA_1_TRANSITION_PROB_RANGE[1];
		lambda2TransitionProb = 1 - lambda1TransitionProb;
	}

	@Override
	public String getParamtersValues() {
		return lambda1EmissionProb + "," + lambda1TransitionProb;
	}

}
