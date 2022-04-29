package me.darkkeks.soa.rest.report

import me.darkkeks.soa.common.model.Task
import me.darkkeks.soa.common.TaskQueueRepository
import me.darkkeks.soa.common.model.TaskStatus
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

@Component
class TasksWorker(
    private val taskQueueRepository: TaskQueueRepository,
) {
    private val delay = Duration.ofSeconds(1)

    @PostConstruct
    fun process() {
        while (true) {
            try {
                val task = grabTask()
                if (task == null) {
                    Thread.sleep(delay.toMillis())
                    continue
                }
                processTask(task)
            } catch (e: InterruptedException) {
                return
            }
        }
    }

    fun grabTask(): Task? {
        return taskQueueRepository.findFirstByStatusOrderByTaskId(TaskStatus.NEW)
    }

    fun processTask(task: Task) {
        val readyTask = task.copy(
            status = TaskStatus.READY,
            result = "sample_result",
        )
        taskQueueRepository.save(readyTask)
    }
}
