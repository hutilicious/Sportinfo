package huti.sportinfo;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import SlidingTabs.SlidingTabLayout;


public class MainActivity extends ActionBarActivity {

    static final int FAVORITES_REQUEST = 1;  // The request code for favorites activity
    static final int SETTINGS_REQUEST = 2;  // The request code for settings activity


    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    SlidingTabLayout mSlidingTabLayout;
    ViewPager mViewPager;


    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SportinfoContent.activity = this;

        Resources res = getResources();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        // initialize Drawer and Tabs
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorMainDark));

        SportinfoContent.updateStand();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(4); // Zahl der gecachten Views/Tabs, verbessert die Performance beim Tabswitch

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        // Verwende einen eigenen Style für die Tabs
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);


        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_color));
        mSlidingTabLayout.setDistributeEvenly(true);
        mViewPager.setAdapter(new MainPages());

        mSlidingTabLayout.setViewPager(mViewPager);


        if (mSlidingTabLayout != null) {
            mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        // Click events for Navigation Drawer

        LinearLayout btnFav = (LinearLayout) findViewById(R.id.txtNavFav);
        btnFav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start FavoritesActivity
                Intent intent = new Intent(getApplicationContext(), FavoritesActivitiy.class);
                startActivityForResult(intent, FAVORITES_REQUEST);

            }
        });

        LinearLayout btnSettings = (LinearLayout) findViewById(R.id.txtNavSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start SettingsActivity
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FAVORITES_REQUEST) {
            // favorites closed and may be changed, refresh data
            mViewPager.getAdapter().notifyDataSetChanged();
        }
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
            SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
            SQLiteDatabase connection = database.getReadableDatabase();
            Cursor sqlresult = connection.rawQuery("SELECT urlspiele,urltabelle,kennung,idfavorit,intsportart FROM favoriten WHERE intaktiv > 0", null);
            if (sqlresult.getCount() > 0) {
                Toast.makeText(getApplicationContext(), R.string.txtActionUpdateStart, Toast.LENGTH_SHORT).show();
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
                        inturlart = Config.URLART_TABELLE; // Tabelle wird abgerufen
                        new UpdateHelper(this, mViewPager, urltabelle, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                    }
                    if (!urlspiele.trim().equals("")) {
                        inturlart = Config.URLART_SPIELE; // Spiele werden abgerufen
                        if (intsportart == Config.SPORTART_FUSSBALL) {
                            // Fussball braucht mehrere Abfragen mit datumvon und datumbis
                            SimpleDateFormat df = new SimpleDateFormat("yyyy");
                            int currentYear = Integer.parseInt(df.format(new Date()));
                            for (int monthnumber = 2; monthnumber < 11; monthnumber++) {
                                String monthstring = String.format("%02d", monthnumber);
                                String urlspieleZiel = urlspiele.replace("{datumvon}", currentYear + "-" + monthstring + "-01");
                                monthstring = String.format("%02d", (monthnumber + 1));
                                urlspieleZiel = urlspieleZiel.replace("{datumbis}", currentYear + "-" + monthstring + "-01");
                                //Log.d("SPORTINFOURL",urlspieleZiel);
                                new UpdateHelper(this, mViewPager, urlspieleZiel, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                            }
                        } else {
                            new UpdateHelper(this, mViewPager, urlspiele, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                        }
                    }

                }
                this.isUpdating = false;
            } else {
                Toast.makeText(getApplicationContext(), R.string.txtActionUpdateError, Toast.LENGTH_SHORT).show();
            }
            connection.close();
            database.close();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START | Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
    class MainPages extends PagerAdapter {

        SparseArray<View> views = new SparseArray<View>();

        private void getPageContent(View view, int key) {
            LinearLayout target_in = (LinearLayout) view.findViewById(R.id.layoutPager);
            new ContentHelper(target_in, key).execute();
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 4;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(android.view.ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p/>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.txtTabUpcomingGames);
                case 1:
                    return getString(R.string.txtTabTables);
                case 2:
                    return getString(R.string.txtTabAllGames);
                case 3:
                    return getString(R.string.txtTabScores);

                default:
                    // No specific name for that tab
                    return "Tab " + (position + 1);
            }
        }

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources
            View view = getLayoutInflater().inflate(R.layout.tab_content,
                    container, false);
            this.getPageContent(view, position);
            // Add the newly created View to the ViewPager
            container.addView(view);

            views.put(position, view);

            // Return the View
            return view;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            views.remove(position);
        }

        @Override
        public void notifyDataSetChanged() {
            int position = 0;
            for (int i = 0; i < views.size(); i++) {
                position = views.keyAt(i);
                View view = views.get(position);
                // Change the content of this view
                this.getPageContent(view, position);
            }
            super.notifyDataSetChanged();
        }

    }
}
