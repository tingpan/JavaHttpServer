package client;

import java.util.ArrayList;


public class Link {
    private String url;
    private String lastModifyTime;
    private boolean breakLink = false;
    private boolean overTime = true;
    private ArrayList<String> headers = new ArrayList<String>();

    public Link(String url) {
        this.setUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public boolean isBreakLink() {
        return breakLink;
    }

    public void setBreakLink(boolean breakLink) {
        this.breakLink = breakLink;
    }

    public boolean isOverTime() {
        return overTime;
    }

    public void setOverTime(boolean overTime) {
        this.overTime = overTime;
    }

    public ArrayList<String> getHeads() {
        return headers;
    }

}
