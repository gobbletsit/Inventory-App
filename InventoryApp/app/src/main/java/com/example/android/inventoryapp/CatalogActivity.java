package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;

import com.example.android.inventoryapp.data.ProductContract;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    // to set on the list view
    ProductCursorAdapter mProductCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // To add a new product in editor activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Uri == null , so new product
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Finding the list view
        ListView productListView = (ListView) findViewById(R.id.list);
        // setting an empty view on it
        View emptyListView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyListView);

        // initializing the adapter
        mProductCursorAdapter = new ProductCursorAdapter(this, null);

        // setting the adapter on the list view
        productListView.setAdapter(mProductCursorAdapter);

        // setting the listener on an list item
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Intent editorIntent = new Intent(CatalogActivity.this, EditorActivity.class);

                // getting the specific product uri with an id
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                // setting the specific Uri on an intent
                editorIntent.setData(currentProductUri);

                // Uri != null, so edit existing product
                startActivity(editorIntent);
            }
        });

        // start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the custom made menu
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // delete everything
            case R.id.action_delete_all_entries:
                // delete the whole table
                deleteAllProducts();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllProducts (){
        // deleting and getting the number of rows deleted
        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from product database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Creating a loader with these attributes
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.NAME_COLUMN_PRODUCT,
                ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT,
                ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT};

        return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // update with the new cursor containing updated data
        mProductCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //  called when the data needs to be deleted
        mProductCursorAdapter.swapCursor(null);
    }


}
