package com.oralvis.oralvisclient.di

import com.oralvis.oralvisclient.core.network.ApiClient
import com.oralvis.oralvisclient.core.session.SessionManager
import com.oralvis.oralvisclient.core.util.DefaultDispatcherProvider
import com.oralvis.oralvisclient.core.util.DispatcherProvider
import com.oralvis.oralvisclient.data.remote.AuthApi
import com.oralvis.oralvisclient.data.remote.BookingApi
import com.oralvis.oralvisclient.data.remote.CalendarApi
import com.oralvis.oralvisclient.data.remote.ClinicalApi
import com.oralvis.oralvisclient.data.remote.ClinicApi
import com.oralvis.oralvisclient.data.repository.AuthRepositoryImpl
import com.oralvis.oralvisclient.data.repository.BookingRepositoryImpl
import com.oralvis.oralvisclient.data.repository.CalendarRepositoryImpl
import com.oralvis.oralvisclient.data.repository.ClinicalRepositoryImpl
import com.oralvis.oralvisclient.data.repository.ClinicRepositoryImpl
import com.oralvis.oralvisclient.domain.repository.AuthRepository
import com.oralvis.oralvisclient.domain.repository.BookingRepository
import com.oralvis.oralvisclient.domain.repository.CalendarRepository
import com.oralvis.oralvisclient.domain.repository.ClinicalRepository
import com.oralvis.oralvisclient.domain.repository.ClinicRepository
import com.oralvis.oralvisclient.domain.usecase.AddBookingNotesUseCase
import com.oralvis.oralvisclient.domain.usecase.BookWalkInUseCase
import com.oralvis.oralvisclient.domain.usecase.CancelBookingUseCase
import com.oralvis.oralvisclient.domain.usecase.CancelBookingsByDateUseCase
import com.oralvis.oralvisclient.domain.usecase.GetAppointmentsByDateUseCase
import com.oralvis.oralvisclient.domain.usecase.GetAppointmentsUseCase
import com.oralvis.oralvisclient.domain.usecase.GetCalendarDateUseCase
import com.oralvis.oralvisclient.domain.usecase.GetCalendarMonthUseCase
import com.oralvis.oralvisclient.domain.usecase.GetCalendarWeekUseCase
import com.oralvis.oralvisclient.domain.usecase.GetClinicalRecordUseCase
import com.oralvis.oralvisclient.domain.usecase.GetCurrentUserUseCase
import com.oralvis.oralvisclient.domain.usecase.GetDashboardStatsUseCase
import com.oralvis.oralvisclient.domain.usecase.GetSlotsUseCase
import com.oralvis.oralvisclient.domain.usecase.LoginUseCase
import com.oralvis.oralvisclient.domain.usecase.LogoutUseCase
import com.oralvis.oralvisclient.domain.usecase.MarkBookingPaidUseCase
import com.oralvis.oralvisclient.domain.usecase.RefreshTokenUseCase
import com.oralvis.oralvisclient.domain.usecase.ResolveClinicIdUseCase
import com.oralvis.oralvisclient.domain.usecase.RescheduleBookingUseCase
import com.oralvis.oralvisclient.domain.usecase.SaveClinicalRecordUseCase
import com.oralvis.oralvisclient.ui.viewmodel.AppointmentViewModel
import com.oralvis.oralvisclient.ui.viewmodel.AuthViewModel
import com.oralvis.oralvisclient.ui.viewmodel.CalendarViewModel
import com.oralvis.oralvisclient.ui.viewmodel.ClinicalViewModel
import com.oralvis.oralvisclient.ui.viewmodel.DashboardViewModel

/**
 * Application dependency graph. Provides repositories and ViewModel factory.
 * Do not modify repositories/use cases; this only wires existing implementations.
 */
object AppGraph {

    private val sessionManager: SessionManager by lazy { SessionManager() }
    private val dispatcherProvider: DispatcherProvider by lazy { DefaultDispatcherProvider() }

    private val authApi: AuthApi by lazy { ApiClient.create(AuthApi::class.java) }
    private val clinicApi: ClinicApi by lazy { ApiClient.create(ClinicApi::class.java) }
    private val bookingApi: BookingApi by lazy { ApiClient.create(BookingApi::class.java) }
    private val calendarApi: CalendarApi by lazy { ApiClient.create(CalendarApi::class.java) }
    private val clinicalApi: ClinicalApi by lazy { ApiClient.create(ClinicalApi::class.java) }

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApi, sessionManager)
    }
    private val clinicRepository: ClinicRepository by lazy {
        ClinicRepositoryImpl(clinicApi)
    }
    private val bookingRepository: BookingRepository by lazy {
        BookingRepositoryImpl(bookingApi)
    }
    private val calendarRepository: CalendarRepository by lazy {
        CalendarRepositoryImpl(calendarApi)
    }
    private val clinicalRepository: ClinicalRepository by lazy {
        ClinicalRepositoryImpl(clinicalApi)
    }

    private val loginUseCase by lazy { LoginUseCase(authRepository) }
    private val refreshTokenUseCase by lazy { RefreshTokenUseCase(authRepository) }
    private val getCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    private val logoutUseCase by lazy { LogoutUseCase(authRepository) }
    private val resolveClinicIdUseCase by lazy { ResolveClinicIdUseCase(clinicRepository) }
    private val getDashboardStatsUseCase by lazy { GetDashboardStatsUseCase(clinicRepository) }
    private val getAppointmentsUseCase by lazy { GetAppointmentsUseCase(clinicRepository) }
    private val getAppointmentsByDateUseCase by lazy { GetAppointmentsByDateUseCase(clinicRepository) }
    private val cancelBookingUseCase by lazy { CancelBookingUseCase(clinicRepository) }
    private val cancelBookingsByDateUseCase by lazy { CancelBookingsByDateUseCase(clinicRepository) }
    private val markBookingPaidUseCase by lazy { MarkBookingPaidUseCase(clinicRepository) }
    private val addBookingNotesUseCase by lazy { AddBookingNotesUseCase(clinicRepository) }
    private val getCalendarMonthUseCase by lazy { GetCalendarMonthUseCase(calendarRepository) }
    private val getCalendarDateUseCase by lazy { GetCalendarDateUseCase(calendarRepository) }
    private val getCalendarWeekUseCase by lazy { GetCalendarWeekUseCase(calendarRepository) }
    private val getClinicalRecordUseCase by lazy { GetClinicalRecordUseCase(clinicalRepository) }
    private val saveClinicalRecordUseCase by lazy { SaveClinicalRecordUseCase(clinicalRepository) }

    fun sessionManager(): SessionManager = sessionManager

    fun createAuthViewModel(): AuthViewModel = AuthViewModel(
        loginUseCase = loginUseCase,
        refreshTokenUseCase = refreshTokenUseCase,
        getCurrentUserUseCase = getCurrentUserUseCase,
        logoutUseCase = logoutUseCase,
        dispatcherProvider = dispatcherProvider
    )

    fun createDashboardViewModel(): DashboardViewModel = DashboardViewModel(
        clinicRepository = clinicRepository,
        dispatcherProvider = dispatcherProvider
    )

    fun createAppointmentViewModel(): AppointmentViewModel = AppointmentViewModel(
        getAppointmentsUseCase = getAppointmentsUseCase,
        getAppointmentsByDateUseCase = getAppointmentsByDateUseCase,
        cancelBookingUseCase = cancelBookingUseCase,
        cancelBookingsByDateUseCase = cancelBookingsByDateUseCase,
        markBookingPaidUseCase = markBookingPaidUseCase,
        addBookingNotesUseCase = addBookingNotesUseCase,
        dispatcherProvider = dispatcherProvider
    )

    fun createCalendarViewModel(): CalendarViewModel = CalendarViewModel(
        getCalendarMonthUseCase = getCalendarMonthUseCase,
        getCalendarDateUseCase = getCalendarDateUseCase,
        getCalendarWeekUseCase = getCalendarWeekUseCase,
        dispatcherProvider = dispatcherProvider
    )

    fun createClinicalViewModel(): ClinicalViewModel = ClinicalViewModel(
        getClinicalRecordUseCase = getClinicalRecordUseCase,
        saveClinicalRecordUseCase = saveClinicalRecordUseCase,
        clinicalRepository = clinicalRepository,
        dispatcherProvider = dispatcherProvider
    )
}
