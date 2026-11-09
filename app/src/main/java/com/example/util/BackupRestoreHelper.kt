package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.ProjectRepository
import com.example.model.Customer
import com.example.model.FurnitureProject
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppBackupData(
    val version: Int = 1,
    val appName: String = "Gaurav Furniture Pro",
    val exportedAt: Long = System.currentTimeMillis(),
    val customers: List<Customer> = emptyList(),
    val projects: List<FurnitureProject> = emptyList()
)

object BackupRestoreHelper {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(AppBackupData::class.java).indent("  ")

    suspend fun createBackupJson(repository: ProjectRepository): String {
        val customers = repository.allCustomers.first()
        val projects = repository.allProjects.first()
        val backup = AppBackupData(
            customers = customers,
            projects = projects
        )
        return adapter.toJson(backup)
    }

    suspend fun restoreFromJson(repository: ProjectRepository, jsonString: String): Boolean {
        return try {
            val backup = adapter.fromJson(jsonString) ?: return false
            backup.customers.forEach { customer ->
                repository.insertCustomer(customer.copy(id = 0))
            }
            backup.projects.forEach { project ->
                repository.insertProject(project.copy(id = 0))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareBackupFile(context: Context, jsonString: String) {
        try {
            val outputDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(outputDir, "GauravFurniturePro_Backup_$timeStamp.json")
            FileOutputStream(file).use { out ->
                out.write(jsonString.toByteArray())
            }

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Gaurav Furniture Pro Backup")
                putExtra(Intent.EXTRA_TEXT, "Backup of Gaurav Furniture Pro database created on $timeStamp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Database JSON Backup"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
