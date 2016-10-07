
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class Model implements Serializable {

	private static final long serialVersionUID = -957852697501339240L;

	// Settings for tuning
	transient protected static final TuningSettings TUNING_SETTINGS = new TuningSettings();
	// Set of all possible POS Tags
	transient protected static final POSTags ALL_POS_TAGS = new POSTags();
	// Set of suffixes, for use during the unknown word model
	transient private static final Suffixes SUFFIXES = new Suffixes();

	transient private String trainingFile;

	/** Written to model_file **/
	protected Set<String> vocabulary;

	// Count of total words/tags pairs. Used during smoothing
	protected int totalTokensCount;

	// C(t)
	/** Written to model_file **/
	protected Map<String, Integer> tagCount;
	// C(w)
	transient protected Map<String, Integer> wordCount;
	// C(w,t)
	transient protected Map<String, Map<String, Integer>> tagAndWordCount;
	// P(w|t)
	/** Written to model_file **/
	private Map<String, Map<String, Double>> wordGivenTag;
	// C(ti,ti-1)
	transient protected Map<String, Map<String, Integer>> prevTagAndTagCount;
	// P(ti|ti-1)
	/** Written to model_file **/
	private Map<String, Map<String, Double>> tagGivenPrevTag;

	// Count of capitalization and suffixes in words in training set. Used to
	// calculate emission probabilities for unknown words
	// C(cap,t)
	/** Written to model_file **/
	private Map<String, Integer> tagAndContainsCapitalCount;
	// C(suf,t)
	/** Written to model_file **/
	private Map<String, Map<String, Integer>> tagAndSuffixCount;
	// P(unknown word|t)
	// private Map<String, Double> unknownWordGivenTag;

	/** Written to model_file **/
	protected boolean isTrained;

	public Model(String trainingFile) {
		initLearner();
		this.trainingFile = trainingFile;
	}

	public void initLearner() {
		isTrained = false;

		totalTokensCount = 0;
		vocabulary = new HashSet<String>();
		tagCount = new HashMap<String, Integer>();
		wordCount = new HashMap<String, Integer>();
		tagAndWordCount = new HashMap<String, Map<String, Integer>>();
		wordGivenTag = new HashMap<String, Map<String, Double>>();
		prevTagAndTagCount = new HashMap<String, Map<String, Integer>>();
		tagGivenPrevTag = new HashMap<String, Map<String, Double>>();

		tagAndContainsCapitalCount = new HashMap<String, Integer>();
		tagAndSuffixCount = new HashMap<String, Map<String, Integer>>();
		// unknownWordGivenTag = new HashMap<String, Double>();

		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			tagCount.put(tag, 0);
			tagAndWordCount.put(tag, new HashMap<String, Integer>());
			wordGivenTag.put(tag, new HashMap<String, Double>());
			prevTagAndTagCount.put(tag, new HashMap<String, Integer>());
			tagGivenPrevTag.put(tag, new HashMap<String, Double>());
			tagAndContainsCapitalCount.put(tag, 0);
			tagAndSuffixCount.put(tag, new HashMap<String, Integer>());
		}
	}

	/**
	 * Compute the training statistics necessary for the Verbeti alogrithm based
	 * on the training file
	 * 
	 * @throws NoSuchFieldException
	 */
	public void loadCountStatistics() throws NoSuchFieldException {

		initLearner();

		TaggedSetReader reader = new TaggedSetReader(trainingFile);
		// Initialize the <s> tag count first
		tagCount.put("<s>", 0);

		// Iterate through each token of a line, line by line and calculate the
		// emission and transition probabilities
		while (reader.nextLine()) {
			String prevTag = "<s>";
			while (reader.nextToken()) {

				// Get the word and tag for this token
				String[] splitWordAndTag = reader.getCurrTokenSplitWordTag();
				String word = splitWordAndTag[0];
				String tag = splitWordAndTag[1];

				// When handling the first word, decapitalize the first letter
				// if necessary to reduce duplicated vocabulary.
				// E.g. If "he" appears as the first word, it is written as
				// "He", which should be converted to "he" as they are the same
				// word.
				// First case is when first token in line is a word,
				// decapitalize first word if only first letter is uppercase,
				// except word "I"
				if (reader.getCurrTokenIndex() == 0 && !word.equals("I"))
					if (word.substring(1).toLowerCase().equals(word.substring(1)))
						word = word.toLowerCase();
				// Second case is when first token in line is ``, decapitalize
				// second word if only first letter is uppercase, except word
				// "I"
				if (reader.getCurrTokenIndex() == 1 && prevTag.equals("``") && !word.equals("I"))
					if (word.substring(1).toLowerCase().equals(word.substring(1)))
						word = word.toLowerCase();

				// Update total tokens count
				totalTokensCount++;

				// Update the vocabulary
				vocabulary.add(word);
				// Update word count
				if (!wordCount.containsKey(word))
					wordCount.put(word, 0);
				wordCount.put(word, wordCount.get(word) + 1);

				// Update the tag count
				if (!ALL_POS_TAGS.has(tag))
					throw new NoSuchFieldException(tag + " not found in list of POS tags");
				// If this is first token, update count of <s> first
				if (prevTag == "<s>")
					tagCount.put(prevTag, tagCount.get(prevTag) + 1);
				tagCount.put(tag, tagCount.get(tag) + 1);

				// Update the emission probability
				// emissionProb.addWordAndTagCount(tag, word);
				if (!tagAndWordCount.get(tag).containsKey(word))
					tagAndWordCount.get(tag).put(word, 0);
				tagAndWordCount.get(tag).put(word, tagAndWordCount.get(tag).get(word) + 1);

				// Update the transition probability
				// transitionProb.addPrevTagAndTagCount(prevTag, tag);
				if (!prevTagAndTagCount.get(prevTag).containsKey(tag))
					prevTagAndTagCount.get(prevTag).put(tag, 0);
				prevTagAndTagCount.get(prevTag).put(tag, prevTagAndTagCount.get(prevTag).get(tag) + 1);

				// If this is the last token, update the additional transition
				// probability P(</s>|tT)
				if (reader.isLastToken()) {
					// transitionProb.addPrevTagAndTagCount(tag, "</s>");
					if (!prevTagAndTagCount.get(tag).containsKey("</s>"))
						prevTagAndTagCount.get(tag).put("</s>", 0);
					prevTagAndTagCount.get(tag).put("</s>", prevTagAndTagCount.get(tag).get("</s>") + 1);
				}

				// Update C(cap, t)
				if (containsCapital(word))
					tagAndContainsCapitalCount.put(tag, tagAndContainsCapitalCount.get(tag) + 1);

				// Update C(suf, t)
				String[] suffixes = SUFFIXES.getAllSuffixes(word);
				for (String suffix : suffixes) {
					if (!tagAndSuffixCount.get(tag).containsKey(suffix))
						tagAndSuffixCount.get(tag).put(suffix, 0);
					tagAndSuffixCount.get(tag).put(suffix, tagAndSuffixCount.get(tag).get(suffix) + 1);
				}

				prevTag = tag;
			}
		}
	}

	/**
	 * Go to the next set of parameters in the model. Used during tuning.
	 * 
	 * @return True if next set of parameters is set. False if all parameters
	 *         values have been returned.
	 */
	abstract public boolean nextSetOfParameters();

	/**
	 * Remember the current parameters as the best. Used during tuning.
	 */
	abstract public void rememberCurrentParametersAsBest();

	/**
	 * Reset the parameters to best values. Used during tuning.
	 */
	abstract public void setParametersToBest();

	abstract public void setParametersToDefault();

	abstract public String getParamtersValues();

	/**
	 * Get P(w|t) for a word w and tag t.
	 * 
	 * @param tag
	 *            The query POS tag t
	 * @param word
	 *            The query word w
	 * @return P(w|t) if word exists in vocabulary, P(w|t) using unknown word
	 *         model if word does not exist in vocabulary
	 */
	public double getWordGivenTag(String tag, String word) throws IllegalStateException {
		if (!isTrained)
			throw new IllegalStateException("Model is not trained!");
		// Word is in vocabulary
		if (vocabulary.contains(word))
			return wordGivenTag.get(tag).get(word);
		// Word is not in vocabulary, estimate P(w|t) using unknown word model
		else
			return emissionProbUnknownWordModel(tag, word);
	}

	/**
	 * Get P(ti|ti-1) for a tag ti and tag ti-1
	 * 
	 * @param prevTag
	 *            Previous POS tag ti-1
	 * @param tag
	 *            Current POS tag ti
	 * @return P(ti|ti-1)
	 */
	public double getTagGivenPrevTag(String prevTag, String tag) throws IllegalStateException {
		if (!isTrained)
			throw new IllegalStateException("Model is not trained!");
		return tagGivenPrevTag.get(prevTag).get(tag);
	}

	public boolean isTrained() {
		return isTrained;
	}

	public void computeHMMStatistics() {
		computeEmissionProbabilities();
		computeTransitionProbabilities();
		isTrained = true;
	}

	/**
	 * Compute P(w|t) based on the current count statistics and smoothing
	 * parameters
	 */
	public void computeEmissionProbabilities() {
		// For known words
		Iterator<String> tagsIter = tagCount.keySet().iterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			Iterator<String> wordsIter = vocabulary.iterator();
			while (wordsIter.hasNext()) {
				String word = wordsIter.next();
				double probability;
				// P(w|<s>) and P(w|</s>) = 0
				if (tag.equals("<s>") || tag.equals("</s>"))
					probability = 0.0;
				else if (tagAndWordCount.get(tag).containsKey(word))
					probability = nonZeroEmissionProb(tag, word);
				else
					probability = zeroEmissionProb(tag, word);
				wordGivenTag.get(tag).put(word, probability);
			}
		}
	}

	/**
	 *  Compute P(ti|ti-1) based on the current count statistics and smoothing
	 * 	parameters
	 */
	public void computeTransitionProbabilities() {
		Iterator<String> prevTagsIter = tagCount.keySet().iterator();
		while (prevTagsIter.hasNext()) {
			String prevTag = prevTagsIter.next();
			Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
			while (tagsIter.hasNext()) {
				String tag = tagsIter.next();
				double probability;
				// P(<s>|ti-1) and P(t|</s>) = 0
				if (tag.equals("<s>") || prevTag.equals("</s>"))
					probability = 0.0;
				else if (prevTagAndTagCount.get(prevTag).containsKey(tag))
					probability = nonZeroTransitionProb(prevTag, tag);
				else
					probability = zeroTransitionProb(prevTag, tag);
				tagGivenPrevTag.get(prevTag).put(tag, probability);
			}
		}
	}

	public int getNumTuningIterations() {
		return (TUNING_SETTINGS.NUM_TRIALS + 1) * (TUNING_SETTINGS.NUM_TRIALS + 1);
	}

	/**
	 * Compute smoothed P(w|t) when C(w,t) > 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(w|t)
	 */
	abstract protected double nonZeroEmissionProb(String tag, String word);

	/**
	 * Compute smoothed P(ti|i-1) when C(ti-1,ti) > 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(ti|i-1)
	 */
	abstract protected double nonZeroTransitionProb(String prevTag, String tag);

	/**
	 * Compute smoothed P(w|t) when C(w,t) = 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(w|t)
	 */
	abstract protected double zeroEmissionProb(String tag, String word);

	/**
	 * Compute smoothed P(ti|i-1) when C(ti-1,ti) = 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(ti-1|i)
	 */
	abstract protected double zeroTransitionProb(String prevTag, String tag);

	/**
	 * Estimates emission probability for an unknown word.
	 * 
	 * This model first examines for the features of the word (presence of
	 * capital letters and individual suffixes), and returns the product of each
	 * P(feature|tag). If the word does not have any features, there is
	 * essentially no clue about the word, and thus the best guess is to return
	 * probability of tag.
	 * 
	 * Add 1 smoothing is used when C(feature, tag) = 0
	 * 
	 * @param tag
	 *            The query POS tag
	 * @param word
	 *            The unknown word
	 * @return the estimated emission probability, P(w|t)
	 */
	private double emissionProbUnknownWordModel(String tag, String word) {

		double emissionProb = 1.0;

		// Include P(cap|tag) if word has capital letter
		// Add 1 smoothing is used if C(cap, tag) = 0
		if (containsCapital(word))
			emissionProb *= (tagAndContainsCapitalCount.get(tag).doubleValue() + 1)
					/ (tagCount.get(tag).doubleValue() + 2);

		// Include P(suffix|tag) for all the suffixes that word contains
		// Add 1 smoothing is used if C(suf, tag) = 0
		String[] suffixes = SUFFIXES.getAllSuffixes(word);
		for (String suffix : suffixes) {
			emissionProb *= (tagAndSuffixCount.get(tag).containsKey(suffix)
					? (tagAndSuffixCount.get(tag).get(suffix).doubleValue() + 1)
							/ (tagCount.get(tag).doubleValue() + SUFFIXES.size())
					: 1 / (tagCount.get(tag).doubleValue() + SUFFIXES.size()));
		}

		// Lastly include the probability of the tag in the corpus
		emissionProb *= tagCount.get(tag).doubleValue() / totalTokensCount;

		return emissionProb;
	}

	/**
	 * @param word
	 * @return true if word contains 1 or more capital letter
	 */
	private boolean containsCapital(String word) {
		return !word.equals(word.toLowerCase());
	}

}
