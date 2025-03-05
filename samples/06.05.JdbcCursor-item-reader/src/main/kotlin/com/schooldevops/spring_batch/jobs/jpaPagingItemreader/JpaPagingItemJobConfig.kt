package com.schooldevops.spring_batch.jobs.jdbcPagingItemreader

import com.schooldevops.spring_batch.jobs.data.Customer2
import jakarta.persistence.EntityManagerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@ConditionalOnProperty(name = ["spring.batch.job.name"], havingValue = "JPA_CHUNK_JOB")
class JpaPagingItemJobConfig {
    private val log: Logger = LoggerFactory.getLogger(JpaPagingItemJobConfig::class.java)

    companion object {
        const val CHUNK_SIZE: Int = 100
        const val ENCODING: String = "UTF-8"
        const val JPA_CHUNK_JOB: String = "JPA_CHUNK_JOB"
    }

    @Autowired
    lateinit var dataSource: DataSource

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @Bean
    @Throws(Exception::class)
    fun customerJpaPagingItemReader(): JpaPagingItemReader<Customer2> {
        return JpaPagingItemReaderBuilder<Customer2>()
            .name("customerJpaPagingItemReader")
            .queryString("SELECT c FROM Customer2 c WHERE c.age > :age order by id desc")
            .pageSize(CHUNK_SIZE)
            .entityManagerFactory(entityManagerFactory)
            .parameterValues(mapOf("age" to 20))
            .build()
    }

    @Bean
    fun customerJpaFlatFileItemWriter(): FlatFileItemWriter<Customer2> {
        return FlatFileItemWriterBuilder<Customer2>()
            .name("CustomerJpaFlatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .build()
    }

    @Bean
    fun jpaPagingStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info("--------------- Init flatFileStep ---------------")
        return StepBuilder("jpaPagingStep", jobRepository)
            .chunk<Customer2, Customer2>(CHUNK_SIZE, transactionManager)
            .reader(customerJpaPagingItemReader())
            .writer(customerJpaFlatFileItemWriter())
            .build()
    }

    @Bean
    fun flatFileJob(jpaPagingStep: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init flatFileJob -----------------");
        return JobBuilder(JPA_CHUNK_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jpaPagingStep)
            .build()
    }
}