package huti.sportinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by Adrian on 12.01.2015.
 */
public class TablesFragment extends Fragment

    {

    private View view;
    private TableRow.LayoutParams tlparams = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upcominggames_fragment, container, false);
        showTables();
        return view;
    }

    public void showTables() {
        SQLiteOpenHelper database = new SqliteHelper(getActivity().getApplicationContext());
        SQLiteDatabase connection = database.getReadableDatabase();
        String sqlget = "SELECT t.idfavorit,t.tabellennr,t.punkte,g.bezeichnung as gegnerbez,t.intfavorit";
        sqlget += " FROM tabellen AS t";
        sqlget += " LEFT JOIN gegner AS g ON t.idmannschaft = g.idgegner";
        sqlget += " ORDER BY t.idfavorit,t.tabellennr";
        Cursor sqlresult = connection.rawQuery(sqlget, null);
        if (sqlresult.getCount() > 0) {

            // Willkommensnachricht kann weg, da wir bereits was in der Datenbank haben
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout =(LinearLayout) view.findViewById(R.id.layoutWelcome);
            if (linearLayout.findViewById(R.id.txtWelcome) != null) {
                linearLayout.removeView(linearLayout.findViewById(R.id.txtWelcome));
            }
            // Add a fresh TableLayout
            TableLayout tblTables = new TableLayout(getActivity());
            tblTables.setId(R.id.tblTables);
            tblTables.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tblTables.setColumnStretchable(2, true);

            int idfavorit = 0;
            int idfavorit_alt = 0;
            int intfavorit = 0;
            int tabellennr = 0;
            int punkte = 0;
            String mannschaftname = "";
            String favoritenbezeichnung = "";
            int rowcounter = 0;
            int trBackground = Color.WHITE;

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

                    tblTables.addView(RowScoreHeader("Tabelle für : " + favoritenbezeichnung));
                    rowcounter = 0;
                }

                if (intfavorit == 0) {
                    mannschaftname = sqlresult.getString(sqlresult.getColumnIndex("gegnerbez"));
                } else {
                    mannschaftname = favoritenbezeichnung;
                }
                if (rowcounter % 2 == 0) {
                    trBackground = Color.WHITE;
                } else {
                    trBackground = getResources().getColor(R.color.colorMainAccent);
                }

                tblTables.addView(RowScore(tabellennr, mannschaftname, punkte, intfavorit > 0, trBackground));

                idfavorit_alt = idfavorit;
                rowcounter++;
            }
            linearLayout.addView(tblTables);
        }
    }

        private TableRow RowScoreHeader(String titel) {
            TableRow tr = new TableRow(getActivity());
            tr.setLayoutParams(tlparams);

            TextView txtTitel = new TextView(getActivity());
            txtTitel.setText(titel);
            txtTitel.setTextSize(15);
            txtTitel.setPadding(30, 5, 30, 5);
            txtTitel.setTextColor(getResources().getColor(R.color.colorTextLight));


            tr.addView(txtTitel);


            tr.setBackgroundColor(getResources().getColor(R.color.colorMainRow));

            TableRow.LayoutParams params = (TableRow.LayoutParams) txtTitel.getLayoutParams();
            params.span = 3;
            txtTitel.setLayoutParams(params); // causes layout update

            return tr;
        }

        private TableRow RowScore(int tabellennr, String name, int punkte, boolean bolHighlight, int trBackground) {
            TableRow tr = new TableRow(getActivity());
            tr.setLayoutParams(tlparams);

            TextView txtNummer = new TextView(getActivity());
            txtNummer.setText(tabellennr + ".");
            txtNummer.setGravity(Gravity.RIGHT);
            txtNummer.setTextSize(17);
            txtNummer.setPadding(30, 5, 30, 5);
            if (bolHighlight) {
                txtNummer.setTypeface(null, Typeface.BOLD);
            }
            tr.addView(txtNummer);

            TextView txtName = new TextView(getActivity());
            txtName.setText(name);
            txtName.setTextSize(17);
            txtName.setPadding(30, 5, 30, 5);
            if (bolHighlight) {
                txtName.setTypeface(null, Typeface.BOLD);
            }
            tr.addView(txtName);

            TextView txtPunkte = new TextView(getActivity());
            txtPunkte.setText(Integer.toString(punkte));
            txtPunkte.setTextSize(17);
            txtPunkte.setGravity(Gravity.RIGHT);
            txtPunkte.setPadding(30, 5, 30, 5);
            if (bolHighlight) {
                txtPunkte.setTypeface(null, Typeface.BOLD);
            }
            tr.addView(txtPunkte);


            tr.setBackgroundColor(trBackground);

            return tr;
        }
}
