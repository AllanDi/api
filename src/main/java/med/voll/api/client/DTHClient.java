package med.voll.api.client;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import lombok.extern.log4j.Log4j;
import med.voll.api.dto.ReportDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j

public class DTHClient {

    @Value("${api.remoteClient.dth.signer.url}")
    public String clientHost;

    public HttpResponse<String> signJWT(String certName, String kid, ReportDTO report) {

        var url = clientHost.replace("{pkid}", certName).replace("{keyId}", kid);
        return Unirest
                .post(url)
                .header("Content = Type", "application/json")
                .body(report)
                .asString()
                .ifFailure(this::handleError);
    }
    public void handleError(HttpResponse<String> res) {
        log.info("Oh No! Status " + res.getStatus());
    }
}
