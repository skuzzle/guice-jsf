package de.skuzzle.blog;

import javax.faces.bean.ManagedBean;

@ManagedBean
@ViewScoped
public class HelloWorldBean {

    public HelloWorldBean() {
        System.out.println("View Scoped bean recreated");
    }

    public String getHelloWorld() {
        return "Hello World";
    }
}
