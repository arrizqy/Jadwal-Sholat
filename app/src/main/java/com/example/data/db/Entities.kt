package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val arabicText: String,
    val translationText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey
    val id: Int = 1,
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
