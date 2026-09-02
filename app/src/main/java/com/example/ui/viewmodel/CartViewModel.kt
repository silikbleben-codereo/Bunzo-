package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BunzoApplication
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.ProductExtra
import com.example.data.model.ProductSize
import com.example.data.repository.BunzoRepository
import com.example.utils.SoundHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: BunzoRepository = BunzoApplication.instance.repository
) : ViewModel() {

    private val _topMessage = MutableStateFlow<String?>(null)
    val topMessage: StateFlow<String?> = _topMessage.asStateFlow()
    val toastMessage: StateFlow<String?> = _topMessage.asStateFlow()

    fun showTopMessage(msg: String = "تمت الإضافة للسلة بنجاح") {
        SoundHelper.playAddClickSound()
        viewModelScope.launch {
            _topMessage.value = msg
            delay(2500)
            if (_topMessage.value == msg) {
                _topMessage.value = null
            }
        }
    }

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalItemCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deliveryFee: StateFlow<Double> = repository.getSettings().map {
        it.deliveryFee
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    val grandTotal: StateFlow<Double> = cartItems.map { items ->
        val sub = items.sumOf { it.totalPrice }
        if (sub > 0) sub + 5000.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(
        product: Product,
        selectedSize: ProductSize? = null,
        selectedExtras: List<ProductExtra> = emptyList(),
        quantity: Int = 1,
        notes: String = ""
    ) {
        showTopMessage("تمت إضافة ${product.nameAr} إلى السلة 🛒")
        viewModelScope.launch {
            repository.addToCart(product, selectedSize, selectedExtras, quantity, notes)
        }
    }

    fun incrementQuantity(item: CartItem) {
        showTopMessage("تمت زيادة كمية ${item.productNameAr}")
        viewModelScope.launch {
            repository.updateCartItem(item.copy(quantity = item.quantity + 1))
        }
    }

    fun decrementQuantity(item: CartItem) {
        if (item.quantity > 1) {
            showTopMessage("تم إنقاص كمية ${item.productNameAr}")
        } else {
            showTopMessage("تم حذف ${item.productNameAr} من السلة")
        }
        viewModelScope.launch {
            if (item.quantity > 1) {
                repository.updateCartItem(item.copy(quantity = item.quantity - 1))
            } else {
                repository.removeFromCart(item)
            }
        }
    }

    fun removeItem(item: CartItem) {
        showTopMessage("تم حذف ${item.productNameAr} من السلة")
        viewModelScope.launch {
            repository.removeFromCart(item)
        }
    }

    fun clearCart() {
        showTopMessage("تم إفراغ السلة بالكامل")
        viewModelScope.launch {
            repository.clearCart()
        }
    }
}
