package ma.ensa.ebankingver1.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "ma.ensa.ebankingver1.repository.elasticsearch",
        elasticsearchTemplateRef = "elasticsearchTemplate"
)
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Value("${elasticsearch.host:localhost}")
    private String esHost;

    @Value("${elasticsearch.port:9200}")
    private int esPort;

    @Bean
    public RestClient restClient() {
        logger.info("Creating RestClient for Elasticsearch at {}:{}", esHost, esPort);
        return RestClient.builder(new HttpHost(esHost, esPort)).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        logger.info("Creating ElasticsearchTransport");
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport elasticsearchTransport) {
        logger.info("Creating ElasticsearchClient");
        return new ElasticsearchClient(elasticsearchTransport);
    }

    @Bean(name = "elasticsearchTemplate")
    public ElasticsearchOperations elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        logger.info("Creating ElasticsearchTemplate bean");
        return new ElasticsearchTemplate(elasticsearchClient);
    }

    @PostConstruct
    public void pingTest() {
        try {
            boolean status = elasticsearchClient().ping().value();
            logger.info("✅ Elasticsearch connected: {}", status);
        } catch (Exception e) {
            logger.error("❌ Elasticsearch connection failed: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to Elasticsearch", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (restClient() != null) {
                restClient().close();
                logger.info("✅ Elasticsearch RestClient closed");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to close Elasticsearch RestClient: {}", e.getMessage());
        }
    }

    private ElasticsearchClient elasticsearchClient() {
        return elasticsearchClient(elasticsearchTransport(restClient()));
    }
}

