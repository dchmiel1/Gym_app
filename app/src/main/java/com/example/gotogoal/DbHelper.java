package com.example.gotogoal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "GymAppDb.db";
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + DbNames.TABLE_NAME + "(" +
                    DbNames._ID + " INTEGER PRIMARY KEY, " +
                    DbNames.COLUMN_NAME_DATE + " TEXT, " +
                    DbNames.COLUMN_NAME_EXERCISE + " TEXT, " +
                    DbNames.COLUMN_NAME_REPS + " INTEGER, " +
                    DbNames.COLUMN_NAME_KG_ADDED + " REAL, " +
                    DbNames.COLUMN_NAME_ONE_REP + " REAL);";
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + DbNames.TABLE_NAME;
    private MainActivity mainActivity;

    public DbHelper(Context c, MainActivity mainActivity){
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    public void insertSet (String exName, int reps, double kgAdded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbNames.COLUMN_NAME_DATE, new SimpleDateFormat("yyyy MM dd", Locale.getDefault()).format(MainActivity.date));
        values.put(DbNames.COLUMN_NAME_EXERCISE, exName);
        values.put(DbNames.COLUMN_NAME_REPS, reps);
        values.put(DbNames.COLUMN_NAME_KG_ADDED, kgAdded);
        if(exName.equals("'Pull up'") || exName.equals("'Dip'") && reps <= 20) {
            Cursor c = getLastWeight();
            c.moveToNext();
            values.put(DbNames.COLUMN_NAME_ONE_REP, ((kgAdded +c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED))) / ((double)MainActivity.multiplier[reps]/100)) - c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED)));
        }else if(reps <= 20)
            values.put(DbNames.COLUMN_NAME_ONE_REP, kgAdded/((double)MainActivity.multiplier[reps]/100));
        else
            values.put(DbNames.COLUMN_NAME_ONE_REP, kgAdded);
        db.insert(DbNames.TABLE_NAME, null, values);
    }

    public void insertOrUpdateWeight(double kgs, int cms){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String date = new SimpleDateFormat("yyyy MM dd", Locale.getDefault()).format(new Date());
        values.put(DbNames.COLUMN_NAME_DATE, date);
        values.put(DbNames.COLUMN_NAME_EXERCISE, "weight");
        values.put(DbNames.COLUMN_NAME_REPS, cms);
        values.put(DbNames.COLUMN_NAME_KG_ADDED, kgs);
        values.put(DbNames.COLUMN_NAME_ONE_REP, 0);
        if(isWeightThatDate(date)) {
            db.update(DbNames.TABLE_NAME, values,  "date = ? and exercise = 'weight'", new String[]{date});
        }
        else{
            insertSet("weight", cms, kgs);
        }
    }

    public void updateSet(int id, int reps, double kgs, String exName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbNames.COLUMN_NAME_REPS, reps);
        values.put(DbNames.COLUMN_NAME_KG_ADDED, kgs);
        if(exName.equals("'Pull up'") || exName.equals("'Dip'") && reps <= 20) {
            Cursor c = getLastWeight();
            c.moveToNext();
            values.put(DbNames.COLUMN_NAME_ONE_REP, ((kgs +c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED))) / ((double)MainActivity.multiplier[reps]/100)) - c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED)));
        }else if(reps <= 20)
            values.put(DbNames.COLUMN_NAME_ONE_REP, kgs/((double)MainActivity.multiplier[reps]/100));
        else
            values.put(DbNames.COLUMN_NAME_ONE_REP, kgs);
        db.update(DbNames.TABLE_NAME, values, " _id = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from " + DbNames.TABLE_NAME + " where _id="+id+"", null );
    }

    public Cursor getExercisesByDate(String date){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select distinct " + DbNames.COLUMN_NAME_EXERCISE +
                        " from " + DbNames.TABLE_NAME +
                        " where date =" + "'" + date + "'" + " and " + DbNames.COLUMN_NAME_EXERCISE + " != 'weight'"
                , null);
    }

    public Cursor getSetsByDateAndExercise(String date, String exercise){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select " + DbNames.COLUMN_NAME_EXERCISE + ", " + DbNames.COLUMN_NAME_REPS + ", " + DbNames.COLUMN_NAME_KG_ADDED +", "+ DbNames._ID+
                " from " + DbNames.TABLE_NAME + " " +
                "where " + DbNames.COLUMN_NAME_DATE + " =" + "'" + date + "'" + " and " + DbNames.COLUMN_NAME_EXERCISE + " = " + "'" + exercise +"'" + "", null);
    }

    public Cursor getLastWeight(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select " + DbNames.COLUMN_NAME_KG_ADDED + ", " + DbNames.COLUMN_NAME_REPS +
                " from " + DbNames.TABLE_NAME +
                " where " + DbNames.COLUMN_NAME_EXERCISE + " = 'weight' " +
                "order by " + DbNames.COLUMN_NAME_DATE + " DESC", null);
    }

    public double getBestRep(String exName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select " + DbNames.COLUMN_NAME_KG_ADDED +
                " from " + DbNames.TABLE_NAME +
                " where " + DbNames.COLUMN_NAME_EXERCISE + " = " + exName +
                "order by " + DbNames.COLUMN_NAME_KG_ADDED + " DESC", null);
        if(c.getCount() > 0) {
            c.moveToNext();
            return c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_KG_ADDED));
        }
        else
            return -1;
    }

    public double getCalculatedLastOneRep(String exName){
        SQLiteDatabase db = this.getReadableDatabase();
        String lastDate;
        Cursor dateCursor;
        dateCursor = db.rawQuery("select " + DbNames.COLUMN_NAME_DATE +
                " from " + DbNames.TABLE_NAME +
                " where " + DbNames.COLUMN_NAME_EXERCISE + " = " + exName +
                "order by " + DbNames.COLUMN_NAME_DATE + " DESC", null);
        if(dateCursor.getCount() > 0){
            dateCursor.moveToNext();
            lastDate = dateCursor.getString(dateCursor.getColumnIndexOrThrow(DbNames.COLUMN_NAME_DATE));
        }else{
            return -1;
        }
        Cursor c = db.rawQuery("select " + DbNames.COLUMN_NAME_ONE_REP +
                " from " + DbNames.TABLE_NAME +
                " where " + DbNames.COLUMN_NAME_EXERCISE + " = " + exName + " and " + DbNames.COLUMN_NAME_DATE + " = " + "'" +lastDate+ "'" +
                " order by " + DbNames.COLUMN_NAME_ONE_REP + " DESC", null);
        if(c.getCount() > 0) {
            c.moveToNext();
            return c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_ONE_REP));
        }
        else
            return -1;
    }

    public double getCalculatedBestOneRep(String exName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select " + DbNames.COLUMN_NAME_ONE_REP +
                " from " + DbNames.TABLE_NAME +
                " where " + DbNames.COLUMN_NAME_EXERCISE + " = " + exName +
                "order by " + DbNames.COLUMN_NAME_ONE_REP + " DESC", null);
        if(c.getCount() > 0) {
            c.moveToNext();
            return c.getDouble(c.getColumnIndexOrThrow(DbNames.COLUMN_NAME_ONE_REP));
        }
        else
            return -1;
    }

    public void deleteById (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("sets_table",
                "_id = ? ",
                new String[] { Integer.toString(id) });
    }

    public void deleteByDateAndExercise(String exName){
        SQLiteDatabase db = this.getWritableDatabase();
        String date = new SimpleDateFormat("yyyy MM dd", Locale.getDefault()).format(MainActivity.date);
        db.delete("sets_table",
                " exercise = ? AND date = ?",
                new String[] { exName, date });
        mainActivity.checkWorkout();
    }

    private boolean isWeightThatDate(String date){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * " +
                " from " + DbNames.TABLE_NAME + " " +
                " where " + DbNames.COLUMN_NAME_DATE + " =" + "'" + date + "' and " + DbNames.COLUMN_NAME_EXERCISE + " = 'weight'", null);
        return c.getCount() > 0;
    }
}
