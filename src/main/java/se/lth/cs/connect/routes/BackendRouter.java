package se.lth.cs.connect.routes;

import iot.jcypher.database.IDBAccess;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteGroup;

import ro.pippo.core.route.RouteHandler;
import se.lth.cs.connect.Connect;
import se.lth.cs.connect.modules.Database;
import se.lth.cs.connect.routes.Entry.NewEntry;

/**
 * This router provides a db access instance to route handlers:
 *
 *		rc.getLocal("db")
 *
 * Because of this, your own routes must be set up in
 *
 *		setup(PippoSettings conf) {}
 *
 * Some helper methods are also included: ALL, GET, POST
 */
public class BackendRouter extends RouteGroup {
    protected Connect app;

    public BackendRouter(Connect app, String prefix) {
        super(prefix);
        this.app = app;

        // All requests will need a database connection
        ALL(".*", (rc) -> {
            rc.setLocal("db", Database.access());
            rc.next();
        });

        setup(app.getPippoSettings());

        // Make sure to close connection, even if request failed/throwed
        ALL(".*", (rc) -> {
            IDBAccess conn = rc.removeLocal("db");
            if (conn != null)
                conn.close();
        }).runAsFinally();
    }

    /**
     * Get router prefix.
     */
    public String getPrefix() { return ""; }

    protected void setup(PippoSettings conf) {
        /* Setup routes here.*/
    }

    protected Route OPTIONS(String uri, RouteHandler handler){
        Route route = new Route("OPTIONS", uri, handler);
        addRoute(route);
        return route;
    }
}