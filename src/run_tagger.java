

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class run_tagger {

	public static void main(String[] args) {
		
		if(args.length != 3) {
			System.out.println("Invalid arguments to program");
			System.exit(-1);
		}

		String untaggedFileName = args[0];
		String modelFileName = args[1];
		String taggedFileName = args[2];
		
		try {
			
			// Obtain Learner from saved file
			System.out.println("Reading model statistics from \"" + modelFileName + "\"...");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFileName));
			Model modelStats = (ModelFinal) in.readObject();
			in.close();
			
			// Start tagging
			System.out.println("Tagging \"" + untaggedFileName + "\"...");
			Tagger tag = new Tagger(modelStats, untaggedFileName, taggedFileName);
			tag.tag();
			System.out.println("All lines successfully tagged! Tagged file: \"" + taggedFileName + "\"");
			
		} catch (FileNotFoundException e) {
			System.out.println(modelFileName + " not found!");
		} catch (IOException e) {
			System.out.println("I/O Exception when writing \"" + taggedFileName + "\"");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			System.out.println("Model is not trained!");
		}
	}

}
