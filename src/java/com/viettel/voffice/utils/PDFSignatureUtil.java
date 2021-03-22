/*
 * Copyright 2011 Viettel Telecom. All rights reserved.
 * VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.viettel.voffice.utils;

import java.awt.font.FontRenderContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
//linhdx lib cu
//import com.viettel.DocumentReader.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.viettel.digitalsignature.certificate.X509ExtensionUtil;
import com.viettel.vis.sigstatus.SigAttribute;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.database.dao.SystemParameterDAO;
import com.viettel.voffice.database.entity.EntityImageOrg;
import com.viettel.voffice.database.entity.EntityImageSignature;
import com.viettel.voffice.database.entity.EntityMarkInfo;
import com.viettel.voffice.database.entity.EntitySystemParameter;
import com.viettel.voffice.database.entity.cm.EntitySignature;
import com.viettel.voffice.database.entity.text.EntityTextProcess;

/**
 *
 * @author ChucVQ
 * @date 22-07-2013
 */
@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
public class PDFSignatureUtil {

    private static final Logger LOGGER = Logger.getLogger(PDFSignatureUtil.class);

    //  private HttpServletRequest request;
    // Loai van ban mat
    private static final Long TEXT_CONFIDENTIAL = 2L;
    // TungHD add for config khung ban hanh
    private static final String PROMULGATE_FRAME = "PROMULGATE_FRAME";

    /**
     *
     * @param data
     * @param pathFileSplit
     * @return
     * @throws DocumentException
     * @throws Exception
     */
    public byte[] addSignPageToData(byte[] data, String pathFileSplit,
            String language) throws DocumentException, Exception {
        // -----------------------------------------------
        // Create output PDF
        // lay danh sach chu ki
        ArrayList<SigAttribute> sigArrayList = getListSinger(data);

        // convert sang dang string
        String[][] arrSign = getSignatureInfo2(sigArrayList);

        byte[] newData = data;

        // chen chu ki vao trang dau tien
        if (arrSign != null && arrSign[0][0] != null
                && !"".equals(arrSign[0][0].trim())) {
            newData = addSignPageToFile(data, arrSign, pathFileSplit, language);
        }

        return newData;
    }
    
    /**
     * @author TungHD
     * getListOrgImage
     * get list image org for mark
     * @param data
     * @param numberPage
     * @return
     * @throws DocumentException
     * @throws Exception
     */
    public String[][]  getListOrgImage(byte[] data, int numberPage) throws DocumentException, Exception {
        // -----------------------------------------------
        // Create output PDF
        // lay danh sach chu ki
        ArrayList<SigAttribute> sigArrayList = getListSinger(data);
        
        // convert sang dang string
        String[][] arrSign = getSignatureInfoMark(sigArrayList, numberPage);
        
        return arrSign;
    }

    /**
     * Lay thong tin Image of Signature
     * 
     * @param inputStream
     * @return
     */
    public Map<Integer, List<EntityImageSignature>> getListImageSignature(InputStream inputStream) {
        Map<Integer, List<EntityImageSignature>> result = new HashMap<Integer, List<EntityImageSignature>>();
        try {
            AnnotationDrawer annotationDrawer = new AnnotationDrawer();
            PDDocument document = PDDocument.loadNonSeq(inputStream, null);
            List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();
            int i = 1;
            for (PDPage pdPage : pdPages) {
                List<EntityImageSignature> listImage = annotationDrawer.convertToImages(pdPage, i);
                if (!CommonUtils.isEmpty(listImage)) {
                    result.put(i, listImage);
                }
                i++;
            }
            document.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }
    
//    private static byte[] mergePDFSerectary(byte[] data1,
//            String pathFileSplit) throws IOException {
//        try {
//
//            Document docCombile = new Document();
//            PdfCopy copy = new PdfCopy(docCombile, new FileOutputStream(
//                    pathFileSplit));
//            docCombile.open();
//            PdfReader readInputPDF;
//            int number_of_pages;
//            readInputPDF = new PdfReader(data1);
//            number_of_pages = readInputPDF.getNumberOfPages();
//            int k;
//            for (int page = 0; page < number_of_pages; page++) {
//                k = page + 1;
//                copy.addPage(copy.getImportedPage(readInputPDF, k));
//            }
//
//            docCombile.close();
//            readInputPDF.close();
//
//        } catch (Exception i) {
//            LOGGER.error(i);
//        }
//        File f = new File(pathFileSplit);
//        byte[] byteMerge = FileUtils.readFileToByteArray(f);
//        if (f.exists()) {
//            f.delete();
//        }
//
//        return byteMerge;
//    }
    /**
     * Con vertchu ki ra dang string
     *
     * @param sigArrayList
     * @return
     */
    private String[][] getSignatureInfo2(ArrayList<SigAttribute> sigArrayList) {
        if (sigArrayList.isEmpty()) {

            return null;
        }
//        String result = "";
        String signDate = "";
        String signReason = "";

//        String personal = "";
        String department = "";
        String isImageSign = "";
        // Hiendv chinh sua bo sung thong tin don vi tu usertoken
        String[][] arrSign = new String[sigArrayList.size()][4];
        String[][] arrShownSign = new String[sigArrayList.size()][4];
        int idx = 0;

        try {
            // Du nguyen du lie hien thi theo chu ky cu
            for (int i = 0; i < sigArrayList.size(); i++) {
                SigAttribute sa = sigArrayList.get(i);
                if (sa != null) {
//                    result = sa.getResult() != null ? sa.getResult() : "";
                    signDate = sa.getSignDate() != null ? sa.getSignDate() : "";
                    signReason = sa.getSignReason() != null ? sa
                            .getSignReason() : "";

//                    personal = sa.getSignName();
                    // Mac dinh lay don vi hien thi theo don vi cua chung
                    // thu
                    department = sa.getDeptOfSigner();
                    isImageSign = "";

                    if (sa.getLocation() != null
                            && !"".equals(sa.getLocation().trim())
                            && !"Viet Nam".equals(sa.getLocation().trim())) {
                        /**
                         * Tach thong tin anh chu ky ra khoi don vi neu co
                         */

                        // 201812-Pitagon: add
                        JSONObject obj = null;
                        String image = null;
                        String marked = null;
                        // TungHD add mark default start
                        String imageConfirm = null;
                        String markedConfirm = null;
                        String signLocate = null;
                        String groupType = null;
                        String sigLocation = sa.getLocation();
                        String lstMark = null;
                        String lstConfigImage = null;
                        if (CommonUtils.isJSON(sa.getLocation())) {
                            obj = new JSONObject(sa.getLocation());
                            image = obj.has("image") ? obj.getString("image") : null;
                            imageConfirm = obj.has("imageConfirm") ? obj.getString("imageConfirm") : null;
                            sigLocation = obj.has("location") ? obj.getString("location") : null;
                            marked = obj.has("marked") ? obj.getString("marked") : null;
                            markedConfirm = obj.has("markedConfirm") ? obj.getString("markedConfirm") : null;
                            signLocate = obj.has("signLocate") ? obj.getString("signLocate") : null;
                            groupType = obj.has("groupType") ? obj.getString("groupType") : null;
                            // DatDC lst con dau tuy chon
                            lstMark = obj.has("lstMark") ? obj.getString ("lstMark") : null;
                            lstConfigImage = obj.has("lstConfigImage") ? obj.getString ("lstConfigImage") : null;
                        }

                        String[] arrLocation = null;
                        if (!CommonUtils.isEmpty(sigLocation)) {
                            arrLocation = sigLocation.split("::");
                            if (arrLocation.length > 1) {
                                isImageSign = arrLocation[1];
                            }
                        }

                        obj = new JSONObject();
                        obj.put("image", image);
                        obj.put("marked", marked);
                        obj.put("imageConfirm", imageConfirm);
                        obj.put("markedConfirm", markedConfirm);
                        obj.put("signLocate", signLocate);
                        obj.put("groupType", groupType);
                        // DatDC lst con dau tuy chon
                        obj.put("lstMark", lstMark);
                        //Tunghd add
                        obj.put("lstConfigImage", lstConfigImage);
                        if ((!CommonUtils.isEmpty(marked) || !CommonUtils.isEmpty(markedConfirm)) && CommonUtils.isEmpty(isImageSign)) {
                            isImageSign = "1";
                        }
                        if (arrLocation != null) {
                            obj.put("location", arrLocation[0]);
                        }
                        department = obj.toString();
                    }
                    // TungHD add mark default end
                    if (!"0".equals(isImageSign.trim())
                            && !"".equals(isImageSign.trim())) {
                        if (!CommonUtils.isEmpty(sa.getSubject())) {
                            arrShownSign[idx][0] = sa.getSubject().toUpperCase();
                        } else {
                            arrShownSign[idx][0] = sa.getSubject();
                        }

                        arrShownSign[idx][1] = department;

                        arrShownSign[idx][2] = signDate;

                        // arrShownSign[idx][3] = "há»£p lá»‡";
                        arrShownSign[idx][3] = (signReason.length() != 0 ? signReason
                                : "");

                        idx++;
                    }
                    if (!CommonUtils.isEmpty(sa.getSubject())) {
                        arrSign[i][0] = sa.getSubject().toUpperCase();
                    } else {
                        arrSign[i][0] = sa.getSubject();
                    }
                    arrSign[i][1] = department;

                    arrSign[i][2] = signDate;

                    // arrSign[i][3] = "há»£p lá»‡";
                    arrSign[i][3] = (signReason.length() != 0 ? signReason : "");
                }

            }

            /*
             * Kiem tra ds ky co anh chu ky moc thi hien thá»‹ theo d/s co anh
             * chu ky moc , nguoc lai hien thi tat ca
             */
            if (arrShownSign[0][0] != null
                    && !"".equals(arrShownSign[0][0].trim())) {
                return arrShownSign;
            } else {
                return arrSign;
            }

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return arrSign;
    }

    /**
     * Lay danh sach nguoi ki
     */
    private ArrayList<SigAttribute> getListSinger(byte[] data) throws Exception {
        ArrayList<SigAttribute> sigArrayList = new ArrayList<SigAttribute>();

        BouncyCastleProvider providerBC = new BouncyCastleProvider();
        Security.addProvider(providerBC);

        PdfReader reader = new PdfReader(data);

        AcroFields af = reader.getAcroFields();
        ArrayList names = getSortedName(af);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        for (int k = 0; k < names.size(); ++k) {
            String name = (String) names.get(k);

            // End revision extraction
            PdfPKCS7 pk = af.verifySignature(name);
            Certificate[] chain = pk.getSignCertificateChain();
            String subject = X509ExtensionUtil
                    .getSubject((X509Certificate) chain[0]);

            SigAttribute sig = new SigAttribute(name, subject,
                    Integer.toString(af.getRevision(name)) + " of "
                    + Integer.toString(af.getTotalRevisions()), true,
                    af.signatureCoversWholeDocument(name), "hợp lệ", name,
                    sdf.format(pk.getSignDate().getTime()), pk.getReason(),
                    pk.getLocation());

            sig.setSignDate2(pk.getSignDate().getTime());
            sigArrayList.add(sig);
        }

        Collections.sort(sigArrayList);
        Collections.reverse(sigArrayList);
        try {
            reader.close();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return sigArrayList;
    }

    public ArrayList<String> getSortedName(AcroFields af) {
        ArrayList<String> names = af.getSignatureNames();
        int numberOfName = af.getTotalRevisions();
        ArrayList results = new ArrayList<String>();
        for (int i = numberOfName; i >= 1; --i) {
            for (int j = 0; j < names.size(); ++j) {
                if (af.getRevision(names.get(j)) == i) {
                    results.add(names.get(j));
                }
            }
        }
        return results;
    }
    
   

    private byte[] addSignPageToFile(byte[] is, String[][] arrSign,
            String pathFileSplit, String language) throws DocumentException, IOException {

        byte[] arr = crtePDFmultiPage(arrSign, language);
        // DatDC check truong hop cho phep append trang start
        byte[] arr1 = is;
        // Datdc check
        if (null != arr && arr.length > 0) {
            // co du lieu moi;
            arr1 = mergePDF(arr, is, pathFileSplit);
        }
        // Datdc check
        // DatDC check truong hop cho phep append trang end
        return arr1;
    }

    /*
     * Add trang phu luc ky phu
     */
    public byte[] addSignPageAppendixToFile(byte[] is, List<EntityTextProcess> lstSignOther,
            String pathFileSplit, String language) throws DocumentException, IOException {

        byte[] arrAll = is;
        byte[] arr = null;
        byte[] arrOther = null;
        // Create output PDF
        // lay danh sach chu ki
        ArrayList<SigAttribute> sigArrayList;
        String[][] arrSign = null;
        try {
            sigArrayList = getListSinger(is);
            // convert sang dang string
            arrSign = getSignatureInfo2(sigArrayList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOGGER.error(e.getMessage(), e);
        }

        String[][] arrSignOther = getSignatureOtherInfo(lstSignOther);
        if (arrSignOther != null && arrSignOther[0][0] != null
                && !"".equals(arrSignOther[0][0].trim())) {
            arrOther = crtePDFAppendixPage(arrSignOther, language);

        }
        if (arrSign != null && arrSign[0][0] != null
                && !"".equals(arrSign[0][0].trim())) {
            arr = crtePDFmultiPage(arrSign, language);

        }
        if (arr != null && arr.length > 0 && arrOther != null
                && arrOther.length > 0) {
            // Ghep phu luc chinh va phu voi file goc
            arrOther = mergePDF(arr, arrOther, pathFileSplit);
            arrAll = mergePDF(arrOther, is, pathFileSplit);
        } else if (arr != null && arr.length > 0) {
            // Ghep phu luc ky chinh voi file goc
            arrAll = mergePDF(arr, is, pathFileSplit);
        } else if (arrOther != null && arrOther.length > 0) {
            // ghep phu luc ky phu voi file goc

            arrAll = mergePDF(arrOther, is, pathFileSplit);
        }

        return arrAll;
    }

    private static byte[] mergePDF(byte[] data1, byte[] data2,
            String pathFileSplit) throws IOException {
        try {

            Document docCombile = new Document();
            PdfCopy copy = new PdfCopy(docCombile, new FileOutputStream(
                    pathFileSplit));
            docCombile.open();
            // TungHD edit for mark start
            PdfReader readInputPDF = null;
            int number_of_pages = 0;
            // TungHD edit for mark end
            readInputPDF = new PdfReader(data1);
            number_of_pages = readInputPDF.getNumberOfPages();
            
            //Tunghd add check th add con dau neu co chan ky start
            int i = 0;
            PdfReader originFile = new PdfReader(data2);
            PdfArray annotsArray = null;
            int number_of_pages_originFile = originFile.getNumberOfPages();
            Boolean haveNote = false;
            while (i < number_of_pages_originFile && !haveNote) {
                i++;
                PdfDictionary page = originFile.getPageN(i);
                if (page.getAsArray(PdfName.ANNOTS) != null) {
                    annotsArray = page.getAsArray(PdfName.ANNOTS);
                    for (ListIterator iter = annotsArray.listIterator(); iter
                            .hasNext();) {
                        PdfDictionary annot = (PdfDictionary) PdfReader.getPdfObject((PdfObject) iter.next());
                        PdfString content = (PdfString) PdfReader.getPdfObject(annot.get(PdfName.CONTENTS));
                        if(content != null){
                            haveNote = true;
                            break;
                        }
                    }
                    
                }
            }
            //Tunghd add check th add con dau neu co chan ky end
            int k;
            //Tunghd add check th add trang neu co chan ky start
            if(haveNote){
                for (int page = 0; page < number_of_pages; page++) {
                    k = page + 1;
                    copy.addPage(copy.getImportedPage(readInputPDF, k));
                }
            }
            //Tunghd add check th add trang neu co chan ky end
            readInputPDF = new PdfReader(data2);
            number_of_pages = readInputPDF.getNumberOfPages();
            for (int page = 0; page < number_of_pages; page++) {
                k = page + 1;
                copy.addPage(copy.getImportedPage(readInputPDF, k));
            }
            docCombile.close();
            readInputPDF.close();

        } catch (Exception i) {
            LOGGER.error(i);
        }
        File f = new File(pathFileSplit);
        byte[] byteMerge = FileUtils.readFileToByteArray(f);
        if (f.exists()) {
            f.delete();
        }

        return byteMerge;
    }

    private String getFontFilePath() {
// CommonUtils.getAppConfigValue("folder.upload");
        String fontFile = CommonUtils.getAppConfigValue("path_font_arial");
        return fontFile;
    }

    public byte[] crtePDFmultiPage(String[][] arrSign, String language) {
        Document document = new Document();
        byte[] arr = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            document.setMargins(0.0F, 0.0F, 100.0F, 0.0F);
            PdfWriter.getInstance(document, baos);
            document.open();
            String fontFile = getFontFilePath();

            BaseFont bf = BaseFont.createFont(fontFile, "Identity-H", false);
            Font font = new Font(bf, 12.0F);
            // String[] header = { "STT", "Người ký", "Đơn vị", "Thời gian ký",
            // "Xác thực", "Ý kiến" };
            I18N i18n = new I18N(language);
            String[] header = {
                i18n.getString(I18N.Key.NO),
                i18n.getString(I18N.Key.SIGNER),
                i18n.getString(I18N.Key.DEPARTMENT),
                i18n.getString(I18N.Key.SIGNED_TIME),
                i18n.getString(I18N.Key.COMMENT),};
            // float[] colWidth = { 10.0F, 30.0F, 40.0F, 25.0F, 25.0F, 32.0F };
            float[] colWidth = {10.0F, 30.0F, 40.0F, 25.0F, 57.0F};
            PdfPTable table = new PdfPTable(header.length);
            table.setTotalWidth(colWidth);
            font.setColor(BaseColor.BLUE);

            for (int i = 0; i < header.length; i++) {
                PdfPCell cell = new PdfPCell();
                cell.setPhrase(new Phrase(header[i], font));
                cell.setHorizontalAlignment(1);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            table.completeRow();

            font.setColor(BaseColor.BLACK);
            // TungHD add param check TH add thong tin trang dau
            Boolean hasValue = false;
            for (int i = 0; i < arrSign.length; i++) {
                // TungHD dong dau xac nhan start
                boolean isMarkConfirm = false;
                if (arrSign[i][1] != null && !"".equals(arrSign[i][1].trim())) {
                    String sigLocation = arrSign[i][1];
                    JSONObject obj = new JSONObject(sigLocation);
                    String markedConfirm = obj.has("markedConfirm") ? obj.getString("markedConfirm") : null;
                    if(null!= markedConfirm)
                        isMarkConfirm = true;
                }
                // TungHD dong dau xac nhan end
                // Kiem tra tung phan tu cua danh sach ky khong bi rong thi add
                // vao trang
                if (arrSign[i][0] != null && !"".equals(arrSign[i][0].trim()) && !isMarkConfirm) {
                    // TungHD add true neu la dong dau don vi
                    hasValue = true;
                    PdfPCell cell = new PdfPCell();
                    cell.setPhrase(new Phrase(i + 1 + "", font));
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                    // 201811: Pitagon - signature
                    String sigLocation = arrSign[i][1];
                    for (int j = 0; j < arrSign[i].length; j++) {
                        if (j == 1) {
                            cell = new PdfPCell();
                            if (sigLocation != null && !sigLocation.isEmpty()) {
                                try {
                                    JSONObject obj = new JSONObject(sigLocation);
                                    String location = obj.has("location") ? obj.getString("location") : null;
                                    if (location != null && !location.isEmpty()) {
                                        cell.setPhrase(new Phrase(location, font));
                                    }
                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
                            table.addCell(cell);
                        } else if (j == 3 && sigLocation != null
                                && !sigLocation.isEmpty()) {
                            PdfPTable tableCell = new PdfPTable(1);
                            cell = new PdfPCell();
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setPhrase(new Phrase(arrSign[i][j], font));
                            tableCell.addCell(cell);

                            try {
                                JSONObject obj = new JSONObject(sigLocation);
                                String marked = obj.has("marked") ? obj.getString("marked") : null;
//                                String markedConfirm = obj.has("markedConfirm") ? obj.getString("markedConfirm") : null;
                                if ("1".equals(marked)) {
                                    cell = new PdfPCell();
                                    cell.setBorder(Rectangle.NO_BORDER);
                                    cell.setPhrase(new Phrase(i18n.getString(I18N.Key.SIGN_IMAGE), font));
                                    tableCell.addCell(cell);
                                }
                                if (!obj.has("signLocate")) {
                                    String image = obj.has("image") ? obj.getString("image") : null;
                                    if (image != null && !image.isEmpty()) {
                                        cell = new PdfPCell();
                                        cell.setBorder(Rectangle.NO_BORDER);
                                        cell.addElement(Image.getInstance(image));
                                        cell.setHorizontalAlignment(1);
                                        tableCell.addCell(cell);
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                            table.addCell(tableCell);
                        } else {
                            cell = new PdfPCell();
                            cell.setPhrase(new Phrase(arrSign[i][j], font));
                            table.addCell(cell);
                        }
                    }
                    // End 201811: Pitagon - signature
                    table.completeRow();
                }

            }
            document.add(table);
            document.close();
            // TungHD dong dau don vi thi add trang start
            if (hasValue) {
                arr = baos.toByteArray();
            }
            // TungHD dong dau don vi thi add trang end
            baos.flush();
            baos.close();
        } catch (DocumentException e) {

            LOGGER.error(e.getMessage(), e);
        } catch (IOException ex) {

            LOGGER.error(ex.getMessage(), ex);
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
        }
        return arr;
    }

    /**
     * <b>Tao trang phu luc tat ca thong tin nguoi ky phu</b><br>
     *
     * @param arrSign
     * @param language
     * @return
     */
    public byte[] crtePDFAppendixPage(String[][] arrSign, String language) {

        Document document = new Document();
        byte[] arr = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            document.setMargins(0.0F, 0.0F, 100.0F, 0.0F);
            PdfWriter.getInstance(document, baos);
            document.open();
            String fontFile = getFontFilePath();

            BaseFont bf = BaseFont.createFont(fontFile, "Identity-H", false);
            Font font = new Font(bf, 12.0F);

            // String[] header = { "STT", "Người ký", "Đơn vị", "Thời gian ký",
            // "Xác thực", "Ý kiến" };
            I18N i18n = new I18N(language);
            String[] header = {
                i18n.getString(I18N.Key.NO),
                i18n.getString(I18N.Key.SIGNER),
                i18n.getString(I18N.Key.DEPARTMENT),
                i18n.getString(I18N.Key.PROCESSING_STATUS),
                i18n.getString(I18N.Key.SIGNED_TIME),
                i18n.getString(I18N.Key.COMMENT)
            };
            // float[] colWidth = { 10.0F, 30.0F, 40.0F, 25.0F, 25.0F, 32.0F };
            float[] colWidth = {12.0F, 30.0F, 40.0F, 25.0F, 27.0F, 55.0F};
            PdfPTable table = new PdfPTable(header.length);
            table.setTotalWidth(colWidth);
            font.setColor(BaseColor.BLUE);

            for (int i = 0; i < header.length; i++) {
                PdfPCell cell = new PdfPCell();
                cell.setPhrase(new Phrase(header[i], font));
                cell.setHorizontalAlignment(1);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            table.completeRow();

            font.setColor(BaseColor.BLACK);
            for (int i = 0; i < arrSign.length; i++) {
                // Kiem tra tung phan tu cua danh sach ky khong bi rong thi add
                // vao trang
                if (arrSign[i][0] != null && !"".equals(arrSign[i][0].trim())) {
                    PdfPCell cell = new PdfPCell();
                    cell.setPhrase(new Phrase(i + 1 + "", font));
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                    for (int j = 0; j < arrSign[i].length; j++) {

                        cell = new PdfPCell();
                        cell.setPhrase(new Phrase(arrSign[i][j], font));
                        table.addCell(cell);
                    }

                    table.completeRow();
                }

            }
            document.add(table);
            document.close();
            arr = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (DocumentException e) {

            LOGGER.error(e.getMessage(), e);
        } catch (IOException ex) {

            LOGGER.error(ex.getMessage(), ex);
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
        }
        return arr;
    }

    /**
     * <b>Danh watermark cho 1 mang du lieu</b><br>
     * TungHD, DatDC add param
     * @param tmpByte
     * @param userName
     * @param signLD
     * @param lstSigner
     * @param newFile
     * @param newFileSplit
     * @param isSecretary
     * @param docCode
     * @param docNumber
     * @param docPublishDate 
     * @param sTypeId
     * @param isOriginal
     * @param langCode
     * @param stateText
     * @param lstMarkOrgPath
     * @param listMarkInfo
     * @param strEmail
     * @param isAccessFile
     * @param textCode
     * @throws Exception
     */
    public void writeWaterMarkPdf(byte[] tmpByte, String userName,
            String signLD, List<EntityTextProcess> lstSigner, String newFile,
            String newFileSplit, String isSecretary, String docCode,
            String docNumber, String docPublishDate, Long sTypeId,
            String isOriginal, String langCode, Long stateText, String[][] lstMarkOrgPath, 
            List<EntityMarkInfo> listMarkInfo, String strEmail, boolean isAccessFile,
            String textCode, boolean isArrive) throws Exception {

        
        // System.out.println("Hiendv2------60");
        if (tmpByte != null) {
            // String tmpFile = "temp.pdf";
            // System.out.println("Hiendv2------71");
            String tmpFile = newFile;
            PdfReader pdfReader = new PdfReader(tmpByte);
            
            // Quet lay Image of Signature
            InputStream inputStream = new ByteArrayInputStream(tmpByte);
            Map<Integer, List<EntityImageSignature>> mapImageSignature = getListImageSignature(inputStream);
            inputStream.close();

            // System.out.println("Hiendv2------8");
            if (isOriginal != null && "1".equals(isOriginal.trim())) {
                writeWaterMarkPdfForSerectary(pdfReader, tmpFile, lstSigner, langCode);
            } else {
                // Datdc add EntityMarkInfo entityMark
                // TungHD add param lstMarkOrgPath, strEmail, isAccessFile, textCode
                writeWaterMarkPdf(pdfReader, tmpFile, userName, signLD,
                        lstSigner, isSecretary, docCode, docNumber,
                        docPublishDate, sTypeId, langCode, stateText, lstMarkOrgPath,
                        listMarkInfo, strEmail, isAccessFile, textCode, isArrive,
                        mapImageSignature);
            }

            pdfReader.close();
        }
        // return result;
    }
    /**
     *
     * @param tmpByte du lieu can watermark
     * @param orgFile file can watermark
     * @param copyFile file ket qua sau khi watermark
     * @param loginName ten dung de danh watermark
     * @throws DocumentException
     */
    private final Long TEXT_ACTION_STATE_LD_SIGNER = 4L; // Lanh dao da ky

    /**
     * @author TungHD modify
     * @param pdfReader
     * @param copyFile
     * @param userName
     * @param signLD
     * @param lstSigner
     * @param isSecretary
     * @param docCode
     * @param docNumber
     * @param docPublishDate
     * @param sTypeId
     * @param langCode
     * @param stateText
     * @param lstMarkOrgPath
     * @param listMarkInfo
     * @param strEmail
     * @param isAccessFile
     * @param textCode
     * @return
     * @throws DocumentException
     * @throws JSONException 
     */
    private int writeWaterMarkPdf(PdfReader pdfReader, String copyFile,
            String userName, String signLD, List<EntityTextProcess> lstSigner,
            String isSecretary, String docCode, String docNumber,
            String docPublishDate, Long sTypeId, String langCode, Long stateText, 
            String[][]  lstMarkOrgPath, List<EntityMarkInfo> listMarkInfo, 
            String strEmail, boolean isAccessFile, String textCode, boolean isArrive,
            Map<Integer, List<EntityImageSignature>> mapImageSignature)
            throws DocumentException {

        int number_of_pages = 0;
        // com.itextpdf.text.Document document = null;
        FileOutputStream outputStream = null;
        try {
            
            SystemParameterDAO systemDAO = new SystemParameterDAO();
            String value = systemDAO.getValue("POSITION_MARK");
            int topMark = 40;
            int leftMark = 130;
            int leftTextMark = 20;
            int scaleImage = 135;
            int scaleMark = 135;
            int witdTextMark = 40;
            try {
                if (!CommonUtils.isEmpty(value) && value.contains("{")) {
                    JSONObject jsonObj = new JSONObject(value);
                    topMark = jsonObj.getInt("top");
                    leftMark = jsonObj.getInt("left");
                    leftTextMark = jsonObj.getInt("leftText");
                    scaleImage = jsonObj.getInt("scaleImage");
                    scaleMark = jsonObj.getInt("scaleMark");
                    witdTextMark = jsonObj.getInt("witdTextMark");
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            // TungHD Lay thong tin vi tri khung ban hanh tu SYSTEM_PARAMETER
            //mac dinh ben phai
            Long locationPromulgateFrame = 2L;
            locationPromulgateFrame = getValueFromSysParam();
            // bo qua check password
            PdfReader.unethicalreading = true;

            // lay thong tin ngay hien tai
            Date curentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.after(curentDate);
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "HH:mm dd/MM/yyyy");
            String timeNow = dateFormat.format(calendar.getTime());
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i <= 20; i++) {
                buff.append(userName).append("_").append(timeNow);
                buff.append("          ");
            }
            userName = buff.toString();

            // Hiendv bo sung warttermark giong phia client
            // Hiendv bo sung dieu kien check null
            if (signLD != null && !"".equals(signLD.trim())
                    && !"null".equals(signLD.trim())) {
                buff = new StringBuffer();
                for (int i = 0; i <= 20; i++) {
                    buff.append(signLD);
                    buff.append("          ");
                }
                signLD = buff.toString();
            }
            // Hiendv bo sung dieu kien check null
            number_of_pages = pdfReader.getNumberOfPages();

            outputStream = new FileOutputStream(copyFile);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, outputStream);
            // Get the PdfContentByte type by pdfStamper.
            PdfContentByte underContent;
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
                    BaseFont.WINANSI, BaseFont.EMBEDDED);

            PdfGState gs = new PdfGState();
            PdfGState gsFooter = new PdfGState();
            gs.setFillOpacity(0.2f);
            gsFooter.setFillOpacity(1f);
            Image image1 = null;
            // TungHD add anh dau don vi
            Image imageOrg = null;
            // TungHD add anh dau xac nhan
            Image imageConfirm = null;
            Image imageConfidential = null;
            String pathImage = null;
            boolean exitsImageSign = false;
            String sContextPath = CommonUtils.getAppConfigValue("folder_upload")
                    + File.separator
                    + CommonUtils.getAppConfigValue("path_to_upload");

            String pathUploadImageSinger = CommonUtils.getAppConfigValue("pathUploadImageSinger");
            // Them khung ban hanh
            String fontFile = getFontFilePath();
            // String fontFooter=getFontFooterPath();
            BaseFont bfbh = BaseFont.createFont(fontFile, "Identity-H", true);

            BaseFont baseFont = BaseFont.createFont(fontFile,
                    BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            PdfContentByte contentbh = null;
            Rectangle r1 = null;
            float height = 0.1F;
            // TungHD mod cho KhungBan hanh
            float recLength = 145.0F;
            float width = 0.1F;
            I18N i18n = new I18N(langCode);
            //Tunghd add ma so van ban cho khung ban hanh
            String tmpDocCode = "";
            //add fix cung mui gio theo gio vietnam
            String timeZone = " +07:00";
            //End
            // Tung HD sua cho khung ban hanh start
            if (docCode != null) {
                tmpDocCode = docCode;
                docCode = i18n.getString(I18N.Key.DOCUMENT_CODE) + ": " + docCode;
                if(docNumber != null){
                    docNumber = i18n.getString(I18N.Key.TIME_SIGN) + ": " + docNumber + timeZone;
                }   
                if (docPublishDate != null && !"".equals(docPublishDate)) {
                    String[] arrPd = docPublishDate.split(" ");
                    docPublishDate = arrPd[0];
                    if(isArrive){
                        docPublishDate = i18n.getString(I18N.Key.ARRIVE_DATE) + ": " + docPublishDate;
                    } else {
                        docPublishDate = i18n.getString(I18N.Key.PUBLISHED_DATE) + ": " + docPublishDate;
                    }
                    
                }
                //Neu Thoi gian Ky dai hon So va Ky Hieu
                if(docNumber != null && docNumber.length() > docCode.length()){
                    recLength += docNumber.length() - 7;
                }
                //Neu So, Ky hieu dai hon Thoi gian ky
                if(docNumber != null && docCode.length() > docNumber.length()){
                    recLength = docCode.length()* 4.9F;
                }
                //Neu chua dong dau
                if(docNumber == null){
                    if(!CommonUtils.isEmpty(docPublishDate) && docPublishDate.length() > docCode.length()){
                        recLength = docPublishDate.length()* 4.9F;
                    } else {
                         recLength = docCode.length()* 4.9F;
                    }
                }
            }
            String pageFooter = i18n.getString(I18N.Key.OWNERSHIP);
            // End them khung ban hanh
            // Tung HD sua cho khung ban hanh end
            // Tung HD lay tham so dong dau end
            List<EntityImageOrg> lstOrgMark = new ArrayList<EntityImageOrg>();
            //Tunghd add lay ra 1 list path anh dau
            String  imageOrgPath = ""; 
            String  imageConfirmPath = ""; 
            String  imageOrgSignLocate = ""; 
            String  markerEmailOrg = ""; 
            String  markerEmailConfirm = ""; 
            String  groupType = null;
            // LstMark dong dau tuy chon sau khi dong dau thanh cong qua usb
            List<EntityMarkInfo> lstMark = new ArrayList<>();
            List<EntityMarkInfo> lstMarkConfig = new ArrayList<>();
            String strMark = null;
            String strMarkConfig = null;
            int countPage = 0;
            if (lstMarkOrgPath != null) {
                for( int i = 0; i < lstMarkOrgPath.length; i++){
                    if (lstMarkOrgPath[i][1] != null
                            && !"".equals(lstMarkOrgPath[i][1].trim())) {
                        try {
                            EntityImageOrg io = new EntityImageOrg();
                            String sigLocation = lstMarkOrgPath[i][1];
                            JSONObject obj = new JSONObject(sigLocation);
                            imageOrgPath = obj.has("image") ? obj.getString("image") : null;
                            imageConfirmPath = obj.has("imageConfirm") ? obj.getString("imageConfirm") : null;
                            imageOrgSignLocate = obj.has("signLocate") ? obj.getString("signLocate") : null;
                            groupType = obj.has("groupType") ? obj.getString("groupType") : null;
                            // Lst dong dau
                            strMark = obj.has("lstMark") ? obj.getString("lstMark") : null;
                            strMarkConfig = obj.has("lstConfigImage") ? obj.getString("lstConfigImage") : null;
                            countPage = obj.has("countPage")? Integer.valueOf(obj.getString("countPage")) : 0;
                            //Email
                            markerEmailOrg = obj.has("markerEmailOrg") ? obj.getString("markerEmailOrg") : null;
                            markerEmailConfirm = obj.has("markerEmailConfirm") ? obj.getString("markerEmailConfirm") : null;
                            if(imageOrgPath != null){
                                imageOrgPath = imageOrgPath.replace("\\", File.separator);
                                imageOrgPath = imageOrgPath.replace("/", File.separator);
                                io.setPath(imageOrgPath);
                            }
                            if(imageConfirmPath != null){
                                imageConfirmPath = imageConfirmPath.replace("\\", File.separator);
                                imageConfirmPath = imageConfirmPath.replace("/", File.separator);
                                io.setPath(imageConfirmPath);
                            }
                            if(imageOrgSignLocate != null){
                                Long signLc = Long.valueOf(imageOrgSignLocate);
                                io.setSignLocate(signLc.toString());
                            }
                            //Email
                            if(markerEmailOrg != null){
                                io.setEmailMarked(markerEmailOrg);
                            }
                            if(markerEmailConfirm != null){
                                io.setEmailMarked(markerEmailConfirm);
                            }
                            if(groupType != null){
                                io.setGroupType(Long.valueOf(groupType));
                                lstOrgMark.add(io);
                            }
                            if(strMark != null){
                                Gson gson = new Gson();
                                Type listType = new TypeToken<ArrayList<EntityMarkInfo>>() {}.getType();
                                List<EntityMarkInfo> lstTmp = gson.fromJson(strMark, listType);
                                for (EntityMarkInfo entityM : lstTmp) {
                                    entityM.setNameUsb(lstMarkOrgPath[i][0].toString());
                                }
                                lstMark.addAll(lstTmp);
                            }
                            //Tunghd
                            if(strMarkConfig != null){
                                Gson gson = new Gson();
                                Type listType = new TypeToken<EntityMarkInfo>() {}.getType();
                                EntityMarkInfo lstTmpConfig = gson.fromJson(strMarkConfig, listType);
                                lstTmpConfig.setSignBy(lstMarkOrgPath[i][0].toString());
                                lstTmpConfig.setTimeMark(lstMarkOrgPath[i][2].toString());
                                lstMarkConfig.add(lstTmpConfig);
                            }
                            
                            
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
            // Tung HD lay tham so dong dau end

            // End them khung ban hanh
            int i = 0;
            // Datdc start dong dau tuy chon start
            Boolean isHassAccessFistPage = false;
            // Check 
            // Dau chinh lay tu usb
            if (!CommonUtils.isEmpty(lstMark)) {
                if (!CommonUtils.isEmpty(listMarkInfo)) {
                    lstMark.addAll(listMarkInfo);
                } 
                listMarkInfo = lstMark;
            }
            // Dau tam lay tu web
            int pageInfo = 1;
            if (!CommonUtils.isEmpty(listMarkInfo)) {
                if (0 != listMarkInfo.get(0).getCountPage() ) {
                    if(listMarkInfo.get(0).getCountPage() < number_of_pages) {
                        isHassAccessFistPage = true;
                        pageInfo = number_of_pages - listMarkInfo.get(0).getCountPage();
                    }
                }
            }
            // Check so trang dau cho dong dau mac dinh
            int infoPages = 0;
            if(countPage > 0) {
                if (countPage < number_of_pages) {
                    infoPages = number_of_pages - countPage;
                }
            }
            // Datdc  dong dau tuy chon end
            while (i < number_of_pages) {

                i++;
                r1 = pdfReader.getPageSizeWithRotation(i);
                height = r1.getHeight();
                width = r1.getWidth();
                PdfContentByte pdfPage = null;
                if (pdfStamper.getOverContent(i) != null) {
                    pdfPage = pdfStamper.getOverContent(i);
                }
                // [tuantm30 - start] Sua phan add anh chu ky phai ngang
                boolean isVertical = false;
                int rotate = pdfReader.getPageRotation(i);
                if ( rotate == 90 || rotate == 270) {
                    isVertical = true;
                }
                // [tuantm30 - end] Sua phan add anh chu ky phai ngang
                // Neu van ban la van ban mat thi dong dau mat sau duoi khung
                // ban hanh
                if (sTypeId != null && sTypeId.equals(TEXT_CONFIDENTIAL)) {
                    pathImage = sContextPath + File.separator + pathUploadImageSinger
                            + File.separator + "CONFIDENTIAL_IMAGE_VN.png";
                    pathImage = pathImage.replace("\\", File.separator);
                    pathImage = pathImage.replace("/", File.separator);
                    imageConfidential = Image.getInstance(pathImage);
                    imageConfidential.setAbsolutePosition(20.0F, height - 90.0F);
                    if (pdfPage != null) {
                        pdfPage.addImage(imageConfidential);
                    }
                }

                // add image of signature vao van ban khi doc
                if (mapImageSignature != null && mapImageSignature.size() > 0) {
                    List<EntityImageSignature> listImageSignature = mapImageSignature.get(i);
                    if (!CommonUtils.isEmpty(listImageSignature)) {
                        for (EntityImageSignature imageSignature : listImageSignature) {
                            if (imageSignature.getImage() != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(imageSignature.getImage(), "png", baos);
                                baos.flush();
                                Image imageInsert = Image.getInstance(baos.toByteArray());
                                baos.close();
                                imageInsert.setAbsolutePosition(imageSignature.getPosition().getLowerLeftX(), imageSignature.getPosition().getLowerLeftY());
                                imageInsert.scalePercent(AnnotationDrawer.SCALE_PERCENT);
                                pdfPage.addImage(imageInsert);
                            }
                        }
                    }
                }

                //2015-08-10 chucvq chen dau dung thu
                // neu chua trinh hoac la van thu: isVanthu = true hoac state=0
                Image image2 = null;
                if (((isSecretary != null && "1".equals(isSecretary
                        .trim())) && stateText != null
                        && (!stateText.equals(3L) && !stateText.equals(4L)))
                        || (stateText != null && stateText.equals(0L))) {
                    // chucvq chen dong dau
                    image2 = Image.getInstance(sContextPath + File.separator + pathUploadImageSinger
                            + File.separator + "STAMP1.1.png");
                    if (langCode == null || langCode.contains("vi")) {
                        image2 = Image.getInstance(sContextPath + File.separator + pathUploadImageSinger
                                + File.separator + "STAMP1.1.png");
                    } else {
                        image2 = Image.getInstance(sContextPath + File.separator + pathUploadImageSinger
                                + File.separator + "STAMP1.1_EN.png");
                    }

                    //image2.setAbsolutePosition(left - 60, top - 70);
                    image2.setRotation((float) Math.PI / 4);
                    //310517 kiem tra van ban bo tri ngang
                    if (isVertical) {
                        //neu la xoay ngang
                        image2.setAbsolutePosition(PageSize.A4.getHeight() / 3, PageSize.A4.getWidth() / 4);
                    } else {
                        //la xoay doc
                        image2.setAbsolutePosition(PageSize.A4.getWidth() / 4, PageSize.A4.getHeight() / 3);
                    }
                    image2.scaleToFit(PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2);
                    if (pdfPage != null) {
                        pdfPage.addImage(image2);
                    }
                }

                // Hiendv bo sung dieu kien check null
                // Set thong bao van ban khong dc download
                underContent = pdfStamper.getOverContent(i);
                underContent.saveState();
                underContent.setLineWidth(0.2f);
                underContent.moveTo(20, 15);
                underContent.lineTo(width - 20, 15);
                underContent.stroke();
                underContent.restoreState();
                underContent.setGState(gsFooter);
                underContent.beginText();
                underContent.setFontAndSize(baseFont, 8);
                underContent.showTextAligned(Element.ALIGN_CENTER,
                        pageFooter, width / 2, 4, 0);

                underContent.endText();

                // underContent.
                PdfDictionary page = pdfReader.getPageN(i);
                PdfArray annotsArray = null;
                // Duong dan anh chu ky mac dinh
                if (langCode == null || !"vi".equals(langCode)) {
                    langCode = "en";
                }
                String defaultImagePath = sContextPath + File.separator
                        + pathUploadImageSinger + File.separator + langCode + "_default_sign.png";
                defaultImagePath = defaultImagePath.replace("\\", File.separator);
                defaultImagePath = defaultImagePath.replace("/", File.separator);
                // Kiem tra tung trang mot xem co comment
                if (page.getAsArray(PdfName.ANNOTS) != null) {
                    annotsArray = page.getAsArray(PdfName.ANNOTS);
                    for (ListIterator iter = annotsArray.listIterator(); iter
                            .hasNext();) {
                        PdfDictionary annot = (PdfDictionary) PdfReader
                                .getPdfObject((PdfObject) iter.next());
                        PdfString content = (PdfString) PdfReader
                                .getPdfObject(annot.get(PdfName.CONTENTS));
                        PdfArray rectArr = (PdfArray) annot.get(PdfName.RECT);

                        if (content != null) {

                            String strContent = content.toUnicodeString()
                                    .replaceAll(" ", "").replaceAll("\r\n", "")
                                    .trim();

                            int size = rectArr.size();
                            float left = 0f;
                            float top = 0f;
                            // Xac dinh toa do cua comment
                            if (size > 2) {
                                //310517 kiem tra van ban bo tri ngang
                                if (isVertical) {
                                    // [tuantm30 - start] Sua phan add anh chu ky phai ngang
                                    if (rotate == 270) {
                                        PdfNumber obj = (PdfNumber) rectArr.getPdfObject(2);
                                        Rectangle rec1 = pdfReader.getPageSizeWithRotation(i);
                                        if (obj != null) {
                                            top = obj.floatValue();
                                        }
                                        PdfNumber obj1 = (PdfNumber) rectArr.getPdfObject(3);
                                        if (obj1 != null) {
                                            Float widthRec = rec1.getLeft() == 0.0 ? rec1.getRight() : rec1.getLeft();
                                            left = widthRec - obj1.floatValue();
                                        }
                                    }
                                    if (rotate == 90) {
                                        PdfNumber obj = (PdfNumber) rectArr.getPdfObject(2);
                                        Rectangle rec1 = pdfReader.getPageSizeWithRotation(i);
                                        if (obj != null) {
                                            top = rec1.getTop() - obj.floatValue();
                                        }
                                        PdfNumber obj1 = (PdfNumber) rectArr.getPdfObject(3);
                                        if (obj1 != null) {
                                            left = obj1.floatValue();
                                        }
                                    }
                                    // [tuantm30 - end] Sua phan add anh chu ky phai ngang
                                } else {
                                    PdfNumber obj = (PdfNumber) rectArr
                                            .getPdfObject(0);
                                    if (obj != null) {
                                        left = obj.floatValue();
                                    }
                                    PdfNumber obj1 = (PdfNumber) rectArr
                                            .getPdfObject(1);
                                    if (obj1 != null) {
                                        top = obj1.floatValue();
                                    }
                                }
                            }
                            // Add image
                            if (left > 0 && top > 0) {
                                // Tung HD sua cho dong dau don vi start
                                if (lstSigner != null && lstSigner.size() > 0) {
                                    // Kiem tra van ban chua trinh ky va van thu
                                    // doc van ban thi hien thi xem chu ky truoc
                                    // duoc
                                    if (lstSigner.get(0).getState() != null
                                            && lstSigner.get(0).getState()
                                            .equals(0L)
                                            || (isSecretary != null && "1".equals(isSecretary
                                                    .trim()))) {
                                        // Neu xem van ban chua trinh ky

                                        exitsImageSign = false;
                                            for (int j = 0; j < lstSigner.size(); j++) {
                                                pathImage = null;
                                                if (lstSigner.get(j).getSignImageIndex() != null
                                                        && lstSigner.get(j).getNameImageSign() != null
                                                        && lstSigner.get(j).getSignImageIndex().toString().equals(strContent)) {
                                                    pathImage = sContextPath + File.separator + pathUploadImageSinger + File.separator + lstSigner.get(j).getNameImageSign()
                                                            .trim();
                                                    if (lstSigner.get(j).getStorage() != null && lstSigner.get(j).getPathImageSign() != null) {
                                                        pathImage = CommonUtils.getAppConfigValue(lstSigner.get(j).getStorage()) + File.separator + lstSigner.get(j).getPathImageSign()
                                                                .trim();
                                                    }
                                                    pathImage = pathImage.replace(
                                                            "\\", File.separator);
                                                    pathImage = pathImage.replace(
                                                            "/", File.separator);
                                                    try {
                                                        image1 = Image.getInstance(pathImage);
                                                        image1.setAbsolutePosition(left - 60, top - 70);
                                                        image1.scalePercent(scaleImage * 100 / image1.getHeight());
                                                        pdfPage.addImage(image1);
                                                        
                                                        if (!CommonUtils.isEmpty(lstOrgMark)) {
                                                            for (EntityImageOrg lst : lstOrgMark) {
                                                                if (lst.getSignLocate() != null && lst.getSignLocate().equals(strContent)) {
                                                                    EntityMarkInfo info = new EntityMarkInfo();
                                                                    if (!CommonUtils.isEmpty(lstMarkConfig)) {
                                                                        for (EntityMarkInfo emi : lstMarkConfig) {
                                                                            if (emi.getSignLocate() != null) {
                                                                                Long signInfo = emi.getSignLocate();
                                                                                if (lst.getSignLocate().equals(signInfo.toString())) {
                                                                                    info = emi;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    if (info.getNumImageInclude() != null && info.getNumImageInclude().equals(3L)) {
                                                                        imageOrg = Image.getInstance(lst.getPath());
                                                                        imageOrg.setAbsolutePosition(left - leftMark, top - topMark);
                                                                        imageOrg.scalePercent(scaleMark * 100 / imageOrg.getHeight());
//                                                                        pdfPage.addImage(imageOrg);
                                                                        List<String> lstCheck = new ArrayList<>();
                                                                        List<String> lstResult = new ArrayList<>();
                                                                        lstCheck = getLstCheck(info, tmpDocCode, textCode, lst.getEmailMarked(), i18n, 1L);
                                                                        // defaule lineheigt 1.5f
                                                                        Float imageHeight = imageOrg.getScaledHeight();
                                                                        Float imageWidth = imageOrg.getScaledWidth();
                                                                        Float topImg = top - topMark;
                                                                        Float imageHalf = Math.abs(imageHeight / 2);
                                                                        Float leftText = Float.valueOf(left - leftMark) + imageWidth + leftTextMark;
                                                                        Float topHeight = 1.5F;
                                                                        Float fontSize = Float.valueOf(info.getNumFontSize());
                                                                        Float widthDiv = Float.valueOf(info.getNumWidth());
                                                                        Float widthTextShow = widthDiv - imageWidth - witdTextMark;
                                                                        // edit end
                                                                        Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                                                        lstResult = countTextShow(lstCheck, countNumber, i18n);
                                                                        if (!CommonUtils.isEmpty(lstResult)) {
                                                                            writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh, topHeight,
                                                                                    leftText, topImg, imageHalf);
                                                                        }
                                                                    } else {
                                                                        if (CommonUtils.isEmpty(lstMarkConfig)) {
                                                                            imageOrg = Image.getInstance(lst.getPath());
                                                                            imageOrg.setAbsolutePosition(left - leftMark, top - topMark);
                                                                            imageOrg.scalePercent(scaleMark * 100 / imageOrg.getHeight());
                                                                            pdfPage.addImage(imageOrg);
                                                                        } else {
                                                                            imageOrg = Image.getInstance(lst.getPath());
                                                                            imageOrg.setAbsolutePosition(left - leftMark, top - topMark);
                                                                            imageOrg.scalePercent(scaleMark * 100 / imageOrg.getHeight());
                                                                            pdfPage.addImage(imageOrg);
                                                                            List<String> lstCheck = new ArrayList<>();
                                                                            List<String> lstResult = new ArrayList<>();
                                                                            lstCheck = getLstCheck(info, tmpDocCode, textCode, lst.getEmailMarked(), i18n, 1L);
                                                                            // defaule lineheigt 1.5f
                                                                            Float imageHeight = imageOrg.getScaledHeight();
                                                                            Float imageWidth = imageOrg.getScaledWidth();
                                                                            Float topImg = top - topMark;
                                                                            Float imageHalf = Math.abs(imageHeight / 2);
                                                                            Float leftText = Float.valueOf(left - leftMark) + imageWidth + leftTextMark;
                                                                            Float topHeight = 1.5F;
                                                                            Float fontSize = Float.valueOf(info.getNumFontSize());
                                                                            Float widthDiv = Float.valueOf(info.getNumWidth());
                                                                            Float widthTextShow = widthDiv - imageWidth - witdTextMark;
                                                                            // edit end
                                                                            Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                                                            lstResult = countTextShow(lstCheck, countNumber, i18n);
                                                                            if (!CommonUtils.isEmpty(lstResult)) {
                                                                                writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh,
                                                                                        topHeight, leftText, topImg, imageHalf);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception ex1) {
                                                        LOGGER.error(ex1);
                                                    }
                                                    exitsImageSign = true;
                                                }
                                            }
//                                        if (exitsImageSign == false) {
//                                            try {
//                                                image1 = Image.getInstance(defaultImagePath);
//                                                image1.setAbsolutePosition(left - 60, top - 70);
//                                                image1.scalePercent(scaleImage * 100 / image1.getHeight());
//                                                pdfPage.addImage(image1);
//                                            } catch (Exception ex1) {
//                                                LOGGER.error(ex1);
//                                            }
//                                        }
                                    } else if (lstSigner.get(0).getState() != null
                                            && !lstSigner.get(0).getState()
                                            .equals(0L)) {
                                        for(int j = 0;j<lstSigner.size();j++) {
                                            pathImage = null;
                                            if (lstSigner.get(j).getStatusSign() != null && lstSigner.get(j).getStatusSign().equals(TEXT_ACTION_STATE_LD_SIGNER)
                                                    && lstSigner.get(j).getSignImageIndex() != null && lstSigner.get(j).getNameImageSign() != null
                                                    && lstSigner.get(j).getSignImageIndex().toString().equals(strContent)) {
                                                pathImage = sContextPath + File.separator + pathUploadImageSinger + File.separator
                                                        + lstSigner.get(j).getNameImageSign().trim();
                                                if (lstSigner.get(j).getStorage() != null && lstSigner.get(j).getPathImageSign() != null) {
                                                    pathImage = CommonUtils.getAppConfigValue(lstSigner.get(j).getStorage()) + File.separator
                                                            + lstSigner.get(j).getPathImageSign().trim();
                                                }
                                                pathImage = pathImage.replace("\\", File.separator);
                                                pathImage = pathImage.replace("/", File.separator);
                                                try {
                                                    image1 = Image.getInstance(pathImage);

                                                    image1.setAbsolutePosition(left - 60, top - 70);
                                                    image1.scalePercent(scaleImage * 100 / image1.getHeight());
                                                    pdfPage.addImage(image1);
                                                    if (!CommonUtils.isEmpty(lstOrgMark)) {
                                                        for (EntityImageOrg lst : lstOrgMark) {
                                                            if (lst.getSignLocate() != null && lst.getSignLocate().equals(strContent)) {
                                                                EntityMarkInfo info = new EntityMarkInfo();
                                                                if (!CommonUtils.isEmpty(lstMarkConfig)) {
                                                                    for (EntityMarkInfo emi : lstMarkConfig) {
                                                                        if (emi.getSignLocate() != null) {
                                                                            Long signInfo = emi.getSignLocate();
                                                                            if (lst.getSignLocate().equals(signInfo.toString())) {
                                                                                info = emi;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                if (info.getNumImageInclude() != null && info.getNumImageInclude().equals(3L)) {
                                                                    imageOrg = Image.getInstance(lst.getPath());
                                                                    imageOrg.setAbsolutePosition(left - leftMark, top - topMark);
                                                                    imageOrg.scalePercent(scaleMark * 100 / imageOrg.getHeight());
//                                                                    pdfPage.addImage(imageOrg);
                                                                    List<String> lstCheck = new ArrayList<>();
                                                                    List<String> lstResult = new ArrayList<>();
                                                                    lstCheck = getLstCheck(info, tmpDocCode, textCode, lst.getEmailMarked(), i18n, 1L);
                                                                    // defaule lineheigt 1.5f
                                                                    Float imageHeight = imageOrg.getScaledHeight();
                                                                    Float imageWidth = imageOrg.getScaledWidth();
                                                                    Float topImg = top - topMark;
                                                                    Float imageHalf = Math.abs(imageHeight / 2);
                                                                    Float leftText = Float.valueOf(left - leftMark) + imageWidth + leftTextMark;
                                                                    Float topHeight = 1.5F;
                                                                    Float fontSize = Float.valueOf(info.getNumFontSize());
                                                                    Float widthDiv = Float.valueOf(info.getNumWidth());
                                                                    Float widthTextShow = widthDiv - imageWidth - witdTextMark;
                                                                    // edit end
                                                                    Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                                                    lstResult = countTextShow(lstCheck, countNumber, i18n);
                                                                    if (!CommonUtils.isEmpty(lstResult)) {
                                                                        writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh, topHeight,
                                                                                leftText, topImg, imageHalf);
                                                                    }
                                                                } else {
                                                                    if (CommonUtils.isEmpty(lstMarkConfig)) {
                                                                        imageOrg = Image.getInstance(lst.getPath());
                                                                        imageOrg.setAbsolutePosition(left - leftMark, top - topMark);
                                                                        imageOrg.scalePercent(scaleMark * 100 / imageOrg.getHeight());
                                                                        pdfPage.addImage(imageOrg);
                                                                    } else {
                                                                        imageOrg = Image.getInstance(lst.getPath());
                                                                        imageOrg.setAbsolutePosition(left - leftMark, top - topMark);
                                                                        imageOrg.scalePercent(scaleMark * 100 / imageOrg.getHeight());
                                                                        pdfPage.addImage(imageOrg);
                                                                        List<String> lstCheck = new ArrayList<>();
                                                                        List<String> lstResult = new ArrayList<>();
                                                                        lstCheck = getLstCheck(info, tmpDocCode, textCode, lst.getEmailMarked(), i18n, 1L);
                                                                        // defaule lineheigt 1.5f
                                                                        Float imageHeight = imageOrg.getScaledHeight();
                                                                        Float imageWidth = imageOrg.getScaledWidth();
                                                                        Float topImg = top - topMark;
                                                                        Float imageHalf = Math.abs(imageHeight / 2);
                                                                        Float leftText = Float.valueOf(left - leftMark) + imageWidth + leftTextMark;
                                                                        Float topHeight = 1.5F;
                                                                        Float fontSize = Float.valueOf(info.getNumFontSize());
                                                                        Float widthDiv = Float.valueOf(info.getNumWidth());
                                                                        Float widthTextShow = widthDiv - imageWidth - witdTextMark;
                                                                        // edit end
                                                                        Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                                                        lstResult = countTextShow(lstCheck, countNumber, i18n);
                                                                        if (!CommonUtils.isEmpty(lstResult)) {
                                                                            writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh,
                                                                                    topHeight, leftText, topImg, imageHalf);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (Exception ex1) {
                                                    LOGGER.error(ex1);
                                                    image1 = Image.getInstance(defaultImagePath);
                                                    image1.setAbsolutePosition(left - 60, top - 70);
                                                    image1.scalePercent(scaleImage * 100 / image1.getHeight());
                                                    pdfPage.addImage(image1);
                                                }
                                            }
                                        }
                                    }
                                } else {
//                                    if (exitsImageSign == false) {
//                                        try {
//                                            image1 = Image.getInstance(defaultImagePath);
//                                            image1.setAbsolutePosition(left - 60, top - 70);
//                                            image1.scalePercent(scaleImage * 100 / image1.getHeight());
//                                            pdfPage.addImage(image1);
//                                        } catch (Exception ex1) {
//                                            LOGGER.error(ex1);
//                                        }
//                                    }
                                }
                               // Tung HD sua cho dong dau don vi end
                            }
                            annot.clear();
                        }
                    }
                }
                // TungHD Them con dau xac nhan file ky chinh start
                if(lstOrgMark != null && ((!isAccessFile && i == infoPages + 1)
                         || (isAccessFile && i == 1))){
                    for(EntityImageOrg lst : lstOrgMark){
                        if(lst.getGroupType().equals(2L)){
                            EntityMarkInfo info = new EntityMarkInfo();
                            if(!CommonUtils.isEmpty(lstMarkConfig)){
                                for(EntityMarkInfo emi : lstMarkConfig){
                                    if(emi.getSignLocate() == null){
                                        info = emi;
                                    }
                                }
                            }
                            if(!lstMarkConfig.isEmpty() && info.getNumImageInclude() != null  
                                    && info.getNumImageInclude().equals(3L)){
                                
                                if (!CommonUtils.isEmpty(lst.getPath())) {
                                    // So sanh kich thuoc width trang voi khung
                                    imageConfirm = Image.getInstance(lst.getPath());
                                    Float widthDiv = Float.valueOf (info.getNumWidth());
                                    Float widthHienthi = widthDiv + 300.0F;
                                    Float leftImgOriginal = 300.0F;
                                    Float divMinus = 0F;
                                    if(widthHienthi > width) {
                                        divMinus += Math.abs(widthHienthi - width);
                                        // Tru di le phai.
                                        // De text hien thi nhieu hon
                                        // Do gioi han le trai
                                        if (divMinus > 20.0F) {
                                            divMinus -= 20.0F;
                                        }
                                    }
                                    imageConfirm.scalePercent(scaleMark * 100 / imageConfirm.getHeight());
                                    float imageHeight = imageConfirm.getScaledHeight();
                                    float imageWidth = imageConfirm.getScaledWidth();
                                    imageConfirm.setAbsolutePosition(leftImgOriginal, height - imageHeight);
//                                    pdfPage.addImage(imageConfirm);
                                    List<String> lstCheck = new ArrayList<>();
                                    List<String> lstResult = new ArrayList<>();
                                    lstCheck = getLstCheck(info, tmpDocCode, textCode, lst.getEmailMarked(), i18n, null);
                                    // defaule lineheigt 1.5f
                                    Float topImg =  height - imageHeight;
                                    Float imageHalf = Math.abs(imageHeight/2);
                                    Float leftText = Float.valueOf(leftImgOriginal) + imageWidth + leftTextMark;
                                    Float topHeight = 1.5F;
                                    Float fontSize = Float.valueOf(info.getNumFontSize());
                                    Float widthTextShow = widthDiv - imageWidth - witdTextMark - divMinus;
                                    //edit end
                                    Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                    lstResult = countTextShow(lstCheck, countNumber, i18n);
                                    if (!CommonUtils.isEmpty(lstResult)) {
                                        writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh, topHeight,
                                                leftText, topImg, imageHalf);
                                    }
                                }
                            } else {
                                if(CommonUtils.isEmpty(lstMarkConfig)){
                                    if(!CommonUtils.isEmpty(lst.getPath())){
                                        imageConfirm= Image.getInstance(lst.getPath());
                                        imageConfirm.scalePercent(scaleMark * 100 / imageConfirm.getHeight());
                                        float imageHeight = imageConfirm.getScaledHeight();
                                        imageConfirm.setAbsolutePosition(300F, height - imageHeight);
                                        pdfPage.addImage(imageConfirm);
                                    }
                                }
                                else {
                                    if (!CommonUtils.isEmpty(lst.getPath())) {
                                        imageConfirm = Image.getInstance(lst.getPath());
                                        Float widthDiv = Float.valueOf (info.getNumWidth());
                                        Float widthHienthi = widthDiv + 300.0F;
                                        Float leftImgOriginal = 300.0F;
                                        Float divMinus = 0F;
                                        if(widthHienthi > width) {
                                            divMinus += Math.abs(widthHienthi - width);
                                            // Tru di le phai.
                                            // De text hien thi nhieu hon
                                            // Do gioi han le trai
                                            if (divMinus > 20.0F) {
                                                divMinus -= 20.0F;
                                            }
                                        }
                                        
                                        imageConfirm.scalePercent(scaleMark * 100 / imageConfirm.getHeight());
                                        float imageHeight = imageConfirm.getScaledHeight();
                                        float imageWidth = imageConfirm.getScaledWidth();
                                        imageConfirm.setAbsolutePosition(leftImgOriginal, height - imageHeight);
                                        pdfPage.addImage(imageConfirm);
                                        List<String> lstCheck = new ArrayList<>();
                                        List<String> lstResult = new ArrayList<>();
                                        lstCheck = getLstCheck(info, tmpDocCode, textCode, lst.getEmailMarked(), i18n, null);
                                        // defaule lineheigt 1.5f
                                        Float topImg =  height - imageHeight;
                                        Float imageHalf = Math.abs(imageHeight/2);
                                        Float leftText = Float.valueOf(leftImgOriginal) + imageWidth + leftTextMark;
                                        Float topHeight = 1.5F;
                                        Float fontSize = Float.valueOf(info.getNumFontSize());
                                        Float widthTextShow = widthDiv - imageWidth - witdTextMark - divMinus;
                                        //edit end
                                        Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                        lstResult = countTextShow(lstCheck, countNumber, i18n);
                                        if (!CommonUtils.isEmpty(lstResult)) {
                                            writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh, topHeight,
                                                    leftText, topImg, imageHalf);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // TungHD Them con dau xac nhan file ky chinh end
                // Datdc  dong dau tuy chon start
                if (!CommonUtils.isEmpty(listMarkInfo)) {
                    for (EntityMarkInfo entityMark : listMarkInfo) {
                        // Check xem la thong tin tam hay lay tu db
                        String markUploadStorage = entityMark
                                .getStorage();
                        String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils
                                .getStorageFolder(markUploadStorage);
                        String pathMark = markUploadStorageFolder
                                + File.separator
                                + entityMark.getImgPath();
                        Image imageMark = null;
                        // neu ton tai file anh
                        Boolean hasImage = false;
                        if(new File(pathMark).exists()) {
                            pathMark = pathMark.replace("\\",
                                    File.separator);
                            pathMark = pathMark.replace("/",
                                    File.separator);
                            imageMark = Image
                                    .getInstance(pathMark);
                            hasImage = true;
                        }
                        if (pdfPage != null) {
                            if (!isHassAccessFistPage ? i == entityMark.getNumberPage() : i == entityMark
                                    .getNumberPage() + pageInfo) {
                                Float imageWidth = 0F;
                                Float imageHeight = 0F;
                                // Do rong, cao cua khung da thu nho o web theo anh
                                // Neu ko co anh
                                if (!entityMark.getIsShowImage() && !hasImage) {
                                    //Kich thuoc default khi ko co image
                                    imageWidth = 135.0F;
                                    imageHeight = 135.0F;
                                }
                                // Neu show anh
                                if (entityMark.getIsShowImage()) {
                                    // Thu nho
                                    imageMark.scalePercent(scaleMark * 100 / imageMark.getHeight());
                                    imageWidth = imageMark.getScaledWidth();
                                    imageHeight = imageMark.getScaledHeight();
                                    imageMark.setAbsolutePosition(Float
                                            .valueOf(entityMark.getLeft()), Float
                                            .valueOf(entityMark.getTop()));
                                    pdfPage.addImage(imageMark);
                                }
                                // Neu ko show anh nhung co anh ko add
                                if ((hasImage && !entityMark.getIsShowImage())) {
                                    // Thu nho
                                    imageMark.scalePercent(scaleMark * 100 / imageMark.getHeight());
                                    imageWidth = imageMark.getScaledWidth();
                                    imageHeight = imageMark.getScaledHeight();
                                }
                                // Tinh toan text Mark
                                List<String> lstCheck = new ArrayList<>();
                                List<String> lstResult = new ArrayList<>();
                                
                                lstCheck.add(getTextLang(entityMark.getDocumentCode(),
                                        i18n.getString(I18N.Key.MARK_CODE), entityMark.getDataLabel()));
                                // Check neu co nameUsb
                                if(!CommonUtils.isEmpty(entityMark.getNameUsb())) {
                                    if (!CommonUtils.isEmpty(entityMark.getSignBy())) {
                                        entityMark.setSignBy(entityMark.getNameUsb());
                                    }
                                }
                                lstCheck.add(getTextLang(entityMark.getSignBy(),
                                        i18n.getString(I18N.Key.MARK_BY), entityMark.getDataLabel()));
                                
                                lstCheck.add(getTextLang(entityMark.getEmailBy(),
                                        i18n.getString(I18N.Key.LABEL_EMAIL), entityMark.getDataLabel()));
                                
                                lstCheck.add(getTextLang(entityMark.getTimeMark(),
                                        i18n.getString(I18N.Key.MARK_TIME), entityMark.getDataLabel()));
                                // defaule lineheigt 1.5f
                                Float top = Float.valueOf(entityMark.getTop());
                                Float imageHalf = Math.abs(imageHeight/2);
                                Float leftText = Float.valueOf(entityMark.getLeft()) + imageWidth + leftTextMark;
                                Float topHeight = 1.5F;
                                Float fontSize = Float.valueOf(entityMark.getFontSize());
                                Float widthDiv = Float.valueOf (entityMark.getWidth());
                                Float widthTextShow = widthDiv - imageWidth - witdTextMark;
                                // Dong dau khi thu qua nho
                                if (widthTextShow <= 0 ) {
                                    widthTextShow = widthDiv - 20F;
                                }
                                Integer countNumber = getCountCharacterInline(widthTextShow, fontSize, bfbh);
                                lstResult = countTextShow(lstCheck, countNumber, i18n);
                                
                                if (!CommonUtils.isEmpty(lstResult)) {
                                    writeTextMark(lstResult, pdfStamper.getOverContent(i), fontSize, bfbh, topHeight,
                                            leftText, top, imageHalf);
                                }
                            }
                        }
                    }
                }
                // Datdc dong dau tuy chon end
                // TungHD Them khung ban hanh hien thi ben phai start
                if (docCode != null && locationPromulgateFrame.equals(2L)) {
                    contentbh = pdfStamper.getOverContent(i);
                    contentbh.setGState(gsFooter);
                    contentbh.beginText();
                    contentbh.setFontAndSize(bfbh, 9.0F);
                    contentbh.showTextAligned(0, docCode, 415.0F, height - 33.0F, 0.0F);
                    if(docNumber != null){
                        contentbh.showTextAligned(0, docNumber, 415.0F, height - 45.0F, 0.0F);
                        if(docPublishDate != null){
                            contentbh.showTextAligned(0, docPublishDate, 415.0F, height - 57.0F, 0.0F);
                        }
                    } else {
                        if(!docCode.contains(i18n.getString(I18N.Key.NOT_ISSUED)) && docPublishDate != null)
                            contentbh.showTextAligned(0, docPublishDate, 415.0F, height - 44.0F, 0.0F);
                    }
                    contentbh.endText();
                    contentbh.setLineWidth(0.5F);
                    contentbh.setColorStroke(BaseColor.GRAY);
                    if(docNumber != null && docPublishDate != null){
                        contentbh.rectangle(410.0F, height - 62.0F, recLength, 40.0F);
                    } else {
                        if(!docCode.contains(i18n.getString(I18N.Key.NOT_ISSUED))){
                            contentbh.rectangle(410.0F, height - 47.0F, recLength, 25.0F);
                        } else {
                            if(docNumber != null){
                                contentbh.rectangle(410.0F, height - 49.0F, recLength, 27.0F);
                            } else {
                                contentbh.rectangle(410.0F, height - 37.0F, recLength, 15.0F);
                            }
                        }
                    }
                        
                    contentbh.stroke();
                }
                // TungHD Them khung ban hanh hien thi ben phai end 
                // TungHD hien thi khung ban hanh ben trai start
                if (docCode != null && locationPromulgateFrame.equals(1L)) {
                    contentbh = pdfStamper.getOverContent(i);
                    contentbh.setGState(gsFooter);
                    contentbh.beginText();
                    contentbh.setFontAndSize(bfbh, 9.0F);
                    contentbh.showTextAligned(0, docCode, 25.0F, height - 33.0F, 0.0F);
                    if(docNumber != null){
                        contentbh.showTextAligned(0, docNumber, 25.0F, height - 45.0F, 0.0F);
                        if(docPublishDate != null){
                            contentbh.showTextAligned(0, docPublishDate, 25.0F, height - 57.0F, 0.0F);
                        }
                    } else {
                        if(!docCode.contains(i18n.getString(I18N.Key.NOT_ISSUED)))
                            contentbh.showTextAligned(0, docPublishDate, 25.0F, height - 44.0F, 0.0F);
                    }
                    contentbh.endText();
                    contentbh.setLineWidth(0.5F);
                    contentbh.setColorStroke(BaseColor.GRAY);
                    if(docNumber != null && docPublishDate != null){
                        contentbh.rectangle(20.0F, height - 62.0F, recLength, 40.0F);
                    } else {
                        if(!docCode.contains(i18n.getString(I18N.Key.NOT_ISSUED))){
                            contentbh.rectangle(20.0F, height - 47.0F, recLength, 25.0F);
                        } else {
                            if(docNumber != null){
                                contentbh.rectangle(20.0F, height - 49.0F, recLength, 27.0F);
                            } else {
                                contentbh.rectangle(20.0F, height - 37.0F, recLength, 15.0F);
                            }
                        }
                    } 
                    contentbh.stroke();
                }
                // TungHD hien thi khung ban hanh ben trai end
                contentbh = pdfStamper.getOverContent(i);
                contentbh.setGState(gs);
                contentbh.beginText();
                contentbh.setFontAndSize(bfbh, 20);
                contentbh.showTextAligned(Element.ALIGN_CENTER, userName, 0, 0,
                        55);
                contentbh.endText();

                // Hiendv bo sung dieu kien check null
                if (signLD != null && !"".equals(signLD.trim())
                        && !"null".equals(signLD.trim())) {
                    contentbh.beginText();
                    contentbh.setFontAndSize(bf, 14);
                    contentbh.showTextAligned(Element.ALIGN_CENTER, signLD, 50,
                            0, 55);
                    contentbh.endText();
                }
            }
            outputStream.flush();
            pdfStamper.close();
            outputStream.close();

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            // if (document.isOpen()) {
            // document.close();
            // }f
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                // System.gc();

            } catch (IOException ioe) {
                LOGGER.error(ioe);
            }
        }
//        System.out.println("Da doc dc file - userId: " + copyFile);
        return number_of_pages;
    }

    /*
     * Thuc hien in van ban dong dau gui ra ngoai danh doi tuong van thu
     */
    private int writeWaterMarkPdfForSerectary(PdfReader pdfReader,
            String copyFile, List<EntityTextProcess> lstSigner, String language)
            throws DocumentException {

        int number_of_pages = 0;
        // com.itextpdf.text.Document document = null;
        FileOutputStream outputStream = null;
        try {
            // bo qua check password
            PdfReader.unethicalreading = true;
            // Hiendv bo sung dieu kien check null
            number_of_pages = pdfReader.getNumberOfPages();
            outputStream = new FileOutputStream(copyFile);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, outputStream);

            Image image1 = null;

            String pathImage = null;
            boolean exitsImageSign = false;
            String sContextPath = CommonUtils.getAppConfigValue("folder_upload")
                    + File.separator
                    + CommonUtils.getAppConfigValue("path_to_upload");
            String pathUploadImageSinger = CommonUtils.getAppConfigValue("pathUploadImageSinger");
            // End them khung ban hanh
            // Duong dan anh chu ky mac dinh
            if (language == null || !"vi".equals(language)) {
                language = "en";
            }
            String defaultImagePath = sContextPath + File.separator
                    + pathUploadImageSinger + File.separator + language + "_default_sign.png";
            defaultImagePath = defaultImagePath.replace("\\", File.separator);
            defaultImagePath = defaultImagePath.replace("/", File.separator);
            int i = 0;
            while (i < number_of_pages) {

                i++;
                PdfContentByte pdfPage = null;
                if (pdfStamper.getOverContent(i) != null) {
                    pdfPage = pdfStamper.getOverContent(i);
                }
                // [tuantm30 - start] Sua phan add anh chu ky phai ngang
                boolean isVertical = false;
                int rotate = pdfReader.getPageRotation(i);
                if ( rotate == 90 || rotate == 270) {
                    isVertical = true;
                }
                // [tuantm30 - end] Sua phan add anh chu ky phai ngang
                // underContent.
                PdfDictionary page = pdfReader.getPageN(i);
                PdfArray annotsArray = null;
                // Kiem tra tung trang mot xem co comment
                if (page.getAsArray(PdfName.ANNOTS) != null) {
                    annotsArray = page.getAsArray(PdfName.ANNOTS);
                    for (ListIterator iter = annotsArray.listIterator(); iter
                            .hasNext();) {
                        PdfDictionary annot = (PdfDictionary) PdfReader
                                .getPdfObject((PdfObject) iter.next());
                        PdfString content = (PdfString) PdfReader
                                .getPdfObject(annot.get(PdfName.CONTENTS));
                        PdfArray rectArr = (PdfArray) annot.get(PdfName.RECT);

                        if (content != null) {

                            String strContent = content.toUnicodeString()
                                    .replaceAll(" ", "").replaceAll("\r\n", "")
                                    .trim();

                            int size = rectArr.size();
                            float left = 0f;
                            float top = 0f;
                            // Xac dinh toa do cua comment
                            if (size > 2) {
                                //310517 kiem tra van ban bo tri ngang
                                if (isVertical) {
                                    // [tuantm30 - start] Sua phan add anh chu ky phai ngang
                                    if (rotate == 270) {
                                        PdfNumber obj = (PdfNumber) rectArr.getPdfObject(2);
                                        Rectangle rec1 = pdfReader.getPageSizeWithRotation(i);
                                        if (obj != null) {
                                            top = obj.floatValue();
                                        }
                                        PdfNumber obj1 = (PdfNumber) rectArr.getPdfObject(3);
                                        if (obj1 != null) {
                                            Float widthRec = rec1.getLeft() == 0.0 ? rec1.getRight() : rec1.getLeft();
                                            left = widthRec - obj1.floatValue();
                                        }
                                    }
                                    if (rotate == 90) {
                                        PdfNumber obj = (PdfNumber) rectArr.getPdfObject(2);
                                        Rectangle rec1 = pdfReader.getPageSizeWithRotation(i);
                                        if (obj != null) {
                                            top = rec1.getTop() - obj.floatValue();
                                        }
                                        PdfNumber obj1 = (PdfNumber) rectArr.getPdfObject(3);
                                        if (obj1 != null) {
                                            left = obj1.floatValue();
                                        }
                                    }
                                    // [tuantm30 - end] Sua phan add anh chu ky phai ngang
                                } else {
                                    PdfNumber obj = (PdfNumber) rectArr
                                            .getPdfObject(0);
                                    if (obj != null) {
                                        left = obj.floatValue();
                                    }
                                    PdfNumber obj1 = (PdfNumber) rectArr
                                            .getPdfObject(1);
                                    if (obj1 != null) {
                                        top = obj1.floatValue();
                                    }
                                }
                            }
                            // Add image
                            if (left > 0 && top > 0) {

                                if (lstSigner != null && lstSigner.size() > 0) {
                                    // Kiem tra van ban chua trinh ky va van thu
                                    // doc van ban thi hien thi xem chu ky truoc
                                    // duoc
                                    if (lstSigner.get(0).getState() != null
                                            && !lstSigner.get(0).getState()
                                            .equals(0L)) {
                                        int sizeImage = lstSigner.size();
                                        for (int j = 0; j < sizeImage; j++) {
                                            pathImage = null;
                                            if (lstSigner.get(j)
                                                    .getStatusSign() != null
                                                    && lstSigner
                                                    .get(j)
                                                    .getStatusSign()
                                                    .equals(TEXT_ACTION_STATE_LD_SIGNER)
                                                    && lstSigner
                                                    .get(j)
                                                    .getSignImageIndex() != null
                                                    && lstSigner.get(j)
                                                    .getNameImageSign() != null
                                                    && lstSigner
                                                    .get(j)
                                                    .getSignImageIndex()
                                                    .toString()
                                                    .equals(strContent)) {

                                                if ((lstSigner.size() - j) > 1) {
                                                    pathImage = sContextPath
                                                            + File.separator
                                                            + pathUploadImageSinger
                                                            + File.separator
                                                            + lstSigner
                                                            .get(j)
                                                            .getNameImageSign()
                                                            .trim();
                                                    if (lstSigner.get(j).getStorage() != null && lstSigner.get(j).getPathImageSign() != null) {
                                                        pathImage = CommonUtils.getAppConfigValue(lstSigner.get(j).getStorage()) + File.separator
                                                                + lstSigner.get(j).getPathImageSign().trim();
                                                    }
                                                } else if (lstSigner.get(j)
                                                        .getImagePrint() != null
                                                        && !"".equals(lstSigner
                                                                .get(j)
                                                                .getImagePrint()
                                                                .trim()
                                                        )) {
                                                    pathImage = sContextPath
                                                            + File.separator
                                                            + pathUploadImageSinger
                                                            + File.separator
                                                            + lstSigner
                                                            .get(j)
                                                            .getImagePrint()
                                                            .trim();
                                                    if (lstSigner.get(j).getStoragePrint() != null && lstSigner.get(j).getImagePrint() != null) {
                                                        pathImage = CommonUtils.getAppConfigValue(lstSigner.get(j).getStoragePrint()) + File.separator
                                                                + pathUploadImageSinger + File.separator + lstSigner.get(j).getImagePrint().trim();
                                                    }
                                                }

                                                try {
                                                    if (pathImage != null && pdfPage != null) {
                                                        pathImage = pathImage
                                                                .replace(
                                                                        "\\",
                                                                        File.separator);
                                                        pathImage = pathImage
                                                                .replace(
                                                                        "/",
                                                                        File.separator);
                                                        image1 = Image
                                                                .getInstance(pathImage);

                                                        image1.setAbsolutePosition(
                                                                left - 60, top - 70);
                                                        image1.scaleToFit(135, 135);
                                                        pdfPage.addImage(image1);
                                                    }
                                                } catch (Exception ex1) {
                                                    LOGGER.error(ex1);
                                                    image1 = Image.getInstance(defaultImagePath);
                                                    image1.setAbsolutePosition(
                                                            left - 60, top - 70);
                                                    image1.scaleToFit(135, 135);
                                                    pdfPage.addImage(image1);
                                                }
                                            }
                                        }
                                    }
                                } else {
//                                    if (exitsImageSign == false && pdfPage != null) {
//                                        try {
//                                            image1 = Image.getInstance(defaultImagePath);
//                                            image1.setAbsolutePosition(
//                                                    left - 60, top - 70);
//                                            image1.scaleToFit(135, 135);
//                                            pdfPage.addImage(image1);
//                                        } catch (Exception ex1) {
//                                            LOGGER.error(ex1);
//                                        }
//                                    }
                                }
                            }
                            annot.clear();
                        }
                    }
                }
            }
            outputStream.flush();
            pdfStamper.close();
            outputStream.close();

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            // if (document.isOpen()) {
            // document.close();
            // }f
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                // System.gc();

            } catch (IOException ioe) {
                LOGGER.error(ioe);
            }
        }

        return number_of_pages;
    }

    /**
     * Convert danh sach chu ky phu
     *
     * @param sigArrayList
     * @return
     */
    private String[][] getSignatureOtherInfo(
            List<EntityTextProcess> sigArrayList) {
        if (sigArrayList.isEmpty()) {

            return null;
        }

        String signDate = "";
        String signComment = "";

        String stateName = "";

        // Hiendv chinh sua bo sung thong tin don vi tu usertoken
        String[][] arrSign = new String[sigArrayList.size()][5];

        try {
            // Du nguyen du lie hien thi theo chu ky cu
            for (int i = 0; i < sigArrayList.size(); i++) {
//                System.out.println("Danh sach ky phu:"
//                        + sigArrayList.get(i).getChiefName());
                EntityTextProcess sa = sigArrayList.get(i);
                if (sa != null) {
                    // Thoi gian ky
                    signDate = sa.getSignDate() != null ? sa.getSignDate() : "";
                    // Comment ky
                    signComment = sa.getTextComment() != null ? sa
                            .getTextComment() : "";
                    // Nguoi ky
                    arrSign[i][0] = sa.getChiefName();
                    // Don vi ky
                    arrSign[i][1] = sa.getGroupName();
                    // Trang thai ky
                    if (sa.getState() != null && sa.getSignatureType() != null) {
                        if (sa.getState().equals(4L)
                                && sa.getSignatureType().equals(3L)) {
                            stateName = "Đã ký duyệt";
                        } else if (sa.getState().equals(4L)
                                && sa.getSignatureType().equals(1L)) {
                            stateName = "Đã xét duyệt";
                        } else if (sa.getState().equals(4L)
                                && sa.getSignatureType().equals(2L)) {
                            stateName = "Đã ký nháy";
                        } else if (sa.getState().equals(2L)
                                && sa.getSignatureType().equals(3L)) {
                            stateName = "Đã từ chối ký duyệt";
                        } else if (sa.getState().equals(2L)
                                && sa.getSignatureType().equals(1L)) {
                            stateName = "Đã từ chối xét duyệt";
                        } else if (sa.getState().equals(2L)
                                && sa.getSignatureType().equals(2L)) {
                            stateName = "Đã từ chối ký nháy";
                        }

                    }

                    arrSign[i][2] = stateName;
                    // Thoi gian ky
                    arrSign[i][3] = signDate;
                    // Comment ky
                    arrSign[i][4] = signComment;

                }

            }

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return arrSign;
    }

    /**
     * <b>Them trang dau hien thi danh sach nguoi ky</b>
     *
     * @param data
     * @param pathFileSplit
     * @param language
     * @return
     * @throws DocumentException
     * @throws Exception
     */
    public byte[] addSigningPage(byte[] data, String pathFileSplit,
            String language) throws DocumentException, Exception {

        // -----------------------------------------------
        // Create output PDF
        // lay danh sach chu ki
        ArrayList<SigAttribute> sigArrayList = getListSinger(data);

        // convert sang dang string
        String[][] arrSign = getSignatureInfo3(sigArrayList);

        byte[] newData = data;

        // chen chu ki vao trang dau tien
        if (arrSign != null && arrSign[0][0] != null
                && !"".equals(arrSign[0][0].trim())) {
            newData = addSignPageToFile(data, arrSign, pathFileSplit, language);
        }
        return newData;
    }

    /**
     * <b>Lay thong tin chu ky</b>
     *
     * @param sigAttributes mang thuoc tinh cua chu ky
     * @return
     */
    private String[][] getSignatureInfo3(ArrayList<SigAttribute> sigAttributes) {

        if (sigAttributes.isEmpty()) {
            return null;
        }
        String[][] signInfos = new String[sigAttributes.size()][4];
        try {
            SigAttribute sa;
            String signDate;
            String signReason;
            String department;
            for (int i = 0; i < sigAttributes.size(); i++) {
                sa = sigAttributes.get(i);
                if (sa != null) {
                    signDate = sa.getSignDate() != null ? sa.getSignDate() : "";
                    signReason = sa.getSignReason() != null ? sa
                            .getSignReason() : "";
                    // Don vi nguoi ky
                    department = sa.getDeptOfSigner();
                    if (sa.getLocation() != null
                            && !"".equals(sa.getLocation().trim())
                            && !"Viet Nam".equals(sa.getLocation().trim())) {
                        // Loai bo thu tu anh chu ky neu co(don_vi::stt)
                        department = sa.getLocation().split("::")[0];
                    }
                    if (!CommonUtils.isEmpty(sa.getSubject())) {
                        signInfos[i][0] = sa.getSubject().toUpperCase();
                    } else {
                        signInfos[i][0] = sa.getSubject();
                    }
                    signInfos[i][1] = department;
                    signInfos[i][2] = signDate;
                    signInfos[i][3] = signReason;
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return signInfos;
    }

    public static X509Principal getSubjectX509Principal(X509Certificate cert)
            throws CertificateEncodingException {

        try {
            TBSCertificateStructure tbsCert = TBSCertificateStructure.getInstance(
                    ASN1Primitive.fromByteArray(cert.getTBSCertificate()));
            return new X509Principal(X509Name.getInstance(tbsCert.getSubject()));
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }

    /**
     * <b>Lay danh sach UID cua CA trong file ky</b>
     *
     * @param fileBytes
     * @return
     * @throws Exception
     */
    public List<EntitySignature> getListSignature(byte[] fileBytes) throws Exception {

        List<EntitySignature> listSignature = new ArrayList<>();
        BouncyCastleProvider providerBC = new BouncyCastleProvider();
        Security.addProvider(providerBC);
        PdfReader reader = new PdfReader(fileBytes);
        AcroFields af = reader.getAcroFields();
        ArrayList names = getSortedName(af);
        PdfPKCS7 pk;
        Certificate[] chain;
        Vector vector;
        EntitySignature signature;
        String uid;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (int k = 0; k < names.size(); ++k) {
            signature = new EntitySignature();
            uid = null;
            String name = (String) names.get(k);
            // End revision extraction
            pk = af.verifySignature(name);
            chain = pk.getSignCertificateChain();
            vector = getSubjectX509Principal((X509Certificate) chain[0]).getValues(X509Principal.UID);
            if (vector.size() > 0) {
                uid = vector.firstElement().toString();
            }
            signature.setUid(uid);
            signature.setSignDate(sdf.format(pk.getSignDate().getTime()));
            listSignature.add(signature);
        }
        try {
            reader.close();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        Collections.reverse(listSignature);
        return listSignature;
    }
    
    /**
     * @author DatDC
     * countTextShow
     * lay text hien tren 1 dong cua con dau
     * @param abc
     * @param countNumber
     * @param i18n
     * @return
     */
    public List<String> countTextShow(List<String> abc, Integer countNumber, I18N i18n) {
        List<String> lst = new ArrayList<>();
        Pattern p = Pattern.compile(".{1,"+countNumber+"}(\\s+|$)");
        if (!CommonUtils.isEmpty(abc)) {
            for (String entity :abc) {
                if (!CommonUtils.isEmpty(entity)) {
                    if (entity.length() > countNumber) {
                        if (entity.contains(i18n.getString(I18N.Key.LABEL_EMAIL))) {
                            String[] splited = entity.split("\\s+");
                            for (String text : splited) {
                                lst.add(text.trim());
                            }
                        }
                        else if (isValid(entity.trim())) {
                            lst.add(entity.trim());
                        }
                        else {
                            Matcher m = p.matcher(entity);
                            while(m.find()) {
                                lst.add(m.group().trim());
                            }
                        }
                    } else {
                        lst.add(entity);
                    }
                }
            }
        }
        return lst;
    }
    
    /**
     * @author DatDC
     * getCountCharacterInline
     * dem so luong ky tu hien tren 1 dong con dau
     * @param widthText
     * @param fontSize
     * @param bfbh
     * @return
     */
    public Integer getCountCharacterInline (Float widthText, Float fontSize, BaseFont bfbh) {
        String text = "a";
        java.awt.Font defaultFont = new java.awt.Font("arial", Font.NORMAL, fontSize.intValue());
        int abcd = (int) defaultFont.getStringBounds(text, new FontRenderContext(defaultFont.getTransform(), false, false)).getBounds().getWidth();
        Float abcTmp = widthText/abcd;
        abcd = abcTmp.intValue();
        return abcd;
    }
    
    /**
     * @author DatDC
     * getCountCharacterInline
     * Lay config theo ngon ngu cua con dau
     * @param text
     * @param lang
     * @param label
     * @return
     */
    private String getTextLang(String text, String lang,  Long label) {
        String result = "";
        if(!CommonUtils.isEmpty(text)) {
            if (Constants.MARK.IS_SHOW.equals(label)) {
                result = lang + ": " + text;
            } else {
                result = text;
            }
        }
        return result;
    }

    /**
     * @author DatDC
     * writeTextMark
     * comon gen text theo con dau
     * @param lstResult
     * @param textMark
     * @param fontSize
     * @param bfbh
     * @param topHeight
     * @param leftText
     * @param top
     * @param imageHalf
     */
    public void writeTextMark(List<String> lstResult, PdfContentByte textMark, Float fontSize, BaseFont bfbh,
            Float topHeight, Float leftText, Float top, Float imageHalf) {
        textMark.beginText();
        textMark.setFontAndSize(bfbh, fontSize);
        Integer position = lstResult.size();
        Integer counTmp = (position / 2);
        Integer countTmpCheck = counTmp;
        Boolean evenNumber = false;
        if (position % 2 == 0) {
            evenNumber = true;
            topHeight = Math.abs(1.5F / 2);
            countTmpCheck = counTmp - 1;
        }
        if (evenNumber) {
            Float fontHalf = Math.abs(fontSize / 2);
            for (int j = 0; j < countTmpCheck; j++) {
                textMark.showTextAligned(0, lstResult.get(j), leftText, top + imageHalf + fontHalf
                        + ((fontSize + 1.5F) * (countTmpCheck - j)), 0.0F);
            }
            textMark.showTextAligned(0, lstResult.get(counTmp - 1), leftText, top + imageHalf + fontHalf, 0.0F);
            textMark.showTextAligned(0, lstResult.get(counTmp), leftText, top + imageHalf - fontHalf, 0.0F);
            int z = 0;
            for (int j = counTmp + 1; j < position; j++) {
                z++;
                textMark.showTextAligned(0, lstResult.get(j), leftText, top + imageHalf - fontHalf
                        - ((fontSize + 1.5F) * (z)), 0.0F);
            }
        } else {
            for (int j = 0; j < countTmpCheck; j++) {
                textMark.showTextAligned(0, lstResult.get(j), leftText, top + imageHalf + topHeight
                        + ((fontSize + 1.5F) * (countTmpCheck - j)), 0.0F);
            }
            textMark.showTextAligned(0, lstResult.get(counTmp), leftText, top + imageHalf, 0.0F);
            int z = 0;
            for (int j = counTmp + 1; j < position; j++) {
                z++;
                textMark.showTextAligned(0, lstResult.get(j), leftText, top + imageHalf - topHeight
                        - ((fontSize + 1.5F) * (z)), 0.0F);
            }
        }
        textMark.endText();
    }
    
    /**
     * @author Tunghd
     * getLstCheck
     * Lay config thong tin cho con dau mac dinh
     * @param markConfig
     * @param docCode
     * @param textCode
     * @param strEmail
     * @param i18n
     * @param styleMark
     * @return
     */
    public List<String> getLstCheck ( EntityMarkInfo markConfig, String docCode, String textCode, String strEmail, I18N i18n, Long styleMark){
        List<String> lstCheck = new ArrayList<>();
        String labelDocNumber = "";
        String labelSignBy = "";
        String labelEmail = "";
        String labelTimeSign = "";
        String timeZone = "+07:00";
        if(markConfig.getNumLable() != null && markConfig.getNumLable().equals(1L)){
            if(styleMark == null && docCode.length() > 0){
                labelDocNumber = i18n.getString(I18N.Key.DOCUMENT_CODE) + ": " + docCode;
            } else {
                labelDocNumber = i18n.getString(I18N.Key.DOCUMENT_CODE) + ": " + textCode;
            }
            labelSignBy = i18n.getString(I18N.Key.LABEL_SIGN_BY) + ": " + markConfig.getSignBy();
            labelEmail = i18n.getString(I18N.Key.LABEL_EMAIL) + ": "  + strEmail;
            labelTimeSign = i18n.getString(I18N.Key.LABEL_TIME_SIGN) + ": " + markConfig.getTimeMark() + timeZone;
        } else {
            if(styleMark == null && docCode.length() > 0){
                labelDocNumber =  docCode;
            } else {
                labelDocNumber =  textCode;
            }
            labelSignBy = markConfig.getSignBy();
            labelEmail = strEmail;
            labelTimeSign = markConfig.getTimeMark();
        }
        
        if(markConfig.getNumCode() != null && markConfig.getNumCode().equals(1L)){
            lstCheck.add(labelDocNumber);
        } else {
            lstCheck.add("");
        }
        if(markConfig.getNumSignBy() != null && markConfig.getNumSignBy().equals(1L)){
            lstCheck.add(labelSignBy);
        }  else {
            lstCheck.add("");
        }
        if(markConfig.getNumEmailBy() != null && markConfig.getNumEmailBy().equals(1L)){
            lstCheck.add(labelEmail);
        } else {
            lstCheck.add("");
        }
        if(markConfig.getNumTimeSign() != null && markConfig.getNumTimeSign().equals(1L)){
            lstCheck.add(labelTimeSign);
        } else {
            lstCheck.add("");
        }
        return lstCheck;
    }
    
    /**
     * @author DatDC
     * getSignatureInfoMark
     * Con vertchu ki ra dang string cho con dau
     * @param sigArrayList
     * @param countPage
     * @return
     */
    private String[][] getSignatureInfoMark(ArrayList<SigAttribute> sigArrayList, int countPage) {
        if (sigArrayList.isEmpty()) {

            return null;
        }
//        String result = "";
        String signDate = "";
        String signReason = "";

//        String personal = "";
        String department = "";
        String isImageSign = "";
        // Hiendv chinh sua bo sung thong tin don vi tu usertoken
        String[][] arrSign = new String[sigArrayList.size()][4];
        String[][] arrShownSign = new String[sigArrayList.size()][4];
        int idx = 0;

        try {
            // Du nguyen du lie hien thi theo chu ky cu
            for (int i = 0; i < sigArrayList.size(); i++) {
                SigAttribute sa = sigArrayList.get(i);
                if (sa != null) {
//                    result = sa.getResult() != null ? sa.getResult() : "";
                    signDate = sa.getSignDate() != null ? sa.getSignDate() : "";
                    signReason = sa.getSignReason() != null ? sa
                            .getSignReason() : "";

//                    personal = sa.getSignName();
                    // Mac dinh lay don vi hien thi theo don vi cua chung
                    // thu
                    department = sa.getDeptOfSigner();
                    isImageSign = "";

                    if (sa.getLocation() != null
                            && !"".equals(sa.getLocation().trim())
                            && !"Viet Nam".equals(sa.getLocation().trim())) {
                        /**
                         * Tach thong tin anh chu ky ra khoi don vi neu co
                         */

                        // 201812-Pitagon: add
                        JSONObject obj = null;
                        String image = null;
                        String marked = null;
                        String imageConfirm = null;
                        String markedConfirm = null;
                        String markerEmailOrg = null;
                        String markerEmailConfirm = null;
                        String signLocate = null;
                        String groupType = null;
                        String sigLocation = sa.getLocation();
                        String lstMark = null;
                        String lstConfigImage = null;
                        if (CommonUtils.isJSON(sa.getLocation())) {
                            obj = new JSONObject(sa.getLocation());
                            image = obj.has("image") ? obj.getString("image") : null;
                            imageConfirm = obj.has("imageConfirm") ? obj.getString("imageConfirm") : null;
                            markerEmailOrg = obj.has("markerEmailOrg") ? obj.getString("markerEmailOrg") : null;
                            markerEmailConfirm = obj.has("markerEmailConfirm") ? obj.getString("markerEmailConfirm") : null;
                            sigLocation = obj.has("location") ? obj.getString("location") : null;
                            marked = obj.has("marked") ? obj.getString("marked") : null;
                            markedConfirm = obj.has("markedConfirm") ? obj.getString("markedConfirm") : null;
                            signLocate = obj.has("signLocate") ? obj.getString("signLocate") : null;
                            groupType = obj.has("groupType") ? obj.getString("groupType") : null;
                            // Lst con dau tuy chon
                            lstMark = obj.has("lstMark") ? obj.getString ("lstMark") : null;
                            lstConfigImage = obj.has("lstConfigImage") ? obj.getString ("lstConfigImage") : null;
                        }

                        String[] arrLocation = null;
                        if (!CommonUtils.isEmpty(sigLocation)) {
                            arrLocation = sigLocation.split("::");
                            if (arrLocation.length > 1) {
                                isImageSign = arrLocation[1];
                            }
                        }

                        obj = new JSONObject();
                        obj.put("image", image);
                        obj.put("marked", marked);
                        obj.put("imageConfirm", imageConfirm);
                        obj.put("markedConfirm", markedConfirm);
                        obj.put("markerEmailOrg", markerEmailOrg);
                        obj.put("markerEmailConfirm", markerEmailConfirm);
                        obj.put("signLocate", signLocate);
                        obj.put("groupType", groupType);
                        // lst con dau tuy chon
                        obj.put("lstMark", lstMark);
                        //Tunghd add
                        obj.put("lstConfigImage", lstConfigImage);
                        obj.put("countPage", countPage);

                        if ((!CommonUtils.isEmpty(marked) || !CommonUtils.isEmpty(markedConfirm)) && CommonUtils.isEmpty(isImageSign)) {
                            isImageSign = "1";
                        }
                        if (arrLocation != null) {
                            obj.put("location", arrLocation[0]);
                        }
                        department = obj.toString();
                    }
                    if (!"0".equals(isImageSign.trim())
                            && !"".equals(isImageSign.trim())) {
                        if (!CommonUtils.isEmpty(sa.getSubject())) {
                            arrShownSign[idx][0] = sa.getSubject().toUpperCase();
                        } else {
                            arrShownSign[idx][0] = sa.getSubject();
                        }

                        arrShownSign[idx][1] = department;

                        arrShownSign[idx][2] = signDate;

                        // arrShownSign[idx][3] = "há»£p lá»‡";
                        arrShownSign[idx][3] = (signReason.length() != 0 ? signReason
                                : "");

                        idx++;
                    }
                    if (!CommonUtils.isEmpty(sa.getSubject())) {
                        arrSign[i][0] = sa.getSubject().toUpperCase();
                    } else {
                        arrSign[i][0] = sa.getSubject();
                    }
                    arrSign[i][1] = department;

                    arrSign[i][2] = signDate;

                    // arrSign[i][3] = "há»£p lá»‡";
                    arrSign[i][3] = (signReason.length() != 0 ? signReason : "");
                }

            }

            /*
             * Kiem tra ds ky co anh chu ky moc thi hien thá»‹ theo d/s co anh
             * chu ky moc , nguoc lai hien thi tat ca
             */
            if (arrShownSign[0][0] != null
                    && !"".equals(arrShownSign[0][0].trim())) {
                return arrShownSign;
            } else {
                return arrSign;
            }

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return arrSign;
    }
    
    /**
     * @author DatDC
     * isValid
     * check dinh dang email cho con dau
     * @param email
     * @return
     */
    public static boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
    
    /**
     * @author TungHD
     * getValueFromSysParam
     * Lay param cau hinh cho khung ban hanh
     * @return
     */
    public Long getValueFromSysParam(){
        SystemParameterDAO spd = new SystemParameterDAO();
        EntitySystemParameter entity = spd.getConfigValueByCode(PROMULGATE_FRAME);
        if(entity.getValue() != null){
            return Long.valueOf(entity.getValue());
        }
        return null;
    }

 }
