package programatorus.client.comm.transport

import android.os.Handler
import programatorus.client.comm.IConnectionBuilder

interface ITransportBuilder<T : ITransportBuilder<T>>: IConnectionBuilder<T>, ITransportProvider {
}