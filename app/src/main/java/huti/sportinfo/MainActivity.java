package huti.sportinfo;

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
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import SlidingTabs.SlidingTabLayout;


public class MainActivity extends ActionBarActivity {

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

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
        LinearLayout btn = (LinearLayout) findViewById(R.id.txtNavHome);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.START | Gravity.LEFT)) {
                    mDrawerLayout.closeDrawers();
                }
                Toast.makeText(v.getContext(), "Startseite öffnen", Toast.LENGTH_SHORT).show();

                // Update loaded Views
                //mViewPager.getAdapter().notifyDataSetChanged();
            }
        });
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
                    new UpdateHelper(this, mViewPager, urltabelle, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                }
                if (!urlspiele.trim().equals("")) {
                    inturlart = 1; // Spiele werden abgerufen
                    new UpdateHelper(this, mViewPager, urlspiele, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                }

            }
            this.isUpdating = false;
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

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
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
            View view = getLayoutInflater().inflate(R.layout.pager_item,
                    container, false);
            switch (position) {
                case 0:
                    // Inhalt fuer Uebersicht laden
                    SportinfoContent.showUpcomingGames(getApplicationContext(), view);
                    break;
                case 1:
                    // Inhalt fuer Tabellen laden
                    SportinfoContent.showTables(getApplicationContext(), view);
                    break;
                default:
                    break;
            }
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
            int key = 0;
            for (int i = 0; i < views.size(); i++) {
                key = views.keyAt(i);
                View view = views.get(key);
                // Change the content of this view
                switch (key) {
                    case 0:
                        // Inhalt fuer Uebersicht laden
                        SportinfoContent.showUpcomingGames(getApplicationContext(), view);
                        break;
                    case 1:
                        // Inhalt fuer Tabellen laden
                        SportinfoContent.showTables(getApplicationContext(), view);
                        break;
                    default:
                        break;
                }
            }
            super.notifyDataSetChanged();
        }

    }
}
