

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * This class writes a tokenised file.
 * 
 * @author Shao Fei
 *
 */
public class SetWriter {

	private LinkedList<String> tokens;
	private BufferedWriter bw;

	public SetWriter(String fileName) {
		tokens = new LinkedList<String>();
		try {
			bw = new BufferedWriter(new FileWriter(fileName, false));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the current line into the tagged file. Reset the line of tokens to
	 * be empty to be written for the next line.
	 * 
	 */
	public void writeLine() {
		StringBuilder sb = new StringBuilder();
		while (tokens.size() != 0)
			sb.append(tokens.removeFirst()).append(" ");
		tokens.clear();
		try {
			bw.write(sb.toString().trim() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Append token to end of the current line.
	 * 
	 * @param token
	 *            Token string to be added
	 */
	public void writeTokenToEndOfLine(String token) {
		assert !token.equals("") || token != null;
		tokens.add(token);
	}

	/**
	 * Append token to start of the current line.
	 * 
	 * @param token
	 *            Token string to be added
	 */
	public void writeTokenToStartOfLine(String token) {
		assert !token.equals("") || token != null;
		tokens.addFirst(token);
	}
	
	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
