package com.example.asyncmethod

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.concurrent.CompletableFuture

@Service
class GitHubLookupService {

	private val restClient: RestClient = RestClient.create()

	@Async
	fun findUser(user: String): CompletableFuture<User> {
		logger.info("Looking up $user")
		val url = "https://api.github.com/users/$user"
		val results = restClient.get()
			.uri(url)
			.retrieve()
			.body<User>()
		// Artificial delay of 1s for demonstration purposes
		Thread.sleep(1000L)
		return CompletableFuture.completedFuture(results)
	}

	companion object {
		private val logger: Logger = LoggerFactory.getLogger(GitHubLookupService::class.java)
	}
}
