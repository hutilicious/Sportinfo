package huti.sportinfo;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;



public class MainActivity extends ActionBarActivity {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    CollectionPagerAdapter myCollectionPagerAdapter;
    ViewPager mViewPager;


    public boolean isUpdating = false; // indicates whether the app data is being updated
    private TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ActionBar actionBar = getSupportActionBar();

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        myCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
        mViewPager.setAdapter(myCollectionPagerAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_name);
        actionBar.setSubtitle(R.string.txtHomeSubtitle);
        ActionBar.TabListener tabListener = new SupportTabListener();

        // Hier neue Tabs einfügen

        actionBar.addTab(actionBar.newTab().setText("UpcomingGames").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Tables").setTabListener(tabListener));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //user presses update button
        if (id == R.id.action_update && !this.isUpdating) {
            Toast.makeText(getApplicationContext(), R.string.txtActionUpdateStart, Toast.LENGTH_SHORT).show();

            SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
            SQLiteDatabase connection = database.getReadableDatabase();
            Cursor sqlresult = connection.rawQuery("SELECT urlspiele,urltabelle,kennung,idfavorit,intsportart FROM favoriten", null);
            int idfavorit = 0;
            int intsportart = 0;
            String urlspiele = "";
            String urltabelle = "";
            String kennung = "";
            int intlast = 0;
            int inturlart = 0;
            while (sqlresult.moveToNext()) {
                this.isUpdating = true;
                urlspiele = sqlresult.getString(0);
                urltabelle = sqlresult.getString(1);
                kennung = sqlresult.getString(2);
                idfavorit = sqlresult.getInt(3);
                intsportart = sqlresult.getInt(4);
                if (sqlresult.isLast()) {
                    intlast = 1;
                }
                if (!urltabelle.trim().equals("")) {
                    inturlart = 0; // Tabelle wird abgerufen
                    new UpdateHelper(this, urltabelle, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                }
                if (!urlspiele.trim().equals("")) {
                    inturlart = 1; // Spiele werden abgerufen
                    new UpdateHelper(this, urlspiele, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                }

            }
            this.isUpdating = false;
            //this.activity.showUpcomingGames();
            //this.activity.showTables();
            //this.activity.updateActionBar();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class SupportTabListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }

    //NAVDRAWER

}