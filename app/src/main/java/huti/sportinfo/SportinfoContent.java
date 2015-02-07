package huti.sportinfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
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

    public static ActionBarActivity activity;
    public static Context context;
    private static View view;
    private static TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);


    /**
     * shows any upcoming games
     */
    public static void showGames(Context in_context, View in_view, String mode) {

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


        if (mode.equals("current")) {
            // Lese nur letztes Ergebnis und nächstes kommendes Spiel
            sqlget += " WHERE s.idspiel IN(SELECT idspiel FROM spiele WHERE idfavorit=s.idfavorit AND punkteheim >= 0 ORDER BY datum DESC LIMIT 0,1)";
            sqlget += " OR s.idspiel IN(SELECT idspiel FROM spiele WHERE idfavorit=s.idfavorit AND punkteheim < 0 ORDER BY datum ASC LIMIT 0,1)";
            sqlget += " ORDER BY datetime(s.datum),s.idfavorit";
        } else if (mode.equals("upcoming")) {
            // Lese Alle kommenden Spiele
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = df.format(new Date());
            sqlget += " WHERE date(s.datum) >= '" + currentDate + "'";
            sqlget += " ORDER BY datetime(s.datum),s.idfavorit";
        } else if (mode.equals("scores")) {
            // Alle Ergebnisse
            sqlget += " WHERE s.punkteheim >= 0";
            sqlget += " ORDER BY datetime(s.datum) DESC,s.idfavorit";
        }


        Cursor sqlresult = connection.rawQuery(sqlget, null);

        if (sqlresult.getCount() > 0) {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutPager);
            linearLayout.removeAllViews();

            String datum = "";
            String uhrzeit = "";
            String datum_alt = "";
            String heim = "";
            String gast = "";
            String favoritenfarbe = "";
            int punkteheim = -1;
            int punktegast = -1;
            int intheimspiel = 0;

            int rowcounter = 0;
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
                if (!datum_alt.equals(datum)) {
                    //Get dayname of currentDate (Datum)
                    Date date = new Date();
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.GERMANY);
                    try {
                        date = inFormat.parse(sqlresult.getString(sqlresult.getColumnIndex("datum_original")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String dayname = outFormat.format(date);
                    //Add date Row if needed
                    linearLayout.addView(RowSeparator(dayname + " " + datum));
                    //reset Rowcounter
                    rowcounter = 0;
                }

                //--------------------------------------------
                // Zeile mit Spielinfos
                //--------------------------------------------
                linearLayout.addView(RowGame(uhrzeit, heim, punkteheim, gast, punktegast, rowcounter, intheimspiel));


                //--------------------------------------------
                // Abschlussarbeiten pro Zeile
                //--------------------------------------------
                datum_alt = datum;
                rowcounter++;
            }
        } else {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layoutPager);
            if (linearLayout.findViewById(R.id.txtWelcome) == null) {
                TextView txtWelcome = new TextView(activity);
                txtWelcome.setId(R.id.txtWelcome);
                if (mode.equals("current")) {
                    txtWelcome.setText(activity.getString(R.string.txtWelcome));
                } else {
                    txtWelcome.setText(activity.getString(R.string.txtNoData));
                }

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
            linearLayout.removeAllViews();

            int idfavorit = 0;
            int idfavorit_alt = 0;
            int intfavorit = 0;
            int tabellennr = 0;
            int punkte = 0;
            String mannschaftname = "";
            String favoritenbezeichnung = "";
            int rowcounter = 0;

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

                    linearLayout.addView(RowSeparator(favoritenbezeichnung));
                    rowcounter = 0;
                }

                if (intfavorit == 0) {
                    mannschaftname = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                } else {
                    mannschaftname = favoritenbezeichnung;
                }

                linearLayout.addView(RowScore(tabellennr, mannschaftname, punkte, rowcounter, intfavorit));

                idfavorit_alt = idfavorit;
                rowcounter++;
            }
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


    private static View RowSeparator(String text) {
        View viewDate = activity.getLayoutInflater().inflate(R.layout.item_separator, null);

        TextView txtSeparator = (TextView) viewDate.findViewById(R.id.txtSeparator);
        txtSeparator.setText(text);

        return viewDate;
    }

    private static View RowGame(String uhrzeit, String nameheim, int punkteheim, String namegast, int punktegast, int counter, int intheimspiel) {

        View gameView = activity.getLayoutInflater().inflate(R.layout.item_game, null);

        String[] arrUhrzeit = uhrzeit.split(":");
        TextView txtHours = (TextView) gameView.findViewById(R.id.txtHours);
        txtHours.setText(arrUhrzeit[0]);
        TextView txtMinutes = (TextView) gameView.findViewById(R.id.txtMinutes);
        txtMinutes.setText(arrUhrzeit[1]);


        TextView txtTeamHome = (TextView) gameView.findViewById(R.id.txtTeamHome);
        txtTeamHome.setText(nameheim);

        TextView txtTeamGuest = (TextView) gameView.findViewById(R.id.txtTeamGuest);
        txtTeamGuest.setText(namegast);

        TextView txtScoreHome = (TextView) gameView.findViewById(R.id.txtScoreHome);
        TextView txtScoreGuest = (TextView) gameView.findViewById(R.id.txtScoreGuest);
        if (punkteheim >= 0) {
            txtScoreHome.setText(Integer.toString(punkteheim));
        }
        if (punktegast >= 0) {
            txtScoreGuest.setText(Integer.toString(punktegast));
        }

        LinearLayout ltHour = (LinearLayout) gameView.findViewById(R.id.ltHour);
        LinearLayout ltMinute = (LinearLayout) gameView.findViewById(R.id.ltMinute);
        if (counter % 2 == 0) {
            ltHour.setBackgroundColor(activity.getResources().getColor(R.color.colorMain));
            ltMinute.setBackgroundColor(activity.getResources().getColor(R.color.colorMain));
        } else {
            ltHour.setBackgroundColor(activity.getResources().getColor(R.color.colorIndicatorGray));
            ltMinute.setBackgroundColor(activity.getResources().getColor(R.color.colorIndicatorGray));
        }
        if (intheimspiel > 0)
        {
            txtTeamHome.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackgroundGold));
            txtScoreHome.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackgroundGold));
        }
        else
        {
            txtTeamGuest.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackgroundGold));
            txtScoreGuest.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackgroundGold));
        }

        return gameView;
    }

    private static View RowScore(int tabellennr, String name, int punkte, int counter, int intfavorit) {
        View scoreView = activity.getLayoutInflater().inflate(R.layout.item_score, null);
        TextView txtPlace = (TextView) scoreView.findViewById(R.id.txtPlace);
        txtPlace.setText(Integer.toString(tabellennr));

        TextView txtTeam = (TextView) scoreView.findViewById(R.id.txtTeam);
        txtTeam.setText(name);

        TextView txtScore = (TextView) scoreView.findViewById(R.id.txtScore);
        txtScore.setText(Integer.toString(punkte));

        //Color
        LinearLayout ltIndicator = (LinearLayout) scoreView.findViewById(R.id.ltIndicator);
        LinearLayout ltTeam = (LinearLayout) scoreView.findViewById(R.id.ltTeam);
        LinearLayout ltScore = (LinearLayout) scoreView.findViewById(R.id.ltScore);

        if (intfavorit > 0) {
            ltIndicator.setBackgroundColor(activity.getResources().getColor(R.color.colorIndicatorGold));
            ltTeam.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackgroundGold));
            ltScore.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackgroundGold));
        } else {
            if (counter % 2 == 0) {

                ltIndicator.setBackgroundColor(activity.getResources().getColor(R.color.colorMain));
                ltTeam.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackground1));
                ltScore.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackground1));
            } else {
                ltIndicator.setBackgroundColor(activity.getResources().getColor(R.color.colorIndicatorGray));
                ltTeam.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackground2));
                ltScore.setBackgroundColor(activity.getResources().getColor(R.color.colorRowBackground2));
            }
        }
        return scoreView;
    }


    public static void updateStand() {
        DrawerLayout layout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        SQLiteOpenHelper database = new SqliteHelper(context);
        SQLiteDatabase connection = database.getReadableDatabase();

        TextView txtSlogan = (TextView) layout.findViewById(R.id.txtNavSlogan);


        String sqlget = "SELECT strftime('%d.%m.%Y %H:%M', u.datum) FROM updates AS u";
        sqlget += " ORDER BY datetime(u.datum) DESC LIMIT 0,1";
        Cursor cur_sqlget = connection.rawQuery(sqlget, null);
        if (cur_sqlget.getCount() > 0) {
            cur_sqlget.moveToFirst();
            txtSlogan.setText("Stand: " + cur_sqlget.getString(0));
        } else {
            txtSlogan.setText(R.string.txtSlogan);
        }
    }

}
