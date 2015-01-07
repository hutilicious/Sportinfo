package huti.sportinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


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
            out.setText("Lade Inhalt...");
            // Infos zum updaten in DB speichern?
            Toast.makeText(getApplicationContext(), "Aktualisiere Datenbank...", Toast.LENGTH_LONG).show();
            new RequestTask().execute("http://www.fussball.de/ajax.team.next.games/-/team-id/011MIEVD2O000000VTVG0001VTR8C1K7", "debug", "last");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class RequestTask extends AsyncTask<String, String, String> {
        private String command = "";
        private boolean bolLetzterEintrag = false;

        @Override
        protected String doInBackground(String... uri) {
            this.bolLetzterEintrag = false;
            if (uri.length > 1) {
                this.command = uri[1];
            } else if (uri.length > 2) {
                this.bolLetzterEintrag = true;
            }
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
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
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (this.command.equals("debug")) {
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
                if (this.bolLetzterEintrag) {
                    Toast.makeText(getApplicationContext(), "Aktualisierung abgeschlossen!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}


