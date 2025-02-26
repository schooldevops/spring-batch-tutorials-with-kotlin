package com.schooldevops.spring_batch.jobs.task01

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BasicTaskJobConfiguration {

    private val log: Logger = LoggerFactory.getLogger(BasicTaskJobConfiguration::class.java)

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager


    fun greetingTasklet(): Tasklet {
        return GreetingTask()
    }

    @Bean
    fun step(jobRepository: JobRepository): Step {
        log.info("------------------ Init myStep -----------------")

        return StepBuilder("myStep", jobRepository)
            .tasklet(greetingTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun myJob(step: Step, jobRepository: JobRepository): Job {
        log.info("------------------ Init myJob -----------------")
        return JobBuilder("MY_JOB", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step)
            .build()
    }
}