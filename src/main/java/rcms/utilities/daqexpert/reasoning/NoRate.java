package rcms.utilities.daqexpert.reasoning;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.Level;

public class NoRate implements Condition {

	@Override
	public Boolean satisfied(DAQ daq) {
		float rate = daq.getFedBuilderSummary().getRate();
		if (rate == 0)
			return true;
		return false;
	}

	@Override
	public Level getLevel() {
		return Level.Info;
	}

	@Override
	public String getText() {
		return "No rate";
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {
		// nothing to do
	}

	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}
}
