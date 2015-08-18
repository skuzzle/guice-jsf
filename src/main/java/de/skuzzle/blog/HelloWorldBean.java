package de.skuzzle.blog;

import javax.faces.bean.ManagedBean;

/**
 * Simple ViewScoped bean.
 *
 * @author Simon Taddiken
 */
@ManagedBean
@ViewScoped
public class HelloWorldBean {

    private int counter;

    public HelloWorldBean() {
    }

    public int getCounter() {
        return this.counter;
    }

    public String increment() {
        ++this.counter;
        return null;
    }
}
