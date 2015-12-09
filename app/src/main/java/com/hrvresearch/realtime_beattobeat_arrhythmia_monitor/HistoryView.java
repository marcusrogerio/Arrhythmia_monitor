/*
package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;

import com.hrvresearch.realtime_beattobeat_arrhythmia_monitor.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

*/
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 *//*

public class HistoryView extends Activity {
    */
/*
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     *//*

    private static final boolean AUTO_HIDE = true;

    */
/**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     *//*

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    */
/**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     *//*

    private static final boolean TOGGLE_ON_CLICK = true;

    */
/**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     *//*

    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    */
/**
     * The instance of the {@link SystemUiHider} for this activity.
     *//*

    private SystemUiHider mSystemUiHider;


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
    private long x;





*/
/*    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history_view);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });
*//*

  */
/*      // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }
*//*

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    */
/**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     *//*

    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    */
/**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     *//*

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            boolean testmode = false;
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            super.onCreate(savedInstanceState);


            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);


            x = System.currentTimeMillis();
            Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            //  Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, 90);   // kills device
            Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, 1);  // 1 locks to david's NEW prefered orientation
            //Settings.System.ACCELEROMETER_ROTATION    ="Rotation OFF";

            context = getApplicationContext();
            series = new TimeSeries(title);
            eseries = new TimeSeries("Heart Efficiency");
            mDataset = new XYMultipleSeriesDataset();
            setContentView(R.layout.activity_main);
            mDataset.addSeries(series);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (testmode) {
                for (int i = 0; i < 70; i++) {
                    updateChart((int) (Math.random() * 1000 + 500));
                }


            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void updateChart(int rr) {
            if ((rr < 1 ) || (rr > 3000)) return;

            int pulserate = 60000 / rr;


            series.add(x, pulserate);
            x = x + rr;

            XYSeriesRenderer renderer = new XYSeriesRenderer();
            XYSeriesRenderer erenderer = new XYSeriesRenderer();
            XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
            mRenderer.setXAxisMin(x - 60000);
            mRenderer.setXAxisMax(x);
            // double firstLabel = ((int)((x - 59000)/5000))*5000;
            // double[] labels = new double[6];
            //  for (int i=0;i<6;i++)
            //  {
            //    labels[i] = firstLabel + i * 5000;


//        }
            //      mRenderer.setRange(labels);

            mRenderer.setYAxisMax(110);
            //       mRenderer.setAxisTitleTextSize(20);
            mRenderer.setLabelsTextSize(20);
            mRenderer.setYAxisMin(30);
            renderer.setFillPoints(true);
            renderer.setPointStyle(style);

            mRenderer.setShowLegend(false);
            mRenderer.setChartTitle(title);
            mRenderer.setShowGrid(true);
            mRenderer.setGridColor(gridcolor);

            mRenderer.setXLabels(11);
            mRenderer.setYLabels(10);
            //mRenderer.setXTitle(("pulse rate: " + pulserate + "                           battery: " + batterylevel ));
            //mRenderer.setYTitle("HR");

            mRenderer.setPointSize(2.0f);
            //  renderer.setAxesColor(axesColor);
            //   renderer.setLabelsColor(labelsColor);
            mRenderer.setYLabelsAlign(Paint.Align.RIGHT);
            // mRenderer.setMargins(new int[] {20, 30, 15, 0});
            mDataset.removeSeries(0);

            mDataset.addSeries(series);

            renderer.setColor(linecolor);
            //mRenderer.setShowLegend(true);
            // mRenderer.setLegendHeight(20);
            // mRenderer.setLegendTextSize(30);
            context = getApplicationContext();
            mRenderer.setApplyBackgroundColor(true);
            mRenderer.setBackgroundColor(bgcolor);
            mRenderer.addSeriesRenderer(renderer);
            chartview = ChartFactory.getTimeChartView(context, mDataset, mRenderer, null);
            chartview.invalidate();

            //   chartview.setElevation(1);
            chartview.repaint();
            //   View layout = findViewById(R.id.graphcontainer);
            FrameLayout chartlayout = (FrameLayout)findViewById(R.id.chartlayout);
            chartlayout.removeAllViewsInLayout();
            //    chartlayout.addView(chartview);


            chartlayout.addView(chartview, new FrameLayout.LayoutParams
                    (FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));

        }


        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event)
        {
            // maybe this gets rid of any keys we don't want pressed
            return false;
        }
    }
*/
