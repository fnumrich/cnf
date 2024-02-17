Small Spring Boot 3.2 application to test and visualize the classloading problems that we're currently facing.

### Steps to reproduce

`gradle clean build`

`java -jar build/libs/cnf-0.0.1-SNAPSHOT.jar`

#### Hint

The following problem ...

- does not occur when running the application within an IDE (e.g. IntelliJ) or via `gradle bootRun`.
- only occurs when running the application via `java -jar build/libs/cnf-0.0.1-SNAPSHOT.jar` and therefore using the Spring Boot classloader.
- is the same for Spring Boot 3.0 and 2.7.

### Output (manually rearranged for better readability)

```
...
[405] - [ForkJoinPool.commonPool-worker-5] - Create JAXBContext with classloader jdk.internal.loader.ClassLoaders$AppClassLoader@531d72ca
[405] - [ForkJoinPool.commonPool-worker-5] - ERROR creating JAXBContext: java.lang.ClassNotFoundException: org.glassfish.jaxb.runtime.v2.ContextFactory
[405] - [ForkJoinPool.commonPool-worker-5] - BUT successful Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory")
...
[328] - [main] - Create JAXBContext with classloader org.springframework.boot.loader.launch.LaunchedClassLoader@2ff4acd0
[328] - [main] - JAXBContext created
...
```
It easy to see that crating the JAXBContext by the main thread works fine while
it fails if done by a worker thread.
It's also obvious that the main thread uses the Spring Boot classloader while the worker threads use the JDK AppClassLoader.

### What is confusing
When creating the JAXBContext fails because of `java.lang.ClassNotFoundException: org.glassfish.jaxb.runtime.v2.ContextFactory` we try to load exactly this class
via `Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory");` which succeeds.
How is this possible?


I'm aware of the Spring Boot issue describing this problem (https://github.com/spring-projects/spring-boot/issues/15737) 
but are quite unsure how to fix this problem in our application.

