package com.bosch.test.cnf;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
public class CommandManager implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public void processParallel() {
        IntStream.range(0, 10).parallel().forEach(i -> {
            try {
                log(i, "1. Create JAXBContext with classloader " + this.applicationContext.getClassLoader());
                createJAXBContext(); // Fails in worker threads
                log(i, "2. JAXBContext created");
            } catch (Exception e) {
                log(i, "2. ERROR creating JAXBContext: " + e.getMessage());
                try {
                    // Succeeds in worker threads
                    Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory");
                    log(i, "3. SUCCESS Class.forName(\"org.glassfish.jaxb.runtime.v2.ContextFactory\")");
                } catch (ClassNotFoundException ex) {
                    log(i, "3. FAILED Class.forName(\"org.glassfish.jaxb.runtime.v2.ContextFactory\")");
                }
            }
        });
    }

    private void log(int i, String message) {
        System.out.printf("[%s] - [%s] - %s%n", i, Thread.currentThread().getName(), message);
    }

    protected JAXBContext createJAXBContext() {
        return this.applicationContext.getBean("jaxbContext", JAXBContext.class);
    }

    @PostConstruct
    public void init() {
        this.processParallel();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
