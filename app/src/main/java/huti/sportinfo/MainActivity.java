package huti.sportinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if (id == R.id.action_update) {
            Toast.makeText(getApplicationContext(), "Aktualisiere Datenbank...", Toast.LENGTH_SHORT).show();

            SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
            SQLiteDatabase connection = database.getReadableDatabase();
            Cursor sqlresult = connection.rawQuery("SELECT urlspiele,urltabelle,idfavorit,intsportart FROM favoriten", null);
            int idfavorit = 0;
            int intsportart = 0;
            String urlspiele = "";
            String urltabelle = "";
            int intlast = 0;
            int inturlart = 0;
            while (sqlresult.moveToNext()) {

                urlspiele = sqlresult.getString(0);
                urltabelle = sqlresult.getString(1);
                idfavorit = sqlresult.getInt(2);
                intsportart = sqlresult.getInt(3);
                if (sqlresult.isLast()) {
                    intlast = 1;
                }

                inturlart = 0; // Spiele werden abgerufen
                new RequestTask(this, urlspiele, inturlart, idfavorit, intsportart, intlast).execute();

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
        TextView out = (TextView) findViewById(R.id.txtUpcomingGames);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        out.setText(currentDateandTime+"\n");
        SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();
        Cursor sqlresult = connection.rawQuery("SELECT datum,idfavorit,idgegner,intheimspiel,punkteheim,punktegast FROM spiele", null);
        if (sqlresult.getCount() > 0) {
            while (sqlresult.moveToNext()) {
                out.append(sqlresult.getString(0) + "\n");
            }
        }
        else
        {
            out.setText("Keine anstehenden Spiele vorhanden.");
        }
        database.close();
        connection.close();
    }

    class RequestTask extends AsyncTask<String, String, String> {
        private MainActivity activity = null;
        private String url = "";
        private int inturlart = 0;
        private int idfavorit = 0;
        private int intsportart = 0;
        private int intlast = 0;

        public RequestTask(MainActivity activity, String url, int urlart, int idfavorit, int intsportart, int intlast) {
            this.activity = activity;
            this.url = url;
            this.inturlart = urlart;
            this.idfavorit = idfavorit;
            this.intsportart = intsportart;
            this.intlast = intlast;
        }

        @Override
        protected String doInBackground(String... params) {
            // only execute if internet is available
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
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
                Toast.makeText(getApplicationContext(), "Update konnte nicht durchgeführt werden.", Toast.LENGTH_LONG).show();
            } else {
                if (this.intsportart == 0 && this.inturlart == 0) {
                    //----------------------------------------------------
                    // update fussball.de games for e specific team
                    //----------------------------------------------------
                    SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
                    SQLiteDatabase connection = database.getWritableDatabase();

                    connection.execSQL("DELETE FROM spiele;");
                    connection.execSQL("VACUUM;");

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
                            split[i] = split[i].replace("&#xE52E;", "-"); // Bei den Scores sind Sonderzeichen angegeben
                            split[i] = split[i].replace("&#xE540;", "-");
                            String cleanString = Html.fromHtml(split[i]).toString().trim();

                            // Speichern des ganzes Spiels
                            String sqlinsert = "INSERT INTO spiele(datum,idfavorit,idgegner,intheimspiel,punkteheim,punktegast)";
                            sqlinsert += " VALUES('" + datum + "'," + idfavorit + ",0,0,1,0);";
                            connection.execSQL(sqlinsert);

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
                    if (this.intlast == 1) {
                        Toast.makeText(getApplicationContext(), "Aktualisierung abgeschlossen!", Toast.LENGTH_SHORT).show();

                        // Refresh auf Fenster fahren
                        this.activity.showUpcomingGames();
                    }
                }
            }
        }
    }
}


