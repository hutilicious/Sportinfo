package huti.sportinfo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.Date;

import huti.sportinfo.modules.ModuleFussball;
import huti.sportinfo.modules.ModuleTischtennis;

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
                    //ByteArrayOutputStream out = new ByteArrayOutputStream();
                    if (this.intsportart == 1) {
                        responseString = EntityUtils.toString(response.getEntity(), HTTP.ISO_8859_1);
                    }
                    else
                    {
                        responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    }
                    //response.getEntity().writeTo(out);
                    //out.close();
                    //responseString = out.toString();
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
            if (this.intsportart == 0)
            {
                // Fussball.de
                ModuleFussball objFussball = new ModuleFussball(this.activity, this.url, kennung, this.inturlart, this.idfavorit, this.intsportart, this.intlast);
                if (this.inturlart == 0) {
                    objFussball.getTable(result);
                }
                else if (this.inturlart == 1)
                {
                    objFussball.getGames(result);
                }
            }
            else if (this.intsportart == 1)
            {
                // TT-info
                ModuleTischtennis objTischtennis = new ModuleTischtennis(this.activity, this.url, kennung, this.inturlart, this.idfavorit, this.intsportart, this.intlast);
                if (this.inturlart == 0) {
                    objTischtennis.getTable(result);
                }
                else if (this.inturlart == 1)
                {
                    objTischtennis.getGames(result);
                }
            }

            if (this.intlast == 1) {
                Toast.makeText(this.activity.getApplicationContext(), R.string.txtActionUpdateOk, Toast.LENGTH_SHORT).show();

                SQLiteOpenHelper database = new SqliteHelper(this.activity.getApplicationContext());
                SQLiteDatabase connection = database.getWritableDatabase();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateTime = sdf.format(new Date());
                ContentValues valUpdate = new ContentValues();
                valUpdate.put("datum", currentDateTime);
                valUpdate.put("log", "");
                connection.insert("updates", null, valUpdate);
                connection.close();
                database.close();

                // Refresh auf Fenster fahren
                this.activity.isUpdating = false;
                this.activity.showUpcomingGames();
                this.activity.showTables();
                this.activity.updateActionBar();
            }
        }
    }
}
