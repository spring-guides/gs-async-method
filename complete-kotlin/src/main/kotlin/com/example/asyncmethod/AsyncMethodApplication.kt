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
	fun taskExecutor(): Executor {
		val executor = ThreadPoolTaskExecutor()
		executor.corePoolSize = 2
		executor.maxPoolSize = 2
        executor.queueCapacity = 500
		executor.setThreadNamePrefix("GithubLookup-")
		executor.initialize()
		return executor
	}
}

fun main(args: Array<String>) {
	runApplication<AsyncMethodApplication>(*args).use { it.close() }
}
