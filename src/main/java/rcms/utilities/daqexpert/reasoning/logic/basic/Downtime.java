package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies avoidable downtime condition in DAQ
 */
public class Downtime extends SimpleLogicModule {

	public Downtime() {
		this.name = "Downtime";
		this.priority = ConditionPriority.WARNING;
		this.description = "No rate during stable beams";
	}

	/**
	 * Avoidable downtime when downtime and no action being executed
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean noRate = results.get(NoRate.class.getSimpleName());
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? ConditionPriority.WARNING : ConditionPriority.DEFAULTT;

		if (stableBeams && noRate)
			return true;
		else
			return false;
	}

}
