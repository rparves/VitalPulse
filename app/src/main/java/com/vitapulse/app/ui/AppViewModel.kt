package com.vitapulse.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitapulse.app.data.*
import com.vitapulse.app.food.FoodCandidate
import com.vitapulse.app.health.DaySummary
import com.vitapulse.app.health.HealthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class Loaded(val profile: Profile?)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val dao = db.dao()
    val health = HealthRepo(app)
    private val day = MutableStateFlow(LocalDate.now())

    val profile: StateFlow<Loaded?> = dao.profileFlow().map { Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val foodToday: StateFlow<List<FoodEntry>> = day.flatMapLatest { dao.foodForDay(it.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val waterToday: StateFlow<Int> = day.flatMapLatest { dao.waterForDay(it.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val kcalWeek: StateFlow<List<Pair<LocalDate, Double>>> = day.flatMapLatest { d ->
        dao.kcalByDay(d.minusDays(6).toEpochDay()).map { rows ->
            (0..6).map { i ->
                val dd = d.minusDays(6L - i)
                dd to (rows.firstOrNull { it.day == dd.toEpochDay() }?.total ?: 0.0)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val weights: StateFlow<List<WeightEntry>> = dao.recentWeights(30)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val today = MutableStateFlow(DaySummary(LocalDate.now()))
    val stepsWeek = MutableStateFlow<List<Pair<LocalDate, Long>>>(emptyList())
    val hcStepsGranted = MutableStateFlow(false)

    init { refreshHealth() }

    fun onResume() {
        day.value = LocalDate.now()
        refreshHealth()
    }

    fun refreshHealth() {
        viewModelScope.launch {
            if (!health.isAvailable()) { hcStepsGranted.value = false; return@launch }
            hcStepsGranted.value = HealthRepo.STEPS in health.granted()
            runCatching { today.value = health.dailySummary(LocalDate.now()) }
            runCatching { stepsWeek.value = health.weeklySteps(LocalDate.now()) }
        }
    }

    fun saveProfile(p: Profile) = viewModelScope.launch { dao.saveProfile(p) }

    fun logFood(c: FoodCandidate, grams: Double, meal: String) = viewModelScope.launch {
        val f = grams / 100.0
        dao.insertFood(
            FoodEntry(
                epochDay = LocalDate.now().toEpochDay(),
                timestamp = System.currentTimeMillis(),
                meal = meal,
                name = c.name.take(120),
                grams = grams,
                kcal = c.kcal100 * f,
                proteinG = c.protein100 * f,
                carbsG = c.carbs100 * f,
                fatG = c.fat100 * f,
                source = c.source,
            )
        )
    }

    fun deleteFood(e: FoodEntry) = viewModelScope.launch { dao.deleteFood(e) }

    fun addWater(ml: Int) = viewModelScope.launch {
        dao.insertWater(WaterEntry(epochDay = LocalDate.now().toEpochDay(), timestamp = System.currentTimeMillis(), ml = ml))
    }

    fun logWeight(kg: Double) = viewModelScope.launch {
        dao.insertWeight(WeightEntry(epochDay = LocalDate.now().toEpochDay(), timestamp = System.currentTimeMillis(), kg = kg))
        dao.profile()?.let { dao.saveProfile(it.copy(weightKg = kg)) }
    }

    fun wipeAllData() = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.clearAllTables() }
    }
}
