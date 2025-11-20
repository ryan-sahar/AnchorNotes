package com.example.anchornotes.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Simple Tag entity for labeling notes.
 * Feature 2a (Smart Organization - Tags).
 */
@Entity(tableName = "tags")
public class Tag {

    @PrimaryKey
    @NonNull
    private UUID id;

    private String name;

    public Tag() {
        // Required by Room
    }

    public Tag(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
