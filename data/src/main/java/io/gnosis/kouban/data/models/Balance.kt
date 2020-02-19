package io.gnosis.kouban.data.models

import io.gnosis.kouban.data.repositories.TokenRepository
import java.math.BigDecimal
import java.math.BigInteger

data class Balance(
    val tokenInfo: TokenRepository.TokenInfo,
    val balance: BigInteger,
    val usdBalance: BigDecimal?
)
