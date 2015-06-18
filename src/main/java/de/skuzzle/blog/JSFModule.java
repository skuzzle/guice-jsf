package de.skuzzle.blog;

import javax.faces.webapp.FacesServlet;

import org.apache.myfaces.webapp.StartupServletContextListener;

import com.google.inject.servlet.ServletModule;

public class JSFModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bindScope(ViewScoped.class, new ViewScopeImpl());
        bind(StartupServletContextListener.class);
        bind(FacesServlet.class).asEagerSingleton();

        serve("/faces/", "/faces/*", "*.jsf", "*.faces", "*.xhtml")
                .with(FacesHttpServlet.class);
    }
}
