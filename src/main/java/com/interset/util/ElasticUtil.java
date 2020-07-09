package com.interset.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
* This class will handle elasticsearch functionality
* */
public class ElasticUtil {
    // create a singleton high level rest client
    RestHighLevelClient client;
    public ElasticUtil(){
        this.client  = SingletonHighLevelRestClient.getInstance();
    }

    // create a index
    public boolean createIndexWithMapping(String file) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("movies");
        String content = new String(Files.readAllBytes(Paths.get(file)));
        request.source(content, XContentType.JSON);
        boolean result = false;
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            result = createIndexResponse.isAcknowledged();
        } catch (ElasticsearchStatusException e) {
            // if the index already exists, skip this step of creating index
            if (e.getMessage().contains("resource_already_exists_exception") ) {
                return result;
            }
            throw e;
        }
        return result;
    }

    // specify the file json file and seed the index
    public  void index(String file) throws Exception {
        long t = System.currentTimeMillis();
        try {
            String indexName = "movies";
            Response response = client.getLowLevelClient().performRequest("HEAD", "/" + indexName);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                throw new Exception("Index does not exist");
            }
            // define the listener
            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                int count = 0;
                @Override
                public void beforeBulk(long l, BulkRequest bulkRequest) {
                    count = count + bulkRequest.numberOfActions();
                    System.out.println("Uploaded " + count + " so far");
                }
                @Override
                public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                    if (bulkResponse.hasFailures()) {
                        for (BulkItemResponse bulkItemResponse : bulkResponse) {
                            if (bulkItemResponse.isFailed()) {
                                BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                                System.out.println("Error " + failure.toString());
                            }
                        }
                    }
                }
                @Override
                public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                    System.out.println("Big errors " + throwable.toString());
                }
            };

            BulkProcessor bulkProcessor = BulkProcessor.builder(client::bulkAsync, listener).build();

            File jsonFilePath = new File(file);
            //initialize jsonReader class by passing reader
            JsonReader jsonReader = new JsonReader(
                    new InputStreamReader(new FileInputStream(jsonFilePath), StandardCharsets.UTF_8)
            );
            Gson gson = new GsonBuilder().serializeNulls().create();
            //start of json array
            jsonReader.beginArray();
            int id = 0;// id of the document
            while (jsonReader.hasNext()) { //next json array element
                id++;
                Document document = gson.fromJson(jsonReader, Document.class);
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("Popularity", document.getPopularity());
                jsonMap.put("Vote Count", document.getVoteCount());
                jsonMap.put("Vote Average", document.getVoteAverage());
                jsonMap.put("Overview", document.getOverview());
                jsonMap.put("Release Date", document.getReleaseDate());
                jsonMap.put("Title", document.getTitle());
                bulkProcessor.add(new IndexRequest(indexName, "doc").id(id + "").source(jsonMap));
            }
            jsonReader.endArray();

            boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);

            client.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
