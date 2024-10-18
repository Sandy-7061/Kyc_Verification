package sandeep.kycverification;

import android.os.Bundle;
import android.util.Log; // Import the Log class
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Home extends AppCompatActivity {

    private TextView tvUserDetails;
    private FirebaseFirestore firestore;
    private static final String TAG = "HomeActivity"; // Define a tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI element
        tvUserDetails = findViewById(R.id.tvUserDetails);

        // Retrieve data from Intent
        String documentNumber = getIntent().getStringExtra("DOCUMENT_NUMBER");
        Log.d(TAG, "Received document number: " + documentNumber); // Log the received document number
        if (documentNumber != null) {
            fetchKycData(documentNumber);
        } else {
            Log.e(TAG, "Document number is null"); // Log error if document number is null
        }
    }

    private void fetchKycData(String documentNumber) {
        firestore.collection("KYC") // Replace with your actual collection name
                .whereEqualTo("documentNumber", documentNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult(); // Get the result of the query
                        Log.d(TAG, "Query successful, number of documents found: " + querySnapshot.size());
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                KycData kycData = document.toObject(KycData.class);
                                displayKycData(kycData);
                            }
                        } else {
                            Log.w(TAG, "No documents found for this number");
                            Toast.makeText(Home.this, "No documents found for this number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: " + task.getException());
                        Toast.makeText(Home.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void displayKycData(KycData kycData) {
        String details = "Name: " + kycData.getFullName() + "\n" +
                "Address: " + kycData.getAddress() + "\n" +
                "Gender: " + kycData.getGender() + "\n" +
                "DOB: " + kycData.getDob() + "\n" +
                "Document Number: " + kycData.getDocumentNumber() + "\n" +
                "Document Name: " + kycData.getDocumentName() + "\n" +
                "Email: " + kycData.getEmail() + "\n" +
                "Phone Number: " + kycData.getUserPhoneNumber() + "\n" +
                "Expiry Date: " + kycData.getExpiryDate() + "\n" +
                "Photograph URL: " + kycData.getPhotograph() + "\n" +
                "Signature URL: " + kycData.getSignature();

        tvUserDetails.setText(details);
    }
}
