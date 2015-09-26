package com.hrvresearch.lowell.Arrhythmia_monitor;

        import android.annotation.TargetApi;
        import android.app.Activity;
        import android.content.Context;
        import android.content.res.Configuration;
        import android.graphics.Color;
        import android.os.Build;
        import android.os.Bundle;
        import android.provider.Settings;
        import android.view.KeyEvent;
        import android.view.MotionEvent;
        import android.view.SoundEffectConstants;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.FrameLayout;
        import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    // SETTINGS THAT WE LIKE TO TWEAK
    boolean plotefficiency = false;
    boolean testmode = false;


    // OBJECTS THAT NEED LONG LIFE
    String title = "";
    private Context context;
    View xmlchartview;
    //   ZephyrSim zm;
    ZephyrManager zm;
    private int batterylevel;
    private int effindex = 0;
    private int[] effeciencyvalues;
    SelfClearingMessage status;
    Mode myMode = Mode.GRAPH;
    private long lastmodechangetime = System.currentTimeMillis();
    LPGraphicalView myGrapher;
    private TextView mybignumber;
    private int lastnumber = -1;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // VOODOO
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Configuration.ORIENTATION_LANDSCAPE);  // 1 locks to david's prefered orientation for tab4
    //    Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Configuration.ORIENTATION_PORTRAIT);  // 1 locks to watch orientation
     //   Settings.System.ACCELEROMETER_ROTATION    ="Rotation OFF";
//        context = getApplicationContext();//
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.graphview);
        setContentView(R.layout.singlebignumberview);

        status = new SelfClearingMessage(this);
        mybignumber = ((TextView)    findViewById(R.id.bignumber));
        myGrapher = new LPGraphicalView(this);
        zm = new ZephyrManager(this);
        if (testmode) {
            for (int i = 0; i < 70; i++) {
                receivenewRR((int) (Math.random() * 1000 + 500));
            }
        }

        setContentView(R.layout.singlebignumberview);

         if (!testmode) zm.connect();
        // at this point we are then just waiting for events(data from Zephyr or clicks from interface)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void receivenewRR(int rr) {
        if ((rr < 1 ) || (rr > 3000)) return;
        int pulserate = 60000 / rr;
        myGrapher.addValue(rr);
        if (myMode==Mode.SINGLENUMBER ) {
            if (rr ==lastnumber)
            {
                // we flash here
            }
            View bignumlayout = findViewById(R.id.bignumber);
            bignumlayout.setBackgroundColor(Color.WHITE);

            // or here we flash every time
                mybignumber.setText("" + pulserate);
            bignumlayout.setBackgroundColor(Color.BLACK);

            lastnumber = rr;
        }
            // if it is the mode, then display it
        if (myMode==Mode.GRAPH )
        {
            setContentView(R.layout.graphview);
            FrameLayout chartlayout = (FrameLayout)findViewById(R.id.graphLayout);
            chartlayout.removeAllViewsInLayout();
            //    chartlayout.addView(chartview);
            chartlayout.addView(myGrapher.getGraph(this.getApplicationContext()), new FrameLayout.LayoutParams
                    (FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
        }
    }
    public void displayBattery(int batterylevel) {
        this.batterylevel = batterylevel;
    }

    public void startZephyr(View v) {
        v.playSoundEffect(SoundEffectConstants.CLICK);
        status.setMessage("Connecting Zephyr");
        zm.connect();
        }
    @Override
    public void onBackPressed() {
    }
   // public boolean onTouchEvent(MotionEvent event)

    public void modechange(View v)
    {
        modechange();
    }

    public void modechange()

    {
        dbg("mode change called");
    // Tapping the screen will cycle between various app modes.  Like single big digit display and graping.
    // reject tap if last change was too recent
    if (lastmodechangetime + 300 > System.currentTimeMillis() ) return;
        myMode = myMode.next();

       // this mode not implemented yet so we are skipping it.
        if (myMode == Mode.TICKERTAPE)myMode.next();


        if (myMode == Mode.SINGLENUMBER)
        {
            setContentView(R.layout.singlebignumberview);
            mybignumber = ((TextView)    findViewById(R.id.bignumber));
           // mybignumber.setTextSize();
            if (testmode)  receivenewRR(1000);
        }
        if (myMode == Mode.GRAPH) {
            dbg("graph mode");
            setContentView(R.layout.graphview);
   //       FrameLayout fl = (FrameLayout) findViewById(R.id.chartview);
   //       fl.removeAllViews();
   //       fl.addView(          );
      if (testmode)  receivenewRR(1000);
        }
        lastmodechangetime = System.currentTimeMillis();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
   // maybe this gets rid of any keys we don't want pressed
    modechange();
    //    dbg("keydown method called");
        return true;
    }
    public void setTempText(String message) {
     //   ((TextView) findViewById(R.id.messages)).setText("    " + message);
    }
    public void dbg(String s)
    {
// commenting out quiets our debug log
 //       Log.d("LP",s);
    }


    @Override
    public void onClick(View v) {
    modechange();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
       modechange();
        return true;
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //write rr values


        myGrapher.writeRRs();

}




}