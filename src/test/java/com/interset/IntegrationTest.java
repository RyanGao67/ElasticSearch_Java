package com.interset;

import com.interset.config.SearchConfiguration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

@ExtendWith(DropwizardExtensionsSupport.class)
class SearchIntegartionTest {

    private static DropwizardAppExtension<SearchConfiguration> EXT = new DropwizardAppExtension(
            SearchApplication.class,
            ResourceHelpers.resourceFilePath("configuration.yml")
    );

    @Test
    void topRatedTest() {
        Client client = EXT.client();
        Response response = client.target(
                String.format("http://localhost:3000/api/query/topRated?startDate=2019-01-01&endDate=2019-02-28&minVos=1000", EXT.getLocalPort()))
                .request()
                .get();
        assert(response.getStatus()==200);
    }

    @Test
    void simpleQueryTest() {
        String currentDirectory = System.getProperty("user.dir");
        System.out.println("The current working directory is " + currentDirectory);
        Client client = EXT.client();
        Response response = client.target(
                String.format("http://localhost:3000/api/query?q=title:The+Lion+King&docCount=50", EXT.getLocalPort()))
                .request()
                .get();
        assert(response.getStatus()==200);
    }
}