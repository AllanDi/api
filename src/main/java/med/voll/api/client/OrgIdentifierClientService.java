package med.voll.api.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

        return (organization != null)
                ? organization.getAsJsonObject().get("OrganisationId").getAsString()
                : null;
    }

    @Cacheable(value = "orgIdsCache")
    public String getIdentifierByBrand(JsonArray orgList, String orgBrand) {
        var organization =
                orgList.asList().stream()
                        .filter(
                                org ->
                                        org.getAsJsonObject()
                                                .get("RegistrationNumber")
                                                .getAsString()
                                                .equalsIgnoreCase(orgBrand))
                        .findFirst()
                        .orElse(null);
        return (organization != null)
                ? organization.getAsJsonObject().get("OrganizationId").getAsString()
                : null;
    }

    @Cacheable(value = "orgIdsCache")
    public String getServerIdentifierByHostMatch(JsonArray orgList, String serverHost) {
        var organization =
                orgList.asList().stream()
                        .filter(
                                org -> {
                                    var pattern = Pattern.compile(serverHost);
                                    var matcher = pattern.matcher(org.toString());
                                    return matcher.find();
                                })
                        .findFirst()
                        .orElse(null);
        return (organization != null)
                ? organization.getAsJsonObject().get("OrganizationId").getAsString()
                : null;
    }

    @Cacheable(value = "orgIdsCache")
    public String getAuthServerId(JsonArray orgList, String url, String brandName) {

        Stream<JsonElement> authServerList =
                orgList.asList().stream()
                        .map(org -> org.getAsJsonObject().get("AuthorisationServers").getAsJsonArray().asList())
                        .flatMap(Collection::stream);
        if (brandName.isBlank()) {
            JsonElement authServer =
                    authServerList
                            .filter(
                                    org -> {
                                        var pattern = Pattern.compile(url.replaceAll("[\\{\\}]", "\\\\$0"));
                                        var matcher = pattern.matcher(org.toString());
                                        return matcher.find();
                                    })
                            .findFirst()
                            .orElse(null);

            return (authServer != null)
                    ? authServer.getAsJsonObject().get("AuthorisationServerId").getAsString()
                    : null;
        } else {
            return filterBrandName(authServerList, url, brandName);
        }
    }

    public String filterBrandName(Stream<JsonElement> authServerList, String url, String brandName) {
        Map<String, String> brands = new HashMap();
        brands.put("api-api-seguros", "api api seguros");
        brands.put("api-api-previdencia", "api api previdencia");
        brands.put("evidencia", "Evidencia");
        brands.put("api-capitalização", "api capitalização");

        String customerFriendlyName = brands.get(brandName);

        JsonElement authServer =
                authServerList
                        .filter(
                                org -> {
                                    var pattern = Pattern.compile(url.replaceAll("[\\{\\}]", "\\\\$0"));
                                    var matcher = pattern.matcher(org.toString());
                                    return matcher.find();
                                })
                        .filter(
                                org ->
                                        org.getAsJsonObject()
                                                .get("RegistrationNumber")
                                                .getAsString()
                                                .equalsIgnoreCase(customerFriendlyName))
                        .findFirst()
                        .orElse(null);
        return (authServer != null)
                ? authServer.getAsJsonObject().get("AuthorisationServerId").getAsString()
                : null;
    }


}
