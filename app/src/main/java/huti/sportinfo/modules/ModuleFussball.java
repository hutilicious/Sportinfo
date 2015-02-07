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
public class ModuleFussball {

    private ActionBarActivity activity = null;
    private String url = "";
    private String kennung = "";
    private int inturlart = 0;
    private int idfavorit = 0;
    private int intsportart = 0;
    private int intlast = 0;

    public ModuleFussball(ActionBarActivity activity, String url, String kennung, int urlart, int idfavorit, int intsportart, int intlast) {
        this.activity = activity;
        this.url = url;
        this.kennung = kennung;
        this.inturlart = urlart;
        this.idfavorit = idfavorit;
        this.intsportart = intsportart;
        this.intlast = intlast;
    }

    public void getGames(String htmlsource) {
        //----------------------------------------------------
        // update fussball.de games for e specific team
        //----------------------------------------------------
        SQLiteOpenHelper database = new SqliteHelper(this.activity.getApplicationContext());
        SQLiteDatabase connection = database.getWritableDatabase();

        String datum = "";
        int punkteheim = -1;
        int punktegast = -1;
        String heim = "";
        String gast = "";

        String[] split = htmlsource.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (split[i].indexOf("td class=\"column-date\"") >= 0) {
                // Datum abrufen
                datum = Html.fromHtml(split[i]).toString().trim();
                try {
                    datum = datum.substring(10, 12) + "-" + datum.substring(7, 9) + "-" + datum.substring(4, 6) + " " + datum.substring(15, 20);
                }
                catch(StringIndexOutOfBoundsException e)
                {
                    //Log.d("SPORTINFO","SPIELFREI Problem!");
                    continue;
                }
                SimpleDateFormat inFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
                SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = null;
                try {
                    date = inFormat.parse(datum);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datum = outFormat.format(date);

            } else if (split[i].indexOf("div class=\"club-name\"") >= 0) {
                // Verein speichern
                String cleanString = Html.fromHtml(split[i]).toString().trim();
                if (heim.equals("")) {
                    heim = cleanString;
                } else {
                    gast = cleanString;
                }
            } else if (split[i].indexOf("class=\"score-left\"") >= 0 && !heim.trim().equals("")) {
                // Ergebnis abrufen und Datensatz speichern, wenn noch nicht da
                int intheimspiel = 0;
                String strGegner = heim;
                if (heim.indexOf(kennung) >= 0) {
                    intheimspiel = 1;
                    strGegner = gast;
                }

                // Valuepairs for all inserts
                ContentValues values = new ContentValues();

                // check ob gegner schon angelegt ist
                //Log.d("SPORTINFO","Gegner:"+strGegner);
                long idgegner;
                String sqlget = "SELECT idgegner FROM gegner AS g";
                sqlget += " WHERE idfavorit=" + idfavorit + " AND bezeichnung = '" + strGegner + "'";
                Cursor sqlresult = connection.rawQuery(sqlget, null);
                if (sqlresult.getCount() > 0) {
                    sqlresult.moveToFirst();
                    idgegner = sqlresult.getInt(0);
                    //Log.d("SPORTINFO", "Habe Gegner '" + strGegner + "' gefunden: " + idgegner);
                } else {
                    values.put("idfavorit", idfavorit);
                    values.put("bezeichnung", strGegner);
                    idgegner = connection.insert("gegner", null, values);
                    //Log.d("SPORTINFO", "Lege Gegner '" + strGegner + "' an: " + idgegner);
                }

                // Sonderzeichen von fussball.de ersetzen
                //Log.d("Sportinfo","original: "+split[i]);
                if (split[i].indexOf("data-obfuscation=\"1\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "2");
                    split[i] = split[i].replace("&#xE501;", "0");
                    split[i] = split[i].replace("&#xE502;", "-");
                    split[i] = split[i].replace("&#xE503;", "9");
                    split[i] = split[i].replace("&#xE504;", "4");
                    split[i] = split[i].replace("&#xE505;", "3");
                    split[i] = split[i].replace("&#xE506;", "8");
                    split[i] = split[i].replace("&#xE507;", "0");
                    split[i] = split[i].replace("&#xE508;", "4");
                    split[i] = split[i].replace("&#xE509;", "8");
                    split[i] = split[i].replace("&#xE50A;", "6");
                    split[i] = split[i].replace("&#xE50B;", "8");
                    split[i] = split[i].replace("&#xE50C;", "-");
                    split[i] = split[i].replace("&#xE50D;", "6");
                    split[i] = split[i].replace("&#xE50E;", "1");
                    split[i] = split[i].replace("&#xE50F;", "3");

                    split[i] = split[i].replace("&#xE510;", "3");
                    split[i] = split[i].replace("&#xE511;", "4");
                    split[i] = split[i].replace("&#xE512;", "8");
                    split[i] = split[i].replace("&#xE513;", "0");
                    split[i] = split[i].replace("&#xE514;", "4");
                    split[i] = split[i].replace("&#xE515;", "5");
                    split[i] = split[i].replace("&#xE516;", "5");
                    split[i] = split[i].replace("&#xE517;", "3");
                    split[i] = split[i].replace("&#xE518;", "2");
                    split[i] = split[i].replace("&#xE519;", "2");
                    split[i] = split[i].replace("&#xE51A;", "6");
                    split[i] = split[i].replace("&#xE51B;", "2");
                    split[i] = split[i].replace("&#xE51C;", "-");
                    split[i] = split[i].replace("&#xE51D;", "9");
                    split[i] = split[i].replace("&#xE51E;", "4");
                    split[i] = split[i].replace("&#xE51F;", "7");

                    split[i] = split[i].replace("&#xE520;", "3");
                    split[i] = split[i].replace("&#xE521;", "9");
                    split[i] = split[i].replace("&#xE522;", "7");
                    split[i] = split[i].replace("&#xE523;", "-");
                    split[i] = split[i].replace("&#xE524;", "2");
                    split[i] = split[i].replace("&#xE525;", "6");
                    split[i] = split[i].replace("&#xE526;", "1");
                    split[i] = split[i].replace("&#xE527;", "1");
                    split[i] = split[i].replace("&#xE528;", "7");
                    split[i] = split[i].replace("&#xE529;", "7");
                    split[i] = split[i].replace("&#xE52A;", "0");
                    split[i] = split[i].replace("&#xE52B;", "9");
                    split[i] = split[i].replace("&#xE52C;", "5");
                    split[i] = split[i].replace("&#xE52D;", "6");
                    split[i] = split[i].replace("&#xE52E;", "3");
                    split[i] = split[i].replace("&#xE52F;", "7");

                    split[i] = split[i].replace("&#xE530;", "-");
                    split[i] = split[i].replace("&#xE531;", "0");
                    split[i] = split[i].replace("&#xE532;", "8");
                    split[i] = split[i].replace("&#xE533;", "5");
                    split[i] = split[i].replace("&#xE534;", "6");
                    split[i] = split[i].replace("&#xE535;", "1");
                    split[i] = split[i].replace("&#xE536;", "5");
                    split[i] = split[i].replace("&#xE537;", "8");
                    split[i] = split[i].replace("&#xE538;", "1");
                    split[i] = split[i].replace("&#xE539;", "2");
                    split[i] = split[i].replace("&#xE53A;", "4");
                    split[i] = split[i].replace("&#xE53B;", "9");
                    split[i] = split[i].replace("&#xE53C;", "9");
                    split[i] = split[i].replace("&#xE53D;", "5");
                    split[i] = split[i].replace("&#xE53E;", "1");
                    split[i] = split[i].replace("&#xE53F;", "7");

                    split[i] = split[i].replace("&#xE540;", "-");
                    split[i] = split[i].replace("&#xE541;", "0");
                }
                else if (split[i].indexOf("data-obfuscation=\"2\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "0");
                    split[i] = split[i].replace("&#xE501;", "7");
                    split[i] = split[i].replace("&#xE502;", "-");
                    split[i] = split[i].replace("&#xE503;", "9");
                    split[i] = split[i].replace("&#xE504;", "8");
                    split[i] = split[i].replace("&#xE505;", "7");
                    split[i] = split[i].replace("&#xE506;", "-");
                    split[i] = split[i].replace("&#xE507;", "9");
                    split[i] = split[i].replace("&#xE508;", "5");
                    split[i] = split[i].replace("&#xE509;", "3");
                    split[i] = split[i].replace("&#xE50A;", "9");
                    split[i] = split[i].replace("&#xE50B;", "7");
                    split[i] = split[i].replace("&#xE50C;", "7");
                    split[i] = split[i].replace("&#xE50D;", "5");
                    split[i] = split[i].replace("&#xE50E;", "0");
                    split[i] = split[i].replace("&#xE50F;", "6");

                    split[i] = split[i].replace("&#xE510;", "6");
                    split[i] = split[i].replace("&#xE511;", "9");
                    split[i] = split[i].replace("&#xE512;", "0");
                    split[i] = split[i].replace("&#xE513;", "3");
                    split[i] = split[i].replace("&#xE514;", "1");
                    split[i] = split[i].replace("&#xE515;", "5");
                    split[i] = split[i].replace("&#xE516;", "8");
                    split[i] = split[i].replace("&#xE517;", "6");
                    split[i] = split[i].replace("&#xE518;", "1");
                    split[i] = split[i].replace("&#xE519;", "0");
                    split[i] = split[i].replace("&#xE51A;", "1");
                    split[i] = split[i].replace("&#xE51B;", "6");
                    split[i] = split[i].replace("&#xE51C;", "-");
                    split[i] = split[i].replace("&#xE51D;", "0");
                    split[i] = split[i].replace("&#xE51E;", "1");
                    split[i] = split[i].replace("&#xE51F;", "9");

                    split[i] = split[i].replace("&#xE520;", "7");
                    split[i] = split[i].replace("&#xE521;", "2");
                    split[i] = split[i].replace("&#xE522;", "4");
                    split[i] = split[i].replace("&#xE523;", "1");
                    split[i] = split[i].replace("&#xE524;", "5");
                    split[i] = split[i].replace("&#xE525;", "2");
                    split[i] = split[i].replace("&#xE526;", "3");
                    split[i] = split[i].replace("&#xE527;", "8");
                    split[i] = split[i].replace("&#xE528;", "6");
                    split[i] = split[i].replace("&#xE529;", "6");
                    split[i] = split[i].replace("&#xE52A;", "7");
                    split[i] = split[i].replace("&#xE52B;", "-");
                    split[i] = split[i].replace("&#xE52C;", "4");
                    split[i] = split[i].replace("&#xE52D;", "2");
                    split[i] = split[i].replace("&#xE52E;", "-");
                    split[i] = split[i].replace("&#xE52F;", "4");

                    split[i] = split[i].replace("&#xE530;", "3");
                    split[i] = split[i].replace("&#xE531;", "4");
                    split[i] = split[i].replace("&#xE532;", "0");
                    split[i] = split[i].replace("&#xE533;", "8");
                    split[i] = split[i].replace("&#xE534;", "8");
                    split[i] = split[i].replace("&#xE535;", "2");
                    split[i] = split[i].replace("&#xE536;", "8");
                    split[i] = split[i].replace("&#xE537;", "2");
                    split[i] = split[i].replace("&#xE538;", "-");
                    split[i] = split[i].replace("&#xE539;", "3");
                    split[i] = split[i].replace("&#xE53A;", "2");
                    split[i] = split[i].replace("&#xE53B;", "3");
                    split[i] = split[i].replace("&#xE53C;", "5");
                    split[i] = split[i].replace("&#xE53D;", "1");
                    split[i] = split[i].replace("&#xE53E;", "4");
                    split[i] = split[i].replace("&#xE53F;", "5");

                    split[i] = split[i].replace("&#xE540;", "9");
                    split[i] = split[i].replace("&#xE541;", "4");
                }
                else if (split[i].indexOf("data-obfuscation=\"3\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "9");
                    split[i] = split[i].replace("&#xE501;", "4");
                    split[i] = split[i].replace("&#xE502;", "6");
                    split[i] = split[i].replace("&#xE503;", "3");
                    split[i] = split[i].replace("&#xE504;", "7");
                    split[i] = split[i].replace("&#xE505;", "2");
                    split[i] = split[i].replace("&#xE506;", "6");
                    split[i] = split[i].replace("&#xE507;", "5");
                    split[i] = split[i].replace("&#xE508;", "9");
                    split[i] = split[i].replace("&#xE509;", "5");
                    split[i] = split[i].replace("&#xE50A;", "0");
                    split[i] = split[i].replace("&#xE50B;", "3");
                    split[i] = split[i].replace("&#xE50C;", "6");
                    split[i] = split[i].replace("&#xE50D;", "-");
                    split[i] = split[i].replace("&#xE50E;", "-");
                    split[i] = split[i].replace("&#xE50F;", "-");

                    split[i] = split[i].replace("&#xE510;", "0");
                    split[i] = split[i].replace("&#xE511;", "2");
                    split[i] = split[i].replace("&#xE512;", "5");
                    split[i] = split[i].replace("&#xE513;", "4");
                    split[i] = split[i].replace("&#xE514;", "9");
                    split[i] = split[i].replace("&#xE515;", "-");
                    split[i] = split[i].replace("&#xE516;", "7");
                    split[i] = split[i].replace("&#xE517;", "4");
                    split[i] = split[i].replace("&#xE518;", "4");
                    split[i] = split[i].replace("&#xE519;", "8");
                    split[i] = split[i].replace("&#xE51A;", "8");
                    split[i] = split[i].replace("&#xE51B;", "3");
                    split[i] = split[i].replace("&#xE51C;", "7");
                    split[i] = split[i].replace("&#xE51D;", "1");
                    split[i] = split[i].replace("&#xE51E;", "9");
                    split[i] = split[i].replace("&#xE51F;", "3");

                    split[i] = split[i].replace("&#xE520;", "5");
                    split[i] = split[i].replace("&#xE521;", "0");
                    split[i] = split[i].replace("&#xE522;", "6");
                    split[i] = split[i].replace("&#xE523;", "2");
                    split[i] = split[i].replace("&#xE524;", "0");
                    split[i] = split[i].replace("&#xE525;", "3");
                    split[i] = split[i].replace("&#xE526;", "0");
                    split[i] = split[i].replace("&#xE527;", "8");
                    split[i] = split[i].replace("&#xE528;", "1");
                    split[i] = split[i].replace("&#xE529;", "7");
                    split[i] = split[i].replace("&#xE52A;", "7");
                    split[i] = split[i].replace("&#xE52B;", "2");
                    split[i] = split[i].replace("&#xE52C;", "2");
                    split[i] = split[i].replace("&#xE52D;", "8");
                    split[i] = split[i].replace("&#xE52E;", "4");
                    split[i] = split[i].replace("&#xE52F;", "1");

                    split[i] = split[i].replace("&#xE530;", "3");
                    split[i] = split[i].replace("&#xE531;", "6");
                    split[i] = split[i].replace("&#xE532;", "9");
                    split[i] = split[i].replace("&#xE533;", "6");
                    split[i] = split[i].replace("&#xE534;", "1");
                    split[i] = split[i].replace("&#xE535;", "5");
                    split[i] = split[i].replace("&#xE536;", "8");
                    split[i] = split[i].replace("&#xE537;", "9");
                    split[i] = split[i].replace("&#xE538;", "2");
                    split[i] = split[i].replace("&#xE539;", "1");
                    split[i] = split[i].replace("&#xE53A;", "8");
                    split[i] = split[i].replace("&#xE53B;", "-");
                    split[i] = split[i].replace("&#xE53C;", "0");
                    split[i] = split[i].replace("&#xE53D;", "5");
                    split[i] = split[i].replace("&#xE53E;", "-");
                    split[i] = split[i].replace("&#xE53F;", "7");

                    split[i] = split[i].replace("&#xE540;", "1");
                    split[i] = split[i].replace("&#xE541;", "4");
                }
                else if (split[i].indexOf("data-obfuscation=\"4\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "6");
                    split[i] = split[i].replace("&#xE501;", "-");
                    split[i] = split[i].replace("&#xE502;", "2");
                    split[i] = split[i].replace("&#xE503;", "3");
                    split[i] = split[i].replace("&#xE504;", "5");
                    split[i] = split[i].replace("&#xE505;", "4");
                    split[i] = split[i].replace("&#xE506;", "-");
                    split[i] = split[i].replace("&#xE507;", "9");
                    split[i] = split[i].replace("&#xE508;", "9");
                    split[i] = split[i].replace("&#xE509;", "7");
                    split[i] = split[i].replace("&#xE50A;", "4");
                    split[i] = split[i].replace("&#xE50B;", "9");
                    split[i] = split[i].replace("&#xE50C;", "7");
                    split[i] = split[i].replace("&#xE50D;", "3");
                    split[i] = split[i].replace("&#xE50E;", "4");
                    split[i] = split[i].replace("&#xE50F;", "-");

                    split[i] = split[i].replace("&#xE510;", "8");
                    split[i] = split[i].replace("&#xE511;", "4");
                    split[i] = split[i].replace("&#xE512;", "2");
                    split[i] = split[i].replace("&#xE513;", "0");
                    split[i] = split[i].replace("&#xE514;", "0");
                    split[i] = split[i].replace("&#xE515;", "1");
                    split[i] = split[i].replace("&#xE516;", "8");
                    split[i] = split[i].replace("&#xE517;", "1");
                    split[i] = split[i].replace("&#xE518;", "1");
                    split[i] = split[i].replace("&#xE519;", "5");
                    split[i] = split[i].replace("&#xE51A;", "7");
                    split[i] = split[i].replace("&#xE51B;", "2");
                    split[i] = split[i].replace("&#xE51C;", "9");
                    split[i] = split[i].replace("&#xE51D;", "6");
                    split[i] = split[i].replace("&#xE51E;", "5");
                    split[i] = split[i].replace("&#xE51F;", "-");

                    split[i] = split[i].replace("&#xE520;", "1");
                    split[i] = split[i].replace("&#xE521;", "9");
                    split[i] = split[i].replace("&#xE522;", "7");
                    split[i] = split[i].replace("&#xE523;", "8");
                    split[i] = split[i].replace("&#xE524;", "2");
                    split[i] = split[i].replace("&#xE525;", "3");
                    split[i] = split[i].replace("&#xE526;", "4");
                    split[i] = split[i].replace("&#xE527;", "-");
                    split[i] = split[i].replace("&#xE528;", "7");
                    split[i] = split[i].replace("&#xE529;", "8");
                    split[i] = split[i].replace("&#xE52A;", "5");
                    split[i] = split[i].replace("&#xE52B;", "0");
                    split[i] = split[i].replace("&#xE52C;", "5");
                    split[i] = split[i].replace("&#xE52D;", "7");
                    split[i] = split[i].replace("&#xE52E;", "6");
                    split[i] = split[i].replace("&#xE52F;", "9");

                    split[i] = split[i].replace("&#xE530;", "8");
                    split[i] = split[i].replace("&#xE531;", "0");
                    split[i] = split[i].replace("&#xE532;", "3");
                    split[i] = split[i].replace("&#xE533;", "6");
                    split[i] = split[i].replace("&#xE534;", "2");
                    split[i] = split[i].replace("&#xE535;", "5");
                    split[i] = split[i].replace("&#xE536;", "2");
                    split[i] = split[i].replace("&#xE537;", "3");
                    split[i] = split[i].replace("&#xE538;", "6");
                    split[i] = split[i].replace("&#xE539;", "4");
                    split[i] = split[i].replace("&#xE53A;", "8");
                    split[i] = split[i].replace("&#xE53B;", "3");
                    split[i] = split[i].replace("&#xE53C;", "-");
                    split[i] = split[i].replace("&#xE53D;", "0");
                    split[i] = split[i].replace("&#xE53E;", "1");
                    split[i] = split[i].replace("&#xE53F;", "0");

                    split[i] = split[i].replace("&#xE540;", "6");
                    split[i] = split[i].replace("&#xE541;", "1");
                }
                else if (split[i].indexOf("data-obfuscation=\"5\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "1");
                    split[i] = split[i].replace("&#xE501;", "8");
                    split[i] = split[i].replace("&#xE502;", "-");
                    split[i] = split[i].replace("&#xE503;", "0");
                    split[i] = split[i].replace("&#xE504;", "4");
                    split[i] = split[i].replace("&#xE505;", "4");
                    split[i] = split[i].replace("&#xE506;", "4");
                    split[i] = split[i].replace("&#xE507;", "-");
                    split[i] = split[i].replace("&#xE508;", "6");
                    split[i] = split[i].replace("&#xE509;", "2");
                    split[i] = split[i].replace("&#xE50A;", "7");
                    split[i] = split[i].replace("&#xE50B;", "5");
                    split[i] = split[i].replace("&#xE50C;", "7");
                    split[i] = split[i].replace("&#xE50D;", "8");
                    split[i] = split[i].replace("&#xE50E;", "8");
                    split[i] = split[i].replace("&#xE50F;", "0");

                    split[i] = split[i].replace("&#xE510;", "2");
                    split[i] = split[i].replace("&#xE511;", "5");
                    split[i] = split[i].replace("&#xE512;", "8");
                    split[i] = split[i].replace("&#xE513;", "9");
                    split[i] = split[i].replace("&#xE514;", "0");
                    split[i] = split[i].replace("&#xE515;", "9");
                    split[i] = split[i].replace("&#xE516;", "0");
                    split[i] = split[i].replace("&#xE517;", "1");
                    split[i] = split[i].replace("&#xE518;", "4");
                    split[i] = split[i].replace("&#xE519;", "3");
                    split[i] = split[i].replace("&#xE51A;", "3");
                    split[i] = split[i].replace("&#xE51B;", "3");
                    split[i] = split[i].replace("&#xE51C;", "7");
                    split[i] = split[i].replace("&#xE51D;", "7");
                    split[i] = split[i].replace("&#xE51E;", "2");
                    split[i] = split[i].replace("&#xE51F;", "9");

                    split[i] = split[i].replace("&#xE520;", "5");
                    split[i] = split[i].replace("&#xE521;", "1");
                    split[i] = split[i].replace("&#xE522;", "-");
                    split[i] = split[i].replace("&#xE523;", "4");
                    split[i] = split[i].replace("&#xE524;", "5");
                    split[i] = split[i].replace("&#xE525;", "1");
                    split[i] = split[i].replace("&#xE526;", "6");
                    split[i] = split[i].replace("&#xE527;", "1");
                    split[i] = split[i].replace("&#xE528;", "7");
                    split[i] = split[i].replace("&#xE529;", "-");
                    split[i] = split[i].replace("&#xE52A;", "7");
                    split[i] = split[i].replace("&#xE52B;", "8");
                    split[i] = split[i].replace("&#xE52C;", "6");
                    split[i] = split[i].replace("&#xE52D;", "2");
                    split[i] = split[i].replace("&#xE52E;", "9");
                    split[i] = split[i].replace("&#xE52F;", "5");

                    split[i] = split[i].replace("&#xE530;", "4");
                    split[i] = split[i].replace("&#xE531;", "6");
                    split[i] = split[i].replace("&#xE532;", "5");
                    split[i] = split[i].replace("&#xE533;", "8");
                    split[i] = split[i].replace("&#xE534;", "3");
                    split[i] = split[i].replace("&#xE535;", "2");
                    split[i] = split[i].replace("&#xE536;", "0");
                    split[i] = split[i].replace("&#xE537;", "6");
                    split[i] = split[i].replace("&#xE538;", "1");
                    split[i] = split[i].replace("&#xE539;", "6");
                    split[i] = split[i].replace("&#xE53A;", "0");
                    split[i] = split[i].replace("&#xE53B;", "3");
                    split[i] = split[i].replace("&#xE53C;", "-");
                    split[i] = split[i].replace("&#xE53D;", "3");
                    split[i] = split[i].replace("&#xE53E;", "2");
                    split[i] = split[i].replace("&#xE53F;", "9");

                    split[i] = split[i].replace("&#xE540;", "9");
                    split[i] = split[i].replace("&#xE541;", "-");
                }
                else if (split[i].indexOf("data-obfuscation=\"6\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "9");
                    split[i] = split[i].replace("&#xE501;", "3");
                    split[i] = split[i].replace("&#xE502;", "8");
                    split[i] = split[i].replace("&#xE503;", "4");
                    split[i] = split[i].replace("&#xE504;", "2");
                    split[i] = split[i].replace("&#xE505;", "8");
                    split[i] = split[i].replace("&#xE506;", "2");
                    split[i] = split[i].replace("&#xE507;", "5");
                    split[i] = split[i].replace("&#xE508;", "5");
                    split[i] = split[i].replace("&#xE509;", "-");
                    split[i] = split[i].replace("&#xE50A;", "0");
                    split[i] = split[i].replace("&#xE50B;", "2");
                    split[i] = split[i].replace("&#xE50C;", "9");
                    split[i] = split[i].replace("&#xE50D;", "7");
                    split[i] = split[i].replace("&#xE50E;", "2");
                    split[i] = split[i].replace("&#xE50F;", "4");

                    split[i] = split[i].replace("&#xE510;", "2");
                    split[i] = split[i].replace("&#xE511;", "0");
                    split[i] = split[i].replace("&#xE512;", "1");
                    split[i] = split[i].replace("&#xE513;", "6");
                    split[i] = split[i].replace("&#xE514;", "-");
                    split[i] = split[i].replace("&#xE515;", "4");
                    split[i] = split[i].replace("&#xE516;", "9");
                    split[i] = split[i].replace("&#xE517;", "-");
                    split[i] = split[i].replace("&#xE518;", "1");
                    split[i] = split[i].replace("&#xE519;", "1");
                    split[i] = split[i].replace("&#xE51A;", "8");
                    split[i] = split[i].replace("&#xE51B;", "0");
                    split[i] = split[i].replace("&#xE51C;", "4");
                    split[i] = split[i].replace("&#xE51D;", "7");
                    split[i] = split[i].replace("&#xE51E;", "2");
                    split[i] = split[i].replace("&#xE51F;", "8");

                    split[i] = split[i].replace("&#xE520;", "-");
                    split[i] = split[i].replace("&#xE521;", "7");
                    split[i] = split[i].replace("&#xE522;", "5");
                    split[i] = split[i].replace("&#xE523;", "6");
                    split[i] = split[i].replace("&#xE524;", "5");
                    split[i] = split[i].replace("&#xE525;", "0");
                    split[i] = split[i].replace("&#xE526;", "7");
                    split[i] = split[i].replace("&#xE527;", "3");
                    split[i] = split[i].replace("&#xE528;", "3");
                    split[i] = split[i].replace("&#xE529;", "7");
                    split[i] = split[i].replace("&#xE52A;", "3");
                    split[i] = split[i].replace("&#xE52B;", "8");
                    split[i] = split[i].replace("&#xE52C;", "8");
                    split[i] = split[i].replace("&#xE52D;", "9");
                    split[i] = split[i].replace("&#xE52E;", "-");
                    split[i] = split[i].replace("&#xE52F;", "0");

                    split[i] = split[i].replace("&#xE530;", "6");
                    split[i] = split[i].replace("&#xE531;", "3");
                    split[i] = split[i].replace("&#xE532;", "5");
                    split[i] = split[i].replace("&#xE533;", "3");
                    split[i] = split[i].replace("&#xE534;", "6");
                    split[i] = split[i].replace("&#xE535;", "0");
                    split[i] = split[i].replace("&#xE536;", "6");
                    split[i] = split[i].replace("&#xE537;", "1");
                    split[i] = split[i].replace("&#xE538;", "1");
                    split[i] = split[i].replace("&#xE539;", "9");
                    split[i] = split[i].replace("&#xE53A;", "6");
                    split[i] = split[i].replace("&#xE53B;", "9");
                    split[i] = split[i].replace("&#xE53C;", "7");
                    split[i] = split[i].replace("&#xE53D;", "1");
                    split[i] = split[i].replace("&#xE53E;", "4");
                    split[i] = split[i].replace("&#xE53F;", "4");

                    split[i] = split[i].replace("&#xE540;", "-");
                    split[i] = split[i].replace("&#xE541;", "5");
                }
                else if (split[i].indexOf("data-obfuscation=\"7\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "4");
                    split[i] = split[i].replace("&#xE501;", "1");
                    split[i] = split[i].replace("&#xE502;", "3");
                    split[i] = split[i].replace("&#xE503;", "9");
                    split[i] = split[i].replace("&#xE504;", "1");
                    split[i] = split[i].replace("&#xE505;", "7");
                    split[i] = split[i].replace("&#xE506;", "-");
                    split[i] = split[i].replace("&#xE507;", "-");
                    split[i] = split[i].replace("&#xE508;", "3");
                    split[i] = split[i].replace("&#xE509;", "5");
                    split[i] = split[i].replace("&#xE50A;", "9");
                    split[i] = split[i].replace("&#xE50B;", "-");
                    split[i] = split[i].replace("&#xE50C;", "0");
                    split[i] = split[i].replace("&#xE50D;", "6");
                    split[i] = split[i].replace("&#xE50E;", "2");
                    split[i] = split[i].replace("&#xE50F;", "1");

                    split[i] = split[i].replace("&#xE510;", "9");
                    split[i] = split[i].replace("&#xE511;", "8");
                    split[i] = split[i].replace("&#xE512;", "2");
                    split[i] = split[i].replace("&#xE513;", "1");
                    split[i] = split[i].replace("&#xE514;", "8");
                    split[i] = split[i].replace("&#xE515;", "7");
                    split[i] = split[i].replace("&#xE516;", "3");
                    split[i] = split[i].replace("&#xE517;", "5");
                    split[i] = split[i].replace("&#xE518;", "0");
                    split[i] = split[i].replace("&#xE519;", "3");
                    split[i] = split[i].replace("&#xE51A;", "9");
                    split[i] = split[i].replace("&#xE51B;", "1");
                    split[i] = split[i].replace("&#xE51C;", "5");
                    split[i] = split[i].replace("&#xE51D;", "5");
                    split[i] = split[i].replace("&#xE51E;", "6");
                    split[i] = split[i].replace("&#xE51F;", "-");

                    split[i] = split[i].replace("&#xE520;", "9");
                    split[i] = split[i].replace("&#xE521;", "7");
                    split[i] = split[i].replace("&#xE522;", "2");
                    split[i] = split[i].replace("&#xE523;", "-");
                    split[i] = split[i].replace("&#xE524;", "8");
                    split[i] = split[i].replace("&#xE525;", "4");
                    split[i] = split[i].replace("&#xE526;", "3");
                    split[i] = split[i].replace("&#xE527;", "8");
                    split[i] = split[i].replace("&#xE528;", "0");
                    split[i] = split[i].replace("&#xE529;", "6");
                    split[i] = split[i].replace("&#xE52A;", "8");
                    split[i] = split[i].replace("&#xE52B;", "0");
                    split[i] = split[i].replace("&#xE52C;", "4");
                    split[i] = split[i].replace("&#xE52D;", "6");
                    split[i] = split[i].replace("&#xE52E;", "8");
                    split[i] = split[i].replace("&#xE52F;", "9");

                    split[i] = split[i].replace("&#xE530;", "6");
                    split[i] = split[i].replace("&#xE531;", "4");
                    split[i] = split[i].replace("&#xE532;", "-");
                    split[i] = split[i].replace("&#xE533;", "3");
                    split[i] = split[i].replace("&#xE534;", "7");
                    split[i] = split[i].replace("&#xE535;", "2");
                    split[i] = split[i].replace("&#xE536;", "4");
                    split[i] = split[i].replace("&#xE537;", "5");
                    split[i] = split[i].replace("&#xE538;", "7");
                    split[i] = split[i].replace("&#xE539;", "2");
                    split[i] = split[i].replace("&#xE53A;", "1");
                    split[i] = split[i].replace("&#xE53B;", "4");
                    split[i] = split[i].replace("&#xE53C;", "2");
                    split[i] = split[i].replace("&#xE53D;", "0");
                    split[i] = split[i].replace("&#xE53E;", "0");
                    split[i] = split[i].replace("&#xE53F;", "5");

                    split[i] = split[i].replace("&#xE540;", "6");
                    split[i] = split[i].replace("&#xE541;", "7");
                }
                else if (split[i].indexOf("data-obfuscation=\"8\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "3");
                    split[i] = split[i].replace("&#xE501;", "1");
                    split[i] = split[i].replace("&#xE502;", "-");
                    split[i] = split[i].replace("&#xE503;", "1");
                    split[i] = split[i].replace("&#xE504;", "9");
                    split[i] = split[i].replace("&#xE505;", "9");
                    split[i] = split[i].replace("&#xE506;", "7");
                    split[i] = split[i].replace("&#xE507;", "-");
                    split[i] = split[i].replace("&#xE508;", "2");
                    split[i] = split[i].replace("&#xE509;", "5");
                    split[i] = split[i].replace("&#xE50A;", "0");
                    split[i] = split[i].replace("&#xE50B;", "1");
                    split[i] = split[i].replace("&#xE50C;", "8");
                    split[i] = split[i].replace("&#xE50D;", "7");
                    split[i] = split[i].replace("&#xE50E;", "3");
                    split[i] = split[i].replace("&#xE50F;", "3");

                    split[i] = split[i].replace("&#xE510;", "5");
                    split[i] = split[i].replace("&#xE511;", "7");
                    split[i] = split[i].replace("&#xE512;", "4");
                    split[i] = split[i].replace("&#xE513;", "6");
                    split[i] = split[i].replace("&#xE514;", "6");
                    split[i] = split[i].replace("&#xE515;", "1");
                    split[i] = split[i].replace("&#xE516;", "8");
                    split[i] = split[i].replace("&#xE517;", "4");
                    split[i] = split[i].replace("&#xE518;", "0");
                    split[i] = split[i].replace("&#xE519;", "9");
                    split[i] = split[i].replace("&#xE51A;", "5");
                    split[i] = split[i].replace("&#xE51B;", "1");
                    split[i] = split[i].replace("&#xE51C;", "3");
                    split[i] = split[i].replace("&#xE51D;", "5");
                    split[i] = split[i].replace("&#xE51E;", "0");
                    split[i] = split[i].replace("&#xE51F;", "9");

                    split[i] = split[i].replace("&#xE520;", "9");
                    split[i] = split[i].replace("&#xE521;", "6");
                    split[i] = split[i].replace("&#xE522;", "2");
                    split[i] = split[i].replace("&#xE523;", "8");
                    split[i] = split[i].replace("&#xE524;", "-");
                    split[i] = split[i].replace("&#xE525;", "-");
                    split[i] = split[i].replace("&#xE526;", "8");
                    split[i] = split[i].replace("&#xE527;", "-");
                    split[i] = split[i].replace("&#xE528;", "2");
                    split[i] = split[i].replace("&#xE529;", "4");
                    split[i] = split[i].replace("&#xE52A;", "4");
                    split[i] = split[i].replace("&#xE52B;", "0");
                    split[i] = split[i].replace("&#xE52C;", "9");
                    split[i] = split[i].replace("&#xE52D;", "8");
                    split[i] = split[i].replace("&#xE52E;", "2");
                    split[i] = split[i].replace("&#xE52F;", "7");

                    split[i] = split[i].replace("&#xE530;", "5");
                    split[i] = split[i].replace("&#xE531;", "7");
                    split[i] = split[i].replace("&#xE532;", "2");
                    split[i] = split[i].replace("&#xE533;", "6");
                    split[i] = split[i].replace("&#xE534;", "6");
                    split[i] = split[i].replace("&#xE535;", "-");
                    split[i] = split[i].replace("&#xE536;", "5");
                    split[i] = split[i].replace("&#xE537;", "3");
                    split[i] = split[i].replace("&#xE538;", "4");
                    split[i] = split[i].replace("&#xE539;", "1");
                    split[i] = split[i].replace("&#xE53A;", "3");
                    split[i] = split[i].replace("&#xE53B;", "7");
                    split[i] = split[i].replace("&#xE53C;", "2");
                    split[i] = split[i].replace("&#xE53D;", "0");
                    split[i] = split[i].replace("&#xE53E;", "0");
                    split[i] = split[i].replace("&#xE53F;", "6");

                    split[i] = split[i].replace("&#xE540;", "4");
                    split[i] = split[i].replace("&#xE541;", "8");
                }
                else if (split[i].indexOf("data-obfuscation=\"9\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "3");
                    split[i] = split[i].replace("&#xE501;", "1");
                    split[i] = split[i].replace("&#xE502;", "-");
                    split[i] = split[i].replace("&#xE503;", "1");
                    split[i] = split[i].replace("&#xE504;", "9");
                    split[i] = split[i].replace("&#xE505;", "9");
                    split[i] = split[i].replace("&#xE506;", "7");
                    split[i] = split[i].replace("&#xE507;", "-");
                    split[i] = split[i].replace("&#xE508;", "2");
                    split[i] = split[i].replace("&#xE509;", "5");
                    split[i] = split[i].replace("&#xE50A;", "0");
                    split[i] = split[i].replace("&#xE50B;", "1");
                    split[i] = split[i].replace("&#xE50C;", "8");
                    split[i] = split[i].replace("&#xE50D;", "7");
                    split[i] = split[i].replace("&#xE50E;", "3");
                    split[i] = split[i].replace("&#xE50F;", "3");

                    split[i] = split[i].replace("&#xE510;", "5");
                    split[i] = split[i].replace("&#xE511;", "7");
                    split[i] = split[i].replace("&#xE512;", "4");
                    split[i] = split[i].replace("&#xE513;", "6");
                    split[i] = split[i].replace("&#xE514;", "6");
                    split[i] = split[i].replace("&#xE515;", "1");
                    split[i] = split[i].replace("&#xE516;", "8");
                    split[i] = split[i].replace("&#xE517;", "4");
                    split[i] = split[i].replace("&#xE518;", "0");
                    split[i] = split[i].replace("&#xE519;", "9");
                    split[i] = split[i].replace("&#xE51A;", "5");
                    split[i] = split[i].replace("&#xE51B;", "1");
                    split[i] = split[i].replace("&#xE51C;", "3");
                    split[i] = split[i].replace("&#xE51D;", "5");
                    split[i] = split[i].replace("&#xE51E;", "0");
                    split[i] = split[i].replace("&#xE51F;", "9");

                    split[i] = split[i].replace("&#xE520;", "9");
                    split[i] = split[i].replace("&#xE521;", "6");
                    split[i] = split[i].replace("&#xE522;", "2");
                    split[i] = split[i].replace("&#xE523;", "8");
                    split[i] = split[i].replace("&#xE524;", "-");
                    split[i] = split[i].replace("&#xE525;", "-");
                    split[i] = split[i].replace("&#xE526;", "8");
                    split[i] = split[i].replace("&#xE527;", "-");
                    split[i] = split[i].replace("&#xE528;", "2");
                    split[i] = split[i].replace("&#xE529;", "4");
                    split[i] = split[i].replace("&#xE52A;", "4");
                    split[i] = split[i].replace("&#xE52B;", "0");
                    split[i] = split[i].replace("&#xE52C;", "9");
                    split[i] = split[i].replace("&#xE52D;", "8");
                    split[i] = split[i].replace("&#xE52E;", "2");
                    split[i] = split[i].replace("&#xE52F;", "7");

                    split[i] = split[i].replace("&#xE530;", "5");
                    split[i] = split[i].replace("&#xE531;", "7");
                    split[i] = split[i].replace("&#xE532;", "2");
                    split[i] = split[i].replace("&#xE533;", "6");
                    split[i] = split[i].replace("&#xE534;", "6");
                    split[i] = split[i].replace("&#xE535;", "-");
                    split[i] = split[i].replace("&#xE536;", "5");
                    split[i] = split[i].replace("&#xE537;", "3");
                    split[i] = split[i].replace("&#xE538;", "4");
                    split[i] = split[i].replace("&#xE539;", "1");
                    split[i] = split[i].replace("&#xE53A;", "3");
                    split[i] = split[i].replace("&#xE53B;", "7");
                    split[i] = split[i].replace("&#xE53C;", "2");
                    split[i] = split[i].replace("&#xE53D;", "0");
                    split[i] = split[i].replace("&#xE53E;", "0");
                    split[i] = split[i].replace("&#xE53F;", "6");

                    split[i] = split[i].replace("&#xE540;", "4");
                    split[i] = split[i].replace("&#xE541;", "8");
                }
                else if (split[i].indexOf("data-obfuscation=\"10\"") >= 0) {
                    split[i] = split[i].replace("&#xE500;", "3");
                    split[i] = split[i].replace("&#xE501;", "1");
                    split[i] = split[i].replace("&#xE502;", "2");
                    split[i] = split[i].replace("&#xE503;", "8");
                    split[i] = split[i].replace("&#xE504;", "5");
                    split[i] = split[i].replace("&#xE505;", "-");
                    split[i] = split[i].replace("&#xE506;", "3");
                    split[i] = split[i].replace("&#xE507;", "1");
                    split[i] = split[i].replace("&#xE508;", "6");
                    split[i] = split[i].replace("&#xE509;", "8");
                    split[i] = split[i].replace("&#xE50A;", "9");
                    split[i] = split[i].replace("&#xE50B;", "4");
                    split[i] = split[i].replace("&#xE50C;", "7");
                    split[i] = split[i].replace("&#xE50D;", "8");
                    split[i] = split[i].replace("&#xE50E;", "9");
                    split[i] = split[i].replace("&#xE50F;", "5");

                    split[i] = split[i].replace("&#xE510;", "0");
                    split[i] = split[i].replace("&#xE511;", "1");
                    split[i] = split[i].replace("&#xE512;", "7");
                    split[i] = split[i].replace("&#xE513;", "0");
                    split[i] = split[i].replace("&#xE514;", "7");
                    split[i] = split[i].replace("&#xE515;", "9");
                    split[i] = split[i].replace("&#xE516;", "2");
                    split[i] = split[i].replace("&#xE517;", "2");
                    split[i] = split[i].replace("&#xE518;", "4");
                    split[i] = split[i].replace("&#xE519;", "6");
                    split[i] = split[i].replace("&#xE51A;", "9");
                    split[i] = split[i].replace("&#xE51B;", "0");
                    split[i] = split[i].replace("&#xE51C;", "1");
                    split[i] = split[i].replace("&#xE51D;", "6");
                    split[i] = split[i].replace("&#xE51E;", "1");
                    split[i] = split[i].replace("&#xE51F;", "0");

                    split[i] = split[i].replace("&#xE520;", "5");
                    split[i] = split[i].replace("&#xE521;", "8");
                    split[i] = split[i].replace("&#xE522;", "3");
                    split[i] = split[i].replace("&#xE523;", "3");
                    split[i] = split[i].replace("&#xE524;", "-");
                    split[i] = split[i].replace("&#xE525;", "4");
                    split[i] = split[i].replace("&#xE526;", "5");
                    split[i] = split[i].replace("&#xE527;", "4");
                    split[i] = split[i].replace("&#xE528;", "6");
                    split[i] = split[i].replace("&#xE529;", "7");
                    split[i] = split[i].replace("&#xE52A;", "-");
                    split[i] = split[i].replace("&#xE52B;", "3");
                    split[i] = split[i].replace("&#xE52C;", "8");
                    split[i] = split[i].replace("&#xE52D;", "3");
                    split[i] = split[i].replace("&#xE52E;", "9");
                    split[i] = split[i].replace("&#xE52F;", "2");

                    split[i] = split[i].replace("&#xE530;", "5");
                    split[i] = split[i].replace("&#xE531;", "0");
                    split[i] = split[i].replace("&#xE532;", "9");
                    split[i] = split[i].replace("&#xE533;", "-");
                    split[i] = split[i].replace("&#xE534;", "4");
                    split[i] = split[i].replace("&#xE535;", "2");
                    split[i] = split[i].replace("&#xE536;", "2");
                    split[i] = split[i].replace("&#xE537;", "-");
                    split[i] = split[i].replace("&#xE538;", "8");
                    split[i] = split[i].replace("&#xE539;", "-");
                    split[i] = split[i].replace("&#xE53A;", "7");
                    split[i] = split[i].replace("&#xE53B;", "4");
                    split[i] = split[i].replace("&#xE53C;", "1");
                    split[i] = split[i].replace("&#xE53D;", "7");
                    split[i] = split[i].replace("&#xE53E;", "6");
                    split[i] = split[i].replace("&#xE53F;", "6");

                    split[i] = split[i].replace("&#xE540;", "0");
                    split[i] = split[i].replace("&#xE541;", "5");
                }
                else
                {
                    // Was machen wenn wieder ne neue Schrift kommt?
                    split[i] = "-:-";
                }

                //Log.d("Sportinfo","ersetzt: "+split[i]);
                String cleanString = Html.fromHtml(split[i]).toString().trim();
                //Log.d("Sportinfo","clean: "+cleanString);
                String[] ergsplit = cleanString.split(":");
                try {
                    punkteheim = Integer.parseInt(ergsplit[0].trim());
                } catch (NumberFormatException nfe) {
                    punkteheim = -1;
                }
                try {
                    punktegast = Integer.parseInt(ergsplit[1].trim());
                } catch (NumberFormatException nfe) {
                    punktegast = -1;
                }

                //Log.d("Sportinfo","Punkte: "+punkteheim+":"+punktegast);

                //check ob spiel angelegt ist
                long idspiel;
                String sqlgetgame = "SELECT idspiel FROM spiele AS s";
                sqlgetgame += " WHERE datum='" + datum + "' AND idfavorit=" + idfavorit + " AND idgegner=" + idgegner;
                Cursor sqlresultgame = connection.rawQuery(sqlgetgame, null);
                if (sqlresultgame.getCount() > 0) {
                    sqlresultgame.moveToFirst();
                    idspiel = sqlresultgame.getInt(0);
                    //Log.d("SPORTINFO", "Update Spiel "+idspiel);
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
                    //Log.d("SPORTINFO", "INSERT Spiel "+idspiel);
                }


                // Reset
                datum = "";
                punkteheim = -1;
                punktegast = -1;
                heim = "";
                gast = "";
            }
        }
        database.close();
        connection.close();
    }

    public void getTable(String htmlsource) {
        //----------------------------------------------------
        // update fussball.de table for e specific team
        //----------------------------------------------------
        SQLiteOpenHelper database = new SqliteHelper(this.activity.getApplicationContext());
        SQLiteDatabase connection = database.getWritableDatabase();

        String mannschaftname = "";
        int punkte = 0;
        int tabellennr = 0;
        int intfavorit = 0;

        String[] split = htmlsource.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (split[i].indexOf("td class=\"column-rank\"") >= 0) {
                // Platzierung
                tabellennr = Integer.parseInt(Html.fromHtml(split[i]).toString().trim().replace(".", ""));
            } else if (split[i].indexOf("div class=\"club-name\"") >= 0) {
                // Name der Mannschaft
                mannschaftname = Html.fromHtml(split[i]).toString().trim();
            } else if (split[i].indexOf("td class=\"column-points\"") >= 0) {
                // Punkte und Ende der Zeile
                punkte = Integer.parseInt(Html.fromHtml(split[i]).toString().trim().replace(".", ""));

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
                }

                mannschaftname = "";
                punkte = 0;
                tabellennr = 0;
                intfavorit = 0;
            }
        }
        database.close();
        connection.close();
    }
}
