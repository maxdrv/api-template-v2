package com.home.project.template.queue;

import com.home.project.template.generator.Sortable;
import com.home.project.template.generator.SortableRepository;
import com.home.project.template.generator.Stage;
import com.home.project.template.generator.StageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yoomoney.tech.dbqueue.api.*;
import ru.yoomoney.tech.dbqueue.api.impl.NoopPayloadTransformer;
import ru.yoomoney.tech.dbqueue.api.impl.ShardingQueueProducer;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;
import ru.yoomoney.tech.dbqueue.spring.dao.SpringDatabaseAccessLayer;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StringQueueConsumer implements QueueConsumer<String> {

    private static final Logger log = LoggerFactory.getLogger(StringQueueConsumer.class);

    private final QueueConfig queueConfig;
    private final SortableRepository sortableRepository;
    private final StageRepository stageRepository;
    private final ShardingQueueProducer<String, SpringDatabaseAccessLayer> stringQueueProducer;
    private final TransactionTemplate transactionTemplate;

    public StringQueueConsumer(
            QueueConfig queueConfig,
            SortableRepository sortableRepository,
            StageRepository stageRepository,
            ShardingQueueProducer<String, SpringDatabaseAccessLayer> stringQueueProducer,
            TransactionTemplate transactionTemplate
    ) {
        this.queueConfig = queueConfig;
        this.sortableRepository = sortableRepository;
        this.stageRepository = stageRepository;
        this.stringQueueProducer = stringQueueProducer;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public TaskExecutionResult execute(Task<String> task) {
        String str = task.getPayloadOrThrow();
        Long sortableId = Long.valueOf(str);
        Sortable sortable = sortableRepository.findByIdOrThrow(sortableId);

        Long curStageId = sortable.stageId();

        List<Stage> allCached = stageRepository.findAllCached();

        Iterator<Stage> iterator = allCached.iterator();
        while (iterator.hasNext()) {
            Stage stage = iterator.next();
            if (Objects.equals(stage.id(), curStageId)) {
                break;
            }
        }

        if (iterator.hasNext()) {
            Stage nextStage = iterator.next();
            sortableRepository.update(sortableId, nextStage);
//            log.info("sortable {} next status: {}", sortableId, nextStage.status());
            EnqueueParams<String> params = EnqueueParams
                    .create(String.valueOf(sortableId))
                    .withExecutionDelay(Duration.ofSeconds(2));

            transactionTemplate.execute(t -> {
                stringQueueProducer.enqueue(params);
                return null;
            });
        } else {
            log.info("sortable {} final status: {}", sortableId, sortable.status());
        }
        return TaskExecutionResult.finish();
    }

    @Override
    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    @Override
    public TaskPayloadTransformer<String> getPayloadTransformer() {
        return NoopPayloadTransformer.getInstance();
    }

}
