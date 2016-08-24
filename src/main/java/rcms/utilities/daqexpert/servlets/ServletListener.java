package rcms.utilities.daqexpert.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataResolutionManager;
import rcms.utilities.daqexpert.ExpertPersistorManager;
import rcms.utilities.daqexpert.ReadTaskController;

public class ServletListener implements ServletContextListener {

	public ServletListener() {
		super();
		String propertyFilePath = System.getenv("EXPERT_CONF");
		if (propertyFilePath == null) {
			logger.fatal(
					"No configuration file supplied. Set the path to configuration file in environment variable EXPERT_CONF");
			throw new RuntimeException("EXPERT_CONF variable is empty");
		}

		Application.initialize(propertyFilePath);

		String snapshotsDir = Application.get().getProp().getProperty(Application.SNAPSHOTS_DIR);

		if (snapshotsDir != null)
			logger.info("Loading snapshots from directory: " + snapshotsDir);
		else {
			logger.warn("Could not load snapshot directory from neither SNAPSHOTS env var nor config.properties file");
			return;
		}

		persistorManager = new ExpertPersistorManager(snapshotsDir);
	}

	private static final Logger logger = Logger.getLogger(ServletListener.class);

	ExpertPersistorManager persistorManager;
	DataResolutionManager dataSegmentator;


	public void contextInitialized(ServletContextEvent e) {
		
		ReadTaskController readerRaskController = new ReadTaskController();
		readerRaskController.firePastReaderTask();
		readerRaskController.fireRealTimeReaderTask();
	}

	public void contextDestroyed(ServletContextEvent e) {
	}

}