package com.interset;

import com.interset.api.IndexResource;
import com.interset.api.QueryResource;
import com.interset.config.SearchConfiguration;
import com.interset.parser.CSVToJSONParser;
import com.interset.util.ElasticUtil;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchApplication extends Application<SearchConfiguration> {

    private static Logger LOG = LoggerFactory.getLogger(SearchApplication.class);
    private final String INDEXMAPPING = "./src/main/resources/movies_template.json";
    private final String MOVIERATINGSCSV = "./src/main/resources/movie_ratings.csv";
    private final String MOVIERATINGSJSON = "./src/main/resources/movie_ratings.json";

    public static void main(String[] args) throws Exception {
        new SearchApplication().run(args);
    }

    /*
        Called on application startup.
     */
    @Override
    public void run(SearchConfiguration searchConfiguration, Environment environment) throws Exception {
        ElasticUtil elasticUtil = new ElasticUtil();
        CSVToJSONParser.parse(MOVIERATINGSCSV);
        elasticUtil.createIndexWithMapping(INDEXMAPPING);
        elasticUtil.index(MOVIERATINGSJSON);
        environment.jersey().register(new IndexResource());
        environment.jersey().register(new QueryResource());
    }

}
