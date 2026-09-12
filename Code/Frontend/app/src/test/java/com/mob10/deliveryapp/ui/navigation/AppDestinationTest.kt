package com.mob10.deliveryapp.ui.navigation

import com.mob10.deliveryapp.data.model.Role
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun `unauthenticated user goes to login`() {
        assertEquals(AppDestination.LOGIN, destinationFor(null))
    }

    @Test
    fun `client goes to client home`() {
        assertEquals(AppDestination.CLIENT_HOME, destinationFor(Role.CLIENT))
    }

    @Test
    fun `delivery goes to delivery home`() {
        assertEquals(AppDestination.DELIVERY_HOME, destinationFor(Role.DELIVERY))
    }

    @Test
    fun `admin goes to admin home`() {
        assertEquals(AppDestination.ADMIN_HOME, destinationFor(Role.ADMIN))
    }
}
