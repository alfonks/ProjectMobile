package com.cargoo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrderFormActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final int image_id = 1;
    Button upload, btnDone;
    TextView imagename;
    ImageView previewimage;

    private EditText edtNamaPengirim, edtTelpPengirim, edtEmailPengirim, edtAlamatPengirim, edtProvinsiPengirim, edtKotaPengirim, edtKecamatanPengirim, edtKodePosPengirim;
    private EditText edtNamaPenerima, edtTelpPenerima, edtEmailPenerima, edtAlamatPenerima, edtProvinsiPenerima, edtKotaPenerima, edtKecamatanPenerima, edtKodePosPenerima;
    private EditText edtTglPengiriman;

    private EditText edtNamaBarang, edtQuantity, edtWeight, edtWidth, edtLength, edtHeight;
    private Spinner spinUnit;
    private CheckBox cbFragile;

    private boolean isFragile = false;

    DatabaseReference dbOrder, dbItems, dbUsers;

    private FirebaseAuth fbAuth;

    private FirebaseUser fbUser = fbAuth.getInstance().getCurrentUser();
    private String fbUserId = fbUser.getUid();

    private int weightPrice = 15000; // Untuk harga per kg
    private int volumePrice = 20000; // Untuk harga per m3
    private int deliveryCost = 5000; // Untuk harga per km
    private int itemPrice = 0;

    private int totalPrice = 0;
    private int totalWeight = 0;
    private int totalVolume = 0;


    // Initiate Form Auto-Fill
    private String refNamaPengirim, refTelpPengirim, refEmailPengirim, refAlamatPengirim, refProvinsiPengirim, refKotaPengirim, refKecamatanPengirim, refKodePosPengirim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_form);
        upload = findViewById(R.id.uploadimage);
        previewimage = findViewById(R.id.previewimage);

        edtNamaPengirim = findViewById(R.id.edtNamaPengirim);
        edtTelpPengirim = findViewById(R.id.edtTelpPengirim);
        edtEmailPengirim = findViewById(R.id.edtEmailPengirim);

        edtNamaPenerima = findViewById(R.id.edtNamaPenerima);
        edtTelpPenerima = findViewById(R.id.edtTelpPenerima);
        edtEmailPenerima = findViewById(R.id.edtEmailPenerima);

        edtAlamatPengirim = findViewById(R.id.edtAlamatPengirim);
        edtProvinsiPengirim = findViewById(R.id.edtProvinsiPengirim);
        edtKotaPengirim = findViewById(R.id.edtKotaPengirim);
        edtKecamatanPengirim = findViewById(R.id.edtKecamatanPengirim);
        edtKodePosPengirim = findViewById(R.id.edtKodePosPengirim);

        edtAlamatPenerima = findViewById(R.id.edtAlamatPenerima);
        edtProvinsiPenerima = findViewById(R.id.edtProvinsiPenerima);
        edtKotaPenerima = findViewById(R.id.edtKotaPenerima);
        edtKecamatanPenerima = findViewById(R.id.edtKecamatanPenerima);
        edtKodePosPenerima = findViewById(R.id.edtKodePosPenerima);

        edtNamaBarang = findViewById(R.id.edtNamaBarang);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtWeight = findViewById(R.id.edtWeight);
        edtWidth = findViewById(R.id.edtWidth);
        edtLength = findViewById(R.id.edtLength);
        edtHeight = findViewById(R.id.edtHeight);

        spinUnit = findViewById(R.id.spinUnit);

        cbFragile = findViewById(R.id.cbFragile);

        // Automated-fill form
        //String authID = fbAuth.getUid().toString();
        //edtNamaPengirim.setText("oy");

        final ProgressBar progressBar3 = findViewById(R.id.progressBar3);

        fbAuth = FirebaseAuth.getInstance();

        dbOrder = FirebaseDatabase.getInstance().getReference().child("Orders");
        dbItems = FirebaseDatabase.getInstance().getReference().child("Items");

        // READ USER'S INFO
        dbUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        Query q1 = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("userID")
                .equalTo(fbUserId);

        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                refNamaPengirim = dataSnapshot.child(fbUserId).child("name").getValue().toString();
                refTelpPengirim = dataSnapshot.child(fbUserId).child("phone").getValue(String.class);
                refEmailPengirim = dataSnapshot.child(fbUserId).child("email").getValue(String.class);
                refAlamatPengirim = dataSnapshot.child(fbUserId).child("address").child("address").getValue(String.class);
                refProvinsiPengirim = dataSnapshot.child(fbUserId).child("address").child("province").getValue(String.class);
                refKotaPengirim = dataSnapshot.child(fbUserId).child("address").child("city").getValue(String.class);
                refKecamatanPengirim = dataSnapshot.child(fbUserId).child("address").child("district").getValue(String.class);
                refKodePosPengirim = String.valueOf(dataSnapshot.child(fbUserId).child("address").child("zipcode").getValue(Integer.class));

                edtNamaPengirim.setText(refNamaPengirim);
                edtEmailPengirim.setText(refEmailPengirim);
                edtTelpPengirim.setText(refTelpPengirim);
                edtAlamatPengirim.setText(refAlamatPengirim);
                edtProvinsiPengirim.setText(refProvinsiPengirim);
                edtKotaPengirim.setText(refKotaPengirim);
                edtKecamatanPengirim.setText(refKecamatanPengirim);
                edtKodePosPengirim.setText(refKodePosPengirim);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getCode(), Toast.LENGTH_LONG).show();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galery, image_id);
            }
        });

        edtTglPengiriman = findViewById(R.id.edtTglPengiriman);
        edtTglPengiriman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "Pick the Delivery Date");
            }
        });

        btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String orderID = dbOrder.push().getKey();
                String userID = fbAuth.getCurrentUser().getUid(); // Retrieve from FB Authentication

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String orderDate = dateFormat.format(date);

                String orderStatus = "Pending";

                String NamaPengirim = edtNamaPengirim.getText().toString();
                String TelpPengirim = edtTelpPengirim.getText().toString();
                String EmailPengirim = edtEmailPengirim.getText().toString();

                String NamaPenerima = edtNamaPenerima.getText().toString();
                String TelpPenerima = edtTelpPenerima.getText().toString();
                String EmailPenerima = edtEmailPenerima.getText().toString();

                String AlamatPengirim = edtAlamatPengirim.getText().toString();
                String ProvinsiPengirim = edtProvinsiPengirim.getText().toString();
                String KotaPengirim = edtKotaPengirim.getText().toString();
                String KecamatanPengirim = edtKecamatanPengirim.getText().toString();
                String KodePosPengirim = edtKodePosPengirim.getText().toString();
                String fullAddrPengirim = AlamatPengirim + ", " + KecamatanPengirim + ", " + KotaPengirim + ", " + ProvinsiPengirim + " " + KodePosPengirim;

                String AlamatPenerima = edtAlamatPenerima.getText().toString();
                String ProvinsiPenerima = edtProvinsiPenerima.getText().toString();
                String KotaPenerima = edtKotaPenerima.getText().toString();
                String KecamatanPenerima = edtKecamatanPenerima.getText().toString();
                String KodePosPenerima = edtKodePosPenerima.getText().toString();
                String fullAddrPenerima = AlamatPenerima + ", " + KecamatanPenerima + ", " + KotaPenerima + ", " + ProvinsiPenerima + " " + KodePosPenerima;

                String tglPengiriman = edtTglPengiriman.getText().toString();

                // ------------------ ITEM ATTRIBUTE --------------
                String itemID = dbItems.push().getKey();
                String itemName = edtNamaBarang.getText().toString();
                int quantity = Integer.parseInt(edtQuantity.getText().toString());
                String unit = spinUnit.getSelectedItem().toString();
                float width = (float) Math.ceil(Float.parseFloat(edtWidth.getText().toString()));
                float length = (float) Math.ceil(Float.parseFloat(edtLength.getText().toString()));
                float height = (float) Math.ceil(Float.parseFloat(edtHeight.getText().toString()));
                float weight = (float) Math.ceil(Float.parseFloat(edtWeight.getText().toString()));
                float volume = width * length * height;

                if (cbFragile.isChecked()) {
                    isFragile = true;
                }

                // -------------------- CALCULATION ------------------

                totalWeight += (int)weight;
                totalVolume += (int) volume;
                itemPrice = ((int) volume * volumePrice) + ((int) weight * weightPrice);

                if(isFragile){
                    itemPrice += 10000;
                }

                int deliveryPrice = distance * deliveryCost;

                totalPrice += deliveryPrice + itemPrice;

                // --------- INPUT ITEM TO DATABASE -----------
                Items dataItems = new Items(itemID, orderID, itemName, quantity, unit, width, length, height, weight, volume, itemPrice, isFragile);

                dbItems.child(itemID).setValue(dataItems);
                progressBar3.setVisibility(View.VISIBLE);
                // Sementara aja, nanti yang bener direct ke activity pembayaran.
                //startActivity(new Intent(OrderFormActivity.this, HomeActivity.class));
                //Toast.makeText(getApplicationContext(), "Order has been created. Please wait for the confirmation", Toast.LENGTH_LONG).show();

                // --------- INPUT ORDER TO DATABASE ----------
                Map<String, Object> mapAlamatPengirim = new HashMap<String, Object>();
                mapAlamatPengirim.put("AddressLine", AlamatPengirim);
                mapAlamatPengirim.put("Provinsi", ProvinsiPengirim);
                mapAlamatPengirim.put("Kota", KotaPengirim);
                mapAlamatPengirim.put("Kecamatan", KecamatanPengirim);
                mapAlamatPengirim.put("KodePos", KodePosPengirim);
                mapAlamatPengirim.put("Full", fullAddrPengirim);

                Map<String, Object> mapAlamatPenerima = new HashMap<String, Object>();
                mapAlamatPenerima.put("AddressLine", AlamatPenerima);
                mapAlamatPenerima.put("Provinsi", ProvinsiPenerima);
                mapAlamatPenerima.put("Kota", KotaPenerima);
                mapAlamatPenerima.put("Kecamatan", KecamatanPenerima);
                mapAlamatPenerima.put("KodePos", KodePosPenerima);
                mapAlamatPenerima.put("Full", fullAddrPenerima);

                Order dataOrder = new Order(orderID, userID, orderDate, orderStatus, NamaPengirim, EmailPengirim, TelpPengirim, NamaPenerima
                        , EmailPenerima, TelpPenerima, tglPengiriman, distance, deliveryPrice, totalWeight, totalVolume, itemPrice, totalPrice);

                dbOrder.child(orderID).setValue(dataOrder);
                dbOrder.child(orderID).child("AlamatPengirim").setValue(mapAlamatPengirim);
                dbOrder.child(orderID).child("AlamatPenerima").setValue(mapAlamatPenerima);

                // ---------------------- EVENT LISTENER --------------------------
                ValueEventListener writeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar3.setVisibility(View.GONE);

                        // Go to payment activity here !
                        Toast.makeText(getApplicationContext(), "Order has been created. Please wait for the confirmation", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_LONG).show();
                    }
                };

                dbItems.addValueEventListener(writeListener);

            }
        });

        Button btnAddItem = findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == image_id && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            previewimage.setImageURI(selectedImage);
        }
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        /*
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String orderDate = dateFormat.format(date);
         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDateString = dateFormat.format(calendar.getTime());

        edtTglPengiriman.setText(currentDateString);
    }
}
