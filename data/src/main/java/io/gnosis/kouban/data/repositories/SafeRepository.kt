package io.gnosis.kouban.data.repositories

import android.content.Context
import io.gnosis.kouban.contracts.*
import io.gnosis.kouban.data.BuildConfig
import io.gnosis.kouban.data.backend.JsonRpcApi
import io.gnosis.kouban.data.backend.MagicApi
import io.gnosis.kouban.data.backend.TransactionServiceApi
import io.gnosis.kouban.data.backend.dto.ServiceTransaction
import io.gnosis.kouban.data.backend.dto.ServiceTransactionRequest
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter
import io.gnosis.kouban.data.models.*
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.data.utils.nullOnThrow
import io.gnosis.kouban.data.utils.performCall
import io.gnosis.kouban.data.utils.shiftedString
import pm.gnosis.crypto.ECDSASignature
import pm.gnosis.crypto.utils.Sha3Utils
import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.utils.*
import java.math.BigInteger

class SafeRepository(
    context: Context,
    private val bip39: Bip39,
    private val jsonRpcApi: JsonRpcApi,
    private val transactionServiceApi: TransactionServiceApi,
    private val magicApi: MagicApi,
    private val searchManager: SearchManager,
    private val preferencesManager: PreferencesManager,
    private val tokensRepository: TokenRepository
) {

    private val accountPrefs = context.getSharedPreferences(ACC_PREF_NAME, Context.MODE_PRIVATE)

    fun isInitialized(): Boolean =
        accountPrefs.getString(PREF_KEY_APP_MNEMONIC, null) != null


    suspend fun setSafeAddress(safe: Solidity.Address) {
        preferencesManager.prefs.edit {
            putString(PREF_KEY_SAFE_ADDRESS, safe.asEthereumAddressString())
        }
    }

    suspend fun loadSafeAddress(): Solidity.Address =
        preferencesManager.prefs.getString(PREF_KEY_SAFE_ADDRESS, null)!!.asEthereumAddress()!!

    suspend fun loadModules(safe: Solidity.Address): List<Solidity.Address> =
        jsonRpcApi.performCall(safe, GnosisSafe.GetModules.encode()).let { GnosisSafe.GetModules.decode(it).param0.items }


    @Deprecated("This uses magic")
    suspend fun getTransactions(safe: Solidity.Address): TransactionsDto =
        magicApi.getTransactions(safe.asEthereumAddressChecksumString()).let {
            TransactionsDto(
                it.pending.filter { (it.dataInfo != null).xor(it.transferInfo != null) },
                it.history.filter { (it.dataInfo != null).xor(it.transferInfo != null) }
            ).also { transactions ->
                val tokenSymbolsPending = transactions.pending.groupBy { it.transferInfo?.tokenSymbol ?: TokenRepository.ETH_TOKEN_INFO.symbol }.keys
                val tokenSymbolsHistory = transactions.history.groupBy { it.transferInfo?.tokenSymbol ?: TokenRepository.ETH_TOKEN_INFO.symbol }.keys

                val tokenSymbols = tokenSymbolsPending + tokenSymbolsHistory
                searchManager.activateFilter(TransactionTokenSymbolFilter(tokenSymbols.distinct(), tokenSymbols.toMutableList()))
                Unit
            }
        }

    suspend fun loadTokenBalances(safe: Solidity.Address): List<Balance> =
        transactionServiceApi.loadBalances(safe.asEthereumAddressChecksumString()).map {
            val tokenAddress = it.tokenAddress ?: TokenRepository.ETH_ADDRESS
            val tokenInfo = it.token?.let { meta ->
                tokensRepository.cacheTokenInfo(
                    TokenRepository.TokenInfo(
                        tokenAddress,
                        meta.symbol,
                        meta.decimals,
                        meta.name,
                        meta.logoUri ?: "https://gnosis-safe-token-logos.s3.amazonaws.com/${tokenAddress.asEthereumAddressChecksumString()}.png"
                    )
                )
            } ?: TokenRepository.ETH_TOKEN_INFO
            Balance(tokenInfo, it.balance, it.balanceUsd?.toBigDecimal())
        }

    suspend fun loadSafeInfo(safe: Solidity.Address): SafeInfo {
        val responses = jsonRpcApi.post(
            listOf(
                JsonRpcApi.JsonRpcRequest(
                    id = 0,
                    method = "eth_getStorageAt",
                    params = listOf(safe, BigInteger.ZERO.toHexString(), "latest")
                ),
                JsonRpcApi.JsonRpcRequest(
                    id = 1,
                    method = "eth_getStorageAt",
                    params = listOf(safe, "0x6c9a6c4a39284e37ed1cf53d337577d14212a4870fb976a4366c693b939918d5", "latest")
                ),
                JsonRpcApi.JsonRpcRequest(
                    id = 2,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to safe,
                            "data" to GnosisSafe.GetOwners.encode()
                        ), "latest"
                    )
                ),
                JsonRpcApi.JsonRpcRequest(
                    id = 3,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to safe,
                            "data" to GnosisSafe.GetThreshold.encode()
                        ), "latest"
                    )
                ),
                JsonRpcApi.JsonRpcRequest(
                    id = 4,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to safe,
                            "data" to GnosisSafe.Nonce.encode()
                        ), "latest"
                    )
                ),
                JsonRpcApi.JsonRpcRequest(
                    id = 5,
                    method = "eth_call",
                    params = listOf(
                        mapOf(
                            "to" to safe,
                            "data" to GnosisSafe.GetModules.encode()
                        ), "latest"
                    )
                )
            )
        )
        val masterCopy = responses[0].result!!.asEthereumAddress()!!
        val fallbackHandler = responses[1].result?.asEthereumAddress()
        val owners = GnosisSafe.GetOwners.decode(responses[2].result!!).param0.items
        val threshold = GnosisSafe.GetThreshold.decode(responses[3].result!!).param0.value
        val nonce = GnosisSafe.Nonce.decode(responses[4].result!!).param0.value //txCount
        val modules = GnosisSafe.GetModules.decode(responses[5].result!!).param0.items
        return SafeInfo(safe, masterCopy, fallbackHandler, owners, threshold, nonce, modules)
    }

    suspend fun loadSafeDeploymentParams(safe: Solidity.Address): SafeInfoDeployment {

        var txHash: String? = null

        // check with previous versions of proxy factory if no results were found
        run loop@{
            PROXY_FACTORY.values().forEach {
                txHash = findDeploymentTransactionHash(safe, it.address)
                if (txHash != null)
                    return@loop
            }
        }

        if (txHash == null)
            throw SafeDeploymentInfoNotFound()

        val deploymentTransaction = loadTransactionByHash(txHash!!)

        val (deploymentMastercopy, initializer) = decodeProxyFactoryArguments(deploymentTransaction?.input!!)

        val deploymentArgsEncoded = initializer.encodePacked().substring(8)

        val safeDeploymentInfo = when (deploymentMastercopy) {

            safeMasterCopy_1_1_1 -> {
                val deploymentArgs = GnosisSafeV1.Setup.decodeArguments(deploymentArgsEncoded)
                SafeInfoDeployment(
                    deploymentMastercopy,
                    deploymentArgs.fallbackhandler,
                    deploymentArgs._owners.items,
                    deploymentArgs._threshold.value
                )
            }
            else -> {
                val deploymentArgs = GnosisSafe.Setup.decodeArguments(deploymentArgsEncoded)
                SafeInfoDeployment(
                    deploymentMastercopy,
                    deploymentArgs.fallbackhandler,
                    deploymentArgs._owners.items,
                    deploymentArgs._threshold.value
                )
            }
        }

        return safeDeploymentInfo
    }

    private fun decodeProxyFactoryArguments(input: String): Pair<Solidity.Address, Solidity.Bytes> {
        val inputDecoded = ProxyFactory.CreateProxyWithNonce.decodeArguments(
            input.removeHexPrefix().substring(8)
        )
        return inputDecoded._mastercopy to inputDecoded.initializer
    }

    private suspend fun findDeploymentTransactionHash(safe: Solidity.Address, proxyFactory: String): String? {
        val creationLogsRequest = jsonRpcApi.logs(
            JsonRpcApi.JsonRpcRequest(
                id = 0,
                method = "eth_getLogs",
                params = listOf(
                    mapOf(
                        "fromBlock" to "earliest",
                        "address" to proxyFactory,
                        "topics" to listOf(ProxyFactory.Events.ProxyCreation.EVENT_ID.addHexPrefix())
                    ), "earliest"

                )
            )
        )

        val logs = creationLogsRequest.result
        return logs.find { it.data == safe.encode().addHexPrefix() }?.transactionHash
    }

    private suspend fun loadTransactionByHash(txHash: String): JsonRpcApi.JsonRpcTransactionResult.Transaction? {

        val transactionRequest = jsonRpcApi.transaction(
            JsonRpcApi.JsonRpcRequest(
                id = 0,
                method = "eth_getTransactionByHash",
                params = listOf(txHash)
            )
        )

        return transactionRequest.result
    }

    suspend fun loadSafeNonce(safe: Solidity.Address): BigInteger =
        GnosisSafe.Nonce.decode(jsonRpcApi.performCall(safe, GnosisSafe.Nonce.encode())).param0.value

    suspend fun loadPendingTransactions(safe: Solidity.Address): List<ServiceSafeTx> =
        transactionServiceApi.loadTransactions(safe.asEthereumAddressChecksumString()).results.map { it.toLocal() }

    suspend fun loadPendingTransaction(txHash: String): ServiceSafeTx =
        transactionServiceApi.loadTransaction(txHash).toLocal()

    suspend fun loadTransactionInformation(safe: Solidity.Address, transaction: SafeTx) =
        nullOnThrow {
            when {
                transaction.to == safe && transaction.data.removeHexPrefix().isBlank() && transaction.value == BigInteger.ZERO -> {// Safe management
                    TransactionInfo(
                        recipient = transaction.to,
                        recipientLabel = transaction.to.asEthereumAddressChecksumString().asMiddleEllipsized(4),
                        assetIcon = "local::settings",
                        assetLabel = "Cancel transaction"
                    )
                }
                transaction.to == safe && transaction.value == BigInteger.ZERO -> {// Safe management
                    TransactionInfo(
                        recipient = transaction.to,
                        recipientLabel = transaction.to.asEthereumAddressChecksumString().asMiddleEllipsized(4),
                        assetIcon = "local::settings",
                        assetLabel = "Safe management"
                    )
                }
                transaction.data.isSolidityMethod(ERC20Token.Transfer.METHOD_ID) -> { // Token transfer
                    val transferArgs = ERC20Token.Transfer.decodeArguments(transaction.data.removeSolidityMethodPrefix(ERC20Token.Transfer.METHOD_ID))
                    val tokenInfo = nullOnThrow { tokensRepository.loadTokenInfo(transaction.to) }
                    val symbol = tokenInfo?.symbol ?: transaction.to.asEthereumAddressChecksumString().asMiddleEllipsized(4)
                    TransactionInfo(
                        recipient = transferArgs._to,
                        recipientLabel = transferArgs._to.asEthereumAddressChecksumString().asMiddleEllipsized(4),
                        assetIcon = tokenInfo?.icon,
                        assetLabel = "${transferArgs._value.value.shiftedString(tokenInfo?.decimals ?: 0)} $symbol",
                        additionalInfo = "Token transfer"
                    )
                }
                transaction.data.isSolidityMethod(ERC20Token.Approve.METHOD_ID) -> { // Token transfer
                    val approveArgs = ERC20Token.Approve.decodeArguments(transaction.data.removeSolidityMethodPrefix(ERC20Token.Approve.METHOD_ID))
                    val tokenInfo = nullOnThrow { tokensRepository.loadTokenInfo(transaction.to) }
                    val symbol = tokenInfo?.symbol ?: transaction.to.asEthereumAddressChecksumString().asMiddleEllipsized(4)
                    TransactionInfo(
                        recipient = approveArgs._spender,
                        recipientLabel = approveArgs._spender.asEthereumAddressChecksumString().asMiddleEllipsized(4),
                        assetIcon = tokenInfo?.icon,
                        assetLabel = "Approve ${approveArgs._value.value.shiftedString(tokenInfo?.decimals ?: 0)} $symbol",
                        additionalInfo = "Token approval"
                    )
                }
                else -> null
            }
        } ?: run {
            // ETH transfer
            var assetLabel = "${transaction.value.shiftedString(18)} ETH"
            val hasData = transaction.data.removeHexPrefix().isNotBlank()
            if (hasData) {
                assetLabel += " / ${transaction.data.length / 2 - 1} bytes"
            }
            TransactionInfo(
                recipient = transaction.to,
                recipientLabel = transaction.to.asEthereumAddressChecksumString().asMiddleEllipsized(4),
                assetIcon = "local::ethereum",
                assetLabel = assetLabel,
                additionalInfo = if (hasData) "Contract interaction: ${transaction.data.addHexPrefix()}" else null
            )
        }

    private fun ServiceTransaction.toLocal() =
        ServiceSafeTx(
            hash = safeTxHash,
            tx = SafeTx(
                to = to?.asEthereumAddress() ?: pm.gnosis.model.Solidity.Address(java.math.BigInteger.ZERO),
                value = value.decimalAsBigIntegerOrNull() ?: java.math.BigInteger.ZERO,
                data = data ?: "",
                operation = operation.toOperation()
            ),
            execInfo = SafeTxExecInfo(
                baseGas = baseGas.decimalAsBigIntegerOrNull() ?: java.math.BigInteger.ZERO,
                txGas = safeTxGas.decimalAsBigIntegerOrNull() ?: java.math.BigInteger.ZERO,
                gasPrice = gasPrice.decimalAsBigIntegerOrNull() ?: java.math.BigInteger.ZERO,
                gasToken = gasToken?.asEthereumAddress() ?: pm.gnosis.model.Solidity.Address(java.math.BigInteger.ZERO),
                refundReceiver = refundReceiver?.asEthereumAddress() ?: pm.gnosis.model.Solidity.Address(java.math.BigInteger.ZERO),
                nonce = nonce.decimalAsBigInteger()
            ),
            confirmations = confirmations.map { confirmation ->
                confirmation.owner.asEthereumAddress()!! to confirmation.signature
            },
            executed = isExecuted,
            txHash = transactionHash
        )

    suspend fun loadSafeTransactionExecutionInformation(
        safe: Solidity.Address,
        transaction: SafeTx,
        overrideNonce: BigInteger?
    ): SafeTxExecInfo {
        val txGas = estimateTx(safe, transaction)
        val nonce = overrideNonce ?: loadSafeNonce(safe)
        return SafeTxExecInfo(
            BigInteger.ZERO,
            txGas,
            BigInteger.ZERO,
            Solidity.Address(BigInteger.ZERO),
            Solidity.Address(BigInteger.ZERO),
            nonce
        )
    }


    private suspend fun estimateTx(safe: Solidity.Address, transaction: SafeTx): BigInteger {
        val response = jsonRpcApi.performCall(
            from = safe,
            to = safe,
            data = GnosisSafe.RequiredTxGas.encode(
                transaction.to,
                Solidity.UInt256(transaction.value),
                Solidity.Bytes(transaction.data.hexStringToByteArray()),
                Solidity.UInt8(transaction.operation.id.toBigInteger())
            )
        )
        if (!response.startsWith(ESTIMATE_RESPONSE_PREFIX)) throw IllegalStateException("Could not estimate transaction!")
        return response.removePrefix(ESTIMATE_RESPONSE_PREFIX).hexAsBigInteger() + BigInteger.valueOf(10000)
    }

    suspend fun calculateSafeTransactionHash(
        safe: Solidity.Address,
        transaction: SafeTx,
        execInfo: SafeTxExecInfo
    ) = calculateHash(
        safe,
        transaction.to,
        transaction.value,
        transaction.data,
        transaction.operation,
        execInfo.txGas,
        execInfo.baseGas,
        execInfo.gasPrice,
        execInfo.gasToken,
        execInfo.nonce
    ).toHexString()

    suspend fun confirmSafeTransaction(
        safe: Solidity.Address,
        transaction: SafeTx,
        execInfo: SafeTxExecInfo
    ): String {
//        val hash =
//            calculateHash(
//                safe,
//                transaction.to,
//                transaction.value,
//                transaction.data,
//                transaction.operation,
//                execInfo.txGas,
//                execInfo.baseGas,
//                execInfo.gasPrice,
//                execInfo.gasToken,
//                execInfo.nonce
//            )
//
//        val keyPair = getKeyPair()
//        val deviceId = keyPair.address.toAddress()
//        val signature = keyPair.sign(hash)
//
//        val confirmation = ServiceTransactionRequest(
//            to = transaction.to.asEthereumAddressChecksumString(),
//            value = transaction.value.asDecimalString(),
//            data = transaction.data,
//            operation = transaction.operation.id,
//            gasToken = execInfo.gasToken.asEthereumAddressChecksumString(),
//            safeTxGas = execInfo.txGas.asDecimalString(),
//            baseGas = execInfo.baseGas.asDecimalString(),
//            gasPrice = execInfo.gasPrice.asDecimalString(),
//            refundReceiver = execInfo.refundReceiver.asEthereumAddressChecksumString(),
//            nonce = execInfo.nonce.asDecimalString(),
//            safeTxHash = hash.toHexString(),
//            sender = deviceId.asEthereumAddressChecksumString(),
//            confirmationType = ServiceTransactionRequest.CONFIRMATION,
//            signature = signature.toSignatureString()
//        )
//        transactionServiceApi.confirmTransaction(safe.asEthereumAddressChecksumString(), confirmation)
//        return hash.toHexString()
        return ""
    }

    suspend fun submitOnChainConfirmationTransactionHash(
        safe: Solidity.Address,
        transaction: SafeTx,
        execInfo: SafeTxExecInfo,
        txHash: String
    ): String {
        // Dirty hack
        val sender = loadSafeInfo(safe).owners.first()
        val safeTxHash = calculateSafeTransactionHash(safe, transaction, execInfo)
        val confirmation = ServiceTransactionRequest(
            to = transaction.to.asEthereumAddressChecksumString(),
            value = transaction.value.asDecimalString(),
            data = transaction.data,
            operation = transaction.operation.id,
            gasToken = execInfo.gasToken.asEthereumAddressChecksumString(),
            safeTxGas = execInfo.txGas.asDecimalString(),
            baseGas = execInfo.baseGas.asDecimalString(),
            gasPrice = execInfo.gasPrice.asDecimalString(),
            refundReceiver = execInfo.refundReceiver.asEthereumAddressChecksumString(),
            nonce = execInfo.nonce.asDecimalString(),
            safeTxHash = safeTxHash,
            sender = sender.asEthereumAddressChecksumString(),
            confirmationType = ServiceTransactionRequest.CONFIRMATION,
            signature = null
        )
        transactionServiceApi.confirmTransaction(safe.asEthereumAddressChecksumString(), confirmation)
        return safeTxHash
    }

    private fun ECDSASignature.toSignatureString() =
        r.toString(16).padStart(64, '0').substring(0, 64) +
                s.toString(16).padStart(64, '0').substring(0, 64) +
                v.toString(16).padStart(2, '0')

    private fun String.toECDSASignature(): ECDSASignature {
        require(length == 130)
        val r = BigInteger(substring(0, 64), 16)
        val s = BigInteger(substring(64, 128), 16)
        val v = substring(128, 130).toByte(16)
        return ECDSASignature(r, s).apply { this.v = v }
    }

    private fun Int.toOperation() =
        when (this) {
            0 -> SafeTx.Operation.CALL
            1 -> SafeTx.Operation.DELEGATE
            else -> throw IllegalArgumentException("Unsupported operation")
        }

    private fun calculateHash(
        safeAddress: Solidity.Address,
        txTo: Solidity.Address,
        txValue: BigInteger,
        txData: String?,
        txOperation: SafeTx.Operation,
        txGas: BigInteger,
        dataGas: BigInteger,
        gasPrice: BigInteger,
        gasToken: Solidity.Address,
        txNonce: BigInteger
    ): ByteArray {
        val to = txTo.value.paddedHexString()
        val value = txValue.paddedHexString()
        val data = Sha3Utils.keccak(txData?.hexToByteArray() ?: ByteArray(0)).toHex().padStart(64, '0')
        val operationString = txOperation.id.toBigInteger().paddedHexString()
        val gasPriceString = gasPrice.paddedHexString()
        val txGasString = txGas.paddedHexString()
        val dataGasString = dataGas.paddedHexString()
        val gasTokenString = gasToken.value.paddedHexString()
        val refundReceiverString = BigInteger.ZERO.paddedHexString()
        val nonce = txNonce.paddedHexString()
        return hash(
            safeAddress,
            to,
            value,
            data,
            operationString,
            txGasString,
            dataGasString,
            gasPriceString,
            gasTokenString,
            refundReceiverString,
            nonce
        )
    }

    private fun hash(safeAddress: Solidity.Address, vararg parts: String): ByteArray {
        val initial = StringBuilder().append(ERC191_BYTE).append(ERC191_VERSION).append(domainHash(safeAddress)).append(valuesHash(parts))
        return Sha3Utils.keccak(initial.toString().hexToByteArray())
    }

    private fun domainHash(safeAddress: Solidity.Address) =
        Sha3Utils.keccak(
            ("0x035aff83d86937d35b32e04f0ddc6ff469290eef2f1b692d8a815c89404d4749" +
                    safeAddress.value.paddedHexString()).hexToByteArray()
        ).toHex()

    private fun valuesHash(parts: Array<out String>) =
        parts.fold(StringBuilder().append(getTypeHash())) { acc, part ->
            acc.append(part)
        }.toString().run {
            Sha3Utils.keccak(hexToByteArray()).toHex()
        }

    private fun BigInteger?.paddedHexString(padding: Int = 64) = (this?.toString(16) ?: "").padStart(padding, '0')

    private fun getTypeHash() = "0xbb8310d486368db6bd6f849402fdd73ad53d316b5a4b2644ad6efe0f941286d8"

    private fun ByteArray.toAddress() = Solidity.Address(this.asBigInteger())

    companion object {

        val safeMasterCopy_0_1_0 = BuildConfig.SAFE_MASTER_COPY_0_1_0.asEthereumAddress()!!
        val safeMasterCopy_1_0_0 = BuildConfig.SAFE_MASTER_COPY_1_0_0.asEthereumAddress()!!
        val safeMasterCopy_1_1_1 = BuildConfig.SAFE_MASTER_COPY_1_1_1.asEthereumAddress()!!

        private const val ERC191_BYTE = "19"
        private const val ERC191_VERSION = "01"

        private const val ACC_PREF_NAME = "AccountRepositoryImpl_Preferences"

        private const val PREF_KEY_APP_MNEMONIC = "accounts.string.app_menmonic"
        private const val PREF_KEY_SAFE_ADDRESS = "accounts.string.safe_address"

        private const val ESTIMATE_RESPONSE_PREFIX =
            "0x08c379a000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000020"

        private const val ENC_PASSWORD = "ThisShouldNotBeHardcoded"

        private enum class PROXY_FACTORY(val address: String) {
            v1_1_1(BuildConfig.PROXY_FACTORY_1_1_1),
            v1_1_0(BuildConfig.PROXY_FACTORY_1_1_0),
            v1_0_0(BuildConfig.PROXY_FACTORY_1_0_0)
        }
    }
}

class SafeDeploymentInfoNotFound : Throwable()

