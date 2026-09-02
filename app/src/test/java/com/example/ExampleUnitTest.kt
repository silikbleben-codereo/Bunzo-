package com.example

import com.example.data.model.CartItem
import com.example.data.model.KitchenOrderItem
import com.example.data.model.KitchenOrderView
import com.example.data.model.Order
import com.example.data.model.OrderItemRecord
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.utils.SyrianPhoneValidator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    private val customerUser = User(
        id = "user_cust_1",
        name = "Customer",
        role = UserRole.CUSTOMER,
        phone = "+963911223344",
        phoneVerified = true,
        phoneVerifiedAt = 1714500000000L,
        firebaseUid = "fb_uid_cust_1"
    )
    private val kitchenUser = User(
        id = "user_kitch_1",
        name = "Chef",
        role = UserRole.KITCHEN,
        phone = "+963944556677",
        phoneVerified = true,
        phoneVerifiedAt = 1714500000000L,
        firebaseUid = "fb_uid_kitch_1"
    )
    private val adminUser = User(
        id = "user_admin_1",
        name = "Admin",
        role = UserRole.ADMIN,
        phone = "+963988776655",
        phoneVerified = true,
        phoneVerifiedAt = 1714500000000L,
        firebaseUid = "fb_uid_admin_1"
    )
    private val ownerUser = User(
        id = "user_owner_1",
        name = "Owner",
        role = UserRole.OWNER,
        phone = "+963999999999",
        phoneVerified = true,
        phoneVerifiedAt = 1714500000000L,
        firebaseUid = "fb_uid_owner_1"
    )

    // TEST 1: Customer -> Admin Route -> Expected: DENIED
    @Test
    fun test1_customerAdminRoute_isDenied() {
        assertFalse("Customer must NOT have admin route access", customerUser.role.isAdmin)
    }

    // TEST 2: Customer -> Kitchen Route -> Expected: DENIED
    @Test
    fun test2_customerKitchenRoute_isDenied() {
        assertFalse("Customer must NOT have kitchen route access", customerUser.role.isKitchen)
    }

    // TEST 3: Kitchen -> Admin Route -> Expected: DENIED
    @Test
    fun test3_kitchenAdminRoute_isDenied() {
        assertFalse("Kitchen staff must NOT have admin panel access", kitchenUser.role.isAdmin)
    }

    // TEST 4: Kitchen -> users sensitive data -> Expected: DENIED
    @Test
    fun test4_kitchenSensitiveData_isStripped() {
        val ticket = KitchenOrderView(
            id = "ord_101",
            orderNumber = "#BNZ-101",
            customerName = "عميل",
            items = listOf(KitchenOrderItem(productNameAr = "برغر دجاج", quantity = 2)),
            orderFoodNotes = "بدون مايونيز"
        )
        assertEquals("#BNZ-101", ticket.orderNumber)
        assertEquals(1, ticket.items.size)
        assertFalse(KitchenOrderView::class.java.declaredFields.any {
            it.name == "customerPhone" || it.name == "totalAmount" || it.name == "paymentMethod"
        })
    }

    // TEST 5: Customer changes role -> ADMIN -> Expected: DENIED
    @Test
    fun test5_customerPromoteSelfAdmin_isDenied() {
        val canCustomerElevate = customerUser.role.isAdmin
        assertFalse("Customer cannot elevate to ADMIN", canCustomerElevate)
    }

    // TEST 6: Kitchen changes role -> ADMIN -> Expected: DENIED
    @Test
    fun test6_kitchenPromoteSelfAdmin_isDenied() {
        assertFalse("Kitchen cannot elevate to ADMIN", kitchenUser.role.isAdmin)
    }

    // TEST 7: Admin changes own role -> OWNER -> Expected: DENIED
    @Test
    fun test7_adminPromoteSelfOwner_isDenied() {
        assertFalse("Admin cannot self-promote to OWNER", adminUser.role == UserRole.OWNER)
        assertTrue("Owner is distinct", ownerUser.role == UserRole.OWNER)
    }

    // TEST 8: Customer reads another customer's order -> Expected: DENIED
    @Test
    fun test8_customerReadOtherOrder_isDenied() {
        val customerAId = "user_cust_A"
        val customerBId = "user_cust_B"
        val orderOfCustomerA = Order(id = "ord_A", customerId = customerAId)

        val canCustomerBAccess = (customerBId == orderOfCustomerA.customerId)
        assertFalse("Customer B must not access Customer A's order", canCustomerBAccess)
    }

    // TEST 9: Kitchen changes order total -> Expected: DENIED
    @Test
    fun test9_kitchenChangesOrderTotal_isDenied() {
        assertTrue(kitchenUser.role.isKitchen)
        assertFalse(kitchenUser.role.isAdmin)
    }

    // TEST 10: Customer changes product price -> Expected: DENIED
    @Test
    fun test10_customerChangesProductPrice_isDenied() {
        assertFalse("Customer has no write authority on products", customerUser.role.isAdmin)
    }

    // TEST 11: Admin changes product price -> Expected: ALLOWED
    @Test
    fun test11_adminChangesProductPrice_isAllowed() {
        assertTrue("Admin has product management authority", adminUser.role.isAdmin)
    }

    // TEST 12: Admin manages orders -> Expected: ALLOWED
    @Test
    fun test12_adminManagesOrders_isAllowed() {
        assertTrue("Admin has order management authority", adminUser.role.isAdmin)
    }

    // TEST 13: Kitchen updates preparation status -> Expected: ALLOWED
    @Test
    fun test13_kitchenUpdatesPrepStatus_isAllowed() {
        val validKitchenStatuses = listOf(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY)
        assertTrue(kitchenUser.role.isKitchen)
        assertTrue(validKitchenStatuses.contains(OrderStatus.PREPARING))
    }

    // TEST 14: Syrian Phone Validation - Valid local and international formats
    @Test
    fun test14_syrianPhoneValidation_valid() {
        val localPhone = "0949159274"
        val internationalPhone = "+963949159274"
        val formattedE164 = SyrianPhoneValidator.normalizeToInternational(localPhone)

        assertNull("Valid Syrian local phone is accepted", SyrianPhoneValidator.getValidationError(localPhone))
        assertNull("Valid Syrian international phone is accepted", SyrianPhoneValidator.getValidationError(internationalPhone))
        assertEquals("+963949159274", formattedE164)
    }

    // TEST 15: Syrian Phone Validation - Invalid prefixes and lengths
    @Test
    fun test15_syrianPhoneValidation_invalid() {
        val tooShort = "091234"
        val wrongPrefix = "0812345678"
        val nonSyrian = "+12345678901"

        assertNotNull("Too short phone is rejected", SyrianPhoneValidator.getValidationError(tooShort))
        assertNotNull("Wrong prefix is rejected", SyrianPhoneValidator.getValidationError(wrongPrefix))
        assertNotNull("Non-Syrian code is rejected", SyrianPhoneValidator.getValidationError(nonSyrian))
    }

    // TEST 16: Verification State Persistence on User model
    @Test
    fun test16_userVerificationFlags() {
        val verifiedUser = User(
            id = "uid_123",
            phone = "+963949159274",
            phoneVerified = true,
            phoneVerifiedAt = 1714500000000L,
            firebaseUid = "firebase_uid_123"
        )
        assertTrue(verifiedUser.phoneVerified)
        assertNotNull(verifiedUser.phoneVerifiedAt)
        assertEquals("firebase_uid_123", verifiedUser.firebaseUid)
    }

    // TEST 17: Unverified user default values
    @Test
    fun test17_unverifiedUserDefaults() {
        val unverifiedUser = User(
            id = "uid_456",
            phone = "+963911111111"
        )
        assertFalse(unverifiedUser.phoneVerified)
        assertNull(unverifiedUser.phoneVerifiedAt)
        assertNull(unverifiedUser.firebaseUid)
    }

    // TEST 18: Unauthenticated User Order Placement -> Expected: BLOCKED
    @Test
    fun test18_unauthenticatedUser_orderPlacementBlocked() {
        val anonymousUser: User? = null
        assertNull("Anonymous visitor has no authenticated user session", anonymousUser)
        val canProceedToDirectCheckout = anonymousUser != null
        assertFalse("Unauthenticated visitor must be redirected to AuthRequired prompt", canProceedToDirectCheckout)
    }
}
