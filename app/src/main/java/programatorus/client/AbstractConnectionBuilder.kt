package programatorus.client

import android.os.Handler
import programatorus.client.comm.IConnectionBuilder
import programatorus.client.utils.TaskRunner

abstract class AbstractConnectionBuilder<B : AbstractConnectionBuilder<B>> : IConnectionBuilder<B> {

    var mTaskRunner: TaskRunner? = null
    var mClientTaskRunner: TaskRunner? = null

    override fun setTaskRunner(taskRunner: TaskRunner): B {
        mTaskRunner = taskRunner
        return this as B
    }

    override fun setClientTaskRunner(taskRunner: TaskRunner): B {
        mClientTaskRunner = taskRunner
        return this as B
    }

}