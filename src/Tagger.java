
import java.util.ArrayList;
import java.util.Map;

/**
 * This class tags a untagged set of sentences using Model and Viberti.
 * 
 * @author Shao Fei
 *
 */
public class Tagger {

	private Model modelStats;
	private String taggingFile;
	private String taggedFile;

	/**
	 * @param learner
	 *            The trained Learner
	 * @param taggingFile
	 *            The file name of the input file to be tagged
	 * @param taggedFile
	 *            The file name of the output tagged file
	 */
	public Tagger(Model learner, String taggingFile, String taggedFile) {
		assert learner.isTrained();
		this.modelStats = learner;
		this.taggingFile = taggingFile;
		this.taggedFile = taggedFile;
	}

	/**
	 * Tags an untagged corpus in taggingFile using the training statistics in
	 * the Learner class
	 */
	public void tag() throws IllegalStateException {
		if (!modelStats.isTrained())
			throw new IllegalStateException("Model is not trained!");

		SetReader reader = new SetReader(taggingFile);
		TaggedSetWriter writer = new TaggedSetWriter(taggedFile);

		while (reader.nextLine()) {
			// Run Viterbi algorithm using the trained statistics in the Learner
			// class
			Viterbi vit = new Viterbi(reader, modelStats);
			vit.runViterbi();

			Map<String, ArrayList<String>> backPointer = vit.getBackPointer();
			String lastTag = vit.getLastTag();
			writeTaggedLineToFile(reader, writer, backPointer, lastTag);
		}
		writer.close();
		reader.close();
	}

	/**
	 * Write to the file (specified in writer) a tagged line using backPointer
	 * computed from the Viterbi algorithm.
	 * 
	 * @param reader
	 *            Reader that is reading the untagged current line
	 * @param writer
	 *            Writer that writes to a file
	 * @param backPointer
	 *            Computed from the Viterbi algorithm
	 * @param lastTag
	 *            Best tag for the last word computed from the Viterbi algorithm
	 * @param lastWord
	 *            Last word in the line
	 */
	private void writeTaggedLineToFile(SetReader reader, TaggedSetWriter writer, Map<String, ArrayList<String>> backPointer,
			String lastTag) {

		// Write the tag for the last word with the best last tag
		reader.goEndOfLine();
		reader.previousToken();
		String lastWord = reader.getCurrToken();
		writer.writeWord(lastWord);
		writer.writeTag(lastTag);
		writer.writeTokenToEndOfLine();

		// Process the rest of the words in the line from the last to the first
		String prevTag = backPointer.get(lastTag).get(reader.getNumTokensInCurrLine() - 1);
		int prevWordIndex = reader.getNumTokensInCurrLine() - 2;
		while (!prevTag.equals("<s>") && reader.previousToken() && prevWordIndex >= 0) {
			String prevWord = reader.getCurrToken();
			writer.writeWord(prevWord);
			writer.writeTag(prevTag);
			writer.writeTokenToStartOfLine();
			prevTag = backPointer.get(prevTag).get(prevWordIndex);
			prevWordIndex--;
		}
		writer.writeLine();
	}

}
