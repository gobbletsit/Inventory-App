package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gobov on 7/3/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    // CONSTANT FOR DATABASE NAME
    private static final String DATABASE_NAME = "inventory.db";

    // DATABASE VERSION, IF CHANGED THE NUMBER WILL INCREASE
    public static final int DATABASE_VERSION = 1;

    public ProductDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // CREATE TABLE STRING WITH SQL COMMANDS
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE "
                + ProductContract.ProductEntry.TABLE_NAME + " ("
                + ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.ProductEntry.NAME_COLUMN_PRODUCT + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT + " FLOAT(24) NOT NULL, "
                + ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT + " INTEGER NOT NULL, "
                + ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT + " TEXT DEFAULT 'UNKNOWN SUPPLIER' );";
        // Execute the given string of SQL commands
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);

    }

    // if updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        onCreate(db);
    }
}
