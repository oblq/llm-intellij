package co.huggingface.llmintellij

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.panels.VerticalLayout
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LlmSettingsComponent {
    val rootPanel: JPanel = JPanel()
    private val apiTokenLabel: JBLabel
    private val apiToken: JBPasswordField
    private val backendTypeLabel: JBLabel
    private val backendType: JComboBox<String>
    private val urlLabel: JBLabel
    private val url: JBTextField
    private val modelLabel: JBLabel
    private val model: JBTextField
    private val tokensToClearLabel: JBLabel
    private val tokensToClear: JBTextField
    private val maxNewTokensLabel: JBLabel
    private val maxNewTokens: JBTextField
    private val temperatureLabel: JBLabel
    private val temperature: JBTextField
    private val topPLabel: JBLabel
    private val topP: JBTextField
    private val stopTokensLabel: JBLabel
    private val stopTokens: JBTextField
    private val fim: JBCheckBox
    private val fimPrefixLabel: JBLabel
    private val fimPrefix: JBTextField
    private val fimMiddleLabel: JBLabel
    private val fimMiddle: JBTextField
    private val fimSuffixLabel: JBLabel
    private val fimSuffix: JBTextField
    private val tlsSkipVerifyInsecure: JBCheckBox
    private val lspBinaryPath: TextFieldWithBrowseButton
    private val lspVersionLabel: JBLabel
    private val lspVersion: JBTextField
    private val lspLogLevelLabel: JBLabel
    private val lspLogLevel: JBTextField
    private val tokenizerConfig: JComboBox<String>
    private val tokenizerConfigLocalPathLabel: JBLabel
    private val tokenizerConfigLocalPath: TextFieldWithBrowseButton
    private val tokenizerConfigHuggingFaceRepositoryLabel: JBLabel
    private val tokenizerConfigHuggingFaceRepository: JBTextField
    private val tokenizerConfigDownloadUrlLabel: JBLabel
    private val tokenizerConfigDownloadUrl: JBTextField
    private val tokenizerConfigDownloadToLabel: JBLabel
    private val tokenizerConfigDownloadTo: JBTextField
    private val contextWindowLabel: JBLabel
    private val contextWindow: JBTextField
    private val enableGhostText: JBCheckBox

    init {
        // Used for all text fields with browse button
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)

        rootPanel.layout = BoxLayout(rootPanel, BoxLayout.Y_AXIS)

        val enableGhostTextPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        enableGhostText = JBCheckBox("Enable ghost text", true)
        enableGhostTextPanel.add(enableGhostText)
        rootPanel.add(enableGhostTextPanel)

        val modelSectionPanel = createSectionPanel("Model", rootPanel)

        apiTokenLabel = JBLabel("API token")
        apiToken = JBPasswordField()
        apiToken.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                val apiTokenValue = String(apiToken.password)
                SecretsService.instance.saveSecretSetting(apiTokenValue)
            }

            override fun removeUpdate(e: DocumentEvent) {
                val apiTokenValue = String(apiToken.password)
                SecretsService.instance.saveSecretSetting(apiTokenValue)
            }

            override fun changedUpdate(e: DocumentEvent) {
                val apiTokenValue = String(apiToken.password)
                SecretsService.instance.saveSecretSetting(apiTokenValue)
            }
        })
        modelSectionPanel.add(apiTokenLabel)
        modelSectionPanel.add(apiToken)
        backendTypeLabel = JBLabel("Backend type")
        backendType = JComboBox(BackendType.entries.map { it.name }.toTypedArray())
        backendType.selectedItem = BackendType.OPENAI.name
        modelSectionPanel.add(backendTypeLabel)
        modelSectionPanel.add(backendType)
        urlLabel = JBLabel("LLM server url")
        url = JBTextField("http://localhost:3715/v1/completions")
        modelSectionPanel.add(urlLabel)
        modelSectionPanel.add(url)
        modelLabel = JBLabel("Model")
        model = JBTextField("bigcode/starcoder2-15b")
        modelSectionPanel.add(modelLabel)
        modelSectionPanel.add(model)
        tokensToClearLabel = JBLabel("Tokens to clear (Comma-separated List)")
        tokensToClear = JBTextField("<|endoftext|>,<file_sep>")
        modelSectionPanel.add(tokensToClearLabel)
        modelSectionPanel.add(tokensToClear)
        tlsSkipVerifyInsecure = JBCheckBox("TLS skip verify insecure", true)
        modelSectionPanel.add(tlsSkipVerifyInsecure)

        val queryParamsSubsectionPanel = createSectionPanel("Query Params", modelSectionPanel)
        maxNewTokensLabel = JBLabel("Max new tokens")
        maxNewTokens = JBTextField("60")
        queryParamsSubsectionPanel.add(maxNewTokensLabel)
        queryParamsSubsectionPanel.add(maxNewTokens)
        temperatureLabel = JBLabel("Temperature")
        temperature = JBTextField("0.2")
        queryParamsSubsectionPanel.add(temperatureLabel)
        queryParamsSubsectionPanel.add(temperature)
        topPLabel = JBLabel("Top P")
        topP = JBTextField("0.95")
        queryParamsSubsectionPanel.add(topPLabel)
        queryParamsSubsectionPanel.add(topP)
        stopTokensLabel = JBLabel("Stop tokens (Comma-separated List)")
        stopTokens = JBTextField()
        queryParamsSubsectionPanel.add(stopTokensLabel)
        queryParamsSubsectionPanel.add(stopTokens)

        val promptSectionPanel = createSectionPanel("Prompt", rootPanel)
        contextWindowLabel = JBLabel("Context window")
        contextWindow = JBTextField("16384")
        promptSectionPanel.add(contextWindowLabel)
        promptSectionPanel.add(contextWindow)

        val fillInTheMiddleSubsectionPanel = createSectionPanel("Fill in the Middle", promptSectionPanel)
        fim = JBCheckBox("Enabled", true)
        fillInTheMiddleSubsectionPanel.add(fim)
        fimPrefixLabel = JBLabel("Prefix")
        fimPrefix = JBTextField("<fim_prefix>")
        fillInTheMiddleSubsectionPanel.add(fimPrefixLabel)
        fillInTheMiddleSubsectionPanel.add(fimPrefix)
        fimMiddleLabel = JBLabel("Middle")
        fimMiddle = JBTextField("<fim_middle>")
        fillInTheMiddleSubsectionPanel.add(fimMiddleLabel)
        fillInTheMiddleSubsectionPanel.add(fimMiddle)
        fimSuffixLabel = JBLabel("Suffix")
        fimSuffix = JBTextField("<fim_suffix>")
        fillInTheMiddleSubsectionPanel.add(fimSuffixLabel)
        fillInTheMiddleSubsectionPanel.add(fimSuffix)

        fim.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                fimPrefixLabel.isEnabled = true
                fimPrefix.isEnabled = true
                fimMiddleLabel.isEnabled = true
                fimMiddle.isEnabled = true
                fimSuffixLabel.isEnabled = true
                fimSuffix.isEnabled = true
            } else {
                fimPrefixLabel.isEnabled = false
                fimPrefix.isEnabled = false
                fimMiddleLabel.isEnabled = false
                fimMiddle.isEnabled = false
                fimSuffixLabel.isEnabled = false
                fimSuffix.isEnabled = false
            }
        }
        val tokenizerSubsectionPanel = createSectionPanel("Tokenizer", promptSectionPanel)
        val tokenizerOptions = arrayOf("Hugging Face", "Local", "Download", "None")
        tokenizerConfig = JComboBox(tokenizerOptions)
        tokenizerSubsectionPanel.add(tokenizerConfig)

        tokenizerConfigLocalPathLabel = JBLabel("Path")
        tokenizerConfigLocalPath = TextFieldWithBrowseButton()
        tokenizerConfigLocalPath.addBrowseFolderListener("Select Path", null, null, descriptor)
        tokenizerConfigLocalPathLabel.isVisible = false
        tokenizerConfigLocalPath.isVisible = false
        tokenizerSubsectionPanel.add(tokenizerConfigLocalPathLabel)
        tokenizerSubsectionPanel.add(tokenizerConfigLocalPath)

        tokenizerConfigHuggingFaceRepositoryLabel = JBLabel("Repository")
        tokenizerConfigHuggingFaceRepository = JBTextField("bigcode/starcoder2-15b")
        tokenizerSubsectionPanel.add(tokenizerConfigHuggingFaceRepositoryLabel)
        tokenizerSubsectionPanel.add(tokenizerConfigHuggingFaceRepository)

        tokenizerConfigDownloadUrlLabel = JBLabel("URL")
        tokenizerConfigDownloadUrl = JBTextField()
        tokenizerConfigDownloadUrlLabel.isVisible = false
        tokenizerConfigDownloadUrl.isVisible = false
        tokenizerSubsectionPanel.add(tokenizerConfigDownloadUrlLabel)
        tokenizerSubsectionPanel.add(tokenizerConfigDownloadUrl)
        tokenizerConfigDownloadToLabel = JBLabel("To")
        tokenizerConfigDownloadTo = JBTextField()
        tokenizerConfigDownloadToLabel.isVisible = false
        tokenizerConfigDownloadTo.isVisible = false
        tokenizerSubsectionPanel.add(tokenizerConfigDownloadToLabel)
        tokenizerSubsectionPanel.add(tokenizerConfigDownloadTo)

        tokenizerConfig.addActionListener { _ ->
            val selectedValue = tokenizerConfig.selectedItem as String
            tokenizerConfigLocalPathLabel.isVisible = selectedValue == "Local"
            tokenizerConfigLocalPath.isVisible = selectedValue == "Local"
            tokenizerConfigHuggingFaceRepositoryLabel.isVisible = selectedValue == "Hugging Face"
            tokenizerConfigHuggingFaceRepository.isVisible = selectedValue == "Hugging Face"
            tokenizerConfigDownloadUrlLabel.isVisible = selectedValue == "Download"
            tokenizerConfigDownloadUrl.isVisible = selectedValue == "Download"
            tokenizerConfigDownloadToLabel.isVisible = selectedValue == "Download"
            tokenizerConfigDownloadTo.isVisible = selectedValue == "Download"
        }

        val llmLsSubsectionPanel = createSectionPanel("llm-ls", rootPanel)
        lspBinaryPath = TextFieldWithBrowseButton()
        lspBinaryPath.addBrowseFolderListener("Select Path", null, null, descriptor)
        val lspBinaryPathLabel = JBLabel("Binary path")
        llmLsSubsectionPanel.add(lspBinaryPathLabel)
        llmLsSubsectionPanel.add(lspBinaryPath)
        lspVersionLabel = JBLabel("Version")
        lspVersion = JBTextField("0.5.2")
        llmLsSubsectionPanel.add(lspVersionLabel)
        llmLsSubsectionPanel.add(lspVersion)
        lspLogLevelLabel = JBLabel("Log level")
        lspLogLevel = JBTextField("warn")
        llmLsSubsectionPanel.add(lspLogLevelLabel)
        llmLsSubsectionPanel.add(lspLogLevel)

    }

    val preferredFocusedComponent: JComponent
        get() = model

    fun isGhostTextEnabled(): Boolean {
        return enableGhostText.isSelected
    }

    fun setGhostTextStatus(enabled: Boolean) {
        enableGhostText.setSelected(enabled)
    }

    fun getBackendType(): BackendType {
        return BackendType.fromString(backendType.selectedItem?.toString()) ?: BackendType.HUGGINGFACE
    }

    fun setBackendType(value: BackendType) {
        backendType.selectedItem = value.name
    }

    fun getLLMServerURL(): String? {
        return if (url.text.length == 0) null else url.text
    }

    fun setLLMServerURL(value: String?) {
        url.text = value ?: ""
    }

    fun getModelId(): String {
        return model.text
    }

    fun setModelId(value: String) {
        model.text = value
    }

    fun getTokensToClear(): String {
        return tokensToClear.text
    }

    fun setTokensToClear(tokens: String) {
        tokensToClear.text = tokens
    }

    fun getMaxNewTokens(): Int? {
        return if (maxNewTokens.text.isEmpty()) null else maxNewTokens.text.toInt()
    }

    fun setMaxNewTokens(value: Int?) {
        maxNewTokens.text = value?.toString() ?: ""
    }

    fun getTemperature(): Float? {
        return if (temperature.text.isEmpty()) null else temperature.text.toFloat()

    }

    fun setTemperature(value: Float?) {
        temperature.text = value?.toString() ?: ""
    }

    fun getTopP(): Float? {
        return if (topP.text.isEmpty()) null else topP.text.toFloat()
    }

    fun setTopP(value: Float?) {
        topP.text = value?.toString() ?: ""
    }

    fun getStopTokens(): List<String>? {
        return if (stopTokens.text.length > 0) {
            stopTokens.text.split(",")
        } else {
            null
        }
    }

    fun setStopTokens(tokens: List<String>?) {
        stopTokens.text = tokens?.joinToString(",") ?: ""
    }

    fun isFimEnabled(): Boolean {
        return fim.isSelected
    }

    fun setFimStatus(enabled: Boolean) {
        fim.setSelected(enabled)
    }

    fun getFimPrefix(): String {
        return fimPrefix.text
    }

    fun setFimPrefix(value: String) {
        fimPrefix.text = value
    }

    fun getFimMiddle(): String {
        return fimMiddle.text
    }

    fun setFimMiddle(value: String) {
        fimMiddle.text = value
    }

    fun getFimSuffix(): String {
        return fimSuffix.text
    }

    fun setFimSuffix(value: String) {
        fimSuffix.text = value
    }

    fun isTlsSkipVerifyInsecureEnabled(): Boolean {
        return tlsSkipVerifyInsecure.isSelected
    }

    fun setTlsSkipVerifyInsecureStatus(enabled: Boolean) {
        tlsSkipVerifyInsecure.setSelected(enabled)
    }

    fun getLspVersion(): String {
        return lspVersion.text
    }

    fun setLspVersion(value: String) {
        lspVersion.text = value
    }

    fun getLspLogLevel(): String {
        return lspLogLevel.text
    }

    fun setLspLogLevel(value: String) {
        lspLogLevel.text = value
    }

    fun getLspBinaryPath(): String? {
        return if (lspBinaryPath.text == "") {
            null
        } else {
            lspBinaryPath.text
        }
    }

    fun setLspBinaryPath(value: String) {
        lspBinaryPath.text = value
    }

    fun getTokenizerConfig(): TokenizerConfig? {
        val type = tokenizerConfig.getItemAt(tokenizerConfig.selectedIndex)
        when (type) {
            "Hugging Face" -> {
                val tokenizerConfig = TokenizerConfig()
                tokenizerConfig.type = type
                tokenizerConfig.repository = tokenizerConfigHuggingFaceRepository.text
                return tokenizerConfig
            }

            "Local" -> {
                val tokenizerConfig = TokenizerConfig()
                tokenizerConfig.type = type
                tokenizerConfig.path = tokenizerConfigLocalPath.text
                return tokenizerConfig
            }

            "Download" -> {
                val tokenizerConfig = TokenizerConfig()
                tokenizerConfig.type = type
                tokenizerConfig.url = tokenizerConfigDownloadUrl.text
                tokenizerConfig.to = tokenizerConfigDownloadTo.text
                return tokenizerConfig
            }

            else -> {
                return null
            }
        }
    }

    fun setTokenizerConfig(value: TokenizerConfig?) {
        when (value?.type) {
            "Hugging Face" -> {
                tokenizerConfig.selectedItem = "Hugging Face"
                tokenizerConfigHuggingFaceRepository.text = value.repository
            }

            "Local" -> {
                tokenizerConfig.selectedItem = "Local"
                tokenizerConfigLocalPath.text = value.path ?: ""
            }

            "Download" -> {
                tokenizerConfig.selectedItem = "Download"
                tokenizerConfigDownloadUrl.text = value.url
                tokenizerConfigDownloadTo.text = value.to
            }

            else -> {
                tokenizerConfig.selectedItem = "None"
            }
        }
    }

    fun getContextWindow(): Int {
        return contextWindow.text.toInt()
    }

    fun setContextWindow(value: Int) {
        contextWindow.text = value.toString()
    }

    private fun createSectionPanel(title: String, parentPanel: JPanel): JPanel {
        val panel = JPanel()
        panel.setLayout(VerticalLayout(5))
        val titledSeparator = TitledSeparator()
        titledSeparator.text = title
        panel.add(titledSeparator)
        parentPanel.add(panel)
        return panel
    }
}
