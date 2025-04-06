package com.schooldevops.spring_batch.jobs.flatfilereader

import com.schooldevops.spring_batch.jobs.data.Customer
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.ConcurrentHashMap

@Configuration
class FlatFileItemJobConfig {
    private val log: org.slf4j.Logger = LoggerFactory.getLogger(FlatFileItemJobConfig::class.java)

    private val aggregateInfos = ConcurrentHashMap<String, Int>()

    private val itemProcessor: ItemProcessor<Customer, Customer> = AggregateCustomerProcessor(aggregateInfos)

    companion object {
        /**
         * CHUNK 크기를 지정한다.
         */
        const val CHUNK_SIZE: Int = 100
        const val ENCODING: String = "UTF-8"
        const val FLAT_FILE_WRITER_CHUNK_JOB: String = "FLAT_FILE_WRITER_CHUNK_JOB"
    }

    @Bean
    fun flatFileItemReader(): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("FlatFileItemReader")
            .resource(ClassPathResource("./customer.csv"))
            .encoding(ENCODING)
            .delimited().delimiter(",")
            .names("name", "age", "gender")
            .targetType(Customer::class.java)
            .build()
    }

    @Bean
    fun flatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("flatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .append(false)
            .lineAggregator(CustomerLineAggregator())
            .headerCallback(CustomerHeader())
            .footerCallback(CustomerFooter(aggregateInfos))
            .build()
    }


    @Bean
    fun flatFileStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info("------------------ Init flatFileStep -----------------")

        return StepBuilder("flatFileStep", jobRepository)
            .chunk<Customer, Customer>(CHUNK_SIZE, transactionManager)
            .reader(flatFileItemReader())
            .processor(itemProcessor)
            .writer(flatFileItemWriter())
            .build()
    }

    @Bean
    fun flatFileJob(flatFileStep: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init flatFileJob -----------------")
        return JobBuilder(FLAT_FILE_WRITER_CHUNK_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(flatFileStep)
            .build()
    }

}