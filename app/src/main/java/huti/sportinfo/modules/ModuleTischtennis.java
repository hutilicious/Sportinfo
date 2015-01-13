package huti.sportinfo.modules;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import huti.sportinfo.MainActivity;
import huti.sportinfo.SqliteHelper;

/**
 * Created by Huti on 09.01.2015.
 */
public class ModuleTischtennis {
    private ActionBarActivity activity = null;
    private String url = "";
    private String kennung = "";
    private int inturlart = 0;
    private int idfavorit = 0;
    private int intsportart = 0;
    private int intlast = 0;

    public ModuleTischtennis(ActionBarActivity activity, String url, String kennung, int urlart, int idfavorit, int intsportart, int intlast) {
        this.activity = activity;
        this.url = url;
        this.kennung = kennung;
        this.inturlart = urlart;
        this.idfavorit = idfavorit;
        this.intsportart = intsportart;
        this.intlast = intlast;
    }

    public void getGames(String htmlsource) {
        SQLiteOpenHelper database = new SqliteHelper(this.activity.getApplicationContext());
        SQLiteDatabase connection = database.getWritableDatabase();

        String datum = "";
        String uhrzeit = "";
        int punkteheim = -1;
        int punktegast = -1;
        String heim = "";
        String gast = "";
        boolean bolGameOpen = false;
        int tdcounter = 0;

        String[] split = htmlsource.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (split[i].indexOf("<tr class=\"tth3h\">") >= 0) {
                bolGameOpen = true;
                tdcounter = 0;
            }
            if (bolGameOpen && split[i].indexOf("</tr>") >= 0) {
                // aktuelles Spiel wegspeichern
                if (!heim.trim().equals("")) {
                    int intheimspiel = 0;
                    String strGegner = heim;
                    if (heim.indexOf(kennung) >= 0) {
                        intheimspiel = 1;
                        strGegner = gast;
                    }

                    // Valuepairs for all inserts
                    ContentValues values = new ContentValues();

                    // check ob gegner schon angelegt ist
                    long idgegner;
                    String sqlget = "SELECT idgegner FROM gegner AS g";
                    sqlget += " WHERE idfavorit=" + idfavorit + " AND bezeichnung = '" + strGegner + "'";
                    Cursor sqlresult = connection.rawQuery(sqlget, null);
                    if (sqlresult.getCount() > 0) {
                        sqlresult.moveToFirst();
                        idgegner = sqlresult.getInt(0);
                    } else {
                        values.put("idfavorit", idfavorit);
                        values.put("bezeichnung", strGegner);
                        idgegner = connection.insert("gegner", null, values);
                    }

                    //check ob spiel angelegt ist
                    long idspiel;
                    String sqlgetgame = "SELECT idspiel FROM spiele AS s";
                    sqlgetgame += " WHERE datum='" + datum + "' AND idfavorit=" + idfavorit + " AND idgegner=" + idgegner;
                    Cursor sqlresultgame = connection.rawQuery(sqlgetgame, null);
                    if (sqlresultgame.getCount() > 0) {
                        sqlresultgame.moveToFirst();
                        idspiel = sqlresultgame.getInt(0);
                        // update game score
                        values = new ContentValues();
                        values.put("punkteheim", punkteheim);
                        values.put("punktegast", punktegast);
                        connection.update("spiele", values, "idspiel=" + idspiel, null);
                    } else {
                        values = new ContentValues();
                        values.put("datum", datum);
                        values.put("idfavorit", idfavorit);
                        values.put("idgegner", idgegner);
                        values.put("intheimspiel", intheimspiel);
                        values.put("punkteheim", punkteheim);
                        values.put("punktegast", punktegast);
                        idspiel = connection.insert("spiele", null, values);
                    }
                }
                // Reset
                datum = "";
                punkteheim = -1;
                punktegast = -1;
                heim = "";
                gast = "";
                bolGameOpen = false;
            }

            if (bolGameOpen && tdcounter == 3) {
                String cleanString = Html.fromHtml(split[i]).toString().trim();
                if (cleanString.length() == 0) {
                    bolGameOpen = false;
                }
                else {
                    cleanString = cleanString.substring(3);

                    SimpleDateFormat inFormat = new SimpleDateFormat("dd.MM.yy");
                    SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = inFormat.parse(cleanString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    datum = outFormat.format(date);
                }
            }
            if (bolGameOpen && tdcounter == 4) {
                uhrzeit = Html.fromHtml(split[i]).toString().trim().replace(".", ":");
                datum = datum + " " + uhrzeit;
            }
            if (bolGameOpen && tdcounter == 5) {
                String cleanString = Html.fromHtml(split[i]).toString().trim();

                String[] arrMannschaften = cleanString.split(" - ");
                heim = arrMannschaften[0].trim();
                gast = arrMannschaften[1].trim();
            }
            if (bolGameOpen && tdcounter == 6) {

                String cleanString = Html.fromHtml(split[i]).toString().trim();
                try {
                    punkteheim = Integer.parseInt(cleanString.substring(0, 1));
                } catch (StringIndexOutOfBoundsException e) {
                    punkteheim = -1;
                } catch (NumberFormatException e) {
                    punkteheim = -1;
                }
                try {
                    punktegast = Integer.parseInt(cleanString.substring(2, 3));
                } catch (StringIndexOutOfBoundsException e) {
                    punktegast = -1;
                } catch (NumberFormatException e) {
                    punktegast = -1;
                }
            }

            if (bolGameOpen) {
                tdcounter++;
            }


        }
    }

    public void getTable(String htmlsource) {
        SQLiteOpenHelper database = new SqliteHelper(this.activity.getApplicationContext());
        SQLiteDatabase connection = database.getWritableDatabase();

        String mannschaftname = "";
        int punkte = 0;
        int tabellennr = 0;
        int intfavorit = 0;
        boolean bolTableOpen = false;
        boolean bolRowOpen = false;
        int tdcounter = 0;

        String[] split = htmlsource.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (split[i].indexOf("table border=\"0\" align=\"center\" cellpadding=\"1\" cellspacing=\"2\" class=\"tth0\"") >= 0) {
                // Platzierung
                bolTableOpen = true;
            }
            if (bolTableOpen && split[i].indexOf("tr class=\"tth3") >= 0)
            {
                bolRowOpen = true;
            }
            if (bolTableOpen && split[i].indexOf("</table>") >= 0)
            {
                bolTableOpen = false;
            }
            if (bolTableOpen && bolRowOpen && split[i].indexOf("</tr>") >= 0 && !mannschaftname.trim().equals(""))
            {
                // Speichern
                // Valuepairs for all inserts
                ContentValues values = new ContentValues();

                // haben wir unsere eigene Mannschaft?
                long idmannschaft = 0;
                if (mannschaftname.indexOf(kennung) >= 0) {
                    intfavorit = 1;
                } else {
                    // check ob gegner schon angelegt ist
                    String sqlget = "SELECT idgegner FROM gegner AS g";
                    sqlget += " WHERE idfavorit=" + idfavorit + " AND bezeichnung = '" + mannschaftname + "'";
                    Cursor sqlresult = connection.rawQuery(sqlget, null);
                    if (sqlresult.getCount() > 0) {
                        sqlresult.moveToFirst();
                        idmannschaft = sqlresult.getInt(0);
                    } else {
                        values.put("idfavorit", idfavorit);
                        values.put("bezeichnung", mannschaftname);
                        idmannschaft = connection.insert("gegner", null, values);
                    }
                }

                //check ob tabelle angelegt ist
                long idtabelle;
                String sqlgettablerow = "SELECT idtabelle FROM tabellen AS t";
                sqlgettablerow += " WHERE intfavorit=" + intfavorit + " AND idfavorit=" + idfavorit + " AND idmannschaft=" + idmannschaft;
                Cursor sqlresultgame = connection.rawQuery(sqlgettablerow, null);
                if (sqlresultgame.getCount() > 0) {
                    sqlresultgame.moveToFirst();
                    idtabelle = sqlresultgame.getInt(0);
                    // update table number and score
                    values = new ContentValues();
                    values.put("punkte", punkte);
                    values.put("tabellennr", tabellennr);
                    connection.update("tabellen", values, "idtabelle=" + idtabelle, null);
                } else {
                    values = new ContentValues();
                    values.put("idfavorit", idfavorit);
                    values.put("intfavorit", intfavorit);
                    values.put("idmannschaft", idmannschaft);
                    values.put("punkte", punkte);
                    values.put("tabellennr", tabellennr);
                    idtabelle = connection.insert("tabellen", null, values);
                    //Log.d("SPORTINFO", "INSERT Spiel "+idspiel);
                }

                mannschaftname = "";
                punkte = 0;
                tabellennr = 0;
                intfavorit = 0;
                bolRowOpen = false;
                tdcounter = 0;
            }

            if (bolTableOpen && bolRowOpen && tdcounter == 1)
            {
                tabellennr = Integer.parseInt(Html.fromHtml(split[i]).toString().trim());
            }
            if (bolTableOpen && bolRowOpen && tdcounter == 2)
            {
                mannschaftname = Html.fromHtml(split[i]).toString().trim().substring(4);
            }
            if (bolTableOpen && bolRowOpen && tdcounter == 11)
            {
                punkte = Integer.parseInt(Html.fromHtml(split[i]).toString().trim().split(":")[0]);
            }

            if (bolTableOpen && bolRowOpen)
            {
                tdcounter ++;
            }
        }
        database.close();
        connection.close();
    }
}
