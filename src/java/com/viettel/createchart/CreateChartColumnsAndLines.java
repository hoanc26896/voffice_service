package com.viettel.createchart;

import com.viettel.createchart.dto.DataChartDTO;
import com.viettel.createchart.dto.TitleChartDTO;
import com.viettel.voffice.utils.CommonUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author datnv5
 */
public class CreateChartColumnsAndLines {

    /** Log file */
    private static final Logger LOGGER = Logger.getLogger(CreateChartColumnsAndLines.class);
    
    static Paint paintColumn1 = new Color(13, 142, 250);
    static Paint paintColumn2 = new Color(68, 114, 196);
    static Paint paintColumn3 = new Color(255, 192, 0);
    static Paint paintColumn4 = new Color(30, 113, 69);
    static Paint paintLine1 = new Color(237, 125, 49);
    static Paint paintLine2 = new Color(195, 45, 46);
    static Paint paintLine3 = new Color(132, 170, 51);
    static Paint paintLine4 = new Color(150, 67, 5);

    /**
     * Thực hiện vẽ biểu đồ dạng đường và cột chung nhau
     *
     * @param listTileColumn : danh sách chú thích biểu đồ cột
     * @param listDataChartColumns : dữ liệu cột
     * @param listTileLine :danh sách chú thích biểu đồ đường
     * @param listDataChartLines : dữ liệu đường
     * @param strNameChart : tên biểu đồ
     * @param strUnitX : thang chia thời gian trục hoành
     * @param strUnitYColumn : đơn vị cho cột: bên trái
     * @param strUnitYLine : đơn vị đường: bên phải
     * @param strPathFolder : đường dẫn folder chứa ảnh
     * @param strNameImg : tên ảnh lưu trữ
     */
    public static void chartColumnsAndLinesDraw(
            List<TitleChartDTO> listTileColumn,
            List<DataChartDTO> listDataChartColumns,
            List<TitleChartDTO> listTileLine,
            List<DataChartDTO> listDataChartLines, String strNameChart,
            String strUnitX, String strUnitYColumn, String strUnitYLine,
            String strPathFolder, String strNameImg, Long maxValue) {

        // gọi tạo biểuđồ
        createChartPanel(listTileColumn, listDataChartColumns, listTileLine,
                listDataChartLines, strNameChart, strUnitX, strUnitYColumn,
                strUnitYLine, strPathFolder, strNameImg, maxValue);
    }

    /**
     * Tao bieu do duong
     *
     * @param listTileColumn : danh sách chú thích biểu đồ cột
     * @param listDataChartColumns : dữ liệu cột
     * @param listTileLine :danh sách chú thích biểu đồ đường
     * @param listDataChartLines : dữ liệu đường
     * @param strNameChart : tên biểu đồ
     * @param strUnitX : thang chia thời gian trục hoành
     * @param strUnitYColumn : đơn vị cho cột: bên trái
     * @param strUnitYLine : đơn vị đường: bên phải
     * @param strPathFolder : đường dẫn folder chứa ảnh
     * @param strNameImg : tên ảnh lưu trữ
     */
    private static void createChartPanel(List<TitleChartDTO> listTileColumn,
            List<DataChartDTO> listDataChartColumns,
            List<TitleChartDTO> listTileLine,
            List<DataChartDTO> listDataChartLines, String strNameChart,
            String strUnitX, String strUnitYColumn, String strUnitYLine,
            String strPathFolder, String strNameImg, Long maxValue) {

        String chartTitle = strNameChart;
        String categoryAxisLabel = strUnitX;
        String valueAxisLabelColumn = strUnitYColumn;
        String valueAxisLabelLine = strUnitYLine;
        CategoryDataset datasetColumns = createDatasetColumn(listTileColumn, listDataChartColumns);
        CategoryDataset datasetLines = createDatasetLine(listTileLine, listDataChartLines);

        final JFreeChart chart;
        if (listTileLine == null) {
            //vẽ biểu đồ chỉ mình cột
            chart = ChartFactory.createBarChart(chartTitle, // chart
                    // title
                    categoryAxisLabel, // domain axis label
                    null, // range axis labelvalueAxisLabelColumn
                    datasetColumns, // dữ liệu đường
                    PlotOrientation.VERTICAL,
                    true, // include legend
                    false, // tooltips?
                    false // URL generator? Not required...
                    );
            customizeChartCoLumn(chart, maxValue);
        } else {
            //vẽ biểu đồ cả đường và cột
            chart = ChartFactory.createBarChart(chartTitle, // chart
                    categoryAxisLabel, // domain axis label
                    valueAxisLabelLine, // range axis label
                    datasetLines, // dữ liệu đường
                    PlotOrientation.VERTICAL, true, // include legend
                    true, // tooltips?
                    false // URL generator? Not required...
                    );
            customizeChart(chart, datasetColumns, valueAxisLabelColumn, maxValue);
        }


        // saves the chart as an image files
        File imageFile = new File(strPathFolder + strNameImg);

        int width = 1392;
        int height = 1200;

        try {
            ChartUtilities.saveChartAsJPEG(imageFile, chart, width, height);
            // artAsPNG(imageFile, chart, width, height);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        // return new ChartPanel(chart);
    }

    /**
     * cấu hình biểu đồ, độ mảnh, mầu biểu đồ
     *
     * @param chart
     */
    private static void customizeChart(JFreeChart chart,
            CategoryDataset datasetColumn, String strTitleUnit1, Long maxValue) {

        String fontPath = CommonUtils.getAppConfigValue("path_urlFont");
        try {
            Font defaultFont = Font.createFont(Font.TRUETYPE_FONT, new File(
                    fontPath + File.separatorChar + "timesbd.ttf"));

            Font font2 = defaultFont.deriveFont(24f);
            Font myFont = defaultFont.deriveFont(20f);
            chart.setBackgroundPaint(Color.white);
            final CategoryPlot plot = chart.getCategoryPlot();
            // sets plot background
            plot.setBackgroundPaint(Color.WHITE);
            // sets paint color for the grid lines
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.BLACK);

            plot.setDomainGridlinesVisible(true);
            plot.setDomainGridlinePaint(Color.BLACK);

            // thiết lập biểu đồ cột
            plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            NumberAxis numberaxis = (NumberAxis) plot.getRangeAxis();
            numberaxis.setNumberFormatOverride(new DecimalFormat("#######.##"));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDrawBarOutline(false);
            // khoảng cách cột
            renderer.setItemMargin(0.06);
            renderer.setMaximumBarWidth(0.1);
            renderer.setShadowVisible(false);

            // đặt hiển thị nhãn số liệu ngay trên đường biểu đồ
			/* StandardCategoryItemLabelGenerator labelGen = new
             StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#######.##"));
             renderer.setBaseItemLabelGenerator(labelGen);
             renderer.setBaseItemLabelsVisible(true);*/

            renderer.setSeriesPaint(0, paintColumn1);
            renderer.setSeriesPaint(1, paintColumn2);
            renderer.setSeriesPaint(2, paintColumn3);
            renderer.setSeriesPaint(3, paintColumn4);
            renderer.setSeriesPaint(4, Color.DARK_GRAY);

            renderer.setDrawBarOutline(false);
            renderer.setBaseOutlinePaint(Color.gray);

            plot.setRenderer(1, renderer);
            plot.setDataset(1, datasetColumn);
            plot.mapDatasetToRangeAxis(1, 1);

            // đặt dữ liệu cột
            final ValueAxis axis2 = new NumberAxis(strTitleUnit1);
            axis2.setTickLabelFont(myFont);
            axis2.setLabelFont(font2);
            plot.setRangeAxis(1, axis2);
            // ======================================================================
            // Thiết lập cài đặt biểu đồ dạng đường
            final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
            renderer2.setSeriesPaint(0, paintLine1);
            renderer2.setSeriesPaint(1, paintLine2);
            renderer2.setSeriesPaint(2, paintLine3);
            renderer2.setSeriesPaint(3, paintLine4);

            // sets thickness for series (using strokes)
            renderer2.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer2.setSeriesStroke(1, new BasicStroke(2.0f));
            renderer2.setSeriesStroke(2, new BasicStroke(2.0f));
            renderer2.setSeriesStroke(3, new BasicStroke(2.5f));
            plot.setRenderer(0, renderer2);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

            // phần tiêu đề biểu đồ
            chart.getTitle().setFont(font2);
            // phan chu thich
            // border phan chu thich bottom

            LegendTitle legend = chart.getLegend();
            legend.setFrame(new BlockBorder(Color.LIGHT_GRAY));
            legend.setMargin(12, 6, 2, 2);
            legend.setItemLabelPadding(new RectangleInsets(4.0, 12.0, 4.0, 12.0));
            legend.setBorder(0D, 0D, 0D, 0D);
            legend.setItemFont(myFont);

            CategoryAxis categoryaxis = plot.getDomainAxis();

            if (plot.getRangeAxisCount() > 1) {
                NumberAxis numberaxis1 = (NumberAxis) plot.getRangeAxis(1);
                numberaxis1.setNumberFormatOverride(new DecimalFormat("#######.##"));
            }


            categoryaxis.setTickLabelFont(myFont);
            numberaxis.setTickLabelFont(myFont);

            categoryaxis.setLabelFont(font2);
            numberaxis.setLabelFont(font2);
            plot.setAxisOffset(new RectangleInsets(1D, 1D, 1D, 1D));
            plot.getRangeAxis().setRangeWithMargins(0, maxValue + 1);

        } catch (FontFormatException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * style cho minh bieu do cột
     *
     * @param chart
     * @param datasetColumn
     * @param strTitleUnit1
     */
    private static void customizeChartCoLumn(JFreeChart chart, Long maxValue) {

        String fontPath = CommonUtils.getAppConfigValue("path_urlFont");
        try {
            Font defaultFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath + File.separatorChar + "timesbd.ttf"));

            Font font2 = defaultFont.deriveFont(28f);
            Font myFont = defaultFont.deriveFont(28f);
            chart.setBackgroundPaint(Color.white);
            final CategoryPlot plot = chart.getCategoryPlot();
            // sets plot background
            plot.setBackgroundPaint(Color.WHITE);
            // sets paint color for the grid lines
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.BLACK);

            plot.setDomainGridlinesVisible(false);
            plot.setDomainGridlinePaint(Color.BLACK);

            // thiết lập biểu đồ cột
            plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            NumberAxis numberaxis = (NumberAxis) plot.getRangeAxis();
            //numberaxis.setNumberFormatOverride(new DecimalFormat("#######.##"));
            numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDrawBarOutline(false);


            //((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());

            // khoảng cách cột
            renderer.setItemMargin(0.06);
            renderer.setMaximumBarWidth(0.1);
            renderer.setShadowVisible(false);
            renderer.setBarPainter(new StandardBarPainter());
            // đặt hiển thị nhãn số liệu ngay trên đường biểu đồ
            StandardCategoryItemLabelGenerator labelGen = new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("#######.##"));

            renderer.setBaseItemLabelGenerator(labelGen);
            renderer.setBaseItemLabelsVisible(false);

            renderer.setSeriesPaint(0, paintColumn1);
            renderer.setSeriesPaint(1, paintColumn2);
            renderer.setSeriesPaint(2, paintColumn3);
            renderer.setSeriesPaint(3, paintColumn4);
            renderer.setSeriesPaint(4, Color.DARK_GRAY);

            renderer.setDrawBarOutline(false);
            renderer.setBaseOutlinePaint(Color.BLACK);

            plot.setRenderer(1, renderer);
            //plot.mapDatasetToRangeAxis(1, 1);
            plot.getRangeAxis().setRange(0, maxValue + 1);
            //final CategoryAxis domainAxis = plot.getDomainAxis();
            //domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

            //plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

            // phần tiêu đề biểu đồ
            chart.getTitle().setFont(font2);

            // phan chu thich
            // border phan chu thich bottom
            LegendTitle legend = chart.getLegend();
            legend.setFrame(new BlockBorder(Color.LIGHT_GRAY));
            legend.setMargin(12, 6, 2, 2);
            legend.setItemLabelPadding(new RectangleInsets(4.0, 12.0, 4.0, 12.0));
            legend.setBorder(0D, 0D, 0D, 0D);
            legend.setItemFont(myFont);
            legend.setVisible(false);


            CategoryAxis categoryaxis = plot.getDomainAxis();
            categoryaxis.setTickLabelFont(myFont);
            numberaxis.setTickLabelFont(myFont);
            categoryaxis.setLabelFont(font2);
            numberaxis.setLabelFont(font2);
            plot.setAxisOffset(new RectangleInsets(1D, 1D, 1D, 1D));

        } catch (FontFormatException e) {
            // TODO Auto-generated catch block
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * tao data set cho thong tin ve bieu do dạng cột
     *
     * @param listTileColumn :danh sach tieu chi vẽ biểu đồ
     * @param listDataChart danh sach du lieu tuong ung voi loai tieu chi ve
     * bieu do
     * @return
     */
    private static CategoryDataset createDatasetColumn(
            List<TitleChartDTO> listTileColumn, List<DataChartDTO> listDataChart) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TitleChartDTO itemTitleLine;
        DataChartDTO itemData;

        // kiem tra trang thai cua danh sach du lieu truyen vao
        if (listTileColumn == null || listTileColumn.size() <= 0
                || listDataChart == null || listDataChart.size() <= 0) {
            // truong hop du lieu bi null hoac khong co
            dataset = new DefaultCategoryDataset();
        } else {
            // truong hop co du lieu, tien hanh set vao data set
            String codeSeries;
            String titleSeries;
//            Double value = 0D;
            // duyet danh sach tieu chi
            for (int i = 0; i < listTileColumn.size(); i++) {
                // thuc hien tach du lieu theo loai bieu do
                itemTitleLine = listTileColumn.get(i);
                titleSeries = itemTitleLine.getTitle();
                codeSeries = itemTitleLine.getCode();
                // gan danh sách dataset cho 1 loai tieu chi
                for (int j = 0; j < listDataChart.size(); j++) {
                    itemData = listDataChart.get(j);
                    // gan du lieu vao tung duong cho bieu do
                    if (codeSeries.equals(itemData.getCode())) {
                        dataset.addValue(itemData.getValue(), titleSeries, itemData.getColumnTitle());
                    }
                }

            }
        }
        return dataset;
    }

    /**
     * tao data set cho thong tin ve bieu do dạng đường
     *
     * @param listTileLine :danh sach tieu chi vẽ biểu đồ
     * @param listDataChart danh sach du lieu tuong ung voi loai tieu chi ve
     * bieu do
     * @return
     */
    private static CategoryDataset createDatasetLine(
            List<TitleChartDTO> listTileLine, List<DataChartDTO> listDataChart) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TitleChartDTO itemTitleLine;
        DataChartDTO itemData;

        // kiem tra trang thai cua danh sach du lieu truyen vao
        if (listTileLine == null || listTileLine.size() <= 0
                || listDataChart == null || listDataChart.size() <= 0) {
            // truong hop du lieu bi null hoac khong co
            dataset = new DefaultCategoryDataset();
        } else {
            // truong hop co du lieu, tien hanh set vao data set
            String codeSeries;
            String titleSeries;
//            Double value = 0D;
            // duyet danh sach tieu chi
            for (int i = 0; i < listTileLine.size(); i++) {
                // thuc hien tach du lieu theo loai bieu do
                itemTitleLine = listTileLine.get(i);
                titleSeries = itemTitleLine.getTitle();
                codeSeries = itemTitleLine.getCode();
                // gan danh sách dataset cho 1 loai tieu chi
                for (int j = 0; j < listDataChart.size(); j++) {
                    itemData = listDataChart.get(j);
                    // gan du lieu vao tung duong cho bieu do
                    if (codeSeries.equals(itemData.getCode())) {
                        dataset.addValue(itemData.getValue(), titleSeries,
                                itemData.getColumnTitle());
                    }
                }

            }
        }
        return dataset;
    }
}
