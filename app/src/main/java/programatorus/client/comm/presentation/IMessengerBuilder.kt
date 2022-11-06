package programatorus.client.comm.presentation

import programatorus.client.comm.IConnectionBuilder

interface IMessengerBuilder<M: IMessengerBuilder<M>>: IConnectionBuilder<M>, IMessengerProvider {
}