Small Spring Boot 3.2 application to test and visualize the classloading problems that we're currently facing.

### Steps to reproduce

`gradle clean build`

`java -jar build/libs/cnf-0.0.1-SNAPSHOT.jar`

#### Hint

Running this application withing an IDE (e.g. IntelliJ) works fine also running via `gradle bootRun` ...

### Output (manually rearranged for better readability)

```
...
[3] - [ForkJoinPool.commonPool-worker-1] - 1. Create JAXBContext with classloader jdk.internal.loader.ClassLoaders$AppClassLoader@531d72ca
[3] - [ForkJoinPool.commonPool-worker-7] - 2. ERROR creating JAXBContext: Error creating bean with name 'jaxbContext' defined in class path resource [com/bosch/test/cnf/MyConfiguration.class]: Failed to instantiate [jakarta.xml.bind.JAXBContext]: Factory method 'jaxbContext' threw exception with message: jakarta.xml.bind.JAXBException: Implementation of Jakarta XML Binding-API has not been found on module path or classpath.
 - with linked exception:
[java.lang.ClassNotFoundException: org.glassfish.jaxb.runtime.v2.ContextFactory]
[3] - [ForkJoinPool.commonPool-worker-7] - 3. SUCCESS Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory")
...
[6] - [main] - 1. Create JAXBContext with classloader org.springframework.boot.loader.launch.LaunchedClassLoader@2ff4acd0
[6] - [main] - 2. JAXBContext created
...
```
It easy to see that crating the JAXBContext by the main thread works fine while
it fails if done by a worker thread.
It's also obvious that the main thread uses the Spring Boot classloader while the worker threads use the JDK AppClassLoader.

### What is confusing
When creating the JAXBContext fails because of `java.lang.ClassNotFoundException: org.glassfish.jaxb.runtime.v2.ContextFactory` we try to load exactly this class
via `Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory");` which succeeds.
How can this be possible?

I'm aware of the Spring Boot issue describing this problem (https://github.com/spring-projects/spring-boot/issues/15737) 
but are quite unsure how to fix this problem in our application.

