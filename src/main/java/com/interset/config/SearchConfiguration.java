package com.interset.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import java.util.HashMap;
import java.util.Map;

/*
    Reads the configuration from `configuration.yml`.
 */
public class SearchConfiguration extends Configuration {

    @JsonProperty("elasticsearch")
    public Map<String, Object> elasticSearch = new HashMap<>();

}
