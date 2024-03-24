package co.huggingface.llmintellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


class LspSettings {
    var binaryPath: String? = null
    var version: String = "0.5.2"
    var logLevel: String = "warn"
}

class FimParams(
    var enabled: Boolean = true,
    var prefix: String = "<fim_prefix>",
    var middle: String = "<fim_middle>",
    var suffix: String = "<fim_suffix>",
)

class QueryParams(
    var max_new_tokens: Int? = null,
    var temperature: Float? = null,
    var do_sample: Boolean = temperature != null && temperature > 0.2,
    var top_p: Float? = 0.95f,
    var stop_tokens: List<String>? = listOf("<|endoftext|>", "<file_sep>")
)

enum class BackendType {
    HUGGINGFACE,
    OLLAMA,
    OPENAI,
    TGI;

    override fun toString(): String {
        return name.lowercase()
    }

    companion object {
        fun fromString(name: String?): BackendType? {
            if (name == null) {
                return null
            }
            return try {
                valueOf(name.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

class TokenizerConfig {
    var type: String? = null
    var path: String? = null
    var repository: String? = null
    var url: String? = null
    var to: String? = null
}

@State(
    name = "co.huggingface.llmintellij.LlmSettingsState",
    storages = [Storage("LlmSettingsPlugin.xml")]
)
class LlmSettingsState : PersistentStateComponent<LlmSettingsState?> {
    var ghostTextEnabled = true
    var backendType: BackendType = BackendType.OPENAI
    var url: String? = "http://localhost:3715/v1/completions"
    var model: String = "bigcode/starcoder2-15b"
    var tokensToClear: String = "<|endoftext|>,<file_sep>"
    var queryParams = QueryParams()
    var fim = FimParams()
    var tlsSkipVerifyInsecure = true
    var lsp = LspSettings()
    var tokenizer: TokenizerConfig? = TokenizerConfig().apply { repository = "bigcode/starcoder2-15b" }
    var contextWindow: Int = 16384

    override fun getState(): LlmSettingsState {
        return this
    }

    override fun loadState(state: LlmSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: LlmSettingsState
            get() = ApplicationManager.getApplication().getService(LlmSettingsState::class.java)
    }
}
