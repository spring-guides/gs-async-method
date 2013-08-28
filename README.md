This guide walks you through the steps to create asynchronous queries to Facebook. The focus is on the asynchronous part, a feature often used when scaling services.

What you'll build
-----------------

You'll build a lookup service that queries Facebook pages and retrieve data through Facebook's Graph API. One approach to scaling services is to run expensive jobs in the background and wait for the results using Java's [`Future`][] interface. Java's `Future` is essentially a container oused to hold the potential results. It gives you methods to let you poll if the results have arrived yet, and when they have, the ability to access the results.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]
 - You can also import the code from this guide as well as view the web page directly into [Spring Tool Suite (STS)][gs-sts] and work your way through it from there.

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi
[gs-sts]: /guides/gs/sts


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-async-method.git`
 - cd into `gs-async-method/initial`.
 - Jump ahead to [Create a representation of a page](#initial).

**When you're finished**, you can check your results against the code in `gs-async-method/complete`.
[zip]: https://github.com/spring-guides/gs-async-method/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/spring-guides/gs-async-method/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/spring-guides/gs-async-method/blob/master/initial/pom.xml). If you are using [Spring Tool Suite (STS)][gs-sts], you can import the guide directly.

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-async-method'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter:0.5.0.BUILD-SNAPSHOT")
    compile("org.springframework:spring-web:4.0.0.M2")
    compile("com.fasterxml.jackson.core:jackson-databind:2.2.2")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```
    
[gs-sts]: /guides/gs/sts    

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/).


<a name="initial"></a>
Create a representation of a page
----------------------------------

Before you can create a Facebook lookup service, you need to define a representation for the data you'll retrieve through Facebook's Graph API.

To model the page representation, you create a resource representation class. Provide a plain old Java object with fields, constructors, and accessors for the `id` and `content` data:

`src/main/java/hello/Page.java`
```java
package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Page {

    private String name;
    private String website;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "Page [name=" + name + ", website=" + website + "]";
    }

}
```

Spring uses the [Jackson JSON][jackson] library to convert Facebook's JSON response into a `Page` object. The `@JsonIgnoreProperties` annotation signals Spring to ignore any attributes not listed in the class. This makes it easy to make REST calls and produce domain objects.

In this guide, we are only grabbing the `id` and the `content` for demonstration purposes.


Create a Facebook lookup service
--------------------------------

Next you need to create a service that queries Facebook to find pages.

`src/main/java/hello/FacebookLookupService.java`
```java
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
```
    
The `FacebookLookupService` class uses Spring's `RestTemplate` to invoke a remote REST point (graph.facebook.com), and then convert the answer into a `Page` object.

The class is marked with the `@Service` annotation, making it a candidate for Spring's component scanning to detect it and add it to the [application context][u-application-context].

The `findPage` method is flagged with Spring's `@Async` annotation, indicating it will run on a separate thread. The method's return type is [`Future<Page>`][`Future`] instead of `Page`, a requirement for any asynchronous service. This code uses the concrete implementation of `AsyncResult` to wrap the results of the Facebook query.

> **Note:** Creating a local instance of the `FacebookLookupService` class does NOT allow the `findPage` method to run asynchronously. It must be created inside a `@Configuration` class or picked up by `@ComponentScan`.

The timing for Facebook's Graph API can vary widely. To demonstrate the benefits later in this guide, an extra delay of one second has been added to this service.

Make the application executable
-------------------------------

To run a sample, you can create an executable jar. Spring's `@Async` annotation works with web apps, but you don't need all the extra steps of setting up a web container to see its benefits.

### Create an Application class

`src/main/java/hello/Application.java`
```java
package hello;

import java.util.concurrent.Future;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableAutoConfiguration
@ComponentScan
public class Application implements CommandLineRunner {

    @Autowired
    FacebookLookupService facebookLookupService;

    @Override
    public void run(String... args) throws Exception {
        // Start the clock
        long start = System.currentTimeMillis();

        // Kick of multiple, asynchronous lookups
        Future<Page> page1 = facebookLookupService.findPage("GoPivotal");
        Future<Page> page2 = facebookLookupService.findPage("SpringSource");
        Future<Page> page3 = facebookLookupService.findPage("CloudFoundry");
        Future<Page> page4 = facebookLookupService.findPage("SpringFramework");

        // Wait until they are all done
        while (!(page1.isDone() && page2.isDone() && page3.isDone() && page4.isDone())) {
            Thread.sleep(10); //millisecond pause between each check
        }

        // Print results, including elapsed time
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
        System.out.println(page1.get());
        System.out.println(page2.get());
        System.out.println(page3.get());
        System.out.println(page4.get());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the [Spring application context][u-application-context].

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `FacebookLookupService`, because it is marked with `@Service`, which in turn is a kind of `@Component` annotation.

The [`@EnableAsync`][] annotation switches on Spring's ability to run `@Async` methods in a background thread pool.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, it looks for any class that implements the `CommandLineRunner` interface and invokes its `run()` method. In this case, it runs the demo code for this guide.

### Build an executable JAR
Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/spring-guides/gs-async-method/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.BUILD-SNAPSHOT")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of `build.gradle` [right here]((https://github.com/spring-guides/gs-async-method/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-async-method-0.1.0.jar
```

If you are using Maven, you can run the JAR by typing:

```sh
$ java -jar target/gs-async-method-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the service
-------------------
If you are using Gradle, you can run your service at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-async-method-0.1.0.jar
```

> **Note:** If you are using Maven, you can run your service by typing `mvn clean package && java -jar target/gs-async-method-0.1.0.jar`.


Logging output is displayed, showing each query to Facebook. Each `Future` result is monitored until available, so when they are all done, the log will print out the results along with the total amount of elapsed time.

```
Looking up GoPivotal
Looking up CloudFoundry
Looking up SpringFramework
Looking up SpringSource
Elapsed time: 1633
Page [name=Pivotal, website=http://www.gopivotal.com]
Page [name=SpringSource, website=http://www.springsource.com]
Page [name=Cloud Foundry, website=http://www.cloudfoundry.com]
Page [name=Spring Framework, website=null]
```

To compare how long this takes without the asynchronous feature, try commenting out the `@Async` annotation and run the service again. The total elapsed time should increase noticeably because each query takes at least a second.

Essentially, the longer the task takes and the more tasks are invoked simultaneously, the more benefit you will see with making things asynchronous. The trade off is handling the `Future` interface. It adds a layer of indirection because you are no longer dealing directly with the results, but must instead poll for them. If multiple method calls were previously chained together in a synchronous fashion, converting to an asynchronous approach may require synchronizing results. But this extra work may be necessary if asynchronous method calls solves a critical scaling issue.


Summary
-------

Congratulations! You've just developed an asynchronous service that lets you scale multiple calls at once.


[`Future`]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html
[`@EnableAsync`]: http://static.springsource.org/spring/docs/4.0.x/spring-framework-reference/html/scheduling.html#scheduling-annotation-support-async
[jackson]: http://wiki.fasterxml.com/JacksonHome
[u-application-context]: /understanding/application-context
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
