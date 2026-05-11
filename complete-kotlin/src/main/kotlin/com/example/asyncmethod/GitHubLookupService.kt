package com.example.asyncmethod

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody
import java.util.concurrent.CompletableFuture

@Service
class GitHubLookupService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    @Async
    fun findUser(user: String): CompletableFuture<User> {
        logger.info("Looking up $user")
        val results = restClient.get()
            .uri("https://api.github.com/users/$user")
            .retrieve()
            .requiredBody<User>()
        // Artificial delay of 1s for demonstration purposes
        Thread.sleep(1000L)
        return CompletableFuture.completedFuture(results)
    }
}