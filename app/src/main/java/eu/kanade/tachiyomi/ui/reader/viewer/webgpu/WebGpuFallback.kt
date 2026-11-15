package eu.kanade.tachiyomi.ui.reader.viewer.webgpu

/**
 * Set once the WebGPU device is lost: the accelerated viewer cannot rebuild it in-process,
 * so the reader stays on the standard viewers until the app restarts.
 */
object WebGpuFallback {
    @Volatile
    var isActive: Boolean = false
        private set

    fun activate() {
        isActive = true
    }
}
