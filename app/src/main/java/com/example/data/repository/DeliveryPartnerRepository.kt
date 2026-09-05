package com.example.data.repository

import com.example.data.local.dao.DeliveryPartnerDao
import com.example.data.local.entity.DeliveryPartnerEntity
import com.example.data.model.DeliveryPartner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeliveryPartnerRepository(private val partnerDao: DeliveryPartnerDao) {

    fun getAllPartners(): Flow<List<DeliveryPartner>> {
        return partnerDao.getAllPartners().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun savePartner(partner: DeliveryPartner) {
        partnerDao.insertPartner(partner.toEntity())
    }

    suspend fun updateAssignment(partnerId: String, isAvailable: Boolean, currentOrderId: String?, activeCount: Int) {
        partnerDao.updatePartnerAssignment(partnerId, isAvailable, currentOrderId, activeCount)
    }

    private fun DeliveryPartnerEntity.toModel(): DeliveryPartner {
        return DeliveryPartner(
            partnerId = partnerId,
            name = name,
            phone = phone,
            vehicle = vehicle,
            isAvailable = isAvailable,
            currentOrderId = currentOrderId,
            activeDeliveriesCount = activeDeliveriesCount,
            rating = rating
        )
    }

    private fun DeliveryPartner.toEntity(): DeliveryPartnerEntity {
        return DeliveryPartnerEntity(
            partnerId = partnerId,
            name = name,
            phone = phone,
            vehicle = vehicle,
            isAvailable = isAvailable,
            currentOrderId = currentOrderId,
            activeDeliveriesCount = activeDeliveriesCount,
            rating = rating
        )
    }
}
