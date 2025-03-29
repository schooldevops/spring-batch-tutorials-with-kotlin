package com.schooldevops.spring_batch.jobs.mybatisPagingItemReader

import com.schooldevops.spring_batch.jobs.data.Customer2
import org.springframework.batch.item.ItemProcessor
import java.util.*

/**
 * 이름 성별을 소문자로 변경하는 ItemProcessor
 */
class LowerCaseItemProcessor : ItemProcessor<Customer2, Customer2> {
    override fun process(item: Customer2): Customer2? {
        item.name = item.name.lowercase(Locale.getDefault())
        item.gender = item.gender.lowercase(Locale.getDefault())
        return item
    }

}