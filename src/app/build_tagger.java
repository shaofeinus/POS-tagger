package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class build_tagger {

	public static void main(String[] args) {
		try {	
			// TODO: Take in file names from args
			String trainingFileName = "sents.train";
			String modelFileName = "model_file";
			
			// Train using Learner
			Learner learner = new Learner(trainingFileName);
			learner.learn();
			
			// Save Learner in model_file
			Gson gson =  new GsonBuilder().setPrettyPrinting().create();
			String learnerJson = gson.toJson(learner);
			BufferedWriter bf = new BufferedWriter(new FileWriter(modelFileName, false));
			bf.write(learnerJson);
			bf.close();
			
		} catch (NoSuchFieldException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}