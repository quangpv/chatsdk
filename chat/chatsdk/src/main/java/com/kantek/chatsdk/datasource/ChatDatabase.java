package com.kantek.chatsdk.datasource;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.models.MessageEntry;
import com.kantek.chatsdk.models.ReceiptState;

import java.util.List;

@Database(entities = {MessageEntry.class, Contact.class}, version = 1, exportSchema = false)
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

    public abstract ContactDao contactDao();

    @Dao
    public interface MessageDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addAll(List<MessageEntry> messageEntries);

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        void addAllIgnore(List<MessageEntry> messageEntries);

        @Query("select * from MessageEntry where (mFromId = :id1 and mToId = :id2) or ( mFromId = :id2 and mToId = :id1)" +
                " order by mTimeReceived desc" +
                " limit :index,:pageSize"
        )
        List<MessageEntry> getMostRecent(String id1, String id2, int index, int pageSize);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void add(MessageEntry messageEntry);

        @Query("update MessageEntry set mReceipt=" + ReceiptState.READ +
                " where (mId=:messageId or (mFromId=:fromId and mToId=:toId))" +
                " and mReceipt!=" + ReceiptState.READ)
        void updateRead(String messageId, String fromId, String toId);

        @Query("update MessageEntry set mReceipt=:state" +
                " where (mId=:messageId or (mFromId=:fromId and mToId=:toId)) " +
                " and mReceipt!=" + ReceiptState.READ)
        void updateReceipt(String messageId, String fromId, String toId, int state);

        @Query("select * from MessageEntry where mId=:id")
        MessageEntry get(String id);
    }

    @Dao
    public interface ContactDao {

        @Query("update Contact set mNumOfUnread=mNumOfUnread+:number where mContactId=:withId and mMyId=:myId")
        void addUnRead(String withId, String myId, int number);

        @Query("update Contact set mNumOfUnread=mNumOfUnread+1" +
                " where (mContactId=:id1 and mMyId=:id2) or (mContactId=:id2 and mMyId=:id1)")
        void increaseUnRead(String id1, String id2);

        @Query("update Contact set mNumOfUnread=0" +
                " where (mContactId=:id1 and mMyId=:id2) or (mContactId=:id2 and mMyId=:id1)")
        void markToRead(String id1, String id2);

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        void addAll(List<Contact> contacts);

        @Query("select mNumOfUnread from Contact where ((mContactId=:id1 and mMyId=:id2) or (mContactId=:id2 and mMyId=:id1))")
        int getNumOfUnread(String id1, String id2);

        @Query("select * from Contact")
        List<Contact> getPrivate();

        @Query("select * from Contact" +
                " where (mContactId=:id1 and mMyId=:id2) or (mContactId=:id2 and mMyId=:id1)")
        Contact get(String id1, String id2);

    }
}
