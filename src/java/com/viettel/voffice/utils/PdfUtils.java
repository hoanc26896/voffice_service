/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.SearchTextExtractionStrategy;
import com.viettel.voffice.constants.TextLocationInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author thanght6
 */
public class PdfUtils {
    
    // Ghi loi
    public static final Logger LOGGER = Logger.getLogger(PdfUtils.class);

    /**
     * <b>Tim kiem vi tri cua chuoi trong file pdf</b><br>
     * 
     * @param pdfFile Duong dan file pdf
     * @param searchText Chuoi can tim
     * @return
     * @throws IOException
     */
    private static List<TextLocationInfo> findLocations(String pdfFile, String searchText)
            throws IOException {

        List<TextLocationInfo> result = new ArrayList<>();
        PdfReader reader = new PdfReader(pdfFile);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        SearchTextExtractionStrategy strategy;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            strategy = parser.processContent(i, new SearchTextExtractionStrategy(i, searchText));
            if (strategy != null) {
                List<TextLocationInfo> locations = strategy.getLocations();
                if ((locations != null) && (locations.size() > 0)) {
                    result.addAll(locations);
                }
            }
        }
        reader.close();
        return result;
    }

    /**
     * <b>Them comment vao file pdf ben duoi chuoi tim kiem</b><br>
     * 
     * @param pdfFile       duong dan file PDF nguon
     * @param searchText    chuoi dung tren vi tri comment
     * @return 
     */
    public static boolean addSignPlaceHolder(String pdfFile, String searchText) {
        
        boolean result = false;
        try {
            List<TextLocationInfo> listLocation = findLocations(pdfFile, searchText);
            if (!CommonUtils.isEmpty(listLocation)) {
                Date date = new Date();
                String outputFile = pdfFile + date.getTime() + ".pdf";
                // Vi tri cua chuoi tim kiem dau tien
                TextLocationInfo location = listLocation.get(0);
                Rectangle position = location.getPosition();
                int shiftY = -90;
                int page = location.getPage();
                float x = ((position.getLeft() + position.getRight()) / 2) + 45;
                float y = position.getTop() + shiftY;
                boolean append = false;
                PdfReader reader = new PdfReader(pdfFile);

                if ((y < Constants.SignatureImageSize.HEIGHT) && (page < reader.getNumberOfPages())) {
                    float pageHeight = reader.getPageSize(reader.getNumberOfPages()).getHeight();
                    append = true;
                    y = pageHeight + (2 * shiftY);
                }

                Rectangle pageSize = reader.getPageSize(1);
                Document document = new Document(pageSize);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
                document.open();
                PdfContentByte contentByte = writer.getDirectContent();
                Calendar calendar = Calendar.getInstance();

                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    PdfImportedPage importedPage = writer.getImportedPage(reader, i);
                    contentByte.addTemplate(importedPage, 0.0F, 0.0F);
                    if ((!append) && (i == page)) {
                        Rectangle rectangle = new Rectangle(x, y, x, y);
                        PdfAnnotation annotation = PdfAnnotation.createText(writer,
                                rectangle, "Signature", "1", true, "Comment");
                        annotation.put(PdfName.TYPE, PdfName.ANNOT);
                        annotation.put(PdfName.M, new PdfDate(calendar));
                        annotation.put(PdfName.CREATIONDATE, new PdfDate(calendar));
                        contentByte.addAnnotation(annotation, true);
                    }
                    pageSize = reader.getPageSize((i < page) ? (i + 1) : page);
                    document.setPageSize(pageSize);
                    document.newPage();
                }
                if (append) {
                    Rectangle rectangle = new Rectangle(x, y, x, y);
                    PdfAnnotation annotation = PdfAnnotation.createText(writer,
                            rectangle, "Signature", "1", true, "Comment");
                    annotation.put(PdfName.TYPE, PdfName.ANNOT);
                    annotation.put(PdfName.M, new PdfDate(calendar));
                    annotation.put(PdfName.CREATIONDATE, new PdfDate(calendar));
                    contentByte.addAnnotation(annotation, true);
                }
                document.close();
                reader.close();

                manipulatePdf(outputFile, pdfFile);
                File file = new File(outputFile);
                if (file.exists()) {
                    file.delete();
                }
                result = true;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }
    
    public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        int n = reader.getNumberOfPages();
        int rot;
        PdfDictionary pageDict;
        for (int i = 1; i <= n; i++) {
            rot = reader.getPageRotation(i);
            pageDict = reader.getPageN(i);
            pageDict.put(PdfName.ROTATE, new PdfNumber(rot + 90));
        }
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        stamper.close();
        reader.close();
    }
        
    /**
     * <b>Tim kiem vi tri cua chuoi trong file pdf</b><br>
     * 
     * @param pdfFile           duong dan file pdf
     * @param listKey           danh sach chuoi can tim
     * @param isNotDuplicate    khong lay trung
     * @return
     * @throws IOException
     */
    private static List<TextLocationInfo> findLocations(String pdfFile,
            List<String> listKey, boolean isNotDuplicate)
            throws IOException {

        Set<String> setKey = new LinkedHashSet<>(listKey);
        List<TextLocationInfo> result = new ArrayList<>();
        PdfReader reader = new PdfReader(pdfFile);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        SearchTextExtractionStrategy strategy;
        List<TextLocationInfo> listLocation;
        // Tim kiem theo tung tu khoa
        for (String key : setKey) {
            if (!CommonUtils.isEmpty(key)) {
                listLocation = new ArrayList<>();
                // Duyet tung trang cua file
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    // Tim kiem cac vi tri trung voi key trong trang
                    strategy = parser.processContent(i, new SearchTextExtractionStrategy(i, key));
                    if (strategy != null && !CommonUtils.isEmpty(strategy.getLocations())) {
                        // Them cac vi tri tim duoc trong trang vao danh sach
                        listLocation.addAll(strategy.getLocations());
                    }
                }
                if (!CommonUtils.isEmpty(listLocation)) {
                    // Chi lay vi tri tim kiem duoc cuoi cung
                    if (isNotDuplicate) {
                        result.add(listLocation.get(listLocation.size() - 1));
                    } // Lay tat ca cac vi tri tim kiem duoc
                    else {
                        result.addAll(listLocation);
                    }
                }                
            }
        }
        reader.close();
        return result;
    }
            
    /**
     * <b>Them note vao ben duoi moi tu khoa trong danh sach</b><br>
     * 
     * @param pdfFile           duong dan file PDF nguon
     * @param listKey           danh sach tu khoa can tim trong file PDF
     * @param isNotDuplicate    khong lay trung
     * @return 
     */
    public static boolean addSignPlaceHolder(String pdfFile, List<String> listKey,
            boolean isNotDuplicate) {
        
        // Kiem tra dau vao
        if (CommonUtils.isEmpty(pdfFile) || CommonUtils.isEmpty(listKey)) {
            LOGGER.error("addSignPlaceHolder - Loi du lieu dau vao!");
            return false;
        }
        boolean result = false;
        try {
            List<TextLocationInfo> listLocation = findLocations(pdfFile, listKey,
                    isNotDuplicate);
            
            if (!CommonUtils.isEmpty(listLocation)) {
                Date date = new Date();
                String outputFile = pdfFile + date.getTime() + ".pdf";
                // Load file nguon
                PdfReader reader = new PdfReader(pdfFile);
                // Lay tong so trang cua file nguon
                int totalPage = reader.getNumberOfPages();
                
                Rectangle pageSize = reader.getPageSize(1);
                Document document = new Document(pageSize);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
                document.open();
                PdfContentByte contentByte = writer.getDirectContent();
                Calendar calendar = Calendar.getInstance();
                // Toa do dinh note
                Rectangle position, rectangle;
                float x, y;
                PdfAnnotation annotation;
                int noteIndex = 1;
                for (int i = 1; i <= totalPage; i++) {
                    PdfImportedPage importedPage = writer.getImportedPage(reader, i);
                    contentByte.addTemplate(importedPage, 0.0F, 0.0F);
                    // Duyet tung vi tri tri tim kiem duoc
                    for (TextLocationInfo location : listLocation) {
                        // Neu trang hien tai trung voi trang tim kiem duoc tu khoa
                        if (i == location.getPage()) {
                            // Tinh toa do note
                            position = location.getPosition();
                            x = (position.getLeft() + position.getRight()) / 2;
                            y = position.getTop() - 45;
                            rectangle = new Rectangle(x, y, x, y);
                            annotation = PdfAnnotation.createText(writer,
                                rectangle, "Signature", String.valueOf(noteIndex++),
                                true, "Comment");
                            annotation.put(PdfName.TYPE, PdfName.ANNOT);
                            annotation.put(PdfName.M, new PdfDate(calendar));
                            annotation.put(PdfName.CREATIONDATE, new PdfDate(calendar));
                            contentByte.addAnnotation(annotation, true);
                        }
                    }
                    pageSize = reader.getPageSize((i < totalPage) ? (i + 1) : totalPage);
                    document.setPageSize(pageSize);
                    document.newPage();
                }
                
                document.close();
                reader.close();

                // Thay the file cu
                (new File(pdfFile)).delete();
                File oldFile = new File(pdfFile);
                new File(outputFile).renameTo(oldFile);
                result = true;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }
    
    /**
     * <b>Them note vao ben duoi moi tu khoa trong danh sach</b><br>
     * 
     * @param pdfFile   duong dan file PDF nguon
     * @param listKey   danh sach tu khoa can tim trong file PDF
     * @return 
     */
    public static boolean addSignPlaceHolder(String pdfFile, List<String> listKey) {
        
        return addSignPlaceHolder(pdfFile, listKey, false);
    }
}
