package com.schooldevops.spring_batch.jobs.mybatisPagingItemReader

import com.schooldevops.spring_batch.jobs.data.Customer2
import com.schooldevops.spring_batch.jobs.flatfilereader.Customer
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.batch.MyBatisPagingItemReader
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
class MybatisReaderJobConfig {

    private val log: Logger = LoggerFactory.getLogger(MybatisReaderJobConfig::class.java)

    companion object {
        const val CHUNK_SIZE: Int = 2
        const val ENCODING: String = "UTF-8"
        const val MYBATIS_PAGING_JOB: String = "MYBATIS_PAGING_JOB"

    }

    @Autowired
    var dataSource: DataSource? = null

    @Autowired
    var sqlSessionFactory: SqlSessionFactory? = null

    @Bean
    @Throws(Exception::class)
    fun myBatisItemReader(): MyBatisPagingItemReader<Customer2> {
        return MyBatisPagingItemReaderBuilder<Customer2>()
            .sqlSessionFactory(sqlSessionFactory)
            .pageSize(CHUNK_SIZE)
            .queryId("com.schooldevops.springbatch.batchsample.jobs.selectCustomers")
            .build()
    }


    @Bean
    fun customerCursorFlatFileItemWriter(): FlatFileItemWriter<Customer2> {
        return FlatFileItemWriterBuilder<Customer2>()
            .name("customerCursorFlatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new_v4.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .build()
    }


    @Bean
    @Throws(Exception::class)
    fun customerJdbcCursorStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info("------------------ Init customerJdbcCursorStep -----------------")

        return StepBuilder("customerJdbcCursorStep", jobRepository)
            .chunk<Customer2, Customer2>(CHUNK_SIZE, transactionManager)
            .reader(myBatisItemReader())
            .processor(CustomerItemProcessor())
            .writer(customerCursorFlatFileItemWriter())
            .build()
    }

    @Bean
    fun customerJdbcCursorPagingJob(customerJdbcCursorStep: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init customerJdbcCursorPagingJob -----------------")
        return JobBuilder(MYBATIS_PAGING_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(customerJdbcCursorStep)
            .build()
    }
}