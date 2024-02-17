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
        IntStream.range(0, 500).parallel().forEach(i -> {
            try {
                log(i, "Create JAXBContext with classloader " + this.applicationContext.getClassLoader());
                JAXBContext.newInstance(ObjectFactory.class); // Fails in worker threads
                log(i, "JAXBContext created");
            } catch (Exception e) {
                log(i, "ERROR creating JAXBContext: " + e.getCause());
                try {
                    // Succeeds in worker threads
                    Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory");
                    log(i, "BUT successful Class.forName(\"org.glassfish.jaxb.runtime.v2.ContextFactory\")");
                } catch (ClassNotFoundException ex) {
                    log(i, "FAILED Class.forName(\"org.glassfish.jaxb.runtime.v2.ContextFactory\")");
                }
            }
        });
    }

    private void log(int i, String message) {
        System.out.printf("[%s] - [%s] - %s%n", i, Thread.currentThread().getName(), message);
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
