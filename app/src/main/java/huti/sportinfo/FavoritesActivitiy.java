package huti.sportinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;


public class FavoritesActivitiy extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout ltFavoriten = (LinearLayout) findViewById(R.id.ltFavoriten);

        CheckedChangeListener myListener = new CheckedChangeListener();

        SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();
        Cursor sqlresult = connection.rawQuery("SELECT idfavorit,intaktiv,bezeichnung FROM favoriten ORDER BY bezeichnung", null);

        if (sqlresult.getCount() > 0) {
            while (sqlresult.moveToNext()) {

                String bezeichnung = sqlresult.getString(sqlresult.getColumnIndex("bezeichnung"));
                int idfavorit = sqlresult.getInt(sqlresult.getColumnIndex("idfavorit"));
                int intaktiv = sqlresult.getInt(sqlresult.getColumnIndex("intaktiv"));

                View switchView = getLayoutInflater().inflate(R.layout.item_switch, null);
                Switch mySwitch = (Switch) switchView.findViewById(R.id.swtSwitch);
                mySwitch.setChecked(intaktiv > 0);
                mySwitch.setText(bezeichnung);
                mySwitch.setTag(new ViewTag(idfavorit, bezeichnung));
                mySwitch.setOnCheckedChangeListener(myListener);
                ltFavoriten.addView(switchView);
            }
        } else {
            // Keine Favoriten gefunden

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
