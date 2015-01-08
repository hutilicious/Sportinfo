package huti.sportinfo;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
            String datum_alt = "";
            String heim = "";
            String gast = "";
            int punkteheim = -1;
            int punktegast = -1;
            int intheimspiel = 0;

            TableLayout tblUpcomingMatches = (TableLayout) findViewById(R.id.tblUpcomingMatches);
            tblUpcomingMatches.removeAllViewsInLayout();

            int rowcounter = 0;
            int trBackground = Color.WHITE;
            while (sqlresult.moveToNext()) {
                datum = sqlresult.getString(0);

                if (sqlresult.getInt(3) == 1) {
                    heim = sqlresult.getString(7);
                    gast = sqlresult.getString(6);
                } else {
                    heim = sqlresult.getString(6);
                    gast = sqlresult.getString(7);
                }
                intheimspiel = sqlresult.getInt(3);
                punkteheim = sqlresult.getInt(4);
                punktegast = sqlresult.getInt(5);


                // Grid test
                TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                TableRow tr = new TableRow(this);
                tr.setLayoutParams(tlparams);
                if (!sqlresult.isFirst()) {
                    tr.setPadding(0, 40, 0, 0);
                }

                if (datum_alt != datum) {
                    TextView txtDatum = new TextView(this);
                    txtDatum.setText(datum);
                    tr.addView(txtDatum);

                    TextView txtSpace = new TextView(this);
                    txtSpace.setText("");
                    tr.addView(txtSpace);
                    tblUpcomingMatches.addView(tr);

                    rowcounter = 0;
                }

                if (rowcounter % 2 == 0) {
                    trBackground = Color.WHITE;
                } else {
                    trBackground = Color.LTGRAY;
                }

                tr = new TableRow(this);
                tr.setLayoutParams(tlparams);

                TextView txtHeim = new TextView(this);
                txtHeim.setText(heim);
                if (intheimspiel == 1) {
                    txtHeim.setTypeface(null, Typeface.BOLD);
                }
                tr.addView(txtHeim);

                TextView txtHeimPunkte = new TextView(this);
                if (punkteheim >= 0) {
                    txtHeimPunkte.setText(" " + punkteheim + " ");
                } else {
                    txtHeimPunkte.setText(" - ");
                }
                txtHeimPunkte.setGravity(Gravity.RIGHT);
                tr.addView(txtHeimPunkte);
                tr.setBackgroundColor(trBackground);
                tblUpcomingMatches.addView(tr);

                tr = new TableRow(this);
                tr.setLayoutParams(tlparams);

                TextView txtGast = new TextView(this);
                txtGast.setText(gast);
                if (intheimspiel == 0) {
                    txtGast.setTypeface(null, Typeface.BOLD);
                }
                tr.addView(txtGast);

                TextView txtGastPunkte = new TextView(this);
                if (punkteheim >= 0) {
                    txtGastPunkte.setText(" " + punktegast + " ");
                } else {
                    txtGastPunkte.setText(" - ");
                }
                tr.addView(txtGastPunkte);
                tr.setBackgroundColor(trBackground);
                tblUpcomingMatches.addView(tr);

                datum_alt = datum;
                rowcounter++;
            }
        } else {
            // No matches handle
        }
        database.close();
        connection.close();
    }


}


