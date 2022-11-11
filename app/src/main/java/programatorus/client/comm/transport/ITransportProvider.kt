package programatorus.client.comm.transport

import programatorus.client.utils.TaskRunner

interface ITransportProvider {

    fun build(
        client: ITransportClient,
        taskRunner: TaskRunner? = null,
        clientTaskRunner: TaskRunner? = null
    ): ITransport

}
