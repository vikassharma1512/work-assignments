package com.vik.app;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
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
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.vik.batch.EncryptProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

	@Value("${input.file}")
	private String inputFile;

	@Value("${thread.pool.size}")
	private Integer threadPoolSize;

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
				.resource(new FileSystemResource(inputFile + ".enc.txt"))
				.lineAggregator(new PassThroughLineAggregator<>()).build();
	}

	@Bean
	public JobExecution jobExecution(FlatFileItemReader<String> reader, EncryptProcessor processor,
			FlatFileItemWriter<String> writer) throws MalformedURLException {
		Path inputFilePath = Paths.get(inputFile);
		if (!Files.exists(inputFilePath)) {
			log.error("Invalid input file");
			System.exit(-1);
		}
		if (threadPoolSize == null) {
			log.error("Invalid threadPoolSize file");
			System.exit(-1);
		}

		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
		executor.setConcurrencyLimit(threadPoolSize);

		Step step1 = stepBuilderFactory.get("step1").<String, String>chunk(1).reader(reader).processor(processor)
				.writer(writer).taskExecutor(executor).build();

		Job job = jobBuilderFactory.get("testJob").incrementer(new RunIdIncrementer()).start(step1).build();

		JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		JobExecution run = null;

		log.info("------------------------ Output file: " + inputFile + ".enc.txt ---------------------------");

		try {
			run = jobLauncher.run(job, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return run;
	}

}