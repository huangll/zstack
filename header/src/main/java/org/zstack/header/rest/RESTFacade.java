package org.zstack.header.rest;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.zstack.header.core.Completion;
import org.zstack.header.exception.CloudRuntimeException;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface RESTFacade {
    void asyncJsonPost(String url, Object body, Map<String, String> headers, AsyncRESTCallback callback, TimeUnit unit, long timeout);

    void asyncJsonPost(String url, Object body, AsyncRESTCallback callback, TimeUnit unit, long timeout);

    void asyncJsonPost(String url, String body, AsyncRESTCallback callback, TimeUnit unit, long timeout);

    void asyncJsonPost(String url, String body, Map<String, String> headers, AsyncRESTCallback callback, TimeUnit unit, long timeout);

    void asyncJsonPost(String url, Object body, Map<String, String> headers, AsyncRESTCallback callback);

    void asyncJsonPost(String url, Object body, AsyncRESTCallback callback);

    void asyncJsonPost(String url, String body, AsyncRESTCallback callback);
    void asyncJsonDelete(String url, String body, Map<String, String> headers, AsyncRESTCallback callback, TimeUnit unit, long timeout);
    void asyncJsonGet(String url, String body, Map<String, String> headers, AsyncRESTCallback callback, TimeUnit unit, long timeout);

    <T> T syncJsonPost(String url, Object body, Class<T> returnClass);

    <T> T syncJsonPost(String url, Object body, Class<T> returnClass, TimeUnit unit, long timeout);

    <T> T syncJsonPost(String url, String body, Class<T> returnClass);

    <T> T syncJsonPost(String url, String body, Map<String, String> headers, Class<T> returnClass);

    <T> T syncJsonPost(String url, String body, Map<String, String> headers, Class<T> returnClass, TimeUnit unit, long timeout);

    /**
     * ZStack's agents only use sync/async post method
     * delete and get methods used for outsides plugins
     */
    <T> T syncJsonDelete(String url, String body, Map<String, String> headers, Class<T> returnClass);

    <T> T syncJsonDelete(String url, String body, Map<String, String> headers, Class<T> returnClass, TimeUnit unit, long timeout);

    <T> T syncJsonGet(String url, String body, Map<String, String> headers, Class<T> returnClass);

    <T> T syncJsonGet(String url, String body, Map<String, String> headers, Class<T> returnClass, TimeUnit unit, long timeout);

    HttpHeaders syncHead(String url);

    HttpEntity<String> httpServletRequestToHttpEntity(HttpServletRequest req);

    RestTemplate getRESTTemplate();

    void echo(String url, Completion callback);

    void echo(String url, Completion callback, long inverval, long timeout);

    Map<String, HttpCallStatistic> getStatistics();

    <T> void registerSyncHttpCallHandler(String path, Class<T> objectType, SyncHttpCallHandler<T> handler);

    String getBaseUrl();

    String getSendCommandUrl();

    String getCallbackUrl();

    String getHostName();

    int getPort();

    String makeUrl(String path);

    Runnable installBeforeAsyncJsonPostInterceptor(BeforeAsyncJsonPostInterceptor interceptor);

    static void setMessageConverter(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringHttpMessageConverter.setWriteAcceptCharset(true);

        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                converters.remove(i);
                converters.add(i, stringHttpMessageConverter);
                break;
            }
        }
    }

    // timeout are in milliseconds
    static TimeoutRestTemplate createRestTemplate(int readTimeout, int connectTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new TimeoutHttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);
        factory.setConnectionRequestTimeout(connectTimeout * 2);

        SSLContext sslContext = DefaultSSLVerifier.getSSLContext(DefaultSSLVerifier.trustAllCerts);

        if (sslContext != null) {
            factory.setHttpClient(HttpClients.custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .setSSLContext(sslContext)
                    .build());
        }

        TimeoutRestTemplate template = new TimeoutRestTemplate(factory);
        setMessageConverter(template.getMessageConverters());

        return template;
    }

    // timeout are in milliseconds
    static AsyncRestTemplate createAsyncRestTemplate(int readTimeout, int connectTimeout, int maxPerRoute, int maxTotal) {
        PoolingNHttpClientConnectionManager connectionManager;
        try {
             connectionManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
        } catch (IOReactorException ex) {
            throw new CloudRuntimeException(ex);
        }

        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.setMaxTotal(maxTotal);

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsAsyncClientHttpRequestFactory cf = new HttpComponentsAsyncClientHttpRequestFactory(httpAsyncClient);
        cf.setConnectTimeout(connectTimeout);
        cf.setReadTimeout(readTimeout);
        cf.setConnectionRequestTimeout(connectTimeout * 2);

        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate(cf);
        setMessageConverter(asyncRestTemplate.getMessageConverters());

        return asyncRestTemplate;
    }
}
