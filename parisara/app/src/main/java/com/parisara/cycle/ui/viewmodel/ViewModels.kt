package com.parisara.cycle.ui.viewmodel

import android.net.Uri
import android.util.Log
import com.parisara.cycle.data.model.EcoCalculator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.parisara.cycle.data.model.*
import com.parisara.cycle.data.repository.*
import com.parisara.cycle.di.AppContainer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── Settings ───────────────────────────────────────────────────────────────

class SettingsViewModel(private val repo: UserPreferencesRepository) : ViewModel() {
    val notificationsEnabled: StateFlow<Boolean> = repo.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeOption: StateFlow<ThemeOption> = repo.themeOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeOption.SYSTEM)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repo.setNotificationsEnabled(enabled)
        }
    }

    fun setThemeOption(option: ThemeOption) {
        viewModelScope.launch {
            repo.setThemeOption(option)
        }
    }
}

// ─── Auth ───────────────────────────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isRegistrationSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.authState.collect { user ->
                if (!_state.value.isLoading) {
                    _state.update { it.copy(isLoggedIn = user != null) }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email and password cannot be empty") }
            return
        }
        viewModelScope.launch {
            Log.d("AuthViewModel", "Attempting login for: $email")
            _state.update { it.copy(isLoading = true, error = null) }
            authRepo.login(email, password)
                .onSuccess { 
                    Log.d("AuthViewModel", "Login successful")
                    _state.update { it.copy(isLoading = false, isLoggedIn = true) } 
                }
                .onFailure { e -> 
                    Log.e("AuthViewModel", "Login failed", e)
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Authentication failed") } 
                }
        }
    }

    fun register(email: String, password: String, profile: UserProfile, imageUri: Uri? = null, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email and password cannot be empty") }
            return
        }
        viewModelScope.launch {
            Log.d("AuthViewModel", "Attempting registration for: $email")
            _state.update { it.copy(isLoading = true, error = null, isRegistrationSuccess = false) }
            
            val authResult = authRepo.register(email, password)
            
            authResult.onSuccess { user ->
                try {
                    Log.d("AuthViewModel", "Auth registration successful, creating Firestore profile")
                    val imageUrl = imageUri?.let { uri ->
                        userRepo.uploadProfileImage(user.uid, uri).getOrNull()
                    } ?: ""
                    
                    val fullProfile = profile.copy(
                        uid = user.uid, 
                        email = email, 
                        profileImageUrl = imageUrl
                    )
                    
                    userRepo.createUser(fullProfile).getOrThrow()
                    
                    Log.d("AuthViewModel", "Firestore profile created successfully")
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            isLoggedIn = true,
                            isRegistrationSuccess = true 
                        ) 
                    }
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Firestore profile creation failed", e)
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Account created, but profile setup failed: ${e.message}" 
                        ) 
                    }
                }
            }.onFailure { e ->
                Log.e("AuthViewModel", "Auth registration failed", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepo.resetPassword(email)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}

// ─── Home / Dashboard ─────────────────────────────────────────────────────────

data class HomeUiState(
    val profile: UserProfile? = null,
    val ecoStats: EcoStats = EcoStats(),
    val ecoMessage: String = "",
    val isLoading: Boolean = true,
    val unreadNotifications: Int = 0
)

class HomeViewModel(
    private val userRepo: UserRepository,
    private val rideRepo: RideRepository,
    private val notifRepo: NotificationRepository,
    private val aiRepo: AiRepository,
    private val uid: String
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (uid.isBlank()) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            combine(
                userRepo.observeUser(uid),
                rideRepo.observeUserRides(uid),
                notifRepo.observeNotifications(uid)
            ) { profile, rides, notifs ->
                val stats = rideRepo.computeEcoStats(rides)
                Triple(profile, stats, notifs.count { !it.read })
            }.collect { (profile, stats, unread) ->
                _state.update {
                    it.copy(profile = profile, ecoStats = stats, unreadNotifications = unread, isLoading = false)
                }
                aiRepo.generateEcoMessage(stats.dailyCo2Grams, stats.dailyDistanceKm)
                    .onSuccess { msg -> _state.update { s -> s.copy(ecoMessage = msg) } }
            }
        }
    }
}

// ─── Map / Route ──────────────────────────────────────────────────────────────

data class MapUiState(
    val source: String = "",
    val destination: String = "",
    val sourceLatLng: LatLng? = null,
    val destLatLng: LatLng? = null,
    val currentLocation: LatLng? = null,
    val routeInfo: RouteInfo? = null,
    val dangerZones: List<DangerZone> = emptyList(),
    val reports: List<RouteReport> = emptyList(),
    val isLoading: Boolean = false,
    val isRiding: Boolean = false,
    val error: String? = null
)

class MapViewModel(
    private val routeRepo: RouteRepository,
    private val rideRepo: RideRepository,
    private val userRepo: UserRepository,
    private val reportRepo: ReportRepository,
    private val notificationRepo: NotificationRepository,
    private val buddyRepo: BuddyRepository,
    private val uid: String,
    private val userName: String
) : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            routeRepo.observeDangerZones().collect { zones ->
                _state.update { it.copy(dangerZones = zones) }
            }
        }
        viewModelScope.launch {
            reportRepo.observeReports().collect { reports ->
                val active = reports.filter {
                    it.status == ReportStatus.PENDING || it.status == ReportStatus.VERIFIED
                }
                _state.update { it.copy(reports = active) }
            }
        }
    }

    fun setSource(text: String, latLng: LatLng? = null) =
        _state.update { it.copy(source = text, sourceLatLng = latLng ?: it.sourceLatLng) }

    fun setDestination(text: String, latLng: LatLng? = null) =
        _state.update { it.copy(destination = text, destLatLng = latLng ?: it.destLatLng) }

    fun setCurrentLocation(latLng: LatLng) =
        _state.update { it.copy(currentLocation = latLng, sourceLatLng = it.sourceLatLng ?: latLng) }

    fun generateRoute() {
        val origin = _state.value.sourceLatLng ?: _state.value.currentLocation ?: return
        val dest = _state.value.destLatLng ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            routeRepo.getCyclingRoute(origin, dest)
                .onSuccess { route -> _state.update { it.copy(routeInfo = route, isLoading = false, isRiding = true) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun completeRide() {
        val route = _state.value.routeInfo ?: return
        val origin = _state.value.sourceLatLng ?: return
        val dest = _state.value.destLatLng ?: return
        viewModelScope.launch {
            val co2 = EcoCalculator.co2FromDistanceKm(route.distanceKm)
            val ride = RideRecord(
                userId = uid,
                sourceName = _state.value.source,
                destinationName = _state.value.destination,
                sourceLat = origin.latitude,
                sourceLng = origin.longitude,
                destLat = dest.latitude,
                destLng = dest.longitude,
                distanceKm = route.distanceKm,
                durationMinutes = route.durationMinutes,
                co2SavedGrams = co2,
                safetyScore = route.safetyScore,
                polylineEncoded = route.encodedPolyline
            )
            rideRepo.saveRide(ride)
            userRepo.addRideStats(uid, route.distanceKm, co2)
            notificationRepo.sendNotification(
                uid,
                "Ride Completed! \uD83C\uDF3F",
                "You saved ${EcoCalculator.formatCo2(co2)} CO₂ on this ${"%.1f".format(route.distanceKm)} km ride.",
                "achievement"
            )
            buddyRepo.stopSharing(uid)
            _state.update { it.copy(isRiding = false, routeInfo = null) }
        }
    }

    fun clearRoute() {
        viewModelScope.launch { buddyRepo.stopSharing(uid) }
        _state.update { it.copy(routeInfo = null, isRiding = false) }
    }
}

// ─── Pit Stops ────────────────────────────────────────────────────────────────

data class PitStopUiState(
    val stops: List<PitStop> = emptyList(),
    val filtered: List<PitStop> = emptyList(),
    val selectedCategory: String = "All",
    val selectedStop: PitStop? = null,
    val userLocation: LatLng? = null,
    val isLoading: Boolean = true
)

class PitStopViewModel(private val repo: PitStopRepository) : ViewModel() {
    private val _state = MutableStateFlow(PitStopUiState())
    val state: StateFlow<PitStopUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observePitStops().collect { stops ->
                applyFilter(stops)
            }
        }
    }

    fun setUserLocation(loc: LatLng) {
        _state.update { it.copy(userLocation = loc) }
        applyFilter(_state.value.stops)
    }

    fun setCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        applyFilter(_state.value.stops)
    }

    fun selectStop(stop: PitStop?) = _state.update { it.copy(selectedStop = stop) }

    private fun applyFilter(stops: List<PitStop>) {
        var filtered = repo.filterByCategory(stops, _state.value.selectedCategory)
        _state.value.userLocation?.let { loc ->
            filtered = repo.sortByDistance(filtered, loc)
        }
        _state.update { it.copy(stops = stops, filtered = filtered, isLoading = false) }
    }
}

// ─── Buddy ────────────────────────────────────────────────────────────────────

data class BuddyUiState(
    val isSharing: Boolean = false,
    val buddies: List<BuddyLocation> = emptyList(),
    val requests: List<BuddyRequest> = emptyList(),
    val message: String = "Ride together for better safety"
)

class BuddyViewModel(
    private val repo: BuddyRepository,
    private val uid: String,
    private val userName: String,
    private val profileImageUrl: String
) : ViewModel() {
    private val _state = MutableStateFlow(BuddyUiState())
    val state: StateFlow<BuddyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (uid.isNotBlank()) {
                repo.observeNearbyBuddies(uid).collect { list ->
                    _state.update { it.copy(buddies = list) }
                }
            }
        }
        viewModelScope.launch {
            if (uid.isNotBlank()) {
                repo.observeIncomingRequests(uid).collect { reqs ->
                    _state.update { it.copy(requests = reqs) }
                }
            }
        }
    }

    fun toggleSharing(enabled: Boolean, location: LatLng?) {
        viewModelScope.launch {
            if (enabled && location != null) {
                repo.updateLocation(
                    BuddyLocation(
                        userId = uid,
                        userName = userName,
                        profileImageUrl = profileImageUrl,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isSharing = true
                    )
                )
                _state.update { it.copy(isSharing = true) }
            } else {
                repo.stopSharing(uid)
                _state.update { it.copy(isSharing = false) }
            }
        }
    }

    fun sendRequest(toUserId: String) {
        viewModelScope.launch { repo.sendBuddyRequest(uid, userName, toUserId) }
    }

    fun respondRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch { repo.respondToRequest(requestId, accept) }
    }
}

// ─── Report ───────────────────────────────────────────────────────────────────

data class ReportUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class ReportViewModel(
    private val repo: ReportRepository,
    private val uid: String,
    private val userName: String
) : ViewModel() {
    private val _state = MutableStateFlow(ReportUiState())
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    fun submit(issueType: String, description: String, location: GeoPoint, imageUri: Uri?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.submitReport(uid, userName, issueType, description, location, imageUri)
                .onSuccess { _state.update { it.copy(isLoading = false, success = true) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun reset() = _state.update { ReportUiState() }
}

// ─── Profile ──────────────────────────────────────────────────────────────────

class ProfileViewModel(
    private val userRepo: UserRepository,
    val uid: String
) : ViewModel() {
    val profile: StateFlow<UserProfile?> = if (uid.isNotBlank()) {
        userRepo.observeUser(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null).asStateFlow()
    }
}

// ─── Ride History ─────────────────────────────────────────────────────────────

class RideHistoryViewModel(
    private val rideRepo: RideRepository,
    uid: String
) : ViewModel() {
    val rides: StateFlow<List<RideRecord>> = if (uid.isNotBlank()) {
        rideRepo.observeUserRides(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        MutableStateFlow(emptyList<RideRecord>()).asStateFlow()
    }
    
    val ecoStats: StateFlow<EcoStats> = rides.map { rideRepo.computeEcoStats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EcoStats())
}

// ─── Admin ────────────────────────────────────────────────────────────────────

class AdminViewModel(
    private val reportRepo: ReportRepository,
    private val adminRepo: AdminRepository
) : ViewModel() {
    val reports: StateFlow<List<RouteReport>> = reportRepo.observeReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateReportStatus(id: String, status: String) {
        viewModelScope.launch { reportRepo.updateStatus(id, status) }
    }

    fun addDangerZone(title: String, desc: String, severity: String, location: GeoPoint) {
        viewModelScope.launch { adminRepo.addDangerZone(title, desc, severity, location) }
    }

    fun addPitStop(stop: PitStop) {
        viewModelScope.launch { adminRepo.addPitStop(stop) }
    }
}

// ─── AI Safety Tips ───────────────────────────────────────────────────────────

class SafetyTipsViewModel(private val aiRepo: AiRepository) : ViewModel() {
    val tips = SampleData.safetyTips
    private val _aiTip = MutableStateFlow<String?>(null)
    val aiTip: StateFlow<String?> = _aiTip.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun generatePersonalizedTip(context: String) {
        viewModelScope.launch {
            _loading.value = true
            aiRepo.generateSafetyAdvice(context)
                .onSuccess { _aiTip.value = it }
            _loading.value = false
        }
    }
}

// ─── Factory ──────────────────────────────────────────────────────────────────

object AppContainerHolder {
    lateinit var container: AppContainer
}

class ParisaraViewModelFactory(
    private val container: AppContainer,
    private val uid: String = "",
    private val userName: String = "",
    private val profileImageUrl: String = ""
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(container.userPreferencesRepository) as T
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(container.authRepository, container.userRepository) as T
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(container.userRepository, container.rideRepository, container.notificationRepository, container.aiRepository, uid) as T
        modelClass.isAssignableFrom(MapViewModel::class.java) ->
            MapViewModel(container.routeRepository, container.rideRepository, container.userRepository, container.reportRepository, container.notificationRepository, container.buddyRepository, uid, userName) as T
        modelClass.isAssignableFrom(PitStopViewModel::class.java) ->
            PitStopViewModel(container.pitStopRepository) as T
        modelClass.isAssignableFrom(BuddyViewModel::class.java) ->
            BuddyViewModel(container.buddyRepository, uid, userName, profileImageUrl) as T
        modelClass.isAssignableFrom(ReportViewModel::class.java) ->
            ReportViewModel(container.reportRepository, uid, userName) as T
        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(container.userRepository, uid) as T
        modelClass.isAssignableFrom(RideHistoryViewModel::class.java) ->
            RideHistoryViewModel(container.rideRepository, uid) as T
        modelClass.isAssignableFrom(AdminViewModel::class.java) ->
            AdminViewModel(container.reportRepository, container.adminRepository) as T
        modelClass.isAssignableFrom(SafetyTipsViewModel::class.java) ->
            SafetyTipsViewModel(container.aiRepository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
