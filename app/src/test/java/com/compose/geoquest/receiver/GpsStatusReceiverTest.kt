package com.compose.geoquest.receiver

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GpsStatusReceiver
 */
class GpsStatusReceiverTest {

    private lateinit var context: Context
    private lateinit var locationManager: LocationManager
    private lateinit var receiver: GpsStatusReceiver

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        locationManager = mockk(relaxed = true)
        receiver = GpsStatusReceiver()

        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
    }

    @Test
    fun `checkGpsStatus returns true when GPS enabled`() {
        // Given
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns true

        // When
        val result = GpsStatusReceiver.checkGpsStatus(context)

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkGpsStatus returns false when GPS disabled`() {
        // Given
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns false

        // When
        val result = GpsStatusReceiver.checkGpsStatus(context)

        // Then
        assertFalse(result)
    }

    @Test
    fun `checkGpsStatus updates StateFlow`() = runTest {
        // Given
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns true

        // When
        GpsStatusReceiver.checkGpsStatus(context)

        // Then
        val flowValue = GpsStatusReceiver.isGpsEnabled.first()
        assertTrue(flowValue)
    }

    @Test
    fun `onReceive updates StateFlow when GPS enabled`() = runTest {
        // Given
        val intent = mockk<Intent>(relaxed = true)
        every { intent.action } returns LocationManager.PROVIDERS_CHANGED_ACTION
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns true

        // When
        receiver.onReceive(context, intent)

        // Then
        val flowValue = GpsStatusReceiver.isGpsEnabled.first()
        assertTrue(flowValue)
    }

    @Test
    fun `onReceive updates StateFlow when GPS disabled`() = runTest {
        // Given
        val intent = mockk<Intent>(relaxed = true)
        every { intent.action } returns LocationManager.PROVIDERS_CHANGED_ACTION
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns false

        // When
        receiver.onReceive(context, intent)

        // Then
        val flowValue = GpsStatusReceiver.isGpsEnabled.first()
        assertFalse(flowValue)
    }

    @Test
    fun `onReceive ignores non-provider-change intents`() = runTest {
        // Given - set initial state to true
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns true
        GpsStatusReceiver.checkGpsStatus(context)

        // When - send a different intent action
        val intent = mockk<Intent>(relaxed = true)
        every { intent.action } returns "some.other.action"
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns false

        receiver.onReceive(context, intent)

        // Then - state should remain unchanged (true)
        val flowValue = GpsStatusReceiver.isGpsEnabled.first()
        assertTrue(flowValue)
    }
}

