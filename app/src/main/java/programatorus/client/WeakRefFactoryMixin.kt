package programatorus.client

import java.lang.ref.WeakReference

interface WeakRefFactoryMixin<T> {

    fun weakRefFromThis(): WeakReference<T> {
        return WeakReference<T>(this as T)
    }

}