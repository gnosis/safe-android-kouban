package io.gnosis.kouban.data.utils

import io.gnosis.kouban.data.backend.JsonRpcApi
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressString

suspend fun JsonRpcApi.performCall(to: Solidity.Address, data: String, from: Solidity.Address? = null) =
    post(
        JsonRpcApi.JsonRpcRequest(
            method = "eth_call",
            params = listOf(
                mutableMapOf(
                    "to" to to.asEthereumAddressString(),
                    "data" to data
                ).apply {
                    from?.let { put("from", from.asEthereumAddressString()) }
                },
                "latest"
            )
        )
    ).result!!
