package co.huggingface.llmintellij

import co.huggingface.llmintellij.lsp.CompletionParams
import co.huggingface.llmintellij.lsp.LlmLsGetCompletionsRequest
import co.huggingface.llmintellij.lsp.LlmLsServerSupportProvider
import co.huggingface.llmintellij.lsp.Position
import com.google.gson.Gson
import com.intellij.codeInsight.inline.completion.*
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Computable
import com.intellij.platform.lsp.api.LspServerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

const val debounceMS: Long = 250

class Debouncer(private val delay: Long) {
    private val executor = AppExecutorUtil.createBoundedScheduledExecutorService(
        "EventDebounceScheduler", 1
    )
    private var future: ScheduledFuture<*>? = null

    fun debounce(task: Runnable) {
        future?.cancel(false)
        future = executor.schedule(task, delay, TimeUnit.MILLISECONDS)
    }
}

class LlmLsCompletionProvider : InlineCompletionProvider {
    private val logger = Logger.getInstance("inlineCompletion")
    private val eventDebounceScheduler = Debouncer(debounceMS)
    override val id: InlineCompletionProviderID = InlineCompletionProviderID("LlmLsCompletionProvider")

    override suspend fun getSuggestion(request: InlineCompletionRequest): InlineCompletionSuggestion {
        return InlineCompletionSuggestion.Default(suggestionFlow = channelFlow {
            val project = request.editor.project
            if (project == null) {
                logger.error("could not find project")
            } else {
                val settings = LlmSettingsState.instance
                val secrets = SecretsService.instance
                val lspServer = LspServerManager.getInstance(project).getServersForProvider(LlmLsServerSupportProvider::class.java).firstOrNull()
                logger.info("lspServer instance: $lspServer")
                if (lspServer != null) {
                    val params = ApplicationManager.getApplication().runReadAction(Computable {
                        val textDocument = lspServer.requestExecutor.getDocumentIdentifier(request.file.virtualFile)
                        val caretPosition = request.editor.caretModel.offset
                        val line = request.document.getLineNumber(caretPosition)
                        val column = caretPosition - request.document.getLineStartOffset(line)
                        val position = Position(line = line, character = column)
                        val queryParams = settings.queryParams
                        val fimParams = settings.fim
                        val model = settings.model
                        val tokenizerConfig = settings.tokenizer
                        val tokensToClear = settings.tokensToClear.split(",")
                        val url = settings.url
                        val backend = settings.backendType.toString()
                        val contextWindow = settings.contextWindow
                        val tlsSkipVerifyInsecure = settings.tlsSkipVerifyInsecure
                        CompletionParams(
                                textDocument = textDocument,
                                position = position,
                                requestBody = queryParams,
                                fim = fimParams,
                                apiToken = secrets.getSecretSetting(),
                                model = model,
                                tokensToClear = tokensToClear,
                                tokenizerConfig = tokenizerConfig,
                                url = url,
                                backend = backend,
                                contextWindow = contextWindow,
                                tlsSkipVerifyInsecure = tlsSkipVerifyInsecure,
                        )
                    })

                    val gson = Gson()

                    eventDebounceScheduler.debounce {
                        lspServer.requestExecutor.sendRequestAsync(
                            LlmLsGetCompletionsRequest(
                                lspServer,
                                params
                            )
                        ) { response ->

                            if (settings.lsp.logLevel.lowercase() == "info") {
                                val paramsJson = gson.toJson(params)
                                val responseJson = gson.toJson(response)
                                logger.info("completions for params: $paramsJson: $responseJson")
                            }

                            CoroutineScope(Dispatchers.Default).launch {
                                if (response != null) {
                                    for (completion in response.completions) {
                                        send(InlineCompletionGrayTextElement(completion.generated_text))
                                    }
                                } else {
                                    logger.warn("completions response is null, look for an error in the logs")
                                }
                            }
                        }
                    }

                } else {
                    logger.error("lspServer not initialized!")
                }
            }
            awaitClose()
        })
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        val settings = LlmSettingsState.instance
        return settings.ghostTextEnabled
    }
}