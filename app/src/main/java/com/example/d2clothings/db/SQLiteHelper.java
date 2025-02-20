package com.example.d2clothings.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.d2clothings.Product;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE cart_item (\n" +
                "    id           TEXT PRIMARY KEY \n" +
                "                         NOT NULL,\n" +
                "    title        TEXT    NOT NULL,\n" +
                "   price      TEXT    NOT NULL,\n" +
                "   qty      TEXT    NOT NULL,\n" +
                "    url TEXT    NOT NULL\n" +
                ");\n");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cart_item");
        onCreate(db);
    }

}