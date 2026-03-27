package com.example.asyncmethod

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@SpringBootApplication
@EnableAsync
class AsyncMethodApplication {

    @Bean
    fun taskExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 2
        maxPoolSize = 2
        queueCapacity = 500
        setThreadNamePrefix("GithubLookup-")
        initialize()
    }
}

fun main(args: Array<String>) {
    runApplication<AsyncMethodApplication>(*args).close()
}