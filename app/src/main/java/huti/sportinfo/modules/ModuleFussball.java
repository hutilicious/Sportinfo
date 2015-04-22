package huti.sportinfo.modules;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import huti.sportinfo.SqliteHelper;
import huti.sportinfo.Config;

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
        // TODO Fix SVK H2 Games
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
                // Reset
                //Log.d("SPORTINFO","Setzte Daten zurück\n");
                datum = "";
                punkteheim = -1;
                punktegast = -1;
                heim = "";
                gast = "";

                // Datum abrufen
                datum = Html.fromHtml(split[i]).toString().trim();
                try {
                    datum = datum.substring(10, 12) + "-" + datum.substring(7, 9) + "-" + datum.substring(4, 6) + " " + datum.substring(15, 20);
                } catch (StringIndexOutOfBoundsException e) {
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
                //Log.d("SPORTINFO","Verein:"+split[i]);
                String cleanString = Html.fromHtml(split[i]).toString().trim();
                //Log.d("SPORTINFO","Verein clean:"+cleanString);
                if (heim.equals("")) {
                    heim = cleanString;
                    //Log.d("SPORTINFO","Heim:"+heim);
                } else {
                    gast = cleanString;
                    //Log.d("SPORTINFO","Gast:"+gast);
                }
            } else if (split[i].indexOf("class=\"score-left\"") >= 0 && !heim.trim().equals("")) {
                // Ergebnis abrufen und Datensatz speichern, wenn noch nicht da

                //Log.d("SPORTINFOSPIEL", datum + ": " + heim + " - " + gast);

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
                    //Log.d("SPORTINFO", "Habe Gegner '" + strGegner + "' gefunden: " + idgegner);
                } else {
                    values.put("idfavorit", idfavorit);
                    values.put("bezeichnung", strGegner);
                    idgegner = connection.insert("gegner", null, values);
                    //Log.d("SPORTINFO", "Lege Gegner '" + strGegner + "' an: " + idgegner);
                }

                // Sonderzeichen von fussball.de ersetzen
                //Log.d("SPORTINFO", "Score ohne Huti:" + split[i]);
                //Log.d("SPORTINFO", "Score von Huti:" + getScore(split[i]));

                split[i] = getScore(split[i]);

                String cleanString = Html.fromHtml(split[i]).toString().trim();
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

                //check ob spiel angelegt ist
                long idspiel;
                String sqlgetgame = "SELECT idspiel FROM spiele AS s";
                sqlgetgame += " WHERE datum='" + datum + "' AND idfavorit=" + idfavorit + " AND idgegner=" + idgegner;
                Cursor sqlresultgame = connection.rawQuery(sqlgetgame, null);
                if (sqlresultgame.getCount() > 0) {
                    sqlresultgame.moveToFirst();
                    idspiel = sqlresultgame.getInt(0);
                    //Log.d("SPORTINFO", "Update Spiel " + datum);
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
                    //Log.d("SPORTINFO", "INSERT Spiel " + datum);
                }
            }
        }
        connection.close();
        database.close();

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
        connection.close();
        database.close();

    }

    private String getScore(String htmlsource) {
        // <a href="http://www.fussball.de/spiel/-/spiel/01L7OUAH9G000000VV0AG813VSP6T6E0"><span data-obfuscation="8" class="score-left">&#xE502;</span><span class="colon">:</span><span data-obfuscation="8" class="score-right">&#xE527;</span></a>
        String find1 = "data-obfuscation=\"";
        String find2 = " class=\"score-left\">";
        String find3 = " class=\"score-right\">";
        String obfu = htmlsource.substring(htmlsource.indexOf(find1) + find1.length(), htmlsource.indexOf(find1) + find1.length() + 2);
        if (obfu.indexOf("\"") >= 0) {
            obfu = obfu.substring(0, 1);
        }
        String score1 = htmlsource.substring(htmlsource.indexOf(find2) + find2.length() + 3, htmlsource.indexOf(find2) + find2.length() + 7);
        String score2 = htmlsource.substring(htmlsource.indexOf(find3) + find3.length() + 3, htmlsource.indexOf(find3) + find3.length() + 7);
        /*Log.d("Sportinfo","Obfu:"+obfu);
        Log.d("Sportinfo","Score1:"+score1);
        Log.d("Sportinfo","Score2:"+score2);*/
        String url = "http://217.160.108.201/_huti/sportinfo/?obfu=" + obfu + "&key=" + score1;
        if (!Config.obfuscationData.containsKey(url)) {
            try {
                score1 = new ObfuscationHelper().execute(url).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Config.obfuscationData.put(url, score1);
        } else {
            //Log.d("SPORTINFO","Lade aus cache:"+url);
            score1 = Config.obfuscationData.get(url);
        }

        url = "http://217.160.108.201/_huti/sportinfo/?obfu=" + obfu + "&key=" + score2;
        if (!Config.obfuscationData.containsKey(url)) {
            try {
                score2 = new ObfuscationHelper().execute(url).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Config.obfuscationData.put(url, score2);
        } else {
            //Log.d("SPORTINFO","Lade aus cache:"+url);
            score2 = Config.obfuscationData.get(url);
        }
        return score1 + ":" + score2;
    }

    public class ObfuscationHelper extends AsyncTask<String, String, String> {

        public String getData(String url) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                } else {
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (UnsupportedEncodingException e) {
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return responseString;
        }

        @Override
        protected String doInBackground(String... urls) {
            return this.getData(urls[0]);
        }
    }
}
