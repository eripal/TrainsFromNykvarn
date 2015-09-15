package se.train.paledal.trainsfromnykvarn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class RetrieveTrainsData extends ActionBarActivity {

    PendingIntent pi;
    BroadcastReceiver br;
    AlarmManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_trains);
        final List<TextView> tvl = new ArrayList<TextView>();
        tvl.add((TextView)findViewById(R.id.row1col1));
        tvl.add((TextView)findViewById(R.id.row1col2));
        tvl.add((TextView)findViewById(R.id.row1col3));
        tvl.add((TextView)findViewById(R.id.row1col4));
        tvl.add((TextView)findViewById(R.id.row2col1));
        tvl.add((TextView)findViewById(R.id.row2col2));
        tvl.add((TextView)findViewById(R.id.row2col3));
        tvl.add((TextView)findViewById(R.id.row2col4));
        tvl.add((TextView)findViewById(R.id.row3col1));
        tvl.add((TextView)findViewById(R.id.row3col2));
        tvl.add((TextView)findViewById(R.id.row3col3));
        tvl.add((TextView) findViewById(R.id.row3col4));
        new CheckTrainsData(tvl).execute();

        Button tv = (Button)findViewById(R.id.button);
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        tv.setText(sdf.format(cal.getTime()));

        final Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckTrainsData(tvl).execute();
                Button tv = (Button)findViewById(R.id.button);
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                tv.setText(sdf.format(cal.getTime()));
            }
        });
        
        setup();

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                1000 * 60, pi);
    }

    private void setup() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                final List<TextView> tvl = new ArrayList<TextView>();
                tvl.add((TextView)findViewById(R.id.row1col1));
                tvl.add((TextView)findViewById(R.id.row1col2));
                tvl.add((TextView)findViewById(R.id.row1col3));
                tvl.add((TextView)findViewById(R.id.row1col4));
                tvl.add((TextView)findViewById(R.id.row2col1));
                tvl.add((TextView)findViewById(R.id.row2col2));
                tvl.add((TextView)findViewById(R.id.row2col3));
                tvl.add((TextView)findViewById(R.id.row2col4));
                tvl.add((TextView)findViewById(R.id.row3col1));
                tvl.add((TextView)findViewById(R.id.row3col2));
                tvl.add((TextView)findViewById(R.id.row3col3));
                tvl.add((TextView) findViewById(R.id.row3col4));
                new CheckTrainsData(tvl).execute();
                Button tv = (Button)findViewById(R.id.button);
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                tv.setText(sdf.format(cal.getTime()));

                try
                {
                    java.util.Date early = sdf.parse("5:00");
                    java.util.Date late = sdf.parse("10:00");
                    if (cal.getTime().after(early) && cal.getTime().before(late))
                        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60 * 5, pi);
                    else
                        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60 * 60 * 19, pi);
                    } catch (ParseException e) {
                        e.printStackTrace();
                }
            }
        };
        registerReceiver(br, new IntentFilter("se.train.paledal.trainsfromnykvarn") );
        pi = PendingIntent.getBroadcast(this, 0, new Intent("se.train.paledal.trainsfromnykvarn"), 0);
        am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_trains, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        am.cancel(pi);
        unregisterReceiver(br);
        super.onDestroy();
    }
}
