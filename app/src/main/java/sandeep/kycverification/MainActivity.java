package sandeep.kycverification;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etFullName, etDocumentNumber, etOtp;
    private Button btnVerifyDocument, btnVerifyOtp;
    private TextView tvKycStatus;
    private String generatedOtp,email;
    private String selectedDocumentType;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Spinner documentTypeSpinner;
    FirestoreHelper firestoreHelper;
    CardView cardview;
    int check = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        documentTypeSpinner = findViewById(R.id.document_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.document_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        documentTypeSpinner.setAdapter(adapter);
        documentTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDocumentType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDocumentType = null; // Handle case when nothing is selected
            }
        });
        // Initialize UI elements
        etFullName = findViewById(R.id.etFullName);
        etDocumentNumber = findViewById(R.id.etDocumentNumber);
        etOtp = findViewById(R.id.etOtp);
        btnVerifyDocument = findViewById(R.id.btnVerifyDocument);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvKycStatus = findViewById(R.id.tvKycStatus);
        cardview = findViewById(R.id.card);
        // Document Verification Button Click Listener
        btnVerifyDocument.setOnClickListener(view -> verifyDocumentNumber());

        // OTP Verification Button Click Listener
        btnVerifyOtp.setOnClickListener(view -> verifyOtp());

        firestoreHelper = new FirestoreHelper();

        adddata();
    }

    private void verifyDocumentNumber() {
        String documentNumber = etDocumentNumber.getText().toString().trim();
        String fullNameInput = etFullName.getText().toString().trim();

        if (documentNumber.isEmpty() || fullNameInput.isEmpty()) {
            Log.d("DocumentVerification", "Document number or full name not entered by user");
            showDialog("Error", "Please enter both document number and full name");
            return;
        }

        Log.d("DocumentVerification", "Entered document number: " + documentNumber);
        Log.d("DocumentVerification", "Entered full name: " + fullNameInput);

        db.collection("KYC")
                .whereEqualTo("documentNumber", documentNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        boolean nameMatched = false;

                        for (DocumentSnapshot document : task.getResult()) {
                            String documentName = document.getString("documentName");
                            String ownerName = document.getString("ownerName");
                            String userEmail = document.getString("email");

                            // Check if the full name matches (case-insensitive)
                            if (ownerName != null && ownerName.equalsIgnoreCase(fullNameInput)) {
                                nameMatched = true;

                                Log.d("DocumentVerification", "Document found: " + documentName + " owned by " + ownerName);
                                showDialog("Success", "Document verified: " + documentName + " owned by " + ownerName);

                                // Generate OTP
                                generatedOtp = generateOtp();
                                Log.d("OTPGeneration", "Generated OTP: " + generatedOtp);

                                // Send OTP via email
                                sendOtpViaEmail(userEmail, generatedOtp);

                                // Display OTP input fields for user to enter the OTP
                                cardview.setVisibility(View.VISIBLE);
                                btnVerifyOtp.setVisibility(View.VISIBLE);
                            }
                        }

                        if (!nameMatched) {
                            Log.d("DocumentVerification", "Document number matches, but full name does not");
                            showDialog("Error", "Document number matches, but the full name does not match the owner.");
                        }

                    } else {
                        Log.d("DocumentVerification", "Document not found in Firestore for document number: " + documentNumber);
                        showDialog("Error", "Document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error retrieving document: " + e.getMessage());
                    showDialog("Error", "Error retrieving document: " + e.getMessage());
                });
    }


    // Function to send OTP via GmailSender
    private void sendOtpViaEmail(String email, String otp) {
        String senderEmail = "sandeepkush880@gmail.com"; // Replace with your Gmail
        String senderPassword = "xjex dbfe vtps sgrc"; // Replace with your Gmail password

        Log.d("EmailSender", "Preparing to send OTP to: " + email);
        GmailSender gmailSender = new GmailSender(senderEmail, senderPassword);
        new Thread(() -> {
            try {
                Log.d("EmailSender", "Attempting to send email...");
                gmailSender.sendEmail(email, "Your OTP for Document Verification", "Dear user, your OTP is: " + otp);
                Log.d("EmailSender", "Email sent successfully to: " + email);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "OTP sent to Registered email", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                Log.e("EmailSender", "Error sending email: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to send OTP email.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    // Function to generate a random 6-digit OTP
    private String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString();

        if (otp.isEmpty()) {
            Log.d("OTPVerification", "No OTP entered by user");
            showDialog("Error", "Please enter OTP");
            return;
        }

        Log.i("OTPVerification", "Entered OTP: " + otp);
        Log.d("OTPVerification", "Generated OTP to match: " + generatedOtp);

        if (otp.equals(generatedOtp)) {
            tvKycStatus.setVisibility(View.VISIBLE);
            tvKycStatus.setText("KYC Verified");
            Log.d("OTPVerification", "KYC verification successful");

            selectedDocumentType = documentTypeSpinner.getSelectedItem().toString();
            String documentNumber = etDocumentNumber.getText().toString();

            Intent intent = new Intent(MainActivity.this, Home.class);
            intent.putExtra("DOCUMENT_TYPE", selectedDocumentType);
            intent.putExtra("DOCUMENT_NUMBER", documentNumber);
            startActivity(intent);

            showDialog("Success", "KYC Verification Complete");
        } else {
            Log.d("OTPVerification", "Entered OTP does not match generated OTP");
            showDialog("Error", "Invalid OTP");
        }
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setIcon(R.mipmap.icon_round)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void adddata() {
        email = user.getEmail();
        Log.e("Email",email);
        // Add KYC data for different document types
        addAadhaarData();
        addNationalIdData();
        addPassportData();

    }
     // Common email for all entries


    private void addAadhaarData()    {

        Map<String, Object> aadhaarData1 = new HashMap<>();
        aadhaarData1.put("fullName", "Rahul Sharma");
        aadhaarData1.put("address", "123 MG Road, Bengaluru, Karnataka, India");
        aadhaarData1.put("gender", "Male");
        aadhaarData1.put("dob", "1990-01-01");
        aadhaarData1.put("fingerprints", "10 fingerprints data here");
        aadhaarData1.put("irisScans", "2 iris scan data here");
        aadhaarData1.put("photoUrl", "http://example.com/photo_rahul.jpg");
        aadhaarData1.put("phoneNumber", "9876543210");
        aadhaarData1.put("email", email);
        aadhaarData1.put("documentNumber", "110222054403");
        aadhaarData1.put("documentName", "Aadhaar Card");
        aadhaarData1.put("userPhoneNumber", "+919876543210");
        firestoreHelper.addOrUpdateKycData("Aadhaar_1", aadhaarData1);

        Map<String, Object> aadhaarData2 = new HashMap<>();
        aadhaarData2.put("fullName", "Priya Gupta");
        aadhaarData2.put("address", "234 Brigade Rd, Bengaluru, Karnataka, India");
        aadhaarData2.put("gender", "Female");
        aadhaarData2.put("dob", "1992-04-12");
        aadhaarData2.put("fingerprints", "10 fingerprints data here");
        aadhaarData2.put("irisScans", "2 iris scan data here");
        aadhaarData2.put("photoUrl", "http://example.com/photo_priya.jpg");
        aadhaarData2.put("phoneNumber", "8765432109");
        aadhaarData2.put("email", email);
        aadhaarData2.put("documentNumber", "111122223333");
        aadhaarData2.put("documentName", "Aadhaar Card");
        aadhaarData2.put("userPhoneNumber", "+918765432109");
        firestoreHelper.addOrUpdateKycData("Aadhaar_2", aadhaarData2);

        Map<String, Object> aadhaarData3 = new HashMap<>();
        aadhaarData3.put("fullName", "Ravi Kumar");
        aadhaarData3.put("address", "345 Ulsoor Rd, Bengaluru, Karnataka, India");
        aadhaarData3.put("gender", "Male");
        aadhaarData3.put("dob", "1988-11-22");
        aadhaarData3.put("fingerprints", "10 fingerprints data here");
        aadhaarData3.put("irisScans", "2 iris scan data here");
        aadhaarData3.put("photoUrl", "http://example.com/photo_ravi.jpg");
        aadhaarData3.put("phoneNumber", "7654321098");
        aadhaarData3.put("email", email);
        aadhaarData3.put("documentNumber", "999988887777");
        aadhaarData3.put("documentName", "Aadhaar Card");
        aadhaarData3.put("userPhoneNumber", "+917654321098");
        firestoreHelper.addOrUpdateKycData("Aadhaar_3", aadhaarData3);
    }

    private void addNationalIdData() {// Common email for all entries

        Map<String, Object> nationalIdData1 = new HashMap<>();
        nationalIdData1.put("fullName", "Anjali Singh");
        nationalIdData1.put("dob", "1985-05-15");
        nationalIdData1.put("gender", "Female");
        nationalIdData1.put("nationality", "Indian");
        nationalIdData1.put("documentNumber", "NID123456");
        nationalIdData1.put("address", "456 Residency Rd, Bengaluru, Karnataka, India");
        nationalIdData1.put("photograph", "http://example.com/photo_nid.jpg");
        nationalIdData1.put("signature", "http://example.com/signature_nid.jpg");
        nationalIdData1.put("expiryDate", "2030-05-15");
        nationalIdData1.put("documentName", "National ID Card");
        nationalIdData1.put("userPhoneNumber", "+919876543210");
        nationalIdData1.put("email", email);
        firestoreHelper.addOrUpdateKycData("National_ID_1", nationalIdData1);

        Map<String, Object> nationalIdData2 = new HashMap<>();
        nationalIdData2.put("fullName", "Kiran Verma");
        nationalIdData2.put("dob", "1990-02-20");
        nationalIdData2.put("gender", "Female");
        nationalIdData2.put("nationality", "Indian");
        nationalIdData2.put("documentNumber", "NID654321");
        nationalIdData2.put("address", "567 Brigade Rd, Bengaluru, Karnataka, India");
        nationalIdData2.put("photograph", "http://example.com/photo_kiran.jpg");
        nationalIdData2.put("signature", "http://example.com/signature_kiran.jpg");
        nationalIdData2.put("expiryDate", "2035-02-20");
        nationalIdData2.put("documentName", "National ID Card");
        nationalIdData2.put("userPhoneNumber", "+918765432109");
        nationalIdData2.put("email", email);
        firestoreHelper.addOrUpdateKycData("National_ID_2", nationalIdData2);

        Map<String, Object> nationalIdData3 = new HashMap<>();
        nationalIdData3.put("fullName", "Vikas Rao");
        nationalIdData3.put("dob", "1987-08-10");
        nationalIdData3.put("gender", "Male");
        nationalIdData3.put("nationality", "Indian");
        nationalIdData3.put("documentNumber", "NID789456");
        nationalIdData3.put("address", "678 Ulsoor Rd, Bengaluru, Karnataka, India");
        nationalIdData3.put("photograph", "http://example.com/photo_vikas.jpg");
        nationalIdData3.put("signature", "http://example.com/signature_vikas.jpg");
        nationalIdData3.put("expiryDate", "2032-08-10");
        nationalIdData3.put("documentName", "National ID Card");
        nationalIdData3.put("userPhoneNumber", "+919876543210");
        nationalIdData3.put("email", email);
        firestoreHelper.addOrUpdateKycData("National_ID_3", nationalIdData3);
    }

    private void addPassportData() {

        Map<String, Object> passportData1 = new HashMap<>();
        passportData1.put("fullName", "Neha Patel");
        passportData1.put("dob", "1992-09-09");
        passportData1.put("nationality", "Indian");
        passportData1.put("documentNumber", "P123456789");
        passportData1.put("photograph", "http://example.com/photo_passport.jpg");
        passportData1.put("signature", "http://example.com/signature_passport.jpg");
        passportData1.put("expiryDate", "2035-09-09");
        passportData1.put("placeOfIssue", "Bengaluru");
        passportData1.put("dateOfIssue", "2020-09-09");
        passportData1.put("documentName", "Passport");
        passportData1.put("userPhoneNumber", "+919876543210");
        passportData1.put("email", email);
        firestoreHelper.addOrUpdateKycData("Passport_1", passportData1);

        Map<String, Object> passportData2 = new HashMap<>();
        passportData2.put("fullName", "Rohit Sharma");
        passportData2.put("dob", "1985-04-05");
        passportData2.put("nationality", "Indian");
        passportData2.put("documentNumber", "P987654321");
        passportData2.put("photograph", "http://example.com/photo_passport_rohit.jpg");
        passportData2.put("signature", "http://example.com/signature_passport_rohit.jpg");
        passportData2.put("expiryDate", "2035-04-05");
        passportData2.put("placeOfIssue", "Delhi");
        passportData2.put("dateOfIssue", "2020-04-05");
        passportData2.put("documentName", "Passport");
        passportData2.put("userPhoneNumber", "+918765432109");
        passportData2.put("email", email);
        firestoreHelper.addOrUpdateKycData("Passport_2", passportData2);

        Map<String, Object> passportData3 = new HashMap<>();
        passportData3.put("fullName", "Sita Devi");
        passportData3.put("dob", "1983-09-15");
        passportData3.put("nationality", "Indian");
        passportData3.put("documentNumber", "P123789456");
        passportData3.put("photograph", "http://example.com/photo_passport_sita.jpg");
        passportData3.put("signature", "http://example.com/signature_passport_sita.jpg");
        passportData3.put("expiryDate", "2035-09-15");
        passportData3.put("placeOfIssue", "Mumbai");
        passportData3.put("dateOfIssue", "2020-09-15");
        passportData3.put("documentName", "Passport");
        passportData3.put("userPhoneNumber", "+919876543210");
        passportData3.put("email", email);
        firestoreHelper.addOrUpdateKycData("Passport_3", passportData3);
    }
}

