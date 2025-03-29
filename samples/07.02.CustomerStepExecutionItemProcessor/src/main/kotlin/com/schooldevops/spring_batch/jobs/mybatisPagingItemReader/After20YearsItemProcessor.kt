package com.schooldevops.spring_batch.jobs.mybatisPagingItemReader

import com.schooldevops.spring_batch.jobs.data.Customer2
import org.springframework.batch.item.ItemProcessor


/**
 * 나이에 20년을 더하는 ItemProcessor
 */
class After20YearsItemProcessor : ItemProcessor<Customer2, Customer2> {
    @Throws(Exception::class)
    override fun process(item: Customer2): Customer2 {
        item.age += 20
        return item
    }
}