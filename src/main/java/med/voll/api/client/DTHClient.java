package med.voll.api.client;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import lombok.extern.log4j.Log4j;
import med.voll.api.dto.ReportDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
**Análise Técnica Detalhada:**

1. `public class DTHClient`: Define uma classe chamada `DTHClient`.

2. `@Value("${api.remoteClient.dth.signer.url}")`: Anotação Spring que injeta o valor da propriedade especificada no
*  arquivo de configuração (por exemplo, `application.properties`) na variável `clientHost`.

3. `public String clientHost`: Declara uma variável pública `clientHost` que armazenará a URL base do serviço remoto.

4. `public HttpResponse<String> signJWT(String certName, String kid, ReportDTO report)`: Define um método `signJWT`
* que retorna um objeto `HttpResponse<String>`. Ele aceita três parâmetros: `certName`, `kid` e um objeto `report`.

5. `var url = clientHost.replace("{pkid}", certName).replace("{keyId}", kid);`: Cria uma URL substituindo os
* placeholders na URL base (`clientHost`) com `certName` e `kid`.

6. `Unirest.post(url)`: Inicia uma requisição POST usando Unirest para a URL modificada.

7. `.header("Content = Type", "application/json")`: Adiciona um cabeçalho à requisição indicando o tipo de conteúdo
* como JSON. (Note: Parece haver um erro aqui, deveria ser `"Content-Type"`).

8. `.body(report)`: Adiciona o objeto `report` como corpo da requisição.

9. `.asString()`: Especifica que a resposta esperada é uma String.

10. `.ifFailure(this::handleError)`: Se a requisição falhar, chama o método `handleError`.

11. `public void handleError(HttpResponse<String> res)`: Um método que é chamado em caso de falha na requisição.
* Ele registra o status da resposta HTTP.

**Explicação estilo Feynman com Storytelling:**

Imagine que DTHClient é um mensageiro em um reino digital. Sua tarefa é entregar relatórios secretos para a
* Torre do Signer, um lugar mágico onde os relatórios recebem um selo especial chamado JWT.

Cada vez que ele precisa entregar um relatório, ele verifica um mapa mágico (`clientHost`) que mostra o caminho
* para a Torre. Mas esse caminho precisa de duas chaves mágicas para ser completo: o `certName` e o `kid`.
* Ele ajusta o mapa com essas chaves.

Então, ele prepara a mensagem (`Unirest.post`), colocando o relatório dentro e marcando que é um tipo especial de
* mensagem, uma mensagem JSON. Ele então a envia pelo mundo digital.

Se por acaso a mensagem não chegar ou houver algum problema, DTHClient tem um diário mágico (`log`).
* Ele abre o diário e escreve "Oh não, algo deu errado! O status é...", registrando o que aconteceu para que possa
* contar ao rei mais tarde.
 */

@Service
@Log4j
public class DTHClient {
    @Value("${api.remoteClient.dth.signer.url}")
    public String clientHost;

    public HttpResponse<String> signJWT(String certName, String kid, ReportDTO report) {

        var url = clientHost.replace("{pkid}", certName).replace("{keyId}", kid);

        return Unirest.post(url).header("Content = Type", "application/json")
                .body(report).asString().ifFailure(this::handleError);
    }

    public void handleError(HttpResponse<String> res) {
        log.info("Oh No! Status " + res.getStatus());
    }

}
