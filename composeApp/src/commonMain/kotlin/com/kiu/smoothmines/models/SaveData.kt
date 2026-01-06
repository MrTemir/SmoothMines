
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig


data class SaveData(
    val config: GameConfig,
    val cells: List<Cell>,
    val timeSeconds: Int,
    val date: String
)