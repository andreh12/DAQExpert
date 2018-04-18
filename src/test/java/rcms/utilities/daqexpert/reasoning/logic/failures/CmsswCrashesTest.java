package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.HltInfo;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class CmsswCrashesTest {


    CmsswCrashes module;
    Map<String, Output> results;
    Logger logger = Logger.getLogger(CmsswCrashes.class);

    @Before
    public void prepare() {
        Logger.getLogger(CmsswCrashes.class).setLevel(Level.INFO);
        results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), new Output(true));
        results.put(BackpressureFromHlt.class.getSimpleName(), new Output(true));
        module = new CmsswCrashes();

        // mock parameters
        Properties config = new Properties();
        config.put(Setting.EXPERT_CMSSW_CRASHES_THRESHOLD.getKey(), "20");
        config.put(Setting.EXPERT_CMSSW_CRASHES_TIME_WINDOW.getKey(), "20");
        module.parametrize(config);
    }

    @Test
    public void simpleThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 0), results));
        Assert.assertTrue(module.satisfied(mockTestObject(1, 21), results));

    }

    @Test
    public void lastMomentInTimeWindowThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(1, 0), results));
        Assert.assertFalse(module.satisfied(mockTestObject(19, 19), results));
        Assert.assertTrue(module.satisfied(mockTestObject(20, 21), results));

    }

    @Test
    public void slidingWindowThresholdNonOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10), results));
        Assert.assertFalse(module.satisfied(mockTestObject(20, 20), results));
        Assert.assertFalse(module.satisfied(mockTestObject(40, 30), results));

    }

    @Test
    public void slidingWindowThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10), results));
        Assert.assertFalse(module.satisfied(mockTestObject(15, 20), results));
        Assert.assertFalse(module.satisfied(mockTestObject(30, 30), results));
        Assert.assertTrue(module.satisfied(mockTestObject(35, 40), results));

    }

    @After
    public void showDescription() {
        logger.info(module.getDescriptionWithContext());
    }

    private DAQ mockTestObject(long timestamp, int crashes) {
        DAQ snapshot = new DAQ();

        snapshot.setLastUpdate(timestamp * 1000);
        snapshot.setHltInfo(new HltInfo());
        snapshot.getHltInfo().setCrashes(crashes);

        return snapshot;
    }

		@Test
		public void testIssue170() throws URISyntaxException {
			
			final String snapshots[] = {
				"1522085133586.json.gz", // 0 FUs crashed       +0.000 sec
				"1522085135697.json.gz", // 120 FUs crashed     +2.111 sec
				"1522085138381.json.gz", // 1280 FUs crashed    +4.795 sec
				"1522085141390.json.gz", // 12596 FUs crashed   +7.804 sec
				"1522085143859.json.gz", // 13792 FUs crashed  +10.273 sec
				"1522085146549.json.gz", // 16160 FUs crashed  +12.963 sec
				"1522085149654.json.gz", // 16208 FUs crashed  +16.068 sec
				"1522085154259.json.gz", // 16208 FUs crashed  +20.673 sec
			};
			
			for (int i = 0; i < snapshots.length; ++i) {
				
				String snapshotFname = snapshots[i];
				DAQ daq = FlowchartCaseTestBase.getSnapshot(snapshotFname);
				
				boolean result = module.satisfied(daq, results);
				
				System.out.println("snapshot " + snapshotFname + " module.satisifed: " + result);
				
				if (i == snapshots.length - 1) {
					// at least at the last snapshot we must detect a failure
					Assert.assertTrue(result);
				}
			}
		}
}

