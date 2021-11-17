package com.swallowincense.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 给容器中注入一个bean
 * 参照API地址 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 */
@Configuration
public class ElasticSearchConfig {
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        /*builder.addHeader("Authorization", "Bearer " + TOKEN);
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory
                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));*/
        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient elasticSearchClient(){
        return new RestHighLevelClient(
                RestClient.builder(
                        //添加集群节点
                        //new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9200, "http")));
    }
}
