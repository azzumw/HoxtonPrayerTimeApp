package com.hoxtonislah.hoxtonprayertimeapp.util

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hoxtonislah.hoxtonprayertimeapp.data.source.local.PrayerBeginningTimesDatabase
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class DatabaseRule: TestWatcher() {

    private var _database:PrayerBeginningTimesDatabase? = null
    val database get() = _database!!
    override fun starting(description: Description) {
        super.starting(description)
        _database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PrayerBeginningTimesDatabase::class.java)
            .build()
    }

    override fun finished(description: Description) {
        super.finished(description)
        _database?.clearAllTables()
        _database?.close()
        _database = null
    }
}