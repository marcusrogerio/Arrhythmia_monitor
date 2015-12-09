package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;


        import android.annotation.TargetApi;
        import android.app.Activity;
        import android.content.ContentValues;
        import android.content.Context;
        import android.graphics.Color;
        import android.graphics.Paint.Align;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.provider.Settings;
        import android.view.KeyEvent;
        import android.view.SoundEffectConstants;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.FrameLayout;
        import android.widget.TextView;


        import org.achartengine.ChartFactory;
        import org.achartengine.GraphicalView;
        import org.achartengine.chart.PointStyle;
        import org.achartengine.model.TimeSeries;
        import org.achartengine.model.XYMultipleSeriesDataset;
        import org.achartengine.renderer.XYMultipleSeriesRenderer;
        import org.achartengine.renderer.XYSeriesRenderer;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.text.SimpleDateFormat;


public class MainActivity extends Activity {

    private TimeSeries series;
    private TimeSeries eseries = null;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chartview;
    String title = "heart rate";
    private Context context;
    int linecolor = Color.YELLOW;
    //int bgcolor = 0xFF1212FF;
    int bgcolor = Color.BLACK;
    int gridcolor = Color.LTGRAY;
    int othercolor = Color.DKGRAY;
    PointStyle style = PointStyle.SQUARE;
    View xmlchartview;
    //   ZephyrSim zm;
    ZephyrManager zm;
    private long x;
    private int batterylevel;
    public Logque timestamplog;
    boolean plotefficiency = false;
    private int effindex = 0;
    private int[] effeciencyvalues;
    SelfClearingMessage status;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        boolean testmode = false;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        status = new SelfClearingMessage(this);
        timestamplog = new Logque();
// TODO make this setting of x  DRY in eliminating it multiple places
        x = System.currentTimeMillis();

        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0); // turn off auto rotation
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, 1);  // 1 locks to david's NEW prefered orientation
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = getApplicationContext();

       // for Achart engine
        series = new TimeSeries(title);
        eseries = new TimeSeries("Heart Efficiency");
        mDataset = new XYMultipleSeriesDataset();
        setContentView(R.layout.activity_main);
        mDataset.addSeries(series);

        // adds an extra efficiency graph.  Experimental.  Set the boolean "plotefficiency" above to see it
        if (plotefficiency) {
            mDataset.addSeries(eseries);
    effeciencyvalues = new int[]{0,0,0,0,0};
        }



        // This handles the Bluetooth data from the Zephyr XxM BT
        zm = new ZephyrManager(this);

        // adds some random points if we need to test that the graph looks ok
        if (testmode) {
            for (int i = 0; i < 70; i++) {
                updateChart((int) (Math.random() * 1000 + 500));
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

    // the zephry manager will send RR values to this listener.
    // RRs are the milliseconds between beats.  Example, steady 60 BPM heartbeat sends rr values of 1000 because there is a full second between beats
    public void updateChart(int rr) {

        // we throw out any 0s or absurd values.  We have found this tends not to happen with a good chest strap
        if ((rr < 1 ) || (rr > 3000)) return;
        int pulserate = 60000 / rr;
        // ((TextView)(findViewById(R.id.pulseratelable))).setText("" + pulserate);
        series.add(x, pulserate);


        // EXPERIMENTAL FEATURE  used by private client but not expected to make sense for general release.
        // it plots a smoothed heart efficiency graph between the 10 and 50 lines (50=100% 10 = 0 %)
        // I don't expect it to make much sence but the client wanted it and paid for the feature.

        if (plotefficiency)
        {
            int min = 10;
            int max = 50;
            int  y = heartEfficiencyPercentage(pulserate);
            dbg("pulse is " + pulserate + "    efficiency:" + y);
            y  = (int)(min + ((max-min) * (((double)y)/100)));

            effeciencyvalues[effindex] = y;
               effindex++;
               if (effindex>4)
            {
                dbg("ey = " + effeciencyvalues[0] + " " + effeciencyvalues[1] + " " + effeciencyvalues[2] + " " + effeciencyvalues[3] + " " + effeciencyvalues[4] + " "  );
                eseries.add(x, (effeciencyvalues[0] + effeciencyvalues[1] + effeciencyvalues[2] + effeciencyvalues[3] + effeciencyvalues[4]) /5  );

                effindex =0;
            }
        }

        //series.addAnnotation();
        
        x = x + rr;  // time is also the accumulation of RR values

        //Achartengine stuff
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        XYSeriesRenderer erenderer = new XYSeriesRenderer();
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.setXAxisMin(x - 60000);
        mRenderer.setXAxisMax(x);

//        double firstLabel = ((int)((x - 59000)/5000))*5000;
  //      double[] labels = new double[6];
    //    for (int i=0;i<6;i++)
      //  {
        //    labels[i] = firstLabel + i * 5000;

      //  }
  // /     mRenderer.setRange(labels);

        mRenderer.setYAxisMax(110);
 //       mRenderer.setAxisTitleTextSize(20);
        mRenderer.setLabelsTextSize(20);
        mRenderer.setYAxisMin(30);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);

        renderer.setPointStyle(style);
if (plotefficiency) {
    erenderer.setFillPoints(true);
    erenderer.setPointStyle(style);
    erenderer.setLineWidth(2);

}

        mRenderer.setShowLegend(false);
        mRenderer.setChartTitle(title);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(gridcolor);
    //
        mRenderer.setXLabels(11);
        mRenderer.setYLabels(10);



        mRenderer.setAxisTitleTextSize(16);
        mRenderer.setChartTitleTextSize(20);
        mRenderer.setLabelsTextSize(15);
        mRenderer.setLegendTextSize(15);
        mRenderer.setPointSize(5f);
        mRenderer.setMargins(new int[]{20, 30, 15, 0});



        //mRenderer.setXTitle(("pulse rate: " + pulserate + "                           battery: " + batterylevel ));
        //mRenderer.setYTitle("HR");

//        mRenderer.setPointSize(4.0f);
        //  renderer.setAxesColor(axesColor);
        //   renderer.setLabelsColor(labelsColor);
        mRenderer.setYLabelsAlign(Align.RIGHT);
        // mRenderer.setMargins(new int[] {20, 30, 15, 0});
        if (plotefficiency) mDataset.removeSeries(1);
        mDataset.removeSeries(0);

        mDataset.addSeries(series);
        if (plotefficiency) mDataset.addSeries(eseries);

        renderer.setColor(linecolor);
        //mRenderer.setShowLegend(true);
       // mRenderer.setLegendHeight(20);
       // mRenderer.setLegendTextSize(30);
        context = getApplicationContext();
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(bgcolor);
        mRenderer.addSeriesRenderer(renderer);
        if (plotefficiency) mRenderer.addSeriesRenderer(erenderer);

        chartview = ChartFactory.getTimeChartView(context, mDataset, mRenderer, null);
        chartview.invalidate();
        ((TextView)findViewById(R.id.heartratelabel)).setText("HR:" + pulserate);
        ((TextView)findViewById(R.id.batterylabel)).setText("batt:" +  batterylevel);

     //   chartview.setElevation(1);
        chartview.repaint();



     //   View layout = findViewById(R.id.graphcontainer);
        FrameLayout chartlayout = (FrameLayout)findViewById(R.id.chartlayout);
        chartlayout.removeAllViewsInLayout();
    //    chartlayout.addView(chartview);


        chartlayout.addView(chartview, new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));

//        TextView tl = new TextView(chartview.getOverlay());
        //      chartview.getOverlay().add(tl);

        status.checkForClear();    // this needs to be done time to time regularly so statuses are not stuck on.


    }

    public void dbg(String s) {
        System.out.println("lp_dbg   " + s);
    }

    public void displayBattery(int batterylevel) {
   this.batterylevel = batterylevel;

    }

    public void writeRR(View v) {
try{

   // status.setMessage("writing RR values to file");
    SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
    v.playSoundEffect(android.view.SoundEffectConstants.CLICK);

    double starttime = series.getX(0);
    String mydate = (format.format(starttime));

    String FILENAME = "rr_" + mydate + ".txt";
    status.setMessage("writing RR values to file to Documents  " + FILENAME);
    File path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS);
    File file = new File(path, "/" + FILENAME);


  //  File file=  (new File(getFilesDir() + FILENAME));
   // new BufferedWriter(new FileWriter(getFilesDir() + FILENAME));

    dbg(file.getAbsolutePath().toString());
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

    public void writetimestamps(View v) {
        try{
            String FILENAME = "timestamps.txt"   ;

            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "/" + FILENAME);


            //      String FILENAME = "RRs" + date   +  ".txt"   ;
            //dbg(getFilesDir().toString());
            //  File file=  (new File(getFilesDir() + FILENAME));

            // new BufferedWriter(new FileWriter(getFilesDir() + FILENAME));


            dbg(file.getAbsolutePath().toString());
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);


            for (int i = 1;timestamplog.getsize()>1 ;i++)
            {

                String s = timestamplog.getnext() + "\n";

                fos.write(s.getBytes());
            }
            fos.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startZephyr(View v) {
        v.playSoundEffect(SoundEffectConstants.CLICK);
        status.setMessage("Connecting Zephyr");
        x = System.currentTimeMillis();
        zm.connect();

        }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
   // maybe this gets rid of any keys we don't want pressed
        dbg("keydown method called");

return false;
    }
   // protected void onStop() {
   //     super.onStop();  // Always call the superclass method first

        // Save the note's current draft, because the activity is stopping
        // and we want to be sure the current note progress isn't lost.
    //    ContentValues values = new ContentValues();
  //      values.put(NotePad.Notes.COLUMN_NAME_TITLE, getCurrentNoteTitle());
//values.putAll(
//serff


  //      private TimeSeries series;
    //    private TimeSeries eseries = null;
      //  private XYMultipleSeriesDataset mDataset;
//        private long x;
//        private int batterylevel;
//        public Logque timestamplog;
//        boolean plotefficiency = false;
//        private int effindex = 0;
//        private int[] effeciencyvalues;
//        SelfClearingMessage status;

        // values.put(NotePad.Notes.COLUMN_NAME_NOTE, getCurrentNoteText());
      //  values.put(NotePad.Notes.COLUMN_NAME_TITLE, getCurrentNoteTitle());

//        getContentResolver().update(
  //              mUri,    // The URI for the note to update.
    //            values,  // The map of column names and new values to apply to them.
      //          null,    // No SELECT criteria are used.
        //        null     // No WHERE columns are used.
      //  );
   // }
public int heartEfficiencyPercentage(int pulserate)
{

    int baseline = 60;
   if (pulserate <= baseline) return 100;
    int efficiency = (int)  (((double)baseline /(double) pulserate)*100);
    dbg("efficiency calced to  :" + efficiency);
   // if (efficiency<0)  efficiency = 0;
    return efficiency;

}

    public void setTempText(String message) {
        ((TextView) findViewById(R.id.messages)).setText("    " + message);
    }
}
