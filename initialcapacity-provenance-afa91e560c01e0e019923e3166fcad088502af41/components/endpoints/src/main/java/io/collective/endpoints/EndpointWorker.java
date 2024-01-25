package io.collective.endpoints;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.collective.articles.ArticleDataGateway;
import io.collective.articles.ArticleInfo;
import io.collective.restsupport.RestTemplate;
import io.collective.rss.Item;
import io.collective.rss.RSS;
import io.collective.workflow.Worker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EndpointWorker implements Worker<EndpointTask> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate template;
    private final ArticleDataGateway gateway;

    public EndpointWorker(RestTemplate template, ArticleDataGateway gateway) {
        this.template = template;
        this.gateway = gateway;
    }

    @NotNull
    @Override
    public String getName() {
        return "ready";
    }

    @Override
    public void execute(EndpointTask task) throws IOException {
        String response = template.get(task.getEndpoint(), task.getAccept());
        gateway.clear();

        // todo - map rss results to an article infos collection and save articles infos
        // to the article gateway

        // Parse the RSS feed
        RSS rss = new XmlMapper().readValue(response, RSS.class);

        // Convert each Item to an ArticleInfo and save it to the gateway
        int id = 1;
        for (Item item : rss.getChannel().getItem()) {
            ArticleInfo article = new ArticleInfo(id, item.getTitle());
            gateway.save(item.getTitle());
        }

    }
}
