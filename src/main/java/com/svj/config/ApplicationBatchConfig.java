package com.svj.config;

import com.svj.entity.StockDayData;
import com.svj.repository.StockDayPriceRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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

    @Bean
    public FlatFileItemReader<StockDayData> itemReader(){

        FlatFileItemReader<StockDayData> itemReader= new FlatFileItemReader<>();
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    @Bean
    public MultiResourceItemReader<StockDayData> multiResourceItemReader(){
        MultiResourceItemReader<StockDayData> multiResourceItemReader = new MultiResourceItemReader<StockDayData>();
        multiResourceItemReader.setResources(resources);
        multiResourceItemReader.setDelegate(itemReader());
        return multiResourceItemReader;
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
        itemWriter.setMethodName("save");
        return itemWriter;
    }

    @Bean
    public Step importStocksDataStep(){
        return stepBuilderFactory.get("ImportStocksDataStep")
                .<StockDayData, StockDayData>chunk(10) // name should match name of bean- use camel casing
                .reader(multiResourceItemReader())
                .processor(processor())
                .writer(itemWriter())
                .faultTolerant()
                .skipPolicy(skipPolicy())
                .listener(skipListener())
//                .taskExecutor(taskExecutor())
//                .skipLimit(100)
//                .skip(NumberFormatException.class)
//                .noSkip(FileNotFoundException.class)
                .build();
    }

    @Bean
    public Job runJob(){
        return jobBuilderFactory.get("ImportStocksDataJob")
                .flow(importStocksDataStep())
                .end().build();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor taskExecutor= new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(8);
        return taskExecutor;
    }

    @Bean
    public SkipPolicy skipPolicy(){
        return new CustomSkipPolicy();
    }

    @Bean
    public SkipListener skipListener(){
        return new BatchStepEventListener();
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
                        .appendPattern("dd-MMM-yyyy")
                        .toFormatter();
                return LocalDate.parse(text, formatter);
            }
        });
        return conversionService;
    }
}
