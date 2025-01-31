package com.redhat.service.smartevents.manager.v2.persistence.dao;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.redhat.service.smartevents.manager.v2.TestConstants;
import com.redhat.service.smartevents.manager.v2.persistence.models.Bridge;
import com.redhat.service.smartevents.manager.v2.persistence.models.Condition;
import com.redhat.service.smartevents.manager.v2.persistence.models.Processor;
import com.redhat.service.smartevents.manager.v2.utils.DatabaseManagerUtils;
import com.redhat.service.smartevents.test.resource.PostgresResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static com.redhat.service.smartevents.manager.v2.utils.Fixtures.createBridge;
import static com.redhat.service.smartevents.manager.v2.utils.Fixtures.createCondition;
import static com.redhat.service.smartevents.manager.v2.utils.Fixtures.createProcessor;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class ProcessorDAOTest {

    @Inject
    ProcessorDAO processorDAO;

    @Inject
    BridgeDAO bridgeDAO;

    @Inject
    ConditionDAO conditionDAO;

    @Inject
    DatabaseManagerUtils databaseManagerUtils;

    @BeforeEach
    public void cleanUp() {
        databaseManagerUtils.cleanUpAndInitWithDefaultShard();
    }

    @Test
    @Transactional
    public void testStoreProcessor() {
        Bridge bridge = createBridge();
        bridgeDAO.persist(bridge);

        Processor processor = createProcessor(bridge);
        processorDAO.persist(processor);

        Processor retrieved = processorDAO.findById(processor.getId());
        assertThat(retrieved.getId()).isEqualTo(processor.getId());
        assertThat(retrieved.getBridge().getId()).isEqualTo(bridge.getId());
    }

    @Test
    @Transactional
    public void testStoreProcessorWithConditions() {
        Bridge bridge = createBridge();
        bridgeDAO.persist(bridge);

        Processor processor = createProcessor(bridge);

        Condition condition = createCondition();
        processor.setConditions(List.of(condition));
        processorDAO.persist(processor);

        Processor retrieved = processorDAO.findById(processor.getId());
        assertThat(retrieved.getId()).isEqualTo(processor.getId());
        assertThat(retrieved.getBridge().getId()).isEqualTo(bridge.getId());
        assertThat(retrieved.getConditions()).hasSize(1);
    }

    @Test
    public void testRemovalOfCondition() {
        Processor processor = createProcessorWithConditions();
        removeConditionFromProcessor(processor);

        List<Condition> conditions = conditionDAO.findAll().list();

        assertThat(conditions.size()).isEqualTo(2);
    }

    @Transactional
    protected Processor createProcessorWithConditions() {
        Bridge bridge = createBridge();
        bridgeDAO.persist(bridge);

        Processor processor = createProcessor(bridge);

        Condition condition1 = createCondition();
        Condition condition2 = createCondition();
        Condition condition3 = createCondition();

        processor.setConditions(List.of(condition1, condition2, condition3));
        processorDAO.persist(processor);

        return processor;
    }

    @Transactional
    protected void removeConditionFromProcessor(Processor processor) {
        Processor retrieved = processorDAO.findById(processor.getId());
        assertThat(retrieved.getConditions()).hasSize(3);

        Condition condition3 = retrieved.getConditions().get(2);
        retrieved.getConditions().remove(condition3);
        processorDAO.persist(retrieved);
    }

    @Test
    public void findByBridgeIdAndName_noMatchingBridgeId() {
        Bridge b = createBridge();
        Processor p = createProcessor(b, "foo");

        assertThat(processorDAO.findByBridgeIdAndName("doesNotExist", p.getName())).isNull();
    }

    @Test
    public void findByBridgeIdAndName_noMatchingProcessorName() {
        Bridge b = createBridge();
        createProcessor(b, "foo");

        assertThat(processorDAO.findByBridgeIdAndName(b.getId(), "doesNotExist")).isNull();
    }

    @Test
    public void findByBridgeIdAndName() {
        Bridge b = createBridge();
        Processor p = createProcessor(b, "foo");
        bridgeDAO.persist(b);
        processorDAO.persist(p);

        Processor byBridgeIdAndName = processorDAO.findByBridgeIdAndName(b.getId(), p.getName());
        assertThat(byBridgeIdAndName).isNotNull();
        assertThat(byBridgeIdAndName.getName()).isEqualTo(p.getName());
        assertThat(byBridgeIdAndName.getBridge().getId()).isEqualTo(b.getId());
    }

    @Test
    public void countByBridgeIdAndCustomerId() {
        Bridge b = createBridge();
        Processor p = createProcessor(b, "foo");
        Processor p1 = createProcessor(b, "bar");
        bridgeDAO.persist(b);
        processorDAO.persist(p);
        processorDAO.persist(p1);

        long total = processorDAO.countByBridgeIdAndCustomerId(b.getId(), TestConstants.DEFAULT_CUSTOMER_ID);
        assertThat(total).isEqualTo(2L);
    }
}
