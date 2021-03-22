/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

import com.itextpdf.text.Rectangle;

/**
 *
 * @author tond
 */
public class TextLocationInfo {

    private int page;
    private String search;
    private Rectangle position;

    /**
     *
     * @param page
     * @param search
     * @param position
     */
    public TextLocationInfo(Integer page, String search, Rectangle position) {
        this.page = page;
        this.search = search;
        this.position = position;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Rectangle getPosition() {
        return position;
    }

    public void setPosition(Rectangle position) {
        this.position = position;
    }
}
