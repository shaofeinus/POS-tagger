package util;

public class HMMStatisticsCompute {

	/**
	 * Calculate the statistic of as',s * bs(o) in log form
	 * 
	 * @param transitionProb
	 *            log(as',s)
	 * @param emissionProb
	 *            log(bs(t))
	 * @return log(as',s * bs(o))
	 */
	public static double calculateFirstStats(double transitionProb, double emissionProb) {
		if (transitionProb == Constants.MIN_VALUE || emissionProb == Constants.MIN_VALUE)
			// TODO: change
			return Constants.VERY_SMALL_VALUE;
		else
			// log(as',s * bs(o)) = log(as',s) + log(bs(t))
			return transitionProb + emissionProb;
	}

	/**
	 * Calculate the statistic of viterbi(s',t-1) * as',s * bs(t)in log form
	 * 
	 * @param transitionProb
	 *            log(as',s)
	 * @param emissionProb
	 *            log(bs(t))
	 * @param prevStateStat
	 *            log(viterbi(s',t-1))
	 * @return log(viterbi(s',t-1) * as',s * bs(t))
	 */
	public static double calculateStats(double transitionProb, double emissionProb, double prevStateStat) {
		if (transitionProb == Constants.MIN_VALUE || emissionProb == Constants.MIN_VALUE
				|| prevStateStat == Constants.MIN_VALUE)
			// TODO: change
			return Constants.VERY_SMALL_VALUE;
		else {
//			System.out.println(transitionProb + " " + emissionProb);
			// log(viterbi(s',t-1) * as',s * bs(t)) = log(prevStateStat) +
			// log(as',s) +
			// log(bs(t))
//			if (prevStateStat + transitionProb + emissionProb < -50) 
//				System.out.println(prevStateStat + transitionProb + emissionProb);
			return prevStateStat + transitionProb + emissionProb;
		}
	}

	/**
	 * Calculate the statistic of viterbi(s',t-1) * as',s in log form
	 * 
	 * @param transitionProb
	 *            log(as',s)
	 * @param prevStateStat
	 *            log(viterbi(s',t-1))
	 * @return log(viterbi(s',t-1) * as',s)
	 */
	public static double calculateLastStats(double transitionProb, double prevStateStat) {
		if (transitionProb == Constants.MIN_VALUE || prevStateStat == Constants.MIN_VALUE)
			// TODO: change
			return Constants.VERY_SMALL_VALUE;
		else
			// log(viterbi(s',t-1) * as',s) = log(viterbi(s',t-1)) + log(as',s)
			return prevStateStat + transitionProb;
	}

}
