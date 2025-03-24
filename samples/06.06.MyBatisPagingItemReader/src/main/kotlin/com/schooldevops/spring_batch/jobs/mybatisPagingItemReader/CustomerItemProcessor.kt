package com.schooldevops.spring_batch.jobs.mybatisPagingItemReader

import com.schooldevops.spring_batch.jobs.data.Customer2
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor

class CustomerItemProcessor : ItemProcessor<Customer2, Customer2> {
    private val log: Logger = LoggerFactory.getLogger(CustomerItemProcessor::class.java)

    override fun process(item: Customer2): Customer2 {
        log.info("Processing customer: {}", item.name)
        return item
    }
}