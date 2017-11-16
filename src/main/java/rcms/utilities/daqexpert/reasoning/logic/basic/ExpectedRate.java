package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class ExpectedRate extends SimpleLogicModule {

	public ExpectedRate() {
		this.name = "Expected rate";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "Expecting rate";
	}


	/**
	 * Transition time in ms
	 */
	private final int transitionTime = 10000;
	private int duration;
	private long started;

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean expectedRate = false;

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		boolean fixingSoftError = daq.getLevelZeroState().equalsIgnoreCase("FixingSoftError") ? true : false;
		boolean dcsPauseResume = daq.getLevelZeroState().equalsIgnoreCase("PerformingDCSPauseResume") ? true : false;
		boolean pausing = daq.getLevelZeroState().equalsIgnoreCase("Pausing") ? true : false;
		boolean paused = daq.getLevelZeroState().equalsIgnoreCase("Paused") ? true : false;
		boolean resuming = daq.getLevelZeroState().equalsIgnoreCase("Resuming") ? true : false;
		boolean ttcHardResettingFromRunning = daq.getLevelZeroState().equalsIgnoreCase("TTCHardResettingFromRunning")
				? true : false;
		boolean ttcResyncingFromRunning = daq.getLevelZeroState().equalsIgnoreCase("TTCResyncingFromRunning") ? true
				: false;

		boolean ttcHardResetting = daq.getLevelZeroState().equalsIgnoreCase("TTCHardResetting") ? true : false;
		boolean ttcResyncing = daq.getLevelZeroState().equalsIgnoreCase("TTCResyncing") ? true : false;

		if (runOngoing && !fixingSoftError && !dcsPauseResume && !pausing && !paused && !resuming
				&& !ttcHardResettingFromRunning && !ttcResyncingFromRunning && !ttcHardResetting && !ttcResyncing)
			expectedRate = true;

		// first check
		if (started == 0) {
			started = daq.getLastUpdate();
		} else {
			duration = (int) (daq.getLastUpdate() - started);
		}

		if (expectedRate) {
			if (duration < transitionTime)
				// transition time
				return true;
			else {
				// transition time passed but run is still ongoing
				return false;
			}
		} else {
			// run is not ongoing, reset the checker
			started = 0;
			duration = 0;
			return false;
		}

	}
}
