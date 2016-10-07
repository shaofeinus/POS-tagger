

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	private static final String POS_TAGS_FILE = "penn_tree_tags.data";
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
//		loadStates();
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

	private void loadStates() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(POS_TAGS_FILE));
			String line;
			assert POSSIBLE_POS_TAGS != null;
			while ((line = br.readLine()) != null)
				POSSIBLE_POS_TAGS.add(line.trim());
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("\"" + POS_TAGS_FILE + "\" not found! Please include file in running path.");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
