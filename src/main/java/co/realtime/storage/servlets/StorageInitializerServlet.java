package co.realtime.storage.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import co.realtime.storage.connection.StorageRefFactorySingleton;

/**
 * The Class StorageInitializerServlet.
 */
@WebServlet(name = "StorageInitializerServlet")
public class StorageInitializerServlet extends HttpServlet {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
        final String storageConfigurationsPath = getServletConfig().getInitParameter("storage.configurations.path");
        StorageRefFactorySingleton.INSTANCE.reloadConfigurations(storageConfigurationsPath);
    }

}
