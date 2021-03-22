package com.viettel.voffice.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

import com.viettel.voffice.database.entity.EntityImageSignature;

public class AnnotationDrawer extends PageDrawer {
    public AnnotationDrawer(int imageType) throws IOException {
        super();
        this.imageType = imageType;
    }

    public AnnotationDrawer () throws IOException{
        super();
        this.imageType = BufferedImage.TYPE_INT_ARGB;
    }

    public List<EntityImageSignature> convertToImages(PDPage p, int pageNumber) throws IOException {
        page = p;
        final List<EntityImageSignature> result = new ArrayList<EntityImageSignature>();
        EntityImageSignature imageSignature = null;
        List<PDAnnotation> annotations = page.getAnnotations();
        for (PDAnnotation annotation : annotations) {
            String appearanceName = annotation.getAppearanceStream();
            PDAppearanceDictionary appearDictionary = annotation.getAppearance();
            if (appearDictionary != null && "widget".equalsIgnoreCase(annotation.getSubtype())) {
                if (appearanceName == null) {
                    appearanceName = "default";
                }
                Map<String, PDAppearanceStream> appearanceMap = (Map<String, PDAppearanceStream>) appearDictionary
                        .getNormalAppearance();
                if (appearanceMap != null) {
                    PDAppearanceStream appearance = (PDAppearanceStream) appearanceMap.get(appearanceName);
                    if (appearance != null) {
                        BufferedImage image = initializeGraphics(annotation);
                        if (image != null) {
                            setTextMatrix(null);
                            setTextLineMatrix(null);
                            getGraphicsStack().clear();
                            processSubStream(page, appearance.getResources(), appearance.getStream());
    
                            String name = annotation.getAnnotationName();
                            if (name == null || name.length() == 0) {
                                name = annotation.getDictionary().getString(COSName.T);
                                if (name == null || name.length() == 0) {
                                    name = Long.toHexString(annotation.hashCode());
                                }
                            }
                            
                            imageSignature = new EntityImageSignature();
                            imageSignature.setPage(pageNumber);
                            imageSignature.setImage(image);
                            imageSignature.setPosition(annotation.getRectangle());
                            
                            result.add(imageSignature);
                        }
                    }
                }
            }
        }

        return result;
    }

    BufferedImage initializeGraphics(PDAnnotation annotation) {
        PDRectangle rect = annotation.getRectangle();
        float widthPt = rect.getWidth();
        float heightPt = rect.getHeight();
        if (widthPt > 0 && heightPt > 0) {
            int widthPx = Math.round(widthPt * SCALE_PERCENT);
            int heightPx = Math.round(heightPt * SCALE_PERCENT);
            Dimension pageDimension = new Dimension((int) widthPt, (int) heightPt);
            BufferedImage retval = new BufferedImage(widthPx, heightPx, imageType);
            Graphics2D graphics = (Graphics2D) retval.getGraphics();
            graphics.setBackground(TRANSPARENT_WHITE);
            graphics.clearRect(0, 0, retval.getWidth(), retval.getHeight());
            graphics.scale(SCALE_PERCENT, SCALE_PERCENT);
            setGraphics(graphics);
            pageSize = pageDimension;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            setGraphicsState(new PDGraphicsState(new PDRectangle(widthPt, heightPt)));
    
            return retval;
        }
        return null;
    }

    void setGraphics(Graphics2D graphics) {
        try {
            Field field = PageDrawer.class.getDeclaredField("graphics");
            field.setAccessible(true);
            field.set(this, graphics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Scale * 10 de lay anh cho ro net
    public static final int SCALE_PERCENT = 10;
    private static final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 0);

    private int imageType;
}
