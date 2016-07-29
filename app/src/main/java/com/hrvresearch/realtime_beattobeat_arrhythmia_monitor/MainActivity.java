package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;

 import android.app.Activity;
        import android.content.Context;
        import android.graphics.Color;
        import android.graphics.Paint.Align;
 import android.net.Uri;
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
 import com.google.android.gms.appindexing.Action;
 import com.google.android.gms.appindexing.AppIndex;
 import com.google.android.gms.common.api.GoogleApiClient;

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
    // 110 is very low but the app was designed for elderly patients at rest.
    int MAX_HEART_RATE = 110;
    int MIN_HEART_RATE = 30;

    private static final float LABELTEXTSIZE = 30 ;

    // user settings to be restored on
    int prexistingRotation ;
    int preexitingScreenSleep;

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
    boolean testmode = false;

    ZephyrManager zm;
    private long x;
    private int batterylevel;
    SelfClearingMessage status;
    String title = "heart rate";
    private Context context;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Test mode quickly generates a batch of fake points for testing the graph
        // future improvement will move this into a test section



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        status = new SelfClearingMessage(this);
        // TODO make this setting of x  DRY in eliminating it multiple places
        x = System.currentTimeMillis();

       setrotation();
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // the zephry manager will send RR values to this listener.
    // RRs are the milliseconds between beats.  Example, steady 60 BPM heartbeat sends rr values of 1000 because there is a full second between beats
    public void updateChart(int rr) {

        // we throw out any 0s or absurd values.  We have found this tends not to happen with a good chest strap
        if ((rr < 1) || (rr > 3000)) return;
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

        mRenderer.setLabelsTextSize(LABELTEXTSIZE);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);

        renderer.setPointStyle(style);
        mRenderer.setShowLegend(false);
        mRenderer.setChartTitle(title);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(gridcolor);
        mRenderer.setXLabels(11);
        mRenderer.setYLabels(5);

        mRenderer.setAxisTitleTextSize(25);
        mRenderer.setChartTitleTextSize(30);
        mRenderer.setLabelsTextSize(20);
        mRenderer.setLegendTextSize(15);
        mRenderer.setPointSize(2f);
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
        ((TextView) findViewById(R.id.heartratelabel)).setText("HR:" + pulserate);

        chartview.repaint();

        FrameLayout chartlayout = (FrameLayout) findViewById(R.id.chartlayout);
        chartlayout.removeAllViewsInLayout();


        chartlayout.addView(chartview, new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));

        status.checkForClear();    // this needs to be done time to time regularly so statuses are not stuck on.


    }

    // method used for debugging.  tagging with lp_dbg makes it easier to find if logcat gets messy
    public void dbg(String s) {

        if (testmode) System.out.println("lp_dbg   " + s);
    }
    public void displayBattery(int batterylevel) {
        this.batterylevel = batterylevel;
        ((TextView) findViewById(R.id.batterylabel)).setText("batt:" + batterylevel);
    }


    // write RR values to a text file kept in the application data directory for possible later use
    public void writeRR(View v) {
        try {

            // status.setMessage("writing RR values to file");
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm");

            // give user feedback the button worked
            v.playSoundEffect(SoundEffectConstants.CLICK);

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

            for (int i = 1; i < series.getItemCount(); i++) {
                int temprr = (int) (series.getX(i) - series.getX(i - 1));
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
  // maybe make this work on a confirmation or several presses near the same time.
        // at this point however, I have a user who wants the app to run without accidentally closing it and this is very critical to him.

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // maybe this gets rid of any keys we don't want pressed
        dbg("keydown method called");

        return false;
    }


    public void setTempText(String message) {
        ((TextView) findViewById(R.id.messages)).setText("    " + message);
    }


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

//        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, prexistingRotation);  // 1 locks to david's NEW prefered orientation

//    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

}


    @Override
    public void onResume()
    {
        super.onResume();
        setrotation();

    }

    private void setrotation() {
// stubbing out.  we are trying to do this in activity manifest now.

        /*
        // Set screen to landscape mode and try to keep device powered on
        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0); // turn off auto rotation

        try {
            prexistingRotation = Settings.System.getInt(this.getContentResolver(), Settings.System.USER_ROTATION);
            preexitingScreenSleep = getWindow().getAttributes().flags;

        } catch (Settings.SettingNotFoundException e) {



            System.out.println("setting not found");
        }
        //     Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, 1);  // 1 locks to david's NEW prefered orientation
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, 0);  // 1 locks to Richards prefered orientation


//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

*/

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hrvresearch.realtime_beattobeat_arrhythmia_monitor/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hrvresearch.realtime_beattobeat_arrhythmia_monitor/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}