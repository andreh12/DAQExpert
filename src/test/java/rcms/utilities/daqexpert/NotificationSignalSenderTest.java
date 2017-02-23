package rcms.utilities.daqexpert;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.notifications.NotificationSignalConnector;
import rcms.utilities.daqexpert.notifications.NotificationSignalSender;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * Tests if notification signals are generated correctly
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@Ignore
public class NotificationSignalSenderTest {

	@Test
	public void notificationSignalStepsTest() throws ParseException {

		Date applicationStartDate = DatatypeConverter.parseDateTime("2016-12-31T23:59:58Z").getTime();
		Date eventDate = DatatypeConverter.parseDateTime("2016-12-31T23:59:59Z").getTime();

		NotificationSignalConnector connector = Mockito.mock(NotificationSignalConnector.class);
		NotificationSignalSender notificationSender = new NotificationSignalSender(connector, "", "",
				applicationStartDate.getTime());

		Condition entry = getTestEntry(eventDate);

		/* Verify the results - entry is new, no notification generated */
		notificationSender.send(entry);
		Mockito.verify(connector, Mockito.times(0)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* entry is mature here - start signal should be fired */
		notificationSender.send(entry);
		Mockito.verify(connector, Mockito.times(1)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* nothing changed - no signal */
		notificationSender.send(entry);
		Mockito.verify(connector, Mockito.times(1)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* entry finished - finish signal should be fired */
		entry.setState(EntryState.FINISHED);
		notificationSender.send(entry);
		Mockito.verify(connector, Mockito.times(2)).sendSignal(Mockito.anyString(), Mockito.anyString());

	}

	@Test
	public void oldNotificationsShouldBeOmmittedTest() throws ParseException {

		Date oldEntryDate = DatatypeConverter.parseDateTime("2016-12-31T23:59:57Z").getTime();
		Date applicationStartDate = DatatypeConverter.parseDateTime("2016-12-31T23:59:58Z").getTime();
		Date newEntryDate = DatatypeConverter.parseDateTime("2016-12-31T23:59:59Z").getTime();

		NotificationSignalConnector connector = Mockito.mock(NotificationSignalConnector.class);
		NotificationSignalSender notificationSender = new NotificationSignalSender(connector, "", "",
				applicationStartDate.getTime());

		Condition oldEntry = getTestEntry(oldEntryDate);
		Condition newEntry = getTestEntry(newEntryDate);

		/*
		 * Verify the results - old entry is new, no notification generated
		 */
		notificationSender.send(oldEntry);
		Mockito.verify(connector, Mockito.times(0)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/*
		 * entry is mature here - but its old entry so - start signal should NOT
		 * be fired
		 */
		notificationSender.send(oldEntry);
		Mockito.verify(connector, Mockito.times(0)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* nothing changed - no signal */
		notificationSender.send(oldEntry);
		Mockito.verify(connector, Mockito.times(0)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/*
		 * entry finished - but its old entry so - finish signal should be fired
		 */
		oldEntry.setState(EntryState.FINISHED);
		notificationSender.send(oldEntry);
		Mockito.verify(connector, Mockito.times(0)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/*
		 * Verify the results - new entry is new, no notification generated
		 */
		notificationSender.send(newEntry);
		Mockito.verify(connector, Mockito.times(0)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* entry is mature here - start signal should be fired */
		notificationSender.send(newEntry);
		Mockito.verify(connector, Mockito.times(1)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* nothing changed - no signal */
		notificationSender.send(newEntry);
		Mockito.verify(connector, Mockito.times(1)).sendSignal(Mockito.anyString(), Mockito.anyString());

		/* entry finished - finish signal should be fired */
		newEntry.setState(EntryState.FINISHED);
		notificationSender.send(newEntry);
		Mockito.verify(connector, Mockito.times(2)).sendSignal(Mockito.anyString(), Mockito.anyString());

	}

	private Condition getTestEntry(Date date) {
		/* Build a test entry */
		Condition entry = new Condition();
		entry.setStart(date);
		entry.setClassName(ConditionPriority.CRITICAL);

		ActionLogicModule logicModule = new ActionLogicModule() {
			@Override
			public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
				return true;
			}
		};
		logicModule.setDescription("test no rate");

		ActionLogicModule eventFinderSpy = Mockito.spy(logicModule);
		// TODO: this should not be commented out
		// entry.setLogicModule(eventFinderSpy);
		return entry;
	}
}
