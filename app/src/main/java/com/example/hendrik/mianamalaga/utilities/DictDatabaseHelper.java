package com.example.hendrik.mianamalaga.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.hendrik.mianamalaga.Constants;

import java.io.Console;
import java.io.File;

public class DictDatabaseHelper extends CustomSQLiteOpenHelper {

    public static final String DICT_TABLE_NAME = "dictionary";
    public static final String DICT_COLUMN_ID = "id";
    public static final String DICT_COLUMN_FIRST_LANGUAGE = "firstLanguage";
    public static final String DICT_COLUMN_SECOND_LANGUAGE = "secondLanguage";
    public static final String DICT_COLUMN_FIRST_AUDIO = "audioFile";
    //public static final String DICT_COLUMN_FIRST_AUDIO = "firstAudio";
    //public static final String DICT_COLUMN_SECOND_AUDIO = "secondAudio";

    public DictDatabaseHelper(String databasePath ){
        super(databasePath, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + DICT_TABLE_NAME +
                        " (" + DICT_COLUMN_ID + " integer primary key, " +
                               DICT_COLUMN_FIRST_LANGUAGE + " text " + "NOT NULL," +
                               DICT_COLUMN_SECOND_LANGUAGE  + " text " + "NOT NULL," +
                               DICT_COLUMN_FIRST_AUDIO  + " blob " + "UNIQUE ON CONFLICT FAIL," +
                               "PRIMARY KEY (" + DICT_COLUMN_FIRST_LANGUAGE +"," + DICT_COLUMN_SECOND_LANGUAGE + ")" +
      //                         DICT_COLUMN_FIRST_AUDIO  + " blob, " +
      //                         DICT_COLUMN_SECOND_AUDIO  + " blob " +
                        " );"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DICT_TABLE_NAME );
        onCreate(db);
    }

    //public boolean insertContact (String firstLang, String secondLang, byte[] firstAudio, byte[] secondAudio ) {
    public boolean insertContact (String firstLang, String secondLang, byte[] audioByteArray ) {

        boolean returnValue = false;

        SQLiteDatabase db = this.getWritableDatabase();

        //onCreate(db);

        db.beginTransaction();

        try{
            String sqlString   =   "REPLACE INTO " + DICT_TABLE_NAME                                // TODO this was INSERT INTO before - verify if this is possible
                    + " ( " + DICT_COLUMN_FIRST_LANGUAGE
                    + ", " + DICT_COLUMN_SECOND_LANGUAGE
                    + ", " + DICT_COLUMN_FIRST_AUDIO
        //            + ", " + DICT_COLUMN_SECOND_AUDIO
        //            + " ) VALUES(?,?,?,?)";
                    + " ) VALUES(?,?,?)";

            SQLiteStatement insertStatement = db.compileStatement( sqlString );
            insertStatement.clearBindings();
            insertStatement.bindString(1, firstLang);
            insertStatement.bindString(2, secondLang);
            insertStatement.bindBlob(3, audioByteArray);
            //insertStatement.bindBlob(4, secondAudio);
            //insertStatement.executeInsert();
            if( insertStatement.executeInsert() == -1 ){                                            // Insert failed because audioByteArray already exists
                insertStatement.bindBlob(3, null );
                insertStatement.executeInsert();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            returnValue = true;

        } catch(Exception e){
            e.printStackTrace();
            returnValue = false;
        }

        /*
        ContentValues contentValues = new ContentValues();
        contentValues.put(DICT_COLUMN_FIRST_LANGUAGE, firstLang );
        contentValues.put(DICT_COLUMN_SECOND_LANGUAGE, secondLang );
        contentValues.put(DICT_COLUMN_FIRST_AUDIO, firstAudio);
        contentValues.put(DICT_COLUMN_SECOND_AUDIO, secondAudio );

        db.insert(DICT_TABLE_NAME, null, contentValues);

         */
        return returnValue;
    }

    public Cursor getData( String firstLang ) {

        Cursor resultCursor = null;

        try{
            SQLiteDatabase db = this.getReadableDatabase();
            resultCursor =  db.rawQuery( "SELECT * FROM " + DICT_TABLE_NAME + " WHERE " + DICT_COLUMN_FIRST_LANGUAGE  + " LIKE " + "'" + firstLang + "%';", null );
        } catch ( Exception exception){
            Log.e(Constants.TAG, "Failed to get data from database!");
            exception.printStackTrace();
        }

        return resultCursor;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DICT_TABLE_NAME,
                DICT_COLUMN_ID + " = ? ",
                new String[] { Integer.toString(id) });
    }

    public byte[] getBytes( int id) {

        byte[] returnArray = null;

        try {

            String selectQuery = "SELECT  I." + DICT_COLUMN_FIRST_AUDIO
                              + " FROM " + DICT_TABLE_NAME + " I WHERE I." + DICT_COLUMN_ID + " = ?";

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, new String[]{ String.valueOf(id) });


            if ( !cursor.isClosed() && cursor.moveToFirst() && cursor.getCount() > 0 ) {

                if ( cursor.getBlob( cursor.getColumnIndex( DICT_COLUMN_FIRST_AUDIO )) != null)
                {
                    returnArray = cursor.getBlob( cursor.getColumnIndex( DICT_COLUMN_FIRST_AUDIO ) );
                }
                cursor.close();
                if (db != null && db.isOpen())
                    db.close();
            }
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
            System.gc();                        // ask JVM to do Garbage collection
        }

        return returnArray;
    }


    public void dropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DICT_TABLE_NAME );
    }
}
