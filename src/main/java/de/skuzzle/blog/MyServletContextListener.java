package de.skuzzle.blog;

import javax.servlet.ServletContextEvent;

import org.apache.myfaces.el.unified.resolver.GuiceResolver;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * ServletContextListener for setting up our application wide injector.
 *
 * @author Simon Taddiken
 */
public class MyServletContextListener extends GuiceServletContextListener {

    private final Injector injector;

    public MyServletContextListener() {
        // List all your modules here
        this.injector = Guice.createInjector(new JSFModule());
    }

    @Override
    protected final Injector getInjector() {
        return this.injector;
    }

    @Override
    public final void contextInitialized(ServletContextEvent event) {
        // this sets up guice servlet
        super.contextInitialized(event);

        // place injector into the context for the GuiceResolver to find
        event.getServletContext().setAttribute(GuiceResolver.KEY, this.injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // properly tear down
        super.contextDestroyed(servletContextEvent);
    }

}
