package rcms.utilities.daqexpert.events;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class ConditionEvent {

	private final static Logger logger = Logger.getLogger(ConditionEvent.class);

	private Condition condition;

	private EventType type;

	private Date date;

	private String title;

	private ConditionPriority priority;

	private LogicModuleRegistry logicModule;

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Event [condition=" + condition + ", type=" + type + ", date=" + date + "]";
	}

	public ConditionEventResource generateEventToSend() {
		ConditionEventResource eventToSend = new ConditionEventResource();

		eventToSend.setMessage(condition.getDescription());
		if (eventToSend.getMessage() == null) {
			logger.warn("Message empty for event: " + title + ", replacing with empty string");
			eventToSend.setMessage("");
		}
		eventToSend.setTitle(title);
		eventToSend.setConditionId(condition.getId());
		eventToSend.setEventType(type);
		eventToSend.setSender(this.getClass().getPackage().getImplementationVersion());
		eventToSend.setEventSenderType(EventSenderType.Expert);
		eventToSend.setDate(date);
		eventToSend.setLogicModule(logicModule);
		eventToSend.setPriority(priority);

		return eventToSend;

	}

	public LogicModuleRegistry getLogicModule() {
		return logicModule;
	}

	public void setLogicModule(LogicModuleRegistry logicModule) {
		this.logicModule = logicModule;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ConditionPriority getPriority() {
		return priority;
	}

	public void setPriority(ConditionPriority priority) {
		this.priority = priority;
	}
}
