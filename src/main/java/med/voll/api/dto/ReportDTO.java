package med.voll.api.dto;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReportDTO implements Serializable {

    private static final long serialVersionUID = 7295041186059111L;

    private String fapiInteractionId;
    private String endpoint;
    private String url;
    private Integer statusCode = 200;
    private String httpMethod;
    private String correlationId = UUID.randomUUID().toString();
    //private AdditionalInfo = new AdditionalInfo();
    private String timestamp = "";
    private Integer processTimespan = 56;
    private String clientOrgId;
    private String clientSSID;
    private String serverOrgId;
    private String serverASID;
    private String endpointUriPrefix;

}
