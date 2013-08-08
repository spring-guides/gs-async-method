package hello;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FacebookLookupService {

    RestTemplate restTemplate = new RestTemplate();

    @Async
    public Future<Page> findPage(String page) throws InterruptedException {
        System.out.println("Looking up " + page);
        Page results = restTemplate.getForObject("http://graph.facebook.com/" + page, Page.class);
        Thread.sleep(1000L);
        return new AsyncResult<Page>(results);
    }

}
