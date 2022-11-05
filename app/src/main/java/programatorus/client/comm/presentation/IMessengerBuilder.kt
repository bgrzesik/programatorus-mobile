package programatorus.client.comm.presentation

import android.os.Handler
import programatorus.client.comm.IConnectionBuilder

interface IMessengerBuilder<M: IMessengerBuilder<M>>: IConnectionBuilder<M>, IMessengerProvider {
}