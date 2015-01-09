package huti.sportinfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Huti on 06.01.2015.
 */
public class SqliteHelper extends SQLiteOpenHelper {

    private Context context;

    public SqliteHelper(Context context){
        super(
                context,
                context.getResources().getString(R.string.dbname),
                null,
                Integer.parseInt(context.getResources().getString(R.string.dbversion)));
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(String sql : context.getResources().getStringArray(R.array.dbcreate)) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
