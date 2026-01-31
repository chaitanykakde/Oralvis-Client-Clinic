package com.oralvis.oralvisclient.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory that creates ViewModels using AppGraph.
 * Do not modify repositories/use cases; only wiring.
 */
class OralVisViewModelFactory(
    private val appGraph: AppGraph
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(com.oralvis.oralvisclient.ui.viewmodel.AuthViewModel::class.java) ->
                appGraph.createAuthViewModel() as T
            modelClass.isAssignableFrom(com.oralvis.oralvisclient.ui.viewmodel.DashboardViewModel::class.java) ->
                appGraph.createDashboardViewModel() as T
            modelClass.isAssignableFrom(com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel::class.java) ->
                appGraph.createAppointmentViewModel() as T
            modelClass.isAssignableFrom(com.oralvis.oralvisclient.ui.viewmodel.CalendarViewModel::class.java) ->
                appGraph.createCalendarViewModel() as T
            modelClass.isAssignableFrom(com.oralvis.oralvisclient.ui.viewmodel.ClinicalViewModel::class.java) ->
                appGraph.createClinicalViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
