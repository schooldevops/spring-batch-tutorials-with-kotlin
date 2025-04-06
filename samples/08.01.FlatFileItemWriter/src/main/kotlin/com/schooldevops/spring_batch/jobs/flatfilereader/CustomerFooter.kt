package com.schooldevops.spring_batch.jobs.flatfilereader

import org.springframework.batch.item.file.FlatFileFooterCallback
import java.io.IOException
import java.io.Writer
import java.util.concurrent.ConcurrentHashMap


class CustomerFooter(val aggregateCustomers: ConcurrentHashMap<String, Int>) : FlatFileFooterCallback {

    @Throws(IOException::class)
    override fun writeFooter(writer: Writer) {
        writer.write("총 고객 수: " + aggregateCustomers["TOTAL_CUSTOMERS"])
        writer.write(System.lineSeparator())
        writer.write("총 나이: " + aggregateCustomers["TOTAL_AGES"])
    }
}