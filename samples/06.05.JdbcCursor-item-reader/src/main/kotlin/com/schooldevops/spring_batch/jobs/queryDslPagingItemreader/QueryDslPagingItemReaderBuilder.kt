package com.schooldevops.spring_batch.jobs.queryDslPagingItemreader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManagerFactory
import org.springframework.util.ClassUtils
import java.util.function.Function

class QuerydslPagingItemReaderBuilder<T> {

    private var entityManagerFactory: EntityManagerFactory? = null
    private var querySupplier: Function<JPAQueryFactory, JPAQuery<T>>? = null
    private var chunkSize: Int = 10
    private var name: String? = null
    private var alwaysReadFromZero: Boolean? = null

    fun entityManagerFactory(entityManagerFactory: EntityManagerFactory): QuerydslPagingItemReaderBuilder<T> {
        this.entityManagerFactory = entityManagerFactory
        return this
    }

    fun querySupplier(querySupplier: Function<JPAQueryFactory, JPAQuery<T>>): QuerydslPagingItemReaderBuilder<T> {
        this.querySupplier = querySupplier
        return this
    }

    fun chunkSize(chunkSize: Int): QuerydslPagingItemReaderBuilder<T> {
        this.chunkSize = chunkSize
        return this
    }

    fun name(name: String): QuerydslPagingItemReaderBuilder<T> {
        this.name = name
        return this
    }

    fun alwaysReadFromZero(alwaysReadFromZero: Boolean): QuerydslPagingItemReaderBuilder<T> {
        this.alwaysReadFromZero = alwaysReadFromZero
        return this
    }

    fun build(): QuerydslPagingItemReader<T> {
        if (name == null) {
            this.name = ClassUtils.getShortName(QuerydslPagingItemReader::class.java)
        }

        if (this.entityManagerFactory == null) {
            throw IllegalArgumentException("EntityManagerFactory can not be null.!")
        }

        if (this.querySupplier == null) {
            throw IllegalArgumentException("Function<JPAQueryFactory, JPAQuery<T>> can not be null.!")
        }

        val readFromZero = this.alwaysReadFromZero ?: false

        return QuerydslPagingItemReader(
            name = this.name!!,
            entityManagerFactory = entityManagerFactory!!,
            querySupplier = querySupplier!!,
            chunkSize = chunkSize,
            alwaysReadFromZero = readFromZero
        )
    }
}