import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("NoteCraftPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveNote(id: String, note: Note) {
        val noteJson = gson.toJson(note)
        val editor = sharedPreferences.edit()
        editor.putString(id, noteJson)
        editor.apply()
    }

    fun getNote(id: String): Note? {
        val noteJson = sharedPreferences.getString(id, null)
        return if (noteJson != null) {
            gson.fromJson(noteJson, Note::class.java)
        } else {
            null
        }
    }

    fun deleteNote(id: String) {
        val editor = sharedPreferences.edit()
        editor.remove(id)
        editor.apply()
    }
}
