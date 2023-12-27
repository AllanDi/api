package med.voll.api.client;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Log4j
public class PCMReportClient {

    @Value("${pcm.integration.url}")
    private String url;

    @Value("${pcm.integration.proxy.url}")
    private String proxyUrl;

    @Value("${pcm.integration.proxy.port}")
    private int proxyPort;

    public HttpResponse<String> sendReport(Object report){
        return Unirest.post(url)
                .proxy(proxyUrl, proxyPort)
                .body(report)
                .header("Content-Type", "application/jwt")
                .asString()
                .ifFailure(this::handleError);

    }

    public void handleError(HttpResponse<String> res){
        log.error("Oh No! Status " + res.getStatus());
    }
}
