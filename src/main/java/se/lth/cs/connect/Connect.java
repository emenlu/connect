package se.lth.cs.connect;

import se.lth.cs.connect.routes.Account;
import se.lth.cs.connect.routes.Admin;
import se.lth.cs.connect.routes.Entry;
import utils.CleanupUsers;
import utils.CORS;
import se.lth.cs.connect.routes.Collection;
import ro.pippo.core.RuntimeMode;

import se.lth.cs.connect.modules.Database;
import se.lth.cs.connect.modules.MailClient;
import se.lth.cs.connect.modules.Mailman;

import ro.pippo.core.Pippo;
import ro.pippo.core.ExceptionHandler;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Application;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;

/**
 * Default addr and neo4j credentials are read from conf/application.properties
 */
public class Connect extends Application {

	private MailClient mailClient;
	
	public MailClient getMailClient() { return mailClient; }
	
	public void useMailClient(MailClient client) {
		mailClient = client;
		client.configure(getPippoSettings());
	}

	private void setupCORS() {
		if (RuntimeMode.getCurrent().equals(RuntimeMode.DEV)) {
			ALL(".*", new CORS(new String[] { 
				"http://localhost:8181", 
				"https://localhost:8181"
			}));
		}

		ALL(".*", new CORS(new String[] { 
			"http://serpconnect.cs.lth.se", 
			"http://api.serpconnect.cs.lth.se", 
			"https://serpconnect.cs.lth.se",
			"https://api.serpconnect.cs.lth.se" 
		}));

		getRouter().addRoute(new Route("OPTIONS", ".*", (rc) -> {
			rc.setHeader("Access-Control-Allow-Methods", "PUT, POST, OPTIONS");
			rc.setHeader("Access-Control-Allow-Headers", "Content-Type");
			rc.setHeader("Access-Control-Max-Age", "86400");
			rc.getResponse().ok();
		}));
	}

	@Override
	protected void onInit() {
		PippoSettings conf = getPippoSettings();

		Database.configure(conf);

		// Use the ordinary mailman by default
		useMailClient(new Mailman());

		setupCORS();

		addRouteGroup(new Admin(this));
		addRouteGroup(new Admin(this));
		addRouteGroup(new Entry(this));
		addRouteGroup(new Account(this));
		addRouteGroup(new Collection(this));

		getErrorHandler().setExceptionHandler(RequestException.class, new ExceptionHandler() {
			@Override
			public void handle(Exception e, RouteContext rc) {
				if (e instanceof RequestException)
					rc.status(((RequestException) e).getStatus());
				else
					rc.status(500);
				rc.text().send(e.getMessage());
			}
		});
	}

	/**
	 * ENTRY POINT
	 */
	public static void main(String[] args) {
		Connect conn = new Connect();

		new Pippo(conn).start();
		new CleanupUsers(conn).everyTwelveHours();
	}

}