

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class build_tagger {

	public static void main(String[] args) {
		
		if(args.length != 3) {
			System.out.println("Invalid arguments to program");
			System.exit(-1);
		}

		String trainingSetFileName = args[0];
		String developmentSetFileName = args[1];
		String modelFileName = args[2];

		try {
			// Learn from the training set and dev set
			Model modelStats = new ModelFinal(trainingSetFileName);
			Learner learn = new Learner(modelStats, developmentSetFileName, "final_model");
			learn.learnAndTune();

			// Save model statistics in model_file
			System.out.println("Writing model_file...");
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFileName));
			out.writeObject(modelStats);
			out.close();

		} catch (NoSuchFieldException e) {
			System.out.println("POS tag found in training set is not a recognized tag. "
					+ "Add the new POS tag to \"penn_tree_tags.data\" or check if the tag is valid.");
		} catch (IOException e) {
			System.out.println("I/O Exception when writing \"" + modelFileName + "\"");
		}
		
	}

}
