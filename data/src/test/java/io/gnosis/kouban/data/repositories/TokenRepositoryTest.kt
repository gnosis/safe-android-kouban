package io.gnosis.kouban.data.repositories

import io.gnosis.kouban.data.backend.RelayServiceApi
import io.gnosis.kouban.data.backend.dto.ServiceTokenInfo
import io.gnosis.kouban.data.db.TokenInfoDao
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import pm.gnosis.utils.asEthereumAddress

class TokenRepositoryTest {

    private val tokenInfoDao = mockk<TokenInfoDao>()
    private val relayServiceApi = mockk<RelayServiceApi>()

    private val tokenRepository = TokenRepository(relayServiceApi, tokenInfoDao)

    @Test
    fun `loadTokenInfo - DAO is empty`() = runBlocking {
        val tokenAddress = "0x123".asEthereumAddress()!!
        coEvery { tokenInfoDao.load(any()) } returns null
        coEvery { tokenInfoDao.insert(any()) } just Runs
        coEvery { relayServiceApi.tokenInfo(any()) } returns ServiceTokenInfo(tokenAddress, 1, "", "", "")

        tokenRepository.loadTokenInfo(tokenAddress)

        coVerify(exactly = 1) { tokenInfoDao.load(tokenAddress) }
        coVerify(exactly = 1) { tokenInfoDao.insert(any()) }
        coVerify(exactly = 1) { relayServiceApi.tokenInfo(any()) }
    }
}
