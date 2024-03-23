package co.huggingface.llmintellij.lsp

import co.huggingface.llmintellij.BackendType
import co.huggingface.llmintellij.FimParams
import co.huggingface.llmintellij.QueryParams
import co.huggingface.llmintellij.TokenizerConfig
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

data class Position(
        val line: Int,
        val character: Int
)

class CompletionParams(
        val textDocument: TextDocumentIdentifier,
        val position: Position,
        val requestBody: QueryParams,
        val ide: String = "jetbrains",
        val fim: FimParams,
        val apiToken: String?,
        val model: String,
        val backend: String = BackendType.OPENAI.toString(),
        val url: String?,
        val tokensToClear: List<String>,
        val tokenizerConfig: TokenizerConfig?,
        val contextWindow: UInt,
        val tlsSkipVerifyInsecure: Boolean
)

@JsonSegment("llm-ls")
public interface LlmLsLanguageServer : LanguageServer {
    @JsonRequest
    fun getCompletions(params: CompletionParams): CompletableFuture<CompletionResponse>;
}