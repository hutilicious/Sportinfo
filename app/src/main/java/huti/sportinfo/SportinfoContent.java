package huti.sportinfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Statische Klasse zur Ausgabe von Inhalt auf einer View des ViewPagers
 */
public class SportinfoContent {

    private static Context context;
    private static View view;
    public static ActionBarActivity activity;
    private static TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);


    /**
     * shows any upcoming games
     */
    public static void showUpcomingGames(Context in_context, View in_view) {

        context = in_context;
        view = in_view;

        SQLiteOpenHelper database = new SqliteHelper(context);
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
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutPager);
            if (linearLayout.findViewById(R.id.txtWelcome) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.txtWelcome));
            }
            if (linearLayout.findViewById(R.id.tblUpcomingMatches) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.tblUpcomingMatches));
            }
            // Add a fresh TableLayout
            TableLayout tblUpcomingMatches = new TableLayout(activity);

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
                    trBackground = activity.getResources().getColor(R.color.colorMainAccent);
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
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutPager);
            if (linearLayout.findViewById(R.id.txtWelcome) == null) {
                TextView txtWelcome = new TextView(activity);
                txtWelcome.setId(R.id.txtWelcome);
                txtWelcome.setText(activity.getString(R.string.txtWelcome));
                txtWelcome.setTextSize(17);
                txtWelcome.setPadding(30, 30, 30, 30);
                linearLayout.addView(txtWelcome);
            }
        }
        database.close();
        connection.close();
    }


    public static void showTables(Context in_context, View in_view) {

        context = in_context;
        view = in_view;

        SQLiteOpenHelper database = new SqliteHelper(context);
        SQLiteDatabase connection = database.getReadableDatabase();
        String sqlget = "SELECT t.idfavorit,t.tabellennr,t.punkte,g.bezeichnung as gegnerbez,t.intfavorit";
        sqlget += " FROM tabellen AS t";
        sqlget += " LEFT JOIN gegner AS g ON t.idmannschaft = g.idgegner";
        sqlget += " ORDER BY t.idfavorit,t.tabellennr";
        Cursor sqlresult = connection.rawQuery(sqlget, null);
        if (sqlresult.getCount() > 0) {

            // Willkommensnachricht kann weg, da wir bereits was in der Datenbank haben
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutPager);
            if (linearLayout.findViewById(R.id.txtWelcome) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.txtWelcome));
            }
            if (linearLayout.findViewById(R.id.tblTables) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.tblTables));
            }
            // Add a fresh TableLayout
            TableLayout tblTables = new TableLayout(activity);
            tblTables.setId(R.id.tblTables);
            tblTables.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tblTables.setColumnStretchable(2, true);

            int idfavorit = 0;
            int idfavorit_alt = 0;
            int intfavorit = 0;
            int tabellennr = 0;
            int punkte = 0;
            String mannschaftname = "";
            String favoritenbezeichnung = "";
            int rowcounter = 0;
            int trBackground = Color.WHITE;

            while (sqlresult.moveToNext()) {
                idfavorit = sqlresult.getInt(sqlresult.getColumnIndex("idfavorit"));
                intfavorit = sqlresult.getInt(sqlresult.getColumnIndex("intfavorit"));
                tabellennr = sqlresult.getInt(sqlresult.getColumnIndex("tabellennr"));
                punkte = sqlresult.getInt(sqlresult.getColumnIndex("punkte"));

                if (idfavorit_alt != idfavorit) {
                    String sqlgetname = "SELECT bezeichnung FROM favoriten AS f";
                    sqlgetname += " WHERE idfavorit=" + idfavorit;
                    Cursor cur_sqlgetname = connection.rawQuery(sqlgetname, null);
                    if (cur_sqlgetname.getCount() > 0) {
                        cur_sqlgetname.moveToFirst();
                        favoritenbezeichnung = cur_sqlgetname.getString(0);
                    } else {
                        favoritenbezeichnung = "error?";
                    }

                    tblTables.addView(RowScoreHeader("Tabelle für : " + favoritenbezeichnung));
                    rowcounter = 0;
                }

                if (intfavorit == 0) {
                    mannschaftname = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                } else {
                    mannschaftname = favoritenbezeichnung;
                }
                if (rowcounter % 2 == 0) {
                    trBackground = Color.WHITE;
                } else {
                    trBackground = activity.getResources().getColor(R.color.colorMainAccent);
                }

                tblTables.addView(RowScore(tabellennr, mannschaftname, punkte, intfavorit > 0, trBackground));

                idfavorit_alt = idfavorit;
                rowcounter++;
            }
            linearLayout.addView(tblTables);
        } else {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutPager);
            if (linearLayout.findViewById(R.id.txtWelcome) == null) {
                TextView txtWelcome = new TextView(activity);
                txtWelcome.setId(R.id.txtWelcome);
                txtWelcome.setText(activity.getString(R.string.txtInfoNoTableData));
                txtWelcome.setTextSize(17);
                txtWelcome.setPadding(30, 30, 30, 30);
                linearLayout.addView(txtWelcome);
            }
        }
    }


    private static TableRow RowDate(String datum, String uhrzeit, String dayname) {
        TableRow tr = new TableRow(activity);
        tr.setLayoutParams(tlparams);

        TextView txtDatum = new TextView(activity);
        txtDatum.setText(dayname + " " + datum);
        txtDatum.setTextSize(15);
        txtDatum.setPadding(30, 5, 30, 5);
        txtDatum.setTextColor(activity.getResources().getColor(R.color.colorTextLight));
        tr.addView(txtDatum);

        TextView txtUhrzeit = new TextView(activity);
        txtUhrzeit.setText(uhrzeit);
        txtUhrzeit.setGravity(Gravity.RIGHT);
        txtUhrzeit.setTextSize(15);
        txtUhrzeit.setPadding(30, 5, 30, 5);
        txtUhrzeit.setTextColor(activity.getResources().getColor(R.color.colorTextLight));
        tr.addView(txtUhrzeit);
        tr.setBackgroundColor(activity.getResources().getColor(R.color.colorMainRow));
        return tr;
    }

    private static TableRow RowGame(String name, boolean bolFavorit, int punkte, int trBackground, String favoritenfarbe) {
        TableRow tr = new TableRow(activity);
        tr.setLayoutParams(tlparams);

        TextView txtName = new TextView(activity);
        txtName.setText(name);
        txtName.setPadding(30, 5, 30, 5);
        if (bolFavorit) {
            //txtName.setTypeface(null, Typeface.BOLD);
            txtName.setTextColor(Color.parseColor("#" + favoritenfarbe));
        }
        txtName.setTextSize(17);
        tr.addView(txtName);

        TextView txtPunkte = new TextView(activity);
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


    private static TableRow RowScoreHeader(String titel) {
        TableRow tr = new TableRow(activity);
        tr.setLayoutParams(tlparams);

        TextView txtTitel = new TextView(activity);
        txtTitel.setText(titel);
        txtTitel.setTextSize(15);
        txtTitel.setPadding(30, 5, 30, 5);
        txtTitel.setTextColor(activity.getResources().getColor(R.color.colorTextLight));


        tr.addView(txtTitel);


        tr.setBackgroundColor(activity.getResources().getColor(R.color.colorMainRow));

        TableRow.LayoutParams params = (TableRow.LayoutParams) txtTitel.getLayoutParams();
        params.span = 3;
        txtTitel.setLayoutParams(params); // causes layout update

        return tr;
    }

    private static TableRow RowScore(int tabellennr, String name, int punkte, boolean bolHighlight, int trBackground) {
        TableRow tr = new TableRow(activity);
        tr.setLayoutParams(tlparams);

        TextView txtNummer = new TextView(activity);
        txtNummer.setText(tabellennr + ".");
        txtNummer.setGravity(Gravity.RIGHT);
        txtNummer.setTextSize(17);
        txtNummer.setPadding(30, 5, 30, 5);
        if (bolHighlight) {
            txtNummer.setTypeface(null, Typeface.BOLD);
        }
        tr.addView(txtNummer);

        TextView txtName = new TextView(activity);
        txtName.setText(name);
        txtName.setTextSize(17);
        txtName.setPadding(30, 5, 30, 5);
        if (bolHighlight) {
            txtName.setTypeface(null, Typeface.BOLD);
        }
        tr.addView(txtName);

        TextView txtPunkte = new TextView(activity);
        txtPunkte.setText(Integer.toString(punkte));
        txtPunkte.setTextSize(17);
        txtPunkte.setGravity(Gravity.RIGHT);
        txtPunkte.setPadding(30, 5, 30, 5);
        if (bolHighlight) {
            txtPunkte.setTypeface(null, Typeface.BOLD);
        }
        tr.addView(txtPunkte);


        tr.setBackgroundColor(trBackground);

        return tr;
    }

}
