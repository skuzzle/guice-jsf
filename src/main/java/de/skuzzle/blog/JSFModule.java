package de.skuzzle.blog;

import javax.faces.webapp.FacesServlet;

import com.google.inject.servlet.ServletModule;

/**
 * Sets up all needed injections.
 *
 * @author Simon Taddiken
 */
class JSFModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // Bind our own ViewScoped implementation
        bindScope(ViewScoped.class, new ViewScopeImpl());

        // Bind the FacesServlet to be instantiated eagerly as soon as the
        // injector is created.
        bind(FacesServlet.class).asEagerSingleton();

        // Serve these uris with JSF
        serve("/faces/", "/faces/*", "*.jsf", "*.faces", "*.xhtml")
                .with(FacesHttpServlet.class);
    }
}
