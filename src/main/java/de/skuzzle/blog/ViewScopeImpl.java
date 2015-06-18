package de.skuzzle.blog;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.servlet.SessionScoped;

/**
 * This is a copy of guice's implementation of {@link SessionScoped} with
 * adjustments to place objects into the current view scope.
 *
 * @author Simon Taddiken
 */
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
        };
    }

    @Override
    public final String toString() {
        return "Custom.ViewScope";
    }

}
