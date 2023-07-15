import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val mockUser = UserDetails("1", "james")
private val mockComments = listOf(
    Comment(id = "2", text = "Comment 1"),
    Comment(id = "3", text = "Comment 2")
)
private val mockFriends = listOf(
    UserDetails("4", "Jane Doe"),
    UserDetails("5", "Joe Bloggs")
)

class TestCases {

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


    // Functions that return mocks

    private suspend fun loadMockUser(): UserDetails {
        delay(1)
        return mockUser
    }

    private suspend fun loadMockComments(userId: String): List<Comment> {
        delay(1)
        return mockComments
    }

    private suspend fun loadMockFriends(userId: String): List<UserDetails> {
        delay(1)
        return mockFriends
    }

}