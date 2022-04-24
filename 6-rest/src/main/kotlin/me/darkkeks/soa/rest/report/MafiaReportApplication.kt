package me.darkkeks.soa.rest.report

import me.darkkeks.soa.rest.Task
import me.darkkeks.soa.rest.TaskQueueRepository
import me.darkkeks.soa.rest.TaskStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class TasksWorker(
    private val taskQueueRepository: TaskQueueRepository
) {
    fun grabTask(): Task? {
        return taskQueueRepository.findAll().firstOrNull()
    }

    @PostConstruct
    fun process() {
        while (true) {
            try {
                val task = grabTask()
                if (task == null) {
                    Thread.sleep(1000)
                    continue
                }
                processTask(task)
            } catch (e: InterruptedException) {
                return
            }
        }
    }

    fun processTask(task: Task) {
        val readyTask = task.copy(
            status = TaskStatus.READY,
            result = "sample_result",
        )
        taskQueueRepository.save(readyTask)
    }
}

@SpringBootApplication
class MafiaReportApplication

fun main(args: Array<String>) {
    runApplication<MafiaReportApplication>(*args)
}
