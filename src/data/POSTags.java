package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class stores the list of valid states in the HMM. The states are loaded
 * from an external data file.
 * 
 * @author Shao Fei
 *
 */
public class POSTags {

	private static final String LOAD_FILE = "penn_tree_tags.data";
	private Set<String> POSSIBLE_POS_TAGS;

	public POSTags() {
		POSSIBLE_POS_TAGS = new HashSet<String>();
		loadStates();
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
			BufferedReader br = new BufferedReader(new FileReader(LOAD_FILE));
			String line;
			assert POSSIBLE_POS_TAGS != null;
			while ((line = br.readLine()) != null)
				POSSIBLE_POS_TAGS.add(line.trim());
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println(LOAD_FILE + " not found!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
