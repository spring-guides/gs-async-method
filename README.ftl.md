<#assign project_id="gs-async-method">
This guide walks you through the steps to create asynchronous queries to Facebook.

What you'll build
-----------------

You'll build a lookup service that queries Facebook pages and retrieve data through Facebook's Graph API. You can scale services to run in the background using Java's [`Future`][] interface.

What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide jump_ahead="Create a representation of a page"/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Gradle build file

    <@snippet path="build.gradle" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a representation of a page
----------------------------------

Before you can create a Facebook lookup service, you need to define a representation for the data you'll retrieve through Facebook's Graph API.

To model the page representation, you create a resource representation class. Provide a plain old Java object with fields, constructors, and accessors for the `id` and `content` data:

    <@snippet path="src/main/java/hello/Page.java" prefix="complete"/>

Spring uses the [Jackson JSON][jackson] library to convert Facebook's JSON response into a `Page` object. The `@JsonIgnoreProperties` annotation signals Spring to ignore any attributes not listed in the class. This makes it easy to make REST calls and produce domain objects.


Create a Facebook lookup service
--------------------------------

Next you need to create a service that queries Facebook to find pages.

    <@snippet path="src/main/java/hello/FacebookLookupService.java" prefix="complete"/>
    
The `FacebookLookupService` class uses Spring's `RestTemplate` to invoke a remote REST point (graph.facebook.com), and then convert the answer into a `Page` object.

The class is marked with the `@Service` annotation, making it a candidate for Spring's component scanning to detect it and add it to the [application context][u-application-context].

The `findPage` method is flagged with Spring's `@Async` annotation, indicating it will run on a separate thread. The method's return type is [`Future<Page>`][`Future`] instead of `Page`, a requirement for any asynchronous service. This code uses the concrete implementation of `AsyncResult` to wrap the results of the Facebook query.

> **Note:** Creating a local instance of the `FacebookLookupService` class does NOT allow the `findPage` method to run asynchronously. It must be created inside a `@Configuration` class or picked up by `@ComponentScan`.

The timing for Facebook's Graph API can vary widely. To demonstrate the benefits later in this guide, an extra delay of one second has been added to this service.

Make the application executable
-------------------------------

To run a sample, you can create an executable jar. Spring's `@Async` annotation works with web apps, but you don't need all the extra steps of setting up a web container to see its benefits.

### Create an Application class

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the [Spring application context][u-application-context].

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `FacebookLookupService`, because it is marked with `@Service`, which in turn is a kind of `@Component` annotation.

The [`@EnableAsync`][] annotation switches on Spring's ability to run `@Async` methods in a background thread pool.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, it looks for any class that implements the `CommandLineRunner` interface and invokes its `run()` method. In this case, it runs the demo code for this guide.

<@build_an_executable_jar_subhead/>

<@build_an_executable_jar_with_gradle/>

<@run_the_application_with_gradle/>

Logging output is displayed, showing each query to Facebook. Each `Future` is monitored, so when they are all done, the log will print out the results along with the total amount of elapsed time.

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

Essentially, the longer the task takes and the more tasks are invoked simultaneously, the more benefit you will see with making things asynchronous. The trade off is handling the `Future` interface, and possible synchronizing the output of multiple calls, especially if one is dependent on another.


Summary
-------

Congratulations! You've just developed an asynchronous service that lets you scale multiple calls at once.


[`Future`]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html
[`@EnableAsync`]: http://static.springsource.org/spring/docs/4.0.x/spring-framework-reference/html/scheduling.html#scheduling-annotation-support-async
[jackson]: http://wiki.fasterxml.com/JacksonHome
<@u_application_context/>
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
