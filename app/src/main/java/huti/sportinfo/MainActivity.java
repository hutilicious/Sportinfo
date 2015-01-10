package huti.sportinfo;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
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
    private TableRow.LayoutParams tlparams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        actionBar.setSubtitle(R.string.txtHomeSubtitle);

        this.updateActionBar();
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
                //new UpdateHelper(this, urltabelle, kennung, inturlart, idfavorit, intsportart, intlast).execute();
                //..neuer Request
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateActionBar() {
        SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();

        String sqlget = "SELECT strftime('%d.%m.%Y', u.datum) AS datum";
        sqlget += " FROM updates AS u";
        sqlget += " ORDER BY datetime(u.datum) DESC LIMIT 0,1";
        Cursor sqlresult = connection.rawQuery(sqlget, null);
        if (sqlresult.getCount() > 0) {
            sqlresult.moveToFirst();
            ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(this.getString(R.string.txtHomeSubtitle) + " | Stand: " + sqlresult.getString(0));
        }

        database.close();
        connection.close();
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
        sqlget += ",s.intheimspiel,s.punkteheim,s.punktegast,g.bezeichnung AS gegnerbez,f.bezeichnung AS favoritenbezeichnung,";
        sqlget += "s.idspiel,strftime('%H:%M', s.datum) AS uhrzeit,";
        sqlget += "date(s.datum) AS datum_original,f.farbe AS favoritenfarbe";
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
                linearLayout.removeView(linearLayout.findViewById(R.id.txtWelcome));
            }
            if (linearLayout.findViewById(R.id.tblUpcomingMatches) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.tblUpcomingMatches));
            }

            // Add a fresh TableLayout
            TableLayout tblUpcomingMatches = new TableLayout(this);
            tblUpcomingMatches.setId(R.id.tblUpcomingMatches);
            tblUpcomingMatches.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tblUpcomingMatches.setColumnStretchable(1, true);


            String datum = "";
            String uhrzeit = "";
            String datumuhrzeit_alt = "";
            String heim = "";
            String gast = "";
            String favoritenfarbe = "";
            int punkteheim = -1;
            int punktegast = -1;
            int intheimspiel = 0;

            //TableLayout tblUpcomingMatches = (TableLayout) findViewById(R.id.tblUpcomingMatches);
            //tblUpcomingMatches.removeAllViewsInLayout();

            int rowcounter = 0;
            int trBackground = Color.WHITE;
            while (sqlresult.moveToNext()) {

                //The home team comes first
                if (sqlresult.getInt(sqlresult.getColumnIndex("intheimspiel")) == 1) {
                    heim = sqlresult.getString(sqlresult.getColumnIndex("favoritenbezeichnung"));
                    gast = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                } else {
                    heim = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                    gast = sqlresult.getString(sqlresult.getColumnIndex("favoritenbezeichnung"));
                }
                intheimspiel = sqlresult.getInt(sqlresult.getColumnIndex("intheimspiel"));
                punkteheim = sqlresult.getInt(sqlresult.getColumnIndex("punkteheim"));
                punktegast = sqlresult.getInt(sqlresult.getColumnIndex("punktegast"));
                favoritenfarbe = sqlresult.getString(sqlresult.getColumnIndex("favoritenfarbe"));

                //Make new TableRowLayout
                tlparams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);


                datum = sqlresult.getString(sqlresult.getColumnIndex("datum"));
                uhrzeit = sqlresult.getString(sqlresult.getColumnIndex("uhrzeit"));
                //--------------------------------------------
                // Zeile mit Datum und Uhrzeit
                //--------------------------------------------
                if (!datumuhrzeit_alt.equals(datum + " " + uhrzeit)) {
                    //Get dayname of currentDate (Datum)
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
                    //Add date Row if needed
                    tblUpcomingMatches.addView(RowDate(datum, uhrzeit, dayname));
                    //reset Rowcounter
                    rowcounter = 0;
                }

                //if there are 2 or more games at same day give every second game another color
                if (rowcounter % 2 == 0) {
                    trBackground = Color.WHITE;
                } else {
                    trBackground = getResources().getColor(R.color.colorMainAccent);
                }

                //--------------------------------------------
                // Zeile mit Heim und Heimpunkte
                //--------------------------------------------

                tblUpcomingMatches.addView(RowGame(heim, intheimspiel > 0, punkteheim, trBackground, favoritenfarbe));

                //--------------------------------------------
                // Zeile mit Gast und Gastpunkte
                //--------------------------------------------

                tblUpcomingMatches.addView(RowGame(gast, intheimspiel == 0, punktegast, trBackground, favoritenfarbe));

                //--------------------------------------------
                // Abschlussarbeiten pro Zeile
                //--------------------------------------------
                datumuhrzeit_alt = datum + " " + uhrzeit;
                rowcounter++;
            }

            linearLayout.addView(tblUpcomingMatches);
        } else {
            // No matches handle
        }
        database.close();
        connection.close();
    }


    private TableRow RowDate(String datum, String uhrzeit, String dayname) {
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(tlparams);

        TextView txtDatum = new TextView(this);
        txtDatum.setText(dayname + " " + datum);
        txtDatum.setTextSize(15);
        txtDatum.setPadding(30, 5, 30, 5);
        txtDatum.setTextColor(getResources().getColor(R.color.colorTextLight));
        tr.addView(txtDatum);

        TextView txtUhrzeit = new TextView(this);
        txtUhrzeit.setText(uhrzeit);
        txtUhrzeit.setGravity(Gravity.RIGHT);
        txtUhrzeit.setTextSize(15);
        txtUhrzeit.setPadding(30, 5, 30, 5);
        txtUhrzeit.setTextColor(getResources().getColor(R.color.colorTextLight));
        tr.addView(txtUhrzeit);
        tr.setBackgroundColor(getResources().getColor(R.color.colorMainRow));
        return tr;
    }

    private TableRow RowGame(String name, boolean bolFavorit, int punkte, int trBackground, String favoritenfarbe) {
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(tlparams);

        TextView txtName = new TextView(this);
        txtName.setText(name);
        txtName.setPadding(30, 5, 30, 5);
        if (bolFavorit) {
            //txtName.setTypeface(null, Typeface.BOLD);
            txtName.setTextColor(Color.parseColor("#" + favoritenfarbe));
        }
        txtName.setTextSize(17);
        tr.addView(txtName);

        TextView txtPunkte = new TextView(this);
        if (punkte >= 0) {
            txtPunkte.setText(punkte);
        } else {
            txtPunkte.setText("-");
        }
        txtPunkte.setGravity(Gravity.RIGHT);
        txtPunkte.setPadding(30, 5, 30, 5);
        txtPunkte.setTextSize(17);
        tr.addView(txtPunkte);
        tr.setBackgroundColor(trBackground);
        return tr;
    }
}


/*
Clicklistner beispiel

TableRow row = (TableRow)findViewById( R.id.row1 );
row.setOnClickListener( new OnClickListener() {
    @Override
    public void onClick( View v ) {
        //Do Stuff
    }
} );*/