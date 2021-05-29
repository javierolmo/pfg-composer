package com.javi.uned.pfgcomposer.services;

import com.javi.uned.pfgcomposer.exceptions.BackendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;

@Service
public class BackendService {

    @Value("${API_HOST:localhost}")
    private String apiHost;
    @Value("${API_PORT:3000}")
    private String apiPort;

    public Object persistXMLSheet(File sheet, String id) throws BackendException {

        RestTemplate restTemplate = new RestTemplate();

        // Add file
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("file", new FileSystemResource(sheet));

        // Url
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(String.format("http://%s:%s/api/sheets/%s/file/musicxml", apiHost, apiPort, id));

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Http request
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<Object> responseEntity = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            return responseEntity.getBody();
        }else{
            throw new BackendException("Error al guardar partitura en xml: ");
        }
    }

    public Object persistPDFSheet(File sheet, String id) throws BackendException {

        RestTemplate restTemplate = new RestTemplate();

        // Add file
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("file", new FileSystemResource(sheet));

        // Url
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(String.format("http://%s:%s/api/sheets/%s/file/pdf", apiHost, apiPort, id));

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Http request
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<Object> responseEntity = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            return responseEntity.getBody();
        }else{
            throw new BackendException("Error al guardar partitura en pdf: ");
        }
    }

}
