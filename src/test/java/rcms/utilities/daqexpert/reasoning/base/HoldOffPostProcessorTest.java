package rcms.utilities.daqexpert.reasoning.base;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;

import java.util.Map;

public class HoldOffPostProcessorTest {

    @Test
    public void satisfiedWithHoldoff() {

        DaqOnFire daqOnFire = new DaqOnFire(1000L);
        Logger.getLogger(HoldOffPostProcessor.class).setLevel(Level.DEBUG);

        DAQ daq = new DAQ();

        daq.setDaqState("NotOnFire");
        daq.setLastUpdate(0);
        Assert.assertFalse(daqOnFire.satisfied(daq, null));
        Assert.assertFalse(daqOnFire.satisfiedWithPostProcessing(daq, null));


        daq.setDaqState("OnFire");
        daq.setLastUpdate(1000);
        Assert.assertTrue(daqOnFire.satisfied(daq, null));
        Assert.assertFalse(daqOnFire.satisfiedWithPostProcessing(daq, null));


        daq.setDaqState("OnFire");
        daq.setLastUpdate(2000);
        Assert.assertTrue(daqOnFire.satisfied(daq, null));
        Assert.assertTrue(daqOnFire.satisfiedWithPostProcessing(daq, null));

    }

}

class DaqOnFire extends SimpleLogicModule {

    public DaqOnFire(Long holdOffPeriod) {
        super(new HoldOffPostProcessor(holdOffPeriod));
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
        if (daq.getDaqState().equals("OnFire")) {
            return true;
        }
        return false;
    }
}