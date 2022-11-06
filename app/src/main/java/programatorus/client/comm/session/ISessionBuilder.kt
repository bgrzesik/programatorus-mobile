package programatorus.client.comm.session

import programatorus.client.comm.IConnectionBuilder

interface ISessionBuilder<S: ISessionBuilder<S>> : IConnectionBuilder<S>, ISessionProvider {
}