package resultGraph;

import AI.AIHandler;
import javafx.scene.chart.XYChart;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Joseph on 6/14/2017.
 */
public class ResultGraph extends JPanel {
    org.knowm.xchart.XYChart chart;
    ResultGraph(){
    }

    void Draw(DefaultListModel<dataHelper> listModel){
        org.knowm.xchart.XYChart chart = new XYChartBuilder().width(800).height(600).title(getClass().getSimpleName()).xAxisTitle("Generation").yAxisTitle("Winrate").build();
        chart.setTitle("Performance of Exploration Methods");

        //customize
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.95);




        int size = listModel.size();
        for(int idx=0;idx<size;idx++){
            dataHelper cur = listModel.get(idx);
            ArrayList<Double> data = cur.datalist;

            Double[] dataArrayDouble = data.toArray(new Double[data.size()]);
            double[] epochArray = new double[100];
            double[] dataArray = new double[100];
            for(int x=0;x<100;x++){
                epochArray[x]=x;
                dataArray[x] = dataArrayDouble[x];
            }
            System.out.println(cur.text + Arrays.toString(dataArray));
            chart.addSeries(cur.text,epochArray,dataArray);
        }

        SwingWrapper< org.knowm.xchart.XYChart> wrapper = new SwingWrapper<>(chart);
        wrapper.displayChart();

    }



}
