package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

public class SubsystemErrorTest {

	/**
	 * method to load a deserialize a snapshot given a file name
	 *
	 * Similar to FlowchartCaseTestBase.getSnapshot() but with
	 * a different base directory
	 */
	public static DAQ getSnapshot(String fname) throws URISyntaxException {

		StructureSerializer serializer = new StructureSerializer();

		URL url = SubsystemError.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath());
	}


	@Test
	public void testExcludedSubsystemInError() throws URISyntaxException {
		// Mon Jul 24 14:09:58 CEST 2017
		// preshower was in error but not part of global run
		DAQ snapshot = getSnapshot("1500898198794.json.gz");

		SubsystemError instance = new SubsystemError();

		// mock inputs in order not to put dependencies on other tests
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		// run is ongoing
		results.put(RunOngoing.class.getSimpleName(), true);

		// we expect rate
		results.put(ExpectedRate.class.getSimpleName(), true);

		// no transition period
		results.put(LongTransition.class.getSimpleName(), false);

		boolean atLeastOneSubsystemInError = instance.satisfied(snapshot, results);

		Set<Object> subsystemsInError = instance.getContext().getContext().get("SUBSYSTEM");

		Assert.assertEquals("subsystem mistakenly reported as in error state: "  + subsystemsInError,
						false, atLeastOneSubsystemInError);
	}

}
