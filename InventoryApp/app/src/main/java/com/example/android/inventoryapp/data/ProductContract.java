package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gobov on 7/3/2017.
 */

public class ProductContract {

    // CONSTANT PATHS TO CREATE THE FINAL CONTENT URI
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    public static final String PATH_PRODUCT = "inventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private ProductContract(){}

    public static class ProductEntry implements BaseColumns {

        // Final Uri with appended paths from above
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        // MIME type for the list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        // MIME type for the single product
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        // TABLE AND COLUMN NAMES CONSTANTS TO USE
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String NAME_COLUMN_PRODUCT = "name";
        public static final String PRICE_COLUMN_PRODUCT = "price";
        public static final String IMAGE_COLUMN_PRODUCT = "image";
        public static final String QUANTITY_COLUMN_PRODUCT = "quantity";
        public static final String SUPPLIER_COLUMN_PRODUCT = "supplier";


    }
}
