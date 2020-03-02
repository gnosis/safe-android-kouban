package io.gnosis.kouban.data.repositories

import io.gnosis.kouban.data.BuildConfig
import io.gnosis.kouban.data.backend.JsonRpcApi
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.model.Solidity
import pm.gnosis.model.SolidityBase
import pm.gnosis.utils.*
import java.math.BigInteger
import java.net.IDN
import java.util.*

class EnsRepository(
    private val jsonRpcApi: JsonRpcApi,
    private val ensNormalizer: EnsNormalizer
) {

    suspend fun resolve(url: String): Solidity.Address? {

        val node = ensNormalizer.normalize(url).nameHash()

        val resolverRequest = jsonRpcApi.post(
            listOf(
                JsonRpcApi.JsonRpcRequest(
                    id = 0,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to ENS_ADDRESS, "data" to GET_RESOLVER + node.toHexString()
                        ), "latest"
                    )
                )
            )
        )

        val resolverAddress = resolverRequest[0].result!!.asEthereumAddress()

        val addressRequest = jsonRpcApi.post(
            listOf(
                JsonRpcApi.JsonRpcRequest(
                    id = 0,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to resolverAddress, "data" to GET_ADDRESS + node.toHexString()
                        ), "latest"
                    )
                )
            )
        )

        return addressRequest[0].result?.asEthereumAddress()
    }


    // reverse ens resolution
    suspend fun resolve(address: Solidity.Address): String? {

        val node = "${address.asEthereumAddressString().removeHexPrefix()}.addr.reverse".nameHash()

        val resolverRequest = jsonRpcApi.post(
            listOf(
                JsonRpcApi.JsonRpcRequest(
                    id = 0,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to ENS_ADDRESS, "data" to GET_RESOLVER + node.toHexString()
                        ), "latest"
                    )
                )
            )
        )

        val resolver = resolverRequest[0].result?.asEthereumAddress()

        val nameRequest = jsonRpcApi.post(
            listOf(
                JsonRpcApi.JsonRpcRequest(
                    id = 0,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to resolver, "data" to GET_NAME + node.toHexString()
                        ), "latest"
                    )
                )
            )
        )

        val nameResult = nameRequest[0].result
        return if (nameResult?.removePrefix("0x").isNullOrBlank()) {
            null
        } else {
            val source = SolidityBase.PartitionData.of(nameResult!!)
            // Add decoders
            val offset = BigIntegerUtils.exact(BigInteger(source.consume(), 16))
            val result = Solidity.String.DECODER.decode(source.subData(offset)).value

            result
        }
    }

    private fun String.nameHash(): ByteArray {
        return this.split(".").foldRight<String, ByteArray?>(null) { part, node ->
            if (node == null && part.isEmpty()) ByteArray(32)
            else Sha3Utils.keccak((node ?: ByteArray(32)) + Sha3Utils.keccak(part.toByteArray()))
        } ?: ByteArray(32)
    }


    companion object {
        private val ENS_ADDRESS = BuildConfig.ENS_REGISTRY.asEthereumAddress()!!
        /*
        contract ENS {
            function resolver(bytes32 node) constant returns (Resolver);
        }
         */
        private const val GET_ADDRESS = "0x3b3b57de"    // addr(bytes 32 node)
        /*
        contract Resolver {
            function addr(bytes32 node) constant returns (address);
        }
         */
        private const val GET_RESOLVER = "0x0178b8bf"   // resolver(bytes32 node)

        private const val GET_NAME = "0x691f3431"       // name(bytes32)
    }
}

interface EnsNormalizer {
    fun normalize(name: String): String
}

class IDNEnsNormalizer : EnsNormalizer {
    override fun normalize(name: String) = IDN.toASCII(name, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.getDefault())
}
