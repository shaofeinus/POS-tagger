/**
 * This program tests the accuracy of the various smoothing methods by creating
 * a Learner class for each Model that implements a smoothing method in a
 * separate thread and calling the learnAndTune() function of Learner. Threads
 * are used to take advantage of the multi-core processors of the machine if
 * they exist.
 * 
 * The results of the tests are written to tuning_results_[smoothing method
 * name].csv.
 * 
 * @author Shao Fei
 *
 */
public class run_smoothing_tests {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println(
					"Invalid arguments to program. Correct format: java run_smoothing_tests [training set file name] [dev set file name]");
			System.exit(-1);
		}

		String trainingSetFileName = args[0];
		String developmentSetFileName = args[1];

		Model[] modelStats = { new ModelAddN(trainingSetFileName), new ModelInterpolate(trainingSetFileName),
				new ModelKneserNey(trainingSetFileName), new ModelWittenBell(trainingSetFileName),
				new ModelUnsmoothed(trainingSetFileName), new ModelFinal(trainingSetFileName) };

		Learner[] learners = { new Learner(modelStats[0], developmentSetFileName, "tuning_results", "addn"),
				new Learner(modelStats[1], developmentSetFileName, "tuning_results", "interpolate"),
				new Learner(modelStats[2], developmentSetFileName, "tuning_results", "KN"),
				new Learner(modelStats[3], developmentSetFileName, "tuning_results", "WB"),
				new Learner(modelStats[4], developmentSetFileName, "tuning_results", "unsmoothed"),
				new Learner(modelStats[5], developmentSetFileName, "tuning_results", "final") };

		Thread[] learnAndTuneTreads = { new Thread(new LearnAndTuneThread(learners[0])),
				new Thread(new LearnAndTuneThread(learners[1])), new Thread(new LearnAndTuneThread(learners[2])),
				new Thread(new LearnAndTuneThread(learners[5])) };

		Thread[] learnNoTuneTreads = { new Thread(new LearnNoTuneThread(learners[3])),
				new Thread(new LearnNoTuneThread(learners[4])) };


		for (int i = 0; i < learnAndTuneTreads.length; i++) {
			learnAndTuneTreads[i].start();
		}

		for (int i = 0; i < learnNoTuneTreads.length; i++) {
			learnNoTuneTreads[i].start();
		}

		for (int i = 0; i < learnAndTuneTreads.length; i++) {
			try {
				learnAndTuneTreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < learnNoTuneTreads.length; i++) {
			try {
				learnNoTuneTreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class LearnAndTuneThread implements Runnable {

	private Learner learner;

	public LearnAndTuneThread(Learner learner) {
		this.learner = learner;
	}

	@Override
	public void run() {
		try {
			learner.learnAndTune();
		} catch (NoSuchFieldException e) {
			System.out.println(e.getMessage());
		}
	}
}

class LearnNoTuneThread implements Runnable {

	private Learner learner;

	public LearnNoTuneThread(Learner learner) {
		this.learner = learner;
	}

	@Override
	public void run() {
		try {
			learner.learnNoTune();
		} catch (NoSuchFieldException e) {
			System.out.println(e.getMessage());
		}
	}

}