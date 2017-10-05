package rcms.utilities.daqexpert.reasoning.base;

import org.apache.log4j.Logger;

public class HoldOffPostProcessor implements ConditionPostProcessor {

	private final Long holdOffPeriod;

	/**
	 * When the condition started to be satisfied - base to calculate holdoff
	 */
	private Long start;

	private Long holdOffRelease;

	private static final Logger logger = Logger.getLogger(HoldOffPostProcessor.class);

	public HoldOffPostProcessor(Long holdOffPeriod) {
		this.holdOffPeriod = holdOffPeriod;
	}

	@Override
	public boolean satisfied(boolean conditionSatisfied, long now) {

		if (conditionSatisfied) {
			if (start == null) {
				start = now;
				holdOffRelease = start + holdOffPeriod;
				logger.debug("Condition satisfied, holding the result for " + holdOffPeriod + "ms, release time: " + holdOffRelease);
			}

			if (now >= holdOffRelease) {
				logger.debug("Releaseing condition from " + holdOffPeriod + "ms hold off period");
				return true;
			}
		} else {
			start = null;
		}

		return false;

	}

}
