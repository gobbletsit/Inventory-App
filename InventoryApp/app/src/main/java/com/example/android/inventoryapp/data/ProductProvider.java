package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by gobov on 7/3/2017.
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    // Uri matcher code for the whole table
    private static final int PRODUCTS = 100;

    // Uri matcher code for the single product
    private static final int PRODUCT_ID = 101;

    private ProductDbHelper mDbHelper;

    // To match a Uri for a corresponding code, NO_MATCH as a default
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // This is run the first time anything is called from this class
    static {
        // adding a Uri for the whole table
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT, PRODUCTS);
        // adding a Uri for the single product
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }


    @Override
    public boolean onCreate() {

        // global so it can be referenced from other methods
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }


    // Performs a query for the given Uri
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Getting the readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // For holding the query result
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = mUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                // For the whole table
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the single product, extracting the id from the Uri and setting the
                // selection and selection args to query, "=?" represents the element in
                // the selection args which will fill the ? with an id number
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // Will contain a query with an id, and a specific row of the table
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        // Set notification URI on the cursor, to know for which Uri is created
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // Returns the MIME typw of data for the content Uri
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                // returning a list type MIME
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                // returning an item type MIME
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown Uri " + uri + " with match " + match);
        }
    }

    // insert data with content values
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // calling a method, it will always be just one insert product
                return insertSingleProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // inserting a single product in a database with content values, returning a Uri with that
    // specific row
    private Uri insertSingleProduct (Uri uri, ContentValues contentValues){

        // getting the values
        String name = contentValues.getAsString(ProductContract.ProductEntry.NAME_COLUMN_PRODUCT);
        Float price = contentValues.getAsFloat(ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT);
        String image = contentValues.getAsString(ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT);
        String supplier = contentValues.getAsString(ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT);

        // checking if the values are null
        if (name == null){
            throw new IllegalArgumentException("Product requires a name");
        }

        if (price != null && price < 0){
            throw new IllegalArgumentException("Product requires a price");
        }

        if (image == null){
            throw new IllegalArgumentException("Product requires an image");
        }

        if (supplier == null){
            throw new IllegalArgumentException("Product requires a supplier information");
        }

        // getting a database to insert a new row
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // inserting a new row with the given values
        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, contentValues);

        // checking if the insertion was successful
        if (id == -1) {
            Log.v(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // notify that the data has been changed
        getContext().getContentResolver().notifyChange(uri, null);

        // return the new Uri with appended ID
        return ContentUris.withAppendedId(uri, id);


    }

    // Delete the data at the given selection and selection arguments
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // to delete from database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // to track the number of rows that were deleted
        int rowsDeleted;

        final int match = mUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                // deleting all rows from the database
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // delete a single row given by the id in the Uri
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // deleting a single row with the given values
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // if deleted notify change data
        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    // Updates the data with new content values
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        // to get the uris
        final int match = mUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                // single update method from below with nothing changed
                return updateSingleProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // extracting the id from Uri to know which row to update
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // updating product with this specific selection and selection args
                return updateSingleProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // To update a single product
    private int updateSingleProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // Checking if the key is present and not null
        if (contentValues.containsKey(ProductContract.ProductEntry.NAME_COLUMN_PRODUCT)){
            String name = contentValues.getAsString(ProductContract.ProductEntry.NAME_COLUMN_PRODUCT);
            if (name == null){
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (contentValues.containsKey(ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT)){
            Float price = contentValues.getAsFloat(ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT);
            if (price != 0 && price < 0){
                throw new IllegalArgumentException("Product requires a price");
            }
        }

        if (contentValues.containsKey(ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT)){
            String image = contentValues.getAsString(ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT);
            if (image == null){
                throw new IllegalArgumentException("Product requires an image");
            }
        }

        if (contentValues.containsKey(ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT)){
            String supplier = contentValues.getAsString(ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT);
            if (supplier == null) {
                throw new IllegalArgumentException("Product requires a supplier information");
            }
        }

        // to know if there's no values, to know if to update
        if (contentValues.size() == 0 ){
            return 0;
        }

        // to update a single product
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // updating with this values and getting the row that is affected
        int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // checking if update is successful to notify the data changes
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;

    }
}
