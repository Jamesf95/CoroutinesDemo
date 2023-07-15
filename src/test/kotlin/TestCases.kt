import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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


    // Cancel

    @Test
    fun `Cancel stops the friend and comment functions being called`() {
        var commentsCalled = false
        var friendsCalled = false

        val result = runBlocking {
            runCatching {
                val useCase = LoadAggregatedUserUseCase(
                    loadUserDetails = { loadMockUser(100) },
                    loadComments = {
                        commentsCalled = true
                        loadMockComments(it)
                                   },
                    loadFriends = {
                        friendsCalled = true
                        loadMockFriends(it, 3000)
                    }
                )

                // loadUser has a delay of 100ms. I'm cancelling it after 10.
                // So it'll be cancelled before the user details have been loaded.
                launch {
                    delay(10)
                    useCase.close()
                }

                useCase.loadAggregatedUserDetails()
            }
        }

        assert(result.isFailure)
        assert(result.exceptionOrNull() is kotlinx.coroutines.CancellationException)
        assertFalse(commentsCalled)
        assertFalse(friendsCalled)
    }



    // Functions that return mocks

    private suspend fun loadMockUser(delayTime: Long = 100): UserDetails {
        delay(delayTime)
        return mockUser
    }

    private suspend fun loadMockComments(userId: String, delayTime: Long = 100): List<Comment> {
        delay(delayTime)
        return mockComments
    }

    private suspend fun loadMockFriends(userId: String, delayTime: Long = 100): List<UserDetails> {
        delay(delayTime)
        return mockFriends
    }
}