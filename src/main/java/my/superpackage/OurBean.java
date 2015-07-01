package my.superpackage;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.naming.Context;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * @author Nikita Konev
 */

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class OurBean {

    private Logger logger = LoggerFactory.getLogger(OurBean.class);

    private volatile Context context = null;

    @PostConstruct
    private void init() {
        logger.info("initializing...");

        try {
            callMethod();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("initialized successfully");
    }

    public void callMethod() throws URISyntaxException, IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 30*1000);
        HttpConnectionParams.setSoTimeout(httpParams, 30*1000);

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("api.data.mos.ru").setPath("/v1/datasets/1122/rows")
        ;
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);
        System.out.println(httpget.getURI());

        try {
            HttpResponse response = httpClient.execute(httpget);
            logger.info(response.toString());

        } catch (SocketTimeoutException e) {
            logger.error("Error: ", e);
        }
    }

    @PreDestroy
    private void destroy() {
        logger.info("Destroying in " + Thread.currentThread());
        try {
            if (context != null)
                context.close();
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

}
