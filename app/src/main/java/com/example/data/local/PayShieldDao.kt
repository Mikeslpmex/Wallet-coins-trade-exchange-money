package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferenciaDao {
    @Query("SELECT * FROM transferencias ORDER BY createdAt DESC")
    fun getAllTransferencias(): Flow<List<TransferenciaEntity>>

    @Query("SELECT * FROM transferencias WHERE id = :id")
    suspend fun getTransferenciaById(id: Long): TransferenciaEntity?

    @Query("SELECT * FROM transferencias WHERE usuarioId = :usuarioId AND destinatario = :destinatario LIMIT 1")
    suspend fun getPreviousTransfer(usuarioId: String, destinatario: String): TransferenciaEntity?

    @Query("SELECT COUNT(*) FROM transferencias WHERE usuarioId = :usuarioId AND createdAt >= :sinceTimestamp")
    suspend fun countTransferenciasSince(usuarioId: String, sinceTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransferencia(transferencia: TransferenciaEntity): Long

    @Update
    suspend fun updateTransferencia(transferencia: TransferenciaEntity)

    @Query("SELECT COUNT(*) FROM transferencias WHERE estado = 'BLOQUEADA'")
    fun getBloqueadasCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM transferencias WHERE estado = 'REQUIERE_2FA'")
    fun getRequiere2FACount(): Flow<Int>
}

@Dao
interface TesoreroDao {
    @Query("SELECT * FROM tesorero_registros ORDER BY createdAt DESC")
    fun getAllRegistros(): Flow<List<TesoreroRegistroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistro(registro: TesoreroRegistroEntity): Long

    @Query("SELECT balanceNuevoUSD FROM tesorero_registros ORDER BY id DESC LIMIT 1")
    suspend fun getLatestBalance(): Double?
}

@Dao
interface AntifraudUmbralDao {
    @Query("SELECT * FROM antifraud_umbrales")
    fun getAllUmbrales(): Flow<List<AntifraudUmbralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUmbrales(umbrales: List<AntifraudUmbralEntity>)

    @Update
    suspend fun updateUmbral(umbral: AntifraudUmbralEntity)
}

@Dao
interface IpBlacklistDao {
    @Query("SELECT * FROM ip_blacklist")
    fun getAllBlacklist(): Flow<List<IpBlacklistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM ip_blacklist WHERE ipAddress = :ipAddress)")
    suspend fun isIpBlacklisted(ipAddress: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIp(ip: IpBlacklistEntity)

    @Delete
    suspend fun removeIp(ip: IpBlacklistEntity)
}
