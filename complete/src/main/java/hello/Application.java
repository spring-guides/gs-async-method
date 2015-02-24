package hello;

import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Application implements CommandLineRunner {

    @Autowired
    FacebookLookupService facebookLookupService;

    @Override
    public void run(String... args) throws Exception {
        // Start the clock
        long start = System.currentTimeMillis();

        // Kick of multiple, asynchronous lookups
        Future<Page> page1 = facebookLookupService.findPage("PivotalSoftware");
        Future<Page> page2 = facebookLookupService.findPage("CloudFoundry");
        Future<Page> page3 = facebookLookupService.findPage("SpringFramework");

        // Wait until they are all done
        while (!(page1.isDone() && page2.isDone() && page3.isDone())) {
            Thread.sleep(10); //10-millisecond pause between each check
        }

        // Print results, including elapsed time
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
        System.out.println(page1.get());
        System.out.println(page2.get());
        System.out.println(page3.get());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
