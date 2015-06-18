package de.skuzzle.blog;

import java.io.IOException;

import javax.faces.webapp.FacesServlet;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * Wrapper for the {@link FacesServlet} to provide a {@link HttpServlet} view onto
 * it.
 *
 * @author Simon Taddiken
 */
@Singleton
class FacesHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Servlet facesServlet;

    @Inject
    FacesHttpServlet(FacesServlet facesServlet) {
        this.facesServlet = facesServlet;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.facesServlet.init(config);
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.facesServlet.getServletConfig();
    }

    @Override
    public String getServletInfo() {
        return this.facesServlet.getServletInfo();
    }

    @Override
    public void destroy() {
        super.destroy();
        this.facesServlet.destroy();
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp)
            throws ServletException, IOException {
        this.facesServlet.service(req, resp);
    }
}
