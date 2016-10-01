package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import util.TaggedSetReader;

public class ModelStatistics {

	transient private static final POSTags ALL_POS_TAGS = new POSTags();

	transient private String trainingFile;
	private EmissionProbabilities emissionProb;
	private TransitionProbabilties transitionProb;
	private Map<String, Integer> tagCount;
	private Set<String> vocabulary;
	private boolean learned;

	public ModelStatistics(String trainingFile) {
		initLearner();
		this.trainingFile = trainingFile;
	}

	public void initLearner() {
		learned = false;
		tagCount = new HashMap<String, Integer>();
		vocabulary = new HashSet<String>();
		emissionProb = new EmissionProbabilities(ALL_POS_TAGS);
		transitionProb = new TransitionProbabilties(ALL_POS_TAGS);
		Iterator<String> tagsIter = ALL_POS_TAGS.getIterator();
		while (tagsIter.hasNext())
			tagCount.put(tagsIter.next(), 0);
	}

	/**
	 * Compute the training statistics necessary for the Verbeti alogrithm 1.
	 * Emission probabilities 2. Transition probabilities 3. Tag counts 4.
	 * Vocabulary
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
				// if when needed to reduce duplicated vocabulary. 
				// Rule is: Decapitalize if only first letter is capital, 
				if (reader.getCurrTokenIndex() == 0) {
					// TODO: Handle the first word of the sentence.
				}

				// Update the vocabulary
				vocabulary.add(word);

				// Update the tag count
				if (!ALL_POS_TAGS.has(tag))
					throw new NoSuchFieldException(tag + " not found in list of POS tags");

				// If this is first token, update count of <s> first
				if (prevTag == "<s>")
					tagCount.put(prevTag, tagCount.get(prevTag) + 1);

				tagCount.put(tag, tagCount.get(tag) + 1);

				// Update the emission probability
				emissionProb.addWordAndTagCount(tag, word);

				// Update the transition probability
				transitionProb.addPrevTagAndTagCount(prevTag, tag);

				// If this is the last token, update the additional transition
				// probability P(</s>|tT)
				if (reader.isLastToken())
					transitionProb.addPrevTagAndTagCount(tag, "</s>");
				prevTag = tag;
			}
		}

		emissionProb.computeEmissionProbabilities(tagCount);
		transitionProb.computeTransitionProbabilities(tagCount);
		learned = true;
	}

	/**
	 * Get log(P(w|t)) for a word w and tag t.
	 * 
	 * @param tag
	 *            The query POS tag
	 * @param word
	 *            The query word
	 * @return log(P(w|t)) if word exists in vocabulary and C(w,t) > 0, log(0) =
	 *         MIN_VALUE if word exists in vocabulary but C(w,t) =0, log(P(w|t))
	 *         using unknown word model if word does not exist in vocabulary
	 */
	public double getWordGivenTag(String tag, String word) {
		// TODO: Handle C(w|t) = 0
		return emissionProb.getWordGivenTag(tag, word, tagCount, vocabulary);
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
		// TODO: Handle C(t|t-1) = 0
		return transitionProb.getTagGivenPrevTag(prevTag, tag);
	}

	public boolean isLearned() {
		return learned;
	}

}
