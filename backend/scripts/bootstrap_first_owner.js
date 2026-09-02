/**
 * Bunzo Bootstrap Script: Set First Owner
 *
 * HOW TO RUN:
 * 1. Download Service Account Key from Firebase Console: Project Settings -> Service Accounts -> Generate new private key
 * 2. Save it locally as `serviceAccountKey.json` in this folder.
 * 3. Run: node bootstrap_first_owner.js <USER_UID_OR_PHONE>
 */

const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function bootstrapOwner(identifier) {
  try {
    let uid = identifier;
    if (identifier.startsWith("09") || identifier.startsWith("+963")) {
      const cleanPhone = identifier.replace("+963", "").replace(/^0/, "");
      uid = `user_${cleanPhone}`;
    }

    console.log(`Setting OWNER role and Custom Claims for: ${uid}...`);

    // 1. Set Custom Claims in Firebase Authentication
    await admin.auth().setCustomUserClaims(uid, {
      role: "owner",
      admin: true,
      kitchen: true,
      owner: true
    });

    // 2. Set Role in Firestore
    await db.collection("users").document(uid).set({
      role: "owner",
      updatedAt: Date.now()
    }, { merge: true });

    // 3. Create Audit Log
    await db.collection("auditLogs").add({
      actorId: "SYSTEM_BOOTSTRAP",
      action: "BOOTSTRAP_FIRST_OWNER",
      targetUid: uid,
      timestamp: Date.now()
    });

    console.log(`✅ SUCCESS: User ${uid} has been elevated to OWNER with full Admin privileges!`);
    process.exit(0);
  } catch (error) {
    console.error("❌ Failed to set owner:", error);
    process.exit(1);
  }
}

const targetUser = process.argv[2];
if (!targetUser) {
  console.log("Usage: node bootstrap_first_owner.js <USER_UID_OR_PHONE>");
  process.exit(1);
}

bootstrapOwner(targetUser);
