package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;


/**
 * Created by gobov on 7/3/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }


    // Create a new list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // inflate the list_item view
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    // Binding the data to the view
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // finding the views
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        Button sale = view.findViewById(R.id.sale);

        // getting column indexes of the attributes that we need from cursor
        int idColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.NAME_COLUMN_PRODUCT);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT);

        // getting the values  with indexes from the cursor
        int idProduct = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        Float price = cursor.getFloat(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        // Uri of the current product with an id
        final Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, idProduct);

        // listener on the sale button to decrease quantity
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // to update and notify the data change
                ContentResolver contentResolver = view.getContext().getContentResolver();
                // to update new values
                ContentValues values = new ContentValues();
                if (quantity > 0){
                    // quantity decrementation
                    final int newQuantity = quantity - 1;
                    // inserting new value
                    values.put(ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT, newQuantity);
                    // updating new values for the current product
                    contentResolver.update(currentProductUri, values, null, null);
                    // notifying data change
                    context.getContentResolver().notifyChange(currentProductUri, null);
                } else {
                    Toast.makeText(context, R.string.noProductsInStock, Toast.LENGTH_SHORT).show();
                }
            }
        });
        // so the list view item can be clicked
        sale.setFocusable(false);


        // setting the text on the views
        if (quantity == 0){
            quantityTextView.setText(R.string.outOfStock);
        } else {
            quantityTextView.setText(String.valueOf(quantity));
        }

        nameTextView.setText(name);
        priceTextView.setText(String.valueOf(price) + " €");

    }
}
