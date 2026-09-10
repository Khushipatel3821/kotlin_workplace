package com.fitpulse.app.data.repository

import com.fitpulse.app.data.local.dao.YogaDao
import com.fitpulse.app.data.local.entity.YogaSessionEntity
import com.fitpulse.app.domain.model.YogaDifficulty
import com.fitpulse.app.domain.model.YogaPose
import com.fitpulse.app.domain.model.YogaRoutine
import com.fitpulse.app.domain.model.YogaSession
import com.fitpulse.app.domain.repository.YogaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class YogaRepositoryImpl(
    private val yogaDao: YogaDao
) : YogaRepository {

    private val defaultPoses = listOf(
        YogaPose(
            id = 1,
            englishName = "Downward-Facing Dog",
            sanskritName = "Adho Mukha Svanasana",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Hamstrings, Calves, Shoulders, Spine",
            benefits = "Decompresses spine, calms the mind, energizes the whole body.",
            defaultHoldSeconds = 45,
            cues = listOf("Spread fingers wide", "Press chest towards thighs", "Engage quadriceps", "Relax neck")
        ),
        YogaPose(
            id = 2,
            englishName = "Warrior II",
            sanskritName = "Virabhadrasana II",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Hips, Groin, Chest, Shoulders",
            benefits = "Strengthens legs and ankles, increases stamina and hip mobility.",
            defaultHoldSeconds = 40,
            cues = listOf("Front knee stacked over ankle", "Arms parallel to ground", "Gaze over front fingertips", "Ground back foot edge")
        ),
        YogaPose(
            id = 3,
            englishName = "Tree Pose",
            sanskritName = "Vrikshasana",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Ankles, Calves, Core, Balance",
            benefits = "Improves neuromuscular balance, focus, and core stability.",
            defaultHoldSeconds = 30,
            cues = listOf("Fix gaze on single point", "Press foot into inner thigh or calf (avoid knee)", "Hands in prayer at heart", "Root through standing foot")
        ),
        YogaPose(
            id = 4,
            englishName = "Cobra Pose",
            sanskritName = "Bhujangasana",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Chest, Lungs, Shoulders, Abdomen",
            benefits = "Strengthens spine, opens chest and lungs, stimulates abdominal organs.",
            defaultHoldSeconds = 30,
            cues = listOf("Press tops of feet down", "Keep elbows slightly bent and tucked", "Lift chest using back muscles", "Keep neck long")
        ),
        YogaPose(
            id = 5,
            englishName = "Child's Pose",
            sanskritName = "Balasana",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Hips, Thighs, Ankles, Back",
            benefits = "Gently stretches hips and lower back, soothes central nervous system.",
            defaultHoldSeconds = 60,
            cues = listOf("Big toes touching, knees wide", "Rest forehead on mat", "Extend arms forward", "Breathe into back ribs")
        )
    )

    private val defaultRoutines = listOf(
        YogaRoutine(
            id = 1,
            title = "Morning Awakening Flow",
            description = "Gentle flow to invigorate your body and clear morning mental fog.",
            difficulty = YogaDifficulty.BEGINNER,
            estimatedMinutes = 15,
            poses = defaultPoses.take(4)
        ),
        YogaRoutine(
            id = 2,
            title = "Post-Workout Hip & Spine Release",
            description = "Deep hip openers and spinal decompression for optimal recovery.",
            difficulty = YogaDifficulty.INTERMEDIATE,
            estimatedMinutes = 20,
            poses = defaultPoses
        )
    )

    override fun getAllPoses(): Flow<List<YogaPose>> = flowOf(defaultPoses)

    override fun getAllRoutines(): Flow<List<YogaRoutine>> = flowOf(defaultRoutines)

    override fun getRoutineById(id: Long): Flow<YogaRoutine?> = flowOf(
        defaultRoutines.find { it.id == id }
    )

    override fun getAllYogaSessions(): Flow<List<YogaSession>> {
        return yogaDao.getAllYogaSessions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveYogaSession(session: YogaSession): Long {
        return yogaDao.insertYogaSession(YogaSessionEntity.fromDomain(session))
    }

    override suspend fun insertPoses(poses: List<YogaPose>) {
        // In-memory or database
    }

    override suspend fun insertRoutines(routines: List<YogaRoutine>) {
        // In-memory or database
    }
}
