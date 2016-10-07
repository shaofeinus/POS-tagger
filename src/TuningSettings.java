/**
 * Settings used for the tuning process.
 * 
 * @author Shao Fei
 *
 */
public class TuningSettings {

	// Default values, if reading from tune_settings.data failed
	public double[] D_EMISSION_PROB_RANGE = { 0, 0.2 };
	public double[] D_TRANSITION_PROB_RANGE = { 0, 0.4 };
	public double[] LAMBDA_1_EMISSION_PROB_RANGE = { 0.85, 1 };
	public double[] LAMBDA_1_TRANSITION_PROB_RANGE = { 0.6, 1 };
	public double[] N_EMISSION_PROB_RANGE = { 0, 0.6 };
	public double[] N_TRANSITION_PROB_RANGE = { 0, 0.3 };
	public int NUM_TRIALS = 3;

	public TuningSettings() {}

}
