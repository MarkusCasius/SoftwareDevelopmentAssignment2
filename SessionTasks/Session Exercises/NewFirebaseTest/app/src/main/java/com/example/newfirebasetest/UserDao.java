package com.example.newfirebasetest;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insertAll(User... users);  // Insert multiple users

    @Query("SELECT * FROM User")  // Fetch all users
    List<User> getAllUsers();
}
