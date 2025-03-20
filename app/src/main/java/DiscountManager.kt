import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DiscountManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("discount_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Fetch discounts from server
    suspend fun fetchDiscounts(): Boolean {
        return try {
            val response = RetrofitClient.instance.getDiscounts()
            if (response.isSuccessful && response.body()?.status == "success") {
                saveDiscountsLocally(response.body()?.discounts ?: emptyMap())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun saveDiscountsLocally(discounts: Map<String, Float>) {
        sharedPref.edit().apply {
            putString("DISCOUNTS", gson.toJson(discounts))
            apply()
        }
    }

    fun getDiscountPercentage(category: String): Float {
        val discounts = gson.fromJson<Map<String, Float>>(
            sharedPref.getString("DISCOUNTS", "{}"),
            object : TypeToken<Map<String, Float>>() {}.type
        )
        return discounts[category] ?: 0f
    }
}