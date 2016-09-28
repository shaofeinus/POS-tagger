package app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;

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
//			StringBuilder sb = new StringBuilder();
//			String line;
//			while((line = br.readLine()) != null)
//				sb.append(line);
			Learner learner = gson.fromJson(br, Learner.class);
			Tagger tag = new Tagger(learner, untaggedFileName, taggedFileName);
			tag.tag();

		} catch (FileNotFoundException e) {
			System.out.println(modelFileName + " not found!");
		}
	}

}
