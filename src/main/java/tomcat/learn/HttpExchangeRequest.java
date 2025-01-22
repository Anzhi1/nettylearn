package tomcat.learn;

import java.net.URI;

interface HttpExchangeRequest {
    String getRequestMethod();
    URI getRequestURI();
}
