
Getting Started: Creating Asynchronous Methods
==============================================

What you'll build
-----------------

This guide walks you through the steps to create an asynchronous query to Facebook. This will help you scale certain services to run in the background using Java's [`Future`][] interface.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-async-method.git`
 - cd into `gs-async-method/initial`.
 - Jump ahead to [Create a representation of a Page](#initial).

**When you're finished**, you can check your results against the code in `gs-async-method/complete`.
[zip]: https://github.com/springframework-meta/gs-async-method/archive/master.zip


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Maven](/guides/gs/maven/content) or [Building Java Projects with Gradle](/guides/gs/gradle/content).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-async-method</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.zero</groupId>
        <artifactId>spring-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

    <dependencies>
        <dependency>
        	<groupId>org.springframework.zero</groupId>
        	<artifactId>spring-starter</artifactId>
        </dependency>
        <dependency>
        	<groupId>org.springframework</groupId>
        	<artifactId>spring-web</artifactId>
        </dependency>
        <dependency>
        	<groupId>com.fasterxml.jackson.core</groupId>
        	<artifactId>jackson-databind</artifactId>
        </dependency>
    </dependencies>

    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

TODO: mention that we're using Spring Bootstrap's [_starter POMs_](../gs-bootstrap-starter) here.

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.


<a name="initial"></a>
Create a representation of a Page
----------------------------------

Now that you've set up the project and build system, you can create a Facebook lookup service. Before you can do that, you need to define a representation for the data you'll retrieve through Facebook's Graph API.

To model the page representation, you create a resource representation class. Provide a plain old java object with fields, constructors, and accessors for the `id` and `content` data:

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

Spring uses the [Jackson JSON][jackson] library to convert Facebook's JSON response into a `Page` object. The `@JsonIgnoreProperties` annotation signals Spring to ignore any attributes not listed in the class. This makes is super easy to make REST calls and produce domain objects.


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
    
The `FacebookLookupService` uses Spring's `RestTemplate` to invoke a remote REST point (graph.facebook.com), and then convert the answer into a `Page` object.

The class is marked with the `@Service` annotation, making it a candidate for Spring's component scanning to detect it and add it to the [application context][u-application-context].

The `findPage` method is flagged with Spring's `@Async` annotation, indicating it will be run on a separate thread. It's return type is [`Future<Page>`][`Future`] instead of `Page`, a requirement for any asynchronous service. This code uses the concrete implementation of `AsyncResult` to wrap the results of the Facebook query.

> **Note:** Creating a local instance of this class will NOT allow this method to run asynchronously. It must be created inside a `@Configuration` class or picked up by `@ComponentScan`.

The timing for Facebook's Graph API can vary widely. To demonstrate the benefits later in this guide, an extra delay of one second has been added to this service.

Make the application executable
-------------------------------

To run a sample, you can create an executable jar. Spring's `@Async` annotation works with web apps, but you don't need all the extra steps of setting up a web container to see it's benefits.

### Create an Application class

`src/main/java/hello/Application.java`
```java
package hello;

import java.util.concurrent.Future;

import org.springframework.autoconfigure.EnableAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.bootstrap.CommandLineRunner;
import org.springframework.bootstrap.SpringApplication;
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

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.zero</groupId>
                <artifactId>spring-package-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run the jar with `java -jar`.

The [Spring Package maven plugin][spring-package-maven-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following to produce a single executable JAR file containing all necessary dependency classes and resources:

    mvn package

[spring-package-maven-plugin]: https://github.com/SpringSource/spring-zero/tree/master/spring-package-maven-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/content) instead.

Run the application
-------------------
Run your application with `java -jar` at the command line:

    java -jar target/gs-async-method-0.1.0.jar



Logging output is displayed. It shows each query to Facebook being kicked off. Then it monitors each `Future` so when they are all done, it will print out the results along with the total amount of elapsed time.

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

To compare how long this would take without the asynchronous feature, try commenting out the `@Async` annotation and run it again. The total elapsed time should increase noticeably since each query will take at least a second.

Essentially, the longer the task takes and the more tasks are invoked simultaneously, the more benefit you will see with making things asynchronous. The trade off is having to handle the `Future` interface, and possible synchronizing on the output of multiple calls, especially if one is dependent on another.


Summary
-------

Congratulations! You've just developed an asynchronous service and seen how it lets you scale multiple calls at once.


[`Future`]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html
[`@EnableAsync`]: http://static.springsource.org/spring/docs/4.0.x/spring-framework-reference/html/scheduling.html#scheduling-annotation-support-async
[jackson]: http://wiki.fasterxml.com/JacksonHome
[u-application-context]: /understanding/application-context
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
