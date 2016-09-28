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
public class Suffixes {
	
	private static final String LOAD_FILE = "suffixes.data";
	private Set<String> POSSIBLE_SUFFIXES;

	public Suffixes() {
		POSSIBLE_SUFFIXES = new HashSet<String>();
		loadSuffixes();
	}

	public boolean has(String state) {
		return POSSIBLE_SUFFIXES.contains(state);
	}
	
	public Iterator<String> getIterator() {
		return POSSIBLE_SUFFIXES.iterator();
	}

	private void loadSuffixes() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(LOAD_FILE));
			String line;
			assert POSSIBLE_SUFFIXES != null;
			while ((line = br.readLine()) != null)
				POSSIBLE_SUFFIXES.add(line.trim());
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println(LOAD_FILE + " not found!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
