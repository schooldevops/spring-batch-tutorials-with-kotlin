package com.schooldevops.spring_batch.jobs.jdbcCursorItemReader

import com.schooldevops.spring_batch.jobs.flatfilereader.Customer
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class CustomerRowMapper: RowMapper<Customer> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Customer? {
        val customer = Customer()
        customer.name = rs.getString("name")
        customer.age = rs.getInt("age")
        customer.gender = rs.getString("gender")

        return customer
    }
}