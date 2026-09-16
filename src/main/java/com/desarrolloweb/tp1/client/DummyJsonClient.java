package com.desarrolloweb.tp1.client;

import com.desarrolloweb.tp1.client.DummyJsonModels.DummyJsonProduct;
import com.desarrolloweb.tp1.client.DummyJsonModels.DummyJsonProductList;
import com.desarrolloweb.tp1.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DummyJsonClient {

    private final RestClient restClient;

    public DummyJsonClient(RestClient dummyJsonRestClient) {
        this.restClient = dummyJsonRestClient;
    }

    public DummyJsonProductList obtenerProductos(int limit, int skip) {
        try {
            return restClient.get()
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("No se pudo obtener el catálogo de productos externo", ex);
        }
    }

    public DummyJsonProduct obtenerProductoPorId(Long id) {
        try {
            return restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProduct.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("No se pudo obtener el producto externo con id " + id, ex);
        }
    }
}
