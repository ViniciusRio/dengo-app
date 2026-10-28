package com.viniciusrio.dengo.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadPresentationTest {
    @Test fun loadingAndErrorKeepTheLastReadyProjectionMarkedAsStale() {
        val old = "última leitura confirmada"

        assertEquals(ReadPresentation(old, ReadNotice.Loading), ScreenReadState.Loading.present(old))
        assertEquals(
            ReadPresentation(old, ReadNotice.Error),
            ScreenReadState.ReadError<String>(IllegalStateException("read"), null).present(old),
        )
        assertEquals(ReadPresentation("atual", null), ScreenReadState.Ready("atual").present(old))
    }

    @Test fun errorPrefersRepositoryLastKnownStateAndInitialLoadingInventsNoData() {
        assertEquals(ReadPresentation<String>(null, ReadNotice.Loading), ScreenReadState.Loading.present(null))
        assertEquals(
            ReadPresentation("último snapshot conhecido", ReadNotice.Error),
            ScreenReadState.ReadError(IllegalStateException("read"), "último snapshot conhecido").present("mais antigo"),
        )
    }
}
