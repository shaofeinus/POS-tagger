
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Settings used for the tuning process.
 * 
 * @author Shao Fei
 *
 */
public class TuningSettings {

	private static final String CONSTANTS_FILE_NAME = "tune_settings.data";

	// Default values, if reading from tune_settings.data failed
	public double[] D_EMISSION_PROB_RANGE = { 0, 0.2 };
	public double[] D_TRANSITION_PROB_RANGE = { 0, 0.4 };
	public double[] LAMBDA_1_EMISSION_PROB_RANGE = { 0.85, 1 };
	public double[] LAMBDA_1_TRANSITION_PROB_RANGE = { 0.6, 1 };
	public double[] N_EMISSION_PROB_RANGE = { 0, 0.6 };
	public double[] N_TRANSITION_PROB_RANGE = { 0, 0.3 };
	public int NUM_TRIALS = 3;

	public TuningSettings() {
		// loadConstants();
	}

	private void loadConstants() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(CONSTANTS_FILE_NAME));
			String line;
			while ((line = br.readLine()) != null) {
				String paramName = line.split("=")[0].trim();
				String value = line.split("=")[1].trim();
				if (paramName.equals("D_EMISSION_PROB_RANGE"))
					D_EMISSION_PROB_RANGE = new double[] { Double.valueOf(value.split(",")[0].trim()),
							Double.valueOf(value.split(",")[1].trim()) };
				if (paramName.equals("D_TRANSITION_PROB_RANGE"))
					D_TRANSITION_PROB_RANGE = new double[] { Double.valueOf(value.split(",")[0].trim()),
							Double.valueOf(value.split(",")[1].trim()) };
				if (paramName.equals("LAMBDA_1_EMISSION_PROB_RANGE"))
					LAMBDA_1_EMISSION_PROB_RANGE = new double[] { Double.valueOf(value.split(",")[0].trim()),
							Double.valueOf(value.split(",")[1].trim()) };
				if (paramName.equals("LAMBDA_1_TRANSITION_PROB_RANGE"))
					LAMBDA_1_TRANSITION_PROB_RANGE = new double[] { Double.valueOf(value.split(",")[0].trim()),
							Double.valueOf(value.split(",")[1].trim()) };
				if (paramName.equals("N_EMISSION_PROB_RANGE"))
					N_EMISSION_PROB_RANGE = new double[] { Double.valueOf(value.split(",")[0].trim()),
							Double.valueOf(value.split(",")[1].trim()) };
				if (paramName.equals("N_TRANSITION_PROB_RANGE"))
					N_TRANSITION_PROB_RANGE = new double[] { Double.valueOf(value.split(",")[0].trim()),
							Double.valueOf(value.split(",")[1].trim()) };
				if (paramName.endsWith("NUM_TRIALS"))
					NUM_TRIALS = Integer.valueOf(value);

			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("\"" + CONSTANTS_FILE_NAME + "\" not found! Please include file in running path.");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
