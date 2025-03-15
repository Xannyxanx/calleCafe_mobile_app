import retrofit2.Response
import retrofit2.http.GET

interface CafeApiService {
    @GET("GetDiscounts.php")
    suspend fun getDiscounts(): Response<DiscountResponse>
}

data class DiscountResponse(
    val status: String,
    val discounts: Map<String, Float>
)