package util;

public class TaggedSetWriter extends SetWriter {

	private String currWord;
	private String currTag;

	public TaggedSetWriter(String fileName) {
		super(fileName);
		currWord = null;
		currTag = null;
	}

	/**
	 * Write the current line into the tagged file. Reset the current line of
	 * tokens, currWord and currTag to be empty to be written for the next line.
	 * 
	 */
	public void writeLine() {
		super.writeLine();	
		currWord = null;
		currTag = null;
	}

	/**
	 * Append the current token into the end of the current line. Reset the
	 * currWord and currTag to be empty to be written for the next line
	 */
	public void writeTokenToEndOfLine() {
		if (currWord == null || currWord.trim().equals("") || currTag == null || currTag.trim().equals("")) {
			System.out.println("Token cannot be written as tag or word is empty");
			return;
		}
		super.writeTokenToEndOfLine(currWord + "/" + currTag);
		currWord = null;
		currTag = null;
	}

	/**
	 * Insert the current token into the start of the current line. Reset the
	 * currWord and currTag to be empty to be written for the next line
	 */
	public void writeTokenToStartOfLine() {
		if (currWord == null || currWord.trim().equals("") || currTag == null || currTag.trim().equals("")) {
			System.out.println("Token cannot be written as tag or word is empty");
			return;
		}
		super.writeTokenToStartOfLine(currWord + "/" + currTag);
		currWord = null;
		currTag = null;
	}

	/**
	 * Update the word in the current token
	 * 
	 * @param word
	 *            Updated word
	 */
	public void writeWord(String word) {
		currWord = word;
	}

	/**
	 * Update the tag in the current token
	 * 
	 * @param tag
	 *            Updated tag
	 */
	public void writeTag(String tag) {
		currTag = tag;
	}

}
