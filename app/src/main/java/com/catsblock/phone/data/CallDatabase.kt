package com.catsblock.phone.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [], version = 1, exportSchema = false)
abstract class CallDatabase : RoomDatabase() {
    // DAOs e definições do banco de dados Room para histórico e gravações
}
