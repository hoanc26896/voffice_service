/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tond
 */
public class SearchTextExtractionStrategy implements TextExtractionStrategy {

    private Integer page;
    private String searchText;
    private StringBuilder builder = new StringBuilder();
    private List<TextLocationInfo> locations = new ArrayList<>();

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    /**
     *
     * @param builder
     */
    public void setBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    /**
     *
     * @return
     */
    public List<TextLocationInfo> getLocations() {
        return locations;
    }

    /**
     *
     * @param locations
     */
    public void setLocations(List<TextLocationInfo> locations) {
        this.locations = locations;
    }

    /**
     *
     * @param page
     * @param searchText
     */
    public SearchTextExtractionStrategy(Integer page, String searchText) {
        this.page = page;
        this.searchText = searchText;
    }

    @Override
    public String getResultantText() {
        String result = null;

        if (locations.size() > 0) {
            result = searchText;
        }
        return result;
    }

    @Override
    public void beginTextBlock() {
    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
        builder.append(renderInfo.getText());
        if (builder.toString().endsWith(searchText)) {
            builder.setLength(0);
            Vector startPoint = renderInfo.getBaseline().getStartPoint();
            Vector endPoint = renderInfo.getAscentLine().getEndPoint();
            Rectangle rectangle = new Rectangle(startPoint.get(Vector.I1), startPoint.get(Vector.I2), endPoint.get(Vector.I1), endPoint.get(Vector.I2));
            locations.add(new TextLocationInfo(page, searchText, rectangle));
        }
    }

    @Override
    public void endTextBlock() {
    }

    @Override
    public void renderImage(ImageRenderInfo iri) {
    }
}
