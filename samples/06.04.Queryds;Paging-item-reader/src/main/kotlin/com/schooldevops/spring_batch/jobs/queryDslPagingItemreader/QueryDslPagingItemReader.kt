package com.schooldevops.spring_batch.jobs.queryDslPagingItemreader

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.util.ClassUtils
import org.springframework.util.CollectionUtils
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function

class QuerydslPagingItemReader<T>(
    entityManagerFactory: EntityManagerFactory,
    private val querySupplier: Function<JPAQueryFactory, JPAQuery<T>>,
    chunkSize: Int
) : AbstractPagingItemReader<T>() {

    private var em: EntityManager
    private var alwaysReadFromZero: Boolean

    init {
        this.setPageSize(chunkSize)
        this.setName(ClassUtils.getShortName(QuerydslPagingItemReader::class.java))
        this.em = entityManagerFactory.createEntityManager()
        this.alwaysReadFromZero = false
    }

    constructor(
        name: String,
        entityManagerFactory: EntityManagerFactory,
        querySupplier: Function<JPAQueryFactory, JPAQuery<T>>,
        chunkSize: Int,
        alwaysReadFromZero: Boolean
    ) : this(entityManagerFactory, querySupplier, chunkSize) {
        this.setName(name)
        this.alwaysReadFromZero = alwaysReadFromZero
    }

    @Throws(Exception::class)
    override fun doClose() {
        em.close()
        super.doClose()
    }

    override fun doReadPage() {
        initQueryResult()

        val jpaQueryFactory = JPAQueryFactory(em)
        var offset: Long = 0
        if (!alwaysReadFromZero) {
            offset = (page * pageSize).toLong()
        }

        val query = querySupplier.apply(jpaQueryFactory).offset(offset).limit(pageSize.toLong())

        val queryResult = query.fetch()
        for (entity in queryResult) {
            em.detach(entity)
            results.add(entity)
        }
    }

    private fun initQueryResult() {
        if (CollectionUtils.isEmpty(results)) {
            results = CopyOnWriteArrayList()
        } else {
            results.clear()
        }
    }
}