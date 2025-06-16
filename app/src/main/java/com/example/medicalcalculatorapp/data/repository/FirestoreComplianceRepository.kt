package com.example.medicalcalculatorapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.medicalcalculatorapp.domain.model.UserCompliance
import com.example.medicalcalculatorapp.domain.model.ComplianceEvent
import com.example.medicalcalculatorapp.domain.model.ComplianceEventType
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Firestore Compliance Repository - Cloud Storage for Medical App Compliance
 *
 * Handles Google Play required compliance data storage in Firestore:
 * - User compliance records
 * - Audit trail events
 * - Professional verification data
 * - Cross-device synchronization
 */
class FirestoreComplianceRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore

    // Collection names
    private companion object {
        const val COMPLIANCE_COLLECTION = "user_compliance"
        const val AUDIT_TRAIL_COLLECTION = "audit_trail"
        const val ANALYTICS_COLLECTION = "compliance_analytics"
    }

    // ====== COMPLIANCE RECORDS ======

    /**
     * Save user compliance record to Firestore
     */
    suspend fun saveUserCompliance(userId: String, compliance: UserCompliance): Boolean {
        return try {
            val complianceData = mapComplianceToFirestore(compliance)

            firestore.collection(COMPLIANCE_COLLECTION)
                .document(userId)
                .set(complianceData)
                .await()

            println("✅ Firestore: Saved compliance for user $userId")
            true

        } catch (e: Exception) {
            println("❌ Firestore: Error saving compliance for $userId: ${e.message}")
            false
        }
    }

    /**
     * Get user compliance record from Firestore
     */
    suspend fun getUserCompliance(userId: String): UserCompliance? {
        return try {
            val document = firestore.collection(COMPLIANCE_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                mapFirestoreToCompliance(document, userId)
            } else {
                println("📄 Firestore: No compliance record found for user $userId")
                null
            }

        } catch (e: Exception) {
            println("❌ Firestore: Error getting compliance for $userId: ${e.message}")
            null
        }
    }

    /**
     * Check if user has compliance record in Firestore
     */
    suspend fun hasComplianceRecord(userId: String): Boolean {
        return try {
            val document = firestore.collection(COMPLIANCE_COLLECTION)
                .document(userId)
                .get()
                .await()

            document.exists()

        } catch (e: Exception) {
            println("❌ Firestore: Error checking compliance record for $userId: ${e.message}")
            false
        }
    }

    /**
     * Delete user compliance record from Firestore (GDPR compliance)
     */
    suspend fun deleteUserCompliance(userId: String): Boolean {
        return try {
            // Delete main compliance record
            firestore.collection(COMPLIANCE_COLLECTION)
                .document(userId)
                .delete()
                .await()

            // Delete audit trail
            deleteAuditTrail(userId)

            println("✅ Firestore: Deleted all compliance data for user $userId")
            true

        } catch (e: Exception) {
            println("❌ Firestore: Error deleting compliance for $userId: ${e.message}")
            false
        }
    }

    // ====== AUDIT TRAIL ======

    /**
     * Log compliance event to Firestore audit trail
     */
    suspend fun logAuditEvent(event: ComplianceEvent): Boolean {
        return try {
            val eventData = mapEventToFirestore(event)

            firestore.collection(COMPLIANCE_COLLECTION)
                .document(event.userId)
                .collection(AUDIT_TRAIL_COLLECTION)
                .add(eventData)
                .await()

            println("✅ Firestore: Logged audit event ${event.eventType} for user ${event.userId}")
            true

        } catch (e: Exception) {
            println("❌ Firestore: Error logging audit event: ${e.message}")
            false
        }
    }

    /**
     * Get audit trail for user from Firestore
     */
    suspend fun getAuditTrail(userId: String): List<ComplianceEvent> {
        return try {
            val querySnapshot = firestore.collection(COMPLIANCE_COLLECTION)
                .document(userId)
                .collection(AUDIT_TRAIL_COLLECTION)
                .orderBy("timestamp")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                mapFirestoreToEvent(document, userId)
            }

        } catch (e: Exception) {
            println("❌ Firestore: Error getting audit trail for $userId: ${e.message}")
            emptyList()
        }
    }

    /**
     * Delete audit trail for user
     */
    private suspend fun deleteAuditTrail(userId: String): Boolean {
        return try {
            val querySnapshot = firestore.collection(COMPLIANCE_COLLECTION)
                .document(userId)
                .collection(AUDIT_TRAIL_COLLECTION)
                .get()
                .await()

            // Delete all audit events
            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
            }

            true

        } catch (e: Exception) {
            println("❌ Firestore: Error deleting audit trail for $userId: ${e.message}")
            false
        }
    }

    // ====== ANALYTICS (for Google Play reporting) ======

    /**
     * Save compliance analytics for Google Play reporting
     */
    suspend fun saveComplianceAnalytics(userId: String, analyticsData: Map<String, Any>): Boolean {
        return try {
            val data = analyticsData.toMutableMap().apply {
                put("timestamp", System.currentTimeMillis())
                put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            }

            firestore.collection(ANALYTICS_COLLECTION)
                .add(data)
                .await()

            println("✅ Firestore: Saved compliance analytics")
            true

        } catch (e: Exception) {
            println("❌ Firestore: Error saving analytics: ${e.message}")
            false
        }
    }

    // ====== MAPPING FUNCTIONS ======

    private fun mapComplianceToFirestore(compliance: UserCompliance): Map<String, Any> {
        return mapOf(
            "userId" to compliance.userId,
            "complianceVersion" to compliance.complianceVersion,
            "lastUpdated" to compliance.lastUpdated,
            "createdAt" to compliance.createdAt,

            // Basic terms consent
            "basicTermsAccepted" to (compliance.basicTermsConsent?.isAccepted ?: false),
            "basicTermsAcceptedAt" to (compliance.basicTermsConsent?.acceptedAt ?: 0L),
            "basicTermsVersion" to (compliance.basicTermsConsent?.version ?: ""),

            // Medical disclaimer consent
            "medicalDisclaimerAccepted" to (compliance.medicalDisclaimerConsent?.isAccepted ?: false),
            "medicalDisclaimerAcceptedAt" to (compliance.medicalDisclaimerConsent?.acceptedAt ?: 0L),
            "medicalDisclaimerVersion" to (compliance.medicalDisclaimerConsent?.version ?: ""),

            // Professional verification
            "professionalVerified" to (compliance.professionalVerification?.isVerified ?: false),
            "professionalVerifiedAt" to (compliance.professionalVerification?.verifiedAt ?: 0L),
            "professionalType" to (compliance.professionalVerification?.professionalType?.code ?: ""),
            "licenseInfo" to (compliance.professionalVerification?.licenseInfo ?: ""),

            // Privacy policy consent
            "privacyPolicyAccepted" to (compliance.privacyPolicyConsent?.isAccepted ?: false),
            "privacyPolicyAcceptedAt" to (compliance.privacyPolicyConsent?.acceptedAt ?: 0L),
            "privacyPolicyVersion" to (compliance.privacyPolicyConsent?.version ?: ""),

            // Overall status
            "isCompliant" to compliance.complianceStatus.isCompliant,
            "needsReview" to compliance.complianceStatus.needsReview,
            "complianceNotes" to (compliance.complianceStatus.notes ?: "")
        )
    }

    private fun mapFirestoreToCompliance(document: DocumentSnapshot, userId: String): UserCompliance? {
        return try {
            val data = document.data ?: return null

            // This is a simplified mapping - in a real implementation,
            // you'd need to reconstruct the full UserCompliance object
            // For now, return null to indicate we need the full mapper implementation

            println("📄 Firestore: Retrieved compliance data for $userId")
            null // TODO: Implement full mapping

        } catch (e: Exception) {
            println("❌ Firestore: Error mapping compliance document: ${e.message}")
            null
        }
    }

    private fun mapEventToFirestore(event: ComplianceEvent): Map<String, Any> {
        return mapOf(
            "eventType" to event.eventType.name,
            "timestamp" to event.timestamp,
            "details" to event.details,
            "version" to (event.version ?: ""),
            "method" to event.method.code,
            "sessionId" to (event.sessionId ?: "")
        )
    }

    private fun mapFirestoreToEvent(document: DocumentSnapshot, userId: String): ComplianceEvent? {
        return try {
            val data = document.data ?: return null

            ComplianceEvent(
                eventType = ComplianceEventType.valueOf(data["eventType"] as String),
                timestamp = data["timestamp"] as Long,
                details = data["details"] as String,
                version = data["version"] as? String,
                method = com.example.medicalcalculatorapp.domain.model.ConsentMethod.fromCode(data["method"] as String),
                userId = userId,
                sessionId = data["sessionId"] as? String
            )

        } catch (e: Exception) {
            println("❌ Firestore: Error mapping event document: ${e.message}")
            null
        }
    }

    // ====== UTILITY METHODS ======

    /**
     * Test Firestore connection
     */
    suspend fun testConnection(): Boolean {
        return try {
            // Try to read from a test collection
            firestore.collection("test")
                .limit(1)
                .get()
                .await()

            println("✅ Firestore: Connection test successful")
            true

        } catch (e: Exception) {
            println("❌ Firestore: Connection test failed: ${e.message}")
            false
        }
    }

    /**
     * Get Firestore status for debugging
     */
    fun getConnectionStatus(): String {
        return try {
            if (Firebase.firestore.app.isDataCollectionDefaultEnabled) {
                "Firestore: Connected and Ready"
            } else {
                "Firestore: Connected but Data Collection Disabled"
            }
        } catch (e: Exception) {
            "Firestore: Connection Error - ${e.message}"
        }
    }
}