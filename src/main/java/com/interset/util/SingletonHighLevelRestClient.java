package com.interset.util;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class SingletonHighLevelRestClient {
    // static variable single_instance of type Singleton
    private static SingletonHighLevelRestClient single_instance = null;
    // private constructor restricted to this class itself
    RestHighLevelClient client = null;

    private SingletonHighLevelRestClient(){
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("es01", 9200, "http"))
        );
    }

    // static method to create instance of Singleton class
    public static RestHighLevelClient getInstance(){
        if (single_instance == null)
            single_instance = new SingletonHighLevelRestClient();
        return single_instance.client;
    }
}
