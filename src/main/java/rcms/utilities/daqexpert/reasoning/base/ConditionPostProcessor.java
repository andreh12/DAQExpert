package rcms.utilities.daqexpert.reasoning.base;

/**
 * Interface for postprocessing result of the satisfied method.
 * Postprocessing decisions are mainly based on the condition
 * triggered by an underlying logic module and the current time.
 */
public interface ConditionPostProcessor {
	
	/** @return true if the condition is satisfied */
	public boolean satisfied(boolean conditionSatisfied, long now);
	
}
