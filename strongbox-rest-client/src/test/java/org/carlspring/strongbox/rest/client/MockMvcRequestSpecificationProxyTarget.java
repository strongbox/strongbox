package org.carlspring.strongbox.rest.client;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.springframework.aop.RawTargetAccess;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.mapper.ObjectMapper;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import io.restassured.module.mockmvc.intercept.MockHttpServletRequestBuilderInterceptor;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.module.mockmvc.specification.MockMvcAuthenticationSpecification;
import io.restassured.module.mockmvc.specification.MockMvcRequestAsyncSender;
import io.restassured.module.mockmvc.specification.MockMvcRequestLogSpecification;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * This class needed to avoid getting proxied <code>this</code> object returned by `MockMvcRequestSpecification` methods. 
 * 
 * @author sbespalov
 * 
 * @see JdkDynamicAopProxy
 * @see RawTargetAccess
 */
public class MockMvcRequestSpecificationProxyTarget implements MockMvcRequestSpecification
{

    private final MockMvcRequestSpecification target;

    public MockMvcRequestSpecificationProxyTarget(MockMvcRequestSpecification target)
    {
        this.target = target;
    }

    public MockMvcResponse get(String path,
                               Object... pathParams)
    {
        return target.get(path, pathParams);
    }

    public MockMvcResponse get(String path,
                               Map<String, ?> pathParams)
    {
        return target.get(path, pathParams);
    }

    public MockMvcAuthenticationSpecification auth()
    {
        return target.auth();
    }

    public MockMvcRequestSpecification contentType(ContentType contentType)
    {
        return target.contentType(contentType);
    }

    public MockMvcResponse post(String path,
                                Object... pathParams)
    {
        return target.post(path, pathParams);
    }

    public MockMvcRequestSpecification contentType(String contentType)
    {
        return target.contentType(contentType);
    }

    public MockMvcRequestSpecification accept(ContentType contentType)
    {
        return target.accept(contentType);
    }

    public MockMvcResponse post(String path,
                                Map<String, ?> pathParams)
    {
        return target.post(path, pathParams);
    }

    public MockMvcRequestSpecification accept(String mediaTypes)
    {
        return target.accept(mediaTypes);
    }

    public MockMvcResponse put(String path,
                               Object... pathParams)
    {
        return target.put(path, pathParams);
    }

    public MockMvcRequestSpecification headers(String firstHeaderName,
                                               Object firstHeaderValue,
                                               Object... headerNameValuePairs)
    {
        return target.headers(firstHeaderName, firstHeaderValue, headerNameValuePairs);
    }

    public MockMvcResponse put(String path,
                               Map<String, ?> pathParams)
    {
        return target.put(path, pathParams);
    }

    public MockMvcResponse delete(String path,
                                  Object... pathParams)
    {
        return target.delete(path, pathParams);
    }

    public MockMvcRequestSpecification headers(Map<String, ?> headers)
    {
        return target.headers(headers);
    }

    public MockMvcResponse delete(String path,
                                  Map<String, ?> pathParams)
    {
        return target.delete(path, pathParams);
    }

    public MockMvcRequestSpecification headers(Headers headers)
    {
        return target.headers(headers);
    }

    public MockMvcResponse head(String path,
                                Object... pathParams)
    {
        return target.head(path, pathParams);
    }

    public MockMvcResponse head(String path,
                                Map<String, ?> pathParams)
    {
        return target.head(path, pathParams);
    }

    public MockMvcRequestSpecification header(String headerName,
                                              Object headerValue,
                                              Object... additionalHeaderValues)
    {
        return target.header(headerName, headerValue, additionalHeaderValues);
    }

    public MockMvcResponse patch(String path,
                                 Object... pathParams)
    {
        return target.patch(path, pathParams);
    }

    public MockMvcResponse patch(String path,
                                 Map<String, ?> pathParams)
    {
        return target.patch(path, pathParams);
    }

    public MockMvcRequestSpecification header(Header header)
    {
        return target.header(header);
    }

    public MockMvcResponse options(String path,
                                   Object... pathParams)
    {
        return target.options(path, pathParams);
    }

    public MockMvcRequestLogSpecification log()
    {
        return target.log();
    }

    public MockMvcResponse options(String path,
                                   Map<String, ?> pathParams)
    {
        return target.options(path, pathParams);
    }

    public MockMvcRequestSpecification params(String firstParameterName,
                                              Object firstParameterValue,
                                              Object... parameterNameValuePairs)
    {
        return target.params(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    public MockMvcResponse get(URI uri)
    {
        return target.get(uri);
    }

    public MockMvcResponse post(URI uri)
    {
        return target.post(uri);
    }

    public MockMvcResponse put(URI uri)
    {
        return target.put(uri);
    }

    public MockMvcResponse delete(URI uri)
    {
        return target.delete(uri);
    }

    public MockMvcRequestSpecification params(Map<String, ?> parametersMap)
    {
        return target.params(parametersMap);
    }

    public MockMvcResponse head(URI uri)
    {
        return target.head(uri);
    }

    public MockMvcResponse patch(URI uri)
    {
        return target.patch(uri);
    }

    public MockMvcResponse options(URI uri)
    {
        return target.options(uri);
    }

    public MockMvcResponse get(URL url)
    {
        return target.get(url);
    }

    public MockMvcResponse post(URL url)
    {
        return target.post(url);
    }

    public MockMvcRequestSpecification param(String parameterName,
                                             Object... parameterValues)
    {
        return target.param(parameterName, parameterValues);
    }

    public MockMvcResponse put(URL url)
    {
        return target.put(url);
    }

    public MockMvcResponse delete(URL url)
    {
        return target.delete(url);
    }

    public MockMvcResponse head(URL url)
    {
        return target.head(url);
    }

    public MockMvcResponse patch(URL url)
    {
        return target.patch(url);
    }

    public MockMvcRequestSpecification param(String parameterName,
                                             Collection<?> parameterValues)
    {
        return target.param(parameterName, parameterValues);
    }

    public MockMvcResponse options(URL url)
    {
        return target.options(url);
    }

    public MockMvcResponse get()
    {
        return target.get();
    }

    public MockMvcResponse post()
    {
        return target.post();
    }

    public MockMvcRequestSpecification queryParams(String firstParameterName,
                                                   Object firstParameterValue,
                                                   Object... parameterNameValuePairs)
    {
        return target.queryParams(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    public MockMvcResponse put()
    {
        return target.put();
    }

    public MockMvcResponse delete()
    {
        return target.delete();
    }

    public MockMvcResponse head()
    {
        return target.head();
    }

    public MockMvcResponse patch()
    {
        return target.patch();
    }

    public MockMvcRequestSpecification queryParams(Map<String, ?> parametersMap)
    {
        return target.queryParams(parametersMap);
    }

    public MockMvcResponse options()
    {
        return target.options();
    }

    public MockMvcResponse request(Method method)
    {
        return target.request(method);
    }

    public MockMvcRequestSpecification queryParam(String parameterName,
                                                  Object... parameterValues)
    {
        return target.queryParam(parameterName, parameterValues);
    }

    public MockMvcResponse request(String method)
    {
        return target.request(method);
    }

    public MockMvcResponse request(Method method,
                                   String path,
                                   Object... pathParams)
    {
        return target.request(method, path, pathParams);
    }

    public MockMvcRequestSpecification queryParam(String parameterName,
                                                  Collection<?> parameterValues)
    {
        return target.queryParam(parameterName, parameterValues);
    }

    public MockMvcResponse request(String method,
                                   String path,
                                   Object... pathParams)
    {
        return target.request(method, path, pathParams);
    }

    public MockMvcRequestSpecification formParams(String firstParameterName,
                                                  Object firstParameterValue,
                                                  Object... parameterNameValuePairs)
    {
        return target.formParams(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    public MockMvcResponse request(Method method,
                                   URI uri)
    {
        return target.request(method, uri);
    }

    public MockMvcResponse request(Method method,
                                   URL url)
    {
        return target.request(method, url);
    }

    public MockMvcResponse request(String method,
                                   URI uri)
    {
        return target.request(method, uri);
    }

    public MockMvcRequestSpecification formParams(Map<String, ?> parametersMap)
    {
        return target.formParams(parametersMap);
    }

    public MockMvcResponse request(String method,
                                   URL url)
    {
        return target.request(method, url);
    }

    public MockMvcRequestSpecification formParam(String parameterName,
                                                 Object... parameterValues)
    {
        return target.formParam(parameterName, parameterValues);
    }

    public MockMvcRequestSpecification formParam(String parameterName,
                                                 Collection<?> parameterValues)
    {
        return target.formParam(parameterName, parameterValues);
    }

    public MockMvcRequestSpecification attribute(String attributeName,
                                                 Object attributeValue)
    {
        return target.attribute(attributeName, attributeValue);
    }

    public MockMvcRequestSpecification attributes(Map<String, ?> attributesMap)
    {
        return target.attributes(attributesMap);
    }

    public MockMvcRequestSpecification body(String body)
    {
        return target.body(body);
    }

    public MockMvcRequestSpecification body(byte[] body)
    {
        return target.body(body);
    }

    public MockMvcRequestSpecification body(File body)
    {
        return target.body(body);
    }

    public MockMvcRequestSpecification body(Object object)
    {
        return target.body(object);
    }

    public MockMvcRequestSpecification body(Object object,
                                            ObjectMapper mapper)
    {
        return target.body(object, mapper);
    }

    public MockMvcRequestSpecification body(Object object,
                                            ObjectMapperType mapperType)
    {
        return target.body(object, mapperType);
    }

    public MockMvcRequestSpecification cookies(String firstCookieName,
                                               Object firstCookieValue,
                                               Object... cookieNameValuePairs)
    {
        return target.cookies(firstCookieName, firstCookieValue, cookieNameValuePairs);
    }

    public MockMvcRequestSpecification cookies(Map<String, ?> cookies)
    {
        return target.cookies(cookies);
    }

    public MockMvcRequestSpecification cookies(Cookies cookies)
    {
        return target.cookies(cookies);
    }

    public MockMvcRequestSpecification cookie(String cookieName,
                                              Object value,
                                              Object... additionalValues)
    {
        return target.cookie(cookieName, value, additionalValues);
    }

    public MockMvcRequestSpecification cookie(Cookie cookie)
    {
        return target.cookie(cookie);
    }

    public MockMvcRequestSpecification multiPart(File file)
    {
        return target.multiPart(file);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 File file)
    {
        return target.multiPart(controlName, file);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 File file,
                                                 String mimeType)
    {
        return target.multiPart(controlName, file, mimeType);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 Object object)
    {
        return target.multiPart(controlName, object);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 Object object,
                                                 String mimeType)
    {
        return target.multiPart(controlName, object, mimeType);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String filename,
                                                 Object object,
                                                 String mimeType)
    {
        return target.multiPart(controlName, filename, object, mimeType);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String fileName,
                                                 byte[] bytes)
    {
        return target.multiPart(controlName, fileName, bytes);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String fileName,
                                                 byte[] bytes,
                                                 String mimeType)
    {
        return target.multiPart(controlName, fileName, bytes, mimeType);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String fileName,
                                                 InputStream stream)
    {
        return target.multiPart(controlName, fileName, stream);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String fileName,
                                                 InputStream stream,
                                                 String mimeType)
    {
        return target.multiPart(controlName, fileName, stream, mimeType);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String contentBody)
    {
        return target.multiPart(controlName, contentBody);
    }

    public MockMvcRequestSpecification multiPart(String controlName,
                                                 String contentBody,
                                                 String mimeType)
    {
        return target.multiPart(controlName, contentBody, mimeType);
    }

    public MockMvcRequestSpecification config(RestAssuredMockMvcConfig config)
    {
        return target.config(config);
    }

    public MockMvcRequestSpecification spec(MockMvcRequestSpecification requestSpecificationToMerge)
    {
        return target.spec(requestSpecificationToMerge);
    }

    public MockMvcRequestSpecification sessionId(String sessionIdValue)
    {
        return target.sessionId(sessionIdValue);
    }

    public MockMvcRequestSpecification sessionId(String sessionIdName,
                                                 String sessionIdValue)
    {
        return target.sessionId(sessionIdName, sessionIdValue);
    }

    public MockMvcRequestSpecification sessionAttrs(Map<String, Object> sessionAttributes)
    {
        return target.sessionAttrs(sessionAttributes);
    }

    public MockMvcRequestSpecification sessionAttr(String name,
                                                   Object value)
    {
        return target.sessionAttr(name, value);
    }

    public MockMvcRequestAsyncSender when()
    {
        return target.when();
    }

    public MockMvcRequestSpecification standaloneSetup(Object... controllerOrMockMvcConfigurer)
    {
        return target.standaloneSetup(controllerOrMockMvcConfigurer);
    }

    public MockMvcRequestSpecification standaloneSetup(MockMvcBuilder builder)
    {
        return target.standaloneSetup(builder);
    }

    public MockMvcRequestSpecification mockMvc(MockMvc mockMvc)
    {
        return target.mockMvc(mockMvc);
    }

    public MockMvcRequestSpecification webAppContextSetup(WebApplicationContext context,
                                                          MockMvcConfigurer... mockMvcConfigurers)
    {
        return target.webAppContextSetup(context, mockMvcConfigurers);
    }

    public MockMvcRequestSpecification interceptor(MockHttpServletRequestBuilderInterceptor interceptor)
    {
        return target.interceptor(interceptor);
    }

    public MockMvcRequestSpecification and()
    {
        return target.and();
    }

    public MockMvcRequestSpecification postProcessors(RequestPostProcessor postProcessor,
                                                      RequestPostProcessor... additionalPostProcessors)
    {
        return target.postProcessors(postProcessor, additionalPostProcessors);
    }

    public MockMvcRequestSpecification accept(MediaType... mediaTypes)
    {
        return target.accept(mediaTypes);
    }
    
}
