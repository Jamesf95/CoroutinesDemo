import kotlinx.coroutines.*

class LoadAggregatedUserUseCase(
    val loadUserDetails: suspend () -> UserDetails,
    val loadComments: suspend (UserId) -> List<Comment>,
    val loadFriends: suspend (UserId) -> List<UserDetails>
) {

    suspend fun loadAggregatedUserDetails(): AggregatedUserDetails {
        val userDetails = loadUserDetails()
        val comments = loadComments(userDetails.id)
        val friends = loadFriends(userDetails.id)

        return AggregatedUserDetails(
            userDetails = userDetails,
            comments = comments,
            friends = friends
        )
    }

    fun close() {
        // TODO
    }
}
