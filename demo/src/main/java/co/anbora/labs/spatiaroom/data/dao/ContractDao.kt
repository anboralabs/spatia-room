package co.anbora.labs.spatiaroom.data.dao

import androidx.room.*
import co.anbora.labs.spatiaroom.data.model.Contract
import io.reactivex.Single

@Dao
interface ContractDao {

    @Query("SELECT * FROM contract")
    fun allContracts(): Single<List<Contract>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dbitem: Contract)

    @Delete
    fun delete(contract: Contract?)

    @Query("SELECT * FROM contract WHERE id = :id")
    fun getById(id: Long): Contract?

}