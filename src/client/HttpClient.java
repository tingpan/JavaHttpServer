package client;

import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/* You will need to use this class to build your HttpClient.
 * It needs to implement the interface IHttpClient and so it
 * will need to have the methods for httpGet, httpHead and httpPost
 */

public class HttpClient implements IHttpClient {
    private static final String IO_EXCEPTION = "Can not get the content.";
    private static final String ENCO_EXCEPTION = "The encoding of input is unsupported.";
    private CloseableHttpClient httpclient;
    private CloseableHttpResponse httpResponse = null;

    public HttpClient() {
        httpclient = HttpClients.createDefault();
    }

    @Override
    public String httpGet(String url) {
        String strResult = null;
        HttpGet requestGet = new HttpGet(url);

        requestGet.setProtocolVersion(HttpVersion.HTTP_1_0);
        try {
            httpResponse = httpclient.execute(requestGet);

            // If response failed than return status line.
            if (responseFail()) {
                strResult = httpResponse.getStatusLine().toString();
                httpResponse.close();
                return strResult;
            }

            // If request succeeded than return the content of the page.
            HttpEntity entity = httpResponse.getEntity();
            strResult = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            httpResponse.close();
        } catch (IOException e) {
            System.err.println(IO_EXCEPTION);
        }
        return strResult;
    }

    @Override
    public String httpHead(String url) {
        String strResult = "";
        HttpHead requestHead = new HttpHead(url);

        requestHead.setProtocolVersion(HttpVersion.HTTP_1_0);
        try {
            httpResponse = httpclient.execute(requestHead);
            if (responseFail()) {
                strResult = httpResponse.getStatusLine().toString();
                httpResponse.close();
            }

            // Get headers and append them into the return string.
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                strResult += header + "\n";
            }
            httpResponse.close();
        } catch (IOException e) {
            System.err.println(IO_EXCEPTION);
        }
        return strResult;
    }

    @Override
    public String httpPost(String url, String body) {
        String strResult = null;
        StringEntity postEntity = null;
        HttpPost requestPost = new HttpPost(url);

        // Set string as post entity.
        try {
            postEntity = new StringEntity(body);
        } catch (UnsupportedEncodingException e1) {
            System.err.println(ENCO_EXCEPTION);
            return null;
        }
        requestPost.setProtocolVersion(HttpVersion.HTTP_1_0);
        requestPost.setEntity(postEntity);

        // Send request and handle the failed request.
        try {
            httpResponse = httpclient.execute(requestPost);
            if (responseFail()) {
                strResult = httpResponse.getStatusLine().toString();
                httpResponse.close();
                return strResult;
            }
            HttpEntity entity = httpResponse.getEntity();
            strResult = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            httpResponse.close();
        } catch (IOException e) {
            System.err.println(IO_EXCEPTION);
        }
        return strResult;
    }

    @Override
    public String httpPost(String url, ArrayList<NameValuePair> nameValuePairs) {
        String strResult = null;
        HttpPost requestPost = new HttpPost(url);

        // Set name-value pairs as post entity.
        try {
            requestPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e1) {
            System.err.println(ENCO_EXCEPTION);
            return null;
        }
        requestPost.setProtocolVersion(HttpVersion.HTTP_1_0);
        try {
            httpResponse = httpclient.execute(requestPost);
            if (responseFail()) {
                strResult = httpResponse.getStatusLine().toString();
                httpResponse.close();
                return strResult;
            }
            HttpEntity entity = httpResponse.getEntity();
            strResult = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            httpResponse.close();
        } catch (IOException e) {
            System.err.println(IO_EXCEPTION);
        }
        return strResult;
    }

    // Test if the request status is successful.
    public boolean responseFail() {
        return httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK;
    }

    public CloseableHttpResponse getResponse() {
        return httpResponse;
    }

}
