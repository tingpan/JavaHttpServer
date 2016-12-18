package client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientTests {
    private HttpClient testClient;
    private String strResponse;
    private ArrayList<Link> links = new ArrayList<Link>();

    public ClientTests(HttpClient testClient) {
        this.testClient = testClient;
    }

    public void runTests(String urlString) {
        strResponse = testClient.httpGet(urlString);
        testClient.getResponse();

        if (testClient.getResponse() == null || testClient.responseFail()) {
            System.exit(-1);
        }
        searchLinks(strResponse);
        printInfo();
    }

    private void searchLinks(String response) {
        String linkPatternStr = "<a href=\"(.*)\">.*</a>";
        Pattern linkPattern = Pattern.compile(linkPatternStr);
        Matcher matchers = linkPattern.matcher(response);
        Link newlink;

        if (matchers.find()) {
            matchers.reset();
            while (matchers.find()) {
                String urlStr = linkPattern.matcher(matchers.group()).replaceAll("$1");

                //To reduce the number of get request, if the link was already existed in the array,
                //the link will be coped and append to the end of the Arraylist.
                if ((newlink = notExistLink(urlStr)) != null) {
                    links.add(newlink);
                } else {
                    newlink = new Link(urlStr);
                    setInfo(newlink);
                    links.add(newlink);
                }
            }
        }
    }

    private Link notExistLink(String urlStr) {
        for (Link existLink : links) {
            if (existLink.getUrl().equals(urlStr)) {
                return existLink;
            }
        }
        return null;
    }

    private void setInfo(Link newlink) {
        String time = "";
        testClient.httpHead(newlink.getUrl());

        if (testClient.responseFail()) {
            newlink.setBreakLink(true);
            return;
        }
        if (testClient.getResponse().getFirstHeader("Last-Modified") != null) {
            time = testClient.getResponse().getFirstHeader("Last-Modified")
                    .getValue();
        }
        newlink.setLastModifyTime(time);
        if (notOverTime(newlink)) {
            setHeader(newlink);
            newlink.setOverTime(false);
        }
    }

    private void setHeader(Link newlink) {
        String content = testClient.httpGet(newlink.getUrl());
        String headerPattern = "<h[1-3]>(.*)</h[1-3]>";
        Pattern header = Pattern.compile(headerPattern);
        Matcher matchers = header.matcher(content);

        if (matchers.find()) {
            matchers.reset();
            while (matchers.find()) {
                String newHead = header.matcher(matchers.group()).replaceAll("$1");
                newlink.getHeads().add(newHead);
            }
        }
    }

    private boolean notOverTime(Link newlink) {
        int compareResult = -2;
        int unvalidResult = -1;
        String modifyDateStr = newlink.getLastModifyTime();
        String[] dateFormats = {"E, dd MMM yyyy HH:mm:ss z",
                "E, dd-MMM-yy HH:mm:ss z", "E MMM  d HH:mm:s yyyy"};
        Date modifyDate = null;
        Calendar modifyDateC = null;
        Calendar nowDateC = null;

        if (!modifyDateStr.equals("")) {
            for (String dateFormat : dateFormats) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
                    modifyDate = format.parse(modifyDateStr);
                } catch (ParseException e) {
                }
            }
            if (modifyDate == null) {
                return false;
            }
            modifyDateC = Calendar.getInstance();
            modifyDateC.setTime(modifyDate);
            modifyDateC.add(Calendar.MONTH, +6);
            nowDateC = Calendar.getInstance();
            compareResult = modifyDateC.compareTo(nowDateC);
            if (compareResult == unvalidResult) {
                return false;
            }
        }
        return true;
    }

    private void printInfo() {
        for (Link eachLink : links) {
            System.out.println(eachLink.getUrl());
            if (eachLink.isBreakLink()) {
                System.out.println("This is a broken link.\n");
                continue;
            }
            if (eachLink.isOverTime()) {
                System.out.println("Page last modified over 6 months ago.\n");
                continue;
            }
            System.out.println(eachLink.getLastModifyTime());
            for (String eachHead : eachLink.getHeads()) {
                System.out.println(eachHead);
            }
            System.out.println();
        }
    }
}
