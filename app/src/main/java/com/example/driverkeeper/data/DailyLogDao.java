package com.example.driverkeeper.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    DailyLog getByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DailyLog log);

    @Update
    int update(DailyLog log);

    @Query("SELECT * FROM daily_logs WHERE date < :today ORDER BY date DESC")
    List<DailyLog> getBefore(String today);

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    List<DailyLog> getBetween(String start, String end);
}