package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Coupon
import com.example.data.repository.CouponRepository
import com.example.data.repository.CouponValidationResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CouponUiState(
    val appliedCoupon: Coupon? = null,
    val appliedDiscount: Double = 0.0,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val availableCoupons: List<Coupon> = emptyList(),
    val isValidating: Boolean = false
)

class CouponViewModel(
    private val couponRepository: CouponRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CouponUiState())
    val uiState: StateFlow<CouponUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            couponRepository.getAllCoupons().collect { coupons ->
                _uiState.update { it.copy(availableCoupons = coupons) }
            }
        }
    }

    fun applyCoupon(code: String, subtotal: Double, customerUsedCoupons: Set<String> = emptySet()) {
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a coupon code.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, errorMessage = null, successMessage = null) }
            when (val result = couponRepository.validateCoupon(code, subtotal, customerUsedCoupons)) {
                is CouponValidationResult.Success -> {
                    _uiState.update {
                        it.copy(
                            appliedCoupon = result.coupon,
                            appliedDiscount = result.discount,
                            successMessage = result.message,
                            errorMessage = null,
                            isValidating = false
                        )
                    }
                }
                is CouponValidationResult.Error -> {
                    _uiState.update {
                        it.copy(
                            appliedCoupon = null,
                            appliedDiscount = 0.0,
                            errorMessage = result.message,
                            successMessage = null,
                            isValidating = false
                        )
                    }
                }
            }
        }
    }

    fun removeCoupon() {
        _uiState.update {
            it.copy(
                appliedCoupon = null,
                appliedDiscount = 0.0,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
