package io.gnosis.kouban.data.repositories

import io.gnosis.kouban.data.backend.RelayServiceApi
import io.gnosis.kouban.data.db.TokenInfoDao
import io.gnosis.kouban.data.db.TokenInfoDb
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.model.Solidity
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

class TokenRepository(
    private val relayServiceApi: RelayServiceApi,
    private val tokenInfoDao: TokenInfoDao
) {

    private val lastInit = System.currentTimeMillis()
    private val pendingTokenInfo = ConcurrentHashMap<Solidity.Address, Deferred<TokenInfoDb>>()

    suspend fun cacheTokenInfo(info: TokenInfo): TokenInfo {
        if (info.address == ETH_ADDRESS) return ETH_TOKEN_INFO
        TokenInfoDb(info.address, info.symbol, info.name, info.decimals, info.icon ?: "", System.currentTimeMillis()).apply {
            tokenInfoDao.insert(this)
        }
        return info
    }

    suspend fun loadTokenInfo(token: Solidity.Address): TokenInfo {
        if (token == ETH_ADDRESS) return ETH_TOKEN_INFO
        val localToken = tokenInfoDao.load(token)
        val tokenInfo = if (shouldLoadRemote(localToken)) loadRemoteInfoAsync(token, localToken).await() else localToken!!
        return tokenInfo.toLocal()
    }

    private fun shouldLoadRemote(token: TokenInfoDb?) = token == null || lastInit > token.lastUpdate

    private fun loadRemoteInfoAsync(token: Solidity.Address, default: TokenInfoDb?) =
        pendingTokenInfo.getOrPut(token, {
            GlobalScope.async {
                try {
                    relayServiceApi.tokenInfo(token.asEthereumAddressChecksumString()).let {
                        TokenInfoDb(token, it.symbol, it.name, it.decimals, it.logoUri ?: "", System.currentTimeMillis()).apply {
                            tokenInfoDao.insert(this)
                            @Suppress("DeferredResultUnused")
                            pendingTokenInfo.remove(token)
                        }
                    }
                } catch (e: Exception) {
                    // Don't throw if we have a local version
                    default ?: throw e
                }
            }
        })

    private fun TokenInfoDb.toLocal() =
        TokenInfo(address, symbol, decimals, name, icon)

    data class TokenInfo(
        val address: Solidity.Address,
        val symbol: String,
        val decimals: Int,
        val name: String,
        val icon: String?
    )

    companion object {
        val ETH_ADDRESS = Solidity.Address(BigInteger.ZERO)
        val ETH_TOKEN_INFO = TokenInfo(ETH_ADDRESS, "ETH", 18, "Ether", "local::ethereum")
    }
}
