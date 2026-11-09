package com.example.data

import com.example.model.Customer
import com.example.model.FurnitureProject
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val customerDao: CustomerDao
) {
    val allProjects: Flow<List<FurnitureProject>> = projectDao.getAllProjects()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    suspend fun getProjectById(id: Long): FurnitureProject? {
        return projectDao.getProjectById(id)
    }

    suspend fun insertProject(project: FurnitureProject): Long {
        return projectDao.insertProject(project)
    }

    suspend fun updateProject(project: FurnitureProject) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: FurnitureProject) {
        projectDao.deleteProject(project)
    }

    suspend fun duplicateProject(project: FurnitureProject): Long {
        val duplicated = project.copy(
            id = 0,
            projectName = "${project.projectName} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return projectDao.insertProject(duplicated)
    }

    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun checkAndSeedIfEmpty() {
        if (projectDao.getProjectsCount() == 0) {
            val converters = Converters()
            SampleData.getSampleCustomers().forEach { customerDao.insertCustomer(it) }
            SampleData.getSampleProjects(converters).forEach { projectDao.insertProject(it) }
        }
    }
}
