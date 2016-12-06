package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class SubsystemRunningDegraded extends ActionLogicModule {

	public SubsystemRunningDegraded() {
		this.name = "Subsystem running degraded";
		this.description = "{{SUBSYSTEM}} subsystem is in running degraded";
		this.action = new SimpleAction("");
		this.group = EventGroup.SUBSYS_DEGRADED;
		this.priority = EventPriority.DEFAULTT;
		this.setNotificationPlay(true);
		this.setNotificationDisplay(true);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		if (!runOngoing)
			return false;
		
		boolean transition = results.get(LongTransition.class.getSimpleName());
		if (transition)
			return false;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {
			if ("RunningDegraded".equalsIgnoreCase(subSystem.getStatus())) {
				context.register("SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
