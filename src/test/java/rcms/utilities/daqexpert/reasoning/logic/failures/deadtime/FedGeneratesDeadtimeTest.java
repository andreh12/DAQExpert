package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.*;

public class FedGeneratesDeadtimeTest {
    @Test
    public void satisfied() throws Exception {

        FedGeneratesDeadtime module = new FedGeneratesDeadtime();
        Properties p = new Properties();
        Map<String,Boolean> r = new HashMap<>();
        r.put(FEDDeadtime.class.getSimpleName(),true);
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(),"2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(),"2");
        module.parametrize(p);

        Assert.assertFalse(module.satisfied(mockTestObject(0,0), r));
        Assert.assertFalse(module.satisfied(mockTestObject(2,2), r));
        Assert.assertTrue(module.satisfied(mockTestObject(3,1), r));
        Assert.assertFalse(module.satisfied(mockTestObject(3,3), r));

    }

    @Test
    public void pseudoFedHierarchyTest() throws Exception {

        FedGeneratesDeadtime module = new FedGeneratesDeadtime();
        Properties p = new Properties();
        Map<String, Boolean> r = new HashMap<>();
        r.put(FEDDeadtime.class.getSimpleName(), true);
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(p);



        DAQ snapshot = new DAQ();
        TTCPartition partition = new TTCPartition();
        Set<FED> feds = new HashSet<>();

        FED pseudoFed = mockTestObject(10000, 10,0);
        FED fed = mockTestObject(1, 0,0);
        fed.setDependentFeds(Arrays.asList(pseudoFed));
        feds.add(fed);
        partition.setFeds(new ArrayList<>(feds));

        Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(partition);
        System.out.println(h);

        for (Map.Entry<FED, Set<FED>> e : h.entrySet()) {
            String deps = "";
            boolean notFirst = false;
            for (FED dep : e.getValue()) {
                if (notFirst) {
                    deps += ", ";
                }
                notFirst = true;
                deps += dep.getSrcIdExpected() + "dt: " + dep.getPercentBackpressure();
            }
            System.out.println(
                    "    [" + e.getKey().getSrcIdExpected() + "bp: " + (e.getKey().getPercentBusy() + e.getKey().getPercentWarning()) + "]" + (deps.equals("") ? "" : ": " + deps));
        }

        snapshot.setFeds(feds);
        snapshot.getFeds();

        snapshot.setTtcPartitions(Arrays.asList(partition));
        Assert.assertTrue(module.satisfied(snapshot, r));

    }

    private DAQ mockTestObject(float deadtime, float backpressure) {
        DAQ snapshot = new DAQ();
        Set<FED> feds = new HashSet<>();
        feds.add(mockTestObject(1, 0, 0));
        feds.add(mockTestObject(2, deadtime, backpressure));
        feds.add(mockTestObject(3, 0, 0));
        snapshot.setFeds(feds);

        TTCPartition p = new TTCPartition();
        p.setName("testpartition");
        p.setFeds(new ArrayList<>(feds));
        snapshot.setTtcPartitions(Arrays.asList(p));

        return snapshot;
    }

    private FED mockTestObject(int id, float deadtime, float backpressure) {
        FED fed = new FED();
        fed.setSrcIdExpected(id);
        fed.setPercentBackpressure(backpressure);
        fed.setPercentBusy(deadtime);
        return fed;
    }

}