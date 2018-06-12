package rcms.utilities.daqexpert.reasoning.causality;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Date;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class DominatingSelectorTest {


    /**
     * In this case 2 LM do not have 'requirement' relationship. 'Causality' relationship is used.
     * @throws Exception
     */
    @Test
    public void getLeafsFromCausality() throws Exception {
        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.VeryHighTcdsInputRate);
        Condition c2 = generateCondition(LogicModuleRegistry.RateTooHigh);
        conditionList.add(c1);
        conditionList.add(c2);

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());
        cm.transformToCanonical(a);

        Set<Condition> result = ds.getLeafsFromCausality(conditionList);
        assertEquals(1, result.size());
        assertThat(result, hasItem(c1));
        assertThat(result, not(hasItem(c2)));
    }

    /**
     *
     * Requirement graph: c1 -> c3, c2 independent
     * Causality graph: c1 -> c2 <- c3
     *
     * Choosing process:
     * - from requirement graph c3 or c2 will be selected
     * - from causality c1 or c3 will be selected
     * - combination of both will yield c3
     *
     *
     * @throws Exception
     */
    @Test
    public void selectDominating() throws Exception {
        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.TTSDeadtime);
        Condition c3 = generateCondition(LogicModuleRegistry.FlowchartCase5);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.add(c3);

        CausalityManager cm = new CausalityManager();

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());

        cm.transformToCanonical(a);



        Set<Condition> subResult1 = ds.getLeafsFromUsageGraph(conditionList);
        assertEquals(2, subResult1.size());

        Set<Condition> subResult2 = ds.getLeafsFromCausality(conditionList);
        assertEquals(1, subResult2.size());

        Condition result = ds.selectDominating(conditionList);

        assertEquals(c3, result);


    }

    @Test
    public void dontConciderConditionsThatEnded() throws Exception {
        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.TTSDeadtime);
        Condition c3 = generateCondition(LogicModuleRegistry.FlowchartCase5);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.add(c3);

        c3.setEnd(new Date());

        CausalityManager cm = new CausalityManager();

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());

        cm.transformToCanonical(a);

        Condition result = ds.selectDominating(conditionList);
        assertEquals(c1, result);

    }

    /**
     * In this case 2 LM do not have 'causality' relation. 'Required' relation is used
     * @throws Exception
     */
    @Test
    public void getLeafsFromUsageGraph() throws Exception {

        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.RuFailed);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<Condition> result = ds.getLeafsFromUsageGraph(conditionList);
        assertEquals(1, result.size());
        assertThat(result, hasItem(c2));
        assertThat(result, not(hasItem(c1)));

    }

    private Condition generateCondition(LogicModuleRegistry logicModuleRegistry) {
        Condition c = new Condition();
        c.setProblematic(true);

        c.setLogicModule(logicModuleRegistry);

        return c;
    }
    /**
     * This test checks that LM can both declare requirement and affected relationship as long as:
     * - if LM A requires B
     * - LM A can affect B
     * - LM A cannot be caused by B - technically can but it will not be taken into account by dominating selection mechanism
     */
    @Test
    public void bothRequirementAndCausalityRelation(){

        LogicModule lm1 = new LogicModuleMock("HLT CPU load mock");
        LogicModule lm2 = new LogicModuleMock("BP from HLT");

        /* This means the LM1 will be used */
        lm1.getAffected().add(lm2);


        Condition c1 = generateCondition(lm1);
        Condition c2 = generateCondition(lm2);

        Set<Condition> conditions = new HashSet<>();
        conditions.add(c1);
        conditions.add(c2);

        DominatingSelector ds = new DominatingSelector();
        Condition dominating = ds.selectDominating(conditions);

        Assert.assertNotNull(dominating);
        Assert.assertEquals(c1, dominating);

        /* This means the LM1 will be used */
        /* The situation should not change after updating required relation*/
        lm1.getRequired().add(lm2);

        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> set = new HashSet<>();
        set.add(lm1);
        set.add(lm2);
        cm.transformToCanonical(set);

        dominating = ds.selectDominating(conditions);

        Assert.assertNotNull(dominating);
        Assert.assertEquals(c1, dominating);

    }

    private Condition generateCondition(LogicModule producer){

        Condition c = new ConditionMock(producer);
        c.setTitle(producer.getName());
        return c;
    }

    class LogicModuleMock extends LogicModule{

        public LogicModuleMock(String name) {
            this.name = name;
        }

    }

    class ConditionMock extends Condition{

        LogicModule producer;

        public ConditionMock(LogicModule producer) {
            this.producer = producer;
            this.setProblematic(true);
        }

        @Override
        public LogicModule getProducer() {
            return producer;
        }
    }

}