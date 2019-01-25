package com.kantek.chatsdk.datasource;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Update;
import android.content.Context;

import java.util.Collection;
import java.util.List;

import com.kantek.chatsdk.models.MessageEntry;

@Database(entities = {MessageEntry.class}, version = 1, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {
    public static ChatDatabase sInstance;

    public static ChatDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ChatDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            ChatDatabase.class, "chat_database")
                            .build();
                }
            }
        }
        return sInstance;
    }

    public abstract MessageDao messageDao();

    @Dao
    public interface MessageDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addAll(List<MessageEntry> messageEntries);

        @Query("select * from MessageEntry order by mTimeReceived asc")
        List<MessageEntry> getAll();

        @Query("select * from MessageEntry where (mFromId = :id1 and mToId = :id2) or ( mFromId = :id2 and mToId = :id1) order by mTimeReceived asc")
        List<MessageEntry> getByPairChat(String id1, String id2);

        @Update(onConflict = OnConflictStrategy.REPLACE)
        void update(MessageEntry messageEntry);

        @Update(onConflict = OnConflictStrategy.REPLACE)
        void update(Collection<MessageEntry> messageEntryMap);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void add(MessageEntry messageEntry);
    }
}
