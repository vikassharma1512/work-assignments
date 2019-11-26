package com.vik.app.config;

import com.vik.app.processor.EncryptProcessor;
import com.vik.listener.ChunkExecutionListener;
import com.vik.listener.JobCompletionNotificationListener;
import com.vik.listener.StepExecutionNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Class for batch configuration
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Value("${input.file}")
    private String inputFile;

    @Value("${max.threads}")
    private Integer maxThreads;

    @Value("${caesar.key}")
    private Integer key;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("classpath:org/springframework/batch/core/schema-h2.sql")
    private Resource schemaScript;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(schemaScript);
        return populator;
    }

    @Bean
    FlatFileItemReader<String> reader() {
        return new FlatFileItemReaderBuilder<String>().name("lineReader").resource(new FileSystemResource(inputFile))
                .lineMapper(new PassThroughLineMapper()).build();
    }

    @Bean
    EncryptProcessor processor() {
        return new EncryptProcessor(5);
    }

    @Bean
    FlatFileItemWriter<String> writer() {
        return new FlatFileItemWriterBuilder<String>().name("lineWriter")
                .resource(new FileSystemResource(inputFile + ".encrypt.txt"))
                .lineAggregator(new PassThroughLineAggregator<>()).build();
    }

    @Bean
    public JobCompletionNotificationListener jobExecutionListener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public StepExecutionNotificationListener stepExecutionListener() {
        return new StepExecutionNotificationListener();
    }

    @Bean
    public ChunkExecutionListener chunkExecutionListener(){
        return new ChunkExecutionListener();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(maxThreads);
        return executor;
    }

    @Bean
    public Job fileEncryptionJob() {
        return jobBuilderFactory.get("file-encryption-job")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("file-encryption-step").<String, String>chunk(1)
                .reader(reader()).processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .listener(stepExecutionListener())
                .listener(chunkExecutionListener())
                .throttleLimit(maxThreads)
                .build();
    }

    @Bean
    public JobParameters jobParameters() {
        return new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
    }

    @Bean
    public JobExecution jobExecution() throws MalformedURLException {
        if (!Files.exists(Paths.get(inputFile))) {
            log.error("Missing input file.");
            System.exit(-1);
        }
        if (maxThreads == null) {
            log.error("Invalid max threads size.");
            System.exit(-1);
        }

        JobExecution run = null;

        log.trace("Encrypted output file is :: " + inputFile + ".encrypt.txt");

        try {
            run = jobLauncher.run(fileEncryptionJob(), jobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return run;
    }
}