package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class Transition extends ActionLogicModule {

	public Transition() {
		this.name = "Transition";
		this.group = EventGroup.TRANSITION;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Transition for new run";
		this.action = null;
	}

	int duration;
	long started;

	/**
	 * No rate when sum of FedBuilders rate equals 0 Hz
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		// first check
		if (started == 0) {
			started = daq.getLastUpdate();
		} else {
			duration = (int) (daq.getLastUpdate() - started);
		}

		if (runOngoing) {
			if (duration < 5000)
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