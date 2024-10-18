package sandeep.kycverification;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class FirestoreHelper {
    private FirebaseFirestore db;

    public FirestoreHelper() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    public void addOrUpdateKycData(String documentType, Map<String, Object> kycData) {
        // Ensure kycData is not null
        if (kycData == null || kycData.isEmpty()) {
            System.err.println("KYC data cannot be null or empty");
            return;
        }

        // Add document name and owner name to kycData
        kycData.put("documentType", documentType); // Add the document type
        kycData.put("ownerName", kycData.get("fullName")); // Assuming fullName is part of kycData

        // Check for existing document by documentNumber (not documentType)
        String documentNumber = (String) kycData.get("documentNumber");

        if (documentNumber == null || documentNumber.isEmpty()) {
            System.err.println("Document number is missing");
            return; // Exit if document number is not present
        }

        db.collection("KYC")
                .whereEqualTo("documentNumber", documentNumber) // Check by document number
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean documentExists = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documentExists = true;
                            // Update existing document
                            db.collection("KYC").document(document.getId())
                                    .set(kycData)
                                    .addOnSuccessListener(aVoid -> {
                                        System.out.println("Document updated successfully: " + document.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        System.err.println("Error updating document: " + e);
                                    });
                            break; // Break after the first found document
                        }

                        // If no existing document, add a new one
                        if (!documentExists) {
                            db.collection("KYC").add(kycData)
                                    .addOnSuccessListener(documentReference -> {
                                        System.out.println("Document added with ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        System.err.println("Error adding document: " + e);
                                    });
                        }
                    } else {
                        System.err.println("Error getting documents: " + task.getException());
                    }
                });
    }
    public interface FirestoreCallback {
        void onCallback(Map<String, Object> data);
    }

}
