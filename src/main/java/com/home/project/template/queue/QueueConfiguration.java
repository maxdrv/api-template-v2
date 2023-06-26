package com.home.project.template.queue;

import com.home.project.template.sortable.SortableRepository;
import com.home.project.template.sortable.StageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.api.impl.NoopPayloadTransformer;
import ru.yoomoney.tech.dbqueue.api.impl.ShardingQueueProducer;
import ru.yoomoney.tech.dbqueue.api.impl.SingleQueueShardRouter;
import ru.yoomoney.tech.dbqueue.config.*;
import ru.yoomoney.tech.dbqueue.config.impl.LoggingTaskLifecycleListener;
import ru.yoomoney.tech.dbqueue.config.impl.LoggingThreadLifecycleListener;
import ru.yoomoney.tech.dbqueue.settings.*;
import ru.yoomoney.tech.dbqueue.spring.dao.SpringDatabaseAccessLayer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Configuration
public class QueueConfiguration {

    @Bean
    public SpringDatabaseAccessLayer springDatabaseAccessLayer(
            JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate
    ) {
        return new SpringDatabaseAccessLayer(
                DatabaseDialect.POSTGRESQL,
                QueueTableSchema.builder().build(),  // заполняется дефолтами
                jdbcTemplate,
                transactionTemplate
        );
    }

    @Bean
    public QueueConfig stringQueueConfig() {
        QueueId queueId = new QueueId("message");

        QueueLocation queueLocation = QueueLocation.builder()
                .withQueueId(queueId)
                .withTableName("queue_tasks")
                .withIdSequence("queue_tasks_id_seq")
                .build();

        ProcessingSettings processingSettings = ProcessingSettings.builder()
                .withProcessingMode(ProcessingMode.SEPARATE_TRANSACTIONS)
                .withThreadCount(4)
                .build();

        ExtSettings extSettings = ExtSettings.builder()
                .withSettings(Collections.emptyMap())
                .build();

        FailureSettings failureSettings = FailureSettings.builder()
                .withRetryInterval(Duration.ofSeconds(15))
                .withRetryType(FailRetryType.LINEAR_BACKOFF)
                .build();

        PollSettings pollSettings = PollSettings.builder()
                .withNoTaskTimeout(Duration.ofSeconds(10))
                .withBetweenTaskTimeout(Duration.ofSeconds(0))
                .withFatalCrashTimeout(Duration.ofSeconds(2))
                .build();

        ReenqueueSettings reenqueueSettings = ReenqueueSettings.builder()
                .withInitialDelay(Duration.ofSeconds(10))
                .withRetryType(ReenqueueRetryType.ARITHMETIC)
                .withArithmeticStep(Duration.ofSeconds(10))
                .withFixedDelay(Duration.ofSeconds(10))
                .withGeometricRatio(5L)
                .withSequentialPlan(List.of(Duration.ofSeconds(10)))
                .build();

        QueueSettings queueSettings = QueueSettings.builder()
                .withProcessingSettings(processingSettings)
                .withExtSettings(extSettings)
                .withFailureSettings(failureSettings)
                .withPollSettings(pollSettings)
                .withReenqueueSettings(reenqueueSettings)
                .build();

        return new QueueConfig(queueLocation, queueSettings);
    }

    @Bean
    public QueueShard<SpringDatabaseAccessLayer> queueShard(
            SpringDatabaseAccessLayer databaseAccessLayer
    ) {
        return new QueueShard<>(new QueueShardId("main"), databaseAccessLayer);
    }

    @Bean
    public ShardingQueueProducer<String, SpringDatabaseAccessLayer> stringQueueProducer(
            QueueShard<SpringDatabaseAccessLayer> queueShard, QueueConfig stringQueueConfig
    ) {
        return new ShardingQueueProducer<>(
                stringQueueConfig, NoopPayloadTransformer.getInstance(), new SingleQueueShardRouter<>(queueShard)
        );
    }

    @Bean
    public StringQueueConsumer stringQueueConsumer(
            QueueConfig stringQueueConfig,
            SortableRepository sortableRepository,
            StageRepository stageRepository,
            ShardingQueueProducer<String, SpringDatabaseAccessLayer> stringQueueProducer,
            TransactionTemplate transactionTemplate
    ) {
        return new StringQueueConsumer(stringQueueConfig, sortableRepository, stageRepository, stringQueueProducer, transactionTemplate);
    }

    @Bean
    public QueueService queueService(
            QueueShard<SpringDatabaseAccessLayer> queueShard,
            QueueConsumer<String> stringQueueConsumer
    ) {
        QueueService queueService = new QueueService(
                Collections.singletonList(queueShard),
                new LoggingThreadLifecycleListener(),
                new LoggingTaskLifecycleListener()
        );

        queueService.registerQueue(stringQueueConsumer);

        return queueService;
    }

}
