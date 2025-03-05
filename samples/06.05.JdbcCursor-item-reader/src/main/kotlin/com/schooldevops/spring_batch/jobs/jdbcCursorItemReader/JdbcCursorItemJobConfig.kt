package com.schooldevops.spring_batch.jobs.jdbcCursorItemReader

import com.schooldevops.spring_batch.jobs.flatfilereader.Customer
import com.schooldevops.spring_batch.jobs.flatfilereader.FlatFileItemJobConfig
import com.schooldevops.spring_batch.jobs.flatfilereader.FlatFileItemJobConfig.Companion
import com.schooldevops.spring_batch.jobs.flatfilereader.FlatFileItemJobConfig.Companion.FLAT_FILE_CHUNK_JOB
import com.schooldevops.spring_batch.jobs.jdbcPagingItemreader.JdbcPagingItemJobConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(name = ["spring.batch.job.name"], havingValue = JdbcCursorItemJobConfig.JDBC_CURSOR_CHUNK_JOB)
class JdbcCursorItemJobConfig {
    private val log: Logger = LoggerFactory.getLogger(JdbcCursorItemJobConfig::class.java)

    companion object {
        const val CHUNK_SIZE: Int = 2
        const val ENCODING: String = "UTF-8"
        const val JDBC_CURSOR_CHUNK_JOB: String = "JDBC_CURSOR_CHUNK_JOB"
    }

    @Autowired
    lateinit var dataSource: DataSource

    @Bean
    fun jdbcCursorItemReader(): JdbcCursorItemReader<Customer> {
        return JdbcCursorItemReaderBuilder<Customer>()
            .name("jdbcCursorItemReader")
            .fetchSize(CHUNK_SIZE)
            .dataSource(dataSource)
            .rowMapper(BeanPropertyRowMapper(Customer::class.java))
//            .rowMapper(CustomerRowMapper())
            .sql("SELECT id, name, age, gender FROM customer WHERE age > ?")
            .queryArguments(listOf(20))
            .build()
    }


    @Bean
    fun customerCursorFlatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("CustomerCursorFlatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new.csv"))
            .encoding(FlatFileItemJobConfig.ENCODING)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .build()
    }

    @Bean
    fun customerCursorFlatFileStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info("--------------- Init flatFileStep ---------------")
        return StepBuilder("customerCursorFlatFileStep", jobRepository)
            .chunk<Customer, Customer>(FlatFileItemJobConfig.CHUNK_SIZE, transactionManager)
            .reader(jdbcCursorItemReader())
            .writer(customerCursorFlatFileItemWriter())
            .build()
    }

    @Bean
    fun customerCursorFlatFileJob(customerCursorFlatFileStep: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init flatFileJob -----------------");
        return JobBuilder(JDBC_CURSOR_CHUNK_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(customerCursorFlatFileStep)
            .build()
    }
}