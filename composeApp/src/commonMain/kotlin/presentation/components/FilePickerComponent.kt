package presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker

@Composable
fun SingleFilePicker(
    show: Boolean,
    fileExtensions: List<String> = emptyList(),
    onFileSelected: (MPFile<Any>?) -> Unit
) {
    FilePicker(
        show = show,
        fileExtensions = fileExtensions
    ) { platformFile ->
        onFileSelected(platformFile)
    }
}

@Composable
fun MultipleFilesPicker(
    show: Boolean,
    fileExtensions: List<String> = emptyList(),
    onFilesSelected: (List<MPFile<Any>>?) -> Unit
) {
    MultipleFilePicker(
        show = show,
        fileExtensions = fileExtensions
    ) { files ->
        onFilesSelected(files)
    }
}

@Composable
fun DirectoryPickerComponent(
    show: Boolean,
    onDirectorySelected: (String?) -> Unit
) {
    DirectoryPicker(
        show = show
    ) { path ->
        onDirectorySelected(path)
    }
} 