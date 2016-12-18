package server;// make sure to add JUnit to your project to run this class.

import org.junit.Assert;
import org.junit.Test;

/*
 * 3 tests are provided for you, these WILL be part of the marking criteria for functionality tests.
 * It's a good idea to make sure they pass, and add some more tests here.
 * You should not modify the existing tests.
 */
public class ServerJunitTests {
    private static final String FILF_ROOT = "./root";
    private static final String LOG = "./root/log.txt";
    private static final String HTTP_CRLF = "\r\n";
    private static final String STATUS_OK = "HTTP/1.0 200 OK";
    private static final String FILE_NOT_FOUND = "HTTP/1.0 404 Not Found\r\n\r\n";
    private static final String FILE_NO_PREMISSION = "HTTP/1.0 403 Forbidden\r\n\r\n";
    private static final String FILE_CREATED = "HTTP/1.0 201 Created\r\n\r\n";
    private static final String NOT_MODIFIED = "HTTP/1.0 304 Not modified\r\n\r\n";
    private static final String UKNOWN_METHOD_1_0 = "HTTP/1.0 501 Rabble Not Implemented\r\n\r\n";    //For some unknown method.
    private static final String BAD_REQUEST_1_0 = "HTTP/1.0 400 Bad Request\r\n\r\n";

    ConcreteLogCenter logCenter = ConcreteLogCenter.getInstance();
    FileLog fileLog = new FileLog();


    @Test
    public void testGetRequestHttp1() {
        logCenter.addObserver(fileLog);
        ConcreteLogCenter.setLogMode(true);
        fileLog.setLogRoot(LOG);
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        // Request handler needs to have a constructor with no arguments.
        RequestHandler requestHandler = new RequestHandler();
        expected = STATUS_OK;
        actual = new String(requestHandler.processRequest(request.getBytes())).split(HTTP_CRLF)[0];
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testHeadRequestHttp1() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "HEAD /index.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = STATUS_OK;
        actual = new String(requestHandler.processRequest(request.getBytes())).split(HTTP_CRLF)[0];
        Assert.assertEquals(expected, actual);
    }

    // Test post2
    @Test
    public void testHttpPost() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "POST /index.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        requestHandler.setEntity("aaaaa".getBytes());
        expected = STATUS_OK;
        actual = new String(requestHandler.processRequest(request.getBytes())).split(HTTP_CRLF)[0];
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInvalidRequest() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "Rabble rabble";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = BAD_REQUEST_1_0;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    // Test simple request.
    @Test
    public void testSimpleRequest() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = "<!DOCTYPE html>";
        actual = new String(requestHandler.processRequest(request.getBytes())).split(HTTP_CRLF)[0];
        Assert.assertEquals(expected, actual);
    }

    // Test with custom but valid method.
    @Test
    public void testUnknowMethod() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "Rabble /index.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = UKNOWN_METHOD_1_0;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getNotFound() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /aaa.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = FILE_NOT_FOUND;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void headNotFound() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "HEAD /aaa.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = FILE_NOT_FOUND;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void postNotFound() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "POST /aaa.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        requestHandler.setEntity("aaaaa".getBytes());
        expected = FILE_CREATED;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    public void getForbid() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /page1.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = FILE_NO_PREMISSION;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void headForbid() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "HEAD /page1.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = FILE_NO_PREMISSION;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void postForbid() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "POST /page1.html HTTP/1.0\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        requestHandler.setEntity("aaaaa".getBytes());
        expected = FILE_NO_PREMISSION;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getConditional1() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: Sun Nov 6 08:49:37 2015\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = NOT_MODIFIED;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getConditional2() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: Sunday, 06-Nov-15 08:49:37 GMT\r\n\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = NOT_MODIFIED;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getConditional3() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: Sun, 06 Nov 2015 08:49:37 GMT\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = NOT_MODIFIED;
        actual = new String(requestHandler.processRequest(request.getBytes()));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getConditionalinTime() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: Sun, 06 Nov 1994 08:49:37 GMT\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = STATUS_OK;
        actual = new String(requestHandler.processRequest(request.getBytes())).split(HTTP_CRLF)[0];
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getConditionalBadTime() {
        HTTPFileServer.setRootDir(FILF_ROOT);
        String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: rabble\r\n\r\n";
        String expected;
        String actual;

        RequestHandler requestHandler = new RequestHandler();
        expected = STATUS_OK;
        actual = new String(requestHandler.processRequest(request.getBytes())).split(HTTP_CRLF)[0];
        Assert.assertEquals(expected, actual);
    }
}

