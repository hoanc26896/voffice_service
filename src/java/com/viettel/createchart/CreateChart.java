package com.viettel.createchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import com.viettel.createchart.dto.DataChartDTO;
import com.viettel.createchart.dto.TitleChartDTO;
import com.viettel.voffice.utils.CommonUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author datnv5
 */
public class CreateChart {
    private static final Logger LOGGER = Logger.getLogger(CreateChart.class);
    static Paint paintColumn1 = new Color(13, 142, 250);
    static Paint paintColumn2 = new Color(68, 114, 196);
    static Paint paintColumn3 = new Color(255, 192, 0);
    static Paint paintColumn4 = new Color(30, 113, 69);
    static Paint paintLine1 = new Color(13, 142, 250);
    static Paint paintLine2 = new Color(195, 45, 46);
    static Paint paintLine3 = new Color(132, 170, 51);
    static Paint paintLine4 = new Color(150, 67, 5);

    /**
     * Thực hiện vẽ biểu đồ dạng đường
     *
     * @param strTitleChart : tên biểu đồ
     * @param arrStrListLabelY: mảng danh sách tên trên trục y
     * @param dataChart : dữ liệu vẽ biểu đồ mảng 2 chiều
     * @param arrStrLegendLabels : mang danh sach ten chu thich cac duong ve
     * bieu do
     * @param arrPaint : mang danh sach cac mau tuong ung voi thu tu label
     */
    public static void chartLineDraw(List<TitleChartDTO> listTileLine,
            List<DataChartDTO> listDataChart, String strUnitX, String strUnitY, String strNameChart, String strPathFolder, String strTitleName) {

        createChartPanel(listTileLine, listDataChart, strNameChart, strUnitX, strUnitY, strPathFolder, strTitleName);
    }

    /**
     * Tao bieu do duong
     *
     * @param listTileLine:danh sach ten diem tren truc hoanh
     * @param listDataChart: danh sach du lieu ve bieu do
     * @param strNameChart:ten bieu do
     * @param strUnitX: ten don vi do
     * @param strUnitY: ten don vi do tren truc tung
     * @param strNameImg: duong dan img
     */
    private static void createChartPanel(List<TitleChartDTO> listTileLine,
            List<DataChartDTO> listDataChart, String strNameChart, String strUnitX, String strUnitY, String strPathFolder, String strNameImg) {
        String chartTitle = strNameChart;
        String categoryAxisLabel = strUnitX;
        String valueAxisLabel = strUnitY;

        CategoryDataset dataset = createDataset(listTileLine, listDataChart);

        JFreeChart chart = ChartFactory.createLineChart(chartTitle, categoryAxisLabel, valueAxisLabel, dataset);

        customizeChart(chart, listDataChart);

        // saves the chart as an image files
        File imageFile = new File(strPathFolder + strNameImg);



        int width = 2016;
        int height = 1268;

        try {
            ChartUtilities.saveChartAsJPEG(imageFile, chart, width, height);
            //artAsPNG(imageFile, chart, width, height);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        //return new ChartPanel(chart);
    }

    private static Double getMinOrMaxValue(List<DataChartDTO> listDataChart, Boolean isMin) {
        double result = 0;
        Boolean isValue = false;
        if (listDataChart != null) {
            if (isMin) {
                result = 1.7976931348623157E+308;
                for (int i = 0; i < listDataChart.size(); i++) {
                    if (listDataChart.get(i).getValue() != null) {
                        if (!isValue) {
                            isValue = true;
                        }
                        double value = listDataChart.get(i).getValue().doubleValue();
                        if (value < result) {
                            result = value;
                        }
                    }
                }
            } else {
                result = -1.7976931348623157E+308;
                for (int i = 0; i < listDataChart.size(); i++) {
                    if (listDataChart.get(i).getValue() != null) {
                        if (!isValue) {
                            isValue = true;
                        }
                        double value = listDataChart.get(i).getValue().doubleValue();
                        if (value > result) {
                            result = value;
                        }
                    }
                }
            }
        }
        if (!isValue) {
            result = 0;
        }
        return result;
    }

    /**
     * cấu hình biểu đồ, độ mảnh, mầu biểu đồ
     *
     * @param chart
     */
    private static void customizeChart(JFreeChart chart, List<DataChartDTO> listDataChart) {
        String fontPath = CommonUtils.getAppConfigValue("path_urlFont");
        try {
            Font defaultFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath + File.separatorChar + "timesbd.ttf"));


            Font font2 = defaultFont.deriveFont(30f);
            Font myFont = defaultFont.deriveFont(28f);
            CategoryPlot plot = chart.getCategoryPlot();
            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            // sets paint color for each series
            renderer.setSeriesPaint(0, paintColumn1);
            renderer.setSeriesPaint(1, paintColumn2);
            renderer.setSeriesPaint(2, paintColumn3);
            renderer.setSeriesPaint(3, paintColumn4);
            renderer.setSeriesPaint(4, Color.cyan);

            // sets thickness for series (using strokes)
            renderer.setSeriesStroke(0, new BasicStroke(2.5f));
            renderer.setSeriesStroke(1, new BasicStroke(2.5f));
            renderer.setSeriesStroke(2, new BasicStroke(2.5f));
            renderer.setSeriesStroke(3, new BasicStroke(2.5f));
            renderer.setSeriesStroke(4, new BasicStroke(2.5f));

            // sets paint color for plot outlines
            plot.setOutlinePaint(Color.BLACK);
            plot.setOutlineStroke(new BasicStroke(1.0f));
            plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

            // sets renderer for lines
            plot.setRenderer(renderer);

            // sets plot background
            plot.setBackgroundPaint(Color.WHITE);
            // sets paint color for the grid lines
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.BLACK);

            plot.setDomainGridlinesVisible(true);
            plot.setDomainGridlinePaint(Color.BLACK);


            //phần tiêu đề biểu đồ
            chart.getTitle().setFont(font2);

            //phan chu thich
            //border phan chu thich bottom        

            LegendTitle legend = chart.getLegend();
            legend.setFrame(new BlockBorder(Color.LIGHT_GRAY));
            legend.setMargin(12, 6, 2, 2);
            legend.setItemLabelPadding(new RectangleInsets(4.0, 12.0, 4.0, 12.0));
            legend.setBorder(0D, 0D, 0D, 0D);
            legend.setItemFont(myFont);
            legend.setVisible(false);

            CategoryAxis categoryaxis = plot.getDomainAxis();

            NumberAxis numberaxis = (NumberAxis) plot.getRangeAxis();
            //numberaxis.setNumberFormatOverride(new DecimalFormat("#######.##"));
            numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            categoryaxis.setTickLabelFont(myFont);
            numberaxis.setTickLabelFont(myFont);
            categoryaxis.setLabelFont(font2);
            numberaxis.setLabelFont(font2);
            numberaxis.setLowerMargin(0D);

            categoryaxis.setCategoryMargin(0.0);
            plot.setAxisOffset(new RectangleInsets(1D, 1D, 1D, 1D));
            plot.setRangeZeroBaselineVisible(true);
            Double minY = getMinOrMaxValue(listDataChart, true);
            Double maxY = getMinOrMaxValue(listDataChart, false);
            if (!maxY.equals(minY)) {
                if (minY >= 0) {
                    minY = 0D;
                }
                plot.getRangeAxis().setRangeWithMargins(minY, maxY);
            }
        } catch (FontFormatException | IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error("ERR!", e);
        }
        // TODO Auto-generated catch block
        //tao duong ke dut cho cac duong nam khac
        /*LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer)plot.getRenderer();
        lineandshaperenderer.setSeriesStroke(1, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {10F, 6F}, 0.0F));
        lineandshaperenderer.setSeriesStroke(2, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {6F, 6F}, 0.0F));
        lineandshaperenderer.setSeriesStroke(3, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {6F, 6F}, 0.0F));
        lineandshaperenderer.setSeriesStroke(4, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {6F, 6F}, 0.0F));*/

        //tao duong ke dut cho cac duong nam khac
        /*LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer)plot.getRenderer();        
         lineandshaperenderer.setSeriesStroke(1, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {10F, 6F}, 0.0F));
         lineandshaperenderer.setSeriesStroke(2, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {6F, 6F}, 0.0F));
         lineandshaperenderer.setSeriesStroke(3, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {6F, 6F}, 0.0F));
         lineandshaperenderer.setSeriesStroke(4, new BasicStroke(2.0F, 1, 1, 1.0F, new float[] {6F, 6F}, 0.0F));*/
    }

    /**
     * tao data set cho thong tin ve bieu do
     *
     * @param listTileLine :danh sach tieu chi vẽ biểu đồ
     * @param listDataChart danh sach du lieu tuong ung voi loai tieu chi ve
     * bieu do
     * @return
     */
    private static CategoryDataset createDataset(List<TitleChartDTO> listTileLine,
            List<DataChartDTO> listDataChart) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TitleChartDTO itemTitleLine;
        DataChartDTO itemData;

        //kiem tra trang thai cua danh sach du lieu truyen vao
        if (listTileLine == null || listTileLine.size() <= 0
                || listDataChart == null || listDataChart.size() <= 0) {
            //truong hop du lieu bi null hoac khong co
            dataset = new DefaultCategoryDataset();
        } else {
            //truong hop co du lieu, tien hanh set vao data set
            String codeSeries;
            String titleSeries;
//            Double value;
            //duyet danh sach tieu chi
            for (int i = 0; i < listTileLine.size(); i++) {
                //thuc hien tach du lieu theo loai bieu do
                itemTitleLine = listTileLine.get(i);
                titleSeries = itemTitleLine.getTitle();
                codeSeries = itemTitleLine.getCode();
                //gan danh sách dataset cho 1 loai tieu chi
                for (int j = 0; j < listDataChart.size(); j++) {
                    itemData = listDataChart.get(j);
                    //gan du lieu vao tung duong cho bieu do
                    if (codeSeries.equals(itemData.getCode())) {
                        dataset.addValue(itemData.getValue(), titleSeries, itemData.getColumnTitle());
                    }
                }

            }
        }
        return dataset;
    }
}
