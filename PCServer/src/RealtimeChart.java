import java.util.LinkedList;
import java.util.List;

//import javafx.scene.chart.XYChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;

import javax.swing.*;

/**
 * Logarithmic Y-Axis
 *
 * <p>
 * Demonstrates the following:
 *
 * <ul>
 * <li>Logarithmic Y-Axis
 * <li>Building a Chart with ChartBuilder
 * <li>Place legend at Inside-NW position
 */
public class RealtimeChart {

    private SwingWrapper<XYChart> swingWrapper;
    private XYChart chart;
    private JFrame frame;

    private String title;// 标题
    private String serieX;// 系列，此处只有一个系列。若存在多组数据，可以设置多个系列
    private String serieY;// 系列，此处只有一个系列。若存在多组数据，可以设置多个系列
    private String serieZ;// 系列，此处只有一个系列。若存在多组数据，可以设置多个系列
    private List<Double> seriesDataX;// 系列的数据
    private List<Double> seriesDataY;// 系列的数据
    private List<Double> seriesDataZ;// 系列的数据
    private int size = 1000;// 最多显示多少数据，默认显示1000个数据

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

//    public String getSeriesName() {
//        return seriesName;
//    }
//
//    public void setSeriesName(String seriesName) {
//        this.seriesName = seriesName;
//    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 实时绘图
     *
     * @param serieX
     * @param serieY
     * @param serieZ
     * @param title
     */
    public RealtimeChart(String title, String serieX, String serieY, String serieZ) {
        super();
        this.serieX = serieX;
        this.serieY = serieY;
        this.serieZ = serieZ;
        this.title = title;
    }

    public RealtimeChart(String title, String serieX, String serieY, String serieZ, int size) {
        super();
        this.title = title;
        this.serieX = serieX;
        this.serieY = serieY;
        this.serieZ = serieZ;
        this.size = size;
    }

    public void plot(double dataX,double dataY,double dataZ) {
        if (seriesDataX == null) {
            seriesDataX = new LinkedList<>();
            seriesDataY = new LinkedList<>();
            seriesDataZ = new LinkedList<>();
        }

        if (seriesDataX.size() == this.size) {
            seriesDataX.clear();
            seriesDataY.clear();
            seriesDataZ.clear();
        }

        seriesDataX.add(dataX);
        seriesDataY.add(dataY);
        seriesDataZ.add(dataZ);

        if (swingWrapper == null) {

            // Create Chart
            chart = new XYChartBuilder().width(600).height(450).theme(ChartTheme.Matlab).title(title).build();
            chart.addSeries(serieX, null, seriesDataX);
            chart.addSeries(serieY, null, seriesDataY);
            chart.addSeries(serieZ, null, seriesDataZ);
            chart.getStyler().setLegendPosition(LegendPosition.OutsideS);// 设置legend的位置为外底部
            chart.getStyler().setLegendLayout(LegendLayout.Horizontal);// 设置legend的排列方式为水平排列

            swingWrapper = new SwingWrapper<XYChart>(chart);
            frame = swingWrapper.displayChart();
//            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);// 防止关闭窗口时退出程序
        } else {

            // Update Chart
            chart.updateXYSeries(serieX, null, seriesDataX, null);
            chart.updateXYSeries(serieY, null, seriesDataY, null);
            chart.updateXYSeries(serieZ, null, seriesDataZ, null);
            swingWrapper.repaintChart();
        }
    }
}