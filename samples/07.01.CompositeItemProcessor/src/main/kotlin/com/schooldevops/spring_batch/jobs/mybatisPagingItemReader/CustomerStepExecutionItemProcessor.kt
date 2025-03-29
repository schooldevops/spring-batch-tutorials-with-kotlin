package com.schooldevops.spring_batch.jobs.mybatisPagingItemReader


import com.schooldevops.spring_batch.jobs.data.Customer2
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomerStepExecutionItemProcessor : ItemProcessor<Customer2, Customer2> {
    private var jobExecution: JobExecution? = null

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        jobExecution = stepExecution.jobExecution
    }

    @Throws(Exception::class)
    override fun process(item: Customer2): Customer2 {
        if (null != this.jobExecution) {
            val jobName = jobExecution!!.jobInstance.jobName
            item.name = item.name.lowercase(Locale.getDefault()) + "_(" + jobName + ", " + jobExecution!!.jobId + ")"
        }

        return item
    }
}