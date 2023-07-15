import kotlinx.coroutines.*

class LoadAggregatedUserUseCase(
    val loadUserDetails: suspend () -> UserDetails,
    val loadComments: suspend (UserId) -> List<Comment>,
    val loadFriends: suspend (UserId) -> List<UserDetails>
) {

    suspend fun loadAggregatedUserDetails(): AggregatedUserDetails {
        val userDetails = loadUserDetails()
        val comments =  tryToLoadList(userDetails.id, loadComments)
        val friends = tryToLoadList(userDetails.id, loadFriends)

        return AggregatedUserDetails(
            userDetails = userDetails,
            comments = comments,
            friends = friends
        )
    }

    fun close() {
        // TODO
    }

    private suspend fun <T> tryToLoadList(
        userId: String,
        loadFunction: suspend (String) -> List<T> ,
        defaultValue: List<T> = listOf()
    ): List<T> {
        return try {
           withTimeout(2000L) {
                loadFunction(userId)
            }
        } catch (e: Throwable) {
            defaultValue
        }
    }
}
