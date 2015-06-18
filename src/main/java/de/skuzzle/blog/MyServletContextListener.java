package de.skuzzle.blog;

import javax.servlet.ServletContextEvent;

import org.apache.myfaces.el.unified.resolver.GuiceResolver;
import org.apache.myfaces.webapp.StartupServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class MyServletContextListener extends GuiceServletContextListener {

    private final Injector injector;
    private StartupServletContextListener startupListener;

    public MyServletContextListener() {
        this.injector = Guice.createInjector(new JSFModule());
    }

    @Override
    protected Injector getInjector() {
        return this.injector;
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // this sets up guice
        super.contextInitialized(event);

        // Get hold of the StartupServletContextListener and call it after Guice
        // has been initialized. This will perform all necessary JSF setup
        this.startupListener = this.injector.getInstance(
                StartupServletContextListener.class);
        this.startupListener.contextInitialized(event);

        // place injector into the context for the GuiceResolver to find
        event.getServletContext().setAttribute(GuiceResolver.KEY, this.injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        this.startupListener.contextDestroyed(servletContextEvent);
    }

}
