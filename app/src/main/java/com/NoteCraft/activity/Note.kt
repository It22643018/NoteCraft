package com.NoteCraft.activity

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val date: String,
    val updatedDate: String = "",
    val time: String = "",
    val updatedTime: String = ""
)
