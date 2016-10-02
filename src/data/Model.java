package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.TaggedSetReader;

public abstract class Model {

	// Set of all possible POS Tags
	transient protected static final POSTags ALL_POS_TAGS = new POSTags();
	// Set of suffixes, for use during the unknown word model
	transient private static final Suffixes SUFFIXES = new Suffixes();

	transient private String trainingFile;

	// private EmissionProbabilities emissionProb;
	// private TransitionProbabilties transitionProb;
	protected Set<String> vocabulary;

	// Count of total words/tags pairs. Used during smoothing
	protected int totalTokensCount;

	// C(t)
	transient protected Map<String, Integer> tagCount;
	// C(w)
	transient protected Map<String, Integer> wordCount;
	// C(w,t)
	transient protected Map<String, Map<String, Integer>> tagAndWordCount;
	// P(w|t)
	private Map<String, Map<String, Double>> wordGivenTag;
	// C(ti,ti-1)
	transient protected Map<String, Map<String, Integer>> prevTagAndTagCount;
	// P(ti|ti-1)
	private Map<String, Map<String, Double>> tagGivenPrevTag;

	// Count of capitalization and suffixes in words in training set. Used to
	// calculate emission probabilities for unknown words
	// C(cap,t)
	transient private Map<String, Integer> tagAndCapCount;
	// C(suf,t)
	transient private Map<String, Integer> tagAndSufCount;
	// P(unknown word|t)
	private Map<String, Double> unknownWordGivenTag;

	private boolean learned;

	public Model(String trainingFile) {
		initLearner();
		this.trainingFile = trainingFile;
	}

	public void initLearner() {
		learned = false;

		totalTokensCount = 0;
		vocabulary = new HashSet<String>();
		tagCount = new HashMap<String, Integer>();
		wordCount = new HashMap<String, Integer>();
		tagAndWordCount = new HashMap<String, Map<String, Integer>>();
		wordGivenTag = new HashMap<String, Map<String, Double>>();
		prevTagAndTagCount = new HashMap<String, Map<String, Integer>>();
		tagGivenPrevTag = new HashMap<String, Map<String, Double>>();

		tagAndCapCount = new HashMap<String, Integer>();
		tagAndSufCount = new HashMap<String, Integer>();
		unknownWordGivenTag = new HashMap<String, Double>();

		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			tagCount.put(tag, 0);
			tagAndWordCount.put(tag, new HashMap<String, Integer>());
			wordGivenTag.put(tag, new HashMap<String, Double>());
			prevTagAndTagCount.put(tag, new HashMap<String, Integer>());
			tagGivenPrevTag.put(tag, new HashMap<String, Double>());
			tagAndCapCount.put(tag, 0);
			tagAndSufCount.put(tag, 0);
		}

		// emissionProb = new EmissionProbabilities(tagCount, wordCount,
		// vocabulary);
		// transitionProb = new TransitionProbabilties(tagCount);
	}

	/**
	 * Compute the training statistics necessary for the Verbeti alogrithm based
	 * on the training file
	 * 
	 * @throws NoSuchFieldException
	 */
	public void learn() throws NoSuchFieldException {

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
					tagAndCapCount.put(tag, tagAndCapCount.get(tag) + 1);
				// Update C(suf, t)
				if (containsSuffix(word))
					tagAndSufCount.put(tag, tagAndSufCount.get(tag) + 1);

				prevTag = tag;
			}
		}

		computeEmissionProbabilities();
		computeTransitionProbabilities();
		learned = true;
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

	/**
	 * Get P(w|t) for a word w and tag t.
	 * 
	 * @param tag
	 *            The query POS tag
	 * @param word
	 *            The query word
	 * @return P(w|t) if word exists in vocabulary, P(w|t) using unknown word
	 *         model if word does not exist in vocabulary
	 */
	public double getWordGivenTag(String tag, String word) {
		// Word is in vocabulary
		if (vocabulary.contains(word))
			return wordGivenTag.get(tag).get(word);
		// Word is not in vocabulary, estimate P(w|t) using unknown word model
		else
			return unknownWordGivenTag.get(tag);
	}

	/**
	 * Get P(ti|ti-1) for a tag ti and tag ti-1
	 * 
	 * @param prevTag
	 *            Previous POS tag
	 * @param tag
	 *            Current POS tag
	 * @return P(ti|ti-1)
	 */
	public double getTagGivenPrevTag(String prevTag, String tag) {
		return tagGivenPrevTag.get(prevTag).get(tag);
	}

	public boolean isLearned() {
		return learned;
	}

	public void computeEmissionProbabilities() {
		// For statistics gathering
		double total = 0.0;
		int count = 0;
		double min = 0;

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
					probability = nonZeroCountEmissionProb(tag, word);
				else
					probability = zeroCountEmissionProb(tag, word);
				wordGivenTag.get(tag).put(word, probability);
				// For statistics gathering
				min = Math.min(min, probability);
				total += probability;
				count++;
			}
		}

		System.out.println("Computed known words emission prob Ave: " + total / count + " Min: " + min);

		// For unknown words
		total = 0.0;
		count = 0;
		min = 1;
		tagsIter = tagCount.keySet().iterator();
		while (tagsIter.hasNext()) {
			String tag = tagsIter.next();
			double probability;
			// P(w|<s>) and P(w|</s>) = 0
			if (tag.equals("<s>") || tag.equals("</s>"))
				probability = 0;
			else
				probability = emissionProbUnknownWordModel(tag);
			unknownWordGivenTag.put(tag, probability);
			min = Math.min(min, probability);
			total += probability;
			count++;
		}
		System.out.println("Computed unknown words emission prob Ave: " + total / count + " Min: " + min);
	}

	public void computeTransitionProbabilities() {
		// For statistics gathering
		double total = 0.0;
		int count = 0;
		double min = 0;
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
					probability = nonZeroCountTransitionProb(prevTag, tag);
				else
					probability = zeroCountTransitionProb(prevTag, tag);
				tagGivenPrevTag.get(prevTag).put(tag, probability);

				// For statistics gathering
				min = Math.min(min, probability);
				total += probability;
				count++;
			}
		}
		System.out.println("Computed transition prob Ave: " + total / count + " Min: " + min);
	}

	/**
	 * Compute smoothed P(w|t) when C(w,t) > 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(w|t)
	 */
	abstract protected double nonZeroCountEmissionProb(String tag, String word);

	/**
	 * Compute smoothed P(ti-1|i) when C(ti-1,ti) > 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(ti-1|i)
	 */
	abstract protected double nonZeroCountTransitionProb(String prevTag, String tag);

	/**
	 * Compute smoothed P(w|t) when C(w,t) = 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(w|t)
	 */
	abstract protected double zeroCountEmissionProb(String tag, String word);

	/**
	 * Compute smoothed P(ti-1|i) when C(ti-1,ti) = 0
	 * 
	 * @param tag
	 * @param word
	 * @return the smoothed P(ti-1|i)
	 */
	abstract protected double zeroCountTransitionProb(String prevTag, String tag);

	/**
	 * Estimates emission probability for an unknown word using the Unknown word
	 * model P(w|t) = P(unknown word|t)*P(capital|t)*P(suffixes|t)
	 * 
	 * @param tag
	 *            The query POS tag
	 * @return the estimated emission probability, P(w|t)
	 */
	private double emissionProbUnknownWordModel(String tag) {
		return 1.0 / tagCount.get(tag) * // P(unknown word|t)
				tagAndCapCount.get(tag).doubleValue() / tagCount.get(tag).doubleValue() * // P(capital|t)
				tagAndSufCount.get(tag).doubleValue() / tagCount.get(tag).doubleValue(); // P(suffixes|t)
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
