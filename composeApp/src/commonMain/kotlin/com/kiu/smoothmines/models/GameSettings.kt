import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GameSettings {
    var showBorders by mutableStateOf(true)
    var animationSpeed by mutableStateOf(300L)
    var vibrationEnabled by mutableStateOf(true)

    // Сюда можно добавить метод для сохранения в память устройства
}

val globalSettings = GameSettings