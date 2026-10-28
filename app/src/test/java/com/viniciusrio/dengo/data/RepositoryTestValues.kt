package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.ui.ScreenReadState

fun FakeCoupleRepository.snapshot(): CoupleState = (state.value as RepositoryReadState.Ready).value

fun <T> WriteResult<T>.confirmed(): T = (this as WriteResult.Confirmed).value

fun <T> ScreenReadState<T>.ready(): T = (this as ScreenReadState.Ready).value
