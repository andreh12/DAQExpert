package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.states.LHCBeamMode;

public class NoRateWhenExpected extends Condition {

	public NoRateWhenExpected() {
		this.name = "No rate when expected";
		this.group = EventGroup.Error;
		this.priority = EventPriority.critical;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		boolean stableBeams = false;
		boolean runOngoing = false;
		boolean noRate = false;
		if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(daq.getLhcBeamMode()))
			stableBeams = true;
		runOngoing = results.get(RunOngoing.class.getSimpleName());
		noRate = results.get(NoRate.class.getSimpleName());

		if (stableBeams && runOngoing && noRate)
			return true;
		return false;
	}

}