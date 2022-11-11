package programatorus.client.comm

import programatorus.client.utils.TaskRunner

interface IConnectionBuilder<B: IConnectionBuilder<B>> {
    fun setTaskRunner(taskRunner: TaskRunner): B

    fun setClientTaskRunner(taskRunner: TaskRunner): B
}