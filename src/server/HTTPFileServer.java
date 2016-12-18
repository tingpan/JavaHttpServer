package server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HTTPFileServer implements IServe {
    private static final String HTTP_CRLF = "\r\n";
    private static final String FILE_NOT_FOUND = "HTTP/1.0 404 Not Found\r\n\r\n";
    private static final String FILE_NO_PREMISSION = "HTTP/1.0 403 Forbidden\r\n\r\n";
    private static final String RUNTIME_ERROR = "HTTP/1.0 500 Internal Server Error\r\n\r\n";
    private static final String FILE_CREATED = "HTTP/1.0 201 Created\r\n\r\n";
    private static final String NOT_MODIFIED = "HTTP/1.0 304 Not modified\r\n\r\n";
    private static final HTTPFileServer INSTANCE = new HTTPFileServer();
    private static String rootDir;
    private String fileName;
    private FileInputStream requestFile;
    private File reqFile;

    private HTTPFileServer() {
    }

    public static void setRootDir(String rootDir) {
        HTTPFileServer.rootDir = rootDir;
    }

    //Singleton
    public static HTTPFileServer getInstance() {
        return INSTANCE;
    }

    public byte[] httpGet(String requestURI) throws HTTPFileNotFoundException,
            HTTPRuntimeException, HTTPPermissionDeniedException {
        byte[] fileByte = null;
        byte[] headers = null;
        byte[] reByte = null;

        initialFile(requestURI);
        try {
            fileByte = readFile();
            headers = creatHeaders(fileByte.length);
            reByte = combineBytes(headers, fileByte);
            return reByte;
        } catch (FileNotFoundException e) {
            throw new HTTPFileNotFoundException(FILE_NOT_FOUND, e);
        } catch (IOException e2) {
            throw new HTTPRuntimeException(RUNTIME_ERROR, e2);
        }
    }

    public byte[] httpGetSimple(String requestURI)
            throws HTTPFileNotFoundException, HTTPRuntimeException,
            HTTPPermissionDeniedException {
        byte[] fileByte = null;

// 		System.out.println("This is a simple request");
        initialFile(requestURI);
        try {
            fileByte = readFile();
            return fileByte;
        } catch (FileNotFoundException e) {
            throw new HTTPFileNotFoundException(FILE_NOT_FOUND, e);
        } catch (IOException e2) {
            throw new HTTPRuntimeException(RUNTIME_ERROR, e2);
        }
    }

    @Override
    public byte[] httpGETconditional(String requestURI, Date ifModifiedSince)
            throws HTTPFileNotFoundException, HTTPRuntimeException,
            HTTPPermissionDeniedException {
        byte[] fileByte = null;
        byte[] headers = null;
        byte[] reByte = null;

//		System.out.println("This is a conditional get request");
        initialFile(requestURI);
        try {
            fileByte = readFile();
            headers = creatHeaders(fileByte.length);
            if (modifyLaterThan(ifModifiedSince)) {
                reByte = combineBytes(headers, fileByte);
            } else {
                throw new HTTPRuntimeException(NOT_MODIFIED);
            }
            return reByte;
        } catch (FileNotFoundException e) {
            throw new HTTPFileNotFoundException(FILE_NOT_FOUND, e);
        } catch (IOException e2) {
            throw new HTTPRuntimeException(RUNTIME_ERROR, e2);
        }
    }

    @Override
    public byte[] httpHEAD(String requestURI) throws HTTPFileNotFoundException,
            HTTPRuntimeException, HTTPPermissionDeniedException {
        byte[] fileByte = null;
        byte[] headers = null;

        initialFile(requestURI);
        try {
            fileByte = readFile();
            headers = creatHeaders(fileByte.length);
            return headers;
        } catch (FileNotFoundException e) {
            throw new HTTPFileNotFoundException(FILE_NOT_FOUND, e);
        } catch (IOException e2) {
            throw new HTTPPermissionDeniedException(FILE_NO_PREMISSION, e2);
        }
    }

    @Override
    public byte[] httpPOST(String requestURI, byte[] postData)
            throws HTTPFileNotFoundException, HTTPRuntimeException,
            HTTPPermissionDeniedException {
        byte[] fileByte = null;
        byte[] headers = null;
        byte[] reByte = null;

        initialFile(requestURI);
        try {
            fileByte = readFile();
            headers = creatHeaders(fileByte.length);
            reByte = combineBytes(headers, fileByte);
            return reByte;
        } catch (FileNotFoundException e) {
            createFile(postData);
            throw new HTTPFileNotFoundException(FILE_CREATED, e);
        } catch (IOException e2) {
            throw new HTTPPermissionDeniedException(RUNTIME_ERROR, e2);
        }
    }

    private synchronized void createFile(byte[] postData)
            throws HTTPPermissionDeniedException {
        File createdFile = new File(fileName);

        try {
            if (!createdFile.exists()) {
                createdFile.createNewFile();
                FileOutputStream fileOutput = new FileOutputStream(createdFile);
                fileOutput.write(postData);
                fileOutput.close();
            }
        } catch (IOException e) {
            throw new HTTPPermissionDeniedException(FILE_NO_PREMISSION, e);
        }
    }

    private byte[] readFile() throws IOException, HTTPPermissionDeniedException {
        byte[] fileByte = null;

        if (!reqFile.exists()) {
            throw new FileNotFoundException();
        }
        if (!reqFile.canRead()) {
            throw new HTTPPermissionDeniedException(FILE_NO_PREMISSION);
        }
        requestFile = new FileInputStream(fileName);
        fileByte = new byte[requestFile.available()];
        requestFile.read(fileByte);
        requestFile.close();
        return fileByte;
    }

    private boolean modifyLaterThan(Date compareDate) {
        int result = -1;
        int expectResult = -1;
        Calendar compareCalendar = Calendar.getInstance();
        Calendar modifiedCalendar = Calendar.getInstance();

        modifiedCalendar.setTime(new Date(reqFile.lastModified()));
        compareCalendar.setTime(compareDate);
        result = compareCalendar.compareTo(modifiedCalendar);
        return result > expectResult ? false : true;

    }

    private byte[] creatHeaders(int length) throws IOException,
            HTTPRuntimeException {
        byte[] reByte = null;
        String headers = null;
        String date = null;
        String lastModified = null;
        String contentLength = null;
        String contentType = null;
        Calendar headerCalendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        SimpleDateFormat formatDate = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        Path requestPath = Paths.get(reqFile.getAbsolutePath());

        formatDate.setTimeZone(timeZone);
        date = "Date: " + formatDate.format(headerCalendar.getTime())
                + HTTP_CRLF;
        lastModified = "Last-Modified: "
                + formatDate.format(new Date(reqFile.lastModified()))
                + HTTP_CRLF;
        contentType = "Content-Type: " + Files.probeContentType(requestPath)
                + HTTP_CRLF + HTTP_CRLF;
        contentLength = "Content-Length: " + length + HTTP_CRLF;
        headers = date + lastModified + contentLength + contentType;
        try {
            reByte = headers.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new HTTPRuntimeException(RUNTIME_ERROR, e);
        }
        return reByte;
    }

    private byte[] combineBytes(byte[] headers, byte[] fileBytes) {
        byte[] reByte = new byte[headers.length + fileBytes.length];

        System.arraycopy(headers, 0, reByte, 0, headers.length);
        System.arraycopy(fileBytes, 0, reByte, headers.length, fileBytes.length);
        return reByte;
    }

    private void initialFile(String requestURI) {
        fileName = rootDir + requestURI;
        reqFile = new File(fileName);
    }
}