import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

class LoadAggregatedUserUseCase(
    val loadUserDetails: suspend () -> UserDetails,
    val loadComments: suspend (UserId) -> List<Comment>,
    val loadFriends: suspend (UserId) -> List<UserDetails>
) {
    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)

    suspend fun loadAggregatedUserDetails(): AggregatedUserDetails {
        val deferredResult = coroutineScope.async {
            val userDetails = loadUserDetails()

            val commentsAndFriends = awaitAll(
                async { tryToLoadList(userDetails.id, loadComments) },
                async { tryToLoadList(userDetails.id, loadFriends) }
            )

            val comments = commentsAndFriends[0] as List<Comment>
            val friends = commentsAndFriends[1] as List<UserDetails>

            AggregatedUserDetails(
                userDetails = userDetails,
                comments = comments,
                friends = friends
            )
        }

        return deferredResult.await()
    }

    fun close() {
        coroutineScope.cancel()
    }

    private suspend fun <T> tryToLoadList(
        userId: String,
        loadFunction: suspend (String) -> List<T> ,
        defaultValue: List<T> = listOf()
    ): List<T> {
        yield()
        return try {
           withTimeout(2000L) {
                loadFunction(userId)
            }
        } catch (e: Throwable) {
            defaultValue
        }
    }
}
