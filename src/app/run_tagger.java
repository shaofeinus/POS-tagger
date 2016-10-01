package app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;

import data.ModelStatistics;

public class run_tagger {

	public static void main(String[] args) {
		
		// TODO: get file name from args
		String modelFileName = "model_file";
		String untaggedFileName = "sents.test";
		String taggedFileName = "sents.out.new";
		
		// Obtain Learner from saved file
		Gson gson = new Gson();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(modelFileName));
			ModelStatistics learner = gson.fromJson(br, ModelStatistics.class);
//			Tagger tag = new Tagger(learner, untaggedFileName, taggedFileName);
//			tag.tag();
			
			Evaluator evaluator = new Evaluator(learner, "sents.devt");
			evaluator.evaluate();

		} catch (FileNotFoundException e) {
			System.out.println(modelFileName + " not found!");
		}
	}

}
