package med.voll.api.client;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class OrgIdentifierClientService {
    private final Gson gson;
    private final String proxyUrl;
    private final int proxyPort;
    private final String urlDiretorio;

    @Autowired
    public OrgIdentifierClientService(
            Gson gson,
            @Value("${api.integration.proxy.url}")
            String proxyUrl,
            @Value("${api.integration.proxy.port}")
            int proxyPort,
            @Value("${api.integration.directory.url}")
            String urlDiretorio){
        this.gson = gson;
        this.proxyUrl = proxyUrl;
        this.proxyPort = proxyPort;
        this.urlDiretorio = urlDiretorio;
    }

    )

}
