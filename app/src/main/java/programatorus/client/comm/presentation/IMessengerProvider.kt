package programatorus.client.comm.presentation

import programatorus.client.utils.TaskRunner

interface IMessengerProvider {

    fun build(
        client: IMessageClient,
        taskRunner: TaskRunner? = null,
        clientTaskRunner: TaskRunner? = null
    ): IMessenger

}