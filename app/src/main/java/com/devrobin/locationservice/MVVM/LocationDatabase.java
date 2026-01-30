package com.devrobin.locationservice.MVVM;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LocationData.class}, version = 4, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    public abstract LocationDAO locationDAO();

    private static LocationDatabase instance;


    public static synchronized LocationDatabase getInstance(Context context){

        if (instance == null){

            instance = Room.databaseBuilder(context.getApplicationContext(),
                    LocationDatabase.class, "locationDB")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

}
