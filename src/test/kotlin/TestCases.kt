import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Error

private val mockUser = UserDetails("1", "james")
private val mockComments = listOf(
    Comment(id = "2", text = "Comment 1"),
    Comment(id = "3", text = "Comment 2")
)
private val mockFriends = listOf(
    UserDetails("4", "Jane Doe"),
    UserDetails("5", "Joe Bloggs")
)

private object MockError: Error()

class TestCases {

    // Loading user

    @Test
    fun `When all sources complete successfully, aggregated data is returned`() {
        val result = runBlocking {
            LoadAggregatedUserUseCase(
                loadUserDetails = { loadMockUser() },
                loadComments = { loadMockComments(it) },
                loadFriends = { loadMockFriends(it) }
            ).loadAggregatedUserDetails()
        }

        val expected = AggregatedUserDetails(
            mockUser, mockComments, mockFriends
        )

        assertEquals(expected, result)
    }

    @Test
    fun `When loading a mocked user fails, the error is propagated`() {
        val result = runBlocking {
            runCatching {
                LoadAggregatedUserUseCase(
                    loadUserDetails = { throw MockError },
                    loadComments = { loadMockComments(it) },
                    loadFriends = { loadMockFriends(it) }
                ).loadAggregatedUserDetails()
            }
        }

        assertEquals(MockError, result.exceptionOrNull())
    }


    // Loading comments

    @Test
    fun `When loading comments fails, an empty list is returned`() {
        val result = runBlocking {
            runCatching {
                LoadAggregatedUserUseCase(
                    loadUserDetails = { loadMockUser() },
                    loadComments = { throw MockError },
                    loadFriends = { loadMockFriends(it) }
                ).loadAggregatedUserDetails()
            }
        }

        val expected = AggregatedUserDetails(
            mockUser, listOf(), mockFriends
        )
        val actual = result.getOrNull()

        assertEquals(expected, actual)
    }

    @Test
    fun `When loading comments times out, an empty list is returned`() {
        val result = runBlocking {
            runCatching {
                LoadAggregatedUserUseCase(
                    loadUserDetails = { loadMockUser() },
                    loadComments = { loadMockComments(it, 3000) },
                    loadFriends = { loadMockFriends(it) }
                ).loadAggregatedUserDetails()
            }
        }

        val expected = AggregatedUserDetails(
            mockUser, listOf(), mockFriends
        )
        val actual = result.getOrNull()

        assertEquals(expected, actual)
    }


    // Friends

    @Test
    fun `When loading friends fails, an empty list is returned`() {
        val result = runBlocking {
            runCatching {
                LoadAggregatedUserUseCase(
                    loadUserDetails = { loadMockUser() },
                    loadComments = { loadMockComments(it) },
                    loadFriends = { throw MockError }
                ).loadAggregatedUserDetails()
            }
        }

        val expected = AggregatedUserDetails(
            mockUser, mockComments, listOf()
        )
        val actual = result.getOrNull()

        assertEquals(expected, actual)
    }

    @Test
    fun `When loading friends times out, an empty list is returned`() {
        val result = runBlocking {
            runCatching {
                LoadAggregatedUserUseCase(
                    loadUserDetails = { loadMockUser() },
                    loadComments = { loadMockComments(it) },
                    loadFriends = { loadMockFriends(it, 3000) }
                ).loadAggregatedUserDetails()
            }
        }

        val expected = AggregatedUserDetails(
            mockUser, mockComments, listOf()
        )
        val actual = result.getOrNull()

        assertEquals(expected, actual)
    }




    // Functions that return mocks

    private suspend fun loadMockUser(delayTime: Long = 1): UserDetails {
        delay(delayTime)
        return mockUser
    }

    private suspend fun loadMockComments(userId: String, delayTime: Long = 1): List<Comment> {
        delay(delayTime)
        return mockComments
    }

    private suspend fun loadMockFriends(userId: String, delayTime: Long = 1): List<UserDetails> {
        delay(delayTime)
        return mockFriends
    }
}