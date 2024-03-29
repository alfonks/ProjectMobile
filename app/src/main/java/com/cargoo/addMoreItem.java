package com.cargoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class addMoreItem extends AppCompatActivity {

    private static final int image_id = 1;
    Button upload;
    ImageView previewimage;
    Uri selectedImage;
    private StorageReference mStorageRef;
    // Variables that gotten from Intent
    private int totalPrice; // To update totalPrice in coll. Orders
    private int totalWeight; // To update totalWeight in coll. Orders
    private int totalVolume; // To update totalVolume in coll. Orders
    private String orderID; // Get order ID from order form.
    private int prevItemPrice;
    private int intent_code;

    private int totalItemPrice;

    private Button btnUpload, btnAddItem, btnDone, btnCancel;
    private EditText edtNamaBarang, edtQuantity, edtWeight, edtWidth, edtLength, edtHeight;
    private Spinner spinUnit;
    private CheckBox cbFragile;

    private int weightPrice = 2000; // Untuk harga per kg
    private int volumePrice = 3000; // Untuk harga per m3
    private int itemPrice = 0;
    private boolean isFragile = false;

    private DatabaseReference dbOrder, dbItems;


    private String uploadImage(){
        if(selectedImage != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference mStorageRef = firebaseStorage.getReference();;
            String path = "orderimage/" + UUID.randomUUID().toString() + "." + getExtension(selectedImage);
            StorageReference ref = mStorageRef.child(path);
            ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
            return path;
        }else {
            return null;
        }
    }

    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mtm = MimeTypeMap.getSingleton();
        return mtm.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_more_item);

        Button back = findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        upload = findViewById(R.id.btnUploadimage);
        btnDone = findViewById(R.id.btnDone);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnCancel = findViewById(R.id.btnCancel);
        btnUpload = findViewById(R.id.btnUploadimage);

        edtNamaBarang = findViewById(R.id.edtNamaBarang);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtWeight = findViewById(R.id.edtWeight);
        edtWidth = findViewById(R.id.edtWidth);
        edtLength = findViewById(R.id.edtLength);
        edtHeight = findViewById(R.id.edtHeight);

        spinUnit = findViewById(R.id.spinUnit);

        cbFragile = findViewById(R.id.cbFragile);

        final ProgressBar progressBar4 = findViewById(R.id.progressBar4);

        dbOrder = FirebaseDatabase.getInstance().getReference().child("Orders");
        dbItems = FirebaseDatabase.getInstance().getReference().child("Items");

        Intent i = getIntent();
        totalPrice = i.getIntExtra("totalPrice", 0);
        totalWeight = i.getIntExtra("totalWeight", 0);
        totalVolume = i.getIntExtra("totalVolume", 0);
        orderID = i.getStringExtra("orderID");
        prevItemPrice = i.getIntExtra("itemPrice", 0);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galery, image_id);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ------------------ ITEM ATTRIBUTE --------------
                String itemID = dbItems.push().getKey();
                String itemName = edtNamaBarang.getText().toString();
                int quantity = Integer.parseInt(edtQuantity.getText().toString());
                String unit = spinUnit.getSelectedItem().toString();
                float width = (float) Math.ceil(Float.parseFloat(edtWidth.getText().toString())) * quantity;
                float length = (float) Math.ceil(Float.parseFloat(edtLength.getText().toString())) * quantity;
                float height = (float) Math.ceil(Float.parseFloat(edtHeight.getText().toString())) * quantity;
                float weight = (float) Math.ceil(Float.parseFloat(edtWeight.getText().toString())) * quantity;
                float volume = width * length * height;

                if (cbFragile.isChecked()) {
                    isFragile = true;
                }

                // -------------------- CALCULATION ------------------
                itemPrice = ((int) volume * volumePrice) + ((int) weight * weightPrice);

                totalItemPrice = prevItemPrice + itemPrice;

                if (isFragile) {
                    itemPrice += 10000;
                }

                totalPrice += itemPrice;
                totalWeight += (int) weight;
                totalVolume += (int) volume;

                String filepath = uploadImage();

                progressBar4.setVisibility(View.VISIBLE);

                // --------- INPUT "ITEM" TO DATABASE -----------
                Items dataItems = new Items(itemID, orderID, itemName, quantity, unit, width, length, height, weight, volume, itemPrice, isFragile, filepath);
                dbItems.child(itemID).setValue(dataItems);

                // -------- UPDATE "ORDER" COLLECTION ------------
                // Update total price, total weight, and total volume.
                dbOrder.child(orderID).child("totalWeight").setValue(totalWeight);
                dbOrder.child(orderID).child("totalVolume").setValue(totalVolume);
                dbOrder.child(orderID).child("totalPrice").setValue(totalPrice);
                dbOrder.child(orderID).child("itemPrice").setValue(totalItemPrice);

                // ---------------------- EVENT LISTENER --------------------------
                ValueEventListener writeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar4.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), "New item has added. Make a payment now. ", Toast.LENGTH_LONG).show();

                        Intent i = new Intent(addMoreItem.this, CheckoutActivity.class);
                        // Add extra orderID & others here
                        i.putExtra("orderID", orderID);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_LONG).show();
                    }
                };

                dbOrder.addValueEventListener(writeListener);

                totalPrice = 0;
                totalWeight = 0;
                totalVolume = 0;
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ------------------ ITEM ATTRIBUTE --------------
                String itemID = dbItems.push().getKey();
                String itemName = edtNamaBarang.getText().toString();
                int quantity = Integer.parseInt(edtQuantity.getText().toString());
                String unit = spinUnit.getSelectedItem().toString();
                float width = (float) Math.ceil(Float.parseFloat(edtWidth.getText().toString())) * quantity;
                float length = (float) Math.ceil(Float.parseFloat(edtLength.getText().toString())) * quantity;
                float height = (float) Math.ceil(Float.parseFloat(edtHeight.getText().toString())) * quantity;
                float weight = (float) Math.ceil(Float.parseFloat(edtWeight.getText().toString())) * quantity;
                float volume = width * length * height;

                if (cbFragile.isChecked()) {
                    isFragile = true;
                }

                // -------------------- CALCULATION ------------------
                itemPrice = ((int) volume * volumePrice) + ((int) weight * weightPrice);

                if (isFragile) {
                    itemPrice += 10000;
                }

                totalItemPrice = prevItemPrice + itemPrice;

                totalPrice += itemPrice;
                totalWeight += (int) weight;
                totalVolume += (int) volume;

                String filepath = uploadImage();
                progressBar4.setVisibility(View.VISIBLE);

                // --------- INPUT "ITEM" TO DATABASE -----------
                Items dataItems = new Items(itemID, orderID, itemName, quantity, unit, width, length, height, weight, volume, itemPrice, isFragile,filepath);
                dbItems.child(itemID).setValue(dataItems);

                // -------- UPDATE "ORDER" COLLECTION ------------
                // Update total price, total weight, and total volume.
                dbOrder.child(orderID).child("totalWeight").setValue(totalWeight);
                dbOrder.child(orderID).child("totalVolume").setValue(totalVolume);
                dbOrder.child(orderID).child("totalPrice").setValue(totalPrice);
                dbOrder.child(orderID).child("itemPrice").setValue(totalItemPrice);

                // ---------------------- EVENT LISTENER --------------------------
                ValueEventListener writeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar4.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(), "New item has added. Add new item now.", Toast.LENGTH_LONG).show();

                        // Go to add new item activity here !
                        Intent i = new Intent(addMoreItem.this, addMoreItem.class);
                        i.putExtra("totalPrice", totalPrice);
                        i.putExtra("totalWeight", totalWeight);
                        i.putExtra("totalVolume", totalVolume);
                        i.putExtra("orderID", orderID);
                        i.putExtra("itemPrice", totalItemPrice);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_LONG).show();
                    }
                };

                dbOrder.addValueEventListener(writeListener);

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(addMoreItem.this);
                dialog.setTitle("Cancellation of Add Item");
                dialog.setMessage("Are you sure to cancel adding new item?");

                dialog.setPositiveButton("Yes, still keep my previous order.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(addMoreItem.this, CheckoutActivity.class);
                        intent.putExtra("orderID", orderID);
                        startActivity(intent);
                        finish();
                    }
                });

                dialog.setNegativeButton("I want to cancel my order entirely", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do deletion of order and item collection here based on last orderID.

                        // ----------- ORDER DELETION ---------------
                        DatabaseReference qDeleteOrder = FirebaseDatabase.getInstance().getReference().child("Orders").child(orderID);
                        qDeleteOrder.removeValue();

                        // ----------- ITEMS DELETION ---------------
                        Query qDeleteItem = FirebaseDatabase.getInstance().getReference().child("Items")
                                .orderByChild("orderID")
                                .equalTo(orderID);

                        qDeleteItem.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Toast.makeText(addMoreItem.this, "Your order has deleted.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(addMoreItem.this, ActivityLoader.class);
                                startActivity(intent);
                                finish();

                                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                                    String itemID = postSnapshot.getKey();
                                    dataSnapshot.getRef().child(itemID).removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(addMoreItem.this);
        dialog.setTitle("Cancellation of Add Item");
        dialog.setMessage("Are you sure to cancel adding new item?");

        dialog.setPositiveButton("Yes, still keep my previous order.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(addMoreItem.this, CheckoutActivity.class);
                intent.putExtra("orderID", orderID);
                startActivity(intent);
                finish();
            }
        });

        dialog.setNegativeButton("I want to cancel my order entirely", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do deletion of order and item collection here based on last orderID.

                // ----------- ORDER DELETION ---------------
                DatabaseReference qDeleteOrder = FirebaseDatabase.getInstance().getReference().child("Orders").child(orderID);
                qDeleteOrder.removeValue();

                // ----------- ITEMS DELETION ---------------
                Query qDeleteItem = FirebaseDatabase.getInstance().getReference().child("Items")
                        .orderByChild("orderID")
                        .equalTo(orderID);

                qDeleteItem.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Toast.makeText(addMoreItem.this, "Your order has deleted.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(addMoreItem.this, ActivityLoader.class);
                        startActivity(intent);
                        finish();

                        for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            String itemID = postSnapshot.getKey();
                            dataSnapshot.getRef().child(itemID).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        dialog.show();
    }



}
