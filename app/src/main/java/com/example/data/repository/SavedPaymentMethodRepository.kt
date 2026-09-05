package com.example.data.repository

import com.example.data.local.dao.SavedPaymentMethodDao
import com.example.data.local.entity.SavedPaymentMethodEntity
import com.example.data.model.SavedPaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedPaymentMethodRepository(private val dao: SavedPaymentMethodDao) {

    fun getCustomerSavedMethods(customerId: String = "USR_101"): Flow<List<SavedPaymentMethod>> {
        return dao.getCustomerSavedPaymentMethods(customerId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun addSavedMethod(method: SavedPaymentMethod) {
        dao.insertSavedPaymentMethod(method.toEntity())
    }

    suspend fun deleteSavedMethod(id: String) {
        dao.deleteSavedPaymentMethod(id)
    }

    suspend fun setDefaultPaymentMethod(id: String, customerId: String = "USR_101") {
        dao.setDefaultPaymentMethod(id, customerId)
    }

    suspend fun seedIfEmpty(defaultMethods: List<SavedPaymentMethod>) {
        if (dao.getSavedPaymentMethodCount() == 0) {
            dao.insertSavedPaymentMethods(defaultMethods.map { it.toEntity() })
        }
    }

    private fun SavedPaymentMethodEntity.toModel(): SavedPaymentMethod {
        return SavedPaymentMethod(
            id = id,
            title = title,
            subtitle = subtitle,
            type = type,
            isDefault = isDefault,
            customerId = customerId,
            bankOrProvider = bankOrProvider,
            maskedDetails = maskedDetails,
            lastFourDigits = lastFourDigits,
            cardNetwork = cardNetwork,
            cardExpiry = cardExpiry,
            gatewayTokenRef = gatewayTokenRef,
            upiVpa = upiVpa,
            createdAt = createdAt
        )
    }

    private fun SavedPaymentMethod.toEntity(): SavedPaymentMethodEntity {
        return SavedPaymentMethodEntity(
            id = id,
            customerId = customerId,
            title = title,
            subtitle = subtitle,
            type = type,
            bankOrProvider = bankOrProvider,
            maskedDetails = maskedDetails,
            lastFourDigits = lastFourDigits,
            cardNetwork = cardNetwork,
            cardExpiry = cardExpiry,
            gatewayTokenRef = gatewayTokenRef,
            upiVpa = upiVpa,
            isDefault = isDefault,
            createdAt = createdAt
        )
    }
}
