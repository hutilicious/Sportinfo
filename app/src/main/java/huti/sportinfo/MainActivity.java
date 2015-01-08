package huti.sportinfo;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    public boolean isUpdating = false; // indicates whether the app data is being updated

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setIcon(R.drawable.ic_launcher);
        actionBar.setTitle(R.string.app_name);
        actionBar.setSubtitle(R.string.txtHomeSubtitle);
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

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //String currentDate = sdf.format(new Date());

        SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();
        String sqlget = "SELECT strftime('%d.%m.%Y', s.datum) AS datum,s.idfavorit,s.idgegner";
        sqlget += ",s.intheimspiel,s.punkteheim,s.punktegast,g.bezeichnung AS gegnerbez,f.kurzbezeichnung,";
        sqlget += "s.idspiel,strftime('%H:%M', s.datum) AS uhrzeit,";
        sqlget += "date(s.datum) as datum_original";
        sqlget += " FROM spiele AS s";
        sqlget += " INNER JOIN gegner AS g ON s.idgegner = g.idgegner";
        sqlget += " INNER JOIN favoriten AS f ON f.idfavorit = s.idfavorit";
        //sqlget += " WHERE date(s.datum) >= '" + currentDate + "'";
        sqlget += " ORDER BY datetime(s.datum),s.idfavorit";
        Cursor sqlresult = connection.rawQuery(sqlget, null);
        if (sqlresult.getCount() > 0) {
            // Willkommensnachricht kann weg, da wir bereits was in der Datenbank haben
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutContent);
            if (linearLayout.findViewById(R.id.txtWelcome) != null) {
                linearLayout.removeViewAt(0);
            }

            String datum = "";
            String uhrzeit = "";
            String datumuhrzeit_alt = "";
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
                datum = sqlresult.getString(sqlresult.getColumnIndex("datum"));
                uhrzeit = sqlresult.getString(sqlresult.getColumnIndex("uhrzeit"));

                if (sqlresult.getInt(3) == 1) {
                    heim = sqlresult.getString(sqlresult.getColumnIndex("kurzbezeichnung"));
                    gast = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                } else {
                    heim = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                    gast = sqlresult.getString(sqlresult.getColumnIndex("kurzbezeichnung"));
                }
                intheimspiel = sqlresult.getInt(sqlresult.getColumnIndex("intheimspiel"));
                punkteheim = sqlresult.getInt(sqlresult.getColumnIndex("punkteheim"));
                punktegast = sqlresult.getInt(sqlresult.getColumnIndex("punktegast"));


                TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                TableRow tr = new TableRow(this);
                tr.setLayoutParams(tlparams);

                //--------------------------------------------
                // Zeile mit Datum und Uhrzeit
                //--------------------------------------------
                if (!datumuhrzeit_alt.equals(datum + " " + uhrzeit)) {
                    Date date = new Date();
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.GERMANY);
                    String goal = outFormat.format(date);
                    try {
                        date = inFormat.parse(sqlresult.getString(sqlresult.getColumnIndex("datum_original")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String dayname = outFormat.format(date);

                    TextView txtDatum = new TextView(this);
                    txtDatum.setText(dayname+" " + datum);
                    txtDatum.setTextSize(15);
                    txtDatum.setPadding(30, 5, 30, 5);
                    txtDatum.setTextColor(Color.WHITE);
                    tr.addView(txtDatum);

                    TextView txtUhrzeit = new TextView(this);
                    txtUhrzeit.setText(uhrzeit);
                    txtUhrzeit.setGravity(Gravity.RIGHT);
                    txtUhrzeit.setTextSize(15);
                    txtUhrzeit.setPadding(30, 5, 30, 5);
                    txtUhrzeit.setTextColor(Color.WHITE);
                    tr.addView(txtUhrzeit);
                    tr.setBackgroundColor(Color.rgb(76,118,159));
                    tblUpcomingMatches.addView(tr);

                    rowcounter = 0;
                }

                if (rowcounter % 2 == 0) {
                    trBackground = Color.WHITE;
                } else {
                    trBackground = Color.rgb(232, 243, 254);
                }

                //--------------------------------------------
                // Zeile mit Heim und Heimpunkte
                //--------------------------------------------
                tr = new TableRow(this);
                tr.setLayoutParams(tlparams);

                TextView txtHeim = new TextView(this);
                txtHeim.setText(heim);
                txtHeim.setPadding(30, 5, 30, 5);
                if (intheimspiel == 1) {
                    txtHeim.setTypeface(null, Typeface.BOLD);
                }
                txtHeim.setTextSize(17);
                tr.addView(txtHeim);

                TextView txtHeimPunkte = new TextView(this);
                if (punkteheim >= 0) {
                    txtHeimPunkte.setText(punkteheim);
                } else {
                    txtHeimPunkte.setText("-");
                }
                txtHeimPunkte.setGravity(Gravity.RIGHT);
                txtHeimPunkte.setPadding(30, 5, 30, 5);
                txtHeimPunkte.setTextSize(17);
                tr.addView(txtHeimPunkte);
                tr.setBackgroundColor(trBackground);
                tblUpcomingMatches.addView(tr);

                //--------------------------------------------
                // Zeile mit Gast und Gastpunkte
                //--------------------------------------------
                tr = new TableRow(this);
                tr.setLayoutParams(tlparams);

                TextView txtGast = new TextView(this);
                txtGast.setText(gast);
                txtGast.setPadding(30, 5, 30, 5);
                if (intheimspiel == 0) {
                    txtGast.setTypeface(null, Typeface.BOLD);
                }
                txtGast.setTextSize(17);
                tr.addView(txtGast);

                TextView txtGastPunkte = new TextView(this);
                if (punkteheim >= 0) {
                    txtGastPunkte.setText(punktegast);
                } else {
                    txtGastPunkte.setText("-");
                }
                txtGastPunkte.setGravity(Gravity.RIGHT);
                txtGastPunkte.setPadding(30, 5, 30, 5);
                txtGastPunkte.setTextSize(17);
                tr.addView(txtGastPunkte);
                tr.setBackgroundColor(trBackground);
                tblUpcomingMatches.addView(tr);

                //--------------------------------------------
                // Abschlussarbeiten pro Zeile
                //--------------------------------------------
                datumuhrzeit_alt = datum + " " + uhrzeit;
                rowcounter++;
            }
        } else {
            // No matches handle
        }
        database.close();
        connection.close();
    }
}


