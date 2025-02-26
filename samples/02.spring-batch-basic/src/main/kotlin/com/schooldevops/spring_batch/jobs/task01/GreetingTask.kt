package com.schooldevops.spring_batch.jobs.task01

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.InitializingBean

class GreetingTask : Tasklet, InitializingBean {
    private val log: Logger = LoggerFactory.getLogger(GreetingTask::class.java)

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        log.info("------------------ Task Execute -----------------");
        log.info("GreetingTask: {}, {}", contribution, chunkContext);
        return RepeatStatus.FINISHED;
    }

    override fun afterPropertiesSet() {
        log.info("----------------- After Properites Sets() --------------");
    }
}