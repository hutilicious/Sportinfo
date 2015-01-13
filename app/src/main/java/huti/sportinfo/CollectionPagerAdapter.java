package huti.sportinfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Adrian on 11.01.2015.
 */
public class CollectionPagerAdapter extends FragmentStatePagerAdapter {

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //Hier alle Fragments einfügen und getCount anpassen !!!
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                //Fragement for Android Tab
                return new UpcomingGamesFragment();
            case 1:
                //Fragement for Android Tab
                return new TablesFragment();
        }
        return null;
    }

    @Override

    // Ganz Wichtig  tab anzahl anpassen !!!!!
    public int getCount() {
        return 2;
    }
}
