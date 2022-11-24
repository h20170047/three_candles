package com.svj.config;

import com.svj.entity.StockDayData;
import com.svj.repository.StockDayPriceRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class ApplicationBatchConfig {

    @Autowired
    private StockDayPriceRepository repository;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Value("classpath:input/*.csv")
    private Resource[] resources;

    public FlatFileItemReader<StockDayData> itemReader(Resource resource){
        FlatFileItemReader<StockDayData> itemReader= new FlatFileItemReader<>();
        try {
            itemReader.setResource(new FileSystemResource(resource.getURL().getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        itemReader.setLinesToSkip(1);
        itemReader.setSaveState(false);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<StockDayData> lineMapper() {
        DefaultLineMapper<StockDayData> lineMapper= new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer= new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("SYMBOL", "SERIES", "OPEN", "HIGH", "LOW", "CLOSE", "LAST", "PREVCLOSE", "TOTTRDQTY", "TOTTRDVAL", "TIMESTAMP");
        BeanWrapperFieldSetMapper<StockDayData> fieldSetMapper= new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setConversionService(createConversionService());
        fieldSetMapper.setTargetType(StockDayData.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public StocksDataProcessor processor(){
        return new StocksDataProcessor();
    }

    @Bean
    public RepositoryItemWriter<StockDayData> itemWriter(){
        RepositoryItemWriter<StockDayData> itemWriter= new RepositoryItemWriter<>();
        itemWriter.setRepository(repository);
//        itemWriter.setMethodName("save"); //using default saveAll() brings up performance by huge margin. Dont override it with save() https://github.com/spring-projects/spring-batch/issues/3720
        return itemWriter;
    }

    public List<Step> importStocksDataStep(){
        List<Step> steps= new LinkedList<>();
        for(Resource resource: resources){
            steps.add(createStepForFile(resource));
        }
        return steps;
    }

    public Step createStepForFile(Resource resource){
        FlatFileItemReader<StockDayData> fileItemReader = itemReader(resource);

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.afterPropertiesSet();

        return stepBuilderFactory.get(resource.getFilename())
                .<StockDayData, StockDayData>chunk(1000)
//                .reader(multiResourceItemReader())
                .reader(fileItemReader)
                .processor(processor())
                .writer(itemWriter())
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(new BatchStepEventListener())
                .taskExecutor(taskExecutor)
//                .skipLimit(100)
//                .skip(NumberFormatException.class)
//                .noSkip(FileNotFoundException.class)
                .build();
    }

    @Bean
    public Job runJob(){
        SimpleJobBuilder importStocksDataJob = jobBuilderFactory.get("ImportStocksDataJob")
                .incrementer(new RunIdIncrementer()) // to make sure new jobInstance is run, with a diff ID
                .start(step0());
        for(Step step: importStocksDataStep()){
            importStocksDataJob.next(step);
        }
        return importStocksDataJob.build();
    }

    private Step step0() {
        return stepBuilderFactory.get("step0")
                .tasklet(tasklet0())
                .build();
    }

    private Tasklet tasklet0() {
        return (new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello world  " );
                return RepeatStatus.FINISHED;
            }
        });
    }

    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor taskExecutor= new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(8);
        return taskExecutor;
    }

    @Bean
    public ConversionService createConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        DefaultConversionService.addDefaultConverters(conversionService);
        conversionService.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String text) {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("d-MMM-yyyy")
                        .toFormatter();
                return LocalDate.parse(text, formatter);
            }
        });
        return conversionService;
    }
}
