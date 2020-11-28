package com.interset.api;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.ml.job.results.Bucket;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Path("/api/index")
public class IndexResource {
    // init high level rest client
    static RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost("localhost", 9200, "http"),
                    new HttpHost("localhost", 9201, "http")
            )
    );
    private static final Logger LOG = LoggerFactory.getLogger(QueryResource.class);

    /*
        Used for bonus.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response index(

    ) {
        throw new UnsupportedOperationException("IndexResource.index method is not implemented yet.");
    }

    @GET
    public String testget() throws IOException {
        // init high level rest client
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("es01", 9200, "http"),
                        new HttpHost("es01", 9201, "http")
                )
        );

//        // lowlevel one
//        RestClient lowrestClient = RestClient.builder(
//                new HttpHost("localhost", 9200, "http"),
//                new HttpHost("localhost", 9201, "http")
//        ).build();
//        // to close
//        lowrestClient.close();
//        //Set the default headers that need to be sent with each request, to prevent having to specify them with each single request
//        RestClientBuilder builder = RestClient.builder(
//                new HttpHost("localhost", 9200, "http")
//        );
//        Header[] defaultHeaders = new Header[]{new BasicHeader("header", "value")};
//        builder.setDefaultHeaders(defaultHeaders);
//        lowrestClient = builder.build();
//
//        //Set a callback that allows to modify the default request configuration (e.g. request timeouts, authentication, or anything that the org.apache.http.client.config.RequestConfig.Builder allows to set)
//        builder = RestClient.builder(
//                new HttpHost("localhost", 9200, "http")
//        );
//        builder.setRequestConfigCallback(
//                new RestClientBuilder.RequestConfigCallback() {
//                    @Override
//                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
//                        return requestConfigBuilder.setSocketTimeout(10000);
//                    }
//                }
//        );
//        lowrestClient = builder.build();


        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 1: match all");
        SearchRequest searchRequest = new SearchRequest();
        //SearchRequest searchRequest = new SearchRequest("posts");
        //Restricts the request to an index

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 2: termquery");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        searchSourceBuilder.query(QueryBuilders.termQuery("Title","Underwater"));//        Set the query. Can be any type of QueryBuilder
        searchSourceBuilder.from(0);//        Set the from option that determines the result index to start searching from. Defaults to 0.
        searchSourceBuilder.size(5);//        Set the size option that determines the number of search hits to return. Defaults to 10.
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));//        Set an optional timeout that controls how long the search is allowed to take.

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 3: match query + response");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        QueryBuilder matchQueryBuilder = matchQuery("Title", "The Platform")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);

        searchRequest.source(searchSourceBuilder);

        // investigate the response
        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        RestStatus status = searchResponse.status();System.out.println(status);
        TimeValue took = searchResponse.getTook();System.out.println(took);
        Boolean terminatedEarly = searchResponse.isTerminatedEarly();System.out.println(terminatedEarly);
        boolean timeOut = searchResponse.isTimedOut();System.out.println(timeOut);
        int totalShards = searchResponse.getTotalShards();System.out.println(totalShards);
        int successfulShards = searchResponse.getSuccessfulShards();System.out.println(successfulShards);
        int failedShards = searchResponse.getFailedShards();System.out.println(failedShards);
        for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
            // failures should be handled here
            System.out.println(failure);
        }

        SearchHits hits = searchResponse.getHits();
        Long totalHits = hits.getTotalHits();System.out.println("totalhits: "+totalHits);
        float maxScore = hits.getMaxScore();System.out.println("max score: "+maxScore);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String index = hit.getIndex();System.out.println("index: "+index);
            String id = hit.getId();System.out.println("id: "+id);
            float score = hit.getScore();System.out.println("score: "+score);

//            String sourceAsString = hit.getSourceAsString();
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            String documentTitle = (String) sourceAsMap.get("title");
//            List<Object> users = (List<Object>) sourceAsMap.get("user");
//            Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");

            String s = hit.getSourceAsString();
            System.out.println(s);
        }


        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 4: sort");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));//Sort descending by _score (the default)
        searchSourceBuilder.sort(new FieldSortBuilder("Popularity").order(SortOrder.ASC));//Also sort ascending by _id field

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }


        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 5: fetch fields");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));//Sort descending by _score (the default)
        searchSourceBuilder.sort(new FieldSortBuilder("Popularity").order(SortOrder.ASC));//Also sort ascending by _id field
        searchSourceBuilder.fetchSource(false); // For example, you can turn off _source retrieval completely


        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }


        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 6: fetch fields includes excludes");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));//Sort descending by _score (the default)
        searchSourceBuilder.sort(new FieldSortBuilder("Popularity").order(SortOrder.ASC));//Also sort ascending by _id field
        String[] includeFields = new String[] {"Title", "Popularity"};
        //String[] excludeFields = new String[] {"user"};
        searchSourceBuilder.fetchSource(includeFields, new String[]{});

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 7: highlight");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));//Sort descending by _score (the default)
        searchSourceBuilder.sort(new FieldSortBuilder("Popularity").order(SortOrder.ASC));//Also sort ascending by _id field

            HighlightBuilder highlightBuilder = new HighlightBuilder();
            HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("Popularity");
            highlightTitle.highlighterType("unified");
            highlightBuilder.field(highlightTitle);
            HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("Title");
            highlightBuilder.field(highlightUser);

        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);


            // retrieve highlights
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println("highlightFiestds: ");
            System.out.println(highlightFields);
            HighlightField highlight = highlightFields.get("Title");
            Text[] fragments = highlight.fragments();
            String fragmentString = fragments[0].string();System.out.println(fragmentString);
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 8: aggregation");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform");
        searchSourceBuilder.query(matchQueryBuilder);

        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms("by_date")
                .field("Release Date");
        aggregation.subAggregation(
                AggregationBuilders
                        .max("average_pop")
                        .field("Popularity")
        );
        searchSourceBuilder.aggregation(aggregation);

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println("search doc: ");
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }


        System.out.println("search agg: ");
        Terms terms = searchResponse.getAggregations().get("by_date");
        Collection<Terms.Bucket> buckets = (Collection<Terms.Bucket>) terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println(bucket.getKeyAsString() +" ("+bucket.getDocCount()+")");
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 9: aggregation more");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform");
        searchSourceBuilder.query(matchQueryBuilder);

        aggregation = AggregationBuilders
                .terms("by_date")
                .field("Release Date");
        aggregation
                .subAggregation(
                    AggregationBuilders
                            .max("average_pop")
                            .field("Popularity")
                )
                .subAggregation(
                    AggregationBuilders
                            .topHits("tgaoinfo")
                            .fetchSource(new String[]{"Popularity", "Title"}, new String[]{})
                            .size(1)
                );
        searchSourceBuilder.size(0).aggregation(aggregation);

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }


        System.out.println("search doc: ");
        result = searchResponse.getHits().getHits();
        for(SearchHit hit:result){
            String s = hit.getSourceAsString();
            System.out.println(s);
        }


        System.out.println("search agg: ");
        terms = searchResponse.getAggregations().get("by_date");
        buckets = (Collection<Terms.Bucket>) terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println("bucket key: "+bucket.getKeyAsString());

            Aggregations entiryAggs = bucket.getAggregations();
            TopHits mostCurrent = entiryAggs.get("tgaoinfo");
            SearchHit[] hitsagg = mostCurrent.getHits().getHits();
            Map<String, Object> aggresult = hitsagg[0].getSourceAsMap();
            System.out.println(aggresult);

        }


        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 10: requesting suggestion");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
            matchQueryBuilder = matchQuery("Title", "The Platform")
                    .fuzziness(Fuzziness.AUTO)
                    .prefixLength(3)
                    .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);   // query
            SuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("Title").text("The Circle");
            SuggestBuilder suggestBuilder = new SuggestBuilder();
            suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);    // suggest
        searchSourceBuilder.profile(true);              // profile

        searchRequest.source(searchSourceBuilder);
        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 11: async");
        searchRequest = new SearchRequest();

        searchSourceBuilder = new SearchSourceBuilder();//        Create a SearchSourceBuilder with default options.
        matchQueryBuilder = matchQuery("Title", "The Platform")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));//Sort descending by _score (the default)
        searchSourceBuilder.sort(new FieldSortBuilder("Popularity").order(SortOrder.ASC));//Also sort ascending by _id field

        searchRequest.source(searchSourceBuilder);
        ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                System.out.println("async");
                SearchHit[] result = searchResponse.getHits().getHits();
                for(SearchHit hit:result){
                    String s = hit.getSourceAsString();
                    System.out.println(s);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
        client.searchAsync(searchRequest, RequestOptions.DEFAULT, listener);

        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("NO 12: async");
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        searchRequest = new SearchRequest("movies");
        searchRequest.scroll(scroll);

        searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchQuery("Title", "The Public"));

        searchRequest.source(searchSourceBuilder);

        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        while (searchHits != null && searchHits.length > 0) {

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();

        return "";
    }

}
