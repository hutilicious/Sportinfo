package huti.sportinfo;

import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Huti on 08.02.2015.
 */
public class ContentHelper extends AsyncTask<String, String, String> {
    SparseArray<View> views;
    LinearLayout target;
    int target_key;

    final int KEY_GAMES_CURRENT = 0;
    final int KEY_TABLES = 1;
    final int KEY_GAMES_UPCOMING = 2;
    final int KEY_GAMES_SCORES = 3;

    public ContentHelper(LinearLayout layout_in, int target_key) {
        this.target = layout_in;
        this.target_key = target_key;
    }

    @Override
    protected String doInBackground(String... params) {
        if (target_key == KEY_GAMES_CURRENT) {
            views = SportinfoContent.getGames("current");
        }
        else if (target_key == KEY_GAMES_UPCOMING)
        {
            views = SportinfoContent.getGames("upcoming");
        }
        else if (target_key == KEY_GAMES_SCORES)
        {
            views = SportinfoContent.getGames("scores");
        }
        else if (target_key == KEY_TABLES)
        {
            views = SportinfoContent.getTables();
        }
        else
        {
            views = new SparseArray<View>();
        }
        return "";
    }

    protected void onPostExecute(String result) {
        // do something with views
        target.removeAllViews();
        for (int i = 0; i < views.size(); i++) {
            target.addView(views.get(i));
        }
    }
}
