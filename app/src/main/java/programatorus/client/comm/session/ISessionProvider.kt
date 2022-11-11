package programatorus.client.comm.session

import programatorus.client.utils.TaskRunner

interface ISessionProvider {
    fun build(
        client: ISessionClient,
        taskRunner: TaskRunner? = null,
        clientTaskRunner: TaskRunner? = null
    ): ISession
}