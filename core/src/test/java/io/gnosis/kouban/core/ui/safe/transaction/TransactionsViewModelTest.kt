//package io.gnosis.kouban.core.ui.safe.transaction
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import io.gnosis.kouban.core.ui.base.Loading
//import io.gnosis.kouban.data.models.TransactionState
//import io.gnosis.kouban.data.repositories.SafeRepository
//import io.mockk.*
//import kotlinx.coroutines.runBlocking
//import org.junit.Test
//import pm.gnosis.utils.asEthereumAddress
//import java.math.BigInteger
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//
//class TransactionsViewModelTest {
//
//    @get:Rule
//    val instantExecutorRule = InstantTaskExecutorRule()
//
//    private val safeRepository = mockk<SafeRepository>()
//
//    private val viewModel = TransactionsViewModel(safeRepository)
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(Dispatchers.Default)
//    }
//
//    @After
//    fun cleanUp() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `loadTransactionsOf - success`() = runBlocking {
//        val safeAddress = "0x123".asEthereumAddress()!!
//        coEvery { safeRepository.loadSafeNonce(any()) } returns BigInteger.ZERO
//        coEvery { safeRepository.loadPendingTransactions(any()) } returns (listOfExecutedTxs() + listOfPendingTxs())
//
//        val liveData = viewModel.loadTransactionsOf(safeAddress)
//
//        liveData.test()
//            .awaitValue()
//            .assertValue { it is Loading && it.isLoading == true }
//            .awaitNextValue()
//            .assertValue { it is Loading && it.isLoading == false }
//            .awaitNextValue()
//            .assertValue {
//                it is Transactions
//                        && it.transactions.containsKey(TransactionState.Pending)
//                        && it.transactions[TransactionState.Pending]?.size == 2
//                        && it.transactions.containsKey(TransactionState.Executed)
//                        && it.transactions[TransactionState.Executed]?.size == 3
//            }
//        coVerify { safeRepository.loadSafeNonce(safeAddress) }
//    }
//
//    @Test
//    fun `loadTransactionOf - failure`() = runBlocking {
//        val throwable = Throwable()
//        val safeAddress = "0x123".asEthereumAddress()!!
//        coEvery { safeRepository.loadSafeNonce(any()) } returns BigInteger.ZERO
//        coEvery { safeRepository.loadPendingTransactions(any()) } throws throwable
//
//        val liveData = viewModel.loadTransactionsOf(safeAddress)
//
//        liveData.test()
//            .awaitValue()
//            .assertValue { it is Loading && it.isLoading == true }
//            .awaitNextValue()
//            .assertValue { it is Loading && it.isLoading == false }
//            .awaitNextValue()
//            .assertValue { it is Error && it.throwable == throwable }
//        coVerify { safeRepository.loadSafeNonce(safeAddress) }
//    }
//
//    private fun listOfPendingTxs() = listOf(
//        ServiceSafeTx("hash", mockk(), exectInfo(), listOf("0x123".asEthereumAddress()!! to ""), false, null),
//        ServiceSafeTx("hash", mockk(), exectInfo(), listOf("0x123".asEthereumAddress()!! to ""), false, null)
//    )
//
//    private fun listOfExecutedTxs() = listOf(
//        ServiceSafeTx("hash", mockk(), exectInfo(), emptyList(), true, null),
//        ServiceSafeTx("hash", mockk(), exectInfo(), emptyList(), true, null),
//        ServiceSafeTx("hash", mockk(), exectInfo(), emptyList(), true, null)
//    )
//
//    private fun exectInfo() = mockk<SafeTxExecInfo>().apply {
//        every { nonce } returns BigInteger.ONE
//    }
//}
