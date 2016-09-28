package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class SetReader {

	private String[] currLineTokens;
	private String currLine;
	private int currTokenIndex;
	private int currLineIndex;
	private BufferedReader br;

	public SetReader(String fileName) {
		currLineIndex = -1;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.out.println(fileName + " not found!");
		}
	}

	/**
	 * Process the next line in the set, updates currLineTokens to the tokens of
	 * the next line and resets currTokenIndex to -1. This means that nexToken()
	 * must be called after nextLine() to be able to access the first token in
	 * the new line
	 * 
	 * @return true a next line exists, false if no more lines
	 */
	public boolean nextLine() {
		currLineIndex++;
		try {
			String line = br.readLine();
			if (line == null)
				return false;
			currLineTokens = line.split(" ");
			currLine = line;
			currTokenIndex = -1;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * Process the next token in the currLineTokens, updates the currTokenIndex
	 * 
	 * @return true a next token exists in the current line, false if next token
	 *         is end of line
	 */
	public boolean nextToken() {
		if (currTokenIndex + 1 == currLineTokens.length)
			return false;
		else {
			currTokenIndex++;
			return true;
		}
	}

	/**
	 * 
	 * Process the previous token in the currLineTokens, updates the
	 * currTokenIndex
	 * 
	 * @return true a previous token exists in the current line, false if
	 *         previous token is start of line
	 */
	public boolean previousToken() {
		if (currTokenIndex - 1 == -1)
			return false;
		else {
			currTokenIndex--;
			return true;
		}
	}

	/**
	 * Rewind currTokenIndex to start of the line so that when nextToken() is
	 * called, currTokenIndex points to the first token in the line.
	 */
	public void goToStartOfLine() {
		currTokenIndex = -1;
	}
	
	/**
	 * Rewind currTokenIndex to end of the line so that when previousToken() is
	 * called, currTokenIndex points to the last token in the line.
	 */
	public void goEndOfLine() {
		currTokenIndex = currLineTokens.length;
	}

	/**
	 * Get the current token without affecting current token or line being
	 * processed
	 * 
	 * @return the current token
	 */
	public String getCurrToken() {
		return currLineTokens[currTokenIndex].trim();
	}

	/**
	 * Get the current line without affecting current token or line being
	 * processed
	 * 
	 * @return the current line
	 */
	public String getCurrLine() {
		return currLine;
	}

	public int currLineLength() {
		return currLine.length();
	}
	
	public int getCurrTokenIndex() {
		return currTokenIndex;
	}
	
	public int getCurrLineIndex() {
		return currLineIndex;
	}
	
	public int getNumTokensInCurrLine() {
		return currLineTokens.length;
	}
	

	public boolean isLastToken() {
		return currTokenIndex + 1 == currLineTokens.length;
	}

	public boolean isFirstToken() {
		return currTokenIndex == 0;
	}

	public void close() {
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
