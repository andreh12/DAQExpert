package rcms.utilities.daqexpert.websocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;

/**
 * 
 * Logical Condition dashboard
 * 
 * ConditionWebSocketServer.sessionHandler.addCondition(condition);
 * ConditionWebSocketServer.sessionHandler.removeCurrent();
 * ConditionWebSocketServer.sessionHandler.updateCurrent(currentCondition);
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ConditionDashboard {

	private final static Logger logger = Logger.getLogger(ConditionDashboard.class);

	private Condition currentCondition;

	private HashMap<Long, Condition> recentConditions = new LinkedHashMap<>();

	private ConditionSessionHandler sessionHander;

	private final int max;

	public ConditionDashboard(int max) {
		this.max = max;
	}

	private void handleRemoveCurrent() {
		currentCondition = null;
		if (sessionHander != null) {
			sessionHander.removeCurrent();
			sessionHander.addRecent();
		}
	}

	private void handleSetCurrent(Condition condition) {
		currentCondition = condition;
		if (sessionHander != null) {
			sessionHander.setCurrent(condition);
		}
	}

	private void handleReplaceCurrent(Condition condition) {
		currentCondition = condition;

		if (sessionHander != null) {
			sessionHander.removeCurrent();
			sessionHander.setCurrent(condition);
		}
		// remove (fire add recent) + set current
	}

	private void handleRemoveRecent(Long id) {
		recentConditions.remove(id);

		if (sessionHander != null) {
			sessionHander.removeRecent(id);
		}
	}

	private void handleAddRecent(Condition condition) {
		recentConditions.put(condition.getId(), condition);
		if (sessionHander != null) {
			sessionHander.addRecent();
		}
	}

	public void update(Set<Condition> conditions) {

		if (currentCondition != null && currentCondition.getEnd() != null) {
			handleRemoveCurrent();
		}

		for (Condition condition : conditions) {
			if (condition.isShow() /*
									 * && condition.getPriority() ==
									 * ConditionPriority.CRITICAL
									 */
					&& condition.getLogicModule().getLogicModule() instanceof ContextLogicModule) {

				// exists some unfinished
				// TODO: add some threshold
				if (condition.getEnd() == null) {

					// no condition at the moment
					if (currentCondition == null) {
						handleSetCurrent(condition);
					}

					// exists other condition at the moemnt
					else {

						// current is more important than old
						if (condition.getPriority().ordinal() > currentCondition.getPriority().ordinal()) {
							handleReplaceCurrent(condition);
						}

						// current is less important than old
						else if (condition.getPriority().ordinal() < currentCondition.getPriority().ordinal()) {
							// nothing to do
						}
						// both are equally important
						else {
							// newest will be displayed
							if (condition.getStart().after(currentCondition.getStart())) {
								handleReplaceCurrent(condition);
							}
						}
					}
				}

				if (!recentConditions.containsKey(condition.getId())) {
					if (recentConditions.size() >= max) {
						Long oldest = recentConditions.values().iterator().next().getId();
						handleRemoveRecent(oldest);
					}

					handleAddRecent(condition);
				}
			}
		}

	}

	public Condition getCurrentCondition() {
		return currentCondition;
	}

	public void setCurrentCondition(Condition currentCondition) {
		this.currentCondition = currentCondition;
	}

	protected Collection<Condition> getCurrentConditions() {
		return recentConditions.values();
	}

	public Collection<Condition> getFilteredCurrentConditions() {
		List<Condition> result = new ArrayList<>();
		Iterator<Condition> i = recentConditions.values().iterator();

		while (i.hasNext()) {
			Condition curr = i.next();
			if (currentCondition == null) {
				result.add(curr);
			} else if (currentCondition.getId() != curr.getId()) {
				result.add(curr);
			}
		}
		return result;

	}

	public ConditionSessionHandler getSessionHander() {
		return sessionHander;
	}

	public void setSessionHander(ConditionSessionHandler sessionHander) {
		this.sessionHander = sessionHander;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("ConditionDashboard [currentCondition="
				+ (currentCondition != null ? currentCondition.getId() : "<none>") + ", recentConditions=[");
		logger.info("recent size: " + recentConditions.size());
		for (Condition condition : recentConditions.values()) {
			sb.append(condition.getId());
			sb.append(",");
		}
		logger.info("filtered size: " + getFilteredCurrentConditions().size());
		sb.append("], filteredConditions=[");
		for (Condition condition : getFilteredCurrentConditions()) {
			sb.append(condition.getId());
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

}
