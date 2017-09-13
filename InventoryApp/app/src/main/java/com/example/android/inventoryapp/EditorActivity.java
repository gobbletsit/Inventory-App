package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryapp.data.ProductContract;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import static android.text.TextUtils.isEmpty;
import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;

/**
 * Created by gobov on 7/3/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    // Edit texts for edit view
    private EditText nameEditText;
    private EditText priceEditText;
    private EditText supplierEditText;
    private EditText quantityInStockEditText;

    // Product image holder
    private ImageView imageProduct;

    // Text views in detail view
    private TextView nameTextView;
    private TextView priceTextView;
    private TextView supplierTextView;
    private TextView inStockTextView;

    // Detail view  quantity buttons and order from supplier
    private Button incrementButton;
    private Button decrementButton;
    private Button orderButton;

    // To check if it's in the use
    private boolean productHasChanged = false;

    // Uri for an existing product
    private Uri currentProductUri;

    // Uri path for a current product image
    private Uri currentPhotoUri;

    // Request code constant for the image intent
    private static final int IMAGE_REQUEST_CODE = 1;

    // For changing the quantity
    private int quantity;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the data from the intent that was used to launch this activity
        Intent fromCatalogIntent = getIntent();
        currentProductUri = fromCatalogIntent.getData();

        // Finding all the relevant views
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        priceEditText = (EditText) findViewById(R.id.price_edit_text);
        imageProduct = (ImageView) findViewById(R.id.image_product_view);
        supplierEditText = (EditText) findViewById(R.id.supplier_edit_text);
        decrementButton = (Button) findViewById(R.id.decrement_button);
        incrementButton = (Button) findViewById(R.id.increment_button);
        orderButton = (Button) findViewById(R.id.order_button);
        quantityInStockEditText = (EditText) findViewById(R.id.in_stock_edit_text);
        nameTextView = (TextView) findViewById(R.id.name_value_text_view);
        priceTextView = (TextView) findViewById(R.id.price_value_text_view);
        supplierTextView = (TextView) findViewById(R.id.supplier_value_text_view);
        inStockTextView = (TextView)findViewById(R.id.in_stock_value_text_view);


        // To check if we're creating a new product or editing an existing one
        if (currentProductUri == null){
            // this is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.addNew));
            decrementButton.setVisibility(View.GONE);
            incrementButton.setVisibility(View.GONE);
            orderButton.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            supplierTextView.setVisibility(View.GONE);
            inStockTextView.setVisibility(View.GONE);
            invalidateOptionsMenu();
            edit();
        } else {
            // otherwise this is an existing product so change the app bar to say "Product details"
            setTitle(getString(R.string.productDetails));

            // Remove and disable edit texts if in detail mode mode
            nameEditText.setVisibility(View.GONE);
            priceEditText.setVisibility(View.GONE);
            quantityInStockEditText.setVisibility(View.GONE);
            supplierEditText.setVisibility(View.GONE);
            // Disable
            nameEditText.setEnabled(false);
            priceEditText.setEnabled(false);
            imageProduct.setEnabled(false);
            quantityInStockEditText.setEnabled(false);
            supplierEditText.setEnabled(false);

        }

        // -1 from quantity
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity = Integer.parseInt(quantityInStockEditText.getText().toString());
                if (quantity > 0){
                    quantity--;
                    quantityInStockEditText.setText(String.valueOf(quantity));
                    inStockTextView.setText(String.valueOf(quantity));
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.noProductsInStock), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // +1 from quantity
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity = Integer.parseInt(quantityInStockEditText.getText().toString());

                if (quantity >= 0){
                    quantity++;
                    inStockTextView.setText(String.valueOf(quantity));
                    quantityInStockEditText.setText(String.valueOf(quantity));
                }
            }
        });

        // An intent to order more from the supplier
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setData(Uri.parse("mailto:"));
                mailIntent.setType("text/plain");
                // Adding an hardcoded email to send to
                mailIntent.putExtra(Intent.EXTRA_EMAIL, "suppliers_mail@company_name.com");
                // Adding a subject with the product name
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.productName) + nameEditText.getText().toString() + ", at the price of " + priceEditText.getText().toString() + " €");
                startActivity(Intent.createChooser(mailIntent, "Send product mail"));
                if (mailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mailIntent);
                }
            }
        });

        // Starting the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating the custom menu from the resources
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                // Save method
                saveProduct();
                return true;
            case R.id.action_delete:
                // Delete confirmation and deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_edit:
                // Changing the bar title for edit view
                setTitle(getString(R.string.editProduct));
                // Edit mode (Removing and discovering views)
                edit();
                return true;
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!productHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Setting a dialog to warn the user and creating a
                // click listener to see if it's discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if (currentProductUri == null) {
            return null;
        }

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.NAME_COLUMN_PRODUCT,
                ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT,
                ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT,
                ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT,
                ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT};
        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()){
            // Getting the indexes of the columns that we need
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.NAME_COLUMN_PRODUCT);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT);
            int supplierColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT);

            // Extracting data using column indexes
            String name = cursor.getString(nameColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            String imageUri = cursor.getString(imageColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            String supplierString = cursor.getString(supplierColumnIndex);

            // Parsing to Uri from a string
            currentPhotoUri = Uri.parse(imageUri);

            if (quantity == 0){
                orderButton.setEnabled(false);
            }

            // Setting text on edit texts with the values from the database
            nameEditText.setText(name);
            priceEditText.setText(String.valueOf(price));
            // Using picasso to load the image
            Picasso.with(this).load(currentPhotoUri).placeholder(R.drawable.photo_add).fit().into(imageProduct);
            quantityInStockEditText.setText(String.valueOf(quantity));
            supplierEditText.setText(supplierString);

            // Same for text views in detail mode
            nameTextView.setText(name);
            priceTextView.setText(String.valueOf(price));
            supplierTextView.setText(supplierString);
            inStockTextView.setText(String.valueOf(quantity));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        quantityInStockEditText.setText("");
        supplierEditText.setText("");
    }

    // To set on edit texts to know if they're changed
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };


    private void saveProduct(){

        // Getting the values from user input
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String imageUriString = "";
        String quantityString = quantityInStockEditText.getText().toString().trim();
        String supplierString = supplierEditText.getText().toString().trim();

        // Checking if there's value, if not return
        if (currentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(imageUriString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString)){
            Toast.makeText(this, getString(R.string.missingInfo), Toast.LENGTH_SHORT).show();
            return;
        }

        // Double checking even for edit mode
        if (!saveCheck()){
            Toast.makeText(this, getString(R.string.missingInfo), Toast.LENGTH_LONG).show();
            return;
        }

        // Checking and saving the image path
        if (currentPhotoUri != null){
            imageUriString = currentPhotoUri.toString();
        } else {
            imageUriString = "no image";
        }

        // Default value, and to parse if there's input
        float price = 0.0f;
        if (!TextUtils.isEmpty(priceString)){
            price = Float.parseFloat(priceString);
        }

        // Checking for input to parse
        if (!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString);
        }

        // Creating a content values object to store new values
        ContentValues values = new ContentValues();

        // Storing the values
        values.put(ProductContract.ProductEntry.NAME_COLUMN_PRODUCT, nameString);
        values.put(ProductContract.ProductEntry.PRICE_COLUMN_PRODUCT, price);
        values.put(ProductContract.ProductEntry.IMAGE_COLUMN_PRODUCT, imageUriString);
        values.put(ProductContract.ProductEntry.QUANTITY_COLUMN_PRODUCT, quantity);
        values.put(ProductContract.ProductEntry.SUPPLIER_COLUMN_PRODUCT, supplierString);

        // Checking if its new or existing product
        if (currentProductUri == null) {
            // inserting a new product
            Uri insertedRow = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            // check if it's added successfully
            if (insertedRow == null){
                Toast.makeText(this, getString(R.string.addError), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.addSuccess), Toast.LENGTH_SHORT).show();
            }
        } else {
            // existing product needs an update so update
            int rowsUpdated = getContentResolver().update(currentProductUri, values, null, null);

            // check if it's updated successfully
            if (rowsUpdated == 0){
                Toast.makeText(this, getString(R.string.updateError), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.updateSuccess), Toast.LENGTH_SHORT).show();
            }
        }

        // Finish activity after saving
        finish();

    }

    // To open the image
    private void openImageSelector() {

        Intent imageIntent;

        // to get the image with an intent
        if (Build.VERSION.SDK_INT < 19) {
            imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            imageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            imageIntent.addCategory(Intent.CATEGORY_OPENABLE);

        }

        // Setting type on an intent
        imageIntent.setType("image/*");
        // Starting with a request code
        startActivityForResult(Intent.createChooser(imageIntent, "Select Image"), IMAGE_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if it's this specific code, then get the data
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // getting the data
                currentPhotoUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + currentPhotoUri.toString());

                // setting the image
                //imageProduct.setImageURI(currentPhotoUri);

                Picasso.with(this).load(currentPhotoUri).placeholder(R.drawable.photo_add).fit().into(imageProduct);

            }

        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If nothing has changed then proceed with getting back
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog and a listener
        // for the user to choose actions to use
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // delete if it's clicked
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        // to delete a single pet, checking if it's a single pet
        if (currentProductUri != null){
            // delete the product row with content resolver
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            // If the delete was successful
            if (rowsDeleted == 0){
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();

    }

    // To be called when in edit mode
    private void edit(){

        // Making edit texts visible for edit mode
        nameEditText.setVisibility(View.VISIBLE);
        priceEditText.setVisibility(View.VISIBLE);
        quantityInStockEditText.setVisibility(View.VISIBLE);
        supplierEditText.setVisibility(View.VISIBLE);

        // Enabling views for edit mode
        nameEditText.setEnabled(true);
        priceEditText.setEnabled(true);
        imageProduct.setEnabled(true);
        quantityInStockEditText.setEnabled(true);
        supplierEditText.setEnabled(true);

        // Removing the views for edit mode
        nameTextView.setVisibility(View.GONE);
        priceTextView.setVisibility(View.GONE);
        supplierTextView.setVisibility(View.GONE);
        inStockTextView.setVisibility(View.GONE);
        incrementButton.setVisibility(View.GONE);
        decrementButton.setVisibility(View.GONE);
        orderButton.setVisibility(View.GONE);
        orderButton.setVisibility(View.GONE);

        // Setting listeners on edits to know if something has changed
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        imageProduct.setOnTouchListener(mTouchListener);
        quantityInStockEditText.setOnTouchListener(mTouchListener);
        supplierEditText.setOnTouchListener(mTouchListener );

        // Listener to open the image selection
        imageProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    // To double check if the input's are empty for edit mode
    private boolean saveCheck(){
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityInStockEditText.getText().toString().trim();
        String supplier = supplierEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplier) || currentPhotoUri == null){
            return false;
        }
        return true;
    }
}
