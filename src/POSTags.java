
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class stores the list of valid POS tags.
 * 
 * @author Shao Fei
 *
 */
public class POSTags {

	private static final String[] POS_TAGS = {
			"CC",
			"CD",
			"DT",
			"EX",
			"FW",
			"IN",
			"JJ",
			"JJR",
			"JJS",
			"LS",
			"MD",
			"NN",
			"NNS",
			"NNP",
			"NNPS",
			"PDT",
			"POS",
			"PRP",
			"PRP$",
			"RB",
			"RBR",
			"RBS",
			"RP",
			"SYM",
			"TO",
			"UH",
			"VB",
			"VBD",
			"VBG",
			"VBN",
			"VBP",
			"VBZ",
			"WDT",
			"WP",
			"WP$",
			"WRB",
			"<s>",
			"</s>",
			"$",
			"#",
			"``",
			"''",
			"-LRB-",
			"-RRB-",
			",",
			".",
			":"
	};
	private Set<String> POSSIBLE_POS_TAGS;

	public POSTags() {
		POSSIBLE_POS_TAGS = new HashSet<String>(Arrays.asList(POS_TAGS));
	}

	public boolean has(String state) {
		return POSSIBLE_POS_TAGS.contains(state);
	}

	public Iterator<String> getIterator() {
		return POSSIBLE_POS_TAGS.iterator();
	}

	public int size() {
		return POSSIBLE_POS_TAGS.size();
	}

}
