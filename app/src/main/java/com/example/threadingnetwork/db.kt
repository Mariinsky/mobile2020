package com.example.threadingnetwork

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity
data class User (
    @PrimaryKey(autoGenerate = true)
    val uid: Long,
    val firstname: String,
    val lastname: String ) {

    override fun toString() = "$uid $firstname $lastname"
}

@Entity(foreignKeys = [(ForeignKey(
    entity = User::class,
    parentColumns = ["uid"],
    childColumns = ["user"]
))])
data class ContactInfo (
    val user: Long,
    val type: String,
    @PrimaryKey
    val value: String
)

class UserContact {
    @Embedded
    var user: User? = null
    @Relation(parentColumn = "uid", entityColumn = "user")
    var contacts: List<ContactInfo>? = null
}

@Dao
interface ContactInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: ContactInfo)
}
@Dao
interface  UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): MutableList<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Update
    fun update(user:User)

    @Query("SELECT * FROM user WHERE user.uid = :userid")
    fun getUserContacts(userid: Long): UserContact
}



@Database( entities = [(User::class), (ContactInfo::class)], version = 1)
abstract class UserDB: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactInfoDao

    companion object {
        private var sInstance: UserDB? = null
        @Synchronized
        fun get(context: Context): UserDB {
            if (sInstance == null) {
                sInstance =
                    Room.databaseBuilder(context.applicationContext,
                        UserDB::class.java, "user.db").build()
            }
            return sInstance!!
        }
    }
}