package huti.sportinfo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by crothhass on 08.01.2015.
 */
class UpdateHelper extends AsyncTask<String, String, String> {
    private MainActivity activity = null;
    private String url = "";
    private String kennung = "";
    private int inturlart = 0;
    private int idfavorit = 0;
    private int intsportart = 0;
    private int intlast = 0;

    public UpdateHelper(MainActivity activity, String url, String kennung, int urlart, int idfavorit, int intsportart, int intlast) {
        this.activity = activity;
        this.url = url;
        this.kennung = kennung;
        this.inturlart = urlart;
        this.idfavorit = idfavorit;
        this.intsportart = intsportart;
        this.intlast = intlast;
    }

    @Override
    protected String doInBackground(String... params) {
        // only execute if internet is available
        ConnectivityManager connMgr = (ConnectivityManager) this.activity.getSystemService(this.activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(this.url));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (UnsupportedEncodingException e) {
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return responseString;
        } else {
            return "::error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result.equals("::error")) {
            Toast.makeText(this.activity.getApplicationContext(), R.string.txtActionUpdateError, Toast.LENGTH_LONG).show();
        } else {
            if (this.intsportart == 0 && this.inturlart == 0) {
                //----------------------------------------------------
                // update fussball.de games for e specific team
                //----------------------------------------------------
                SQLiteOpenHelper database = new SqliteHelper(this.activity.getApplicationContext());
                SQLiteDatabase connection = database.getWritableDatabase();

                /*connection.execSQL("DELETE FROM spiele;");
                connection.execSQL("VACUUM;");
                connection.execSQL("DELETE FROM gegner;");
                connection.execSQL("VACUUM;");*/

                String datum = "";
                int punkteheim = -1;
                int punktegast = -1;
                String heim = "";
                String gast = "";

                String[] split = result.split("\n");
                for (int i = 0; i < split.length; i++) {
                    /* ablauf für fussball:
                    td class="column-date",div class="club-name",div class="club-name",class="column-score" | class="score-left",class="score-right"
                     */
                    if (split[i].indexOf("td class=\"column-date\"") >= 0) {
                        // Datum abrufen
                        datum = Html.fromHtml(split[i]).toString().trim();
                        datum = "20" + datum.substring(10, 12) + "-" + datum.substring(7, 9) + "-" + datum.substring(4, 6) + " " + datum.substring(15, 20) + ":00";
                    } else if (split[i].indexOf("div class=\"club-name\"") >= 0) {
                        // Verein speichern
                        String cleanString = Html.fromHtml(split[i]).toString().trim();
                        if (heim.equals("")) {
                            heim = cleanString;
                        } else {
                            gast = cleanString;
                        }
                    } else if (split[i].indexOf("class=\"score-left\"") >= 0) {
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

                        split[i] = split[i].replace("&#xE52E;", "-"); // Bei den Scores sind Sonderzeichen angegeben
                        split[i] = split[i].replace("&#xE540;", "-");
                        String cleanString = Html.fromHtml(split[i]).toString().trim();
                        String[] ergsplit = result.split(":");
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

                if (this.intlast == 1) {
                    Toast.makeText(this.activity.getApplicationContext(), R.string.txtActionUpdateOk, Toast.LENGTH_SHORT).show();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDateTime = sdf.format(new Date());
                    ContentValues valUpdate = new ContentValues();
                    valUpdate.put("datum", currentDateTime);
                    valUpdate.put("log", "");
                    connection.insert("updates", null, valUpdate);

                    // Refresh auf Fenster fahren
                    this.activity.isUpdating = false;
                    this.activity.showUpcomingGames();
                }
                database.close();
                connection.close();

            }
        }
    }
}
