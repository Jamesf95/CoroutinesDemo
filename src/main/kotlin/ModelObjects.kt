
typealias UserId = String
data class AggregatedUserDetails(
    val userDetails: UserDetails,
    val comments: List<Comment>,
    val friends: List<UserDetails>
)

data class UserDetails(
    val id: UserId,
    val username: String
)

data class Comment(
    val id: String,
    val text: String
)