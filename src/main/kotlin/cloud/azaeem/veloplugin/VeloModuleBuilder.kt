package cloud.azaeem.veloplugin

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class VeloModuleBuilder : ModuleBuilder() {
    private var projectName: String = "my_app"
    private var orgName: String = "com.company"
    private var velocliPath: String = ""
    private var apiUrl: String = "http://127.0.0.1:8082"

    override fun getBuilderId() = "cloud.azaeem.veloplugin.VeloModuleBuilder"
    override fun getPresentableName() = "VeloCLI Flutter Project"
    override fun getDescription() = "Create a new Flutter project using VeloCLI"
    override fun getWeight() = 2000 // Show near top if possible
    override fun getNodeIcon(): Icon = IconLoader.getIcon("/META-INF/pluginIcon.svg", VeloModuleBuilder::class.java)

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val contentEntry = doAddContentEntry(modifiableRootModel)
        val outputDir = contentEntry?.file?.path ?: return
        if (velocliPath.isEmpty()) {
            velocliPath = findVeloCli()
        }

        if (velocliPath.isEmpty()) {
             throw ConfigurationException("VeloCLI binary not found. Please set VELOCLI_BIN environment variable.")
        }

        try {
            val cmd = GeneralCommandLine(velocliPath)
            cmd.addParameter("create")
            cmd.addParameter("--project-name")
            cmd.addParameter(projectName)
            cmd.addParameter("--org")
            cmd.addParameter(orgName)
            cmd.addParameter("--output-dir")
            cmd.addParameter(File(outputDir).parent)
            cmd.addParameter("--api-url")
            cmd.addParameter(apiUrl)
            cmd.addParameter("--ide")
            cmd.addParameter("none") // Don't open another IDE
            
            val output = ExecUtil.execAndGetOutput(cmd)
            if (output.exitCode != 0) {
                 throw ConfigurationException("VeloCLI failed: ${output.stderr}")
            }
            modifiableRootModel.project.baseDir.refresh(false, true)
            
        } catch (e: Exception) {
            throw ConfigurationException("Failed to run VeloCLI: ${e.message}")
        }
    }

    override fun getModuleType(): ModuleType<*> {
        return com.intellij.openapi.module.ModuleType.EMPTY
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep? {
        return VeloWizardStep(this)
    }

    private fun findVeloCli(): String {
        val envBin = System.getenv("VELOCLI_BIN")?.trim().orEmpty()
        if (envBin.isNotEmpty()) return envBin
        val paths = listOf(
            "/usr/local/bin/velocli",
            "/opt/homebrew/bin/velocli",
            System.getProperty("user.home") + "/.local/bin/velocli"
        )
        for (p in paths) {
            if (File(p).exists()) return p
        }

        return ""
    }
    class VeloWizardStep(private val builder: VeloModuleBuilder) : ModuleWizardStep() {
        private val panel = JPanel(GridBagLayout())
        private val projectNameField = JBTextField(builder.projectName)
        private val orgField = JBTextField(builder.orgName)
        private val apiUrlField = JBTextField(builder.apiUrl)
        
        override fun getComponent(): JComponent {
             val c = GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                insets = Insets(6, 8, 6, 8)
                weightx = 1.0
                gridx = 0
                gridy = 0
            }
            
            addRow("Project Name (snake_case):", projectNameField, c)
            addRow("Organization (com.example):", orgField, c)
            addRow("API URL:", apiUrlField, c)
            c.gridy++
            c.weighty = 1.0
            panel.add(JPanel(), c)
            
            return panel
        }
        
        private fun addRow(label: String, comp: JComponent, c: GridBagConstraints) {
            c.gridx = 0
            c.weightx = 0.0
            panel.add(JLabel(label), c)
            c.gridx = 1
            c.weightx = 1.0
            panel.add(comp, c)
            c.gridy++
        }

        override fun updateDataModel() {
            builder.projectName = projectNameField.text.trim()
            builder.orgName = orgField.text.trim()
            builder.apiUrl = apiUrlField.text.trim()
        }
    }
}
