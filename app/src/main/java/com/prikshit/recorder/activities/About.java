package com.prikshit.recorder.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.prikshit.recorder.R;
import com.prikshit.recorder.main.Information;
import com.prikshit.recorder.main.MyAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * Java file associated with About Activity
 */
public class About extends ActionBarActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // setting the custom toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // for showing back buttons to go back on previous activity
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setting thr recycler view
        recyclerView = (RecyclerView) findViewById(R.id.recycler1);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(this.getData());
        recyclerView.setAdapter(adapter);
        TextView textView = (TextView) findViewById(R.id.versionCode);
        String version = "2.1";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        textView.setText(version);
    }

    /**
     * Developer data which is to be shown in about activity
     * using a recyclerView (equivalent to listView in old APIs)
     * Right now, the data is hard coded,
     * but at later stages, the data should be taken from a separate file
     * @return
     */
    public List<Information> getData() {
        List<Information> data = new ArrayList<>();
        int[] pics = {R.drawable.prikshit_pic, R.drawable.prikshit_pic, R.drawable.prikshit_pic};
        String[] names = {"Prikshit Kumar", "Pankaj Kumar", "Parmeet Singh"};
        String[] emails = {"kprikshit@iitrpr.ac.in", "pankajkmr@iitrpr.ac.in", "sparmeet@iitrpr.ac.in"};
        for (int i = 0; i < pics.length; i++) {
            Information info = new Information(names[i],emails[i],pics[i]);
            data.add(info);
        }
        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
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
        /**
         * when clicked on Back button
         * go back to home screen
         */
        if (id == R.id.home) {
            overridePendingTransition(R.anim.abc_left_out,R.anim.abc_right_in);
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //overridePendingTransition(R.anim.slideoutleft,R.anim.slideoutleft);
    }
}
