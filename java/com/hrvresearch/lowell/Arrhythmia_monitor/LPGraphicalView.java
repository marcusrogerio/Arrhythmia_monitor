package com.hrvresearch.lowell.Arrhythmia_monitor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.model.TimeSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import static org.achartengine.ChartFactory.getTimeChartView;

/**
 * Created by lowell on 8/8/15.
 */

public class LPGraphicalView {
    int linecolor = Color.YELLOW;
    //int bgcolor = 0xFF1212FF;
    int bgcolor = Color.BLACK;
    int gridcolor = Color.LTGRAY;
    int othercolor = Color.DKGRAY;
    private long x = -1;
    private TimeSeries series;
    private TimeSeries eseries = null;
    private XYMultipleSeriesDataset mDataset;
    String title = "heart rate";
    PointStyle style = PointStyle.SQUARE;
    private Context context;
    private GraphicalView chartview;
    XYMultipleSeriesRenderer mRenderer;
    MainActivity parent;

    public LPGraphicalView(MainActivity parent) {

        this.parent=parent;
        //     SETTINGS AND INITIALIZING
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        XYSeriesRenderer erenderer = new XYSeriesRenderer();
        mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.setYAxisMax(110);
        //       mRenderer.setAxisTitleTextSize(20);
        mRenderer.setLabelsTextSize(20);
        mRenderer.setYAxisMin(30);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);
        renderer.setPointStyle(style);
        mRenderer.setShowLegend(false);
        mRenderer.setChartTitle(title);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(gridcolor);
        mRenderer.setXLabels(11);
        mRenderer.setYLabels(10);

        mRenderer.setPointSize(2.0f);
        //  renderer.setAxesColor(axesColor);
        //   renderer.setLabelsColor(labelsColor);
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        // mRenderer.setMargins(new int[] {20, 30, 15, 0});
        //     if (plotefficiency) mDataset.removeSeries(1);
        renderer.setColor(linecolor);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(bgcolor);
        mRenderer.addSeriesRenderer(renderer);

        series = new TimeSeries(title);
    eseries = new TimeSeries("Heart Efficiency");
    mDataset = new XYMultipleSeriesDataset();
    mDataset.addSeries(series);

    }

     public void addValue(int rr)
     {
         if (x==-1)  // we set the time when our first value comes in, not at initialization
         {
             x = System.currentTimeMillis();
         }
         x = x + rr;
        int pulserate =  60000 / rr;
         series.add(x, pulserate);

     }
    public GraphicalView getGraph(Context context)
    {
        mRenderer.setXAxisMin(x - 60000);
        mRenderer.setXAxisMax(x);


        mDataset.removeSeries(0);
        mDataset.addSeries(series);
        chartview = getTimeChartView(context, mDataset, mRenderer, null);
        chartview.invalidate();
        chartview.repaint();
        chartview.setClickable(true);
        chartview.setOnClickListener(parent);
        chartview.setOnTouchListener(parent);
        return chartview;
      //  FrameLayout chartlayout = (FrameLayout) context.findViewById(R.id.chartlayout);
      //  chartlayout.removeAllViewsInLayout();
      //  chartlayout.addView(chartview, new FrameLayout.LayoutParams
      //          (FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));


    }


    public void writeRRs() {



        try{

            // status.setMessage("writing RR values to file");
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            //      v.playSoundEffect(android.view.SoundEffectConstants.CLICK);

            double starttime = series.getX(0);
            String mydate = (format.format(starttime));

            String FILENAME = "rr_" + mydate + ".txt";
        //    status.setMessage("writing RR values to file to Documents  " + FILENAME);
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "/" + FILENAME);


            //  File file=  (new File(getFilesDir() + FILENAME));
            // new BufferedWriter(new FileWriter(getFilesDir() + FILENAME));

          //  dbg(file.getAbsolutePath().toString());
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);


            //        File file = new File(Environment.getExternalStoragePublicDirectory(
            //              Environment.DIRECTORY_DOCUMENTS), "RRs.txt");
            //    if (!file.exists()) file.createNewFile();
            //    dbg( file.getPath());
            //      file.toString();
//            OutputStreamWriter out=  new OutputStreamWriter(openFileOutput(file.toString()));

            //out.close();

            //    FileOutputStream out = openFileOutput(file.toString(), Context.MODE_APPEND);
            for (int i = 1;i<series.getItemCount();i++)
            {int temprr = (int) (series.getX(i)-series.getX(i-1));
                // dbg("writing" + temprr);
                String s = String.valueOf(temprr) + "\n";

                fos.write(s.getBytes());
            }
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }





    }
}
