

/**
 * This class implements the Witten-Bell smoothing method to handle zero counts. 
 * P(a|b) = C(a,b)/(C(b) +T(b)) for C(a,b) > 0
 * P(a|b) = T(b)/(Z(b) * (C(b) +T(b))) for C(a,b) > 0
 * 
 * @author Shao Fei
 *
 */
public class ModelWittenBell extends Model {

	private static final long serialVersionUID = 1593848188208510367L;

	public ModelWittenBell(String trainingFile) {
		super(trainingFile);
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

	@Override
	public String getParamtersValues() {
		return "No parameters with Witten-Bell method";
	}

	@Override
	protected double nonZeroEmissionProb(String tag, String word) {
		// Total number of seen word/tag types
		double t = (double) tagAndWordCount.get(tag).size();
		return tagAndWordCount.get(tag).get(word).doubleValue() / (tagCount.get(tag).doubleValue() + t);
	}

	@Override
	protected double nonZeroTransitionProb(String prevTag, String tag) {
		// Total number of seen prevTag/tag types
		double t = (double) prevTagAndTagCount.get(prevTag).size();
		return prevTagAndTagCount.get(prevTag).get(tag).doubleValue() / (tagCount.get(prevTag).doubleValue() + t);
	}

	@Override
	protected double zeroEmissionProb(String tag, String word) {
		// Total number of seen word/tag types
		double t = (double) tagAndWordCount.get(tag).size();
		// Total number of unseen word/tag types
		double z = (double) (vocabulary.size() - t);
		return t / (z * (tagCount.get(tag).doubleValue() + t));
	}

	@Override
	protected double zeroTransitionProb(String prevTag, String tag) {
		// Total number of seen prevTag/tag types
		double t = (double) prevTagAndTagCount.get(prevTag).size();
		// Total number of unseen prevTag/tag types
		double z = (double) (ALL_POS_TAGS.size() - t);
		return t / (z * (tagCount.get(prevTag).doubleValue() + t));
	}

}
