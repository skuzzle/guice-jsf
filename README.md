## JSF with Apache my-faces and Google Guice

This article describes how to set up a JSF web application using Apache my-faces and Google Guice for dependency injection. Not only will we use Guice for resolving managed beans, we also use Guice's Servlet extension to partly replace the `web.xml` with Guice Modules and to have Guice run the whole web stack.

### Dependencies
We start by defining some minimal required dependencies for our web application. Those 
include the `my-faces` API and implementation as well as `guice` and its servlet 
extension.

```xml
<!-- Web things -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>servlet-api</artifactId>
    <version>3.0-alpha-1</version>
    <scope>provided</scope>
</dependency>
<dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>jstl</artifactId>
      <version>1.2</version>
      <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.apache.myfaces.core</groupId>
    <artifactId>myfaces-api</artifactId>
    <version>2.2.8</version>
</dependency>
<dependency>
    <groupId>org.apache.myfaces.core</groupId>
    <artifactId>myfaces-impl</artifactId>
    <version>2.2.8</version>
</dependency>

<!-- Guice -->
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>4.0-beta5</version>
</dependency>
<dependency>
    <groupId>com.google.inject.extensions</groupId>
    <artifactId>guice-servlet</artifactId>
    <version>4.0-beta5</version>
</dependency>
```

### Setting up Guice Servlet
Guice servlet provides a complete filter- and servlet stack, allowing Filters and Servlets to be created by Guice. This allows to inject any dependencies into those Filters or Servlets ([Guice Servlet Wiki](https://github.com/google/guice/wiki/Servlets)).

The first step is to implement a `GuiceServletContextListener` which is responsible for creating the `Injector`:

```java
package de.skuzzle.blog;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class MyGuiceServletContextListener extends
        GuiceServletContextListener {

    private final Injector injector;
    
    public MyGuiceServletContextListener() {
        this.injector = Guice.createInjector(...);
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }
}
```

The second step is to add the listener and the `GuiceFilter` to your `web.xml`:

```xml
<listener>
    <listener-class>de.skuzzle.blog.MyServletContextListener</listener-class>
</listener>

<filter>
    <filter-name>guiceFilter</filter-name>
    <filter-class>com.google.inject.servlet.GuiceFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>guiceFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

That's it. You can now create `ServletModule`s register Filters and Servlets that shall be executed by Guice. 

### Binding the FacesServlet
The `FacesServlet` is the main entry point for JSF and we want it to be created and routed be Guice. In a perfect world, this would be as easy as adding a single statement to our ServletModule:

```java
public class JSFModule extends ServletModule {

    @Override
    public void configureServlets() {
        serve("/faces/", "/faces/*", "*.jsf", "*.faces", "*.xhtml").with(
                FacesServlet.class);
    }
}
```
Unfortunately, this code does not compile and that's not even the hardest part to fix.

Guice only allows classes which extend `HttpServlet` to be served like that. The `FacesServlet` however only implements the `Servlet` interface. Luckily we can easily fix this by wrapping the FacesServlet into a HttpServlet:

```java
@Singleton
public class FacesHttpServlet extends HttpServlet {

    private final Servlet wrapped;
    
    @Inject
    public FacesHttpServlet(FacesServlet facesServlet) {
        this.wrapped = facesServlet;
   }
   
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.wrapped.init(config);
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.wrapped.getServletConfig();
    }

    @Override
    public String getServletInfo() {
        return this.wrapped.getServletInfo();
    }

    @Override
    public void destroy() {
        super.destroy();
        this.wrapped.destroy();
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp)
            throws ServletException, IOException {
        this.wrapped.service(req, resp);
    }
}
```

Please note the `@Singleton` annotation. This is mandatory as Guice requires all Servlets to be singletons.

```java
@Override 
public void configureServlets() {
    // Just to be safe, bin the FacesServlet for injecting it 
    // into our FacesHttpServlet
    bind(FacesServlet.class).asEagerSingleton();
    serve("/faces/", "/faces/*", "*.jsf", "*.faces", "*.xhtml").with(
            FacesHttpServlet.class);
}
```

By now our application _should_ be ready to answer requests using the JSF servlet. In fact, a little testing reveals it actually does. But something is still strange: a little debugging teaches us that our `FacesHttpServlet` never once gets instantiated. The problem we are facing here is, that JSF uses the Java Service Provider to inject a ServletContainerInitializer which is responsible for setting up the FacesServlet automatically (namely the `MyFacesContainerInitializer`). We can prevent it from doing so by adding the following context-param to the `web.xml`:

```xml
<context-param>
    <param-name>org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE</param-name>
    <param-value>true</param-value>
</context-param>
```

From now on, the complete web stack with filtering and serving is managed by Guice. All your filters and further servlets can now be created, injected and routed by Guice. Of course this includes Guice's own `PersistFilter` to use JPA in your application.

### Using Guice for creating beans
We haven't even talked about the biggest advantage of having your web application run by Guice: you can use its full range of dependency injection features for creating your managed beans. Achieving this is one the easier parts: you need to tell JSF it should use the `GuiceResolver` as _el-resolver_ and you need to tell the GuiceResolver how to find the Injector that you created in your ServletContextListener. 

Place the following lines into your `faces-config.xml`:
```xml
<application>
    <!-- Use Guice as Dependency Injector -->
    <el-resolver>org.apache.myfaces.el.unified.resolver.GuiceResolver</el-resolver>
</application>
```

Now, again, we need to modify our ServletContextListener to place our Injector into the current `ServletContext` so the GuiceResolver will find it.

```java
@Override
public void contextInitialized(ServletContextEvent event) {
    // this sets up guice
    super.contextInitialized(event);

    // NEW: place injector into the context for the GuiceResolver to find
    event.getServletContext().setAttribute(GuiceResolver.KEY, this.injector);
}
```

That's it!

### ViewScoped beans?
One last thing: having your beans created and injected by Guice implies they will be scoped by Guice too and Guice does know nothing about `ViewScope`. Luckily it is pretty easy to implement this scope for Guice. The first thing is to create a custom `ViewScope` annotation (sadly we can't use the original one from JSF).

```java
package de.skuzzle.blog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface ViewScoped {}
```

View scoped beans are placed into the UIRoot's `viewMap` just like session scoped beans are placed into the current session's attribute map. So the following is a simple copy of Guice's own `SessionScope` implementation with slight modifications to access the view map:

```java
package de.skuzzle.blog;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;

final class ViewScopeImpl implements Scope {

    private enum NullObject {
        INSTANCE
    }

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        final String name = key.toString();
        return new Provider<T>() {

            @Override
            public T get() {
                final FacesContext facesContext = FacesContext.getCurrentInstance();
                final UIViewRoot viewRoot = facesContext.getViewRoot();

                // fallback if no view is active
                if (viewRoot == null) {
                    return unscoped.get();
                }

                final Map<String, Object> viewMap = viewRoot.getViewMap(true);
                synchronized (viewMap) {
                    Object obj = viewMap.get(name);
                    if (obj == NullObject.INSTANCE) {
                        return null;
                    }

                    @SuppressWarnings("unchecked")
                    T t = (T) obj;
                    if (t == null) {
                        t = unscoped.get();
                        if (!Scopes.isCircularProxy(t)) {
                            viewRoot.getViewMap().put(name, t == null
                                    ? NullObject.INSTANCE
                                    : t);
                        }
                    }
                    return t;
                }
            }

            @Override
            public String toString() {
                return String.format("%s[%s]", unscoped, ViewScopeImpl.this);
            }

    }

    @Override
    public final String toString() {
        return "Custom.ViewScope";
    }
}
```

The last thing is to bind  this scope in a Module or ServletModule:

```java
@Override
public void configure() {
    bindScope(ViewScoped.class, new ViewScopeImpl());
}
```

