
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class stores the list of suffixes used in the unknown word model.
 * 
 * @author Shao Fei
 *
 */
public class Suffixes {

	private static final String[] SUFFIXES = {
			"able",
			"al",
			"an",
			"ance",
			"ancy",
			"ant",
			"ar",
			"ary",
			"ate",
			"ed",
			"ee",
			"en",
			"ence",
			"ency",
			"ent",
			"er",
			"es",
			"est",
			"fication",
			"ful",
			"fy",
			"ian",
			"ible",
			"ic",
			"ing",
			"ion",
			"ish",
			"ism",
			"ist",
			"ity",
			"ive",
			"ize",
			"less",
			"logy",
			"ly",
			"ment",
			"ness",
			"or",
			"ous",
			"s",
			"ship",
			"sion",
			"tion",
			"y"
	};
	
	private Set<String> POSSIBLE_SUFFIXES;

	public Suffixes() {
		POSSIBLE_SUFFIXES = new HashSet<String>(Arrays.asList(SUFFIXES));
	}

	public boolean has(String suffix) {
		return POSSIBLE_SUFFIXES.contains(suffix);
	}

	/**
	 * Identifies a list of suffixes in a word.
	 * 
	 * @param word
	 * @return The list of suffixes in the word
	 */
	public String[] getAllSuffixes(String word) {
		ArrayList<String> allMatchingSuffixes = new ArrayList<String>();
		Iterator<String> iter = POSSIBLE_SUFFIXES.iterator();
		while (iter.hasNext()) {
			String suffix = iter.next();
			if (word.length() - 3 >= suffix.length() // First check if word
													// is at least 3 letters longer than
													// suffix to avoid unnecessary
													// check
					&& suffix.equals(word.substring(word.length() - suffix.length())))			
				allMatchingSuffixes.add(suffix);
		}
		return allMatchingSuffixes.toArray(new String[allMatchingSuffixes.size()]);
	}

	public Iterator<String> getIterator() {
		return POSSIBLE_SUFFIXES.iterator();
	}
	
	public int size() {
		return POSSIBLE_SUFFIXES.size();
	}
	
}
