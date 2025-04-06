package com.schooldevops.spring_batch.jobs.flatfilereader

import com.schooldevops.spring_batch.jobs.data.Customer
import org.springframework.batch.item.file.transform.LineAggregator

class CustomerLineAggregator : LineAggregator<Customer> {
    override fun aggregate(item: Customer): String {
        return item.name + "," + item.age
    }
}