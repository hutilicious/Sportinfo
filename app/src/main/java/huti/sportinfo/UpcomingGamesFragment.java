package huti.sportinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Adrian on 11.01.2015.
 */
public class UpcomingGamesFragment extends Fragment {

    private View view;
    private TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upcominggames_fragment, container, false);
        showUpcomingGames();
        return view;
    }


    /**
     * shows any upcoming games
     */
    public void showUpcomingGames() {

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //String currentDate = sdf.format(new Date());

        SQLiteOpenHelper database = new SqliteHelper(getActivity().getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();
        String sqlget = "SELECT strftime('%d.%m.%Y', s.datum) AS datum,s.idfavorit,s.idgegner";
        sqlget += ",s.intheimspiel,s.punkteheim,s.punktegast,g.bezeichnung AS gegnerbez,f.bezeichnung AS favoritenbezeichnung,";
        sqlget += "s.idspiel,strftime('%H:%M', s.datum) AS uhrzeit,";
        sqlget += "date(s.datum) AS datum_original,f.farbe AS favoritenfarbe";
        sqlget += " FROM spiele AS s";
        sqlget += " INNER JOIN gegner AS g ON s.idgegner = g.idgegner";
        sqlget += " INNER JOIN favoriten AS f ON f.idfavorit = s.idfavorit";

        // Lese nur letztes Ergebnis und nächstes kommendes Spiel
        sqlget += " WHERE s.idspiel IN(SELECT idspiel FROM spiele WHERE idfavorit=s.idfavorit AND punkteheim >= 0 ORDER BY datum DESC LIMIT 0,1)";
        sqlget += " OR s.idspiel IN(SELECT idspiel FROM spiele WHERE idfavorit=s.idfavorit AND punkteheim < 0 ORDER BY datum ASC LIMIT 0,1)";

        sqlget += " ORDER BY datetime(s.datum),s.idfavorit";
        Cursor sqlresult = connection.rawQuery(sqlget, null);

        if (sqlresult.getCount() > 0) {

            // Willkommensnachricht kann weg, da wir bereits was in der Datenbank haben
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout =(LinearLayout) view.findViewById(R.id.layoutWelcome);
            if (linearLayout.findViewById(R.id.txtWelcome) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.txtWelcome));
            }
            // Add a fresh TableLayout
            TableLayout tblUpcomingMatches = new TableLayout(getActivity());

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
            //do nothing
        }
        database.close();
        connection.close();
    }

    private TableRow RowDate(String datum, String uhrzeit, String dayname) {
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(tlparams);

        TextView txtDatum = new TextView(getActivity());
        txtDatum.setText(dayname + " " + datum);
        txtDatum.setTextSize(15);
        txtDatum.setPadding(30, 5, 30, 5);
        txtDatum.setTextColor(getResources().getColor(R.color.colorTextLight));
        tr.addView(txtDatum);

        TextView txtUhrzeit = new TextView(getActivity());
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
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(tlparams);

        TextView txtName = new TextView(getActivity());
        txtName.setText(name);
        txtName.setPadding(30, 5, 30, 5);
        if (bolFavorit) {
            //txtName.setTypeface(null, Typeface.BOLD);
            txtName.setTextColor(Color.parseColor("#" + favoritenfarbe));
        }
        txtName.setTextSize(17);
        tr.addView(txtName);

        TextView txtPunkte = new TextView(getActivity());
        if (punkte >= 0) {
            txtPunkte.setText(Integer.toString(punkte));
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
