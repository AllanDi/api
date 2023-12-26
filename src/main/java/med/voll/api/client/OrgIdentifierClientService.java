package med.voll.api.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
            String urlDiretorio) {

        this.gson = gson;
        this.proxyUrl = proxyUrl;
        this.proxyPort = proxyPort;
        this.urlDiretorio = urlDiretorio;
    }

    public OrgIdentifierClientService() {

        this.urlDiretorio = null;
        this.gson = new Gson();
        this.proxyUrl = null;
        this.proxyPort = 80;
    }

    @Cacheable("remoteData")
    public JsonArray fetchOrgData() {

        String respBody =
                Unirest.get(urlDiretorio)
                .proxy(proxyUrl, proxyPort)
                .asString()
                .ifFailure(this::emptyOrgListCache)
                .getBody();

        return gson.fromJson(respBody, JsonArray.class);
    }

    @CacheEvict(value = {"remoteData", "orgIdsCache"}, allEntries = true)
    public void emptyOrgListCache(
            HttpResponse<String> res) {

        log.error("Organization list fetch error, cleaning chache.");

        log.info("emptying remote organization list data chache");
    }

    @Cacheable(value = "orgIdsCache")
    public String getIdentifier(
            JsonArray orglist,
            String orgBrand) {

        var organization = orglist.asList()
                .stream()
                .filter(org -> {

            var orgObj = org.getAsJsonObject();

            return orgObj
                    .get("OrganisationName")
                    .getAsString()
                    .equalsIgnoreCase(orgBrand)
                    || orgObj.get("LegalEntityName").getAsString().equalsIgnoreCase(orgBrand)
                    || orgObj.get("RegisteredName").getAsString().equalsIgnoreCase(orgBrand)
                    || orgObj.get("AuthorisationServers").getAsJsonArray().asList().stream()
                    .anyMatch(
                            auth ->
                                    auth.getAsJsonObject()
                                            .get("CustomerFriendName")
                                            .getAsString()
                                            .equalsIgnoreCase(orgBrand));

        }).findFirst().orElse(null);

        return (organization != null) ? organization.getAsJsonObject().get("OrganisationId").getAsString() : null;
    }


}
