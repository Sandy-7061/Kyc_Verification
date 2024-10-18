
# KYC Verification System

### Overview

This project is a KYC (Know Your Customer) document verification system that integrates with **Firebase Firestore** to store, update, and validate identity documents. The system handles multiple document types, such as **Aadhaar Card**, **National ID Card**, and **Passport**, and provides functionality to match document numbers with associated full names and emails for verification.

### Features

- **KYC Data Insertion**: Supports adding new KYC data or updating existing data in Firestore based on the document number.
- **Document Verification**: Matches the document number and owner name to verify the authenticity of the submitted KYC data.
- **Firestore Integration**: Utilizes Firebase Firestore for secure, cloud-based storage of KYC documents.
- **OTP Verification**: Sends OTP to the user's email for additional verification steps.
  
### Setup Instructions

1. **Firebase Setup**: 
   - Configure Firebase in your Android application by adding the `google-services.json` file to your project.
   - Enable Firestore in Firebase Console.

2. **Dependencies**:
   Add the following dependencies to your `build.gradle` (Module) file:
   ```gradle
   implementation 'com.google.firebase:firebase-firestore:24.3.0'
   implementation 'com.google.firebase:firebase-auth:21.0.1'
   ```

3. **FirestoreHelper**:
   This class handles all interactions with Firebase Firestore. It allows you to add or update KYC data based on the document number. Here's an example of using it in your application:
   
   ```java
   FirestoreHelper firestoreHelper = new FirestoreHelper();
   
   Map<String, Object> kycDataMap = new HashMap<>();
   kycDataMap.put("fullName", "Rahul Sharma");
   kycDataMap.put("documentNumber", "110222054403");
   kycDataMap.put("email", "rahul.sharma@example.com");

   firestoreHelper.addOrUpdateKycData("Aadhaar Card", kycDataMap);
   ```

4. **KycData Class**:
   This is the model class for KYC data. You can use this class to structure and manage KYC details in your app.

### Verified Data For Test

Below are the use cases for testing the KYC verification system using provided test data.

#### 1. **Aadhaar Card Data**

- **Document Number**: 110222054403
- **Full Name**: Rahul Sharma
- **Document Type**: Aadhaar Card

- **Document Number**: 111122223333
- **Full Name**: Priya Gupta
- **Document Type**: Aadhaar Card

- **Document Number**: 999988887777
- **Full Name**: Ravi Kumar
- **Document Type**: Aadhaar Card

#### 2. **National ID Card Data**

- **Document Number**: NID123456
- **Full Name**: Anjali Singh
- **Document Type**: National ID Card

- **Document Number**: NID654321
- **Full Name**: Kiran Verma
- **Document Type**: National ID Card

- **Document Number**: NID789456
- **Full Name**: Vikas Rao
- **Document Type**: National ID Card

#### 3. **Passport Data**

- **Document Number**: P123456789
- **Full Name**: Neha Patel
- **Document Type**: Passport

- **Document Number**: P987654321
- **Full Name**: Rohit Sharma
- **Document Type**: Passport

- **Document Number**: P123789456
- **Full Name**: Sita Devi
- **Document Type**: Passport

### Testing the KYC System

You can test the KYC system by inputting the test data provided above. The system will verify the document number and full name, and either update an existing record or insert a new one based on the document type.

- **Case 1**: If you enter a document number that already exists in Firestore, the system will update the existing record with the new data.
- **Case 2**: If the document number does not exist, it will create a new entry for the given document type (Aadhaar, National ID, or Passport).
  
### Expected Behavior:

- If the document number and name match with the data in Firestore, a success message will be shown.
- If the document number exists but the name does not match, the system will notify that the name does not match with the document number.
- After verification, an OTP will be sent to the registered email for further authentication.

### Future Enhancements

- Add support for additional document types (e.g., Voter ID, Driver's License).
- Improve the UI/UX for smoother KYC submission and verification processes.
- Implement multi-factor authentication for added security.

---

Here are 10 use cases for your KYC Verification System:

### Use Cases

1. **KYC Data Submission**:
   - A user submits their Aadhaar Card details (document number and full name) for KYC verification.

2. **KYC Data Update**:
   - An existing user updates their National ID Card information, changing their full name and address.

3. **Document Type Selection**:
   - A user selects a document type (Aadhaar, National ID, Passport) from a dropdown menu to submit their KYC data.

4. **Document Verification**:
   - The system verifies the submitted document number against the Firestore database and checks for a matching full name.

5. **Duplicate Document Detection**:
   - The system detects when a user tries to submit KYC data with a document number that already exists in Firestore, prompting an update instead of a new entry.

6. **Error Notification**:
   - The system notifies the user if the document number is missing or invalid during the KYC submission process.

7. **OTP Generation and Verification**:
   - After successful submission of KYC data, an OTP is generated and sent to the user’s email for further verification.

8. **User Feedback on Verification**:
   - The system provides feedback to the user indicating whether their KYC submission was successful, including any discrepancies found during verification.

9. **Multiple Document Handling**:
   - A user can submit KYC data for multiple document types (e.g., Aadhaar and Passport) within the same session, with the system managing each submission separately.

10. **Data Retrieval for Review**:
    - An admin can retrieve and review all KYC data submitted by users for auditing purposes, displaying document types, numbers, and user details.

Here's how you can format your contact information for the README file:

---

## Contact Information

**Name:** Sandeep Kushwaha  
**Email:** [sandeepkush880@gmail.com](mailto:sandeepkush880@gmail.com)

--- 
