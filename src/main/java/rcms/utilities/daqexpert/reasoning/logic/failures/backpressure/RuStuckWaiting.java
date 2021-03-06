package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RuStuckWaiting extends BackpressureAnalyzer {

	public RuStuckWaiting() {
		this.name = "RU waiting for other FED";

		this.description = "RU {{AFFECTED-RU}} is stuck waiting for FED {{PROBLEM-FED}}. "
				+ "FED(s) belong(s) to partition {{PROBLEM-PARTITION}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "Minimum fragment count in FED-builder {{PROBLEM-FED-BUILDER}} is {{MIN-FRAGMENT-COUNT}} in {{MIN-FRAGMENT-PARTITION}} partition "
				+ "and maximum is {{MAX-FRAGMENT-COUNT}} in {{MAX-FRAGMENT-PARTITION}} partition. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in the same FED-builder {{PROBLEM-FED-BUILDER}}. "
				+ "Note that there is nothing wrong with backpressured FED {{AFFECTED-FED}}.";
		this.briefDescription = "RU {{AFFECTED-RU}} is stuck waiting for FED {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}}";

		this.action = null;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.WaitingForOtherFedsInFB) {
			result = true;
		}
		return result;
	}

}
