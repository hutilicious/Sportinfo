package huti.sportinfo;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public boolean isUpdating = false; // indicates whether the app data is being updated

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.showUpcomingGames();
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

                inturlart = 0; // Spiele werden abgerufen
                new UpdateHelper(this, urlspiele, kennung, inturlart, idfavorit, intsportart, intlast).execute();

                //inturlart = 1; // Tabelle wird abgerufen
                //..neuer Request
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * shows any upcoming games
     */
    public void showUpcomingGames() {
        TextView out = (TextView) findViewById(R.id.txtUpcomingGames);
        out.setText("");
        SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();
        String sqlget = "SELECT strftime('%d.%m.%Y %H:%M', s.datum),s.idfavorit,s.idgegner";
        sqlget += ",s.intheimspiel,s.punkteheim,s.punktegast,g.bezeichnung,f.kurzbezeichnung,s.idspiel";
        sqlget += " FROM spiele AS s";
        sqlget += " INNER JOIN gegner AS g ON s.idgegner = g.idgegner";
        sqlget += " INNER JOIN favoriten AS f ON f.idfavorit = s.idfavorit";
        sqlget += " ORDER BY datetime(s.datum)";
        Cursor sqlresult = connection.rawQuery(sqlget, null);
        if (sqlresult.getCount() > 0) {

            // Willkommensnachricht kann weg, da wir bereits was in der Datenbank haben
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutContent);
            if (linearLayout.findViewById(R.id.txtWelcome) != null) {
                linearLayout.removeViewAt(0);
            }

            String datum = "";
            String heim = "";
            String gast = "";
            int punkteheim = -1;
            int punktegast = -1;
            String appendString = "";
            while (sqlresult.moveToNext()) {
                datum = sqlresult.getString(0);

                if (sqlresult.getInt(3) == 1) {
                    heim = sqlresult.getString(7);
                    gast = sqlresult.getString(6);
                } else {
                    heim = sqlresult.getString(6);
                    gast = sqlresult.getString(7);
                }
                punkteheim = sqlresult.getInt(4);
                punktegast = sqlresult.getInt(5);

                appendString = sqlresult.getInt(8) + "---" + datum + "\n" + heim + " - " + gast;
                if (punkteheim >= 0) {
                    appendString += " " + punkteheim + ":" + punktegast;
                } else {
                    appendString += " -:-";
                }
                appendString += "\n\n";
                out.append(appendString);
            }
        } else {
            out.setText(R.string.txtNoUpcomingMatches);
        }
        database.close();
        connection.close();
    }


}


