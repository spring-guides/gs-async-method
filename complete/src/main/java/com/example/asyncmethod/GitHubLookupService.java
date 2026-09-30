package com.example.asyncmethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

@Service
public class GitHubLookupService {

	private static final Logger logger = LoggerFactory.getLogger(GitHubLookupService.class);

	private final RestClient restClient;

	public GitHubLookupService() {
		this.restClient = RestClient.create();
	}

	@Async
	public CompletableFuture<User> findUser(String user) throws InterruptedException {
		logger.info("Looking up " + user);
		String url = String.format("https://api.github.com/users/%s", user);
		User results = restClient.get()
				.uri(url)
				.retrieve()
				.body(User.class);
		// Artificial delay of 1s for demonstration purposes
		Thread.sleep(1000L);
		return CompletableFuture.completedFuture(results);
	}
}
