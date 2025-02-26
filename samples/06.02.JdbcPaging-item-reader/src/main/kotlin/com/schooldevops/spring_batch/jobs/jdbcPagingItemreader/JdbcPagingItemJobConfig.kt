package com.schooldevops.spring_batch.jobs.jdbcPagingItemreader

import com.schooldevops.spring_batch.jobs.flatfilereader.Customer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
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
@ConditionalOnProperty(name = ["spring.batch.job.name"], havingValue = "JDBC_PAGING_CHUNK_JOB")
class JdbcPagingItemJobConfig {
    private val log: Logger = LoggerFactory.getLogger(JdbcPagingItemJobConfig::class.java)

    companion object {
        const val CHUNK_SIZE: Int = 100
        const val ENCODING: String = "UTF-8"
        const val FLAT_FILE_CHUNK_JOB: String = "JDBC_PAGING_CHUNK_JOB"
    }

    @Autowired
    lateinit var dataSource: DataSource

    @Bean
    fun queryProvider(): PagingQueryProvider {
        val queryProvider = SqlPagingQueryProviderFactoryBean()
        queryProvider.setDataSource(dataSource)  // DB 에 맞는 PagingQueryProvider 를 선택하기 위함
        queryProvider.setSelectClause("id, name, age, gender")
        queryProvider.setFromClause("from customer")
        queryProvider.setWhereClause("where age >= :age")

        val sortKeys = mapOf("id" to Order.DESCENDING)

        queryProvider.setSortKeys(sortKeys)

        return queryProvider.getObject()
    }

    @Bean
    fun jdbcPagingItemReader(): JdbcPagingItemReader<Customer> {
        val parameterValue = mutableMapOf<String, Any>()
        parameterValue["age"] = 20

        return JdbcPagingItemReaderBuilder<Customer>()
            .name("jdbcPagingItemReader")
            .fetchSize(CHUNK_SIZE)
            .dataSource(dataSource)
            .rowMapper(BeanPropertyRowMapper(Customer::class.java))
            .queryProvider(queryProvider())
            .parameterValues(parameterValue)
            .build()
    }

    @Bean
    fun flatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("FlatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .build()
    }

    @Bean
    fun flatFileStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info("--------------- Init flatFileStep ---------------")
        return StepBuilder("flatFileStep", jobRepository)
            .chunk<Customer, Customer>(CHUNK_SIZE, transactionManager)
            .reader(jdbcPagingItemReader())
            .writer(flatFileItemWriter())
            .build()
    }

    @Bean
    fun flatFileJob(flatFileStep: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init flatFileJob -----------------");
        return JobBuilder(FLAT_FILE_CHUNK_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(flatFileStep)
            .build()
    }
}