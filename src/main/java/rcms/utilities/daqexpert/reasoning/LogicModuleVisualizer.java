package rcms.utilities.daqexpert.reasoning;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.mixin.IdGenerators;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.Requiring;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.causality.CausalityManager;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LogicModuleVisualizer {

    private static Logger logger = Logger.getLogger(LogicModuleVisualizer.class);


    public void generateGraph(Set<CausalityNode> nodes) {

        Set<CausalityNode> r = getNextLevel(new HashSet<>(), nodes, 0);


        boolean modify = true;

        if(modify) {

            CausalityNode max = r.stream().max((a1, a2) -> a1.getLevel() > a2.getLevel() ? 1 : -1).orElse(null);
            System.out.println("max " + max.getLevel());


            Set<CausalityNode> referenced = new HashSet<>();
            for (CausalityNode causalityNode : r) {
                if (causalityNode.getCausing().size() > 0) {
                    for (CausalityNode referencedEntry : causalityNode.getCausing()) {
                        referenced.add(referencedEntry);
                    }
                }
            }

            System.out.println("Referenced: " + referenced.stream().map(c -> c.getNodeName()).collect(Collectors.toCollection(LinkedHashSet::new)));
            Set<CausalityNode> result = r.stream().filter(c -> c.getLevel() == 0 && !referenced.contains(c)).collect(Collectors.toCollection(LinkedHashSet::new));


            result.stream().forEach(c -> c.setLevel(max.getLevel()));

        }

        r = r.stream().sorted((c1, c2)-> c1.getLevel() < c2.getLevel()? -1 : (c1.getLevel() == c2.getLevel()? 0 : 1)).collect(Collectors.toCollection(LinkedHashSet::new));

        r.stream().forEach(c->c.getNodeName());

        ObjectMapper om = new ObjectMapper();
        om.addMixIn(CausalityNode.class, LogicModuleVisualizer.CausalityNodeMixin.class);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try {
            om.writeValue(new File("src/main/webapp/static/causality.json"), r);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Set<CausalityNode> getNextLevel(Set<CausalityNode> orderedNodes, Set<CausalityNode> remainingNodes, final int level) {

        if (remainingNodes.size() == 0) {
            return orderedNodes;
        }

        // find nodes in remaining nodes that are affected only by already ordered nodes
        Iterator<CausalityNode> iterator = remainingNodes.iterator();


        Set<CausalityNode> next = new HashSet<>();
        while(iterator.hasNext()){
            CausalityNode node = iterator.next();
            // all causing nodes must be in ordered nodes
            boolean allCausingAlreadyOrdered = true;
            for (CausalityNode causing : node.getCausing()) {
                if (!orderedNodes.contains(causing)) {
                    allCausingAlreadyOrdered = false;
                }
            }

            // confirmed
            if (allCausingAlreadyOrdered) {
                logger.info("Moving node: " + node + " with level " + level);

                next.add(node);
            }

        }

        remainingNodes.removeAll(next);
        orderedNodes.addAll(next);
        next.forEach(c->c.setLevel(level));

        return getNextLevel(orderedNodes, remainingNodes, level + 1);
    }

    public static void main(String[] args){

        LogicModuleVisualizer lmv = new LogicModuleVisualizer();

        List<LogicModule> modules = LogicModuleRegistry.getModulesInRunOrder();
        modules.forEach(m-> {if (m.getName().length() > 20){ m.setName(m.getName().substring(0,20));}});
        modules.stream().forEach(c->c.declareRelations());
        CausalityManager cm = new CausalityManager();

        Set<CausalityNode> nodes = modules.stream().filter(c->c.isProblematic()).map(c -> (CausalityNode) c).collect(Collectors.toSet());
        cm.transformToCanonical(nodes);
        cm.verifyNoCycle(nodes);

        lmv.generateGraph(nodes);



        Set<Requiring> requirementNodes = modules.stream().map(c -> (Requiring) c).collect(Collectors.toSet());

        lmv.generateRequirementGraph(requirementNodes);
    }


    public void generateRequirementGraph(Set<Requiring> nodes) {

        Set<Requiring> r = getNextRequirementLevel(new HashSet<>(), nodes, 0);


        r = r.stream().sorted((c1, c2)-> c1.getLevel() < c2.getLevel()? -1 : (c1.getLevel() == c2.getLevel()? 0 : 1)).collect(Collectors.toCollection(LinkedHashSet::new));

        r.stream().forEach(c->c.getNodeName());

        ObjectMapper om = new ObjectMapper();
        om.addMixIn(Requiring.class, LogicModuleVisualizer.RequiringMixin.class);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        try {
            om.writeValue(new File("src/main/webapp/static/requirement.json"), r);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Set<Requiring> getNextRequirementLevel(Set<Requiring> orderedNodes, Set<Requiring> remainingNodes, final int level) {

        if (remainingNodes.size() == 0) {
            return orderedNodes;
        }

        // find nodes in remaining nodes that are affected only by already ordered nodes
        Iterator<Requiring> iterator = remainingNodes.iterator();


        Set<Requiring> next = new HashSet<>();
        while(iterator.hasNext()){
            Requiring node = iterator.next();
            // all causing nodes must be in ordered nodes
            logger.info("Checking whether to add " + node.getNodeName());
            boolean add = true;
            for (Requiring required : node.getRequired()) {

                logger.info(" - " + required.getNodeName());
                if (!orderedNodes.contains(required)) {
                    add = false;
                    logger.info(" don't add!, not in ordered!");
                }
            }

            // confirmed
            if (add) {
                logger.info("Moving node: " + node + " with level " + level);
                next.add(node);
            } else{
                logger.info("Not adding " + node.getNodeName());


            }

        }

        remainingNodes.removeAll(next);
        orderedNodes.addAll(next);
        next.forEach(c->c.setLevel(level));

        return getNextRequirementLevel(orderedNodes, remainingNodes, level + 1);
    }



    @JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "id")
    @JsonIgnoreProperties({"priority", "logicModuleRegistry", "description", "holdNotifications","maturityThreshold", "contextHandler", "descriptionWithContext", "mature", "action", "actionWithContext", "last", "actionWithContextRawRecovery", "briefDescription", "problematic"})
    public interface CausalityNodeMixin {


        @JsonIgnore
        abstract List<CausalityNode> getCausing();

        @JsonProperty("required")
        @JsonIdentityReference(alwaysAsId = true)
        abstract List<CausalityNode> getAffected();

        @JsonProperty("name")
        abstract String getNodeName();


    }

    @JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "id")
    @JsonIgnoreProperties({"priority", "affected", "causing", "logicModuleRegistry", "description", "holdNotifications","maturityThreshold", "contextHandler", "descriptionWithContext", "mature", "action", "actionWithContext", "last", "actionWithContextRawRecovery", "briefDescription", "problematic"})
    public interface RequiringMixin {

        @JsonProperty("required")
        @JsonIdentityReference(alwaysAsId = true)
        abstract List<Requiring> getRequired();

        @JsonProperty("name")
        abstract String getNodeName();


    }
}


