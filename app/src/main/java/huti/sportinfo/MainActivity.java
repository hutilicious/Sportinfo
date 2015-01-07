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


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            TextView out = (TextView) findViewById(R.id.txtWelcome);
            out.setText("");

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
                new RequestTask(urlspiele, inturlart, idfavorit, intsportart, intlast).execute();

                //inturlart = 1; // Tabelle wird abgerufen
                //..neuer Request
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class RequestTask extends AsyncTask<String, String, String> {
        private String url = "";
        private int inturlart = 0;
        private int idfavorit = 0;
        private int intsportart = 0;
        private int intlast = 0;

        public RequestTask(String url, int urlart, int idfavorit, int intsportart, int intlast) {
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
                    // update fussball.de games for e specific team
                    TextView out = (TextView) findViewById(R.id.txtWelcome);
                    out.append("\n\n");
                    String[] split = result.split("\n");
                    for (int i = 0; i < split.length; i++) {
                    /* ablauf für fussball:
                    td class="column-date"
                    div class="club-name"
                    div class="club-name"
                    class="column-score" | class="score-left",class="score-right"
                     */
                        if (split[i].indexOf("td class=\"column-date\"") >= 0 || split[i].indexOf("div class=\"club-name\"") >= 0 || split[i].indexOf("class=\"score-left\"") >= 0) {
                            split[i] = split[i].replace("&#xE52E;", "-"); // Bei den Scores sind Sonderzeichen angegeben
                            String cleanString = Html.fromHtml(split[i]).toString().trim();
                            out.append(i + ": " + split[i].trim() + "\n");
                            out.append(i + ": " + cleanString + "\n");
                        }
                    }

                    /* Datenbank befüllen


                    SQLiteOpenHelper database = new SqliteHelper(getApplicationContext());
                    SQLiteDatabase connection = database.getWritableDatabase();

                    connection.execSQL("DELETE FROM sportarten;");
                    connection.execSQL("VACUUM;");

                    connection.execSQL("INSERT INTO sportarten(bezeichnung) VALUES ('hallo');");

                    Cursor sqlresult = connection.rawQuery("SELECT bezeichnung FROM sportarten", null);
                    String sqlstring = "";
                    while (sqlresult.moveToNext()) {
                        sqlstring += sqlresult.getString(0) + "\n";
                    }
                    out.append(sqlstring);

                    database.close();
                    connection.close();
                    */
                    if (this.intlast == 1) {
                        Toast.makeText(getApplicationContext(), "Aktualisierung abgeschlossen!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}


