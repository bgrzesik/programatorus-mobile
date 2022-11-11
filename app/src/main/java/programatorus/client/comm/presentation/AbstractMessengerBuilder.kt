package programatorus.client.comm.presentation

import programatorus.client.AbstractConnectionBuilder
import programatorus.client.utils.TaskRunner

abstract class AbstractMessengerBuilder<M : AbstractMessengerBuilder<M>> :
    AbstractConnectionBuilder<M>(), IMessengerBuilder<M> {

    abstract fun construct(
        client: IMessageClient,
        taskRunner: TaskRunner,
        clientTaskRunner: TaskRunner
    ): IMessenger

    override fun build(
        client: IMessageClient,
        taskRunner: TaskRunner?,
        clientTaskRunner: TaskRunner?
    ): IMessenger {
        val runner = mTaskRunner ?: taskRunner ?: TaskRunner.constructDefault()
        return construct(
            client,
            runner,
            mClientTaskRunner ?: clientTaskRunner ?: runner
        )
    }
}