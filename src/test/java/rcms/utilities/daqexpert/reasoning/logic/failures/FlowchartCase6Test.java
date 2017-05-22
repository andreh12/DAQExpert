package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import static rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase.getSnapshot;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase6Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {

		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		test("1480141295312.smile");
  }
	
	@Test
	public void testTCDSonlyBusyWarning() throws URISyntaxException {

		// Thu May 18 18:45:42 CEST 2017
		// see https://github.com/cmsdaq/DAQExpert/issues/56#issuecomment-303091911
		// TWINMUX (uTCA FED) 100% busy
		test("1495125942911.smile");
  }
	
  private void test(String snapshotFile) throws URISyntaxException {
		DAQ snapshot = getSnapshot(snapshotFile);

		assertEqualsAndUpdateResults(false, fc1,snapshot);
		assertEqualsAndUpdateResults(false, fc2,snapshot);
		assertEqualsAndUpdateResults(false, fc3,snapshot);
		
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, piProblem,snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem,snapshot);
		
		assertEqualsAndUpdateResults(false, fc5,snapshot);
		assertEqualsAndUpdateResults(true, fc6,snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

	}

}
