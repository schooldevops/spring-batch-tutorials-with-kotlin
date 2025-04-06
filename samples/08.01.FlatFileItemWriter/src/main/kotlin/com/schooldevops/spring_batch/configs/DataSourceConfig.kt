package com.schooldevops.spring_batch.configs

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import javax.sql.DataSource

@Configuration
@PropertySource("classpath:application.yaml")
class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    fun hikariConfig(): HikariConfig {
        return HikariConfig()
    }

    @Bean
    fun dataSource(): DataSource {
        return HikariDataSource(hikariConfig())
    }

    @Bean
    fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactoryBean {
        val mybatisConfig = PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml")
        val sqlSession = SqlSessionFactoryBean()
        sqlSession.setDataSource(dataSource)
        val resources = PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml")
        sqlSession.setMapperLocations(*resources)
        sqlSession.setConfigLocation(mybatisConfig)

        return sqlSession

    }
}