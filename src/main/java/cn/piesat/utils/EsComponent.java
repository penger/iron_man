package cn.piesat.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EsComponent {

    @Data
    @Builder
    public static class EsParam implements Serializable {
        private List<String> hostList;
        private Integer port;
        private String username;
        private String password;
    }


    private EsComponent(){
    }

    //最新的 ElasticCient
    public static ElasticsearchClient getElasticsearchClient(EsParam esParam){
        HttpHost[] httpHosts = getHttpHosts(esParam);

        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(esParam.username, esParam.password));


        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts).setHttpClientConfigCallback(x -> x.setDefaultCredentialsProvider(basicCredentialsProvider));

        RestClientTransport restClientTransport = new RestClientTransport(
                restClientBuilder.build()
                , new JacksonJsonpMapper()
        );
        return  new ElasticsearchClient(restClientTransport);
    }




    public static RestHighLevelClient getRestHighLevelClient(EsParam esParam){
        RestClientBuilder builder = RestClient.builder(getHttpHosts(esParam));
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esParam.getUsername(), esParam.getPassword()));
        builder.setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(basicCredentialsProvider));
        RestHighLevelClient client = new RestHighLevelClient(builder);// 初始化
        return client;
    }


    private static HttpHost[] getHttpHosts(EsParam esParam) {
        List<HttpHost> httpHostList = new ArrayList<>();
        for (String host : esParam.hostList) {
            HttpHost httpHost = new HttpHost(host, esParam.getPort());
            httpHostList.add(httpHost);
        }

        HttpHost[] httpHosts = new HttpHost[httpHostList.size()];
        httpHostList.toArray(httpHosts);
        return httpHosts;
    }


}
