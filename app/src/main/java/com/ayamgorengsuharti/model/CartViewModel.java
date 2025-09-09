package com.ayamgorengsuharti.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ayamgorengsuharti.model.CartItem;
import com.ayamgorengsuharti.model.MenuItemResponse;
import java.util.HashMap;
import java.util.Map;

public class CartViewModel extends ViewModel {

    // Map<Integer, CartItem> -> Key: ID Produk, Value: Objek CartItem
    private final MutableLiveData<Map<Integer, CartItem>> cartItems = new MutableLiveData<>();
    // --- BARU: LiveData untuk status visibilitas kapsul ---
    private final MutableLiveData<Boolean> isCartCapsuleVisible = new MutableLiveData<>(false);


    public CartViewModel() {
        cartItems.setValue(new HashMap<>());
    }

    public LiveData<Map<Integer, CartItem>> getCartItems() {
        return cartItems;
    }



    public void addItem(MenuItemResponse product) {
        Map<Integer, CartItem> currentCart = cartItems.getValue();
        if (currentCart == null) {
            currentCart = new HashMap<>();
        }

        CartItem existingItem = currentCart.get(product.getId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
        } else {
            currentCart.put(product.getId(), new CartItem(product, 1));
        }
        cartItems.setValue(currentCart); // Trigger observer
    }

    public void removeItem(MenuItemResponse product) {
        Map<Integer, CartItem> currentCart = cartItems.getValue();
        if (currentCart == null || !currentCart.containsKey(product.getId())) {
            return;
        }

        CartItem existingItem = currentCart.get(product.getId());
        if (existingItem.getQuantity() > 1) {
            existingItem.setQuantity(existingItem.getQuantity() - 1);
        } else {
            currentCart.remove(product.getId());
        }
        cartItems.setValue(currentCart); // Trigger observer
    }
    public void clearCart() {
        Map<Integer, CartItem> currentCart = cartItems.getValue();
        if (currentCart != null) {
            currentCart.clear();
            cartItems.setValue(currentCart); // Trigger observer untuk mengosongkan
        }
    }
    public LiveData<Boolean> getIsCartCapsuleVisible() {
        return isCartCapsuleVisible;
    }

    public void setCartCapsuleVisible(boolean isVisible) {
        // Cek agar tidak update berulang jika nilainya sama
        if (this.isCartCapsuleVisible.getValue() != null && this.isCartCapsuleVisible.getValue() == isVisible) return;
        this.isCartCapsuleVisible.setValue(isVisible);
    }
}