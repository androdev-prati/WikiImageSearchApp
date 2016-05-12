package com.sample.wiki.imagesearch;

/** Data model that holds the search result's each item.
 *
 * @author Pratibha
 */
public class SearchItem {
    private String pageID;
    private String title;
    private String url;

    public void setPageID(String pageID) {
        this.pageID = pageID;
    }

    public String getPageID() {
        return pageID;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
