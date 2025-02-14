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
        db.execSQL("CREATE TABLE products (\n" +
                "    id           TEXT PRIMARY KEY,\n" +
                "    title        TEXT NOT NULL,\n" +
                "    description  TEXT NOT NULL,\n" +
                "    price        INTEGER NOT NULL,\n" + // Changed from TEXT to INTEGER
                "    qty          TEXT NOT NULL,\n" +
                "    url          TEXT NOT NULL\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

    public void insertProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", product.getId());
        values.put("title", product.getTitle());
        values.put("description", product.getDescription());
        values.put("price", product.getPrice()); // Stored as INTEGER
        values.put("qty", product.getQty());
        values.put("url", product.getImageUrl());

        db.insert("products", null, values);
        db.close();
    }

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products", null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // title
                        cursor.getString(2),  // description
                        cursor.getLong(3),    // price (now correctly retrieved as Long)
                        cursor.getString(4),  // qty
                        cursor.getString(5)   // url
                );
                products.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return products;
    }
}
