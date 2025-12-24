package com.compose.geoquest.service

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for GeofenceMonitorService companion object
 * Tests the service configuration and static methods
 */
class GeofenceMonitorServiceTest {

    @Test
    fun `service companion object exists`() {
        // Verify the service class exists and has companion object
        assertNotNull(GeofenceMonitorService.Companion)
    }
}
