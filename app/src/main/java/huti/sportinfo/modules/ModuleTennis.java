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

import huti.sportinfo.SqliteHelper;

/**
 * Created by Huti on 09.01.2015.
 */
public class ModuleTennis {
    private ActionBarActivity activity = null;
    private String url = "";
    private String kennung = "";
    private int inturlart = 0;
    private int idfavorit = 0;
    private int intsportart = 0;
    private int intlast = 0;

    public ModuleTennis(ActionBarActivity activity, String url, String kennung, int urlart, int idfavorit, int intsportart, int intlast) {
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
            if (split[i].indexOf("<th>Spielbericht</th>") >= 0) {
                // Start der Tabelle
                bolGameOpen = true;
            } else if (bolGameOpen && split[i].indexOf("</table>") >= 0) {
                // Ende der Tabelle
                bolGameOpen = false;
                break;
            } else if (bolGameOpen && split[i].indexOf("</tr>") >= 0) {

                // Reset neue Zeile
                tdcounter = 0;
                heim = "";
                gast = "";
                datum = "";
                uhrzeit = "";
                punkteheim = -1;
                punktegast = -1;

            } else if (bolGameOpen && (split[i].indexOf("<td>") >= 0 || split[i].indexOf("<tr>") == -1)) {
                tdcounter++;
                if (tdcounter == 3) {
                    // Datum und Uhrzeit
                    datum = Html.fromHtml(split[i]).toString().trim();

                    SimpleDateFormat inFormat = new SimpleDateFormat("dd.MM.yyy HH:mm");
                    SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date date = null;
                    try {
                        date = inFormat.parse(datum);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    datum = outFormat.format(date);
                } else if (tdcounter == 5) {
                    // heim
                    heim = Html.fromHtml(split[i]).toString().trim();
                } else if (tdcounter == 8) {
                    // gast
                    gast = Html.fromHtml(split[i]).toString().trim();
                } else if (tdcounter == 11) {
                    //Log.d("TENNISSPIEL",heim+" gegen "+gast + " am "+datum);
                    // scores
                    String punktestring = Html.fromHtml(split[i]).toString().trim();
                    if (punktestring.trim() != "" && punktestring.indexOf(":") >= 0)
                    {
                        String[] punktearr = punktestring.split(":");
                        punkteheim = Integer.parseInt(punktearr[0]);
                        punktegast = Integer.parseInt(punktearr[1]);
                    }

                    // speichern
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
            }
        }
        connection.close();
        database.close();
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
            if (split[i].indexOf("<th class=\"center\">Games</th>") >= 0) {
                // Start der Tabelle
                bolTableOpen = true;
                //Log.d("TENNISTABELLE START", split[i]);
            } else if (bolTableOpen && split[i].indexOf("</table>") >= 0) {
                // Ende der Tabelle
                bolTableOpen = false;
                //Log.d("TENNISTABELLE ENDE", split[i]);
                break;
            } else if (bolTableOpen && split[i].indexOf("</tr>") >= 0) {
                // Reset neue Zeile
                tdcounter = 0;
                tabellennr = 0;
                punkte = 0;
                intfavorit = 0;
                //Log.d("TENNISTABELLE ZEILE", split[i]);
            } else if (bolTableOpen && (split[i].indexOf("<td>") >= 0 || split[i].indexOf("<tr>") == -1)) {
                tdcounter++;

                if (tdcounter == 7) {
                    // nummer
                    //Log.d("TENNISTABELLE Rang", split[i]);
                    tabellennr = Integer.parseInt(Html.fromHtml(split[i]).toString().trim());
                }
                else if (tdcounter == 12) {
                    // mannschaft
                    //Log.d("TENNISTABELLE Mannscha", split[i]);
                    mannschaftname = Html.fromHtml(split[i]).toString().trim();
                }
                else if (tdcounter == 22) {
                    // punkte
                    //Log.d("TENNISTABELLE Punkte", split[i]);
                    String punktestring = Html.fromHtml(split[i]).toString().trim();
                    String[] punktesplit = punktestring.split(":");
                    punkte = Integer.parseInt(punktesplit[0]);


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
                }
            }
        }
        connection.close();
        database.close();

    }
}
