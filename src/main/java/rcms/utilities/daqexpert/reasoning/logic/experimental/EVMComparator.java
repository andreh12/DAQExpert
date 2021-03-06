package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class EVMComparator extends ComparatorLogicModule {

	public EVMComparator() {
		this.name = "n/a";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "New EVM state identified";
	}

	private static Logger logger = Logger.getLogger(EVMComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;
		RU currentEVM = null;
		RU previousEVM = null;

		for (FEDBuilder a : current.getFedBuilders()) {
			RU ru = a.getRu();
			if (ru.isEVM())
				currentEVM = ru;
		}

		for (FEDBuilder a : previous.getFedBuilders()) {
			RU ru = a.getRu();
			if (ru.isEVM())
				previousEVM = ru;
		}
		if (currentEVM == null || previousEVM == null) {
			logger.debug("EVM not found for shapshot " + new Date(current.getLastUpdate()));
			return false;
		}

		if (!currentEVM.getStateName().equals(previousEVM.getStateName())) {
			logger.debug("EVM state " + currentEVM.getStateName());
			this.name = "EVM state: " + currentEVM.getStateName();
			result = true;
		}
		return result;
	}

}
