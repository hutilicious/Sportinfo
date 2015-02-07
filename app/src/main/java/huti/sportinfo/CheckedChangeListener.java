package huti.sportinfo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.CompoundButton;

/**
 * Created by Huti on 07.02.2015.
 */
public class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        ViewTag myTag = (ViewTag) view.getTag();
        int idpk = myTag.idpk;
        if (idpk > 0) {
            // Log.d("ChangeListener", myTag.bezeichnung + " wird gesetzt zu: " + (isChecked ? 1 : 0));
            SQLiteOpenHelper database = new SqliteHelper(view.getContext());
            SQLiteDatabase connection = database.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put("intaktiv", isChecked ? 1 : 0);
            connection.update("favoriten", values, "idfavorit=" + idpk, null);
        }
    }
}
