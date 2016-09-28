package util;

public class TaggedSetReader extends SetReader {

	public TaggedSetReader(String fileName) {
		super(fileName);
	}

	/**
	 * Get the word and POS tag of the current token as separate strings
	 * 
	 * @returns 1st element is the word contained in the token, 2nd element is
	 *          the POS tag of the word in the token
	 */
	public String[] getCurrTokenSplitWordTag() {
		String token = getCurrToken();
		// Find the last occurrence of "/" in the token. That is where the token
		// is split into word and tag
		int delimiterIndex = token.lastIndexOf("/");
		// Obtain the word and tag part of the token
		String word = token.substring(0, delimiterIndex);
		String tag = token.substring(delimiterIndex + 1);
		return new String[] { word, tag };
	}

}
