package com.bink.wallet.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, R, X> MediatorLiveData<X>.combine(
    source1: LiveData<T>,
    source2: LiveData<R>,
    merger: (T, R) -> X
) {
    addSource(source1) { source ->
        value = merger(
            source,
            source2.value ?: return@addSource
        )
    }
    addSource(source2) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source
        )
    }
}

fun <T, R, L, X> MediatorLiveData<X>.combine(
    source1: LiveData<T>,
    source2: LiveData<R>,
    source3: LiveData<L>,
    merger: (T, R, L) -> X
) {
    addSource(source1) { source ->
        value = merger(
            source,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource
        )
    }
    addSource(source2) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source,
            source3.value ?: return@addSource
        )
    }
    addSource(source3) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source
        )
    }
}

fun <T, R, L, M, X> MediatorLiveData<X>.combine(
    source1: LiveData<T>,
    source2: LiveData<R>,
    source3: LiveData<L>,
    source4: LiveData<M>,
    merger: (T, R, L, M) -> X
) {
    addSource(source1) { source ->
        value = merger(
            source,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource
        )
    }
    addSource(source2) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource
        )
    }
    addSource(source3) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source,
            source4.value ?: return@addSource
        )
    }
    addSource(source4) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source
        )
    }
}

fun <T, R, L, M, N, X> MediatorLiveData<X>.combine(
    source1: LiveData<T>,
    source2: LiveData<R>,
    source3: LiveData<L>,
    source4: LiveData<M>,
    source5: LiveData<N>,
    merger: (T, R, L, M, N) -> X
) {
    addSource(source1) { source ->
        value = merger(
            source,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource,
            source5.value ?: return@addSource
        )
    }
    addSource(source2) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource,
            source5.value ?: return@addSource
        )
    }
    addSource(source3) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source,
            source4.value ?: return@addSource,
            source5.value ?: return@addSource
        )
    }
    addSource(source4) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source,
            source5.value ?: return@addSource
        )
    }
    addSource(source5) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource,
            source
        )
    }
}