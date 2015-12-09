package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;

 import android.app.Activity;
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

        // For Graphing
        import org.achartengine.ChartFactory;
        import org.achartengine.GraphicalView;
        import org.achartengine.chart.PointStyle;
        import org.achartengine.model.TimeSeries;
        import org.achartengine.model.XYMultipleSeriesDataset;
        import org.achartengine.renderer.XYMultipleSeriesRenderer;
        import org.achartengine.renderer.XYSeriesRenderer;

        // for writing RR values
        import java.io.File;
        import java.io.FileOutputStream;
        import java.text.SimpleDateFormat;


public class MainActivity extends Activity {


    // graph max and min set for use by our elderly patient.
    // TODO make max and min heart rate user configurable
    int MAX_HEART_RATE=110;
    int MIN_HEART_RATE = 30;


    // Settings you may wish to change regarding the coloring
    int linecolor = Color.YELLOW;
    int bgcolor = Color.BLACK;
    int gridcolor = Color.LTGRAY;
    PointStyle style = PointStyle.SQUARE;

    // Store our graph data
    private TimeSeries series;
    private TimeSeries eseries = null;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chartview;



    ZephyrManager zm;
    private long x;
    private int batterylevel;
    SelfClearingMessage status;
    String title = "heart rate";
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Test mode quickly generates a batch of fake points for testing the graph
        // future improvement will move this into a test section
        boolean testmode = false;


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);

        status = new SelfClearingMessage(this);
        // TODO make this setting of x  DRY in eliminating it multiple places
        x = System.currentTimeMillis();

        // Set screen to landscape mode and try to keep device powered on
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

        // This handles the Bluetooth data from the Zephyr XxM BT
        zm = new ZephyrManager(this);

        // adds some random points if we need to test that the graph looks ok
        if (testmode) {
            for (int i = 0; i < 70; i++) {
                updateChart((int) (Math.random() * 1000 + 500));
            }
        }
    }

    // the zephry manager will send RR values to this listener.
    // RRs are the milliseconds between beats.  Example, steady 60 BPM heartbeat sends rr values of 1000 because there is a full second between beats
    public void updateChart(int rr) {

        // we throw out any 0s or absurd values.  We have found this tends not to happen with a good chest strap
        if ((rr < 1 ) || (rr > 3000)) return;
        int pulserate = 60000 / rr;
        series.add(x, pulserate);
        x = x + rr;  // time is also the accumulation of RR values

        //Achartengine stuff
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

        // Set the right side to current time
        mRenderer.setXAxisMax(x);

        // set the left side to one minute earlier 60000 miliseconds
        mRenderer.setXAxisMin(x - 60000);

        mRenderer.setYAxisMin(MIN_HEART_RATE);
        mRenderer.setYAxisMax(MAX_HEART_RATE);

        mRenderer.setLabelsTextSize(20);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);

        renderer.setPointStyle(style);
        mRenderer.setShowLegend(false);
        mRenderer.setChartTitle(title);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(gridcolor);
        mRenderer.setXLabels(11);
        mRenderer.setYLabels(10);

        mRenderer.setAxisTitleTextSize(16);
        mRenderer.setChartTitleTextSize(20);
        mRenderer.setLabelsTextSize(15);
        mRenderer.setLegendTextSize(15);
        mRenderer.setPointSize(5f);
        mRenderer.setMargins(new int[]{20, 30, 15, 0});

        mRenderer.setYLabelsAlign(Align.RIGHT);
        mDataset.removeSeries(0);

        mDataset.addSeries(series);

        renderer.setColor(linecolor);
        context = getApplicationContext();
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(bgcolor);
        mRenderer.addSeriesRenderer(renderer);

        chartview = ChartFactory.getTimeChartView(context, mDataset, mRenderer, null);
        chartview.invalidate();
        ((TextView)findViewById(R.id.heartratelabel)).setText("HR:" + pulserate);
        ((TextView)findViewById(R.id.batterylabel)).setText("batt:" +  batterylevel);

        chartview.repaint();

        FrameLayout chartlayout = (FrameLayout)findViewById(R.id.chartlayout);
        chartlayout.removeAllViewsInLayout();


        chartlayout.addView(chartview, new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));

        status.checkForClear();    // this needs to be done time to time regularly so statuses are not stuck on.


    }

    // method used for debugging.  tagging with lp_dbg makes it easier to find if logcat gets messy
    public void dbg(String s) {
        System.out.println("lp_dbg   " + s);
    }

    public void displayBattery(int batterylevel) {
        this.batterylevel = batterylevel;
    }


    // write RR values to a text file kept in the application data directory for possible later use
    public void writeRR(View v) {
        try{

   // status.setMessage("writing RR values to file");
    SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm");

    // give user feedback the button worked
    v.playSoundEffect(android.view.SoundEffectConstants.CLICK);

    double starttime = series.getX(0);
    String mydate = (format.format(starttime));

    String FILENAME = "rr_" + mydate + ".txt";
    status.setMessage("writing RR values to file to Documents  " + FILENAME);
    File path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS);
    File file = new File(path, "/" + FILENAME);

    dbg(file.getAbsolutePath().toString());
    file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);

            for (int i = 1;i<series.getItemCount();i++)
            {int temprr = (int) (series.getX(i)-series.getX(i-1));
            String s = String.valueOf(temprr) + "\n";

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

    // eliminate back button from closing app
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
// TODO have app reset screen orientation back to the way it was when app started


        // Save the note's current draft, because the activity is stopping
        // and we want to be sure the current note progress isn't lost.
    //    ContentValues values = new ContentValues();
  //      values.put(NotePad.Notes.COLUMN_NAME_TITLE, getCurrentNoteTitle());
//values.putAll(

   // }

    public void setTempText(String message) {
        ((TextView) findViewById(R.id.messages)).setText("    " + message);
    }
}
