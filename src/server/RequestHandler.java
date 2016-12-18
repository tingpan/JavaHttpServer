package server;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class RequestHandler implements IRequestHandler {
    private static final String BAD_REQUEST = "HTTP/1.0 400 Bad Request\r\n\r\n";
    private static final String STATUS_OK = "HTTP/1.0 200 OK\r\n";
    private static final String HTTP_CRLF = "\r\n";
    private static final String HTTP_SP = " ";
    private static final String REG_REQUEST = "(^"
            + "([^\\x00-\\x1F\\x7F\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\{\\} \t]*)"
            + " " + "(/[^ ]*)" + " " + "(HTTP/[0-9]+\\.[0-9]+)" + ")" + "|"
            + "(^GET" + " " + "(/[^ ]*)" + ")";
    private byte[] entity = null;
    private String logContent = "";
    private HTTPFileServer httpFileServer;
    private ConcreteLogCenter logCenter;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatDate = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z");

    // Method for request process.
    public RequestHandler() {
        this.httpFileServer = HTTPFileServer.getInstance();
        this.logCenter = ConcreteLogCenter.getInstance();
    }

    // Method for set post entity.
    public void setEntity(byte[] entity) {
        this.entity = entity;
    }

    @Override
    public byte[] processRequest(byte[] request) {

        byte[] reByte = null;
        String strRequest = new String(request);
        String statusLine = strRequest.split(HTTP_CRLF)[0];
        String method = null;
        String fileName = null;

        if (validStatusLine(statusLine)) {
            method = statusLine.split(HTTP_SP)[0];
            fileName = statusLine.split(HTTP_SP)[1];
            logContent += getLogDetail(fileName, statusLine, true);
            switch (method) {
                case "GET":
                    reByte = responseGet(fileName, getModifiedSince(strRequest),
                            isSimpleRequest(statusLine));
                    break;
                case "POST":
                    reByte = responsePost(fileName);
                    break;
                case "HEAD":
                    reByte = responseHead(fileName);
                    break;
                default:
                    reByte = responseOtherMethod(fileName, method);
            }
        } else {
            logContent += getLogDetail(fileName, statusLine, true);
            reByte = responseBad(fileName);
        }
        return reByte;
    }

    // Method for validating the status line of request.
    private boolean validStatusLine(String statusLine) {
        boolean result;
        Pattern regExpGet = Pattern.compile(REG_REQUEST);

        result = regExpGet.matcher(statusLine).matches();
        return result;
    }

    private boolean isSimpleRequest(String statusLine) {
        int simpleFields = 2;
        String[] fields = statusLine.split(HTTP_SP);
        return fields.length == simpleFields ? true : false;
    }

    private String getLogDetail(String requestFile, String Logline,
                                boolean isRequest) {
        String content;
        if (isRequest) {
            content = formatDate.format(calendar.getTime()) + ", >, "
                    + requestFile + ", " + Logline + "\r\n";
        } else {
            content = formatDate.format(calendar.getTime()) + ", <, "
                    + requestFile + ", " + Logline.split(HTTP_CRLF)[0] + "\r\n";
        }
        return content;
    }

    // Identify the request and return the corresponding response.
    private byte[] responseOtherMethod(String fileName, String method) {
        String logLine = "HTTP/1.0 501 " + method + " Not Implemented\r\n\r\n";
        logContent += getLogDetail(fileName, logLine, false);
        logCenter.setLog(logContent);
        return logLine.getBytes();
    }

    private byte[] responseGet(String fileName, Date modifiedSince,
                               boolean isSimpleRequest) {
        byte[] reBytes = null;
        byte[] content = null;
        byte[] statusLine = null;
        String logLine = null;

        try {
            if (isSimpleRequest) {
                content = httpFileServer.httpGetSimple(fileName);
                logLine = "This is a simple request\r\n\r\n";
                return content;
            } else if (modifiedSince != null) {
                content = httpFileServer.httpGETconditional(fileName,
                        modifiedSince);
            } else {
                content = httpFileServer.httpGet(fileName);
            }
            statusLine = encodeString(STATUS_OK);
            logLine = new String(statusLine);
            reBytes = combineBytes(statusLine, content);
        } catch (HTTPFileNotFoundException | HTTPPermissionDeniedException
                | HTTPRuntimeException e) {
            logLine = e.getMessage();
            reBytes = encodeString(logLine);
        } finally {
            logContent += getLogDetail(fileName, logLine, false);
            logCenter.setLog(logContent);
        }
        return reBytes;
    }

    private byte[] responsePost(String fileName) {
        byte[] reBytes = null;
        byte[] content = null;
        byte[] statusLine = null;
        String logLine = null;

        try {
            if (entity != null) {
                content = httpFileServer.httpPOST(fileName, entity);
                statusLine = encodeString(STATUS_OK);
                logLine = new String(statusLine);
                reBytes = combineBytes(statusLine, content);
            } else {

                // Post request without entity is treated as bad request based
                // on specification.
                logLine = BAD_REQUEST;
                reBytes = encodeString(BAD_REQUEST);
            }
        } catch (HTTPFileNotFoundException | HTTPPermissionDeniedException
                | HTTPRuntimeException e) {
            logLine = e.getMessage();
            reBytes = encodeString(logLine);
        } finally {
            logContent += getLogDetail(fileName, logLine, false);
            logCenter.setLog(logContent);
        }
        return reBytes;
    }

    private byte[] responseHead(String fileName) {
        byte[] reBytes = null;
        byte[] content = null;
        byte[] statusLine = null;
        String logLine = null;

        try {
            content = httpFileServer.httpHEAD(fileName);
            statusLine = encodeString(STATUS_OK);
            logLine = new String(statusLine);
            reBytes = combineBytes(statusLine, content);
        } catch (HTTPFileNotFoundException | HTTPPermissionDeniedException
                | HTTPRuntimeException e) {
            logLine = e.getMessage();
            reBytes = encodeString(logLine);
        } finally {
            logContent += getLogDetail(fileName, logLine, false);
            logCenter.setLog(logContent);
        }
        return reBytes;
    }

    private byte[] responseBad(String fileName) {
        logContent += getLogDetail(fileName, BAD_REQUEST, false);
        logCenter.setLog(logContent);
        return BAD_REQUEST.getBytes();
    }

    private byte[] encodeString(String encodStr) {
        try {
            return encodStr.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    private byte[] combineBytes(byte[] headers, byte[] fileBytes) {
        byte[] reByte = new byte[headers.length + fileBytes.length];

        System.arraycopy(headers, 0, reByte, 0, headers.length);
        System.arraycopy(fileBytes, 0, reByte, headers.length, fileBytes.length);
        return reByte;
    }

    // Three different date format.
    private Date getModifiedSince(String request) {
        String modifyDateStr = null;
        String[] dateFormats = {"E, dd MMM yyyy HH:mm:ss z",
                "E, dd-MMM-yy HH:mm:ss z", "E MMM d HH:mm:ss yyyy"};
        String[] lines = request.split(HTTP_CRLF);
        Pattern pattern = Pattern.compile("^If-Modified-Since: (.*)");
        Date modifiedDate = null;

        for (String line : lines) {
            if (pattern.matcher(line).matches()) {
                modifyDateStr = pattern.matcher(line).replaceAll("$1");
                break;
            }
        }

        // Parse three types of the date.
        if (modifyDateStr != null) {
            for (String dateFormat : dateFormats) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(dateFormat,
                            Locale.ENGLISH);
                    modifiedDate = format.parse(modifyDateStr);
                } catch (ParseException e) {
                    continue;
                }
            }
        }
        return modifiedDate;
    }
}
