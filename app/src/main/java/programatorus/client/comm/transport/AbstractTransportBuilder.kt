package programatorus.client.comm.transport

import programatorus.client.AbstractConnectionBuilder
import programatorus.client.utils.TaskRunner

abstract class AbstractTransportBuilder<T: AbstractTransportBuilder<T>> : AbstractConnectionBuilder<T>(), ITransportBuilder<T> {

    abstract fun construct(
        client: ITransportClient,
        taskRunner: TaskRunner,
        clientTaskRunner: TaskRunner
    ): ITransport

    override fun build(
        client: ITransportClient,
        taskRunner: TaskRunner?,
        clientTaskRunner: TaskRunner?
    ): ITransport {
        val runner = mTaskRunner ?: taskRunner ?: TaskRunner.constructDefault()
        return construct(
            client,
            runner,
            mClientTaskRunner ?: clientTaskRunner ?: runner
        )
    }
}