

/**
 * This class transforms a set of tagged sentences into untagged sentences by
 * removing the tags. Used for tuning of parameters when given a tagged
 * development set.
 * 
 * @author Shao Fei
 *
 */
public class Untagger {

	private String taggedFileName;
	private String untaggedFileName;

	/**
	 * @param taggedFileName
	 *            Input tagged file name
	 * @param untaggedFileName
	 *            Output untagged file name
	 */
	public Untagger(String taggedFileName, String untaggedFileName) {
		this.taggedFileName = taggedFileName;
		this.untaggedFileName = untaggedFileName;
	}

	/**
	 * Untags tagged file of taggedFileName and save untagged set to
	 * untaggedFileName
	 */
	public void untag() {
		TaggedSetReader reader = new TaggedSetReader(taggedFileName);
		SetWriter writer = new SetWriter(untaggedFileName);
		while (reader.nextLine()) {
			while (reader.nextToken()) {
				String word = reader.getCurrTokenSplitWordTag()[0];
				writer.writeTokenToEndOfLine(word);
			}
			writer.writeLine();
		}
		writer.close();
	}
}
