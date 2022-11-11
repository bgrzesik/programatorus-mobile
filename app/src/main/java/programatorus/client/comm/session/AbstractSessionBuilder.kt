package programatorus.client.comm.session

import programatorus.client.AbstractConnectionBuilder
import programatorus.client.utils.TaskRunner

abstract class AbstractSessionBuilder<S: AbstractSessionBuilder<S>> : AbstractConnectionBuilder<S>(), ISessionBuilder<S> {

    abstract fun construct(
        client: ISessionClient,
        taskRunner: TaskRunner,
        clientTaskRunner: TaskRunner
    ): ISession

    override fun build(
        client: ISessionClient,
        taskRunner: TaskRunner?,
        clientTaskRunner: TaskRunner?
    ): ISession {
        val runner = mTaskRunner ?: taskRunner?: TaskRunner.constructDefault()
        return construct(
            client,
            runner,
            mClientTaskRunner ?: clientTaskRunner ?: runner
        )
    }
}