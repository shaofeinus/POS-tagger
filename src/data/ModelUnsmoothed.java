package data;

/**
 * This class implements a model that does not use any smoothing to handle zero
 * counts. Hence for C(w,t) = 0 and C(ti-1, ti) = 0, P(w|t) and P(ti|ti-1) are 0
 * respectively.
 * 
 * @author Shao Fei
 *
 */
public class ModelUnsmoothed extends Model {

	public ModelUnsmoothed(String trainingFile) {
		super(trainingFile);
	}

	@Override
	protected double zeroCountEmissionProb(String tag, String word) {
		// P(w|t) = 0
		return 0;
	}

	@Override
	protected double zeroCountTransitionProb(String prevTag, String tag) {
		// P(ti|ti-1) = 0
		return 0;
	}

	@Override
	protected double nonZeroCountEmissionProb(String tag, String word) {
		// P(w|t) = C(w,t)/C(t)
		return tagAndWordCount.get(tag).get(word).doubleValue() / tagCount.get(tag).doubleValue();
	}

	@Override
	protected double nonZeroCountTransitionProb(String prevTag, String tag) {
		// P(ti|ti-1) = C(ti,ti-1)/C(ti)
		return prevTagAndTagCount.get(prevTag).get(tag).doubleValue() / tagCount.get(prevTag).doubleValue();
	}

	@Override
	public boolean nextSetOfParameters() {
		// No parameters to vary
		return false;
	}

	@Override
	public void rememberCurrentParametersAsBest() {
		// No parameters to remember
		return;
	}

	@Override
	public void setParametersToBest() {
		// No parameters to vary
		return;
	}

	@Override
	public void setParametersToDefault() {
		// No parameters to set
		return;
	}

}
