import android.content.Context
import androidx.compose.runtime.MutableState

class DiscountPreferences(context: Context) {
    private val sharedPref = context.getSharedPreferences("discount_prefs", Context.MODE_PRIVATE)

    fun saveDiscountPercentage(type: String, percentage: Float) {
        with(sharedPref.edit()) {
            putFloat(type, percentage)
            apply()
        }
    }

    fun getDiscountPercentage(type: String): Float {
        return sharedPref.getFloat(type, 0f) // 0f is default value if not found
    }
}