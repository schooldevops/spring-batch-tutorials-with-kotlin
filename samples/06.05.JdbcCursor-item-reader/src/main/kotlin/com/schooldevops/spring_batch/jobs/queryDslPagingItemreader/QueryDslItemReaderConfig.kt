package com.schooldevops.spring_batch.jobs.queryDslPagingItemreader;

import com.schooldevops.spring_batch.jobs.jdbcPagingItemreader.JpaPagingItemJobConfig
import com.schooldevops.spring_batch.jobs.data.Customer2
import com.schooldevops.spring_batch.jobs.data.QCustomer2
import jakarta.persistence.EntityManagerFactory
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(name = ["spring.batch.job.name"], havingValue = QueryDSLPagingReaderJobConfig.QUERYDSL_PAGING_CHUNK_JOB)
class QueryDSLPagingReaderJobConfig {

    private val log: Logger = LoggerFactory.getLogger(JpaPagingItemJobConfig::class.java)

    /**
     * CHUNK 크기를 지정한다.
     */
    companion object {

        const val CHUNK_SIZE: Int = 2
        const val ENCODING: String = "UTF-8"
        const val QUERYDSL_PAGING_CHUNK_JOB: String = "QUERYDSL_PAGING_CHUNK_JOB"
    }

    @Autowired
    lateinit var dataSource: DataSource

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

//    @Bean
//    public QuerydslPagingItemReader<Customer> customerQuerydslPagingItemReader() throws Exception {
//
//        Function<JPAQueryFactory, JPAQuery<Customer>> query = jpaQueryFactory -> jpaQueryFactory.select(QCustomer.customer).from(QCustomer.customer);
//
//        return new QuerydslPagingItemReader<>("customerQuerydslPagingItemReader", entityManagerFactory, query, CHUNK_SIZE, false);
//    }

    // QCustomer2 클래스를 자동 생성하기 위한 IntelliJ 설정 방법:
    // 1. build.gradle.kts에 QueryDSL 관련 의존성이 이미 추가되어 있는지 확인
    // 2. IntelliJ IDEA에서 Annotation Processor 활성화:
    //    - File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors
    //    - "Enable annotation processing" 체크박스 선택
    // 3. Entity 클래스(Customer2)에 @Entity 어노테이션이 있는지 확인
    // 4. 프로젝트 빌드: Build > Rebuild Project
    // 5. 빌드 후 generated 디렉토리에 QCustomer2 클래스가 생성됨
    @Bean
    fun customerQuerydslPagingItemReader(): QuerydslPagingItemReader<Customer2> {
        return QuerydslPagingItemReaderBuilder<Customer2>()
            .name("customerQuerydslPagingItemReader")
            .entityManagerFactory(entityManagerFactory)
            .chunkSize(CHUNK_SIZE)
            .querySupplier { jpaQueryFactory ->
                jpaQueryFactory.select(QCustomer2.customer2)
                    .from(QCustomer2.customer2)
                    .where(QCustomer2.customer2.age.gt(20))
            }
            .build()

    }

    @Bean
    fun customerQuerydslFlatFileItemWriter(): FlatFileItemWriter<Customer2> {
        return FlatFileItemWriterBuilder<Customer2>()
            .name("customerQuerydslFlatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new_v2.csv"))
            .encoding(ENCODING)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .build()
    }

    @Bean
    fun customerQuerydslPagingStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info("------------------ Init customerQuerydslPagingStep -----------------")

        return StepBuilder("customerJpaPagingStep", jobRepository)
            .chunk<Customer2, Customer2>(CHUNK_SIZE, transactionManager)
            .reader(customerQuerydslPagingItemReader())
            .writer(customerQuerydslFlatFileItemWriter())
            .build()
    }

    @Bean
    fun customerQueryDslPagingJob(customerQuerydslPagingStep: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init customerJpaPagingJob -----------------")
        return JobBuilder(QUERYDSL_PAGING_CHUNK_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(customerQuerydslPagingStep)
            .build()
    }
}