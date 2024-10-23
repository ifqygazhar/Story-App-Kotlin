package com.example.storyapp

import com.example.storyapp.data.local.database.ListStoryItem
import java.util.Random
import java.util.UUID

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 1..100) {
            val story = ListStoryItem(
                photoUrl = "https://picsum.photos/200/300?random=$i", // URL gambar acak
                createdAt = "2024-10-${i % 31 + 1}T12:34:56Z", // Tanggal acak
                name = "Story $i", // Nama cerita dengan penomoran
                description = "This is a description for Story $i", // Deskripsi cerita
                lon = (100.0..140.0).random(), // Longitude acak
                id = UUID.randomUUID().toString(), // ID unik
                lat = (-10.0..10.0).random() // Latitude acak
            )
            items.add(story)
        }
        return items
    }

    private fun ClosedRange<Double>.random() =
        Random().nextDouble() * (endInclusive - start) + start
}

