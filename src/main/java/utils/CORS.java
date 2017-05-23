package utils;

import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteContext;
import se.lth.cs.connect.RequestException;

import java.util.Arrays;

public class CORS<T extends RouteContext> implements RouteHandler<T> {
    final String[] allowedOrigins;

    public CORS(final String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    private boolean isAuthorized(T rc) {
        return rc.getResponse().getHeader("Access-Control-Allow-Origin") != null;
    }

    public void handle(T rc) {
        if (isAuthorized(rc)) {
            rc.next();
            return;
        }
        
        String origin = rc.getHeader("Origin");
        boolean allowed = Arrays.stream(allowedOrigins).anyMatch(origin::equals);
        if (!allowed && origin != null)
            throw new RequestException("CORS for this origin is not allowed");

        if (origin != null) {
            rc.setHeader("Access-Control-Allow-Origin", origin);
            rc.setHeader("Access-Control-Allow-Credentials", "true");
            rc.setHeader("Access-Control-Allow-Headers", "*");
        }
        rc.next();
    }
}