package de.skuzzle.blog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

/**
 * Custom scoping annotation. Sadly we can not use JSF own ViewScoped annotation
 * for binding our scope.
 *
 * @author Simon Taddiken
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface ViewScoped {
}
